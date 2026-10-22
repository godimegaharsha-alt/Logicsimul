package com.example.logiclab.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.engine.CircuitInputInfo
import com.example.logiclab.engine.CircuitOutputInfo
import com.example.logiclab.engine.OutputFormula
import com.example.logiclab.engine.TruthTableData
import com.example.logiclab.engine.TruthTableEngine
import com.example.logiclab.engine.TruthTableMode
import com.example.logiclab.engine.TruthTableRow
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown

enum class TableRowFilter(val label: String) {
    ALL("All"),
    MIN_TERMS("Min (1)"),
    MAX_TERMS("Max (0)"),
    UNVERIFIED("Unverified")
}

/**
 * Data-driven verification Composable that takes the current circuit's inputs and outputs
 * and generates an interactive truth table for verification.
 *
 * Features:
 * - Direct data-driven signature taking inputs and outputs
 * - Interactive row clicking to inject test vectors directly into the circuit
 * - Interactive row verification checklist with "Verified X / Total" progress
 * - Canonical Sum-Of-Products (SOP) Boolean equations per output
 * - Small, sleek CAD-grade buttons with fluid animated transitions
 */
@Composable
fun CircuitTruthTableVerificationView(
    inputHeaders: List<String>,
    outputHeaders: List<String>,
    rows: List<TruthTableRow>,
    formulas: List<OutputFormula> = emptyList(),
    summaryMessage: String = "",
    onApplyRow: ((TruthTableRow) -> Unit)? = null,
    onVerifyRowToggle: ((Int) -> Unit)? = null,
    verifiedRowIndices: Set<Int> = emptySet(),
    onExportMarkdown: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var activeFilter by remember { mutableStateOf(TableRowFilter.ALL) }
    var showFormulasCard by remember { mutableStateOf(true) }

    // Filter rows based on selected filter
    val displayedRows = remember(rows, activeFilter, verifiedRowIndices) {
        when (activeFilter) {
            TableRowFilter.ALL -> rows
            TableRowFilter.MIN_TERMS -> rows.filter { row ->
                row.outputValues.values.any { it == DigitalState.HIGH }
            }
            TableRowFilter.MAX_TERMS -> rows.filter { row ->
                row.outputValues.values.all { it != DigitalState.HIGH }
            }
            TableRowFilter.UNVERIFIED -> rows.filter { row ->
                !verifiedRowIndices.contains(row.rowNumber)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LabPanelBg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("circuit_verification_truth_table")
    ) {
        // Verification Header Bar: Stats, Verification Progress, Export
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stats & Verified Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = LabAccentBlue,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "VERIFICATION TABLE",
                    color = LabTextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                if (rows.isNotEmpty()) {
                    Text(
                        text = "(${inputHeaders.size} In • ${outputHeaders.size} Out)",
                        color = LabTextTertiary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    // Verified Progress Chip
                    val verifiedCount = verifiedRowIndices.size
                    val totalCount = rows.size
                    val allVerified = verifiedCount == totalCount && totalCount > 0
                    val badgeBg by animateColorAsState(
                        targetValue = if (allVerified) Color(0xFF1B3822) else Color(0xFF162536),
                        label = "badgeBg"
                    )
                    val badgeTextColor by animateColorAsState(
                        targetValue = if (allVerified) SignalHigh else LabAccentBlue,
                        label = "badgeTextColor"
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(badgeBg)
                            .border(1.dp, if (allVerified) SignalHigh.copy(alpha = 0.5f) else Color(0xFF1E3A5F), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (allVerified) "ALL $totalCount VERIFIED ✓" else "$verifiedCount/$totalCount Verified",
                            color = badgeTextColor,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Export & Formula Toggle Buttons (Small)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (formulas.isNotEmpty()) {
                    SmallTableButton(
                        icon = Icons.Default.Functions,
                        label = if (showFormulasCard) "SOP" else "+SOP",
                        isActive = showFormulasCard,
                        onClick = { showFormulasCard = !showFormulasCard },
                        tag = "toggle_sop_btn"
                    )
                }

                if (onExportMarkdown != null && rows.isNotEmpty()) {
                    SmallTableButton(
                        icon = Icons.Default.ContentCopy,
                        label = "Export",
                        onClick = onExportMarkdown,
                        tag = "export_tt_btn"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Empty State Handler
        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(LabCardBg)
                    .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = summaryMessage.ifBlank { "No inputs or outputs connected." },
                        color = LabTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Add Switches or connect logic gates to automatically generate verification table.",
                        color = LabTextTertiary,
                        fontSize = 9.sp
                    )
                }
            }
            return
        }

        // Secondary Bar: Small Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TableRowFilter.entries.forEach { filter ->
                    val isSelected = filter == activeFilter
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) LabAccentBlue.copy(alpha = 0.25f) else Color.Transparent,
                        animationSpec = tween(200),
                        label = "filterChipBg"
                    )
                    val chipBorder by animateColorAsState(
                        targetValue = if (isSelected) LabAccentBlue else Color(0xFF263238),
                        animationSpec = tween(200),
                        label = "filterChipBorder"
                    )
                    val chipText by animateColorAsState(
                        targetValue = if (isSelected) LabAccentBlue else LabTextSecondary,
                        animationSpec = tween(200),
                        label = "filterChipText"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(chipBg)
                            .border(1.dp, chipBorder, RoundedCornerShape(3.dp))
                            .clickable { activeFilter = filter }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = filter.label,
                            color = chipText,
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Text(
                text = "Tap row to test live",
                color = LabTextTertiary,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Boolean SOP Equations Card (Collapsible)
        AnimatedVisibility(visible = showFormulasCard && formulas.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0F1722))
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                formulas.forEach { formula ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "${formula.outputName} =",
                                color = LabAccentBlue,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formula.canonicalSOP,
                                color = Color(0xFF81D4FA),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (formula.simplifiedExpression.isNotBlank() && formula.simplifiedExpression != formula.canonicalSOP) {
                            Text(
                                text = "(${formula.simplifiedExpression})",
                                color = LabTextTertiary,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Main Matrix Scroll Container
        val hScrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(LabCardBg)
                .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(hScrollState)
            ) {
                // Table Header Row
                Row(
                    modifier = Modifier
                        .background(Color(0xFF121B28))
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "VERIFY",
                        color = LabTextTertiary,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(42.dp)
                    )

                    Text(
                        text = "#",
                        color = LabTextTertiary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp)
                    )

                    // Input Headers
                    inputHeaders.forEach { inHeader ->
                        Text(
                            text = inHeader,
                            color = Color(0xFFE2E8F0),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(44.dp)
                        )
                    }

                    // Divider between Inputs and Outputs
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = LabAccentBlue.copy(alpha = 0.6f),
                            modifier = Modifier.size(10.dp)
                        )
                    }

                    // Output Headers
                    outputHeaders.forEach { outHeader ->
                        Text(
                            text = outHeader,
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.width(48.dp)
                        )
                    }

                    // Test Action Column
                    Text(
                        text = "TEST",
                        color = LabTextTertiary,
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(32.dp)
                    )
                }

                HorizontalDivider(color = LabBorder, thickness = 1.dp)

                // Virtualized Table Rows
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    itemsIndexed(displayedRows) { _, row ->
                        val isLive = row.isCurrent
                        val isVerified = verifiedRowIndices.contains(row.rowNumber)
                        val rowBg by animateColorAsState(
                            targetValue = if (isLive) Color(0x33388BFD) else if (isVerified) Color(0x1100E676) else Color.Transparent,
                            animationSpec = tween(200),
                            label = "rowBg"
                        )

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .border(1.dp, if (isLive) SignalHigh.copy(alpha = 0.7f) else Color.Transparent)
                                .clickable {
                                    onApplyRow?.invoke(row)
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Verification Checkmark Button (Small 20dp)
                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .clickable {
                                        onVerifyRowToggle?.invoke(row.rowNumber)
                                    },
                                contentAlignment = Alignment.CenterStart
                            ) {
                                val verifyBtnBg by animateColorAsState(
                                    targetValue = if (isVerified) Color(0xFF1B3822) else Color(0xFF1A222D),
                                    label = "verifyBtnBg"
                                )
                                val verifyIconColor by animateColorAsState(
                                    targetValue = if (isVerified) SignalHigh else LabTextTertiary,
                                    label = "verifyIconColor"
                                )

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(verifyBtnBg)
                                        .border(1.dp, if (isVerified) SignalHigh.copy(alpha = 0.5f) else Color(0xFF263238), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Verify row",
                                        tint = verifyIconColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (isVerified) "OK" else "?",
                                        color = verifyIconColor,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Row Number & Live Indicator
                            Row(
                                modifier = Modifier.width(24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isLive) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(SignalHigh)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                }
                                Text(
                                    text = "${row.rowNumber}",
                                    color = if (isLive) SignalHigh else LabTextTertiary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (isLive) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            // Input Values
                            inputHeaders.forEach { inHeader ->
                                val state = row.inputValues[inHeader] ?: DigitalState.UNKNOWN
                                Box(
                                    modifier = Modifier.width(44.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    CompactDigitalBadge(state)
                                }
                            }

                            // Spacer for column divider
                            Spacer(modifier = Modifier.width(18.dp))

                            // Output Values
                            outputHeaders.forEach { outHeader ->
                                val state = row.outputValues[outHeader] ?: DigitalState.UNKNOWN
                                Box(
                                    modifier = Modifier.width(48.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    CompactDigitalBadge(state, isOutput = true)
                                }
                            }

                            // Apply row test icon
                            Box(
                                modifier = Modifier.width(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Apply inputs to circuit",
                                    tint = if (isLive) SignalHigh else LabTextTertiary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        HorizontalDivider(color = LabBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

/**
 * High-performance, comprehensive Truth Table viewer that automatically analyzes
 * the logic circuit schematic and displays truth tables, min-terms, and canonical Boolean equations.
 */
@Composable
fun TruthTableViewer(
    truthTable: TruthTableData,
    onApplyRow: ((TruthTableRow) -> Unit)? = null,
    onModeChange: ((TruthTableMode) -> Unit)? = null,
    isExpanded: Boolean = false,
    onToggleExpand: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var verifiedRows by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = modifier.fillMaxSize()) {
        // Top Toolbar: Mode Pill, Expand Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LabPanelBg)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Toggle: Circuit vs Selected Gate (Small buttons)
            if (onModeChange != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F151F))
                        .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                        .padding(1.5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isCircuitMode = truthTable.mode == TruthTableMode.CIRCUIT
                    val activePrimary = MaterialTheme.colorScheme.primary
                    val activeOnPrimary = MaterialTheme.colorScheme.onPrimary
                    val circuitBg by animateColorAsState(
                        targetValue = if (isCircuitMode) activePrimary else Color.Transparent,
                        animationSpec = tween(220),
                        label = "circuitBg"
                    )
                    val compBg by animateColorAsState(
                        targetValue = if (!isCircuitMode) activePrimary else Color.Transparent,
                        animationSpec = tween(220),
                        label = "compBg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(circuitBg)
                            .clickable { onModeChange(TruthTableMode.CIRCUIT) }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            .testTag("tt_mode_circuit_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Circuit",
                            fontSize = 9.sp,
                            fontWeight = if (isCircuitMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCircuitMode) activeOnPrimary else LabTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(compBg)
                            .clickable { onModeChange(TruthTableMode.SELECTED_COMPONENT) }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp)
                            .testTag("tt_mode_gate_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Gate",
                            fontSize = 9.sp,
                            fontWeight = if (!isCircuitMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isCircuitMode) activeOnPrimary else LabTextSecondary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // Expand / Collapse Drawer Button (Small)
            if (onToggleExpand != null) {
                SmallTableButton(
                    icon = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    label = if (isExpanded) "Collapse" else "Expand",
                    onClick = onToggleExpand,
                    tag = "expand_truth_table_btn"
                )
            }
        }

        // Render Data-driven Verification View
        CircuitTruthTableVerificationView(
            inputHeaders = truthTable.inputHeaders,
            outputHeaders = truthTable.outputHeaders,
            rows = truthTable.rows,
            formulas = truthTable.formulas,
            summaryMessage = truthTable.summaryMessage,
            onApplyRow = onApplyRow,
            onVerifyRowToggle = { rowIndex ->
                verifiedRows = if (verifiedRows.contains(rowIndex)) {
                    verifiedRows - rowIndex
                } else {
                    verifiedRows + rowIndex
                }
            },
            verifiedRowIndices = verifiedRows,
            onExportMarkdown = {
                copyTruthTableToClipboard(context, truthTable)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Standalone Data-driven Interactive Truth Table Verification Composable that
 * accepts raw components and wires directly, evaluating inputs and outputs on demand.
 */
@Composable
fun InteractiveCircuitVerificationTable(
    components: List<CircuitComponent>,
    wires: List<CircuitWire>,
    onApplyInputs: ((Map<String, DigitalState>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val truthTable = remember(components, wires) {
        TruthTableEngine.generateForCircuit(components, wires)
    }

    TruthTableViewer(
        truthTable = truthTable,
        onApplyRow = { row ->
            onApplyInputs?.invoke(row.inputValues)
        },
        modifier = modifier
    )
}

@Composable
private fun CompactDigitalBadge(
    state: DigitalState,
    isOutput: Boolean = false
) {
    val (bgColor, textColor, label) = when (state) {
        DigitalState.HIGH -> Triple(Color(0xFF1B3822), SignalHigh, "1")
        DigitalState.LOW -> Triple(Color(0xFF161D26), SignalLow, "0")
        DigitalState.UNKNOWN -> Triple(Color(0xFF1E242B), SignalUnknown, "X")
    }

    val animatedBg by animateColorAsState(targetValue = bgColor, animationSpec = tween(200), label = "badgeBg")
    val animatedText by animateColorAsState(targetValue = textColor, animationSpec = tween(200), label = "badgeText")

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(animatedBg)
            .border(
                1.dp,
                if (isOutput && state == DigitalState.HIGH) SignalHigh.copy(alpha = 0.5f) else Color(0xFF263238),
                RoundedCornerShape(3.dp)
            )
            .padding(horizontal = 6.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = animatedText,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallTableButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit,
    tag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )
    val btnBg by animateColorAsState(
        targetValue = if (isActive) Color(0x33388BFD) else Color(0xFF121B28),
        animationSpec = tween(200),
        label = "btnBg"
    )
    val btnBorder by animateColorAsState(
        targetValue = if (isActive) LabAccentBlue else LabBorder,
        animationSpec = tween(200),
        label = "btnBorder"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(3.dp))
            .background(btnBg)
            .border(1.dp, btnBorder, RoundedCornerShape(3.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 5.dp, vertical = 2.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) LabAccentBlue else LabTextPrimary,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = label,
                color = if (isActive) LabAccentBlue else LabTextPrimary,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun copyTruthTableToClipboard(context: Context, data: TruthTableData) {
    val sb = StringBuilder()
    sb.append("### LogicLab Truth Table: ${data.title}\n\n")

    // Markdown Table
    val allHeaders = listOf("Row") + data.inputHeaders + data.outputHeaders
    sb.append("| ${allHeaders.joinToString(" | ")} |\n")
    sb.append("| ${allHeaders.map { "---" }.joinToString(" | ")} |\n")

    data.rows.forEach { row ->
        val inValues = data.inputHeaders.map { h -> row.inputValues[h]?.symbol ?: "X" }
        val outValues = data.outputHeaders.map { h -> row.outputValues[h]?.symbol ?: "X" }
        val allValues = listOf(row.rowNumber.toString()) + inValues + outValues
        sb.append("| ${allValues.joinToString(" | ")} |\n")
    }

    if (data.formulas.isNotEmpty()) {
        sb.append("\n#### Canonical Sum-of-Products (SOP):\n")
        data.formulas.forEach { formula ->
            sb.append("- **${formula.outputName}** = `${formula.canonicalSOP}`")
            if (formula.simplifiedExpression.isNotBlank() && formula.simplifiedExpression != formula.canonicalSOP) {
                sb.append(" *(Simplified: `${formula.simplifiedExpression}`)*")
            }
            sb.append("\n")
        }
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Truth Table", sb.toString()))
    Toast.makeText(context, "Truth Table exported to clipboard", Toast.LENGTH_SHORT).show()
}
