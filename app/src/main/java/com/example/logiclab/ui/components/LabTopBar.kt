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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LogicLabDesign
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalFlowHigh
import com.example.ui.theme.SignalFlowLow
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalUnknown

enum class AppMode(val label: String) {
    SIMULATOR("Workspace"),
    LEARN("Learn"),
    EXPERIMENTS("Labs")
}

@Composable
fun LabTopBar(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    isSimulating: Boolean,
    onToggleSimulate: () -> Unit,
    onStepSimulate: () -> Unit,
    onResetSimulation: () -> Unit,
    onNewCircuit: () -> Unit,
    onOpenRepo: () -> Unit,
    onSaveCircuit: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    isWireDrawingMode: Boolean = false,
    onToggleWireDrawingMode: () -> Unit = {},
    isSignalFlowMode: Boolean = true,
    onToggleSignalFlowMode: () -> Unit = {},
    onOpenAiAssistant: () -> Unit,
    is3DMode: Boolean = false,
    onToggle3DMode: () -> Unit = {},
    board3DStyle: com.example.logiclab.model.Board3DStyle = com.example.logiclab.model.Board3DStyle.PCB_LAB,
    onToggleBoard3DStyle: () -> Unit = {},
    onOpenThemeSelector: () -> Unit = {}
) {
    val colors = LogicLabDesign.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.darkBg)
            .statusBarsPadding()
            .border(1.dp, colors.border)
            .testTag("lab_top_bar")
    ) {
        // Primary Top Header - Compact, single-row layout that never wraps or clips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title & Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Heartbeat indicator with pulse animation color
                val pulseColor by animateColorAsState(
                    targetValue = if (isSimulating) SignalHigh else SignalUnknown,
                    animationSpec = tween(300),
                    label = "pulseColor"
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(pulseColor)
                )
                Text(
                    text = "LogicLab",
                    color = colors.textPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Mode Navigation Pills + Theme Switcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Mode Navigation Pills (Compact, single line, no text wrapping)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.cardBg)
                        .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                        .padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppMode.entries.forEach { mode ->
                        val isSelected = mode == currentMode
                        val tabBg by animateColorAsState(
                            targetValue = if (isSelected) colors.accentPrimary else Color.Transparent,
                            animationSpec = tween(250),
                            label = "tabBg"
                        )
                        val tabText by animateColorAsState(
                            targetValue = if (isSelected) colors.onAccent else colors.textSecondary,
                            animationSpec = tween(250),
                            label = "tabText"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(tabBg)
                                .clickable { onModeChange(mode) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("nav_mode_${mode.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = tabText,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Theme Quick Button - Always prominently visible and accessible
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.cardBg)
                        .border(1.dp, colors.accentPrimary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable { onOpenThemeSelector() }
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        .testTag("topbar_theme_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Themes",
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Theme",
                            color = colors.textPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Secondary Toolbar (only for Simulator mode, engineered with small controls)
        if (currentMode == AppMode.SIMULATOR) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBg)
                    .border(1.dp, colors.border)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left File / History actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    CompactIconButton(
                        icon = Icons.Default.Add,
                        label = "New",
                        onClick = onNewCircuit,
                        tag = "new_circuit_btn"
                    )
                    CompactIconButton(
                        icon = Icons.Default.FolderOpen,
                        label = "Open",
                        onClick = onOpenRepo,
                        tag = "open_repo_btn"
                    )
                    CompactIconButton(
                        icon = Icons.Default.Save,
                        label = "Save",
                        onClick = onSaveCircuit,
                        tag = "save_circuit_btn"
                    )

                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(colors.border)
                            .padding(horizontal = 2.dp)
                    )

                    CompactIconButton(
                        icon = Icons.AutoMirrored.Filled.Undo,
                        label = "Undo",
                        onClick = onUndo,
                        enabled = canUndo,
                        tag = "undo_btn"
                    )
                    CompactIconButton(
                        icon = Icons.AutoMirrored.Filled.Redo,
                        label = "Redo",
                        onClick = onRedo,
                        enabled = canRedo,
                        tag = "redo_btn"
                    )

                    CompactIconButton(
                        icon = Icons.Default.Gesture,
                        label = if (isWireDrawingMode) "Draw Mode ON" else "Draw Wire",
                        onClick = onToggleWireDrawingMode,
                        tint = if (isWireDrawingMode) SignalHigh else colors.accentPrimary,
                        tag = "topbar_wire_mode_btn"
                    )

                    CompactIconButton(
                        icon = Icons.Default.Timeline,
                        label = if (isSignalFlowMode) "Flow ON" else "Flow",
                        onClick = onToggleSignalFlowMode,
                        tint = if (isSignalFlowMode) SignalFlowHigh else colors.textSecondary,
                        tag = "topbar_signal_flow_btn"
                    )

                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(colors.border)
                            .padding(horizontal = 2.dp)
                    )

                    // 2D / 3D Mode Toggle Switch (Small, with Animated State Transitions)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.panelBg)
                            .border(1.dp, colors.border, RoundedCornerShape(4.dp))
                            .padding(1.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val bg2D by animateColorAsState(
                            targetValue = if (!is3DMode) colors.accentPrimary else Color.Transparent,
                            animationSpec = tween(220),
                            label = "bg2D"
                        )
                        val text2D by animateColorAsState(
                            targetValue = if (!is3DMode) colors.onAccent else colors.textSecondary,
                            animationSpec = tween(220),
                            label = "text2D"
                        )
                        val bg3D by animateColorAsState(
                            targetValue = if (is3DMode) colors.accentPrimary else Color.Transparent,
                            animationSpec = tween(220),
                            label = "bg3D"
                        )
                        val text3D by animateColorAsState(
                            targetValue = if (is3DMode) colors.onAccent else colors.textSecondary,
                            animationSpec = tween(220),
                            label = "text3D"
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(bg2D)
                                .clickable { if (is3DMode) onToggle3DMode() }
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                .testTag("toggle_2d_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2D",
                                fontSize = 9.5.sp,
                                fontWeight = if (!is3DMode) FontWeight.Bold else FontWeight.Normal,
                                color = text2D
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(bg3D)
                                .clickable { if (!is3DMode) onToggle3DMode() }
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                .testTag("toggle_3d_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "3D",
                                fontSize = 9.5.sp,
                                fontWeight = if (is3DMode) FontWeight.Bold else FontWeight.Normal,
                                color = text3D
                            )
                        }
                    }

                    if (is3DMode) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.panelBg)
                                .border(
                                    1.dp,
                                    if (board3DStyle == com.example.logiclab.model.Board3DStyle.IC_TRAINER_KIT) Color(0xFFFFB300) else Color(0xFF00E676),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable { onToggleBoard3DStyle() }
                                .padding(horizontal = 4.5.dp, vertical = 2.dp)
                                .testTag("topbar_board_3d_style_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (board3DStyle == com.example.logiclab.model.Board3DStyle.IC_TRAINER_KIT) "🎛️ Kit" else "⚡ PCB",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (board3DStyle == com.example.logiclab.model.Board3DStyle.IC_TRAINER_KIT) Color(0xFFFFB300) else Color(0xFF00E676)
                            )
                        }
                    }
                }

                // Right Simulation Control actions (Small Buttons with Transitions)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    // AI Assistant Trigger
                    CompactIconButton(
                        icon = Icons.Default.Psychology,
                        label = "AI Assistant",
                        onClick = onOpenAiAssistant,
                        tint = colors.accentPrimary,
                        tag = "ai_assistant_btn"
                    )

                    Box(
                        modifier = Modifier
                            .height(14.dp)
                            .width(1.dp)
                            .background(colors.border)
                    )

                    // Step Simulation
                    CompactIconButton(
                        icon = Icons.Default.SkipNext,
                        label = "Step Clock",
                        onClick = onStepSimulate,
                        tag = "step_sim_btn"
                    )

                    // Reset Simulation
                    CompactIconButton(
                        icon = Icons.Default.Refresh,
                        label = "Reset",
                        onClick = onResetSimulation,
                        tag = "reset_sim_btn"
                    )

                    // Run / Pause Button (Small & Sleek with animated transitions)
                    val runInteraction = remember { MutableInteractionSource() }
                    val runPressed by runInteraction.collectIsPressedAsState()
                    val runScale by animateFloatAsState(
                        targetValue = if (runPressed) 0.90f else 1.0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "runScale"
                    )

                    val runContainerColor by animateColorAsState(
                        targetValue = if (isSimulating) Color(0xFF1B3822) else LabAccentBlue,
                        animationSpec = tween(250),
                        label = "runContainerColor"
                    )
                    val runTextColor by animateColorAsState(
                        targetValue = if (isSimulating) SignalHigh else Color.White,
                        animationSpec = tween(250),
                        label = "runTextColor"
                    )

                    Button(
                        onClick = onToggleSimulate,
                        interactionSource = runInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = runContainerColor
                        ),
                        modifier = Modifier
                            .height(22.dp)
                            .scale(runScale)
                            .padding(start = 2.dp)
                            .testTag("run_pause_btn"),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = runTextColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSimulating) "Running" else "Run",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = runTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = LabTextPrimary,
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
        targetValue = if (enabled) tint else LabTextTertiary,
        animationSpec = tween(200),
        label = "iconTint"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .scale(scale)
            .clip(RoundedCornerShape(3.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = animatedTint,
            modifier = Modifier.size(13.dp)
        )
    }
}
