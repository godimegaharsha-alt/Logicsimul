package com.example.logiclab.model

enum class DigitalState(val symbol: String, val intValue: Int?) {
    LOW("0", 0),
    HIGH("1", 1),
    UNKNOWN("X", null);

    fun isHigh(): Boolean = this == HIGH
    fun isLow(): Boolean = this == LOW

    companion object {
        fun fromBoolean(value: Boolean): DigitalState = if (value) HIGH else LOW
        fun fromInt(value: Int): DigitalState = when (value) {
            0 -> LOW
            1 -> HIGH
            else -> UNKNOWN
        }
    }
}

enum class PortType {
    INPUT,
    OUTPUT
}

data class Port(
    val id: String,
    val name: String,
    val type: PortType,
    val relativeX: Float, // Normalized 0.0f .. 1.0f on component bounds
    val relativeY: Float,
    val bitIndex: Int = 0
)

enum class ComponentCategory(val title: String) {
    IO("Inputs & Outputs"),
    BASIC_GATES("Basic Gates"),
    SEQUENTIAL("Latches, Clocks & Memory"),
    ARITHMETIC("Arithmetic Circuits"),
    PRESETS("Presets & Adders"),
    MULTIPLEXERS("Multiplexers"),
    DEMULTIPLEXERS("Demultiplexers"),
    ENCODERS("Encoders"),
    DECODERS("Decoders"),
    COMBINATIONAL("Combinational Circuits")
}

enum class Board3DStyle(val label: String, val subtitle: String) {
    PCB_LAB("PCB Lab", "Dark green solder-masked circuit board"),
    IC_TRAINER_KIT("IC Trainer Kit", "Classic university digital lab IC trainer apparatus")
}

data class ComponentDefinition(
    val type: String,
    val name: String,
    val category: ComponentCategory,
    val inputs: List<Port>,
    val outputs: List<Port>,
    val width: Float = 100f,
    val height: Float = 80f,
    val description: String,
    val booleanFormula: String,
    val evaluate: (inputs: Map<String, DigitalState>, switchState: Boolean) -> Map<String, DigitalState>
)

data class CircuitComponent(
    val id: String,
    val type: String,
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Int = 0, // 0, 90, 180, 270
    val label: String = "",
    val switchState: Boolean = false, // For interactive switch
    val clockHz: Float = 1.0f,
    val inputValues: Map<String, DigitalState> = emptyMap(),
    val outputValues: Map<String, DigitalState> = emptyMap()
)

data class CircuitWire(
    val id: String,
    val fromComponentId: String,
    val fromPortId: String,
    val toComponentId: String,
    val toPortId: String,
    val state: DigitalState = DigitalState.UNKNOWN
)

enum class DiagnosticSeverity {
    ERROR,
    WARNING,
    INFO
}

data class CircuitDiagnostic(
    val severity: DiagnosticSeverity,
    val componentId: String?,
    val portId: String?,
    val message: String
)
