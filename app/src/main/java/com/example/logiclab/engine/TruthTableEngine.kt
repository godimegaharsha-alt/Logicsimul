package com.example.logiclab.engine

import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState

data class TruthTableRow(
    val rowNumber: Int,
    val inputValues: Map<String, DigitalState>,
    val outputValues: Map<String, DigitalState>,
    val isCurrent: Boolean = false,
    val isMinTerm: Boolean = false
)

data class OutputFormula(
    val outputName: String,
    val canonicalSOP: String,
    val minTerms: List<Int>,
    val maxTerms: List<Int>,
    val simplifiedExpression: String
)

enum class TruthTableMode {
    CIRCUIT,
    SELECTED_COMPONENT
}

data class CircuitInputInfo(
    val id: String,
    val displayName: String,
    val componentId: String,
    val portId: String?,
    val isSwitch: Boolean
)

data class CircuitOutputInfo(
    val id: String,
    val displayName: String,
    val componentId: String,
    val portId: String?,
    val isProbeOrLed: Boolean
)

data class TruthTableData(
    val title: String,
    val inputHeaders: List<String>,
    val outputHeaders: List<String>,
    val rows: List<TruthTableRow>,
    val formulas: List<OutputFormula> = emptyList(),
    val totalCombinations: Int = 0,
    val mode: TruthTableMode = TruthTableMode.CIRCUIT,
    val inputComponents: List<CircuitInputInfo> = emptyList(),
    val isTruncated: Boolean = false,
    val summaryMessage: String = ""
)

object TruthTableEngine {

    private const val MAX_INPUTS_EVALUATED = 7 // Up to 128 rows for fast real-time 60fps evaluation

    /**
     * Automatically analyzes the entire circuit schematic:
     * 1. Detects primary inputs (Switches, Clocks, and any open/unconnected input pins).
     * 2. Detects primary outputs (LEDs, Probes, and any terminal output pins).
     * 3. Generates all 2^N input permutations.
     * 4. Evaluates the full circuit simulation for each permutation.
     * 5. Computes canonical Sum-Of-Products (SOP) boolean formulas and min-terms.
     */
    fun generateForCircuit(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>
    ): TruthTableData {
        if (components.isEmpty()) {
            return TruthTableData(
                title = "Empty Circuit",
                inputHeaders = emptyList(),
                outputHeaders = emptyList(),
                rows = emptyList(),
                summaryMessage = "No components placed on canvas."
            )
        }

        // 1. Discover Primary Inputs
        val discoveredInputs = mutableListOf<CircuitInputInfo>()

        // 1a. Explicit SWITCH inputs
        val switches = components.filter { it.type == "SWITCH" }
        for (sw in switches) {
            val label = sw.label.ifBlank { "SW_${sw.id.takeLast(3)}" }
            discoveredInputs.add(
                CircuitInputInfo(
                    id = sw.id,
                    displayName = label,
                    componentId = sw.id,
                    portId = null,
                    isSwitch = true
                )
            )
        }

        // 1b. If no switches, discover open unconnected gate input ports
        if (discoveredInputs.isEmpty()) {
            for (comp in components) {
                if (comp.type in listOf("LED", "DIGITAL_PROBE", "CONST_HIGH", "CONST_LOW")) continue
                val def = ComponentRegistry.get(comp.type) ?: continue
                for (port in def.inputs) {
                    val isConnected = wires.any {
                        (it.fromComponentId == comp.id && it.fromPortId == port.id) ||
                        (it.toComponentId == comp.id && it.toPortId == port.id)
                    }
                    if (!isConnected) {
                        val compName = comp.label.ifBlank { def.name }
                        val name = if (components.size == 1) port.name else "${compName}.${port.name}"
                        discoveredInputs.add(
                            CircuitInputInfo(
                                id = "${comp.id}_${port.id}",
                                displayName = name,
                                componentId = comp.id,
                                portId = port.id,
                                isSwitch = false
                            )
                        )
                    }
                }
            }
        }

        // 2. Discover Primary Outputs
        val discoveredOutputs = mutableListOf<CircuitOutputInfo>()

        // 2a. Explicit LEDs & Digital Probes
        val indicators = components.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }
        for (ind in indicators) {
            val label = ind.label.ifBlank { if (ind.type == "LED") "LED_${ind.id.takeLast(3)}" else "Probe_${ind.id.takeLast(3)}" }
            discoveredOutputs.add(
                CircuitOutputInfo(
                    id = ind.id,
                    displayName = label,
                    componentId = ind.id,
                    portId = "IN",
                    isProbeOrLed = true
                )
            )
        }

