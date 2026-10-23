package com.example.logiclab.engine

import com.example.logiclab.model.ComponentCategory
import com.example.logiclab.model.ComponentDefinition
import com.example.logiclab.model.DigitalState
import com.example.logiclab.model.Port
import com.example.logiclab.model.PortType

object ComponentRegistry {

    private fun inPort(id: String, name: String, yNorm: Float, bit: Int = 0): Port =
        Port(id, name, PortType.INPUT, 0.0f, yNorm, bit)

    private fun outPort(id: String, name: String, yNorm: Float, bit: Int = 0): Port =
        Port(id, name, PortType.OUTPUT, 1.0f, yNorm, bit)

    val definitions: Map<String, ComponentDefinition> by lazy {
        listOf(
            // ==================== I/O COMPONENTS ====================
            ComponentDefinition(
                type = "SWITCH",
                name = "Input Switch",
                category = ComponentCategory.IO,
                inputs = emptyList(),
                outputs = listOf(outPort("OUT", "Q", 0.5f)),
                width = 80f,
                height = 50f,
                description = "Manual SPST toggle switch providing a stable HIGH (1) or LOW (0) logic level.",
                booleanFormula = "Q = Switch State",
                evaluate = { _, sw -> mapOf("OUT" to if (sw) DigitalState.HIGH else DigitalState.LOW) }
            ),
            ComponentDefinition(
                type = "CONST_HIGH",
                name = "Constant HIGH (VCC)",
                category = ComponentCategory.IO,
                inputs = emptyList(),
                outputs = listOf(outPort("OUT", "1", 0.5f)),
                width = 70f,
                height = 40f,
                description = "Tied to logic high (VCC / 1).",
                booleanFormula = "OUT = 1",
                evaluate = { _, _ -> mapOf("OUT" to DigitalState.HIGH) }
            ),
            ComponentDefinition(
                type = "CONST_LOW",
                name = "Constant LOW (GND)",
                category = ComponentCategory.IO,
                inputs = emptyList(),
                outputs = listOf(outPort("OUT", "0", 0.5f)),
                width = 70f,
                height = 40f,
                description = "Tied to logic low (GND / 0).",
                booleanFormula = "OUT = 0",
                evaluate = { _, _ -> mapOf("OUT" to DigitalState.LOW) }
            ),
            ComponentDefinition(
                type = "CLOCK",
                name = "Clock Generator",
                category = ComponentCategory.IO,
                inputs = emptyList(),
                outputs = listOf(outPort("CLK", "CLK", 0.5f)),
                width = 80f,
                height = 50f,
                description = "Periodic square-wave logic oscillator signal.",
                booleanFormula = "CLK = Toggling",
                evaluate = { _, sw -> mapOf("CLK" to if (sw) DigitalState.HIGH else DigitalState.LOW) }
            ),
            ComponentDefinition(
                type = "LED",
                name = "Output LED",
                category = ComponentCategory.IO,
                inputs = listOf(inPort("IN", "D", 0.5f)),
                outputs = emptyList(),
                width = 70f,
                height = 50f,
                description = "Visual status indicator. Illuminates green on HIGH, dark on LOW.",
                booleanFormula = "LED = D",
                evaluate = { _, _ -> emptyMap() }
            ),
            ComponentDefinition(
                type = "DIODE",
                name = "Semiconductor Diode / LED",
                category = ComponentCategory.IO,
                inputs = listOf(inPort("IN", "Anode", 0.5f)),
                outputs = emptyList(),
                width = 70f,
                height = 50f,
                description = "Authentic 5mm through-hole semiconductor light-emitting diode with internal leadframe, anvil, and die cup.",
                booleanFormula = "Diode = Forward Biased (HIGH)",
                evaluate = { _, _ -> emptyMap() }
            ),
            ComponentDefinition(
                type = "DIGITAL_PROBE",
                name = "Digital Probe",
                category = ComponentCategory.IO,
                inputs = listOf(inPort("IN", "P", 0.5f)),
                outputs = emptyList(),
                width = 70f,
                height = 50f,
                description = "Logic analyzer probe reading the digital state (0, 1, or X).",
                booleanFormula = "Probe = P",
                evaluate = { _, _ -> emptyMap() }
            ),
            ComponentDefinition(
                type = "SEVEN_SEG",
                name = "7-Segment Display",
                category = ComponentCategory.IO,
                inputs = listOf(
                    inPort("A", "a", 0.125f),
                    inPort("B", "b", 0.25f),
                    inPort("C", "c", 0.375f),
                    inPort("D", "d", 0.5f),
                    inPort("E", "e", 0.625f),
                    inPort("F", "f", 0.75f),
                    inPort("G", "g", 0.875f)
                ),
                outputs = emptyList(),
                width = 90f,
                height = 110f,
                description = "Common cathode 7-segment LED display with inputs a through g.",
                booleanFormula = "Segments a..g",
                evaluate = { _, _ -> emptyMap() }
            ),
            ComponentDefinition(
                type = "BUZZER",
                name = "Piezo Buzzer",
                category = ComponentCategory.IO,
                inputs = listOf(inPort("IN", "SIG", 0.5f)),
                outputs = emptyList(),
                width = 75f,
                height = 55f,
                description = "Electronic piezo speaker audio transducer. Emits an audible tone when driven HIGH.",
                booleanFormula = "Sound = 1 (when SIG=1)",
                evaluate = { _, _ -> emptyMap() }
            ),

            // ==================== BASIC GATES ====================
            ComponentDefinition(
                type = "AND",
                name = "AND Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 90f,
                height = 60f,
                description = "Output is HIGH only if all inputs are HIGH.",
                booleanFormula = "Y = A · B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.LOW || b == DigitalState.LOW -> DigitalState.LOW
                        a == DigitalState.HIGH && b == DigitalState.HIGH -> DigitalState.HIGH
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "OR",
                name = "OR Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 90f,
                height = 60f,
                description = "Output is HIGH if at least one input is HIGH.",
                booleanFormula = "Y = A + B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.HIGH || b == DigitalState.HIGH -> DigitalState.HIGH
                        a == DigitalState.LOW && b == DigitalState.LOW -> DigitalState.LOW
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "NOT",
                name = "NOT Gate (Inverter)",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.5f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 80f,
                height = 50f,
                description = "Outputs the inverted logic level of its input.",
                booleanFormula = "Y = A'",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val y = when (a) {
                        DigitalState.HIGH -> DigitalState.LOW
                        DigitalState.LOW -> DigitalState.HIGH
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "NAND",
                name = "NAND Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 95f,
                height = 60f,
                description = "Universal gate. Output is LOW only when all inputs are HIGH.",
                booleanFormula = "Y = (A · B)'",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.LOW || b == DigitalState.LOW -> DigitalState.HIGH
                        a == DigitalState.HIGH && b == DigitalState.HIGH -> DigitalState.LOW
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "NOR",
                name = "NOR Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 95f,
                height = 60f,
                description = "Universal gate. Output is HIGH only when all inputs are LOW.",
                booleanFormula = "Y = (A + B)'",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.HIGH || b == DigitalState.HIGH -> DigitalState.LOW
                        a == DigitalState.LOW && b == DigitalState.LOW -> DigitalState.HIGH
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "XOR",
                name = "XOR Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 95f,
                height = 60f,
                description = "Exclusive-OR gate. Output is HIGH when odd number of inputs are HIGH.",
                booleanFormula = "Y = A ⊕ B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN -> DigitalState.UNKNOWN
                        a != b -> DigitalState.HIGH
                        else -> DigitalState.LOW
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "XNOR",
                name = "XNOR Gate",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 100f,
                height = 60f,
                description = "Equivalence gate. Output is HIGH when inputs have identical states.",
                booleanFormula = "Y = (A ⊕ B)'",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val y = when {
                        a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN -> DigitalState.UNKNOWN
                        a == b -> DigitalState.HIGH
                        else -> DigitalState.LOW
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "BUFFER",
                name = "Buffer",
                category = ComponentCategory.BASIC_GATES,
                inputs = listOf(inPort("A", "A", 0.5f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 80f,
                height = 50f,
                description = "Non-inverting logic buffer.",
                booleanFormula = "Y = A",
                evaluate = { inp, _ ->
                    mapOf("Y" to (inp["A"] ?: DigitalState.UNKNOWN))
                }
            ),

            // ==================== SEQUENTIAL LOGIC & OSCILLATORS ====================
            ComponentDefinition(
                type = "OSCILLATOR",
                name = "Digital Oscillator",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("EN", "EN", 0.5f)),
                outputs = listOf(outPort("OUT", "CLK", 0.35f), outPort("INV", "CLK'", 0.65f)),
                width = 90f,
                height = 65f,
                description = "High-stability digital clock oscillator with enable gating and complementary outputs.",
                booleanFormula = "CLK = Toggling (when EN=1), CLK' = (CLK)'",
                evaluate = { inp, sw ->
                    val en = inp["EN"]
                    // EN is active-high; unconnected or unknown defaults to HIGH (free-running oscillator)
                    val isEnabled = (en == null || en == DigitalState.HIGH || en == DigitalState.UNKNOWN)
                    if (!isEnabled) {
                        mapOf(
                            "OUT" to DigitalState.LOW,
                            "CLK" to DigitalState.LOW,
                            "INV" to DigitalState.HIGH,
                            "CLK'" to DigitalState.HIGH,
                            "CLK_BAR" to DigitalState.HIGH
                        )
                    } else {
                        val clk = if (sw) DigitalState.HIGH else DigitalState.LOW
                        val inv = if (sw) DigitalState.LOW else DigitalState.HIGH
                        mapOf(
                            "OUT" to clk,
                            "CLK" to clk,
                            "INV" to inv,
                            "CLK'" to inv,
                            "CLK_BAR" to inv
                        )
                    }
                }
            ),
            ComponentDefinition(
                type = "D_FLIP_FLOP",
                name = "D Flip-Flop",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("D", "D", 0.35f), inPort("CLK", "CLK", 0.75f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 75f,
                description = "Edge-triggered Data Flip-Flop. Latches the D input state on the clock transition.",
                booleanFormula = "Q(next) = D",
                evaluate = { inp, _ ->
                    val d = inp["D"] ?: DigitalState.UNKNOWN
                    val clk = inp["CLK"] ?: DigitalState.LOW
                    if (clk == DigitalState.HIGH) {
                        val q = d
                        val qBar = when (d) {
                            DigitalState.HIGH -> DigitalState.LOW
                            DigitalState.LOW -> DigitalState.HIGH
                            else -> DigitalState.UNKNOWN
                        }
                        mapOf("Q" to q, "Q_BAR" to qBar)
                    } else {
                        // Hold previous latch state if input is valid
                        val q = if (d != DigitalState.UNKNOWN) d else DigitalState.LOW
                        val qBar = if (q == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to q, "Q_BAR" to qBar)
                    }
                }
            ),
            ComponentDefinition(
                type = "JK_FLIP_FLOP",
                name = "JK Flip-Flop",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("J", "J", 0.25f), inPort("CLK", "CLK", 0.50f), inPort("K", "K", 0.75f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 100f,
                height = 80f,
                description = "Universal JK Flip-Flop. Supports Set (J=1), Reset (K=1), Hold (0,0), and Toggle (1,1).",
                booleanFormula = "Q(next) = J·Q' + K'·Q",
                evaluate = { inp, sw ->
                    val j = inp["J"] ?: DigitalState.LOW
                    val k = inp["K"] ?: DigitalState.LOW
                    val clk = inp["CLK"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (clk == DigitalState.HIGH) {
                        val nextQ = when {
                            j == DigitalState.HIGH && k == DigitalState.LOW -> DigitalState.HIGH
                            j == DigitalState.LOW && k == DigitalState.HIGH -> DigitalState.LOW
                            j == DigitalState.HIGH && k == DigitalState.HIGH -> if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                            else -> prevQ
                        }
                        val nextQBar = if (nextQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to nextQ, "Q_BAR" to nextQBar)
                    } else {
                        val qBar = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to prevQ, "Q_BAR" to qBar)
                    }
                }
            ),
            ComponentDefinition(
                type = "T_FLIP_FLOP",
                name = "T Flip-Flop",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("T", "T", 0.35f), inPort("CLK", "CLK", 0.70f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 75f,
                description = "Toggle Flip-Flop. Inverts stored output on each clock pulse when T=1.",
                booleanFormula = "Q(next) = T ⊕ Q",
                evaluate = { inp, sw ->
                    val t = inp["T"] ?: DigitalState.LOW
                    val clk = inp["CLK"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (clk == DigitalState.HIGH && t == DigitalState.HIGH) {
                        val nextQ = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to nextQ, "Q_BAR" to if (nextQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    } else {
                        val qBar = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to prevQ, "Q_BAR" to qBar)
                    }
                }
            ),
            ComponentDefinition(
                type = "SR_FLIP_FLOP",
                name = "SR Flip-Flop",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("S", "S", 0.25f), inPort("CLK", "CLK", 0.50f), inPort("R", "R", 0.75f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 100f,
                height = 80f,
                description = "Clocked Set-Reset latch. Sets Q=1 on S=1, resets Q=0 on R=1.",
                booleanFormula = "Q(next) = S + R'·Q (S·R = 0)",
                evaluate = { inp, sw ->
                    val s = inp["S"] ?: DigitalState.LOW
                    val r = inp["R"] ?: DigitalState.LOW
                    val clk = inp["CLK"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (clk == DigitalState.HIGH) {
                        when {
                            s == DigitalState.HIGH && r == DigitalState.HIGH -> mapOf("Q" to DigitalState.UNKNOWN, "Q_BAR" to DigitalState.UNKNOWN)
                            s == DigitalState.HIGH -> mapOf("Q" to DigitalState.HIGH, "Q_BAR" to DigitalState.LOW)
                            r == DigitalState.HIGH -> mapOf("Q" to DigitalState.LOW, "Q_BAR" to DigitalState.HIGH)
                            else -> mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                        }
                    } else {
                        mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    }
                }
            ),
            ComponentDefinition(
                type = "SR_LATCH",
                name = "SR Latch",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("S", "S", 0.30f), inPort("R", "R", 0.70f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 70f,
                description = "Bistable Set-Reset latch memory element. Stores 1 on S=1, resets to 0 on R=1.",
                booleanFormula = "Q(next) = S + R'·Q (S=R=1 invalid)",
                evaluate = { inp, sw ->
                    val s = inp["S"] ?: DigitalState.LOW
                    val r = inp["R"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    when {
                        s == DigitalState.HIGH && r == DigitalState.HIGH -> mapOf("Q" to DigitalState.UNKNOWN, "Q_BAR" to DigitalState.UNKNOWN)
                        s == DigitalState.HIGH -> mapOf("Q" to DigitalState.HIGH, "Q_BAR" to DigitalState.LOW)
                        r == DigitalState.HIGH -> mapOf("Q" to DigitalState.LOW, "Q_BAR" to DigitalState.HIGH)
                        else -> mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    }
                }
            ),
            ComponentDefinition(
                type = "SR_LATCH_NAND",
                name = "Active-Low SR Latch (NAND)",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("S_BAR", "S'", 0.30f), inPort("R_BAR", "R'", 0.70f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 70f,
                description = "Cross-coupled NAND Set-Reset latch with active-low inputs (S'=0 sets, R'=0 resets).",
                booleanFormula = "When S'=0,R'=1 -> Q=1; When S'=1,R'=0 -> Q=0; S'=R'=1 -> Hold; S'=R'=0 -> Invalid",
                evaluate = { inp, sw ->
                    val sBar = inp["S_BAR"] ?: DigitalState.HIGH
                    val rBar = inp["R_BAR"] ?: DigitalState.HIGH
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    when {
                        sBar == DigitalState.LOW && rBar == DigitalState.LOW -> mapOf("Q" to DigitalState.UNKNOWN, "Q_BAR" to DigitalState.UNKNOWN)
                        sBar == DigitalState.LOW -> mapOf("Q" to DigitalState.HIGH, "Q_BAR" to DigitalState.LOW)
                        rBar == DigitalState.LOW -> mapOf("Q" to DigitalState.LOW, "Q_BAR" to DigitalState.HIGH)
                        else -> mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    }
                }
            ),
            ComponentDefinition(
                type = "GATED_SR_LATCH",
                name = "Gated SR Latch",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("S", "S", 0.25f), inPort("EN", "EN", 0.50f), inPort("R", "R", 0.75f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 100f,
                height = 80f,
                description = "Level-sensitive Gated SR latch. Transparent when EN=1; latches stored state when EN=0.",
                booleanFormula = "When EN=1: Q=S+R'Q; When EN=0: Latched",
                evaluate = { inp, sw ->
                    val en = inp["EN"] ?: DigitalState.LOW
                    val s = inp["S"] ?: DigitalState.LOW
                    val r = inp["R"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (en == DigitalState.HIGH) {
                        when {
                            s == DigitalState.HIGH && r == DigitalState.HIGH -> mapOf("Q" to DigitalState.UNKNOWN, "Q_BAR" to DigitalState.UNKNOWN)
                            s == DigitalState.HIGH -> mapOf("Q" to DigitalState.HIGH, "Q_BAR" to DigitalState.LOW)
                            r == DigitalState.HIGH -> mapOf("Q" to DigitalState.LOW, "Q_BAR" to DigitalState.HIGH)
                            else -> mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                        }
                    } else {
                        mapOf("Q" to prevQ, "Q_BAR" to if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    }
                }
            ),
            ComponentDefinition(
                type = "D_LATCH",
                name = "Transparent D Latch",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("D", "D", 0.35f), inPort("EN", "EN", 0.70f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 75f,
                description = "Transparent Data Latch (7475/74373 style). Q follows D when EN=1; freezes state when EN=0.",
                booleanFormula = "Q = D (EN=1), Q = Latched (EN=0)",
                evaluate = { inp, sw ->
                    val d = inp["D"] ?: DigitalState.LOW
                    val en = inp["EN"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (en == DigitalState.HIGH) {
                        val q = d
                        val qBar = if (q == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to q, "Q_BAR" to qBar)
                    } else {
                        val qBar = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to prevQ, "Q_BAR" to qBar)
                    }
                }
            ),
            ComponentDefinition(
                type = "JK_LATCH",
                name = "Gated JK Latch",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("J", "J", 0.25f), inPort("EN", "EN", 0.50f), inPort("K", "K", 0.75f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 100f,
                height = 80f,
                description = "Level-gated universal JK latch with toggle, set, reset, and hold functions.",
                booleanFormula = "When EN=1: J·Q' + K'·Q; When EN=0: Latched",
                evaluate = { inp, sw ->
                    val en = inp["EN"] ?: DigitalState.LOW
                    val j = inp["J"] ?: DigitalState.LOW
                    val k = inp["K"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (en == DigitalState.HIGH) {
                        val nextQ = when {
                            j == DigitalState.HIGH && k == DigitalState.LOW -> DigitalState.HIGH
                            j == DigitalState.LOW && k == DigitalState.HIGH -> DigitalState.LOW
                            j == DigitalState.HIGH && k == DigitalState.HIGH -> if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                            else -> prevQ
                        }
                        val nextQBar = if (nextQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to nextQ, "Q_BAR" to nextQBar)
                    } else {
                        val qBar = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to prevQ, "Q_BAR" to qBar)
                    }
                }
            ),
            ComponentDefinition(
                type = "T_LATCH",
                name = "Gated T Latch",
                category = ComponentCategory.SEQUENTIAL,
                inputs = listOf(inPort("T", "T", 0.35f), inPort("EN", "EN", 0.70f)),
                outputs = listOf(outPort("Q", "Q", 0.35f), outPort("Q_BAR", "Q'", 0.65f)),
                width = 95f,
                height = 75f,
                description = "Level-gated toggle latch. Inverts stored output while EN=1 and T=1.",
                booleanFormula = "When EN=1: T ⊕ Q; When EN=0: Latched",
                evaluate = { inp, sw ->
                    val en = inp["EN"] ?: DigitalState.LOW
                    val t = inp["T"] ?: DigitalState.LOW
                    val prevQ = if (sw) DigitalState.HIGH else DigitalState.LOW

                    if (en == DigitalState.HIGH && t == DigitalState.HIGH) {
                        val nextQ = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to nextQ, "Q_BAR" to if (nextQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH)
                    } else {
                        val qBar = if (prevQ == DigitalState.HIGH) DigitalState.LOW else DigitalState.HIGH
                        mapOf("Q" to prevQ, "Q_BAR" to qBar)
                    }
                }
            ),

            // ==================== ARITHMETIC CIRCUITS & PRESETS ====================
            ComponentDefinition(
                type = "HALF_ADDER",
                name = "Half Adder",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("SUM", "S", 0.3f), outPort("CARRY", "C", 0.7f)),
                width = 110f,
                height = 70f,
                description = "Adds two 1-bit binary inputs, producing Sum and Carry.",
                booleanFormula = "S = A ⊕ B, C = A · B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    if (a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN) {
                        mapOf("SUM" to DigitalState.UNKNOWN, "CARRY" to DigitalState.UNKNOWN)
                    } else {
                        val sum = if (a != b) DigitalState.HIGH else DigitalState.LOW
                        val carry = if (a == DigitalState.HIGH && b == DigitalState.HIGH) DigitalState.HIGH else DigitalState.LOW
                        mapOf("SUM" to sum, "CARRY" to carry)
                    }
                }
            ),
            ComponentDefinition(
                type = "FULL_ADDER",
                name = "Full Adder",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(inPort("A", "A", 0.25f), inPort("B", "B", 0.5f), inPort("CIN", "Cin", 0.75f)),
                outputs = listOf(outPort("SUM", "S", 0.35f), outPort("COUT", "Cout", 0.65f)),
                width = 120f,
                height = 80f,
                description = "Adds two 1-bit inputs plus Carry-in, producing Sum and Carry-out.",
                booleanFormula = "S = A ⊕ B ⊕ Cin, Cout = (A·B) + (Cin·(A⊕B))",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val cin = inp["CIN"] ?: DigitalState.UNKNOWN
                    if (a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN || cin == DigitalState.UNKNOWN) {
                        mapOf("SUM" to DigitalState.UNKNOWN, "COUT" to DigitalState.UNKNOWN)
                    } else {
                        val aVal = if (a == DigitalState.HIGH) 1 else 0
                        val bVal = if (b == DigitalState.HIGH) 1 else 0
                        val cVal = if (cin == DigitalState.HIGH) 1 else 0
                        val total = aVal + bVal + cVal
                        mapOf(
                            "SUM" to if (total % 2 == 1) DigitalState.HIGH else DigitalState.LOW,
                            "COUT" to if (total >= 2) DigitalState.HIGH else DigitalState.LOW
                        )
                    }
                }
            ),
            ComponentDefinition(
                type = "ADDER_4BIT",
                name = "4-bit Ripple Adder",
                category = ComponentCategory.PRESETS,
                inputs = (0..3).map { inPort("A$it", "A$it", 0.12f + it * 0.1f) } +
                        (0..3).map { inPort("B$it", "B$it", 0.52f + it * 0.1f) } +
                        listOf(inPort("CIN", "Cin", 0.92f)),
                outputs = (0..3).map { outPort("S$it", "S$it", 0.2f + it * 0.18f) } +
                        listOf(outPort("COUT", "Cout", 0.88f)),
                width = 140f,
                height = 130f,
                description = "4-bit binary adder with carry-in and carry-out (74LS283 architecture).",
                booleanFormula = "Sum[3:0] = A + B + Cin, Cout = Overflow",
                evaluate = { inp, _ ->
                    var a = 0
                    var b = 0
                    var anyUnknown = false
                    for (i in 0..3) {
                        val bitA = inp["A$i"] ?: DigitalState.LOW
                        val bitB = inp["B$i"] ?: DigitalState.LOW
                        if (bitA == DigitalState.UNKNOWN || bitB == DigitalState.UNKNOWN) anyUnknown = true
                        if (bitA == DigitalState.HIGH) a = a or (1 shl i)
                        if (bitB == DigitalState.HIGH) b = b or (1 shl i)
                    }
                    val cin = inp["CIN"] ?: DigitalState.LOW
                    val cVal = if (cin == DigitalState.HIGH) 1 else 0

                    if (anyUnknown) {
                        (0..3).associate { "S$it" to DigitalState.UNKNOWN } + mapOf("COUT" to DigitalState.UNKNOWN)
                    } else {
                        val sumTotal = a + b + cVal
                        val outMap = mutableMapOf<String, DigitalState>()
                        for (i in 0..3) {
                            outMap["S$i"] = if ((sumTotal and (1 shl i)) != 0) DigitalState.HIGH else DigitalState.LOW
                        }
                        outMap["COUT"] = if (sumTotal >= 16) DigitalState.HIGH else DigitalState.LOW
                        outMap
                    }
                }
            ),
            ComponentDefinition(
                type = "HALF_SUBTRACTOR",
                name = "Half Subtractor",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(inPort("A", "A", 0.3f), inPort("B", "B", 0.7f)),
                outputs = listOf(outPort("DIFF", "D", 0.3f), outPort("BORR", "B", 0.7f)),
                width = 110f,
                height = 70f,
                description = "Subtracts B from A, generating Difference (D) and Borrow (B).",
                booleanFormula = "D = A ⊕ B, B = A' · B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    if (a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN) {
                        mapOf("DIFF" to DigitalState.UNKNOWN, "BORR" to DigitalState.UNKNOWN)
                    } else {
                        val diff = if (a != b) DigitalState.HIGH else DigitalState.LOW
                        val borr = if (a == DigitalState.LOW && b == DigitalState.HIGH) DigitalState.HIGH else DigitalState.LOW
                        mapOf("DIFF" to diff, "BORR" to borr)
                    }
                }
            ),
            ComponentDefinition(
                type = "FULL_SUBTRACTOR",
                name = "Full Subtractor",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(inPort("A", "A", 0.25f), inPort("B", "B", 0.5f), inPort("BIN", "Bin", 0.75f)),
                outputs = listOf(outPort("DIFF", "D", 0.35f), outPort("BOUT", "Bout", 0.65f)),
                width = 120f,
                height = 80f,
                description = "Subtracts B and Borrow-in from A, generating Difference and Borrow-out.",
                booleanFormula = "D = A ⊕ B ⊕ Bin, Bout = A'·B + (A ⊕ B)'·Bin",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    val bin = inp["BIN"] ?: DigitalState.UNKNOWN
                    if (a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN || bin == DigitalState.UNKNOWN) {
                        mapOf("DIFF" to DigitalState.UNKNOWN, "BOUT" to DigitalState.UNKNOWN)
                    } else {
                        val aV = if (a == DigitalState.HIGH) 1 else 0
                        val bV = if (b == DigitalState.HIGH) 1 else 0
                        val binV = if (bin == DigitalState.HIGH) 1 else 0
                        val dVal = aV - bV - binV
                        val diff = if (Math.abs(dVal) % 2 == 1) DigitalState.HIGH else DigitalState.LOW
                        val bout = if (dVal < 0) DigitalState.HIGH else DigitalState.LOW
                        mapOf("DIFF" to diff, "BOUT" to bout)
                    }
                }
            ),
            ComponentDefinition(
                type = "ADDER_2BIT",
                name = "2-bit Binary Adder",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(
                    inPort("A0", "A0", 0.2f),
                    inPort("A1", "A1", 0.35f),
                    inPort("B0", "B0", 0.5f),
                    inPort("B1", "B1", 0.65f),
                    inPort("CIN", "Cin", 0.8f)
                ),
                outputs = listOf(
                    outPort("S0", "S0", 0.3f),
                    outPort("S1", "S1", 0.5f),
                    outPort("COUT", "Cout", 0.7f)
                ),
                width = 130f,
                height = 100f,
                description = "Adds two 2-bit binary numbers A[1:0] and B[1:0] with Carry-in.",
                booleanFormula = "S[1:0] + Cout = A + B + Cin",
                evaluate = { inp, _ ->
                    val a0 = inp["A0"] ?: DigitalState.LOW
                    val a1 = inp["A1"] ?: DigitalState.LOW
                    val b0 = inp["B0"] ?: DigitalState.LOW
                    val b1 = inp["B1"] ?: DigitalState.LOW
                    val cin = inp["CIN"] ?: DigitalState.LOW
                    val a = (if (a0 == DigitalState.HIGH) 1 else 0) + (if (a1 == DigitalState.HIGH) 2 else 0)
                    val b = (if (b0 == DigitalState.HIGH) 1 else 0) + (if (b1 == DigitalState.HIGH) 2 else 0)
                    val c = if (cin == DigitalState.HIGH) 1 else 0
                    val sum = a + b + c
                    mapOf(
                        "S0" to if ((sum and 1) != 0) DigitalState.HIGH else DigitalState.LOW,
                        "S1" to if ((sum and 2) != 0) DigitalState.HIGH else DigitalState.LOW,
                        "COUT" to if ((sum and 4) != 0) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "ADDER_4BIT",
                name = "4-bit Binary Adder",
                category = ComponentCategory.ARITHMETIC,
                inputs = listOf(
                    inPort("A0", "A0", 0.12f),
                    inPort("A1", "A1", 0.22f),
                    inPort("A2", "A2", 0.32f),
                    inPort("A3", "A3", 0.42f),
                    inPort("B0", "B0", 0.52f),
                    inPort("B1", "B1", 0.62f),
                    inPort("B2", "B2", 0.72f),
                    inPort("B3", "B3", 0.82f),
                    inPort("CIN", "Cin", 0.92f)
                ),
                outputs = listOf(
                    outPort("S0", "S0", 0.2f),
                    outPort("S1", "S1", 0.35f),
                    outPort("S2", "S2", 0.5f),
                    outPort("S3", "S3", 0.65f),
                    outPort("COUT", "Cout", 0.8f)
                ),
                width = 140f,
                height = 140f,
                description = "4-bit parallel ripple-carry binary adder (e.g., 74LS283).",
                booleanFormula = "Sum[3:0] + Cout = A[3:0] + B[3:0] + Cin",
                evaluate = { inp, _ ->
                    var a = 0
                    var b = 0
                    for (i in 0..3) {
                        if (inp["A$i"] == DigitalState.HIGH) a = a or (1 shl i)
                        if (inp["B$i"] == DigitalState.HIGH) b = b or (1 shl i)
                    }
                    val c = if (inp["CIN"] == DigitalState.HIGH) 1 else 0
                    val sum = a + b + c
                    val res = mutableMapOf<String, DigitalState>()
                    for (i in 0..3) {
                        res["S$i"] = if ((sum and (1 shl i)) != 0) DigitalState.HIGH else DigitalState.LOW
                    }
                    res["COUT"] = if ((sum and 16) != 0) DigitalState.HIGH else DigitalState.LOW
                    res
                }
            ),

            // ==================== MULTIPLEXERS ====================
            ComponentDefinition(
                type = "MUX_2_1",
                name = "2:1 Multiplexer",
                category = ComponentCategory.MULTIPLEXERS,
                inputs = listOf(
                    inPort("D0", "D0", 0.25f),
                    inPort("D1", "D1", 0.5f),
                    inPort("S", "S", 0.75f)
                ),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 100f,
                height = 70f,
                description = "Selects between two inputs D0 and D1 using select line S.",
                booleanFormula = "Y = S'·D0 + S·D1",
                evaluate = { inp, _ ->
                    val s = inp["S"] ?: DigitalState.UNKNOWN
                    val y = when (s) {
                        DigitalState.LOW -> inp["D0"] ?: DigitalState.UNKNOWN
                        DigitalState.HIGH -> inp["D1"] ?: DigitalState.UNKNOWN
                        else -> DigitalState.UNKNOWN
                    }
                    mapOf("Y" to y)
                }
            ),
            ComponentDefinition(
                type = "MUX_4_1",
                name = "4:1 Multiplexer",
                category = ComponentCategory.MULTIPLEXERS,
                inputs = listOf(
                    inPort("D0", "D0", 0.18f),
                    inPort("D1", "D1", 0.32f),
                    inPort("D2", "D2", 0.46f),
                    inPort("D3", "D3", 0.60f),
                    inPort("S0", "S0", 0.76f),
                    inPort("S1", "S1", 0.90f)
                ),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 120f,
                height = 100f,
                description = "Directs one of 4 data inputs (D0-D3) to output Y based on S1,S0.",
                booleanFormula = "Y = D[S1 S0]",
                evaluate = { inp, _ ->
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    if (s0 == DigitalState.UNKNOWN || s1 == DigitalState.UNKNOWN) {
                        mapOf("Y" to DigitalState.UNKNOWN)
                    } else {
                        val sel = (if (s0 == DigitalState.HIGH) 1 else 0) + (if (s1 == DigitalState.HIGH) 2 else 0)
                        mapOf("Y" to (inp["D$sel"] ?: DigitalState.UNKNOWN))
                    }
                }
            ),
            ComponentDefinition(
                type = "MUX_8_1",
                name = "8:1 Multiplexer",
                category = ComponentCategory.MULTIPLEXERS,
                inputs = (0..7).map { inPort("D$it", "D$it", 0.1f + it * 0.08f) } +
                        listOf(inPort("S0", "S0", 0.78f), inPort("S1", "S1", 0.86f), inPort("S2", "S2", 0.94f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 130f,
                height = 150f,
                description = "8-channel multiplexer with select lines S2, S1, S0.",
                booleanFormula = "Y = D[S2 S1 S0]",
                evaluate = { inp, _ ->
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    val s2 = inp["S2"] ?: DigitalState.UNKNOWN
                    if (s0 == DigitalState.UNKNOWN || s1 == DigitalState.UNKNOWN || s2 == DigitalState.UNKNOWN) {
                        mapOf("Y" to DigitalState.UNKNOWN)
                    } else {
                        val sel = (if (s0 == DigitalState.HIGH) 1 else 0) +
                                (if (s1 == DigitalState.HIGH) 2 else 0) +
                                (if (s2 == DigitalState.HIGH) 4 else 0)
                        mapOf("Y" to (inp["D$sel"] ?: DigitalState.UNKNOWN))
                    }
                }
            ),
            ComponentDefinition(
                type = "MUX_16_1",
                name = "16:1 Multiplexer",
                category = ComponentCategory.MULTIPLEXERS,
                inputs = (0..15).map { inPort("D$it", "D$it", 0.05f + it * 0.045f) } +
                        listOf(inPort("S0", "S0", 0.8f), inPort("S1", "S1", 0.85f), inPort("S2", "S2", 0.9f), inPort("S3", "S3", 0.95f)),
                outputs = listOf(outPort("Y", "Y", 0.5f)),
                width = 140f,
                height = 200f,
                description = "16-channel multiplexer addressed by 4 select lines S3..S0.",
                booleanFormula = "Y = D[S3 S2 S1 S0]",
                evaluate = { inp, _ ->
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    val s2 = inp["S2"] ?: DigitalState.UNKNOWN
                    val s3 = inp["S3"] ?: DigitalState.UNKNOWN
                    if (listOf(s0, s1, s2, s3).any { it == DigitalState.UNKNOWN }) {
                        mapOf("Y" to DigitalState.UNKNOWN)
                    } else {
                        var sel = 0
                        if (s0 == DigitalState.HIGH) sel += 1
                        if (s1 == DigitalState.HIGH) sel += 2
                        if (s2 == DigitalState.HIGH) sel += 4
                        if (s3 == DigitalState.HIGH) sel += 8
                        mapOf("Y" to (inp["D$sel"] ?: DigitalState.UNKNOWN))
                    }
                }
            ),

            // ==================== DEMULTIPLEXERS ====================
            ComponentDefinition(
                type = "DEMUX_1_2",
                name = "1:2 Demultiplexer",
                category = ComponentCategory.DEMULTIPLEXERS,
                inputs = listOf(inPort("DIN", "D", 0.35f), inPort("S", "S", 0.7f)),
                outputs = listOf(outPort("Y0", "Y0", 0.35f), outPort("Y1", "Y1", 0.65f)),
                width = 100f,
                height = 70f,
                description = "Routes data input D to Y0 (when S=0) or Y1 (when S=1).",
                booleanFormula = "Y0 = S'·D, Y1 = S·D",
                evaluate = { inp, _ ->
                    val d = inp["DIN"] ?: DigitalState.UNKNOWN
                    val s = inp["S"] ?: DigitalState.UNKNOWN
                    when (s) {
                        DigitalState.LOW -> mapOf("Y0" to d, "Y1" to DigitalState.LOW)
                        DigitalState.HIGH -> mapOf("Y0" to DigitalState.LOW, "Y1" to d)
                        else -> mapOf("Y0" to DigitalState.UNKNOWN, "Y1" to DigitalState.UNKNOWN)
                    }
                }
            ),
            ComponentDefinition(
                type = "DEMUX_1_4",
                name = "1:4 Demultiplexer",
                category = ComponentCategory.DEMULTIPLEXERS,
                inputs = listOf(inPort("DIN", "D", 0.25f), inPort("S0", "S0", 0.55f), inPort("S1", "S1", 0.75f)),
                outputs = (0..3).map { outPort("Y$it", "Y$it", 0.2f + it * 0.2f) },
                width = 110f,
                height = 90f,
                description = "Routes input D to one of 4 outputs based on S1,S0.",
                booleanFormula = "Y_i = D when S == i else 0",
                evaluate = { inp, _ ->
                    val d = inp["DIN"] ?: DigitalState.UNKNOWN
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    if (s0 == DigitalState.UNKNOWN || s1 == DigitalState.UNKNOWN) {
                        (0..3).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        val sel = (if (s0 == DigitalState.HIGH) 1 else 0) + (if (s1 == DigitalState.HIGH) 2 else 0)
                        (0..3).associate { "Y$it" to if (it == sel) d else DigitalState.LOW }
                    }
                }
            ),
            ComponentDefinition(
                type = "DEMUX_1_8",
                name = "1:8 Demultiplexer",
                category = ComponentCategory.DEMULTIPLEXERS,
                inputs = listOf(inPort("DIN", "D", 0.2f), inPort("S0", "S0", 0.5f), inPort("S1", "S1", 0.7f), inPort("S2", "S2", 0.85f)),
                outputs = (0..7).map { outPort("Y$it", "Y$it", 0.12f + it * 0.11f) },
                width = 120f,
                height = 140f,
                description = "Routes data input D to one of 8 outputs.",
                booleanFormula = "Y_i = D when S[2:0] == i",
                evaluate = { inp, _ ->
                    val d = inp["DIN"] ?: DigitalState.UNKNOWN
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    val s2 = inp["S2"] ?: DigitalState.UNKNOWN
                    if (s0 == DigitalState.UNKNOWN || s1 == DigitalState.UNKNOWN || s2 == DigitalState.UNKNOWN) {
                        (0..7).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        val sel = (if (s0 == DigitalState.HIGH) 1 else 0) + (if (s1 == DigitalState.HIGH) 2 else 0) + (if (s2 == DigitalState.HIGH) 4 else 0)
                        (0..7).associate { "Y$it" to if (it == sel) d else DigitalState.LOW }
                    }
                }
            ),
            ComponentDefinition(
                type = "DEMUX_1_16",
                name = "1:16 Demultiplexer",
                category = ComponentCategory.DEMULTIPLEXERS,
                inputs = listOf(inPort("DIN", "D", 0.1f), inPort("S0", "S0", 0.5f), inPort("S1", "S1", 0.65f), inPort("S2", "S2", 0.8f), inPort("S3", "S3", 0.9f)),
                outputs = (0..15).map { outPort("Y$it", "Y$it", 0.05f + it * 0.06f) },
                width = 130f,
                height = 190f,
                description = "1-to-16 data distributor.",
                booleanFormula = "Y_i = D when S[3:0] == i",
                evaluate = { inp, _ ->
                    val d = inp["DIN"] ?: DigitalState.UNKNOWN
                    val s0 = inp["S0"] ?: DigitalState.UNKNOWN
                    val s1 = inp["S1"] ?: DigitalState.UNKNOWN
                    val s2 = inp["S2"] ?: DigitalState.UNKNOWN
                    val s3 = inp["S3"] ?: DigitalState.UNKNOWN
                    if (listOf(s0, s1, s2, s3).any { it == DigitalState.UNKNOWN }) {
                        (0..15).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        var sel = 0
                        if (s0 == DigitalState.HIGH) sel += 1
                        if (s1 == DigitalState.HIGH) sel += 2
                        if (s2 == DigitalState.HIGH) sel += 4
                        if (s3 == DigitalState.HIGH) sel += 8
                        (0..15).associate { "Y$it" to if (it == sel) d else DigitalState.LOW }
                    }
                }
            ),

            // ==================== ENCODERS ====================
            ComponentDefinition(
                type = "ENCODER_4_2",
                name = "4:2 Encoder",
                category = ComponentCategory.ENCODERS,
                inputs = (0..3).map { inPort("D$it", "D$it", 0.2f + it * 0.2f) },
                outputs = listOf(outPort("Y1", "Y1", 0.35f), outPort("Y0", "Y0", 0.65f)),
                width = 110f,
                height = 80f,
                description = "Encodes 4 active-high inputs into 2-bit binary code (assumes one input HIGH).",
                booleanFormula = "Y0 = D1 + D3, Y1 = D2 + D3",
                evaluate = { inp, _ ->
                    val d1 = inp["D1"] == DigitalState.HIGH
                    val d2 = inp["D2"] == DigitalState.HIGH
                    val d3 = inp["D3"] == DigitalState.HIGH
                    mapOf(
                        "Y0" to if (d1 || d3) DigitalState.HIGH else DigitalState.LOW,
                        "Y1" to if (d2 || d3) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "PRIORITY_ENCODER_4_2",
                name = "4:2 Priority Encoder",
                category = ComponentCategory.ENCODERS,
                inputs = (0..3).map { inPort("D$it", "D$it", 0.2f + it * 0.2f) },
                outputs = listOf(outPort("Y1", "Y1", 0.3f), outPort("Y0", "Y0", 0.5f), outPort("V", "V", 0.7f)),
                width = 120f,
                height = 90f,
                description = "4:2 encoder resolving multiple HIGH inputs by priority (D3 highest) with Valid bit V.",
                booleanFormula = "Highest index active input is encoded",
                evaluate = { inp, _ ->
                    val d3 = inp["D3"] == DigitalState.HIGH
                    val d2 = inp["D2"] == DigitalState.HIGH
                    val d1 = inp["D1"] == DigitalState.HIGH
                    val d0 = inp["D0"] == DigitalState.HIGH
                    when {
                        d3 -> mapOf("Y1" to DigitalState.HIGH, "Y0" to DigitalState.HIGH, "V" to DigitalState.HIGH)
                        d2 -> mapOf("Y1" to DigitalState.HIGH, "Y0" to DigitalState.LOW, "V" to DigitalState.HIGH)
                        d1 -> mapOf("Y1" to DigitalState.LOW, "Y0" to DigitalState.HIGH, "V" to DigitalState.HIGH)
                        d0 -> mapOf("Y1" to DigitalState.LOW, "Y0" to DigitalState.LOW, "V" to DigitalState.HIGH)
                        else -> mapOf("Y1" to DigitalState.LOW, "Y0" to DigitalState.LOW, "V" to DigitalState.LOW)
                    }
                }
            ),
            ComponentDefinition(
                type = "ENCODER_8_3",
                name = "8:3 Encoder",
                category = ComponentCategory.ENCODERS,
                inputs = (0..7).map { inPort("D$it", "D$it", 0.12f + it * 0.11f) },
                outputs = listOf(outPort("Y2", "Y2", 0.3f), outPort("Y1", "Y1", 0.5f), outPort("Y0", "Y0", 0.7f)),
                width = 120f,
                height = 140f,
                description = "Standard 8-line to 3-line octal to binary encoder.",
                booleanFormula = "Y2 = D4+D5+D6+D7, Y1 = D2+D3+D6+D7, Y0 = D1+D3+D5+D7",
                evaluate = { inp, _ ->
                    val d = (0..7).map { inp["D$it"] == DigitalState.HIGH }
                    mapOf(
                        "Y2" to if (d[4] || d[5] || d[6] || d[7]) DigitalState.HIGH else DigitalState.LOW,
                        "Y1" to if (d[2] || d[3] || d[6] || d[7]) DigitalState.HIGH else DigitalState.LOW,
                        "Y0" to if (d[1] || d[3] || d[5] || d[7]) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "PRIORITY_ENCODER_8_3",
                name = "8:3 Priority Encoder",
                category = ComponentCategory.ENCODERS,
                inputs = (0..7).map { inPort("D$it", "D$it", 0.12f + it * 0.11f) },
                outputs = listOf(
                    outPort("Y2", "Y2", 0.25f),
                    outPort("Y1", "Y1", 0.45f),
                    outPort("Y0", "Y0", 0.65f),
                    outPort("V", "V", 0.85f)
                ),
                width = 130f,
                height = 150f,
                description = "8-to-3 priority encoder (74LS148 logic) with valid flag V.",
                booleanFormula = "Y[2:0] = Binary code of highest active D",
                evaluate = { inp, _ ->
                    var active = -1
                    for (i in 7 downTo 0) {
                        if (inp["D$i"] == DigitalState.HIGH) {
                            active = i
                            break
                        }
                    }
                    if (active == -1) {
                        mapOf("Y2" to DigitalState.LOW, "Y1" to DigitalState.LOW, "Y0" to DigitalState.LOW, "V" to DigitalState.LOW)
                    } else {
                        mapOf(
                            "Y2" to if ((active and 4) != 0) DigitalState.HIGH else DigitalState.LOW,
                            "Y1" to if ((active and 2) != 0) DigitalState.HIGH else DigitalState.LOW,
                            "Y0" to if ((active and 1) != 0) DigitalState.HIGH else DigitalState.LOW,
                            "V" to DigitalState.HIGH
                        )
                    }
                }
            ),
            ComponentDefinition(
                type = "ENCODER_10_4",
                name = "Decimal to BCD Encoder",
                category = ComponentCategory.ENCODERS,
                inputs = (0..9).map { inPort("D$it", "D$it", 0.08f + it * 0.09f) },
                outputs = listOf(
                    outPort("Y3", "Y3", 0.25f),
                    outPort("Y2", "Y2", 0.45f),
                    outPort("Y1", "Y1", 0.65f),
                    outPort("Y0", "Y0", 0.85f)
                ),
                width = 130f,
                height = 160f,
                description = "Converts 10 decimal lines (0-9) to 4-bit 8421 BCD code.",
                booleanFormula = "Y[3:0] = BCD(highest active digit)",
                evaluate = { inp, _ ->
                    var active = -1
                    for (i in 9 downTo 0) {
                        if (inp["D$i"] == DigitalState.HIGH) {
                            active = i
                            break
                        }
                    }
                    val code = if (active >= 0) active else 0
                    mapOf(
                        "Y3" to if ((code and 8) != 0) DigitalState.HIGH else DigitalState.LOW,
                        "Y2" to if ((code and 4) != 0) DigitalState.HIGH else DigitalState.LOW,
                        "Y1" to if ((code and 2) != 0) DigitalState.HIGH else DigitalState.LOW,
                        "Y0" to if ((code and 1) != 0) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),

            // ==================== DECODERS ====================
            ComponentDefinition(
                type = "DECODER_2_4",
                name = "2:4 Decoder",
                category = ComponentCategory.DECODERS,
                inputs = listOf(
                    inPort("A0", "A0", 0.3f),
                    inPort("A1", "A1", 0.5f),
                    inPort("EN", "EN", 0.75f)
                ),
                outputs = (0..3).map { outPort("Y$it", "Y$it", 0.2f + it * 0.2f) },
                width = 110f,
                height = 90f,
                description = "2-to-4 binary line decoder with Active-High Enable (EN).",
                booleanFormula = "Y_i = EN · (A == i)",
                evaluate = { inp, _ ->
                    val en = inp["EN"] ?: DigitalState.HIGH // Default enabled if floating
                    val a0 = inp["A0"] ?: DigitalState.UNKNOWN
                    val a1 = inp["A1"] ?: DigitalState.UNKNOWN
                    if (en == DigitalState.LOW) {
                        (0..3).associate { "Y$it" to DigitalState.LOW }
                    } else if (a0 == DigitalState.UNKNOWN || a1 == DigitalState.UNKNOWN) {
                        (0..3).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        val sel = (if (a0 == DigitalState.HIGH) 1 else 0) + (if (a1 == DigitalState.HIGH) 2 else 0)
                        (0..3).associate { "Y$it" to if (it == sel) DigitalState.HIGH else DigitalState.LOW }
                    }
                }
            ),
            ComponentDefinition(
                type = "DECODER_3_8",
                name = "3:8 Decoder",
                category = ComponentCategory.DECODERS,
                inputs = listOf(
                    inPort("A0", "A0", 0.25f),
                    inPort("A1", "A1", 0.45f),
                    inPort("A2", "A2", 0.65f),
                    inPort("EN", "EN", 0.85f)
                ),
                outputs = (0..7).map { outPort("Y$it", "Y$it", 0.12f + it * 0.11f) },
                width = 120f,
                height = 140f,
                description = "3-line to 8-line binary decoder with Enable (74LS138 style active-high).",
                booleanFormula = "Y_i = EN · (A == i)",
                evaluate = { inp, _ ->
                    val en = inp["EN"] ?: DigitalState.HIGH
                    val a0 = inp["A0"] ?: DigitalState.UNKNOWN
                    val a1 = inp["A1"] ?: DigitalState.UNKNOWN
                    val a2 = inp["A2"] ?: DigitalState.UNKNOWN
                    if (en == DigitalState.LOW) {
                        (0..7).associate { "Y$it" to DigitalState.LOW }
                    } else if (listOf(a0, a1, a2).any { it == DigitalState.UNKNOWN }) {
                        (0..7).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        val sel = (if (a0 == DigitalState.HIGH) 1 else 0) +
                                (if (a1 == DigitalState.HIGH) 2 else 0) +
                                (if (a2 == DigitalState.HIGH) 4 else 0)
                        (0..7).associate { "Y$it" to if (it == sel) DigitalState.HIGH else DigitalState.LOW }
                    }
                }
            ),
            ComponentDefinition(
                type = "DECODER_4_16",
                name = "4:16 Decoder",
                category = ComponentCategory.DECODERS,
                inputs = listOf(
                    inPort("A0", "A0", 0.2f),
                    inPort("A1", "A1", 0.4f),
                    inPort("A2", "A2", 0.6f),
                    inPort("A3", "A3", 0.8f)
                ),
                outputs = (0..15).map { outPort("Y$it", "Y$it", 0.05f + it * 0.06f) },
                width = 130f,
                height = 190f,
                description = "4-to-16 binary decoder. Exactly one output is HIGH corresponding to input binary value.",
                booleanFormula = "Y_i = (A[3:0] == i)",
                evaluate = { inp, _ ->
                    val a0 = inp["A0"] ?: DigitalState.UNKNOWN
                    val a1 = inp["A1"] ?: DigitalState.UNKNOWN
                    val a2 = inp["A2"] ?: DigitalState.UNKNOWN
                    val a3 = inp["A3"] ?: DigitalState.UNKNOWN
                    if (listOf(a0, a1, a2, a3).any { it == DigitalState.UNKNOWN }) {
                        (0..15).associate { "Y$it" to DigitalState.UNKNOWN }
                    } else {
                        var sel = 0
                        if (a0 == DigitalState.HIGH) sel += 1
                        if (a1 == DigitalState.HIGH) sel += 2
                        if (a2 == DigitalState.HIGH) sel += 4
                        if (a3 == DigitalState.HIGH) sel += 8
                        (0..15).associate { "Y$it" to if (it == sel) DigitalState.HIGH else DigitalState.LOW }
                    }
                }
            ),
            ComponentDefinition(
                type = "DECODER_BCD_DEC",
                name = "BCD to Decimal Decoder",
                category = ComponentCategory.DECODERS,
                inputs = listOf(
                    inPort("A", "A", 0.2f),
                    inPort("B", "B", 0.4f),
                    inPort("C", "C", 0.6f),
                    inPort("D", "D", 0.8f)
                ),
                outputs = (0..9).map { outPort("Y$it", "Y$it", 0.08f + it * 0.09f) },
                width = 130f,
                height = 160f,
                description = "Decodes 4-bit BCD (8421) into one of ten decimal outputs 0 through 9.",
                booleanFormula = "Y_i = (BCD == i), i ∈ [0..9]",
                evaluate = { inp, _ ->
                    val a = inp["A"] == DigitalState.HIGH
                    val b = inp["B"] == DigitalState.HIGH
                    val c = inp["C"] == DigitalState.HIGH
                    val d = inp["D"] == DigitalState.HIGH
                    val valDec = (if (a) 1 else 0) + (if (b) 2 else 0) + (if (c) 4 else 0) + (if (d) 8 else 0)
                    (0..9).associate { "Y$it" to if (it == valDec) DigitalState.HIGH else DigitalState.LOW }
                }
            ),

            // ==================== OTHER COMBINATIONAL CIRCUITS ====================
            ComponentDefinition(
                type = "COMPARATOR_1BIT",
                name = "1-bit Comparator",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(inPort("A", "A", 0.35f), inPort("B", "B", 0.65f)),
                outputs = listOf(
                    outPort("GT", "A>B", 0.25f),
                    outPort("EQ", "A=B", 0.5f),
                    outPort("LT", "A<B", 0.75f)
                ),
                width = 110f,
                height = 80f,
                description = "Compares two 1-bit inputs A and B.",
                booleanFormula = "GT = A·B', EQ = (A⊕B)', LT = A'·B",
                evaluate = { inp, _ ->
                    val a = inp["A"] ?: DigitalState.UNKNOWN
                    val b = inp["B"] ?: DigitalState.UNKNOWN
                    if (a == DigitalState.UNKNOWN || b == DigitalState.UNKNOWN) {
                        mapOf("GT" to DigitalState.UNKNOWN, "EQ" to DigitalState.UNKNOWN, "LT" to DigitalState.UNKNOWN)
                    } else {
                        val aV = if (a == DigitalState.HIGH) 1 else 0
                        val bV = if (b == DigitalState.HIGH) 1 else 0
                        mapOf(
                            "GT" to if (aV > bV) DigitalState.HIGH else DigitalState.LOW,
                            "EQ" to if (aV == bV) DigitalState.HIGH else DigitalState.LOW,
                            "LT" to if (aV < bV) DigitalState.HIGH else DigitalState.LOW
                        )
                    }
                }
            ),
            ComponentDefinition(
                type = "COMPARATOR_4BIT",
                name = "4-bit Magnitude Comparator",
                category = ComponentCategory.COMBINATIONAL,
                inputs = (0..3).map { inPort("A$it", "A$it", 0.1f + it * 0.1f) } +
                        (0..3).map { inPort("B$it", "B$it", 0.55f + it * 0.1f) },
                outputs = listOf(
                    outPort("GT", "A>B", 0.3f),
                    outPort("EQ", "A=B", 0.5f),
                    outPort("LT", "A<B", 0.7f)
                ),
                width = 130f,
                height = 140f,
                description = "4-bit magnitude comparator (74LS85 logic) comparing binary words A and B.",
                booleanFormula = "Outputs A>B, A=B, A<B",
                evaluate = { inp, _ ->
                    var a = 0
                    var b = 0
                    for (i in 0..3) {
                        if (inp["A$i"] == DigitalState.HIGH) a = a or (1 shl i)
                        if (inp["B$i"] == DigitalState.HIGH) b = b or (1 shl i)
                    }
                    mapOf(
                        "GT" to if (a > b) DigitalState.HIGH else DigitalState.LOW,
                        "EQ" to if (a == b) DigitalState.HIGH else DigitalState.LOW,
                        "LT" to if (a < b) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "PARITY_GEN",
                name = "Parity Generator",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(
                    inPort("A", "A", 0.2f),
                    inPort("B", "B", 0.4f),
                    inPort("C", "C", 0.6f),
                    inPort("D", "D", 0.8f)
                ),
                outputs = listOf(
                    outPort("EVEN", "Even", 0.35f),
                    outPort("ODD", "Odd", 0.65f)
                ),
                width = 120f,
                height = 90f,
                description = "Generates even and odd parity check bits for 4-bit data.",
                booleanFormula = "Even = A⊕B⊕C⊕D, Odd = (A⊕B⊕C⊕D)'",
                evaluate = { inp, _ ->
                    val highCount = listOf("A", "B", "C", "D").count { inp[it] == DigitalState.HIGH }
                    val isOdd = highCount % 2 != 0
                    mapOf(
                        "EVEN" to if (isOdd) DigitalState.HIGH else DigitalState.LOW,
                        "ODD" to if (!isOdd) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "PARITY_CHECK",
                name = "Parity Checker",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(
                    inPort("A", "A", 0.18f),
                    inPort("B", "B", 0.36f),
                    inPort("C", "C", 0.54f),
                    inPort("D", "D", 0.72f),
                    inPort("P", "P", 0.90f)
                ),
                outputs = listOf(
                    outPort("ERR_EVEN", "ErrEven", 0.35f),
                    outPort("ERR_ODD", "ErrOdd", 0.65f)
                ),
                width = 125f,
                height = 100f,
                description = "Checks 4-bit data + parity bit P for transmission parity errors.",
                booleanFormula = "ErrEven = A⊕B⊕C⊕D⊕P",
                evaluate = { inp, _ ->
                    val totalHigh = listOf("A", "B", "C", "D", "P").count { inp[it] == DigitalState.HIGH }
                    val oddParity = totalHigh % 2 != 0
                    mapOf(
                        "ERR_EVEN" to if (oddParity) DigitalState.HIGH else DigitalState.LOW,
                        "ERR_ODD" to if (!oddParity) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "BIN_TO_GRAY",
                name = "Binary to Gray Converter",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(
                    inPort("B3", "B3", 0.2f),
                    inPort("B2", "B2", 0.4f),
                    inPort("B1", "B1", 0.6f),
                    inPort("B0", "B0", 0.8f)
                ),
                outputs = listOf(
                    outPort("G3", "G3", 0.2f),
                    outPort("G2", "G2", 0.4f),
                    outPort("G1", "G1", 0.6f),
                    outPort("G0", "G0", 0.8f)
                ),
                width = 130f,
                height = 110f,
                description = "Converts 4-bit binary code B[3:0] into reflected Gray code G[3:0].",
                booleanFormula = "G3 = B3, G2 = B3⊕B2, G1 = B2⊕B1, G0 = B1⊕B0",
                evaluate = { inp, _ ->
                    val b3 = inp["B3"] == DigitalState.HIGH
                    val b2 = inp["B2"] == DigitalState.HIGH
                    val b1 = inp["B1"] == DigitalState.HIGH
                    val b0 = inp["B0"] == DigitalState.HIGH
                    mapOf(
                        "G3" to if (b3) DigitalState.HIGH else DigitalState.LOW,
                        "G2" to if (b3 xor b2) DigitalState.HIGH else DigitalState.LOW,
                        "G1" to if (b2 xor b1) DigitalState.HIGH else DigitalState.LOW,
                        "G0" to if (b1 xor b0) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "GRAY_TO_BIN",
                name = "Gray to Binary Converter",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(
                    inPort("G3", "G3", 0.2f),
                    inPort("G2", "G2", 0.4f),
                    inPort("G1", "G1", 0.6f),
                    inPort("G0", "G0", 0.8f)
                ),
                outputs = listOf(
                    outPort("B3", "B3", 0.2f),
                    outPort("B2", "B2", 0.4f),
                    outPort("B1", "B1", 0.6f),
                    outPort("B0", "B0", 0.8f)
                ),
                width = 130f,
                height = 110f,
                description = "Converts 4-bit Gray code G[3:0] into standard binary B[3:0].",
                booleanFormula = "B3=G3, B2=B3⊕G2, B1=B2⊕G1, B0=B1⊕G0",
                evaluate = { inp, _ ->
                    val g3 = inp["G3"] == DigitalState.HIGH
                    val g2 = inp["G2"] == DigitalState.HIGH
                    val g1 = inp["G1"] == DigitalState.HIGH
                    val g0 = inp["G0"] == DigitalState.HIGH
                    val b3 = g3
                    val b2 = b3 xor g2
                    val b1 = b2 xor g1
                    val b0 = b1 xor g0
                    mapOf(
                        "B3" to if (b3) DigitalState.HIGH else DigitalState.LOW,
                        "B2" to if (b2) DigitalState.HIGH else DigitalState.LOW,
                        "B1" to if (b1) DigitalState.HIGH else DigitalState.LOW,
                        "B0" to if (b0) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            ),
            ComponentDefinition(
                type = "BCD_TO_7SEG",
                name = "BCD to 7-Segment Decoder",
                category = ComponentCategory.COMBINATIONAL,
                inputs = listOf(
                    inPort("A", "A", 0.2f),
                    inPort("B", "B", 0.4f),
                    inPort("C", "C", 0.6f),
                    inPort("D", "D", 0.8f)
                ),
                outputs = listOf(
                    outPort("SA", "a", 0.12f),
                    outPort("SB", "b", 0.25f),
                    outPort("SC", "c", 0.38f),
                    outPort("SD", "d", 0.50f),
                    outPort("SE", "e", 0.63f),
                    outPort("SF", "f", 0.76f),
                    outPort("SG", "g", 0.88f)
                ),
                width = 130f,
                height = 140f,
                description = "Decodes 4-bit BCD into standard 7-segment display drives (74LS47 active-high).",
                booleanFormula = "Generates segments a-g for numerals 0-9",
                evaluate = { inp, _ ->
                    val a = inp["A"] == DigitalState.HIGH
                    val b = inp["B"] == DigitalState.HIGH
                    val c = inp["C"] == DigitalState.HIGH
                    val d = inp["D"] == DigitalState.HIGH
                    val digit = (if (a) 1 else 0) + (if (b) 2 else 0) + (if (c) 4 else 0) + (if (d) 8 else 0)

                    // Standard 7-segment mapping (a, b, c, d, e, f, g)
                    val segMap = mapOf(
                        0 to listOf(1, 1, 1, 1, 1, 1, 0),
                        1 to listOf(0, 1, 1, 0, 0, 0, 0),
                        2 to listOf(1, 1, 0, 1, 1, 0, 1),
                        3 to listOf(1, 1, 1, 1, 0, 0, 1),
                        4 to listOf(0, 1, 1, 0, 0, 1, 1),
                        5 to listOf(1, 0, 1, 1, 0, 1, 1),
                        6 to listOf(1, 0, 1, 1, 1, 1, 1),
                        7 to listOf(1, 1, 1, 0, 0, 0, 0),
                        8 to listOf(1, 1, 1, 1, 1, 1, 1),
                        9 to listOf(1, 1, 1, 1, 0, 1, 1)
                    )
                    val bits = segMap[digit] ?: listOf(0, 0, 0, 0, 0, 0, 0)
                    mapOf(
                        "SA" to if (bits[0] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SB" to if (bits[1] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SC" to if (bits[2] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SD" to if (bits[3] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SE" to if (bits[4] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SF" to if (bits[5] == 1) DigitalState.HIGH else DigitalState.LOW,
                        "SG" to if (bits[6] == 1) DigitalState.HIGH else DigitalState.LOW
                    )
                }
            )
        ).associateBy { it.type }
    }

    fun get(type: String): ComponentDefinition? = definitions[type]
}
