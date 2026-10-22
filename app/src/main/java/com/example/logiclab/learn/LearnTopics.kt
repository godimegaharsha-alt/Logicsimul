package com.example.logiclab.learn

data class LearnTopic(
    val id: String,
    val title: String,
    val category: String = "Digital Logic",
    val concept: String,
    val symbolDescription: String,
    val inputsOutputs: String,
    val booleanExpression: String,
    val truthTableSummary: String,
    val practicalApplication: String,
    val practiceQuestion: String,
    val practiceAnswer: String,
    val starterCircuitId: String? = null
)

object LearnTopics {

    val topics: List<LearnTopic> = listOf(
        LearnTopic(
            id = "gates",
            title = "Fundamental Logic Gates",
            category = "Basic Gates",
            concept = "Logic gates are the elementary building blocks of digital integrated circuits. They implement basic Boolean operations: Conjunction (AND), Disjunction (OR), and Inversion (NOT), along with universal gates (NAND, NOR) and parity gates (XOR, XNOR).",
            symbolDescription = "AND: Flat back, semi-circular front. OR: Curved back, pointed apex front. NOT: Triangle with inversion bubble at tip.",
            inputsOutputs = "Inputs: 2 or more digital lines (1 for NOT). Output: 1 binary line.",
            booleanExpression = "AND: Y = A · B\nOR: Y = A + B\nNOT: Y = A'\nNAND: Y = (A · B)'\nNOR: Y = (A + B)'\nXOR: Y = A ⊕ B\nXNOR: Y = (A ⊕ B)'",
            truthTableSummary = "AND: 0,0->0 | 0,1->0 | 1,0->0 | 1,1->1\nOR: 0,0->0 | 0,1->1 | 1,0->1 | 1,1->1\nXOR: 0,0->0 | 0,1->1 | 1,0->1 | 1,1->0",
            practicalApplication = "Found in all digital electronics, memory address decoders, condition tests, and microprocessors.",
            practiceQuestion = "Why are NAND and NOR gates called 'Universal Gates'?",
            practiceAnswer = "Because any Boolean function (AND, OR, NOT, etc.) can be constructed entirely using only NAND gates or only NOR gates without requiring any other gate type.",
            starterCircuitId = "and_gate"
        ),
        LearnTopic(
            id = "mux",
            title = "Multiplexers (Data Selectors)",
            concept = "A multiplexer (MUX) is a combinational circuit that selects binary information from one of many input lines and directs it to a single output line. The selection is controlled by a set of selection inputs.",
            symbolDescription = "Trapezoid or rectangular IC block with 2^n data inputs on the left, n select lines on the bottom/top, and 1 output on the right.",
            inputsOutputs = "2^n Data inputs (D0..D_{2^n-1}), n Select inputs (S0..S_{n-1}), 1 Output (Y).",
            booleanExpression = "2:1 MUX: Y = S'·D0 + S·D1\n4:1 MUX: Y = S1'·S0'·D0 + S1'·S0·D1 + S1·S0'·D2 + S1·S0·D3",
            truthTableSummary = "Select lines form a binary address pointing directly to the data line whose value is passed through to Y.",
            practicalApplication = "Data routing, parallel-to-serial conversion, time-division multiplexing (TDM), and implementing arbitrary logic functions without gate synthesis.",
            practiceQuestion = "How many select lines are required for a 32-to-1 Multiplexer?",
            practiceAnswer = "5 select lines (since 2^5 = 32).",
            starterCircuitId = "mux_4_1"
        ),
        LearnTopic(
            id = "demux",
            title = "Demultiplexers (Data Distributors)",
            concept = "A demultiplexer (DEMUX) takes a single data input and transmits it over one of 2^n possible output lines, selected by n address inputs. It is the functional opposite of a multiplexer.",
            symbolDescription = "Reverse trapezoid with 1 input on the left and 2^n outputs expanding to the right.",
            inputsOutputs = "1 Data input (Din), n Select inputs (S0..S_{n-1}), 2^n Outputs (Y0..Y_{2^n-1}).",
            booleanExpression = "Y_k = Din when S == k, else 0.",
            truthTableSummary = "The addressed output carries the signal of Din; all unselected outputs remain LOW (0).",
            practicalApplication = "Serial-to-parallel conversion, bus distribution, memory chip address line routing.",
            practiceQuestion = "What is the difference between a 1:4 Demultiplexer and a 2:4 Decoder?",
            practiceAnswer = "A 1:4 DEMUX has 1 data line routed by 2 select lines, whereas a 2:4 Decoder typically has 2 address inputs and an Enable pin. If the DEMUX data line is treated as an active-high Enable, they are functionally identical.",
            starterCircuitId = "demux_1_4"
        ),
        LearnTopic(
            id = "encoders",
            title = "Binary & Priority Encoders",
            concept = "An encoder converts 2^n input lines into an n-bit binary code. In a Priority Encoder, if two or more inputs arrive simultaneously, the input with the highest priority level takes precedence.",
            symbolDescription = "Rectangular IC chip with inputs on left, coded outputs on right, and a Valid (V) status output.",
            inputsOutputs = "2^n inputs (D0..D_{2^n-1}), n binary outputs (Y0..Y_{n-1}), 1 Valid output (V).",
            booleanExpression = "Standard 4:2: Y1 = D2+D3, Y0 = D1+D3. Priority Encoder masks lower bits when higher bits are active.",
            truthTableSummary = "If D3 is 1, Y=11 regardless of D0-D2. If only D0 is 1, Y=00 and V=1. If no lines are 1, V=0.",
            practicalApplication = "Microprocessor interrupt handling (e.g., NVIC in ARM), keyboard scan matrix encoding, analog-to-digital flash converters.",
            practiceQuestion = "What happens in a 4:2 standard encoder if D1 and D2 are both HIGH simultaneously?",
            practiceAnswer = "It produces an ambiguous or erroneous code (Y1=1, Y0=1 -> binary 3) because standard encoders assume strictly one input is HIGH. A priority encoder is required to resolve this ambiguity.",
            starterCircuitId = "priority_encoder_4_2"
        ),
        LearnTopic(
            id = "decoders",
            title = "Binary & BCD Decoders",
            concept = "A decoder is a combinational circuit that detects the presence of a specific combination of input bits and asserts a corresponding output line. An n-to-2^n decoder activates exactly 1 of 2^n output lines.",
            symbolDescription = "IC block with n binary inputs, optional Enable line (EN), and 2^n output lines.",
            inputsOutputs = "n input bits (A0..A_{n-1}), optional Enable (EN), 2^n output bits (Y0..Y_{2^n-1}).",
            booleanExpression = "Y_i = m_i (the i-th minterm). E.g., Y0 = A1'·A0', Y1 = A1'·A0, Y2 = A1·A0', Y3 = A1·A0.",
            truthTableSummary = "Each row in the truth table activates exactly one output line corresponding to the binary value represented by the inputs.",
            practicalApplication = "Microprocessor memory mapping, I/O port address selection, 7-segment display drivers.",
            practiceQuestion = "How many 3:8 decoders are required to build a 4:16 decoder?",
            practiceAnswer = "Two 3:8 decoders plus one inverter (NOT gate) connected to their Enable inputs to select between the upper and lower 8 outputs.",
            starterCircuitId = "decoder_2_4"
        ),
        LearnTopic(
            id = "adders",
            title = "Binary Adders (Half & Full)",
            concept = "Adders perform binary arithmetic. A Half Adder adds two 1-bit operands. A Full Adder adds three 1-bit inputs (A, B, and Carry-in Cin), allowing cascading into arbitrary n-bit binary adders.",
            symbolDescription = "HA or FA block with inputs on left and SUM, CARRY outputs on right.",
            inputsOutputs = "Half Adder: A, B -> SUM, CARRY. Full Adder: A, B, Cin -> SUM, Cout.",
            booleanExpression = "Half Adder: S = A ⊕ B, C = A · B\nFull Adder: S = A ⊕ B ⊕ Cin, Cout = (A·B) + (Cin · (A ⊕ B))",
            truthTableSummary = "SUM is 1 when an odd number of inputs are 1. CARRY is 1 when 2 or more inputs are 1.",
            practicalApplication = "Arithmetic Logic Units (ALUs) in CPUs and GPUs, floating-point units, address generation units.",
            practiceQuestion = "Can a Full Adder be built using two Half Adders and an OR gate?",
            practiceAnswer = "Yes. First HA adds A and B to get partial Sum and Carry1. Second HA adds partial Sum and Cin to get final Sum and Carry2. The OR gate combines Carry1 and Carry2 to produce Cout.",
            starterCircuitId = "full_adder"
        ),
        LearnTopic(
            id = "subtractors",
            title = "Binary Subtractors",
            concept = "Subtractors calculate the difference between two binary numbers and generate a borrow bit if the subtrahend is greater than the minuend.",
            symbolDescription = "HS (Half Subtractor) or FS (Full Subtractor) block with Difference (D) and Borrow (B) pins.",
            inputsOutputs = "Half Subtractor: A, B -> Diff, Borrow. Full Subtractor: A, B, Bin -> Diff, Bout.",
            booleanExpression = "Half Subtractor: Diff = A ⊕ B, Borrow = A' · B\nFull Subtractor: Diff = A ⊕ B ⊕ Bin, Bout = A'·B + (A ⊕ B)'·Bin",
            truthTableSummary = "When A=0, B=1: Diff=1, Borrow=1 (borrowing 2 from next column makes 2-1 = 1).",
            practicalApplication = "Arithmetic units, comparator magnitude logic, 2's complement computation.",
            practiceQuestion = "How do modern processors implement binary subtraction using adders?",
            practiceAnswer = "They invert the subtrahend (using XOR gates with a subtract control line) and assert Carry-in Cin=1, effectively calculating A + (-B) via 2's complement addition.",
            starterCircuitId = "half_adder"
        ),
        LearnTopic(
            id = "comparators",
            title = "Magnitude Comparators",
            concept = "A digital comparator examines two binary words A and B and determines whether A is greater than, equal to, or less than B.",
            symbolDescription = "IC block with word A inputs, word B inputs, and three status outputs: A > B, A = B, A < B.",
            inputsOutputs = "Inputs: A[n-1:0], B[n-1:0]. Outputs: GT (A>B), EQ (A=B), LT (A<B).",
            booleanExpression = "1-bit: EQ = (A ⊕ B)', GT = A · B', LT = A' · B.",
            truthTableSummary = "Exactly one of the three outputs is HIGH at any given time.",
            practicalApplication = "Sorting networks, boundary checkers, threshold monitoring in industrial sensors, CPU branch condition evaluation.",
            practiceQuestion = "For two 4-bit numbers, what is the condition for equality A = B?",
            practiceAnswer = "All corresponding bits must match: (A3 XNOR B3) AND (A2 XNOR B2) AND (A1 XNOR B1) AND (A0 XNOR B0) = 1.",
            starterCircuitId = "comparator_1bit"
        ),
        LearnTopic(
            id = "parity",
            title = "Parity Generators & Checkers",
            concept = "Parity is an error-detection technique where an extra bit (parity bit) is appended to a binary data word so that the total count of 1s is always even (Even Parity) or always odd (Odd Parity).",
            symbolDescription = "Multi-input XOR tree block with Even and Odd parity outputs.",
            inputsOutputs = "Data bits (D0..Dn) and optional received parity bit (P). Outputs: Parity / Error flag.",
            booleanExpression = "Even Parity = D0 ⊕ D1 ⊕ ... ⊕ Dn. Odd Parity is the complement (XNOR tree).",
            truthTableSummary = "If an odd number of bits in transmission flip, the parity check fails and an error is flagged.",
            practicalApplication = "UART serial communication, RAM error detection, RAID storage arrays, PCI bus data integrity.",
            practiceQuestion = "Can a simple single-bit parity check detect two bit errors in the same word?",
            practiceAnswer = "No. A single parity bit only detects odd numbers of bit flips (1, 3, 5). Two bit errors cancel each other out in the XOR sum. Double error detection requires CRC or Hamming codes.",
            starterCircuitId = "parity_gen"
        ),
        LearnTopic(
            id = "converters",
            title = "Code Converters (Binary ↔ Gray)",
            concept = "Gray code is an unweighted cyclic binary code in which adjacent values differ by exactly one bit. This eliminates false transient states (glitches) in mechanical encoders and asynchronous clock domain crossings.",
            symbolDescription = "Code conversion block with parallel bit lines.",
            inputsOutputs = "4-bit Binary inputs B[3:0] -> 4-bit Gray outputs G[3:0], or vice versa.",
            booleanExpression = "Bin to Gray: G3 = B3, G2 = B3 ⊕ B2, G1 = B2 ⊕ B1, G0 = B1 ⊕ B0.\nGray to Bin: B3 = G3, B2 = B3 ⊕ G2, B1 = B2 ⊕ G1, B0 = B1 ⊕ G0.",
            truthTableSummary = "Sequential values: 0->0000, 1->0001, 2->0011, 3->0010, 4->0110, 5->0111, etc.",
            practicalApplication = "Rotary optical shaft encoders, asynchronous FIFO pointers, Karnaugh map axis labeling.",
            practiceQuestion = "Why is Gray code preferred over standard binary for rotary shaft encoders?",
            practiceAnswer = "Because mechanical sensors cannot switch multiple bits simultaneously. In standard binary (e.g. 011 -> 100), all 3 bits change, risking brief erroneous readouts (e.g. 111). In Gray code, only 1 bit ever changes at a time.",
            starterCircuitId = "bcd_7seg"
        )
    )
}
