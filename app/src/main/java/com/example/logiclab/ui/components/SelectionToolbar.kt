package com.example.logiclab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh

/**
 * Gate Information Card & Action Bar.
 * Shown when clicking any logic gate or wire in both 2D and 3D schematic editors.
 * Features live technical information, boolean formula, pin state telemetry,
 * waveform visualizer for digital oscillators, and action buttons.
 */
@Composable
fun SelectionToolbar(
    selectedComponent: CircuitComponent?,
    selectedWireId: String?,
    onDeleteSelected: () -> Unit,
    onDuplicateSelected: () -> Unit,
    onRotateSelected: () -> Unit,
    onToggleSelectedSwitch: () -> Unit,
    onOpenDetails: (() -> Unit)? = null,
    onOpenWaveforms: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isVisible = selectedComponent != null || selectedWireId != null

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        ) + fadeIn(animationSpec = tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { -it / 2 },
            animationSpec = tween(180)
        ) + fadeOut(animationSpec = tween(180)),
        modifier = modifier
    ) {
        if (selectedComponent != null) {
            GateInfoCard(
                component = selectedComponent,
                onDelete = onDeleteSelected,
                onDuplicate = onDuplicateSelected,
                onRotate = onRotateSelected,
                onToggleSwitch = onToggleSelectedSwitch,
                onOpenDetails = onOpenDetails,
                onOpenWaveforms = onOpenWaveforms,
                onDismiss = onDismiss
            )
        } else if (selectedWireId != null) {
            WireInfoCard(
                wireId = selectedWireId,
                onDelete = onDeleteSelected,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
fun GateInfoCard(
    component: CircuitComponent,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onRotate: () -> Unit,
    onToggleSwitch: () -> Unit,
    onOpenDetails: (() -> Unit)? = null,
    onOpenWaveforms: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val def = remember(component.type) { ComponentRegistry.get(component.type) }
    val gateName = def?.name ?: component.type
    val categoryName = def?.category?.name?.replace("_", " ") ?: "COMPONENT"
    val formula = def?.booleanFormula ?: ""
    val description = def?.description ?: ""

    // Accent color based on category
    val accentColor = when (component.type) {
        "OSCILLATOR", "CLOCK" -> Color(0xFFFFB300) // Amber for oscillator / clocks
        "SWITCH" -> if (component.switchState) SignalHigh else Color(0xFF81D4FA)
        "LED", "DIGITAL_PROBE" -> Color(0xFF00E676)
        else -> LabAccentBlue
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(LabPanelBg.copy(alpha = 0.96f))
            .border(1.2.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("gate_info_card_${component.id}")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Top Row: Category badge, Gate Name, and Small Delete Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.16f))
                            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = categoryName,
                            color = accentColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Gate Name & Label
                    Text(
                        text = component.label.ifBlank { gateName },
                        color = LabTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Action buttons: Info, Delete & Optional Close (X)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onOpenDetails != null) {
                        SmallActionButton(
                            icon = Icons.Default.Info,
                            label = "Info",
                            contentColor = LabAccentBlue,
                            backgroundColor = LabAccentBlue.copy(alpha = 0.14f),
                            borderColor = LabAccentBlue.copy(alpha = 0.55f),
                            onClick = onOpenDetails,
                            tag = "open_gate_details_btn"
                        )
                    }

                    // Small Delete Button (Prominent, sleek, compact)
                    SmallActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        contentColor = SignalError,
                        backgroundColor = SignalError.copy(alpha = 0.14f),
                        borderColor = SignalError.copy(alpha = 0.55f),
                        onClick = onDelete,
                        tag = "delete_gate_btn"
                    )

                    // Optional Dismiss button
                    if (onDismiss != null) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LabTextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // Middle Technical Info: Boolean formula & description
            if (formula.isNotBlank() || description.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (formula.isNotBlank()) {
                        Text(
                            text = formula,
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (formula.isNotBlank() && description.isNotBlank()) {
                        Text(text = "•", color = LabTextTertiary, fontSize = 9.sp)
                    }
                    if (description.isNotBlank()) {
                        Text(
                            text = description,
                            color = LabTextSecondary,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            // Live Pin States Telemetry & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Live Pin Signals
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Display inputs
                    if (component.inputValues.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = "IN:", color = LabTextTertiary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            component.inputValues.entries.take(4).forEach { (port, state) ->
                                PinStateBadge(port = port, state = state)
                            }
                        }
                    }

                    // Display outputs
                    if (component.outputValues.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(text = "OUT:", color = LabTextTertiary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
                            component.outputValues.entries.take(4).forEach { (port, state) ->
                                PinStateBadge(port = port, state = state)
                            }
                        }
                    }

                    // Digital Oscillator active pulse badge & Waveform Trigger
                    if (component.type == "OSCILLATOR" || component.type == "CLOCK") {
                        val isTogglingHigh = component.switchState
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x33FFB300))
                                .border(0.8.dp, Color(0xFFFFB300).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .clickable { onOpenWaveforms?.invoke() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("toolbar_visualise_waves_badge"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isTogglingHigh) SignalHigh else Color(0xFFFFB300))
                            )
                            Text(
                                text = "📈 Visualise Waves",
                                color = Color(0xFFFFB300),
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Quick Tools (Toggle if switch, Waveforms if oscillator, Rotate, Duplicate)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (component.type == "SWITCH" || component.type == "OSCILLATOR" || component.type == "CLOCK") {
                        SmallToolButton(
                            icon = Icons.Default.PowerSettingsNew,
                            contentDescription = if (component.type == "SWITCH") "Toggle Switch" else "Toggle Run/Halt",
                            tint = if (component.switchState) SignalHigh else LabTextSecondary,
                            onClick = onToggleSwitch,
                            tag = "toggle_switch_btn"
                        )
                    }

                    if (component.type == "OSCILLATOR" || component.type == "CLOCK") {
                        SmallToolButton(
                            icon = Icons.Default.ShowChart,
                            contentDescription = "Visualise Waveforms",
                            tint = Color(0xFFFFB300),
                            onClick = { onOpenWaveforms?.invoke() },
                            tag = "visualise_waves_btn"
                        )
                    }

                    SmallToolButton(
                        icon = Icons.AutoMirrored.Filled.RotateRight,
                        contentDescription = "Rotate 90°",
                        tint = LabTextPrimary,
                        onClick = onRotate,
                        tag = "rotate_comp_btn"
                    )

                    SmallToolButton(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Gate",
                        tint = LabTextPrimary,
                        onClick = onDuplicate,
                        tag = "duplicate_comp_btn"
                    )
                }
            }
        }
    }
}

@Composable
fun WireInfoCard(
    wireId: String,
    onDelete: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LabPanelBg.copy(alpha = 0.95f))
            .border(1.dp, LabAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("wire_info_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Timeline,
                contentDescription = null,
                tint = LabAccentBlue,
                modifier = Modifier.size(14.dp)
            )
            Column {
                Text(
                    text = "Circuit Wire Connection",
                    color = LabTextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Signal conductor trace",
                    color = LabTextSecondary,
                    fontSize = 8.5.sp
                )
            }

            // Small Delete Button
            SmallActionButton(
                icon = Icons.Default.Delete,
                label = "Delete",
                contentColor = SignalError,
                backgroundColor = SignalError.copy(alpha = 0.14f),
                borderColor = SignalError.copy(alpha = 0.55f),
                onClick = onDelete,
                tag = "delete_wire_btn"
            )

            if (onDismiss != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = LabTextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PinStateBadge(port: String, state: DigitalState) {
    val (bg, textColor, symbol) = when (state) {
        DigitalState.HIGH -> Triple(SignalHigh.copy(alpha = 0.18f), SignalHigh, "1")
        DigitalState.LOW -> Triple(Color(0xFF263238), LabTextSecondary, "0")
        DigitalState.UNKNOWN -> Triple(Color(0xFF37474F), Color(0xFFFFB300), "?")
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(bg)
            .padding(horizontal = 3.5.dp, vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = port,
            color = LabTextTertiary,
            fontSize = 7.5.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = symbol,
            color = textColor,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallActionButton(
    icon: ImageVector,
    label: String,
    contentColor: Color,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(5.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(5.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SmallToolButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
    tag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnScale"
    )
    val animatedTint by animateColorAsState(
        targetValue = tint,
        animationSpec = tween(200),
        label = "iconTint"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(LabCardBg)
            .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedTint,
            modifier = Modifier.size(12.dp)
        )
    }
}