        // 2b. If no explicit indicators, discover terminal output ports
        if (discoveredOutputs.isEmpty()) {
            for (comp in components) {
                if (comp.type in listOf("SWITCH", "CLOCK", "CONST_HIGH", "CONST_LOW")) continue
                val def = ComponentRegistry.get(comp.type) ?: continue
                for (port in def.outputs) {
                    val compName = comp.label.ifBlank { def.name }
                    val name = if (components.size == 1) port.name else "${compName}.${port.name}"
                    discoveredOutputs.add(
                        CircuitOutputInfo(
                            id = "${comp.id}_${port.id}",
                            displayName = name,
                            componentId = comp.id,
                            portId = port.id,
                            isProbeOrLed = false
                        )
                    )
                }
            }
        }

        if (discoveredInputs.isEmpty() || discoveredOutputs.isEmpty()) {
            return TruthTableData(
                title = "Entire Circuit",
                inputHeaders = discoveredInputs.map { it.displayName },
                outputHeaders = discoveredOutputs.map { it.displayName },
                rows = emptyList(),
                summaryMessage = if (discoveredInputs.isEmpty()) "No inputs found. Add Switches or logic gates." else "No outputs found. Add LEDs or probes."
            )
        }

        // Truncate to MAX_INPUTS_EVALUATED to maintain smooth UI
        val effectiveInputs = discoveredInputs.take(MAX_INPUTS_EVALUATED)
        val isTruncated = discoveredInputs.size > MAX_INPUTS_EVALUATED
        val n = effectiveInputs.size
        val totalCombinations = 1 shl n

        val inHeaders = effectiveInputs.map { it.displayName }
        val outHeaders = discoveredOutputs.map { it.displayName }
        val rows = mutableListOf<TruthTableRow>()

        // Track min-terms for canonical Boolean equation extraction
        val minTermsMap = mutableMapOf<String, MutableList<Int>>()
        val maxTermsMap = mutableMapOf<String, MutableList<Int>>()
        discoveredOutputs.forEach {
            minTermsMap[it.displayName] = mutableListOf()
            maxTermsMap[it.displayName] = mutableListOf()
        }

        // Current live input values for live operating point matching
        val currentInputValues = effectiveInputs.map { input ->
            if (input.isSwitch) {
                val sw = components.find { it.id == input.componentId }
                if (sw?.switchState == true) DigitalState.HIGH else DigitalState.LOW
            } else {
                val comp = components.find { it.id == input.componentId }
                comp?.inputValues?.get(input.portId) ?: DigitalState.LOW
            }
        }

        for (i in 0 until totalCombinations) {
            // Configure input states for combination i
            var isCurrentMatch = true
            val inMap = mutableMapOf<String, DigitalState>()

            val testComponents = components.map { comp ->
                var updated = comp
                for (b in 0 until n) {
                    val input = effectiveInputs[b]
                    val bitVal = (i shr (n - 1 - b)) and 1
                    val state = if (bitVal == 1) DigitalState.HIGH else DigitalState.LOW

                    if (input.componentId == comp.id) {
                        if (input.isSwitch) {
                            updated = updated.copy(switchState = bitVal == 1)
                        } else if (input.portId != null) {
                            val newInputs = updated.inputValues.toMutableMap()
                            newInputs[input.portId] = state
                            updated = updated.copy(inputValues = newInputs)
                        }
                    }

                    if (currentInputValues[b] != state) {
                        isCurrentMatch = false
                    }
                    inMap[input.displayName] = state
                }
                updated
            }

            // Execute full circuit simulation
            val simResult = SimulationEngine.simulate(testComponents, wires)

            // Extract output states
            val outMap = mutableMapOf<String, DigitalState>()
            var anyHigh = false

            for (output in discoveredOutputs) {
                val simComp = simResult.components.find { it.id == output.componentId }
                val outState = if (output.isProbeOrLed) {
                    simComp?.inputValues?.get("IN") ?: DigitalState.LOW
                } else if (output.portId != null) {
                    simComp?.outputValues?.get(output.portId) ?: DigitalState.LOW
                } else {
                    DigitalState.LOW
                }

                outMap[output.displayName] = outState
                if (outState == DigitalState.HIGH) {
                    anyHigh = true
                    minTermsMap[output.displayName]?.add(i)
                } else {
                    maxTermsMap[output.displayName]?.add(i)
                }
            }

            rows.add(
                TruthTableRow(
                    rowNumber = i,
                    inputValues = inMap,
                    outputValues = outMap,
                    isCurrent = isCurrentMatch,
                    isMinTerm = anyHigh
                )
            )
        }

        // Generate Canonical SOP Expressions
        val formulas = discoveredOutputs.map { out ->
            val minTerms = minTermsMap[out.displayName] ?: emptyList()
            val maxTerms = maxTermsMap[out.displayName] ?: emptyList()

            val sopString = when {
                minTerms.isEmpty() -> "0"
                minTerms.size == totalCombinations -> "1"
                else -> "Σm(" + minTerms.joinToString(", ") + ")"
            }

            val productTerms = if (minTerms.size in 1..8 && n <= 4) {
                minTerms.joinToString(" + ") { termIdx ->
                    (0 until n).joinToString("") { b ->
                        val inputName = inHeaders[b]
                        val bitVal = (termIdx shr (n - 1 - b)) and 1
                        if (bitVal == 1) inputName else "$inputName'"
                    }
                }
            } else {
                sopString
            }

            OutputFormula(
                outputName = out.displayName,
                canonicalSOP = sopString,
                minTerms = minTerms,
                maxTerms = maxTerms,
                simplifiedExpression = productTerms
            )
        }

