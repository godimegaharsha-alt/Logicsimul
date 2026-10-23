package com.example.logiclab.ui.canvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.ComponentDefinition
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalHighGlow
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown

object GateSymbols {

    fun drawComponent(
        drawScope: DrawScope,
        comp: CircuitComponent,
        def: ComponentDefinition,
        isSelected: Boolean
    ) {
        val x = comp.x
        val y = comp.y
        val w = def.width
        val h = def.height

        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E)
        val strokeWidth = if (isSelected) 3.5f else 2.2f
        val fillColor = LabCardBg

        when (comp.type) {
            "AND" -> drawAndGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "OR" -> drawOrGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "NOT" -> drawNotGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "NAND" -> drawNandGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "NOR" -> drawNorGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "XOR" -> drawXorGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "XNOR" -> drawXnorGate(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "BUFFER" -> drawBuffer(drawScope, x, y, w, h, strokeColor, fillColor, strokeWidth)
            "SWITCH" -> drawSwitch(drawScope, x, y, w, h, comp.switchState, isSelected)
            "LED", "DIODE" -> drawLed(drawScope, x, y, w, h, comp.inputValues["IN"] ?: DigitalState.LOW, isSelected)
            "CLOCK" -> drawClock(drawScope, x, y, w, h, comp.switchState, isSelected)
            "OSCILLATOR" -> drawOscillator(drawScope, x, y, w, h, comp.switchState, isSelected)
            "D_FLIP_FLOP", "JK_FLIP_FLOP", "T_FLIP_FLOP", "SR_FLIP_FLOP",
            "SR_LATCH", "SR_LATCH_NAND", "GATED_SR_LATCH", "D_LATCH", "JK_LATCH", "T_LATCH" -> drawFlipFlop(drawScope, x, y, w, h, def, comp, strokeColor, strokeWidth)
            "CONST_HIGH" -> drawConstant(drawScope, x, y, w, h, true, isSelected)
            "CONST_LOW" -> drawConstant(drawScope, x, y, w, h, false, isSelected)
            "DIGITAL_PROBE" -> drawProbe(drawScope, x, y, w, h, comp.inputValues["IN"] ?: DigitalState.UNKNOWN, isSelected)
            "SEVEN_SEG" -> drawSevenSegment(drawScope, x, y, w, h, comp.inputValues, isSelected)
            "BUZZER" -> drawBuzzer(drawScope, x, y, w, h, comp.inputValues["IN"] ?: DigitalState.LOW, isSelected)
            "MUX_2_1", "MUX_4_1", "MUX_8_1", "MUX_16_1" -> drawMuxTrapezoid(drawScope, x, y, w, h, def, comp, strokeColor, strokeWidth)
            "DEMUX_1_2", "DEMUX_1_4", "DEMUX_1_8", "DEMUX_1_16" -> drawDemuxTrapezoid(drawScope, x, y, w, h, def, comp, strokeColor, strokeWidth)
            else -> drawIcBlock(drawScope, x, y, w, h, def, comp, strokeColor, strokeWidth)
        }
    }

