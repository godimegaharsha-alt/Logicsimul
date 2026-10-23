package com.example.logiclab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown

// Standard DSO Channel Palette
private val ChannelColors = listOf(
    Color(0xFFFFD600), // CH1: Canary Yellow
    Color(0xFF00E5FF), // CH2: Electric Cyan
    Color(0xFFE040FB), // CH3: Neon Magenta
    Color(0xFF76FF03)  // CH4: Phosphor Lime
)

private val TimebaseOptions = listOf("20 ms", "50 ms", "100 ms", "200 ms", "500 ms")
private val VoltsDivOptions = listOf("1 V/div", "2 V/div", "5 V/div")

@Composable
fun OscilloscopeView(
    wires: List<CircuitWire>,
    components: List<CircuitComponent>,
    selectedWireId: String?,
    probedWireIds: Set<String>,
    wireWaveforms: Map<String, List<DigitalState>>,
    timeStepCount: Int,
    onToggleWireProbe: (String) -> Unit,
    onAddWireProbe: (String) -> Unit,
    onRemoveWireProbe: (String) -> Unit,
    onClearProbes: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRunning by remember { mutableStateOf(true) }
    var selectedTimebaseIndex by remember { mutableStateOf(2) } // 100 ms default
    var selectedVoltsDivIndex by remember { mutableStateOf(1) } // 2 V/div default
    var isWirePickerOpen by remember { mutableStateOf(false) }

    // Pulsing phosphor beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "scope_anim")
    val beamPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beamPulse"
    )

    // Frozen snapshot when STOP is clicked
    var frozenWaveforms by remember { mutableStateOf<Map<String, List<DigitalState>>>(emptyMap()) }
    val displayWaveforms = if (isRunning) wireWaveforms else frozenWaveforms

    // Active probed wires (limit to max 4 channels)
    // If no wire is explicitly probed, automatically probe the selected wire, or first 2 wires
    val effectiveProbedIds = remember(probedWireIds, selectedWireId, wires) {
        if (probedWireIds.isNotEmpty()) {
            probedWireIds.take(4).toList()
        } else if (selectedWireId != null) {
            listOf(selectedWireId)
        } else if (wires.isNotEmpty()) {
            wires.take(2).map { it.id }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F14))
            .border(1.dp, Color(0xFF1E2A38))
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("oscilloscope_panel")
    ) {
        // --- TOP DSO HEADER BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Instrument Model & Live Sampling Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = Color(0xFF00E5FF),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "TEK-LAB 4000 DSO",
                    color = Color(0xFFECEFF1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "100 MSa/s",
                    color = Color(0xFF78909C),
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace
                )

                // RUN / STOP Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isRunning) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF5252).copy(alpha = 0.25f))
                        .border(1.dp, if (isRunning) Color(0xFF00E676) else Color(0xFFFF5252), RoundedCornerShape(3.dp))
                        .clickable {
                            if (isRunning) {
                                frozenWaveforms = wireWaveforms
                            }
                            isRunning = !isRunning
                        }
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        .testTag("scope_run_stop_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) Color(0xFF00E676) else Color(0xFFFF5252))
                        )
                        Text(
                            text = if (isRunning) "RUN" else "STOP",
                            color = if (isRunning) Color(0xFF00E676) else Color(0xFFFF5252),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Controls: TIME/DIV, VOLTS/DIV, & Add Wire Probe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Timebase Selector
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF161E27))
                        .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(3.dp))
                        .clickable {
                            selectedTimebaseIndex = (selectedTimebaseIndex + 1) % TimebaseOptions.size
                        }
                        .padding(horizontal = 5.dp, vertical = 1.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "T: ${TimebaseOptions[selectedTimebaseIndex]}",
                        color = Color(0xFF90CAF9),
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Volts / Div Selector
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF161E27))
                        .border(1.dp, Color(0xFF2C3E50), RoundedCornerShape(3.dp))
                        .clickable {
                            selectedVoltsDivIndex = (selectedVoltsDivIndex + 1) % VoltsDivOptions.size
                        }
                        .padding(horizontal = 5.dp, vertical = 1.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V: ${VoltsDivOptions[selectedVoltsDivIndex]}",
                        color = Color(0xFFFFE082),
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Add Probe Clip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF00E5FF).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                        .clickable { isWirePickerOpen = true }
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        .testTag("add_scope_probe_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Probe", tint = Color(0xFF00E5FF), modifier = Modifier.size(9.dp))
                        Text(
                            text = "PROBE",
                            color = Color(0xFF00E5FF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Dropdown of all circuit wires to probe
                    DropdownMenu(
                        expanded = isWirePickerOpen,
                        onDismissRequest = { isWirePickerOpen = false }
                    ) {
                        if (wires.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No wires in circuit", fontSize = 11.sp) },
                                onClick = { isWirePickerOpen = false }
                            )
                        } else {
                            wires.forEach { wire ->
                                val fromComp = components.find { it.id == wire.fromComponentId }
                                val toComp = components.find { it.id == wire.toComponentId }
                                val fromLabel = fromComp?.label?.ifBlank { null } ?: fromComp?.type ?: wire.fromComponentId.take(4)
                                val toLabel = toComp?.label?.ifBlank { null } ?: toComp?.type ?: wire.toComponentId.take(4)
                                val isProbed = effectiveProbedIds.contains(wire.id)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${if (isProbed) "✓ " else "+ "} Wire: $fromLabel:${wire.fromPortId} → $toLabel:${wire.toPortId}",
                                            fontSize = 11.sp,
                                            color = if (isProbed) Color(0xFF00E5FF) else Color.White
                                        )
                                    },
                                    onClick = {
                                        onToggleWireProbe(wire.id)
                                        isWirePickerOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- CHANNEL PROBES PILL BAR (CH1 .. CH4) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            effectiveProbedIds.forEachIndexed { index, wireId ->
                val wire = wires.find { it.id == wireId }
                val fromComp = components.find { it.id == wire?.fromComponentId }
                val toComp = components.find { it.id == wire?.toComponentId }
                val fromName = fromComp?.label?.ifBlank { null } ?: fromComp?.type ?: wire?.fromComponentId?.take(4) ?: "SRC"
                val toName = toComp?.label?.ifBlank { null } ?: toComp?.type ?: wire?.toComponentId?.take(4) ?: "DST"
                val chColor = ChannelColors.getOrElse(index) { Color.White }
                val state = wire?.state ?: DigitalState.UNKNOWN
                val voltageStr = when (state) {
                    DigitalState.HIGH -> "5.00V"
                    DigitalState.LOW -> "0.00V"
                    DigitalState.UNKNOWN -> "2.50V (Z)"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(chColor.copy(alpha = 0.12f))
                        .border(1.dp, chColor.copy(alpha = 0.6f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "CH${index + 1}",
                            color = chColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$fromName:$toName",
                            color = Color(0xFFCFD8DC),
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = voltageStr,
                            color = chColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Unprobe",
                            tint = Color(0xFF78909C),
                            modifier = Modifier
                                .size(9.dp)
                                .clickable { onRemoveWireProbe(wireId) }
                        )
                    }
                }
            }
        }

        // --- PHOSPHOR CRT OSCILLOSCOPE SCREEN ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF05100B)) // Vintage dark phosphor green background
                .border(1.2.dp, Color(0xFF1B3828), RoundedCornerShape(4.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("oscilloscope_screen_canvas")
            ) {
                val w = size.width
                val h = size.height

                // 1. Draw Reticle Grid (8 vertical divisions, 10 horizontal divisions)
                val hDivisions = 10
                val vDivisions = 8
                val cellW = w / hDivisions
                val cellH = h / vDivisions

                // Minor subdivisions ticks along center axes
                val gridColor = Color(0xFF0F3B25)
                val centerAxisColor = Color(0xFF1B5938)
                val centerY = h / 2f
                val centerX = w / 2f

                // Draw vertical grid lines
                for (i in 0..hDivisions) {
                    val x = i * cellW
                    val isCenter = (i == hDivisions / 2)
                    drawLine(
                        color = if (isCenter) centerAxisColor else gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = if (isCenter) 1.2f else 0.6f
                    )
                }

                // Draw horizontal grid lines
                for (j in 0..vDivisions) {
                    val y = j * cellH
                    val isCenter = (j == vDivisions / 2)
                    drawLine(
                        color = if (isCenter) centerAxisColor else gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = if (isCenter) 1.2f else 0.6f
                    )
                }

                // Draw fine graduation tick marks on center axes
                for (t in 0..(hDivisions * 5)) {
                    val tx = t * (cellW / 5f)
                    drawLine(color = centerAxisColor, start = Offset(tx, centerY - 2.5f), end = Offset(tx, centerY + 2.5f), strokeWidth = 0.8f)
                }
                for (t in 0..(vDivisions * 5)) {
                    val ty = t * (cellH / 5f)
                    drawLine(color = centerAxisColor, start = Offset(centerX - 2.5f, ty), end = Offset(centerX + 2.5f, ty), strokeWidth = 0.8f)
                }

                // 2. Draw Trigger Level Dotted Line (Center at 2.5V reference)
                val trigY = centerY
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f), 0f)
                drawLine(
                    color = Color(0xFFFFB300).copy(alpha = 0.45f),
                    start = Offset(0f, trigY),
                    end = Offset(w, trigY),
                    strokeWidth = 0.8f,
                    pathEffect = dashEffect
                )
                // Trigger Marker 'T' on right edge
                drawCircle(color = Color(0xFFFFB300), radius = 3.5f, center = Offset(w - 6f, trigY))

                // 3. Draw Channel Traces
                if (effectiveProbedIds.isEmpty()) {
                    // Empty state notice
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(72, 120, 95)
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.MONOSPACE
                    }
                    drawContext.canvas.nativeCanvas.drawText("NO PROBE ATTACHED", centerX, centerY - 10f, textPaint)
                    textPaint.textSize = 20f
                    drawContext.canvas.nativeCanvas.drawText("Tap a wire or '+ PROBE' to clip DSO leads", centerX, centerY + 18f, textPaint)
                } else {
                    val numChannels = effectiveProbedIds.size
                    val chSlotHeight = h / (numChannels + 0.2f)

                    effectiveProbedIds.forEachIndexed { chIndex, wireId ->
                        val chColor = ChannelColors.getOrElse(chIndex) { Color.White }
                        val rawHistory = displayWaveforms[wireId] ?: emptyList()

                        // Slot baseline and amplitude
                        val slotCenterY = chSlotHeight * (chIndex + 0.6f)
                        val highY = slotCenterY - chSlotHeight * 0.32f
                        val lowY = slotCenterY + chSlotHeight * 0.32f
                        val midY = slotCenterY

                        // Draw Ground Reference Triangle on left edge
                        val gndPath = Path().apply {
                            moveTo(2f, lowY - 4f)
                            lineTo(9f, lowY)
                            lineTo(2f, lowY + 4f)
                            close()
                        }
                        drawPath(path = gndPath, color = chColor)

                        // If no history yet, draw flat baseline
                        if (rawHistory.isEmpty()) {
                            drawLine(
                                color = chColor.copy(alpha = 0.6f),
                                start = Offset(12f, lowY),
                                end = Offset(w - 4f, lowY),
                                strokeWidth = 1.8f
                            )
                        } else {
                            // Render realistic digital wave with rise/fall slew transitions
                            val points = rawHistory.takeLast(50)
                            val stepX = (w - 24f) / (points.size.coerceAtLeast(2) - 1)

                            val tracePath = Path()
                            val glowPath = Path()

                            var lastY = lowY
                            points.forEachIndexed { idx, state ->
                                val curX = 14f + idx * stepX
                                val targetY = when (state) {
                                    DigitalState.HIGH -> highY
                                    DigitalState.LOW -> lowY
                                    DigitalState.UNKNOWN -> midY
                                }

                                if (idx == 0) {
                                    tracePath.moveTo(curX, targetY)
                                    glowPath.moveTo(curX, targetY)
                                } else {
                                    if (targetY != lastY) {
                                        // Exponential/slew edge transition (steep transition)
                                        val slewX = curX - stepX * 0.15f
                                        tracePath.lineTo(slewX, lastY)
                                        tracePath.lineTo(curX, targetY)
                                        glowPath.lineTo(slewX, lastY)
                                        glowPath.lineTo(curX, targetY)
                                    } else {
                                        tracePath.lineTo(curX, targetY)
                                        glowPath.lineTo(curX, targetY)
                                    }
                                }
                                lastY = targetY
                            }

                            // 1st Pass: Wide Phosphor Bloom / Glow
                            drawPath(
                                path = glowPath,
                                color = chColor.copy(alpha = 0.35f * beamPulse),
                                style = Stroke(width = 4.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // 2nd Pass: Brilliant Center Beam Trace
                            drawPath(
                                path = tracePath,
                                color = chColor,
                                style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )

                            // 3rd Pass: Live Electron Beam Tip (blinking spot on the rightmost edge)
                            val lastX = 14f + (points.size - 1) * stepX
                            drawCircle(
                                color = Color.White,
                                radius = 2.4f,
                                center = Offset(lastX, lastY)
                            )
                            drawCircle(
                                color = chColor.copy(alpha = 0.6f * beamPulse),
                                radius = 5.5f,
                                center = Offset(lastX, lastY)
                            )
                        }
                    }
                }
            }

            // Bottom HUD Readout (Vpp, Vmax, Vmin, Freq)
            if (effectiveProbedIds.isNotEmpty()) {
                val firstWireId = effectiveProbedIds.first()
                val hist = displayWaveforms[firstWireId] ?: emptyList()
                val hasHigh = hist.contains(DigitalState.HIGH)
                val hasLow = hist.contains(DigitalState.LOW)
                val transitions = hist.zipWithNext().count { it.first != it.second }
                val freqStr = if (transitions > 1) {
                    val estHz = (transitions * 1.65f).coerceIn(0.5f, 20f)
                    "%.2f Hz".format(estHz)
                } else if (hasHigh) {
                    "DC (5V)"
                } else {
                    "DC (0V)"
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 4.dp)
                        .background(Color(0xFF040A07).copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vpp: 5.00V",
                        color = Color(0xFF00E676),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Vmax: 5.0V",
                        color = Color(0xFF81C784),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Freq: $freqStr",
                        color = Color(0xFFFFD54F),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Trig: CH1 ↑",
                        color = Color(0xFF80DEEA),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
