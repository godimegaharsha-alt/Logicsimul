package com.example.logiclab.learn

data class ObservationRow(
    val inputValues: String,
    val expectedOutput: String,
    val observedOutput: String = "-"
)

data class LabExperiment(
    val id: String,
    val title: String,
    val category: String,
    val objective: String,
    val componentsRequired: List<String>,
    val procedure: List<String>,
    val expectedResult: String,
    val observationTable: List<ObservationRow>,
    val conclusion: String,
    val starterCircuitId: String? = null
)

object LabExperiments {

    val list: List<LabExperiment> = listOf(
        LabExperiment(
            id = "exp1",
            title = "Experiment 1: Verification of Logic Gate Truth Tables",
            category = "Basic Gates",
            objective = "To construct and verify the truth table of two-input AND, OR, and NOT gates using digital logic components.",
            componentsRequired = listOf(
                "2x Input Switches (A, B)",
                "1x 2-input AND Gate",
                "1x 2-input OR Gate",
                "1x NOT Gate (Inverter)",
                "Output LEDs",
                "Connecting wires"
            ),
            procedure = listOf(
                "1. Place two Input Switches on the schematic workspace representing inputs A and B.",
                "2. Place an AND Gate and connect Switch A to pin A and Switch B to pin B.",
                "3. Connect an Output LED to the AND gate's output pin Y.",
                "4. Toggle switches A and B through all binary combinations (00, 01, 10, 11).",
                "5. Observe and record whether the LED lights up for each input state."
            ),
            expectedResult = "AND gate output Y is HIGH (1) only when both inputs A and B are HIGH (1).",
            observationTable = listOf(
                ObservationRow("A=0, B=0", "Y=0"),
                ObservationRow("A=0, B=1", "Y=0"),
                ObservationRow("A=1, B=0", "Y=0"),
                ObservationRow("A=1, B=1", "Y=1")
            ),
            conclusion = "The experiment successfully verified that an AND gate functions as a logical conjunction, asserting HIGH only when all connected inputs are concurrently asserted.",
            starterCircuitId = "and_gate"
        ),
        LabExperiment(
            id = "exp2",
            title = "Experiment 2: Design and Realization of a Half Adder",
            category = "Arithmetic",
            objective = "To design, simulate, and verify a combinational Half Adder circuit to compute binary addition of two 1-bit numbers.",
            componentsRequired = listOf(
                "2x Input Switches (A, B)",
                "1x Half Adder Block (or XOR + AND gate)",
                "2x Output LEDs (SUM, CARRY)"
            ),
            procedure = listOf(
                "1. Place inputs A and B on the workspace.",
                "2. Connect A and B into the inputs of the Half Adder module.",
                "3. Connect the SUM output pin to LED_S and the CARRY output pin to LED_C.",
                "4. Cycle inputs through 0+0, 0+1, 1+0, and 1+1.",
                "5. Verify that SUM produces A ⊕ B and CARRY produces A · B."
            ),
            expectedResult = "0+0 = 0 (Carry 0), 0+1 = 1 (Carry 0), 1+0 = 1 (Carry 0), 1+1 = 0 with Carry 1.",
            observationTable = listOf(
                ObservationRow("A=0, B=0", "SUM=0, CARRY=0"),
                ObservationRow("A=0, B=1", "SUM=1, CARRY=0"),
                ObservationRow("A=1, B=0", "SUM=1, CARRY=0"),
                ObservationRow("A=1, B=1", "SUM=0, CARRY=1")
            ),
            conclusion = "The Half Adder adds two 1-bit inputs correctly without a carry-in stage, fulfilling the Boolean expressions S = A ⊕ B and C = A · B.",
            starterCircuitId = "half_adder"
        ),
        LabExperiment(
            id = "exp3",
            title = "Experiment 3: Implementation of a Full Adder Circuit",
            category = "Arithmetic",
            objective = "To design and test a Full Adder capable of adding two binary bits along with a Carry-in (Cin).",
            componentsRequired = listOf(
                "3x Input Switches (A, B, Cin)",
                "1x Full Adder module",
                "2x Output LEDs (SUM, Cout)"
            ),
            procedure = listOf(
                "1. Connect inputs A, B, and Cin to the Full Adder module.",
                "2. Connect the SUM output to an LED and Cout to a second LED.",
                "3. Systematically test all 8 input combinations from 000 to 111.",
                "4. Confirm that odd parity among inputs results in SUM=1, and 2 or more inputs result in Cout=1."
            ),
            expectedResult = "Full adder calculates A + B + Cin with proper carry lookahead propagation.",
            observationTable = listOf(
                ObservationRow("A=0, B=0, Cin=0", "SUM=0, Cout=0"),
                ObservationRow("A=0, B=0, Cin=1", "SUM=1, Cout=0"),
                ObservationRow("A=0, B=1, Cin=0", "SUM=1, Cout=0"),
                ObservationRow("A=0, B=1, Cin=1", "SUM=0, Cout=1"),
                ObservationRow("A=1, B=0, Cin=0", "SUM=1, Cout=0"),
                ObservationRow("A=1, B=0, Cin=1", "SUM=0, Cout=1"),
                ObservationRow("A=1, B=1, Cin=0", "SUM=0, Cout=1"),
                ObservationRow("A=1, B=1, Cin=1", "SUM=1, Cout=1")
            ),
            conclusion = "The Full Adder correctly performs 3-bit binary addition, enabling cascading into multi-bit parallel arithmetic logic units.",
            starterCircuitId = "full_adder"
        ),
        LabExperiment(
            id = "exp4",
            title = "Experiment 4: Verification of 4:1 Multiplexer",
            category = "Data Routing",
            objective = "To demonstrate digital data multiplexing by selecting one of four data channels using two binary address lines.",
            componentsRequired = listOf(
                "4x Data Input Switches (D0, D1, D2, D3)",
                "2x Select Address Switches (S0, S1)",
                "1x 4:1 Multiplexer",
                "1x Output LED (Y)"
            ),
            procedure = listOf(
                "1. Connect D0..D3 to the data inputs of the 4:1 MUX.",
                "2. Connect S0 and S1 to the address select lines.",
                "3. Wire output Y to an indicator LED.",
                "4. Set different test bit patterns on D0..D3.",
                "5. Change address lines S1 S0: 00 selects D0, 01 selects D1, 10 selects D2, 11 selects D3.",
                "6. Verify that changing the non-selected data lines has zero effect on output Y."
            ),
            expectedResult = "Output Y mirrors only the data line addressed by select lines S1,S0.",
            observationTable = listOf(
                ObservationRow("S1=0, S0=0", "Y = D0"),
                ObservationRow("S1=0, S0=1", "Y = D1"),
                ObservationRow("S1=1, S0=0", "Y = D2"),
                ObservationRow("S1=1, S0=1", "Y = D3")
            ),
            conclusion = "The 4:1 Multiplexer acts as a digitally controlled single-pole 4-throw selector, fundamental for bus multiplexing and functional logic realization.",
            starterCircuitId = "mux_4_1"
        ),
        LabExperiment(
            id = "exp5",
            title = "Experiment 5: Implementation of 3:8 Line Decoder",
            category = "Decoders",
            objective = "To decode a 3-bit binary address into 8 distinct mutually exclusive active-high output lines.",
            componentsRequired = listOf(
                "3x Address Switches (A0, A1, A2)",
                "1x Enable Switch (EN)",
                "1x 3:8 Decoder (74LS138 style)",
                "Output LEDs on Y0..Y7"
            ),
            procedure = listOf(
                "1. Connect address inputs A0, A1, A2 to the decoder.",
                "2. Assert Enable EN = 1.",
                "3. Sweep address A2 A1 A0 from 000 through 111.",
                "4. Check that exactly one output line is energized at any time corresponding to the binary value."
            ),
            expectedResult = "Only output line Y_i is HIGH where i = (A2*4 + A1*2 + A0). All other outputs remain LOW.",
            observationTable = listOf(
                ObservationRow("A2=0, A1=0, A0=0", "Y0=1, all other Y=0"),
                ObservationRow("A2=0, A1=1, A0=0", "Y2=1, all other Y=0"),
                ObservationRow("A2=1, A1=0, A0=1", "Y5=1, all other Y=0"),
                ObservationRow("A2=1, A1=1, A0=1", "Y7=1, all other Y=0")
            ),
            conclusion = "The 3:8 decoder successfully converted binary coded words into 1-of-8 spatial line activations, as used in memory chip selection.",
            starterCircuitId = "decoder_3_8"
        ),
        LabExperiment(
            id = "exp6",
            title = "Experiment 6: 4:2 Priority Encoder Verification",
            category = "Encoders",
            objective = "To study priority resolution when multiple inputs are concurrently asserted in a 4:2 Priority Encoder.",
            componentsRequired = listOf(
                "4x Data Switches (D0, D1, D2, D3)",
                "1x 4:2 Priority Encoder",
                "3x LEDs (Y1, Y0, and V - Valid bit)"
            ),
            procedure = listOf(
                "1. Connect switches D0..D3 to encoder inputs.",
                "2. Connect outputs Y1, Y0 and V to indicator LEDs.",
                "3. Test with no inputs HIGH -> confirm V=0.",
                "4. Test with multiple inputs HIGH simultaneously (e.g. D1 and D3 HIGH) -> confirm D3 wins priority and encodes 11.",
                "5. Observe how lower priority lines are masked by higher priority inputs."
            ),
            expectedResult = "Output represents the highest active input line index; V bit is 1 whenever any input is active.",
            observationTable = listOf(
                ObservationRow("D0=0, D1=0, D2=0, D3=0", "Y1=0, Y0=0, V=0 (No Request)"),
                ObservationRow("D0=1, D1=0, D2=0, D3=0", "Y1=0, Y0=0, V=1 (Input 0)"),
                ObservationRow("D0=1, D1=1, D2=0, D3=0", "Y1=0, Y0=1, V=1 (D1 overrides D0)"),
                ObservationRow("D0=1, D1=1, D2=1, D3=1", "Y1=1, Y0=1, V=1 (D3 overrides all)")
            ),
            conclusion = "The Priority Encoder reliably resolves arbitration conflicts by selecting the highest-order request, critical for interrupt controller architectures.",
            starterCircuitId = "priority_encoder_4_2"
        )
    )
}