    private fun drawAndGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(x + 12f, y + 4f)
            lineTo(x + w * 0.5f, y + 4f)
            arcTo(
                rect = Rect(x + 12f, y + 4f, x + w - 12f, y + h - 4f),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(x + 12f, y + h - 4f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }

    private fun drawOrGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(x + 10f, y + 4f)
            quadraticTo(x + w * 0.45f, y + 4f, x + w - 10f, y + h * 0.5f)
            quadraticTo(x + w * 0.45f, y + h - 4f, x + 10f, y + h - 4f)
            quadraticTo(x + 22f, y + h * 0.5f, x + 10f, y + 4f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    private fun drawNotGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val bubbleRadius = 5f
        val rightX = x + w - bubbleRadius * 2 - 4f
        val path = Path().apply {
            moveTo(x + 12f, y + 6f)
            lineTo(rightX, y + h * 0.5f)
            lineTo(x + 12f, y + h - 6f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Inversion bubble
        scope.drawCircle(
            color = fillColor,
            radius = bubbleRadius,
            center = Offset(rightX + bubbleRadius, y + h * 0.5f)
        )
        scope.drawCircle(
            color = strokeColor,
            radius = bubbleRadius,
            center = Offset(rightX + bubbleRadius, y + h * 0.5f),
            style = Stroke(width = strokeWidth)
        )
    }

    private fun drawNandGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val bubbleR = 5f
        val rightW = w - bubbleR * 2 - 4f
        val path = Path().apply {
            moveTo(x + 12f, y + 4f)
            lineTo(x + rightW * 0.5f, y + 4f)
            arcTo(
                rect = Rect(x + 12f, y + 4f, x + rightW, y + h - 4f),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            lineTo(x + 12f, y + h - 4f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        scope.drawCircle(fillColor, bubbleR, Offset(x + rightW + bubbleR, y + h * 0.5f))
        scope.drawCircle(strokeColor, bubbleR, Offset(x + rightW + bubbleR, y + h * 0.5f), style = Stroke(strokeWidth))
    }

    private fun drawNorGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val bubbleR = 5f
        val rightX = x + w - bubbleR * 2 - 6f
        val path = Path().apply {
            moveTo(x + 10f, y + 4f)
            quadraticTo(x + w * 0.4f, y + 4f, rightX, y + h * 0.5f)
            quadraticTo(x + w * 0.4f, y + h - 4f, x + 10f, y + h - 4f)
            quadraticTo(x + 22f, y + h * 0.5f, x + 10f, y + 4f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        scope.drawCircle(fillColor, bubbleR, Offset(rightX + bubbleR, y + h * 0.5f))
        scope.drawCircle(strokeColor, bubbleR, Offset(rightX + bubbleR, y + h * 0.5f), style = Stroke(strokeWidth))
    }

    private fun drawXorGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        // Back input arc
        val backArc = Path().apply {
            moveTo(x + 8f, y + 4f)
            quadraticTo(x + 18f, y + h * 0.5f, x + 8f, y + h - 4f)
        }
        scope.drawPath(backArc, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        // Body
        val body = Path().apply {
            moveTo(x + 16f, y + 4f)
            quadraticTo(x + w * 0.5f, y + 4f, x + w - 10f, y + h * 0.5f)
            quadraticTo(x + w * 0.5f, y + h - 4f, x + 16f, y + h - 4f)
            quadraticTo(x + 26f, y + h * 0.5f, x + 16f, y + 4f)
            close()
        }
        scope.drawPath(body, fillColor, style = Fill)
        scope.drawPath(body, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }

    private fun drawXnorGate(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val bubbleR = 5f
        val rightX = x + w - bubbleR * 2 - 6f

        val backArc = Path().apply {
            moveTo(x + 8f, y + 4f)
            quadraticTo(x + 18f, y + h * 0.5f, x + 8f, y + h - 4f)
        }
        scope.drawPath(backArc, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        val body = Path().apply {
            moveTo(x + 16f, y + 4f)
            quadraticTo(x + w * 0.45f, y + 4f, rightX, y + h * 0.5f)
            quadraticTo(x + w * 0.45f, y + h - 4f, x + 16f, y + h - 4f)
            quadraticTo(x + 26f, y + h * 0.5f, x + 16f, y + 4f)
            close()
        }
        scope.drawPath(body, fillColor, style = Fill)
        scope.drawPath(body, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))

        scope.drawCircle(fillColor, bubbleR, Offset(rightX + bubbleR, y + h * 0.5f))
        scope.drawCircle(strokeColor, bubbleR, Offset(rightX + bubbleR, y + h * 0.5f), style = Stroke(strokeWidth))
    }

    private fun drawBuffer(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        strokeColor: Color, fillColor: Color, strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(x + 12f, y + 6f)
            lineTo(x + w - 12f, y + h * 0.5f)
            lineTo(x + 12f, y + h - 6f)
            close()
        }
        scope.drawPath(path, fillColor, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
    }

    private fun drawSwitch(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        isOn: Boolean, isSelected: Boolean
    ) {
        // Toggle Switch housing
        val borderColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF38465B)
        val bodyRect = Rect(x + 4f, y + 4f, x + w - 4f, y + h - 4f)
        scope.drawRoundRect(
            color = LabPanelBg,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = borderColor,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(if (isSelected) 3f else 1.8f)
        )

        // Pill track
        val trackW = 34f
        val trackH = 18f
        val trackX = x + 12f
        val trackY = y + (h - trackH) / 2
        val trackColor = if (isOn) Color(0xFF1E3A24) else Color(0xFF1F242C)

        scope.drawRoundRect(
            color = trackColor,
            topLeft = Offset(trackX, trackY),
            size = Size(trackW, trackH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(9f, 9f)
        )

        // Switch thumb
        val thumbR = 8f
        val thumbX = if (isOn) trackX + trackW - thumbR - 2f else trackX + thumbR + 2f
        val thumbY = trackY + trackH / 2
        val thumbColor = if (isOn) SignalHigh else Color(0xFF717D8E)

        scope.drawCircle(thumbColor, thumbR, Offset(thumbX, thumbY))

        // State indicator dot
        val ledColor = if (isOn) SignalHigh else SignalLow
        scope.drawCircle(ledColor, 4f, Offset(x + w - 16f, y + h / 2))
    }

    private fun drawLed(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        state: DigitalState, isSelected: Boolean
    ) {
        val cx = x + w / 2
        val cy = y + h / 2
        val r = 16f
        val isHigh = state == DigitalState.HIGH
        val ledColor = if (isHigh) SignalHigh else SignalLow

        if (isHigh) {
            // Glow halo
            scope.drawCircle(SignalHighGlow, r + 8f, Offset(cx, cy))
        }

        // Rim
        scope.drawCircle(Color(0xFF263242), r + 2f, Offset(cx, cy))
        scope.drawCircle(Color(0xFF141923), r, Offset(cx, cy))

        // Lens
        scope.drawCircle(ledColor, r - 3f, Offset(cx, cy))

        // Specular reflection highlight
        scope.drawCircle(Color.White.copy(alpha = if (isHigh) 0.6f else 0.2f), 3.5f, Offset(cx - 5f, cy - 5f))

        if (isSelected) {
            scope.drawCircle(Color(0xFF58A6FF), r + 5f, Offset(cx, cy), style = Stroke(2f))
        }
    }

    private fun drawBuzzer(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        state: DigitalState, isSelected: Boolean
    ) {
        val cx = x + w / 2
        val cy = y + h / 2
        val isHigh = state == DigitalState.HIGH
        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E)

        // Draw speaker body cylinder
        scope.drawCircle(Color(0xFF1E2836), 18f, Offset(cx, cy))
        scope.drawCircle(if (isHigh) Color(0xFFFFB300) else strokeColor, 18f, Offset(cx, cy), style = Stroke(2f))

        // Center acoustic hole
        scope.drawCircle(Color(0xFF0F141C), 8f, Offset(cx, cy))

        // Sound waves when active
        if (isHigh) {
            scope.drawArc(
                color = Color(0xFFFFB300),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx + 8f, cy - 12f),
                size = Size(14f, 24f),
                style = Stroke(2f, cap = StrokeCap.Round)
            )
            scope.drawArc(
                color = Color(0xFFFFD54F),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(cx + 14f, cy - 18f),
                size = Size(20f, 36f),
                style = Stroke(1.8f, cap = StrokeCap.Round)
            )
        }
    }

    private fun drawClock(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        tick: Boolean, isSelected: Boolean
    ) {
        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E)
        scope.drawRoundRect(
            color = LabPanelBg,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(1.8f)
        )

        // Pulse wave symbol
        val path = Path().apply {
            moveTo(x + 14f, y + h * 0.65f)
            lineTo(x + 24f, y + h * 0.65f)
            lineTo(x + 24f, y + h * 0.35f)
            lineTo(x + 36f, y + h * 0.35f)
            lineTo(x + 36f, y + h * 0.65f)
            lineTo(x + 46f, y + h * 0.65f)
        }
        val waveColor = if (tick) SignalHigh else Color(0xFF8B949E)
        scope.drawPath(path, waveColor, style = Stroke(width = 2.5f, cap = StrokeCap.Square, join = StrokeJoin.Miter))
    }

    private fun drawOscillator(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        tick: Boolean, isSelected: Boolean
    ) {
        val bodyColor = if (isSelected) Color(0xFF0C1924) else LabPanelBg
        val strokeColor = if (isSelected) Color(0xFFFFB300) else Color(0xFF28384E)
        scope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )

        // Oscilloscope Screen Insert
        val crtLeft = x + 10f
        val crtTop = y + 10f
        val crtWidth = w - 20f
        val crtHeight = h - 20f
        scope.drawRoundRect(
            color = Color(0xFF061017),
            topLeft = Offset(crtLeft, crtTop),
            size = Size(crtWidth, crtHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
        )
        // Oscilloscope CRT division crosshair lines
        val gridColor = Color(0x2258A6FF)
        scope.drawLine(gridColor, Offset(crtLeft, crtTop + crtHeight / 2f), Offset(crtLeft + crtWidth, crtTop + crtHeight / 2f), strokeWidth = 0.8f)
        scope.drawLine(gridColor, Offset(crtLeft + crtWidth / 2f, crtTop), Offset(crtLeft + crtWidth / 2f, crtTop + crtHeight), strokeWidth = 0.8f)

        // Oscillating waveform symbol (Primary CLK wave)
        val waveColor = if (tick) SignalHigh else Color(0xFF388BFD)
        val highY = crtTop + crtHeight * 0.24f
        val lowY = crtTop + crtHeight * 0.52f

        val path = Path().apply {
            moveTo(crtLeft + 4f, if (tick) highY else lowY)
            lineTo(crtLeft + crtWidth * 0.3f, if (tick) highY else lowY)
            lineTo(crtLeft + crtWidth * 0.3f, if (tick) lowY else highY)
            lineTo(crtLeft + crtWidth * 0.65f, if (tick) lowY else highY)
            lineTo(crtLeft + crtWidth * 0.65f, if (tick) highY else lowY)
            lineTo(crtLeft + crtWidth - 4f, if (tick) highY else lowY)
        }
        scope.drawPath(
            path,
            waveColor,
            style = Stroke(width = if (isSelected) 2.4f else 2f, cap = StrokeCap.Round, join = StrokeJoin.Miter)
        )

        // Glowing phosphor sweep indicator particle
        val sweepX = if (tick) crtLeft + crtWidth * 0.65f else crtLeft + crtWidth * 0.3f
        val sweepY = if (tick) highY else lowY
        scope.drawCircle(
            color = if (tick) SignalHigh else Color(0xFF58A6FF),
            radius = if (isSelected) 3.2f else 2.2f,
            center = Offset(sweepX, sweepY)
        )

        // Complementary / Inverted wave preview at bottom
        val invWaveColor = if (!tick) SignalHigh.copy(alpha = 0.85f) else Color(0xFF484F58)
        val invHighY = crtTop + crtHeight * 0.68f
        val invLowY = crtTop + crtHeight * 0.88f
        val invPath = Path().apply {
            moveTo(crtLeft + 4f, if (!tick) invHighY else invLowY)
            lineTo(crtLeft + crtWidth * 0.3f, if (!tick) invHighY else invLowY)
            lineTo(crtLeft + crtWidth * 0.3f, if (!tick) invLowY else invHighY)
            lineTo(crtLeft + crtWidth * 0.65f, if (!tick) invLowY else invHighY)
            lineTo(crtLeft + crtWidth * 0.65f, if (!tick) invHighY else invLowY)
            lineTo(crtLeft + crtWidth - 4f, if (!tick) invHighY else invLowY)
        }
        scope.drawPath(invPath, invWaveColor, style = Stroke(width = 1.6f, cap = StrokeCap.Round, join = StrokeJoin.Miter))

        // Outer border with selection indicator
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(if (isSelected) 2.2f else 1.8f)
        )
    }

    private fun drawFlipFlop(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        def: ComponentDefinition, comp: CircuitComponent,
        strokeColor: Color, strokeWidth: Float
    ) {
        val bodyRect = Rect(x + 6f, y + 4f, x + w - 6f, y + h - 4f)
        scope.drawRoundRect(
            color = LabCardBg,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(strokeWidth)
        )

        // Draw clock chevron indicator (>) on the CLK input port if present
        def.inputs.find { it.name == "CLK" || it.id == "CLK" }?.let { clkPort ->
            val clkY = y + h * clkPort.relativeY
            val clkX = x + 6f
            val triPath = Path().apply {
                moveTo(clkX, clkY - 6f)
                lineTo(clkX + 8f, clkY)
                lineTo(clkX, clkY + 6f)
            }
            scope.drawPath(triPath, strokeColor, style = Stroke(1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }

        // Draw inverting bubble on complementary output (Q_BAR / Q') if present
        def.outputs.find { it.id == "Q_BAR" || it.name.contains("'") }?.let { qBarPort ->
            val bubbleR = 4f
            val qBarY = y + h * qBarPort.relativeY
            val qBarX = x + w - 6f - bubbleR
            scope.drawCircle(LabCardBg, bubbleR, Offset(qBarX, qBarY))
            scope.drawCircle(strokeColor, bubbleR, Offset(qBarX, qBarY), style = Stroke(1.8f))
        }
    }

    private fun drawConstant(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        isHigh: Boolean, isSelected: Boolean
    ) {
        val color = if (isHigh) SignalHigh else SignalLow
        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF38465B)
        scope.drawRoundRect(
            color = LabPanelBg,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
        )
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
            style = Stroke(1.8f)
        )
        scope.drawCircle(color, 6f, Offset(x + 20f, y + h / 2))
    }

    private fun drawProbe(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        state: DigitalState, isSelected: Boolean
    ) {
        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF8B949E)
        scope.drawRoundRect(
            color = Color(0xFF0F1722),
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(1.8f)
        )
    }

    private fun drawSevenSegment(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        inputs: Map<String, DigitalState>, isSelected: Boolean
    ) {
        scope.drawRoundRect(
            color = Color(0xFF0C1017),
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
        )
        val strokeColor = if (isSelected) Color(0xFF58A6FF) else Color(0xFF283648)
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(x + 4f, y + 4f),
            size = Size(w - 8f, h - 8f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f),
            style = Stroke(2f)
        )

        // Segments: a (top), b (top-right), c (bottom-right), d (bottom), e (bottom-left), f (top-left), g (middle)
        val onColor = SignalHigh
        val offColor = Color(0xFF1E2836)

        val sx = x + 35f
        val sy = y + 25f
        val segW = 28f
        val segH = 6f
        val segL = 24f

        fun segColor(name: String): Color = if (inputs[name]?.isHigh() == true) onColor else offColor

        // a (top horizontal)
        scope.drawRoundRect(segColor("A"), Offset(sx, sy), Size(segW, segH), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // b (top right vertical)
        scope.drawRoundRect(segColor("B"), Offset(sx + segW - segH, sy + segH), Size(segH, segL), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // c (bottom right vertical)
        scope.drawRoundRect(segColor("C"), Offset(sx + segW - segH, sy + segH + segL + segH), Size(segH, segL), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // d (bottom horizontal)
        scope.drawRoundRect(segColor("D"), Offset(sx, sy + (segL + segH) * 2), Size(segW, segH), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // e (bottom left vertical)
        scope.drawRoundRect(segColor("E"), Offset(sx, sy + segH + segL + segH), Size(segH, segL), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // f (top left vertical)
        scope.drawRoundRect(segColor("F"), Offset(sx, sy + segH), Size(segH, segL), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
        // g (middle horizontal)
        scope.drawRoundRect(segColor("G"), Offset(sx, sy + segH + segL), Size(segW, segH), androidx.compose.ui.geometry.CornerRadius(3f, 3f))
    }

    private fun drawMuxTrapezoid(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        def: ComponentDefinition, comp: CircuitComponent,
        strokeColor: Color, strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(x + 10f, y + 4f)
            lineTo(x + w - 10f, y + 16f)
            lineTo(x + w - 10f, y + h - 16f)
            lineTo(x + 10f, y + h - 4f)
            close()
        }
        scope.drawPath(path, LabCardBg, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    private fun drawDemuxTrapezoid(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        def: ComponentDefinition, comp: CircuitComponent,
        strokeColor: Color, strokeWidth: Float
    ) {
        val path = Path().apply {
            moveTo(x + 10f, y + 16f)
            lineTo(x + w - 10f, y + 4f)
            lineTo(x + w - 10f, y + h - 4f)
            lineTo(x + 10f, y + h - 16f)
            close()
        }
        scope.drawPath(path, LabCardBg, style = Fill)
        scope.drawPath(path, strokeColor, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    private fun drawIcBlock(
        scope: DrawScope,
        x: Float, y: Float, w: Float, h: Float,
        def: ComponentDefinition, comp: CircuitComponent,
        strokeColor: Color, strokeWidth: Float
    ) {
        // IC body with alignment notch at top
        val bodyRect = Rect(x + 8f, y + 4f, x + w - 8f, y + h - 4f)
        scope.drawRoundRect(
            color = LabCardBg,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
        )
        scope.drawRoundRect(
            color = strokeColor,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = Size(bodyRect.width, bodyRect.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            style = Stroke(strokeWidth)
        )

        // Top notch arc
        val notchR = 7f
        val notchX = x + w / 2
        scope.drawArc(
            color = strokeColor,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(notchX - notchR, y + 4f - notchR / 2),
            size = Size(notchR * 2, notchR * 2),
            style = Stroke(strokeWidth)
        )
    }
}
