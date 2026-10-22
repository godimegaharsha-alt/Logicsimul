package com.example.logiclab.ai

import com.example.BuildConfig
import com.example.logiclab.data.CircuitData
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LabAssistantService {

    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isApiKeyConfigured(): Boolean {
        return BuildConfig.GEMINI_API_KEY.isNotBlank() &&
                BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"
    }

    private suspend fun callGemini(prompt: String, systemInstruction: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyConfigured()) {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Configure GEMINI_API_KEY in the AI Studio Secrets panel.")
            )
        }

        try {
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
            val jsonRoot = JSONObject()

            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val sysParts = JSONArray()
                sysParts.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", sysParts)
                jsonRoot.put("systemInstruction", sysObj)
            }

            val contents = JSONArray()
            val contentObj = JSONObject()
            val parts = JSONArray()
            parts.put(JSONObject().put("text", prompt))
            contentObj.put("parts", parts)
            contents.put(contentObj)
            jsonRoot.put("contents", contents)

            val requestBody = jsonRoot.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error (${response.code}): $body"))
            }

            val respJson = JSONObject(body)
            val candidates = respJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val respParts = content?.optJSONArray("parts")
            val text = respParts?.optJSONObject(0)?.optString("text")

            if (text != null) {
                Result.success(text)
            } else {
                Result.failure(Exception("No content returned from Gemini"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Serializes actual simulator circuit state into structured text for Gemini.
     */
    fun buildCircuitContext(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        selectedComponentId: String? = null
    ): String {
        val sb = StringBuilder()
        sb.append("CURRENT CIRCUIT STATE:\n")
        sb.append("Components (${components.size}):\n")
        for (c in components) {
            val isSelected = c.id == selectedComponentId
            val marker = if (isSelected) " [SELECTED]" else ""
            sb.append("• ${c.id} (${c.type}): label='${c.label}'$marker\n")
            sb.append("   Inputs: ${c.inputValues.entries.joinToString { "${it.key}=${it.value.symbol}" }}\n")
            sb.append("   Outputs: ${c.outputValues.entries.joinToString { "${it.key}=${it.value.symbol}" }}\n")
            if (c.type == "SWITCH") sb.append("   Switch State: ${if (c.switchState) "ON (1)" else "OFF (0)"}\n")
        }
        sb.append("\nConnections (${wires.size}):\n")
        for (w in wires) {
            sb.append("• ${w.fromComponentId}.${w.fromPortId} -> ${w.toComponentId}.${w.toPortId} (Signal: ${w.state.symbol})\n")
        }
        return sb.toString()
    }

    suspend fun explainCircuit(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        selectedComponentId: String?
    ): String {
        val circuitContext = buildCircuitContext(components, wires, selectedComponentId)
        val systemPrompt = "You are a senior digital electronics professor and laboratory instructor for LogicLab. Be concise, technical, precise, and analytical. Base your response purely on the actual circuit topology and live signal states provided."
        val prompt = "$circuitContext\n\nPlease explain this digital circuit, how signals propagate from inputs to outputs, and what functional operation is being performed."

        val result = callGemini(prompt, systemPrompt)
        return result.getOrElse {
            // Built-in rule-based expert explanation fallback
            buildFallbackCircuitExplanation(components, wires, selectedComponentId)
        }
    }

    suspend fun explainWhyOutputState(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        targetComponentId: String,
        targetPortId: String
    ): String {
        val targetComp = components.find { it.id == targetComponentId }
        val state = targetComp?.outputValues?.get(targetPortId) ?: targetComp?.inputValues?.get(targetPortId) ?: DigitalState.UNKNOWN
        val circuitContext = buildCircuitContext(components, wires, targetComponentId)

        val systemPrompt = "You are a digital electronics lab tutor. Explain why a specific pin has its current logic state by tracing backwards through the Boolean gates to the input switches."
        val prompt = "$circuitContext\n\nExplain why pin '$targetPortId' of component '$targetComponentId' (${targetComp?.type}) has logic state ${state.symbol} (${if (state == DigitalState.HIGH) "HIGH" else if (state == DigitalState.LOW) "LOW" else "UNKNOWN/FLOATING"}). Trace the path from the inputs."

        val result = callGemini(prompt, systemPrompt)
        return result.getOrElse {
            // Built-in deterministic trace fallback
            buildFallbackPinTrace(components, wires, targetComp, targetPortId, state)
        }
    }

    suspend fun buildCircuitFromDescription(description: String): Result<CircuitData> {
        val validTypes = ComponentRegistry.definitions.keys.joinToString(", ")
        val systemPrompt = """
            You are a digital circuit synthesis engine. Convert user natural-language requests into structured digital circuit schematics.
            Allowed component types ONLY: $validTypes.
            Return STRICTLY valid JSON with format:
            {
              "components": [
                { "id": "SW1", "type": "SWITCH", "label": "A", "x": 100, "y": 120, "switchState": false },
                { "id": "U1", "type": "AND", "label": "AND Gate", "x": 300, "y": 150 },
                { "id": "LED1", "type": "LED", "label": "OUT", "x": 500, "y": 150 }
              ],
              "connections": [
                { "id": "w1", "fromComponentId": "SW1", "fromPortId": "OUT", "toComponentId": "U1", "toPortId": "A" }
              ]
            }
            Do not include markdown backticks or commentary. Only raw JSON.
        """.trimIndent()

        val prompt = "Create circuit for: $description"
        val geminiResult = callGemini(prompt, systemPrompt)

        if (geminiResult.isSuccess) {
            val text = geminiResult.getOrThrow()
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()
            try {
                val data = validateAndParseGeneratedCircuit(text)
                return Result.success(data)
            } catch (e: Exception) {
                // fall through to rule-based parser
            }
        }

        // Rule-based fallback synthesis for common requests
        val fallback = synthesizeRuleBased(description)
        return if (fallback != null) {
            Result.success(fallback)
        } else {
            Result.failure(Exception(geminiResult.exceptionOrNull()?.message ?: "Could not synthesize circuit."))
        }
    }

    private fun validateAndParseGeneratedCircuit(jsonText: String): CircuitData {
        val root = JSONObject(jsonText)
        val compArr = root.getJSONArray("components")
        val components = mutableListOf<CircuitComponent>()

        for (i in 0 until compArr.length()) {
            val obj = compArr.getJSONObject(i)
            val type = obj.getString("type").uppercase()
            val def = ComponentRegistry.get(type) ?: continue
            val id = obj.optString("id", "U${i + 1}")
            val label = obj.optString("label", def.name)
            val x = obj.optDouble("x", 100.0 + (i % 3) * 180.0).toFloat()
            val y = obj.optDouble("y", 100.0 + (i / 3) * 120.0).toFloat()
            val swState = obj.optBoolean("switchState", false)

            components.add(
                CircuitComponent(
                    id = id,
                    type = type,
                    x = x,
                    y = y,
                    label = label,
                    switchState = swState
                )
            )
        }

        val wireArr = root.optJSONArray("connections") ?: JSONArray()
        val wires = mutableListOf<CircuitWire>()
        val compIds = components.map { it.id }.toSet()

        for (i in 0 until wireArr.length()) {
            val obj = wireArr.getJSONObject(i)
            val fromId = obj.getString("fromComponentId")
            val fromPort = obj.getString("fromPortId")
            val toId = obj.getString("toComponentId")
            val toPort = obj.getString("toPortId")

            if (fromId in compIds && toId in compIds) {
                wires.add(
                    CircuitWire(
                        id = "w_$i",
                        fromComponentId = fromId,
                        fromPortId = fromPort,
                        toComponentId = toId,
                        toPortId = toPort
                    )
                )
            }
        }

        return CircuitData(components, wires)
    }

    private fun synthesizeRuleBased(description: String): CircuitData? {
        val lower = description.lowercase()
        return when {
            lower.contains("mux") || lower.contains("multiplexer") -> {
                val comps = listOf(
                    CircuitComponent("SW_D0", "SWITCH", 60f, 80f, label = "D0"),
                    CircuitComponent("SW_D1", "SWITCH", 60f, 150f, label = "D1", switchState = true),
                    CircuitComponent("SW_D2", "SWITCH", 60f, 220f, label = "D2"),
                    CircuitComponent("SW_D3", "SWITCH", 60f, 290f, label = "D3", switchState = true),
                    CircuitComponent("SW_S0", "SWITCH", 60f, 370f, label = "S0"),
                    CircuitComponent("SW_S1", "SWITCH", 60f, 440f, label = "S1"),
                    CircuitComponent("MUX1", "MUX_4_1", 280f, 200f, label = "4:1 MUX"),
                    CircuitComponent("LED1", "LED", 500f, 220f, label = "Output")
                )
                val wires = listOf(
                    CircuitWire("w1", "SW_D0", "OUT", "MUX1", "D0"),
                    CircuitWire("w2", "SW_D1", "OUT", "MUX1", "D1"),
                    CircuitWire("w3", "SW_D2", "OUT", "MUX1", "D2"),
                    CircuitWire("w4", "SW_D3", "OUT", "MUX1", "D3"),
                    CircuitWire("w5", "SW_S0", "OUT", "MUX1", "S0"),
                    CircuitWire("w6", "SW_S1", "OUT", "MUX1", "S1"),
                    CircuitWire("w7", "MUX1", "Y", "LED1", "IN")
                )
                CircuitData(comps, wires)
            }
            lower.contains("adder") -> {
                val comps = listOf(
                    CircuitComponent("SW_A", "SWITCH", 80f, 100f, label = "A", switchState = true),
                    CircuitComponent("SW_B", "SWITCH", 80f, 200f, label = "B", switchState = true),
                    CircuitComponent("HA1", "HALF_ADDER", 280f, 150f, label = "Half Adder"),
                    CircuitComponent("LED_S", "LED", 500f, 130f, label = "SUM"),
                    CircuitComponent("LED_C", "LED", 500f, 210f, label = "CARRY")
                )
                val wires = listOf(
                    CircuitWire("w1", "SW_A", "OUT", "HA1", "A"),
                    CircuitWire("w2", "SW_B", "OUT", "HA1", "B"),
                    CircuitWire("w3", "HA1", "SUM", "LED_S", "IN"),
                    CircuitWire("w4", "HA1", "CARRY", "LED_C", "IN")
                )
                CircuitData(comps, wires)
            }
            else -> {
                val comps = listOf(
                    CircuitComponent("SW_A", "SWITCH", 80f, 120f, label = "Input A", switchState = true),
                    CircuitComponent("SW_B", "SWITCH", 80f, 220f, label = "Input B", switchState = true),
                    CircuitComponent("U1", "AND", 280f, 170f, label = "AND Gate"),
                    CircuitComponent("LED1", "LED", 480f, 175f, label = "Output LED")
                )
                val wires = listOf(
                    CircuitWire("w1", "SW_A", "OUT", "U1", "A"),
                    CircuitWire("w2", "SW_B", "OUT", "U1", "B"),
                    CircuitWire("w3", "U1", "Y", "LED1", "IN")
                )
                CircuitData(comps, wires)
            }
        }
    }

    private fun buildFallbackCircuitExplanation(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        selectedId: String?
    ): String {
        val sb = StringBuilder()
        sb.append("Circuit Analysis:\n\n")
        sb.append("• Total Components: ${components.size}\n")
        sb.append("• Interconnections: ${wires.size}\n\n")

        val drivers = components.filter { it.type == "SWITCH" || it.type == "CLOCK" }
        if (drivers.isNotEmpty()) {
            sb.append("Inputs:\n")
            for (d in drivers) {
                sb.append("  - ${d.label.ifBlank { d.id }}: ${if (d.switchState) "HIGH (1)" else "LOW (0)"}\n")
            }
            sb.append("\n")
        }

        val logicBlocks = components.filter { it.type !in listOf("SWITCH", "LED", "CLOCK", "DIGITAL_PROBE") }
        if (logicBlocks.isNotEmpty()) {
            sb.append("Logic Operations:\n")
            for (b in logicBlocks) {
                val def = ComponentRegistry.get(b.type)
                sb.append("  - ${b.label.ifBlank { b.id }} (${b.type}): ${def?.booleanFormula ?: ""}\n")
                sb.append("    Inputs: ${b.inputValues.entries.joinToString { "${it.key}=${it.value.symbol}" }}\n")
                sb.append("    Outputs: ${b.outputValues.entries.joinToString { "${it.key}=${it.value.symbol}" }}\n")
            }
            sb.append("\n")
        }

        val leds = components.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }
        if (leds.isNotEmpty()) {
            sb.append("Observed Outputs:\n")
            for (led in leds) {
                val st = led.inputValues["IN"] ?: DigitalState.LOW
                sb.append("  - ${led.label.ifBlank { led.id }}: ${st.symbol} (${if (st.isHigh()) "HIGH / ON" else "LOW / OFF"})\n")
            }
        }
        return sb.toString()
    }

    private fun buildFallbackPinTrace(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        targetComp: CircuitComponent?,
        targetPortId: String,
        state: DigitalState
    ): String {
        if (targetComp == null) return "Component not found."
        val def = ComponentRegistry.get(targetComp.type)
        val sb = StringBuilder()
        sb.append("Pin '$targetPortId' of ${targetComp.label.ifBlank { targetComp.id }} (${targetComp.type}) is currently ${state.symbol} (${if (state.isHigh()) "HIGH" else "LOW"}).\n\n")
        sb.append("Cause Analysis:\n")

        when (targetComp.type) {
            "AND" -> {
                val a = targetComp.inputValues["A"] ?: DigitalState.UNKNOWN
                val b = targetComp.inputValues["B"] ?: DigitalState.UNKNOWN
                if (state.isHigh()) {
                    sb.append("Both inputs A ($a) and B ($b) are HIGH, satisfying Y = A · B.")
                } else {
                    sb.append("At least one input is LOW (A=$a, B=$b). An AND gate requires all inputs to be HIGH to assert HIGH.")
                }
            }
            "OR" -> {
                val a = targetComp.inputValues["A"] ?: DigitalState.UNKNOWN
                val b = targetComp.inputValues["B"] ?: DigitalState.UNKNOWN
                if (state.isHigh()) {
                    sb.append("At least one input is HIGH (A=$a, B=$b), satisfying Y = A + B.")
                } else {
                    sb.append("Both inputs A ($a) and B ($b) are LOW. An OR gate asserts HIGH when any input is 1.")
                }
            }
            "NOT" -> {
                val a = targetComp.inputValues["A"] ?: DigitalState.UNKNOWN
                sb.append("Input A is ${a.symbol}. The inverter produces the inverted output Y = A' = ${state.symbol}.")
            }
            "MUX_4_1" -> {
                val s0 = targetComp.inputValues["S0"] ?: DigitalState.UNKNOWN
                val s1 = targetComp.inputValues["S1"] ?: DigitalState.UNKNOWN
                val sel = (if (s0 == DigitalState.HIGH) 1 else 0) + (if (s1 == DigitalState.HIGH) 2 else 0)
                val selectedD = targetComp.inputValues["D$sel"] ?: DigitalState.UNKNOWN
                sb.append("Address lines S1=${s1.symbol}, S0=${s0.symbol} select channel D$sel.\n")
                sb.append("Input line D$sel is currently ${selectedD.symbol}, so output Y passes ${selectedD.symbol}.")
            }
            else -> {
                sb.append("Boolean formula: ${def?.booleanFormula ?: ""}\n")
                sb.append("Current inputs: ${targetComp.inputValues.entries.joinToString { "${it.key}=${it.value.symbol}" }}")
            }
        }
        return sb.toString()
    }
}