        return TruthTableData(
            title = "Complete Circuit Analysis",
            inputHeaders = inHeaders,
            outputHeaders = outHeaders,
            rows = rows,
            formulas = formulas,
            totalCombinations = totalCombinations,
            mode = TruthTableMode.CIRCUIT,
            inputComponents = effectiveInputs,
            isTruncated = isTruncated,
            summaryMessage = "Analyzed ${effectiveInputs.size} inputs and ${discoveredOutputs.size} outputs across $totalCombinations states."
        )
    }

    /**
     * Generates truth table for a single selected logic component.
     */
    fun generateForComponent(
        component: CircuitComponent
    ): TruthTableData {
        val def = ComponentRegistry.get(component.type)
            ?: return TruthTableData("Unknown Component", emptyList(), emptyList(), emptyList())

        val inputPorts = def.inputs
        val outputPorts = def.outputs

        if (inputPorts.isEmpty()) {
            val curOut = def.evaluate(emptyMap(), component.switchState)
            val rows = listOf(
                TruthTableRow(
                    rowNumber = 0,
                    inputValues = mapOf("State" to DigitalState.LOW),
                    outputValues = def.evaluate(emptyMap(), false),
                    isCurrent = !component.switchState
                ),
                TruthTableRow(
                    rowNumber = 1,
                    inputValues = mapOf("State" to DigitalState.HIGH),
                    outputValues = def.evaluate(emptyMap(), true),
                    isCurrent = component.switchState
                )
            )
            return TruthTableData(
                title = "${def.name} (${component.label.ifBlank { component.id }})",
                inputHeaders = listOf("State"),
                outputHeaders = outputPorts.map { it.name.ifBlank { it.id } },
                rows = rows,
                mode = TruthTableMode.SELECTED_COMPONENT
            )
        }

        val inHeaders = inputPorts.map { it.name.ifBlank { it.id } }
        val outHeaders = outputPorts.map { it.name.ifBlank { it.id } }

        val n = inputPorts.size.coerceAtMost(6)
        val numCombinations = 1 shl n

        val rows = mutableListOf<TruthTableRow>()
        val currentInputs = component.inputValues
        val minTermsMap = mutableMapOf<String, MutableList<Int>>()
        outHeaders.forEach { minTermsMap[it] = mutableListOf() }

        for (i in 0 until numCombinations) {
            val comboInputs = mutableMapOf<String, DigitalState>()
            var isCurrentMatch = true

            for (b in 0 until n) {
                val port = inputPorts[b]
                val bitVal = (i shr (n - 1 - b)) and 1
                val state = if (bitVal == 1) DigitalState.HIGH else DigitalState.LOW
                comboInputs[port.id] = state

                val curSt = currentInputs[port.id] ?: DigitalState.UNKNOWN
                if (curSt != state) {
                    isCurrentMatch = false
                }
            }

            val outMap = def.evaluate(comboInputs, component.switchState)
            val mappedOutputs = outMap.mapKeys { entry ->
                outputPorts.find { it.id == entry.key }?.name ?: entry.key
            }

            mappedOutputs.forEach { (name, state) ->
                if (state == DigitalState.HIGH) minTermsMap[name]?.add(i)
            }

            rows.add(
                TruthTableRow(
                    rowNumber = i,
                    inputValues = comboInputs.mapKeys { entry ->
                        inputPorts.find { it.id == entry.key }?.name ?: entry.key
                    },
                    outputValues = mappedOutputs,
                    isCurrent = isCurrentMatch,
                    isMinTerm = mappedOutputs.values.any { it == DigitalState.HIGH }
                )
            )
        }

        val formulas = outHeaders.map { name ->
            val minTerms = minTermsMap[name] ?: emptyList()
            OutputFormula(
                outputName = name,
                canonicalSOP = if (minTerms.isEmpty()) "0" else "Σm(" + minTerms.joinToString(", ") + ")",
                minTerms = minTerms,
                maxTerms = emptyList(),
                simplifiedExpression = def.booleanFormula
            )
        }

        return TruthTableData(
            title = "${def.name} (${component.label.ifBlank { component.id }})",
            inputHeaders = inHeaders,
            outputHeaders = outHeaders,
            rows = rows,
            formulas = formulas,
            totalCombinations = numCombinations,
            mode = TruthTableMode.SELECTED_COMPONENT
        )
    }
}
