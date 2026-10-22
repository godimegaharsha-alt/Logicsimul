package com.example.logiclab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.DigitalState
import com.example.logiclab.model.PortType
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown

/**
 * Compact, beautifully animated Gate Details & Actions Dialog.
 * Triggered on DOUBLE-CLICKING any gate on the grid or in 3D view.
 * Sized ergonomically for mobile screens with spring animations,
 * live signal pulse glows, animated rotate spin, and quick actions.
 */
@Composable
fun GateDetailsDialog(
    component: CircuitComponent,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onRotate: () -> Unit,
    onToggleSwitch: () -> Unit,
    onDismiss: () -> Unit
) {
    val def = remember(component.type) { ComponentRegistry.get(component.type) }
    val gateName = def?.name ?: component.type
    val categoryName = def?.category?.name?.replace("_", " ") ?: "GATE"
    val formula = def?.booleanFormula ?: ""
    val description = def?.description ?: ""

    // Dialog Entrance Spring Animation
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.86f,
        animationSpec = spring(
            dampingRatio = 0.74f,
            stiffness = 450f
        ),
        label = "dialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(180),
        label = "dialogAlpha"
    )

    // Animated Rotate Icon Spin
    val animatedRotateAngle by animateFloatAsState(
        targetValue = component.rotation.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotateSpin"
    )

    // Accent color theme for the gate type
    val accentColor = when (component.type) {
        "OSCILLATOR", "CLOCK" -> Color(0xFFFFB300)
        "SWITCH" -> if (component.switchState) SignalHigh else Color(0xFF81D4FA)
        "LED", "DIGITAL_PROBE" -> Color(0xFF00E676)
        else -> LabAccentBlue
    }

    // Active tab in compact view: 0 = Info & Signals, 1 = Truth Table
    val standardTable = remember(component.type) { getMiniTruthTable(component.type) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 295.dp)
                .fillMaxWidth(0.78f)
                .heightIn(max = 360.dp)
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                }
                .clip(RoundedCornerShape(12.dp))
                .border(1.2.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .testTag("gate_details_dialog"),
            color = LabPanelBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row: Gate Icon, Title, Category Badge & Compact Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (component.type == "SWITCH") Icons.Default.PowerSettingsNew else Icons.Default.Memory,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Column {
                            Text(
                                text = component.label.ifBlank { gateName },
                                color = LabTextPrimary,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(accentColor.copy(alpha = 0.15f))
                                        .border(0.6.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                                        .padding(horizontal = 4.dp, vertical = 0.5.dp)
                                ) {
                                    Text(
                                        text = categoryName,
                                        color = accentColor,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = component.id,
                                    color = LabTextTertiary,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("close_gate_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = LabTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                HorizontalDivider(color = LabBorder.copy(alpha = 0.5f))

                // Compact Action Strip: [Rotate 90°] [Duplicate] [Delete Gate]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Rotate 90° Button with animated spinning icon
                    CompactDialogActionBtn(
                        onClick = onRotate,
                        tag = "dialog_rotate_btn",
                        modifier = Modifier.weight(1.2f),
                        borderColor = LabAccentBlue.copy(alpha = 0.7f),
                        bgNormal = LabAccentBlue.copy(alpha = 0.18f),
                        contentColor = LabAccentBlue
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.RotateRight,
                            contentDescription = "Rotate 90 degrees",
                            modifier = Modifier
                                .size(15.dp)
                                .rotate(animatedRotateAngle),
                            tint = LabAccentBlue
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Rotate ${component.rotation}°",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = LabAccentBlue
                        )
                    }

                    // Duplicate Button
                    CompactDialogActionBtn(
                        onClick = onDuplicate,
                        tag = "dialog_duplicate_btn",
                        modifier = Modifier.weight(0.9f),
                        borderColor = LabBorder,
                        bgNormal = LabCardBg,
                        contentColor = LabTextPrimary
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Duplicate",
                            modifier = Modifier.size(13.dp),
                            tint = LabTextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clone",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = LabTextPrimary
                        )
                    }

                    // Prominent Delete Button
                    CompactDialogActionBtn(
                        onClick = {
                            onDelete()
                            onDismiss()
                        },
                        tag = "delete_gate_dialog_button",
                        modifier = Modifier.weight(1.05f),
                        borderColor = SignalError.copy(alpha = 0.7f),
                        bgNormal = SignalError.copy(alpha = 0.15f),
                        contentColor = SignalError
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Gate",
                            tint = SignalError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Delete",
                            color = SignalError,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If Switch, Oscillator, or Clock: Compact Animated Slider Toggle
                if (component.type == "SWITCH" || component.type == "OSCILLATOR" || component.type == "CLOCK") {
                    val swActive = component.switchState
                    val swBgColor by animateColorAsState(
                        targetValue = if (swActive) SignalHigh.copy(alpha = 0.2f) else LabCardBg,
                        animationSpec = tween(220),
                        label = "swBg"
                    )
                    val swBorderColor by animateColorAsState(
                        targetValue = if (swActive) SignalHigh else LabBorder,
                        animationSpec = tween(220),
                        label = "swBorder"
                    )
                    val swThumbOffset by animateFloatAsState(
                        targetValue = if (swActive) 20f else 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "thumbOffset"
                    )

                    Surface(
                        onClick = onToggleSwitch,
                        shape = RoundedCornerShape(8.dp),
                        color = swBgColor,
                        border = BorderStroke(1.dp, swBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("dialog_toggle_switch_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = if (swActive) SignalHigh else LabTextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (swActive) "Switch: ON [Logic 1]" else "Switch: OFF [Logic 0]",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (swActive) SignalHigh else LabTextSecondary
                                )
                            }

                            // Animated toggle track & thumb
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (swActive) SignalHigh.copy(alpha = 0.4f) else LabDarkBg)
                                    .border(1.dp, if (swActive) SignalHigh else LabBorder, RoundedCornerShape(10.dp))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .offset { IntOffset(swThumbOffset.toInt(), 0) }
                                        .clip(CircleShape)
                                        .background(if (swActive) SignalHigh else Color.LightGray)
                                )
                            }
                        }
                    }
                }

                // If Gate has Truth Table: Compact Segmented Tab Switcher
                if (standardTable != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(LabDarkBg)
                            .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedTab == 0) LabAccentBlue.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Specs & Signals",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) LabAccentBlue else LabTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selectedTab == 1) LabAccentBlue.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Truth Table",
                                fontSize = 10.5.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) LabAccentBlue else LabTextSecondary
                            )
                        }
                    }
                }

                // Content View: Tab 0 (Specs & Live Signals)
                if (selectedTab == 0 || standardTable == null) {
                    // Boolean formula card
                    if (formula.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(LabDarkBg)
                                .border(1.dp, LabAccentBlue.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Y =",
                                    color = LabTextTertiary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formula,
                                    color = LabAccentBlue,
                                    fontSize = 11.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Live Pin Signal Telemetry (Compact Indicator Pills with Pulsing Dots)
                    val defInputs = def?.inputs ?: emptyList()
                    val defOutputs = def?.outputs ?: emptyList()
                    if (defInputs.isNotEmpty() || defOutputs.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(LabCardBg)
                                .border(1.dp, LabBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PIN TELEMETRY",
                                    color = LabTextTertiary,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "(${component.x.toInt()}, ${component.y.toInt()}) px",
                                    color = LabTextTertiary,
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Pins row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                defInputs.forEach { port ->
                                    val state = component.inputValues[port.id] ?: DigitalState.UNKNOWN
                                    CompactPinPill(name = port.name, state = state, isInput = true, modifier = Modifier.weight(1f))
                                }
                                defOutputs.forEach { port ->
                                    val state = component.outputValues[port.id] ?: DigitalState.UNKNOWN
                                    CompactPinPill(name = port.name, state = state, isInput = false, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            color = LabTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.5.sp
                        )
                    }
                } else {
                    // Content View: Tab 1 (Truth Table Reference)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(LabCardBg)
                            .border(1.dp, LabBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(LabDarkBg)
                                .clip(RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = standardTable.inputsHeader,
                                color = LabTextSecondary,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = standardTable.outputHeader,
                                color = LabAccentBlue,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Rows
                        standardTable.rows.forEach { (inStr, outStr) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 1.5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = inStr,
                                    color = LabTextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = outStr,
                                    color = if (outStr.contains("1")) SignalHigh else SignalLow,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact Action Button with spring scale animation on press.
 */
@Composable
private fun CompactDialogActionBtn(
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    borderColor: Color,
    bgNormal: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "actionBtnScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interaction,
        shape = RoundedCornerShape(8.dp),
        color = bgNormal,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .height(34.dp)
            .scale(scale)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * Compact Pin Pill with animated pulsating status indicator dot.
 */
@Composable
private fun CompactPinPill(
    name: String,
    state: DigitalState,
    isInput: Boolean,
    modifier: Modifier = Modifier
) {
    val pulseTransition = rememberInfiniteTransition(label = "pulsePin")
    val pulsePhase by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val (stateBg, stateBorder, stateColor, symbol) = when (state) {
        DigitalState.HIGH -> Quad(SignalHigh.copy(alpha = 0.18f), SignalHigh, SignalHigh, "1")
        DigitalState.LOW -> Quad(SignalLow.copy(alpha = 0.15f), SignalLow.copy(alpha = 0.5f), SignalLow, "0")
        DigitalState.UNKNOWN -> Quad(SignalUnknown.copy(alpha = 0.15f), SignalUnknown.copy(alpha = 0.5f), SignalUnknown, "?")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(5.dp))
            .background(stateBg)
            .border(0.8.dp, stateBorder, RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Pulsing live signal dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .graphicsLayer {
                        if (state == DigitalState.HIGH) {
                            alpha = 0.6f + 0.4f * pulsePhase
                            scaleX = 0.85f + 0.25f * pulsePhase
                            scaleY = 0.85f + 0.25f * pulsePhase
                        }
                    }
                    .clip(CircleShape)
                    .background(stateColor)
            )
            Text(
                text = "$name: $symbol",
                color = stateColor,
                fontSize = 9.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private data class MiniTruthTable(
    val inputsHeader: String,
    val outputHeader: String,
    val rows: List<Pair<String, String>>
)

private fun getMiniTruthTable(type: String): MiniTruthTable? {
    return when (type) {
        "AND" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "0", "0   1" to "0", "1   0" to "0", "1   1" to "1"))
        "OR" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "0", "0   1" to "1", "1   0" to "1", "1   1" to "1"))
        "NOT" -> MiniTruthTable("IN", "OUT", listOf("0" to "1", "1" to "0"))
        "NAND" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "1", "0   1" to "1", "1   0" to "1", "1   1" to "0"))
        "NOR" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "1", "0   1" to "0", "1   0" to "0", "1   1" to "0"))
        "XOR" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "0", "0   1" to "1", "1   0" to "1", "1   1" to "0"))
        "XNOR" -> MiniTruthTable("A   B", "OUT", listOf("0   0" to "1", "0   1" to "0", "1   0" to "0", "1   1" to "1"))
        "BUFFER" -> MiniTruthTable("IN", "OUT", listOf("0" to "0", "1" to "1"))
        "HALF_ADDER" -> MiniTruthTable("A   B", "SUM   CARRY", listOf("0   0" to "0       0", "0   1" to "1       0", "1   0" to "1       0", "1   1" to "0       1"))
        else -> null
    }
}
