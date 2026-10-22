package com.example.logiclab.starters

import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire

data class StarterCircuit(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val components: List<CircuitComponent>,
    val wires: List<CircuitWire>
)

object StarterCircuits {

    val list: List<StarterCircuit> by lazy {
        listOf(
            createAndGateStarter(),
            createOscillatorLabStarter(),
            createHalfAdderStarter(),
            createFullAdderStarter(),
            createALU4BitStarter(),
            createMux4to1Starter(),
            createDemux1to4Starter(),
            createPriorityEncoderStarter(),
            createDecoder2to4Starter(),
            createDecoder3to8Starter(),
            createParityGenStarter(),
            createComparatorStarter(),
            createBcd7SegStarter(),
            createTrafficLightStarter()
        )
    }

    private fun createAndGateStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW1", type = "SWITCH", x = 100f, y = 140f, label = "Input A", switchState = true),
            CircuitComponent(id = "SW2", type = "SWITCH", x = 100f, y = 240f, label = "Input B", switchState = true),
            CircuitComponent(id = "U1", type = "AND", x = 300f, y = 180f, label = "AND Gate"),
            CircuitComponent(id = "LED1", type = "LED", x = 500f, y = 185f, label = "Y = A · B")
        )
        val wires = listOf(
            CircuitWire("w1", "SW1", "OUT", "U1", "A"),
            CircuitWire("w2", "SW2", "OUT", "U1", "B"),
            CircuitWire("w3", "U1", "Y", "LED1", "IN")
        )
        return StarterCircuit(
            id = "and_gate",
            name = "AND Gate Verification",
            category = "Basic Gates",
            description = "Basic 2-input AND gate with toggle switches and output LED indicator.",
            components = components,
            wires = wires
        )
    }

    private fun createHalfAdderStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 80f, y = 120f, label = "A", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 80f, y = 220f, label = "B", switchState = false),
            CircuitComponent(id = "HA1", type = "HALF_ADDER", x = 280f, y = 160f, label = "Half Adder"),
            CircuitComponent(id = "LED_S", type = "LED", x = 500f, y = 140f, label = "SUM"),
            CircuitComponent(id = "LED_C", type = "LED", x = 500f, y = 220f, label = "CARRY")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "HA1", "A"),
            CircuitWire("w2", "SW_B", "OUT", "HA1", "B"),
            CircuitWire("w3", "HA1", "SUM", "LED_S", "IN"),
            CircuitWire("w4", "HA1", "CARRY", "LED_C", "IN")
        )
        return StarterCircuit(
            id = "half_adder",
            name = "Half Adder",
            category = "Arithmetic",
            description = "Binary 1-bit half adder computing Sum = A ⊕ B and Carry = A · B.",
            components = components,
            wires = wires
        )
    }

    private fun createFullAdderStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 80f, y = 100f, label = "A", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 80f, y = 190f, label = "B", switchState = true),
            CircuitComponent(id = "SW_CIN", type = "SWITCH", x = 80f, y = 280f, label = "Cin", switchState = false),
            CircuitComponent(id = "FA1", type = "FULL_ADDER", x = 280f, y = 180f, label = "Full Adder"),
            CircuitComponent(id = "LED_SUM", type = "LED", x = 500f, y = 150f, label = "SUM"),
            CircuitComponent(id = "LED_COUT", type = "LED", x = 500f, y = 230f, label = "Cout")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "FA1", "A"),
            CircuitWire("w2", "SW_B", "OUT", "FA1", "B"),
            CircuitWire("w3", "SW_CIN", "OUT", "FA1", "CIN"),
            CircuitWire("w4", "FA1", "SUM", "LED_SUM", "IN"),
            CircuitWire("w5", "FA1", "COUT", "LED_COUT", "IN")
        )
        return StarterCircuit(
            id = "full_adder",
            name = "Full Adder",
            category = "Arithmetic",
            description = "3-input full adder circuit computing Sum and Carry-out.",
            components = components,
            wires = wires
        )
    }

    private fun createMux4to1Starter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_D0", type = "SWITCH", x = 60f, y = 80f, label = "D0", switchState = false),
            CircuitComponent(id = "SW_D1", type = "SWITCH", x = 60f, y = 150f, label = "D1", switchState = true),
            CircuitComponent(id = "SW_D2", type = "SWITCH", x = 60f, y = 220f, label = "D2", switchState = false),
            CircuitComponent(id = "SW_D3", type = "SWITCH", x = 60f, y = 290f, label = "D3", switchState = true),
            CircuitComponent(id = "SW_S0", type = "SWITCH", x = 60f, y = 370f, label = "S0", switchState = true),
            CircuitComponent(id = "SW_S1", type = "SWITCH", x = 60f, y = 440f, label = "S1", switchState = false),
            CircuitComponent(id = "MUX1", type = "MUX_4_1", x = 280f, y = 200f, label = "4:1 MUX"),
            CircuitComponent(id = "LED_Y", type = "LED", x = 500f, y = 220f, label = "Output Y")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_D0", "OUT", "MUX1", "D0"),
            CircuitWire("w2", "SW_D1", "OUT", "MUX1", "D1"),
            CircuitWire("w3", "SW_D2", "OUT", "MUX1", "D2"),
            CircuitWire("w4", "SW_D3", "OUT", "MUX1", "D3"),
            CircuitWire("w5", "SW_S0", "OUT", "MUX1", "S0"),
            CircuitWire("w6", "SW_S1", "OUT", "MUX1", "S1"),
            CircuitWire("w7", "MUX1", "Y", "LED_Y", "IN")
        )
        return StarterCircuit(
            id = "mux_4_1",
            name = "4:1 Multiplexer",
            category = "Multiplexers",
            description = "4-data input selector steered by two select lines S0 and S1.",
            components = components,
            wires = wires
        )
    }

    private fun createDemux1to4Starter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_DIN", type = "SWITCH", x = 60f, y = 100f, label = "Data In", switchState = true),
            CircuitComponent(id = "SW_S0", type = "SWITCH", x = 60f, y = 200f, label = "S0", switchState = false),
            CircuitComponent(id = "SW_S1", type = "SWITCH", x = 60f, y = 280f, label = "S1", switchState = true),
            CircuitComponent(id = "DEMUX1", type = "DEMUX_1_4", x = 280f, y = 160f, label = "1:4 DEMUX"),
            CircuitComponent(id = "LED_Y0", type = "LED", x = 500f, y = 90f, label = "Y0"),
            CircuitComponent(id = "LED_Y1", type = "LED", x = 500f, y = 160f, label = "Y1"),
            CircuitComponent(id = "LED_Y2", type = "LED", x = 500f, y = 230f, label = "Y2"),
            CircuitComponent(id = "LED_Y3", type = "LED", x = 500f, y = 300f, label = "Y3")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_DIN", "OUT", "DEMUX1", "DIN"),
            CircuitWire("w2", "SW_S0", "OUT", "DEMUX1", "S0"),
            CircuitWire("w3", "SW_S1", "OUT", "DEMUX1", "S1"),
            CircuitWire("w4", "DEMUX1", "Y0", "LED_Y0", "IN"),
            CircuitWire("w5", "DEMUX1", "Y1", "LED_Y1", "IN"),
            CircuitWire("w6", "DEMUX1", "Y2", "LED_Y2", "IN"),
            CircuitWire("w7", "DEMUX1", "Y3", "LED_Y3", "IN")
        )
        return StarterCircuit(
            id = "demux_1_4",
            name = "1:4 Demultiplexer",
            category = "Demultiplexers",
            description = "Routes a single input signal to one of four outputs based on address lines.",
            components = components,
            wires = wires
        )
    }

    private fun createPriorityEncoderStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_D0", type = "SWITCH", x = 60f, y = 80f, label = "D0", switchState = false),
            CircuitComponent(id = "SW_D1", type = "SWITCH", x = 60f, y = 150f, label = "D1", switchState = true),
            CircuitComponent(id = "SW_D2", type = "SWITCH", x = 60f, y = 220f, label = "D2", switchState = true),
            CircuitComponent(id = "SW_D3", type = "SWITCH", x = 60f, y = 290f, label = "D3", switchState = false),
            CircuitComponent(id = "ENC1", type = "PRIORITY_ENCODER_4_2", x = 280f, y = 160f, label = "4:2 Priority Enc"),
            CircuitComponent(id = "LED_Y1", type = "LED", x = 500f, y = 120f, label = "Y1 (MSB)"),
            CircuitComponent(id = "LED_Y0", type = "LED", x = 500f, y = 190f, label = "Y0 (LSB)"),
            CircuitComponent(id = "LED_V", type = "LED", x = 500f, y = 260f, label = "V (Valid)")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_D0", "OUT", "ENC1", "D0"),
            CircuitWire("w2", "SW_D1", "OUT", "ENC1", "D1"),
            CircuitWire("w3", "SW_D2", "OUT", "ENC1", "D2"),
            CircuitWire("w4", "SW_D3", "OUT", "ENC1", "D3"),
            CircuitWire("w5", "ENC1", "Y1", "LED_Y1", "IN"),
            CircuitWire("w6", "ENC1", "Y0", "LED_Y0", "IN"),
            CircuitWire("w7", "ENC1", "V", "LED_V", "IN")
        )
        return StarterCircuit(
            id = "priority_encoder_4_2",
            name = "4:2 Priority Encoder",
            category = "Encoders",
            description = "Priority encoder prioritizing higher index inputs and reporting validity.",
            components = components,
            wires = wires
        )
    }

    private fun createDecoder2to4Starter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A0", type = "SWITCH", x = 60f, y = 120f, label = "A0", switchState = false),
            CircuitComponent(id = "SW_A1", type = "SWITCH", x = 60f, y = 200f, label = "A1", switchState = true),
            CircuitComponent(id = "SW_EN", type = "SWITCH", x = 60f, y = 280f, label = "Enable", switchState = true),
            CircuitComponent(id = "DEC1", type = "DECODER_2_4", x = 280f, y = 180f, label = "2:4 Decoder"),
            CircuitComponent(id = "LED_Y0", type = "LED", x = 500f, y = 100f, label = "Y0"),
            CircuitComponent(id = "LED_Y1", type = "LED", x = 500f, y = 170f, label = "Y1"),
            CircuitComponent(id = "LED_Y2", type = "LED", x = 500f, y = 240f, label = "Y2"),
            CircuitComponent(id = "LED_Y3", type = "LED", x = 500f, y = 310f, label = "Y3")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A0", "OUT", "DEC1", "A0"),
            CircuitWire("w2", "SW_A1", "OUT", "DEC1", "A1"),
            CircuitWire("w3", "SW_EN", "OUT", "DEC1", "EN"),
            CircuitWire("w4", "DEC1", "Y0", "LED_Y0", "IN"),
            CircuitWire("w5", "DEC1", "Y1", "LED_Y1", "IN"),
            CircuitWire("w6", "DEC1", "Y2", "LED_Y2", "IN"),
            CircuitWire("w7", "DEC1", "Y3", "LED_Y3", "IN")
        )
        return StarterCircuit(
            id = "decoder_2_4",
            name = "2:4 Decoder with Enable",
            category = "Decoders",
            description = "Active-high 2:4 decoder highlighting the addressed output line.",
            components = components,
            wires = wires
        )
    }

    private fun createDecoder3to8Starter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A0", type = "SWITCH", x = 60f, y = 100f, label = "A0", switchState = true),
            CircuitComponent(id = "SW_A1", type = "SWITCH", x = 60f, y = 180f, label = "A1", switchState = false),
            CircuitComponent(id = "SW_A2", type = "SWITCH", x = 60f, y = 260f, label = "A2", switchState = true),
            CircuitComponent(id = "SW_EN", type = "SWITCH", x = 60f, y = 340f, label = "Enable", switchState = true),
            CircuitComponent(id = "DEC1", type = "DECODER_3_8", x = 280f, y = 180f, label = "3:8 Decoder"),
            CircuitComponent(id = "LED_Y5", type = "LED", x = 500f, y = 200f, label = "Y5 Selected")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A0", "OUT", "DEC1", "A0"),
            CircuitWire("w2", "SW_A1", "OUT", "DEC1", "A1"),
            CircuitWire("w3", "SW_A2", "OUT", "DEC1", "A2"),
            CircuitWire("w4", "SW_EN", "OUT", "DEC1", "EN"),
            CircuitWire("w5", "DEC1", "Y5", "LED_Y5", "IN")
        )
        return StarterCircuit(
            id = "decoder_3_8",
            name = "3:8 Decoder (74LS138)",
            category = "Decoders",
            description = "Standard octal line decoder decoding binary 101 into line Y5.",
            components = components,
            wires = wires
        )
    }

    private fun createParityGenStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 60f, y = 80f, label = "D0", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 60f, y = 160f, label = "D1", switchState = false),
            CircuitComponent(id = "SW_C", type = "SWITCH", x = 60f, y = 240f, label = "D2", switchState = true),
            CircuitComponent(id = "SW_D", type = "SWITCH", x = 60f, y = 320f, label = "D3", switchState = true),
            CircuitComponent(id = "P1", type = "PARITY_GEN", x = 280f, y = 180f, label = "Parity Generator"),
            CircuitComponent(id = "LED_EVEN", type = "LED", x = 500f, y = 150f, label = "Even Parity"),
            CircuitComponent(id = "LED_ODD", type = "LED", x = 500f, y = 230f, label = "Odd Parity")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "P1", "A"),
            CircuitWire("w2", "SW_B", "OUT", "P1", "B"),
            CircuitWire("w3", "SW_C", "OUT", "P1", "C"),
            CircuitWire("w4", "SW_D", "OUT", "P1", "D"),
            CircuitWire("w5", "P1", "EVEN", "LED_EVEN", "IN"),
            CircuitWire("w6", "P1", "ODD", "LED_ODD", "IN")
        )
        return StarterCircuit(
            id = "parity_gen",
            name = "Parity Generator (4-bit)",
            category = "Combinational",
            description = "Calculates even and odd parity check bits for 4-bit data transmissions.",
            components = components,
            wires = wires
        )
    }

    private fun createComparatorStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 80f, y = 120f, label = "A", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 80f, y = 220f, label = "B", switchState = false),
            CircuitComponent(id = "CMP1", type = "COMPARATOR_1BIT", x = 280f, y = 160f, label = "1-bit Comparator"),
            CircuitComponent(id = "LED_GT", type = "LED", x = 500f, y = 110f, label = "A > B"),
            CircuitComponent(id = "LED_EQ", type = "LED", x = 500f, y = 180f, label = "A = B"),
            CircuitComponent(id = "LED_LT", type = "LED", x = 500f, y = 250f, label = "A < B")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "CMP1", "A"),
            CircuitWire("w2", "SW_B", "OUT", "CMP1", "B"),
            CircuitWire("w3", "CMP1", "GT", "LED_GT", "IN"),
            CircuitWire("w4", "CMP1", "EQ", "LED_EQ", "IN"),
            CircuitWire("w5", "CMP1", "LT", "LED_LT", "IN")
        )
        return StarterCircuit(
            id = "comparator_1bit",
            name = "1-bit Magnitude Comparator",
            category = "Combinational",
            description = "Compares single-bit logic levels asserting A>B, A=B, or A<B.",
            components = components,
            wires = wires
        )
    }

    private fun createBcd7SegStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 60f, y = 100f, label = "A (1)", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 60f, y = 180f, label = "B (2)", switchState = false),
            CircuitComponent(id = "SW_C", type = "SWITCH", x = 60f, y = 260f, label = "C (4)", switchState = true),
            CircuitComponent(id = "SW_D", type = "SWITCH", x = 60f, y = 340f, label = "D (8)", switchState = false),
            CircuitComponent(id = "BCD1", type = "BCD_TO_7SEG", x = 270f, y = 200f, label = "BCD-to-7Seg"),
            CircuitComponent(id = "DISP1", type = "SEVEN_SEG", x = 500f, y = 190f, label = "Display")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "BCD1", "A"),
            CircuitWire("w2", "SW_B", "OUT", "BCD1", "B"),
            CircuitWire("w3", "SW_C", "OUT", "BCD1", "C"),
            CircuitWire("w4", "SW_D", "OUT", "BCD1", "D"),
            CircuitWire("w5", "BCD1", "SA", "DISP1", "A"),
            CircuitWire("w6", "BCD1", "SB", "DISP1", "B"),
            CircuitWire("w7", "BCD1", "SC", "DISP1", "C"),
            CircuitWire("w8", "BCD1", "SD", "DISP1", "D"),
            CircuitWire("w9", "BCD1", "SE", "DISP1", "E"),
            CircuitWire("w10", "BCD1", "SF", "DISP1", "F"),
            CircuitWire("w11", "BCD1", "SG", "DISP1", "G")
        )
        return StarterCircuit(
            id = "bcd_7seg",
            name = "BCD to 7-Segment Display",
            category = "Combinational",
            description = "Drives a 7-segment numerical indicator from 4-bit binary-coded decimal input (showing digit 5).",
            components = components,
            wires = wires
        )
    }

    private fun createOscillatorLabStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_PWR", type = "SWITCH", x = 80f, y = 160f, label = "Run Enable", switchState = true),
            CircuitComponent(id = "OSC1", type = "OSCILLATOR", x = 240f, y = 160f, label = "Oscillator Core"),
            CircuitComponent(id = "LED_PULSE", type = "LED", x = 440f, y = 140f, label = "Pulse LED"),
            CircuitComponent(id = "PROBE1", type = "DIGITAL_PROBE", x = 440f, y = 240f, label = "Frequency Probe")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_PWR", "OUT", "OSC1", "EN"),
            CircuitWire("w2", "OSC1", "OUT", "LED_PULSE", "IN"),
            CircuitWire("w3", "OSC1", "OUT", "PROBE1", "IN")
        )
        return StarterCircuit(
            id = "oscillator_lab",
            name = "Digital Oscillator Pulse Lab",
            category = "Clocks & Signal Gen",
            description = "High-precision digital oscillator pulse generator with live LED optical pulse and frequency probe telemetry.",
            components = components,
            wires = wires
        )
    }

    private fun createALU4BitStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "SW_A", type = "SWITCH", x = 80f, y = 120f, label = "Operand A", switchState = true),
            CircuitComponent(id = "SW_B", type = "SWITCH", x = 80f, y = 200f, label = "Operand B", switchState = true),
            CircuitComponent(id = "SW_CIN", type = "SWITCH", x = 80f, y = 280f, label = "Carry In", switchState = false),
            CircuitComponent(id = "ALU_CORE", type = "FULL_ADDER", x = 280f, y = 180f, label = "Arithmetic Logic Unit"),
            CircuitComponent(id = "LED_SUM", type = "LED", x = 500f, y = 140f, label = "Result SUM"),
            CircuitComponent(id = "LED_COUT", type = "LED", x = 500f, y = 240f, label = "Carry Out (Flag)")
        )
        val wires = listOf(
            CircuitWire("w1", "SW_A", "OUT", "ALU_CORE", "A"),
            CircuitWire("w2", "SW_B", "OUT", "ALU_CORE", "B"),
            CircuitWire("w3", "SW_CIN", "OUT", "ALU_CORE", "CIN"),
            CircuitWire("w4", "ALU_CORE", "SUM", "LED_SUM", "IN"),
            CircuitWire("w5", "ALU_CORE", "COUT", "LED_COUT", "IN")
        )
        return StarterCircuit(
            id = "alu_core",
            name = "4-Bit ALU Core Math Unit",
            category = "Complex Systems",
            description = "Fundamental computer arithmetic unit with dual operand inputs and carry flag computation.",
            components = components,
            wires = wires
        )
    }

    private fun createTrafficLightStarter(): StarterCircuit {
        val components = listOf(
            CircuitComponent(id = "CLK_MAIN", type = "CLOCK", x = 80f, y = 180f, label = "Timer Clock"),
            CircuitComponent(id = "DEC_ST", type = "DECODER_2TO4", x = 260f, y = 180f, label = "State Decoder"),
            CircuitComponent(id = "LED_RED", type = "LED", x = 480f, y = 110f, label = "RED STOP"),
            CircuitComponent(id = "LED_YEL", type = "LED", x = 480f, y = 180f, label = "YELLOW WARN"),
            CircuitComponent(id = "LED_GRN", type = "LED", x = 480f, y = 250f, label = "GREEN GO")
        )
        val wires = listOf(
            CircuitWire("w1", "CLK_MAIN", "OUT", "DEC_ST", "A"),
            CircuitWire("w2", "DEC_ST", "Y0", "LED_RED", "IN"),
            CircuitWire("w3", "DEC_ST", "Y1", "LED_YEL", "IN"),
            CircuitWire("w4", "DEC_ST", "Y2", "LED_GRN", "IN")
        )
        return StarterCircuit(
            id = "traffic_light",
            name = "Traffic Light Sequential Controller",
            category = "State Machines",
            description = "Automated sequential intersection signal controller transitioning between Red, Yellow, and Green lights.",
            components = components,
            wires = wires
        )
    }
}

