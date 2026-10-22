package com.example.logiclab.engine

import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitDiagnostic
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.ComponentDefinition
import com.example.logiclab.model.DiagnosticSeverity
import com.example.logiclab.model.DigitalState

data class SimulationResult(
    val components: List<CircuitComponent>,
    val wires: List<CircuitWire>,
    val diagnostics: List<CircuitDiagnostic>,
    val hasError: Boolean
)

object SimulationEngine {

    private const val MAX_PROPAGATION_STEPS = 50

    fun simulate(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>
    ): SimulationResult {
        val diagnostics = mutableListOf<CircuitDiagnostic>()
        var compMap = components.associateBy { it.id }.toMutableMap()
        var updatedWires = wires.toMutableList()

        // 1. Initial output evaluation for independent sources (Switches, Clocks, Consts, Oscillators)
        for ((id, comp) in compMap) {
            val def = ComponentRegistry.get(comp.type) ?: continue
            if (def.inputs.isEmpty() || comp.type == "OSCILLATOR" || comp.type == "CLOCK") {
                val outVals = def.evaluate(comp.inputValues, comp.switchState)
                compMap[id] = comp.copy(outputValues = outVals)
            }
        }

        // Check for shorted outputs: wires connecting two OUTPUT ports together
        val outputToWires = mutableMapOf<Pair<String, String>, MutableList<CircuitWire>>()
        for (w in wires) {
            val fromDef = ComponentRegistry.get(compMap[w.fromComponentId]?.type ?: "")
            val toDef = ComponentRegistry.get(compMap[w.toComponentId]?.type ?: "")

            val fromIsOut = fromDef?.let { isOutputPort(it, w.fromPortId) } == true
            val toIsOut = toDef?.let { isOutputPort(it, w.toPortId) } == true

            if (fromIsOut && toIsOut) {
                diagnostics.add(
                    CircuitDiagnostic(
                        severity = DiagnosticSeverity.ERROR,
                        componentId = w.fromComponentId,
                        portId = w.fromPortId,
                        message = "Shorted outputs! '${compMap[w.fromComponentId]?.label ?: w.fromComponentId}' and '${compMap[w.toComponentId]?.label ?: w.toComponentId}' are both driving the same line."
                    )
                )
            }
        }

        // 2. Iterative signal propagation
        var changed = true
        var stepCount = 0
        var feedbackLoopDetected = false

        while (changed && stepCount < MAX_PROPAGATION_STEPS) {
            changed = false
            stepCount++

            // Transfer signals along wires
            // A wire connects (fromComponent, fromPort) to (toComponent, toPort)
            // One end should be output, one end input (or bidirectional in user drag)
            val newWires = mutableListOf<CircuitWire>()
            val inputDrives = mutableMapOf<Pair<String, String>, MutableList<DigitalState>>()

            for (w in updatedWires) {
                val fromComp = compMap[w.fromComponentId]
                val toComp = compMap[w.toComponentId]
                val fromDef = fromComp?.let { ComponentRegistry.get(it.type) }
                val toDef = toComp?.let { ComponentRegistry.get(it.type) }

                val fromIsOut = fromDef?.let { isOutputPort(it, w.fromPortId) } == true
                val toIsOut = toDef?.let { isOutputPort(it, w.toPortId) } == true

                val wireState = when {
                    fromIsOut -> fromComp?.outputValues?.get(w.fromPortId)
                        ?: fromComp?.outputValues?.get("OUT")
                        ?: fromComp?.outputValues?.get("CLK")
                        ?: fromComp?.outputValues?.get("Y")
                        ?: fromComp?.outputValues?.get("Q")
                        ?: fromComp?.outputValues?.get("INV")
                        ?: fromComp?.outputValues?.get("Q_BAR")
                        ?: fromComp?.outputValues?.get("CLK'")
                        ?: DigitalState.UNKNOWN
                    toIsOut -> toComp?.outputValues?.get(w.toPortId)
                        ?: toComp?.outputValues?.get("OUT")
                        ?: toComp?.outputValues?.get("CLK")
                        ?: toComp?.outputValues?.get("Y")
                        ?: toComp?.outputValues?.get("Q")
                        ?: toComp?.outputValues?.get("INV")
                        ?: toComp?.outputValues?.get("Q_BAR")
                        ?: toComp?.outputValues?.get("CLK'")
                        ?: DigitalState.UNKNOWN
                    else -> DigitalState.UNKNOWN
                }

                newWires.add(w.copy(state = wireState))

                // Accumulate driver into target input
                if (fromIsOut) {
                    val targetPort = toDef?.inputs?.find { it.id == w.toPortId || it.name == w.toPortId }?.id ?: w.toPortId
                    inputDrives.getOrPut(Pair(w.toComponentId, targetPort)) { mutableListOf() }.add(wireState)
                } else if (toIsOut) {
                    val targetPort = fromDef?.inputs?.find { it.id == w.fromPortId || it.name == w.fromPortId }?.id ?: w.fromPortId
                    inputDrives.getOrPut(Pair(w.fromComponentId, targetPort)) { mutableListOf() }.add(wireState)
                }
            }
            updatedWires = newWires

            // Update components with new input values and evaluate outputs
            for ((id, comp) in compMap.toMap()) {
                val def = ComponentRegistry.get(comp.type) ?: continue
                if (def.inputs.isEmpty()) continue

                val newInputs = mutableMapOf<String, DigitalState>()
                for (inPort in def.inputs) {
                    val drivers = inputDrives[Pair(id, inPort.id)] ?: emptyList()
                    val resolvedState = when {
                        drivers.isEmpty() -> comp.inputValues[inPort.id] ?: DigitalState.UNKNOWN
                        drivers.contains(DigitalState.HIGH) && drivers.contains(DigitalState.LOW) -> {
                            // Multiple drivers contending
                            DigitalState.UNKNOWN
                        }
                        drivers.all { it == DigitalState.HIGH } -> DigitalState.HIGH
                        drivers.all { it == DigitalState.LOW } -> DigitalState.LOW
                        else -> drivers.firstOrNull { it != DigitalState.UNKNOWN } ?: DigitalState.UNKNOWN
                    }
                    newInputs[inPort.id] = resolvedState
                }

                val newOutputs = def.evaluate(newInputs, comp.switchState)

                val nextSwitchState = when (comp.type) {
                    "SR_LATCH", "SR_LATCH_NAND", "GATED_SR_LATCH", "D_LATCH", "JK_LATCH", "T_LATCH",
                    "D_FLIP_FLOP", "JK_FLIP_FLOP", "T_FLIP_FLOP", "SR_FLIP_FLOP" -> {
                        newOutputs["Q"] == DigitalState.HIGH
                    }
                    else -> comp.switchState
                }

                if (newInputs != comp.inputValues || newOutputs != comp.outputValues || nextSwitchState != comp.switchState) {
                    compMap[id] = comp.copy(inputValues = newInputs, outputValues = newOutputs, switchState = nextSwitchState)
                    changed = true
                }
            }
        }

        if (stepCount >= MAX_PROPAGATION_STEPS) {
            feedbackLoopDetected = true
            diagnostics.add(
                CircuitDiagnostic(
                    severity = DiagnosticSeverity.WARNING,
                    componentId = null,
                    portId = null,
                    message = "Unstable feedback loop or oscillation detected! Circuit may not settle."
                )
            )
        }

        // 3. Floating inputs diagnostic checks
        for ((id, comp) in compMap) {
            val def = ComponentRegistry.get(comp.type) ?: continue
            for (p in def.inputs) {
                // Digital Oscillator EN pin has internal pull-up and does not require an external driver
                if (comp.type == "OSCILLATOR" && p.id == "EN") continue

                val st = comp.inputValues[p.id] ?: DigitalState.UNKNOWN
                if (st == DigitalState.UNKNOWN) {
                    val compName = comp.label.ifBlank { def.name }
                    diagnostics.add(
                        CircuitDiagnostic(
                            severity = DiagnosticSeverity.INFO,
                            componentId = id,
                            portId = p.id,
                            message = "Input '${p.name}' of $compName ($id) is floating / unconnected."
                        )
                    )
                }
            }
        }

        val hasError = diagnostics.any { it.severity == DiagnosticSeverity.ERROR }

        return SimulationResult(
            components = compMap.values.toList(),
            wires = updatedWires,
            diagnostics = diagnostics,
            hasError = hasError
        )
    }

    private fun isOutputPort(def: ComponentDefinition, portId: String): Boolean {
        return def.outputs.any { out ->
            out.id == portId ||
            out.name == portId ||
            (out.id == "OUT" && (portId == "CLK" || portId == "Q" || portId == "Y")) ||
            (out.id == "CLK" && (portId == "OUT" || portId == "Y")) ||
            (out.id == "Y" && (portId == "OUT" || portId == "CLK")) ||
            (out.id == "INV" && (portId == "CLK'" || portId == "CLK_BAR" || portId == "OUT'")) ||
            (out.id == "Q_BAR" && (portId == "Q'" || portId == "INV" || portId == "QBAR"))
        }
    }
}
