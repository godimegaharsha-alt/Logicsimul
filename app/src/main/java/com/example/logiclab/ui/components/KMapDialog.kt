package com.example.logiclab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.data.CircuitData
import com.example.logiclab.engine.KMapCellState
import com.example.logiclab.engine.KMapEngine
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalHigh

/**
 * Interactive Karnaugh Map (K-Map) Solver & Boolean Circuit Synthesizer.
 * Supports 2, 3, and 4 variables (A, B, C, D) with real-time grouping,
 * minimal Sum-of-Products (SOP) equation calculation, and one-click
 * automatic circuit schematic synthesis.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KMapDialog(
    onApplySynthesizedCircuit: (CircuitData) -> Unit,
    onDismiss: () -> Unit
) {
    var numVars by remember { mutableIntStateOf(4) }
    val cellStates = remember { mutableStateMapOf<Int, KMapCellState>() }

    // Gray code coordinate mappings
    val gray2 = listOf(0, 1)
    val gray4 = listOf(0, 1, 3, 2) // 00, 01, 11, 10

    val (rowGray, colGray, rowVars, colVars) = when (numVars) {
        2 -> Quadruple(gray2, gray2, "A", "B")
        3 -> Quadruple(gray2, gray4, "A", "BC")
        else -> Quadruple(gray4, gray4, "AB", "CD")
    }

    val solution = remember(numVars, cellStates.toMap()) {
        KMapEngine.solve(numVars, cellStates)
    }

    // Dialog Entrance Spring Animation
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        ),
        label = "kmapDialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(180),
        label = "kmapDialogAlpha"
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth(0.78f)
            .heightIn(max = 370.dp)
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
                alpha = dialogAlpha
            }
            .clip(RoundedCornerShape(12.dp))
            .background(LabPanelBg)
            .border(1.2.dp, LabAccentBlue.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .testTag("kmap_dialog")
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Variable count toggle pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = null,
                        tint = LabAccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "K-Map Solver & Synthesizer",
                        color = LabTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = LabTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Variable Selector Row (2, 3, 4 Variables)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Input Variables:",
                    color = LabTextSecondary,
                    fontSize = 10.5.sp
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LabCardBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(2, 3, 4).forEach { count ->
                        val isSelected = numVars == count
                        val bg by animateColorAsState(
                            targetValue = if (isSelected) LabAccentBlue else Color.Transparent,
                            animationSpec = tween(200),
                            label = "varBg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable {
                                    if (numVars != count) {
                                        numVars = count
                                        cellStates.clear()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .testTag("kmap_var_${count}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$count Vars",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else LabTextSecondary,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Interactive K-Map Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F151F))
                    .border(1.dp, Color(0xFF1E2B3E), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Column Labels Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Corner variable diagonal label
                    Box(
                        modifier = Modifier.size(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$rowVars\\$colVars",
                            color = LabAccentBlue,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Col binary labels
                    colGray.forEach { colVal ->
                        val colBits = if (colGray.size == 2) colVal.toString(2) else colVal.toString(2).padStart(2, '0')
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = colBits,
                                color = LabTextSecondary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Grid Rows
                rowGray.forEach { rVal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Row binary label
                        val rBits = if (rowGray.size == 2) rVal.toString(2) else rVal.toString(2).padStart(2, '0')
                        Box(
                            modifier = Modifier.size(34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = rBits,
                                color = LabTextSecondary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Cells in this row
                        colGray.forEach { cVal ->
                            val minterm = when (numVars) {
                                2 -> (rVal shl 1) or cVal
                                3 -> (rVal shl 2) or cVal
                                else -> (rVal shl 2) or cVal
                            }
                            val state = cellStates[minterm] ?: KMapCellState.ZERO

                            // Highlight cell if covered by an implicant
                            val coveringGroup = solution.implicants.find { minterm in it.coveredMinterms }
                            val cellBorderColor = if (coveringGroup != null) Color(coveringGroup.colorHex) else Color(0xFF26354A)

                            val cellBg = when (state) {
                                KMapCellState.ONE -> Color(0xFF1E382A)
                                KMapCellState.ZERO -> Color(0xFF141A24)
                                KMapCellState.DONT_CARE -> Color(0xFF332918)
                            }
                            val cellTextColor = when (state) {
                                KMapCellState.ONE -> SignalHigh
                                KMapCellState.ZERO -> LabTextTertiary
                                KMapCellState.DONT_CARE -> Color(0xFFFFB300)
                            }

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .padding(1.5.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellBg)
                                    .border(1.dp, cellBorderColor, RoundedCornerShape(4.dp))
                                    .clickable {
                                        // Cycle 0 -> 1 -> X -> 0
                                        cellStates[minterm] = when (state) {
                                            KMapCellState.ZERO -> KMapCellState.ONE
                                            KMapCellState.ONE -> KMapCellState.DONT_CARE
                                            KMapCellState.DONT_CARE -> KMapCellState.ZERO
                                        }
                                    }
                                    .testTag("kmap_cell_${minterm}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = state.symbol,
                                    color = cellTextColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                // Tiny minterm index badge (bottom right)
                                Text(
                                    text = "$minterm",
                                    color = LabTextTertiary.copy(alpha = 0.5f),
                                    fontSize = 6.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Minimal Boolean Equation Output Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141C28))
                    .border(1.dp, LabAccentBlue.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Minimized Sum-of-Products (SOP):",
                    color = LabTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = solution.sopExpression,
                    color = Color(0xFF58A6FF),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (solution.posExpression.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Product-of-Sums (POS):",
                        color = LabTextSecondary,
                        fontSize = 8.5.sp
                    )
                    Text(
                        text = solution.posExpression,
                        color = Color(0xFF7EE787),
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Implicant Group Color Badges
            if (solution.implicants.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Loops:", color = LabTextTertiary, fontSize = 8.5.sp)
                    solution.implicants.forEach { imp ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(imp.colorHex).copy(alpha = 0.15f))
                                .border(1.dp, Color(imp.colorHex).copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${imp.term} {${imp.coveredMinterms.joinToString(",")}}",
                                color = Color(imp.colorHex),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action Buttons: Synthesize into Circuit / Clear / Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LabCardBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                        .clickable { cellStates.clear() }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = LabTextSecondary, modifier = Modifier.size(12.dp))
                    Text(text = "Clear Grid", color = LabTextSecondary, fontSize = 10.sp)
                }

                // Synthesize & Insert Circuit Button
                val synthInteraction = remember { MutableInteractionSource() }
                val isSynthPressed by synthInteraction.collectIsPressedAsState()
                val synthScale by animateFloatAsState(
                    targetValue = if (isSynthPressed) 0.92f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "synthScale"
                )

                Row(
                    modifier = Modifier
                        .scale(synthScale)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            interactionSource = synthInteraction,
                            indication = null,
                            onClick = {
                                val circuit = KMapEngine.synthesizeCircuit(solution, numVars)
                                onApplySynthesizedCircuit(circuit)
                                onDismiss()
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("kmap_synthesize_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(13.dp))
                    Text(
                        text = "Synthesize Circuit",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
