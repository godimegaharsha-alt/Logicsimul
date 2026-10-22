package com.example.logiclab.data

data class ICPin(
    val pinNumber: Int,
    val name: String,
    val description: String,
    val isPower: Boolean = false,
    val isGround: Boolean = false
)

data class ICDatasheet(
    val partNumber: String,
    val title: String,
    val family: String = "TTL / 74xx",
    val packageType: String = "DIP-14",
    val pinCount: Int = 14,
    val supplyVoltage: String = "5.0V ± 5% (4.75V - 5.25V)",
    val propDelay: String = "9.0 ns typical",
    val operatingTemp: String = "0°C to 70°C",
    val powerDissipation: String = "15 mW per gate",
    val description: String,
    val booleanFormula: String,
    val pins: List<ICPin>,
    val truthTableHeaders: List<String>,
    val truthTableRows: List<List<String>>,
    val equivalentComponentType: String
)

object ICDatasheetRegistry {

    val datasheets: List<ICDatasheet> by lazy {
        listOf(
            ICDatasheet(
                partNumber = "7400",
                title = "Quad 2-Input NAND Gates",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "9.0 ns (tPHL/tPLH)",
                description = "Contains four independent 2-input NAND gates in standard DIP-14 package. Universal gate capable of synthesizing any combinational Boolean function.",
                booleanFormula = "Y = (A • B)'",
                pins = listOf(
                    ICPin(1, "1A", "Gate 1 Input A"),
                    ICPin(2, "1B", "Gate 1 Input B"),
                    ICPin(3, "1Y", "Gate 1 Output"),
                    ICPin(4, "2A", "Gate 2 Input A"),
                    ICPin(5, "2B", "Gate 2 Input B"),
                    ICPin(6, "2Y", "Gate 2 Output"),
                    ICPin(7, "GND", "Circuit Ground (0V)", isGround = true),
                    ICPin(8, "3Y", "Gate 3 Output"),
                    ICPin(9, "3A", "Gate 3 Input A"),
                    ICPin(10, "3B", "Gate 3 Input B"),
                    ICPin(11, "4Y", "Gate 4 Output"),
                    ICPin(12, "4A", "Gate 4 Input A"),
                    ICPin(13, "4B", "Gate 4 Input B"),
                    ICPin(14, "VCC", "Supply Voltage (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "B", "Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "1"),
                    listOf("0", "1", "1"),
                    listOf("1", "0", "1"),
                    listOf("1", "1", "0")
                ),
                equivalentComponentType = "NAND"
            ),
            ICDatasheet(
                partNumber = "7402",
                title = "Quad 2-Input NOR Gates",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "8.0 ns",
                description = "Contains four independent 2-input NOR gates. Note the inverted pin configuration with outputs located on the first pin of each gate pair.",
                booleanFormula = "Y = (A + B)'",
                pins = listOf(
                    ICPin(1, "1Y", "Gate 1 Output"),
                    ICPin(2, "1A", "Gate 1 Input A"),
                    ICPin(3, "1B", "Gate 1 Input B"),
                    ICPin(4, "2Y", "Gate 2 Output"),
                    ICPin(5, "2A", "Gate 2 Input A"),
                    ICPin(6, "2B", "Gate 2 Input B"),
                    ICPin(7, "GND", "Circuit Ground (0V)", isGround = true),
                    ICPin(8, "3A", "Gate 3 Input A"),
                    ICPin(9, "3B", "Gate 3 Input B"),
                    ICPin(10, "3Y", "Gate 3 Output"),
                    ICPin(11, "4A", "Gate 4 Input A"),
                    ICPin(12, "4B", "Gate 4 Input B"),
                    ICPin(13, "4Y", "Gate 4 Output"),
                    ICPin(14, "VCC", "Supply Voltage (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "B", "Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "1"),
                    listOf("0", "1", "0"),
                    listOf("1", "0", "0"),
                    listOf("1", "1", "0")
                ),
                equivalentComponentType = "NOR"
            ),
            ICDatasheet(
                partNumber = "7404",
                title = "Hex Inverter (NOT Gates)",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "9.5 ns",
                description = "Six independent inverting logic gates. Performs single-input Boolean complement operation with high-speed silicon-gate technology.",
                booleanFormula = "Y = A'",
                pins = listOf(
                    ICPin(1, "1A", "Gate 1 Input"),
                    ICPin(2, "1Y", "Gate 1 Output"),
                    ICPin(3, "2A", "Gate 2 Input"),
                    ICPin(4, "2Y", "Gate 2 Output"),
                    ICPin(5, "3A", "Gate 3 Input"),
                    ICPin(6, "3Y", "Gate 3 Output"),
                    ICPin(7, "GND", "Ground (0V)", isGround = true),
                    ICPin(8, "4Y", "Gate 4 Output"),
                    ICPin(9, "4A", "Gate 4 Input"),
                    ICPin(10, "5Y", "Gate 5 Output"),
                    ICPin(11, "5A", "Gate 5 Input"),
                    ICPin(12, "6Y", "Gate 6 Output"),
                    ICPin(13, "6A", "Gate 6 Input"),
                    ICPin(14, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "Y"),
                truthTableRows = listOf(
                    listOf("0", "1"),
                    listOf("1", "0")
                ),
                equivalentComponentType = "NOT"
            ),
            ICDatasheet(
                partNumber = "7408",
                title = "Quad 2-Input AND Gates",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "7.0 ns",
                description = "Four independent 2-input AND gates. Outputs logic HIGH if and only if both input terminals are at logic HIGH.",
                booleanFormula = "Y = A • B",
                pins = listOf(
                    ICPin(1, "1A", "Gate 1 Input A"),
                    ICPin(2, "1B", "Gate 1 Input B"),
                    ICPin(3, "1Y", "Gate 1 Output"),
                    ICPin(4, "2A", "Gate 2 Input A"),
                    ICPin(5, "2B", "Gate 2 Input B"),
                    ICPin(6, "2Y", "Gate 2 Output"),
                    ICPin(7, "GND", "Ground (0V)", isGround = true),
                    ICPin(8, "3Y", "Gate 3 Output"),
                    ICPin(9, "3A", "Gate 3 Input A"),
                    ICPin(10, "3B", "Gate 3 Input B"),
                    ICPin(11, "4Y", "Gate 4 Output"),
                    ICPin(12, "4A", "Gate 4 Input A"),
                    ICPin(13, "4B", "Gate 4 Input B"),
                    ICPin(14, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "B", "Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "0"),
                    listOf("0", "1", "0"),
                    listOf("1", "0", "0"),
                    listOf("1", "1", "1")
                ),
                equivalentComponentType = "AND"
            ),
            ICDatasheet(
                partNumber = "7432",
                title = "Quad 2-Input OR Gates",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "8.5 ns",
                description = "Four independent 2-input OR gates. Output is HIGH if either input A or input B is at logic HIGH.",
                booleanFormula = "Y = A + B",
                pins = listOf(
                    ICPin(1, "1A", "Gate 1 Input A"),
                    ICPin(2, "1B", "Gate 1 Input B"),
                    ICPin(3, "1Y", "Gate 1 Output"),
                    ICPin(4, "2A", "Gate 2 Input A"),
                    ICPin(5, "2B", "Gate 2 Input B"),
                    ICPin(6, "2Y", "Gate 2 Output"),
                    ICPin(7, "GND", "Ground (0V)", isGround = true),
                    ICPin(8, "3Y", "Gate 3 Output"),
                    ICPin(9, "3A", "Gate 3 Input A"),
                    ICPin(10, "3B", "Gate 3 Input B"),
                    ICPin(11, "4Y", "Gate 4 Output"),
                    ICPin(12, "4A", "Gate 4 Input A"),
                    ICPin(13, "4B", "Gate 4 Input B"),
                    ICPin(14, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "B", "Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "0"),
                    listOf("0", "1", "1"),
                    listOf("1", "0", "1"),
                    listOf("1", "1", "1")
                ),
                equivalentComponentType = "OR"
            ),
            ICDatasheet(
                partNumber = "7486",
                title = "Quad 2-Input Exclusive-OR (XOR) Gates",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "11.0 ns",
                description = "Four independent 2-input XOR gates. Fundamental building block for binary adders, parity checkers, and arithmetic logic circuits.",
                booleanFormula = "Y = A ⊕ B = A'B + AB'",
                pins = listOf(
                    ICPin(1, "1A", "Gate 1 Input A"),
                    ICPin(2, "1B", "Gate 1 Input B"),
                    ICPin(3, "1Y", "Gate 1 Output"),
                    ICPin(4, "2A", "Gate 2 Input A"),
                    ICPin(5, "2B", "Gate 2 Input B"),
                    ICPin(6, "2Y", "Gate 2 Output"),
                    ICPin(7, "GND", "Ground (0V)", isGround = true),
                    ICPin(8, "3Y", "Gate 3 Output"),
                    ICPin(9, "3A", "Gate 3 Input A"),
                    ICPin(10, "3B", "Gate 3 Input B"),
                    ICPin(11, "4Y", "Gate 4 Output"),
                    ICPin(12, "4A", "Gate 4 Input A"),
                    ICPin(13, "4B", "Gate 4 Input B"),
                    ICPin(14, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("A", "B", "Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "0"),
                    listOf("0", "1", "1"),
                    listOf("1", "0", "1"),
                    listOf("1", "1", "0")
                ),
                equivalentComponentType = "XOR"
            ),
            ICDatasheet(
                partNumber = "7474",
                title = "Dual D Positive-Edge Flip-Flop",
                packageType = "DIP-14",
                pinCount = 14,
                propDelay = "14.0 ns",
                description = "Dual D-type edge-triggered flip-flops with asynchronous Preset and Clear inputs. State transitions occur on the rising edge of clock signal.",
                booleanFormula = "Q(next) = D (on rising CLK)",
                pins = listOf(
                    ICPin(1, "1CLR", "FF 1 Async Clear (Active Low)"),
                    ICPin(2, "1D", "FF 1 Data Input"),
                    ICPin(3, "1CLK", "FF 1 Clock (Positive Edge)"),
                    ICPin(4, "1PRE", "FF 1 Async Preset (Active Low)"),
                    ICPin(5, "1Q", "FF 1 Output"),
                    ICPin(6, "1Q'", "FF 1 Inverted Output"),
                    ICPin(7, "GND", "Ground (0V)", isGround = true),
                    ICPin(8, "2Q'", "FF 2 Inverted Output"),
                    ICPin(9, "2Q", "FF 2 Output"),
                    ICPin(10, "2PRE", "FF 2 Async Preset (Active Low)"),
                    ICPin(11, "2CLK", "FF 2 Clock (Positive Edge)"),
                    ICPin(12, "2D", "FF 2 Data Input"),
                    ICPin(13, "2CLR", "FF 2 Async Clear (Active Low)"),
                    ICPin(14, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("PRE", "CLR", "CLK", "D", "Q", "Q'"),
                truthTableRows = listOf(
                    listOf("0", "1", "X", "X", "1", "0"),
                    listOf("1", "0", "X", "X", "0", "1"),
                    listOf("1", "1", "↑", "0", "0", "1"),
                    listOf("1", "1", "↑", "1", "1", "0")
                ),
                equivalentComponentType = "D_FLIP_FLOP"
            ),
            ICDatasheet(
                partNumber = "7447",
                title = "BCD to 7-Segment Decoder / Driver",
                packageType = "DIP-16",
                pinCount = 16,
                propDelay = "24.0 ns",
                description = "Active-low open-collector outputs designed to drive common-anode 7-segment displays directly. Features Lamp Test (LT) and Blanking Input (BI).",
                booleanFormula = "Decodes 4-bit BCD (A, B, C, D) to segments a..g",
                pins = listOf(
                    ICPin(1, "B", "BCD Input Bit 1"),
                    ICPin(2, "C", "BCD Input Bit 2"),
                    ICPin(3, "LT", "Lamp Test (Active Low)"),
                    ICPin(4, "BI/RBO", "Blanking Input / Ripple Blanking Output"),
                    ICPin(5, "RBI", "Ripple Blanking Input"),
                    ICPin(6, "D", "BCD Input Bit 3 (MSB)"),
                    ICPin(7, "A", "BCD Input Bit 0 (LSB)"),
                    ICPin(8, "GND", "Ground (0V)", isGround = true),
                    ICPin(9, "e", "Segment e Output"),
                    ICPin(10, "d", "Segment d Output"),
                    ICPin(11, "c", "Segment c Output"),
                    ICPin(12, "b", "Segment b Output"),
                    ICPin(13, "a", "Segment a Output"),
                    ICPin(14, "g", "Segment g Output"),
                    ICPin(15, "f", "Segment f Output"),
                    ICPin(16, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("BCD", "Digit", "Active Segments"),
                truthTableRows = listOf(
                    listOf("0000", "0", "a, b, c, d, e, f"),
                    listOf("0001", "1", "b, c"),
                    listOf("0010", "2", "a, b, d, e, g"),
                    listOf("0011", "3", "a, b, c, d, g"),
                    listOf("0100", "4", "b, c, f, g"),
                    listOf("0101", "5", "a, c, d, f, g")
                ),
                equivalentComponentType = "SEVEN_SEG"
            ),
            ICDatasheet(
                partNumber = "74138",
                title = "3-to-8 Line Decoder / Demultiplexer",
                packageType = "DIP-16",
                pinCount = 16,
                propDelay = "12.0 ns",
                description = "High-speed 3-line to 8-line decoder with 3 enable inputs (two active-low, one active-high). Widely used in memory address decoding.",
                booleanFormula = "Yi = (G1 • G2A' • G2B' • Mi)'",
                pins = listOf(
                    ICPin(1, "A", "Address Input A (LSB)"),
                    ICPin(2, "B", "Address Input B"),
                    ICPin(3, "C", "Address Input C (MSB)"),
                    ICPin(4, "G2A", "Enable Input 2A (Active Low)"),
                    ICPin(5, "G2B", "Enable Input 2B (Active Low)"),
                    ICPin(6, "G1", "Enable Input 1 (Active High)"),
                    ICPin(7, "Y7", "Output 7 (Active Low)"),
                    ICPin(8, "GND", "Ground (0V)", isGround = true),
                    ICPin(9, "Y6", "Output 6 (Active Low)"),
                    ICPin(10, "Y5", "Output 5 (Active Low)"),
                    ICPin(11, "Y4", "Output 4 (Active Low)"),
                    ICPin(12, "Y3", "Output 3 (Active Low)"),
                    ICPin(13, "Y2", "Output 2 (Active Low)"),
                    ICPin(14, "Y1", "Output 1 (Active Low)"),
                    ICPin(15, "Y0", "Output 0 (Active Low)"),
                    ICPin(16, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("Enable", "C", "B", "A", "Active Output"),
                truthTableRows = listOf(
                    listOf("Valid", "0", "0", "0", "Y0 = LOW"),
                    listOf("Valid", "0", "0", "1", "Y1 = LOW"),
                    listOf("Valid", "0", "1", "0", "Y2 = LOW"),
                    listOf("Valid", "1", "1", "1", "Y7 = LOW")
                ),
                equivalentComponentType = "DECODER_3_8"
            ),
            ICDatasheet(
                partNumber = "74151",
                title = "8-Line to 1-Line Data Selector / Multiplexer",
                packageType = "DIP-16",
                pinCount = 16,
                propDelay = "15.0 ns",
                description = "Selects one of eight binary data sources and routes it to complementary outputs (Y and W). Includes Strobe (Active Low enable).",
                booleanFormula = "Y = S' • (D0..D7 selected by S0,S1,S2)",
                pins = listOf(
                    ICPin(1, "D3", "Data Input 3"),
                    ICPin(2, "D2", "Data Input 2"),
                    ICPin(3, "D1", "Data Input 1"),
                    ICPin(4, "D0", "Data Input 0"),
                    ICPin(5, "Y", "Output"),
                    ICPin(6, "W", "Inverted Output (Y')"),
                    ICPin(7, "G", "Strobe Enable (Active Low)"),
                    ICPin(8, "GND", "Ground (0V)", isGround = true),
                    ICPin(9, "S", "Select C (MSB)"),
                    ICPin(10, "B", "Select B"),
                    ICPin(11, "A", "Select A (LSB)"),
                    ICPin(12, "D7", "Data Input 7"),
                    ICPin(13, "D6", "Data Input 6"),
                    ICPin(14, "D5", "Data Input 5"),
                    ICPin(15, "D4", "Data Input 4"),
                    ICPin(16, "VCC", "Supply (+5V)", isPower = true)
                ),
                truthTableHeaders = listOf("S2", "S1", "S0", "Output Y"),
                truthTableRows = listOf(
                    listOf("0", "0", "0", "D0"),
                    listOf("0", "0", "1", "D1"),
                    listOf("0", "1", "0", "D2"),
                    listOf("1", "1", "1", "D7")
                ),
                equivalentComponentType = "MUX_8_1"
            ),
            ICDatasheet(
                partNumber = "NE555",
                title = "Precision Timer & Digital Oscillator",
                packageType = "DIP-8",
                pinCount = 8,
                propDelay = "0.1 µs to Hours",
                description = "Standard silicon timer capable of generating accurate time delays or stable square wave oscillations in astable and monostable modes.",
                booleanFormula = "f = 1.44 / ((RA + 2RB) • C)",
                pins = listOf(
                    ICPin(1, "GND", "Ground (0V)", isGround = true),
                    ICPin(2, "TRIG", "Trigger (Active Low < 1/3 VCC)"),
                    ICPin(3, "OUT", "Clock Pulse Output"),
                    ICPin(4, "RESET", "Direct Reset (Active Low)"),
                    ICPin(5, "CTRL", "Control Voltage Bypass (2/3 VCC)"),
                    ICPin(6, "THRES", "Threshold Comparator (> 2/3 VCC)"),
                    ICPin(7, "DISCH", "Open Collector Discharge Transistor"),
                    ICPin(8, "VCC", "Supply Voltage (+4.5V to +15V)", isPower = true)
                ),
                truthTableHeaders = listOf("Trigger", "Threshold", "Reset", "Output"),
                truthTableRows = listOf(
                    listOf("X", "X", "0", "LOW"),
                    listOf("< 1/3 VCC", "X", "1", "HIGH"),
                    listOf("> 1/3 VCC", "> 2/3 VCC", "1", "LOW"),
                    listOf("> 1/3 VCC", "< 2/3 VCC", "1", "HOLD")
                ),
                equivalentComponentType = "OSCILLATOR"
            )
        )
    }
}
