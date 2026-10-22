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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabAccentCyan
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalFlowHigh
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown

data class SignalChannel(
    val name: String,
    val history: List<DigitalState>
)

/**
 * Advanced Logic Analyzer & Digital Oscilloscope Waveform Viewer.
 * Visualizes real-time square waves, digital transitions, and provides a dedicated
 * CRT oscilloscope display when an Oscillator or Clock component is clicked.
 */
@Composable
fun TimingDiagramView(
    channels: List<SignalChannel>,
    timeStepCount: Int,
    selectedComponent: CircuitComponent? = null,
    modifier: Modifier = Modifier
) {
    var zoomMultiplier by remember { mutableFloatStateOf(1.0f) }
    val isOscillatorSelected = selectedComponent != null &&
            (selectedComponent.type == "OSCILLATOR" || selectedComponent.type == "CLOCK")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LabPanelBg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("timing_diagram_view"),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Top Header: Title, Telemetry & Timebase Zoom controls
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
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = if (isOscillatorSelected) Color(0xFFFFB300) else LabAccentBlue,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isOscillatorSelected) "DIGITAL OSCILLOSCOPE & LOGIC ANALYZER" else "LOGIC ANALYZER / TIMING DIAGRAM",
                    color = LabTextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Zoom Timebase Selector Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(0.75f, 1.0f, 1.5f).forEach { scale ->
                    val isSel = zoomMultiplier == scale
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else LabCardBg)
                            .border(1.dp, if (isSel) MaterialTheme.colorScheme.primary else LabBorder, RoundedCornerShape(4.dp))
                            .clickable { zoomMultiplier = scale }
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${scale}x",
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else LabTextSecondary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dedicated Oscilloscope Waveform Display for Digital Oscillator
        if (isOscillatorSelected) {
            val oscName = selectedComponent!!.label.ifBlank { selectedComponent.id }
            val isHigh = selectedComponent.switchState

            Surface(
                color = Color(0xFF071118),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dedicated_oscilloscope_panel")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Oscilloscope Channel Badge & Live Metrics
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (isHigh) SignalHigh else Color(0xFFFFB300))
                        )
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "CH1: $oscName",
                                    color = Color(0xFFFFB300),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = if (isHigh) "[HIGH 5V]" else "[LOW 0V]",
                                    color = if (isHigh) SignalHigh else LabTextSecondary,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = "f ≈ 1.7 Hz • T = 600 ms • 50% Duty • 5.0 V TTL",
                                color = LabTextSecondary,
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Mini live animated CRT oscilloscope sweep trace
                    LiveCrtWaveformTrace(
                        isHigh = isHigh,
                        modifier = Modifier
                            .width(130.dp)
                            .height(28.dp)
                    )
                }
            }
        }

        // Waveform Channels Section
        if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .background(LabCardBg, RoundedCornerShape(4.dp))
                    .border(1.dp, LabBorder, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Add Oscillators, Clocks, or Switches to visualise live waveforms.",
                    color = LabTextSecondary,
                    fontSize = 11.sp
                )
            }
            return
        }

        val scrollState = rememberScrollState()
        val stepWidth = 22.dp * zoomMultiplier
        val traceHeight = 28.dp
        val totalCanvasWidth = (stepWidth * timeStepCount.coerceAtLeast(20)).coerceAtLeast(320.dp)

        // Automatically follow wave progression
        LaunchedEffect(timeStepCount) {
            scrollState.scrollTo(scrollState.maxValue)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LabCardBg, RoundedCornerShape(4.dp))
                .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
        ) {
            // Left fixed column for Channel Labels
            Column(
                modifier = Modifier
                    .width(96.dp)
                    .background(LabPanelBg)
                    .border(1.dp, LabBorder)
            ) {
                // Header spacer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text("SIGNAL", color = LabTextSecondary, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                }

                channels.forEach { ch ->
                    val isOscChannel = selectedComponent != null &&
                            (ch.name.equals(selectedComponent.label, ignoreCase = true) ||
                             ch.name.equals(selectedComponent.id, ignoreCase = true))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(traceHeight)
                            .background(if (isOscChannel) Color(0x22FFB300) else Color.Transparent)
                            .padding(horizontal = 6.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (isOscChannel) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB300))
                                )
                            }
                            Text(
                                text = ch.name,
                                color = if (isOscChannel) Color(0xFFFFB300) else LabTextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isOscChannel) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Right scrollable waveform canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
            ) {
                Canvas(
                    modifier = Modifier
                        .width(totalCanvasWidth)
                        .height(traceHeight * channels.size + 18.dp)
                ) {
                    val stepPx = stepWidth.toPx()
                    val traceHPx = traceHeight.toPx()
                    val topPaddingPx = 18.dp.toPx()

                    // Time axis ticks
                    val totalSteps = timeStepCount.coerceAtLeast(20)
                    for (i in 0 until totalSteps) {
                        val x = i * stepPx
                        drawLine(
                            color = Color(0xFF1E2A3B),
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = 0.8f
                        )
                    }

                    // Render waveforms per channel
                    channels.forEachIndexed { chIdx, ch ->
                        val isOscChannel = selectedComponent != null &&
                                (ch.name.equals(selectedComponent.label, ignoreCase = true) ||
                                 ch.name.equals(selectedComponent.id, ignoreCase = true))

                        val baseY = topPaddingPx + chIdx * traceHPx
                        val lowY = baseY + traceHPx - 5f
                        val highY = baseY + 5f

                        // Grid guideline for low level
                        drawLine(
                            color = if (isOscChannel) Color(0x33FFB300) else Color(0xFF1B2330),
                            start = Offset(0f, lowY),
                            end = Offset(size.width, lowY),
                            strokeWidth = 1f
                        )

                        if (ch.history.isNotEmpty()) {
                            val path = Path()
                            var prevY = if (ch.history[0] == DigitalState.HIGH) highY else lowY
                            path.moveTo(0f, prevY)

                            for (i in ch.history.indices) {
                                val state = ch.history[i]
                                val curY = if (state == DigitalState.HIGH) highY else lowY
                                val x = i * stepPx
                                val nextX = (i + 1) * stepPx

                                // Vertical edge transition
                                if (curY != prevY) {
                                    path.lineTo(x, curY)
                                }
                                // Horizontal level
                                path.lineTo(nextX, curY)
                                prevY = curY
                            }

                            // Active glow for oscillator channel
                            if (isOscChannel) {
                                drawPath(
                                    path = path,
                                    color = Color(0x55FFB300),
                                    style = Stroke(width = 4.5f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
                                )
                            }

                            drawPath(
                                path = path,
                                color = if (isOscChannel) Color(0xFFFFB300) else SignalHigh,
                                style = Stroke(width = if (isOscChannel) 2.6f else 2.0f, cap = StrokeCap.Square, join = StrokeJoin.Miter)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated Phosphor CRT Waveform Sweep for Live Oscilloscope Visualization
 */
@Composable
fun LiveCrtWaveformTrace(
    isHigh: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "crt_sweep")
    val sweepPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepPhase"
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF040A0F))
            .border(0.8.dp, Color(0xFF1C2C38), RoundedCornerShape(4.dp))
    ) {
        val w = size.width
        val h = size.height

        // CRT Division Grid
        val gridColor = Color(0x1A00E5FF)
        drawLine(gridColor, Offset(0f, h * 0.25f), Offset(w, h * 0.25f), strokeWidth = 0.6f)
        drawLine(gridColor, Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 0.6f)
        drawLine(gridColor, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 0.6f)
        drawLine(gridColor, Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 0.6f)
        drawLine(gridColor, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 0.6f)

        // Draw animated running square wave
        val wavePath = Path()
        val periodWidth = w / 3.0f
        val highY = h * 0.22f
        val lowY = h * 0.78f

        val startX = -sweepPhase * periodWidth
        var x = startX
        var curHigh = isHigh

        wavePath.moveTo(x, if (curHigh) highY else lowY)
        while (x < w + periodWidth) {
            val nextX = x + periodWidth / 2f
            wavePath.lineTo(nextX, if (curHigh) highY else lowY)
            curHigh = !curHigh
            wavePath.lineTo(nextX, if (curHigh) highY else lowY)
            x = nextX
        }

        // Phosphor glow aura
        drawPath(
            path = wavePath,
            color = Color(0x4400E5FF),
            style = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
        )
        // Primary trace line
        drawPath(
            path = wavePath,
            color = Color(0xFF00E5FF),
            style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
        )

        // Sweep phosphor leading beam
        val sweepX = sweepPhase * w
        drawLine(
            color = Color(0x3300E5FF),
            start = Offset(sweepX, 0f),
            end = Offset(sweepX, h),
            strokeWidth = 1.2f
        )
    }
}
