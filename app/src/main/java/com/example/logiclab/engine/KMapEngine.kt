package com.example.logiclab.engine

import com.example.logiclab.data.CircuitData
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import java.util.UUID

enum class KMapCellState(val symbol: String) {
    ZERO("0"),
    ONE("1"),
    DONT_CARE("X")
}

data class KMapImplicant(
    val mask: String, // e.g. "01-1" means A=0, B=1, C=- (any), D=1 => A'BD
    val term: String, // "A'BD"
    val coveredMinterms: List<Int>,
    val colorHex: Long = 0xFF58A6FF
)

data class KMapSolution(
    val sopExpression: String,
    val posExpression: String,
    val implicants: List<KMapImplicant>
)

object KMapEngine {

    /**
     * Solves a 2, 3, or 4-variable Karnaugh Map and returns the minimized SOP & POS expressions.
     */
    fun solve(numVars: Int, cellStates: Map<Int, KMapCellState>): KMapSolution {
        val totalCells = 1 shl numVars
        val ones = cellStates.filter { it.value == KMapCellState.ONE }.keys.toSet()
        val dontCares = cellStates.filter { it.value == KMapCellState.DONT_CARE }.keys.toSet()

        if (ones.isEmpty()) {
            return KMapSolution("F = 0", "F = 0", emptyList())
        }
        if (ones.size + dontCares.size == totalCells) {
            return KMapSolution("F = 1", "F = 1", emptyList())
        }

        val allActive = ones + dontCares
        val primeImplicants = mutableListOf<KMapImplicant>()
        val colors = listOf(0xFF58A6FF, 0xFF3FB950, 0xFFFFB300, 0xFFA371F7, 0xFFF778BA, 0xFF79C0FF)

        // Generate rectangular group sizes (powers of 2: 16, 8, 4, 2, 1)
        val varNames = when (numVars) {
            2 -> listOf("A", "B")
            3 -> listOf("A", "B", "C")
            else -> listOf("A", "B", "C", "D")
        }

        // Iterate over potential power-of-2 cubes
        val terms = mutableListOf<String>()
        val uncoveredOnes = ones.toMutableSet()
        var colorIdx = 0

        // Find largest valid groups that cover uncovered 1s
        val groups = generateAllValidGroups(numVars, allActive)
        // Sort groups by size descending (largest power of 2 covers first)
        val sortedGroups = groups.sortedByDescending { it.coveredMinterms.size }

        for (group in sortedGroups) {
            // Check if this group covers any remaining uncovered 1s
            val coversNew = group.coveredMinterms.any { it in uncoveredOnes }
            if (coversNew) {
                val coloredGroup = group.copy(colorHex = colors[colorIdx % colors.size])
                primeImplicants.add(coloredGroup)
                terms.add(group.term)
                uncoveredOnes.removeAll(group.coveredMinterms.toSet())
                colorIdx++
            }
        }

        // Fallback for any stray 1s
        for (m in uncoveredOnes) {
            val mask = m.toString(2).padStart(numVars, '0')
            val term = maskToTerm(mask, varNames)
            primeImplicants.add(KMapImplicant(mask, term, listOf(m), colors[colorIdx % colors.size]))
            terms.add(term)
            colorIdx++
        }

        val sop = if (terms.isEmpty()) "F = 0" else "F = " + terms.joinToString(" + ")
        val pos = generatePos(numVars, cellStates, varNames)

        return KMapSolution(sop, pos, primeImplicants)
    }

    private fun generateAllValidGroups(numVars: Int, allActive: Set<Int>): List<KMapImplicant> {
        val list = mutableListOf<KMapImplicant>()
        val varNames = when (numVars) {
            2 -> listOf("A", "B")
            3 -> listOf("A", "B", "C")
            else -> listOf("A", "B", "C", "D")
        }

        // Generate masks with '-' for don't cares in the cube
        fun generateMasks(current: String, index: Int) {
            if (index == numVars) {
                // Evaluate mask
                val covered = getCoveredMinterms(current)
                if (covered.isNotEmpty() && covered.all { it in allActive }) {
                    val term = maskToTerm(current, varNames)
                    list.add(KMapImplicant(current, term, covered))
                }
                return
            }
            generateMasks(current + "0", index + 1)
            generateMasks(current + "1", index + 1)
            generateMasks(current + "-", index + 1)
        }

        generateMasks("", 0)
        return list
    }

    private fun getCoveredMinterms(mask: String): List<Int> {
        var results = listOf(0)
        for (i in mask.indices) {
            val char = mask[i]
            val bitWeight = 1 shl (mask.length - 1 - i)
            results = when (char) {
                '0' -> results
                '1' -> results.map { it + bitWeight }
                '-' -> results + results.map { it + bitWeight }
                else -> results
            }
        }
        return results
    }

    private fun maskToTerm(mask: String, varNames: List<String>): String {
        val sb = StringBuilder()
        for (i in mask.indices) {
            val char = mask[i]
            val vName = varNames[i]
            when (char) {
                '1' -> sb.append(vName)
                '0' -> sb.append("$vName'")
            }
        }
        return if (sb.isEmpty()) "1" else sb.toString()
    }

    private fun generatePos(numVars: Int, cellStates: Map<Int, KMapCellState>, varNames: List<String>): String {
        val zeros = cellStates.filter { it.value == KMapCellState.ZERO }.keys
        if (zeros.isEmpty()) return "F = 1"
        val maxTerms = zeros.take(6).map { m ->
            val bits = m.toString(2).padStart(numVars, '0')
            val parts = bits.mapIndexed { idx, bit ->
                if (bit == '0') varNames[idx] else "${varNames[idx]}'"
            }
            "(${parts.joinToString(" + ")})"
        }
        return "F = " + maxTerms.joinToString(" • ")
    }

    /**
     * Synthesizes a minimized K-Map solution directly into complete logic circuit components and wires!
     */
    fun synthesizeCircuit(solution: KMapSolution, numVars: Int): CircuitData {
        val components = mutableListOf<CircuitComponent>()
        val wires = mutableListOf<CircuitWire>()

        val varNames = when (numVars) {
            2 -> listOf("A", "B")
            3 -> listOf("A", "B", "C")
            else -> listOf("A", "B", "C", "D")
        }

        // 1. Create Input Switches (A, B, C, D)
        val switchComps = mutableMapOf<String, CircuitComponent>()
        var startY = 80f
        for (name in varNames) {
            val swId = "SW_$name"
            val swComp = CircuitComponent(
                id = swId,
                type = "SWITCH",
                x = 60f,
                y = startY,
                label = "Input $name"
            )
            components.add(swComp)
            switchComps[name] = swComp
            startY += 90f
        }

        // 2. Create NOT gates for inverted signals if needed
        val notComps = mutableMapOf<String, CircuitComponent>()
        var notY = 80f
        for (name in varNames) {
            val notId = "NOT_$name"
            val notComp = CircuitComponent(
                id = notId,
                type = "NOT",
                x = 190f,
                y = notY,
                label = "NOT $name"
            )
            components.add(notComp)
            notComps[name] = notComp

            // Wire from switch to NOT gate
            wires.add(
                CircuitWire(
                    id = "w_not_${name}",
                    fromComponentId = switchComps[name]!!.id,
                    fromPortId = "OUT",
                    toComponentId = notId,
                    toPortId = "IN"
                )
            )
            notY += 90f
        }

        // 3. Create AND Gates for each product term
        val andComps = mutableListOf<CircuitComponent>()
        var andY = 80f
        for ((idx, imp) in solution.implicants.take(6).withIndex()) {
            val andId = "AND_TERM_${idx + 1}"
            val andComp = CircuitComponent(
                id = andId,
                type = if (imp.mask.count { it != '-' } <= 1) "BUFFER" else "AND",
                x = 360f,
                y = andY,
                label = imp.term
            )
            components.add(andComp)
            andComps.add(andComp)

            // Connect inputs to this AND gate
            var portIdx = 0
            val ports = listOf("A", "B")
            for (i in imp.mask.indices) {
                val char = imp.mask[i]
                val vName = varNames[i]
                if (char == '1') {
                    // Connect from Switch
                    if (portIdx < ports.size) {
                        wires.add(
                            CircuitWire(
                                id = "w_and_${idx}_$portIdx",
                                fromComponentId = switchComps[vName]!!.id,
                                fromPortId = "OUT",
                                toComponentId = andId,
                                toPortId = ports[portIdx]
                            )
                        )
                        portIdx++
                    }
                } else if (char == '0') {
                    // Connect from NOT gate
                    if (portIdx < ports.size) {
                        wires.add(
                            CircuitWire(
                                id = "w_and_inv_${idx}_$portIdx",
                                fromComponentId = notComps[vName]!!.id,
                                fromPortId = "Y",
                                toComponentId = andId,
                                toPortId = ports[portIdx]
                            )
                        )
                        portIdx++
                    }
                }
            }

            andY += 90f
        }

        // 4. Output OR Gate summing product terms
        val orId = "OR_SUM"
        val orComp = CircuitComponent(
            id = orId,
            type = if (andComps.size <= 1) "BUFFER" else "OR",
            x = 520f,
            y = 170f,
            label = "Sum (F)"
        )
        components.add(orComp)

        // Wire AND gates into OR gate
        if (andComps.isNotEmpty()) {
            wires.add(
                CircuitWire(
                    id = "w_or_in_1",
                    fromComponentId = andComps[0].id,
                    fromPortId = "Y",
                    toComponentId = orId,
                    toPortId = "A"
                )
            )
            if (andComps.size > 1) {
                wires.add(
                    CircuitWire(
                        id = "w_or_in_2",
                        fromComponentId = andComps[1].id,
                        fromPortId = "Y",
                        toComponentId = orId,
                        toPortId = "B"
                    )
                )
            }
        }

        // 5. Output LED Indicator
        val ledComp = CircuitComponent(
            id = "LED_OUTPUT_F",
            type = "LED",
            x = 660f,
            y = 170f,
            label = "Output F"
        )
        components.add(ledComp)

        wires.add(
            CircuitWire(
                id = "w_led_f",
                fromComponentId = orId,
                fromPortId = "Y",
                toComponentId = ledComp.id,
                toPortId = "IN"
            )
        )

        return CircuitData(components, wires)
    }
}
