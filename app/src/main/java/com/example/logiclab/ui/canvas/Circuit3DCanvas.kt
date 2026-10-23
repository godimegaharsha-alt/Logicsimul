package com.example.logiclab.ui.canvas

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.audio.AudioEngine
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.Board3DStyle
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalFlowHigh
import com.example.ui.theme.SignalFlowLow
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalUnknown
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class TouchMode3D(val label: String) {
    ORBIT("Orbit 3D"),
    PAN("Pan View")
}

enum class CameraPreset3D(val label: String, val yaw: Float, val pitch: Float) {
    ISO("ISO", -35f, 35f),
    TOP("TOP 2.5D", 0f, 85f),
    FRONT("FRONT", 0f, 15f),
    RIGHT("RIGHT", 90f, 18f),
    BACK("BACK", 180f, 18f),
    LEFT("LEFT", -90f, 18f)
}

/**
 * High-performance, pure Jetpack Compose 3D Isometric Circuit Engine.
 * Features detailed 3D camera controls:
 * - Touch mode toggle (1-finger Orbit vs 1-finger Pan)
 * - Directional 3D navigation D-Pad / Compass
 * - Camera angle presets (ISO, TOP, FRONT, RIGHT, BACK, LEFT)
 * - Cinematic 360° Turntable auto-rotate with speed & direction controls
 * - Visual Signal Flow mode with vivid Green (1) and Red (0) elevated wire glow
 * - Comprehensive Detailed 3D Controls Sheet with sliders for Yaw, Pitch, Zoom, Wire Standoff
 * - PCB Silkscreen grid and layer visibility toggles
 * - Live real-time telemetry (angles, zoom percentage, chip count)
 */
/**
 * Authentic Daier ASW-23-101 Metal Low Voltage Toggle Switch 3D Renderer.
 * Accurately models:
 * - Molded dark phenolic/bakelite lower box chassis with side screw terminal lugs
 * - Stamped zinc/nickel top mounting strap with folded ear wings
 * - Stamped nickel-plated keyed ON/OFF indexing nameplate with alignment notch
 * - M12 threaded cylindrical collar bushing with horizontal thread ridges
 * - 6-sided chrome hexagonal mounting lock nut
 * - Polished solid cylindrical chrome bat lever with rounded hemispherical dome bat tip
 * - Dynamic tilt physics between ON (-Y / UP) and OFF (+Y / DOWN)
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawASW23101ToggleSwitch(
    cx: Float,
    cy: Float,
    isOn: Boolean,
    scale: Float = 1.0f,
    showChassisBox: Boolean = false,
    project3D: (Float, Float, Float) -> Offset
) {
    val z0 = 0.5f

    // 1. Lower Chassis: Molded Dark Phenolic Bakelite Box with M3 Screw Terminals (for Breadboard / PCB)
    if (showChassisBox) {
        val bw = 26f * scale
        val bh = 17f * scale
        val bz = 9.5f * scale

        // Bakelite box corners
        val b0 = project3D(cx - bw / 2f, cy - bh / 2f, z0)
        val b1 = project3D(cx + bw / 2f, cy - bh / 2f, z0)
        val b2 = project3D(cx + bw / 2f, cy + bh / 2f, z0)
        val b3 = project3D(cx - bw / 2f, cy + bh / 2f, z0)

        val t0 = project3D(cx - bw / 2f, cy - bh / 2f, bz)
        val t1 = project3D(cx + bw / 2f, cy - bh / 2f, bz)
        val t2 = project3D(cx + bw / 2f, cy + bh / 2f, bz)
        val t3 = project3D(cx - bw / 2f, cy + bh / 2f, bz)

        // Drop shadow under bakelite box
        val shadowPath = Path().apply {
            moveTo(b0.x + 4f * scale, b0.y + 4f * scale)
            lineTo(b1.x + 4f * scale, b1.y + 4f * scale)
            lineTo(b2.x + 4f * scale, b2.y + 4f * scale)
            lineTo(b3.x + 4f * scale, b3.y + 4f * scale)
            close()
        }
        drawPath(path = shadowPath, color = Color.Black.copy(alpha = 0.45f))

        // Side & Front Phenolic Box Faces (Molded Dark Charcoal Bakelite #14171A, #1C2024)
        val frontFace = Path().apply {
            moveTo(b3.x, b3.y)
            lineTo(b2.x, b2.y)
            lineTo(t2.x, t2.y)
            lineTo(t3.x, t3.y)
            close()
        }
        drawPath(path = frontFace, color = Color(0xFF14171A))

        val rightFace = Path().apply {
            moveTo(b1.x, b1.y)
            lineTo(b2.x, b2.y)
            lineTo(t2.x, t2.y)
            lineTo(t1.x, t1.y)
            close()
        }
        drawPath(path = rightFace, color = Color(0xFF1C2024))

        // Stamped Zinc/Nickel Top Bracket Plate (#B0BEC5 with folded side tabs)
        val topPlate = Path().apply {
            moveTo(t0.x, t0.y)
            lineTo(t1.x, t1.y)
            lineTo(t2.x, t2.y)
            lineTo(t3.x, t3.y)
            close()
        }
        drawPath(path = topPlate, color = Color(0xFFB0BEC5))
        drawPath(path = topPlate, color = Color(0xFFECEFF1), style = Stroke(0.8f * scale))

        // Stamped zinc side bracket wings
        val wingL0 = project3D(cx - bw / 2f, cy - 4.5f * scale, bz)
        val wingL1 = project3D(cx - bw / 2f - 3.5f * scale, cy - 4.5f * scale, bz - 3.5f * scale)
        val wingL2 = project3D(cx - bw / 2f - 3.5f * scale, cy + 4.5f * scale, bz - 3.5f * scale)
        val wingL3 = project3D(cx - bw / 2f, cy + 4.5f * scale, bz)
        drawPath(Path().apply {
            moveTo(wingL0.x, wingL0.y)
            lineTo(wingL1.x, wingL1.y)
            lineTo(wingL2.x, wingL2.y)
            lineTo(wingL3.x, wingL3.y)
            close()
        }, color = Color(0xFF90A4AE))

        // Side Screw Terminal Posts (ASW-23-101 M3 Screw Terminals)
        val screwL = project3D(cx - bw / 2f - 3f * scale, cy + 3.5f * scale, 3.5f * scale)
        val screwR = project3D(cx + bw / 2f + 3f * scale, cy + 3.5f * scale, 3.5f * scale)

        // Screw Lug Threads & Heads
        listOf(screwL, screwR).forEach { sc ->
            drawCircle(color = Color(0xFF78909C), radius = 4.5f * scale, center = sc)
            drawCircle(color = Color(0xFFECEFF1), radius = 3.6f * scale, center = sc)
            // Phillips Cross Slot
            drawLine(color = Color(0xFF37474F), start = Offset(sc.x - 2.4f * scale, sc.y), end = Offset(sc.x + 2.4f * scale, sc.y), strokeWidth = 1.1f * scale)
            drawLine(color = Color(0xFF37474F), start = Offset(sc.x, sc.y - 2.4f * scale), end = Offset(sc.x, sc.y + 2.4f * scale), strokeWidth = 1.1f * scale)
        }
    }

    val baseZ = if (showChassisBox) 10f * scale else 1.8f
    val swCenter = project3D(cx, cy, baseZ)

    // 2. Keyed Metal Indexing Nameplate (Stamped nickel plate with notch & "ON"/"OFF" tabs)
    val plateW = 15f * scale
    val plateH = 22f * scale
    val np0 = project3D(cx - plateW / 2f, cy - plateH / 2f, baseZ + 0.2f)
    val np1 = project3D(cx + plateW / 2f, cy - plateH / 2f, baseZ + 0.2f)
    val np2 = project3D(cx + plateW / 2f, cy + plateH / 2f, baseZ + 0.2f)
    val np3 = project3D(cx - plateW / 2f, cy + plateH / 2f, baseZ + 0.2f)

    val nameplatePath = Path().apply {
        moveTo(np0.x, np0.y)
        lineTo(np1.x, np1.y)
        lineTo(np2.x, np2.y)
        lineTo(np3.x, np3.y)
        close()
    }
    drawPath(path = nameplatePath, color = Color(0xFFCFD8DC))
    drawPath(path = nameplatePath, color = Color(0xFFFFFFFF).copy(alpha = 0.85f), style = Stroke(0.8f * scale))

    // Keyed alignment notch at the top
    val notchTip = project3D(cx, cy - plateH / 2f - 2.2f * scale, baseZ + 0.2f)
    val notchL = project3D(cx - 2.8f * scale, cy - plateH / 2f, baseZ + 0.2f)
    val notchR = project3D(cx + 2.8f * scale, cy - plateH / 2f, baseZ + 0.2f)
    drawPath(Path().apply {
        moveTo(notchL.x, notchL.y)
        lineTo(notchTip.x, notchTip.y)
        lineTo(notchR.x, notchR.y)
        close()
    }, color = Color(0xFFB0BEC5))

    // Stamped ON / OFF lettering on the indexing plate
    val onPos = project3D(cx, cy - plateH * 0.32f, baseZ + 0.3f)
    val offPos = project3D(cx, cy + plateH * 0.38f, baseZ + 0.3f)
    val plateFontPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.rgb(55, 71, 79)
        textSize = 6.8f * scale
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText("ON", onPos.x, onPos.y, plateFontPaint)
    drawContext.canvas.nativeCanvas.drawText("OFF", offPos.x, offPos.y, plateFontPaint)

    // 3. Threaded Cylindrical Bushing Collar (M12 Threaded Neck with machine ridges)
    val collarRadius = 6.5f * scale
    drawCircle(color = Color(0xFF455A64), radius = collarRadius + 1.2f * scale, center = swCenter)
    drawCircle(color = Color(0xFF90A4AE), radius = collarRadius, center = swCenter)
    // Horizontal machine thread grooves
    drawCircle(color = Color(0xFFECEFF1), radius = collarRadius * 0.84f, center = swCenter, style = Stroke(1.0f * scale))
    drawCircle(color = Color(0xFF607D8B), radius = collarRadius * 0.68f, center = swCenter, style = Stroke(0.8f * scale))

    // 4. Hexagonal Chrome Locking Nut (6-sided faceted nut with 3D extrusion thickness)
    val hexRadius = 9.0f * scale
    val hexNutBottom = baseZ + 0.3f * scale
    val hexNutTop = baseZ + 1.8f * scale

    val hexBottomPoints = (0 until 6).map { i ->
        val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
        project3D(cx + hexRadius * kotlin.math.cos(angle), cy + hexRadius * kotlin.math.sin(angle), hexNutBottom)
    }
    val hexTopPoints = (0 until 6).map { i ->
        val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
        project3D(cx + hexRadius * kotlin.math.cos(angle), cy + hexRadius * kotlin.math.sin(angle), hexNutTop)
    }

    // 3D Hex side faces
    for (i in 0 until 6) {
        val next = (i + 1) % 6
        val sideFace = Path().apply {
            moveTo(hexBottomPoints[i].x, hexBottomPoints[i].y)
            lineTo(hexBottomPoints[next].x, hexBottomPoints[next].y)
            lineTo(hexTopPoints[next].x, hexTopPoints[next].y)
            lineTo(hexTopPoints[i].x, hexTopPoints[i].y)
            close()
        }
        val sideColor = if (i in 1..3) Color(0xFF78909C) else Color(0xFFB0BEC5)
        drawPath(path = sideFace, color = sideColor)
    }

    // Top face of hex nut
    val hexTopPath = Path().apply {
        moveTo(hexTopPoints[0].x, hexTopPoints[0].y)
        for (i in 1 until 6) lineTo(hexTopPoints[i].x, hexTopPoints[i].y)
        close()
    }
    drawPath(path = hexTopPath, color = Color(0xFFECEFF1))
    drawPath(path = hexTopPath, color = Color(0xFF78909C), style = Stroke(1.2f * scale))
    // Nut central bushing hole
    drawCircle(color = Color(0xFF263238), radius = 4.5f * scale, center = project3D(cx, cy, hexNutTop))

    // 5. Solid Volumetric Polished Chrome Bat Lever (Tapered Teardrop Metallic Mesh)
    // In ASW-23-101: UP is ON (-tiltY), DOWN is OFF (+tiltY)
    val tiltY = if (isOn) -10f * scale else 10f * scale
    val leverHeightZ = baseZ + 24f * scale
    val leverTip = project3D(cx, cy + tiltY, leverHeightZ)
    val leverBase = project3D(cx, cy, baseZ + 2.0f * scale)

    // Shadow cast by the tilting bat lever
    val shadowTip = project3D(cx + 5f * scale, cy + tiltY + 5f * scale, baseZ)
    drawLine(
        color = Color.Black.copy(alpha = 0.38f),
        start = swCenter,
        end = shadowTip,
        strokeWidth = 5.5f * scale,
        cap = StrokeCap.Round
    )

    // Calculate perpendicular 2D screen normal for solid volumetric rendering
    val dx = leverTip.x - leverBase.x
    val dy = leverTip.y - leverBase.y
    val len = kotlin.math.hypot(dx, dy).coerceAtLeast(0.001f)
    val nx = -dy / len
    val ny = dx / len

    // Sculpted bat profile: base collar (rBase), neck (rNeck), bat bulb (rBulb), tip (rTip)
    val rBase = 3.2f * scale
    val rNeck = 2.6f * scale
    val rBulb = 5.2f * scale
    val rTip = 4.2f * scale

    // Interpolated control positions along lever axis
    val pNeck = Offset(leverBase.x + dx * 0.28f, leverBase.y + dy * 0.28f)
    val pBulb = Offset(leverBase.x + dx * 0.78f, leverBase.y + dy * 0.78f)

    // Facet 1: Left Dark Metallic Shadow Body (Dark Chrome #37474F)
    val leftFacet = Path().apply {
        moveTo(leverBase.x - nx * rBase, leverBase.y - ny * rBase)
        lineTo(pNeck.x - nx * rNeck, pNeck.y - ny * rNeck)
        lineTo(pBulb.x - nx * rBulb, pBulb.y - ny * rBulb)
        lineTo(leverTip.x - nx * rTip, leverTip.y - ny * rTip)
        lineTo(leverTip.x, leverTip.y)
        lineTo(pBulb.x, pBulb.y)
        lineTo(pNeck.x, pNeck.y)
        lineTo(leverBase.x, leverBase.y)
        close()
    }
    drawPath(path = leftFacet, color = Color(0xFF37474F))

    // Facet 2: Right Polished Chrome Body (Chrome #ECEFF1)
    val rightFacet = Path().apply {
        moveTo(leverBase.x, leverBase.y)
        lineTo(pNeck.x, pNeck.y)
        lineTo(pBulb.x, pBulb.y)
        lineTo(leverTip.x, leverTip.y)
        lineTo(leverTip.x + nx * rTip, leverTip.y + ny * rTip)
        lineTo(pBulb.x + nx * rBulb, pBulb.y + ny * rBulb)
        lineTo(pNeck.x + nx * rNeck, pNeck.y + ny * rNeck)
        lineTo(leverBase.x + nx * rBase, leverBase.y + ny * rBase)
        close()
    }
    drawPath(path = rightFacet, color = Color(0xFFCFD8DC))

    // Facet 3: Bright Center Specular Highlight Strip (#FFFFFF)
    val highlightStrip = Path().apply {
        moveTo(leverBase.x + nx * 0.4f * scale, leverBase.y + ny * 0.4f * scale)
        lineTo(pNeck.x + nx * 0.5f * scale, pNeck.y + ny * 0.5f * scale)
        lineTo(pBulb.x + nx * 0.8f * scale, pBulb.y + ny * 0.8f * scale)
        lineTo(leverTip.x + nx * 0.6f * scale, leverTip.y + ny * 0.6f * scale)
        lineTo(leverTip.x + nx * 1.8f * scale, leverTip.y + ny * 1.8f * scale)
        lineTo(pBulb.x + nx * 2.2f * scale, pBulb.y + ny * 2.2f * scale)
        lineTo(pNeck.x + nx * 1.4f * scale, pNeck.y + ny * 1.4f * scale)
        lineTo(leverBase.x + nx * 1.2f * scale, leverBase.y + ny * 1.2f * scale)
        close()
    }
    drawPath(path = highlightStrip, color = Color.White.copy(alpha = 0.90f))

    // 6. Volumetric Spherical Bat Lever Cap at the Tip
    drawCircle(color = Color(0xFF263238), radius = rTip + 0.6f * scale, center = leverTip)
    drawCircle(color = Color(0xFFB0BEC5), radius = rTip, center = leverTip)
    drawCircle(color = Color(0xFFECEFF1), radius = rTip * 0.82f, center = Offset(leverTip.x + nx * 0.8f * scale, leverTip.y + ny * 0.8f * scale))
    drawCircle(color = Color.White, radius = 2.2f * scale, center = Offset(leverTip.x + nx * 1.4f * scale - 1f, leverTip.y + ny * 1.4f * scale - 1f))

    // 7. Active State Silkscreen Indicator Glow
    if (isOn) {
        drawCircle(
            color = Color(0xFF00E676).copy(alpha = 0.65f),
            radius = hexRadius + 3.5f * scale,
            center = swCenter,
            style = Stroke(1.6f * scale)
        )
    }
}

/**
 * Authentic 3D Through-Hole 5mm LED Diode based on physical reference photography:
 * - Dual tin/silver rectangular wire leads with stamped leadframe shoulder crimps.
 * - Circular base flange collar (lip ring) with cathode flat indexing notch.
 * - Transparent/translucent optical epoxy barrel cylinder with lateral refraction.
 * - Visible internal leadframe:
 *     * Large trapezoidal reflective metal ANVIL on the cathode side
 *     * Deep conical DIE CUP inside the anvil
 *     * Microscopic semiconductor LED die chip seated in the cup
 *     * Slender vertical POST on the anode side
 *     * Arched microscopic gold BOND WIRE bridging from post to die
 * - Hemispherical optical dome lens top with dual glass specular highlights.
 * - Photonic emission states:
 *     * OFF: Crystal clear transparent tinted epoxy with visible leadframe.
 *     * ON: Blinding core die luminescence, internal diffusion, and multi-tier radial photonic bloom aura.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DThroughHoleLEDDiode(
    cx: Float,
    cy: Float,
    isLit: Boolean,
    colorTint: Color = Color(0xFFFF1744), // Brilliant Red by default, or Blue/Green
    scale: Float = 1.0f,
    baseZ: Float = 0.5f,
    showLeads: Boolean = true,
    label: String = "LED",
    project3D: (Float, Float, Float) -> Offset
) {
    // 1. Through-Hole Metal Wire Leads descending directly into PCB solder pads / breadboard tie points
    if (showLeads) {
        val cathodeX = cx - 4.2f * scale
        val anodeX = cx + 4.2f * scale

        // Solder Annular Pads on Board
        val padCathode = project3D(cathodeX, cy, 0f)
        val padAnode = project3D(anodeX, cy, 0f)
        listOf(padCathode, padAnode).forEach { p ->
            drawCircle(color = Color(0xFFD4AF37), radius = 3.8f * scale, center = p)
            drawCircle(color = Color(0xFFECEFF1), radius = 3.0f * scale, center = p)
            drawCircle(color = Color(0xFF1E2124), radius = 1.6f * scale, center = p)
        }

        // Stamped Shoulder Crimps (Kinks in through-hole leadframes)
        val leadC0 = project3D(cathodeX, cy, 0f)
        val leadC1 = project3D(cathodeX, cy, 2.5f * scale)
        val leadC2 = project3D(cathodeX - 1.4f * scale, cy, 3.8f * scale)
        val leadC3 = project3D(cathodeX, cy, baseZ + 5.0f * scale)

        val leadA0 = project3D(anodeX, cy, 0f)
        val leadA1 = project3D(anodeX, cy, 2.5f * scale)
        val leadA2 = project3D(anodeX + 1.4f * scale, cy, 3.8f * scale)
        val leadA3 = project3D(anodeX, cy, baseZ + 5.0f * scale)

        // Cathode Lead Path
        val cLeadPath = Path().apply {
            moveTo(leadC0.x, leadC0.y)
            lineTo(leadC1.x, leadC1.y)
            lineTo(leadC2.x, leadC2.y)
            lineTo(leadC3.x, leadC3.y)
        }
        drawPath(path = cLeadPath, color = Color(0xFF546E7A), style = Stroke(2.8f * scale, cap = StrokeCap.Round))
        drawPath(path = cLeadPath, color = Color(0xFFCFD8DC), style = Stroke(1.8f * scale, cap = StrokeCap.Round))

        // Anode Lead Path
        val aLeadPath = Path().apply {
            moveTo(leadA0.x, leadA0.y)
            lineTo(leadA1.x, leadA1.y)
            lineTo(leadA2.x, leadA2.y)
            lineTo(leadA3.x, leadA3.y)
        }
        drawPath(path = aLeadPath, color = Color(0xFF546E7A), style = Stroke(2.8f * scale, cap = StrokeCap.Round))
        drawPath(path = aLeadPath, color = Color(0xFFCFD8DC), style = Stroke(1.8f * scale, cap = StrokeCap.Round))
    }

    // 2. Under-Diode Ambient Ground Glow Pool on Substrate
    val gndCenter = project3D(cx, cy, 0.2f)
    if (isLit) {
        drawCircle(color = colorTint.copy(alpha = 0.25f), radius = 34f * scale, center = gndCenter)
        drawCircle(color = colorTint.copy(alpha = 0.60f), radius = 17f * scale, center = gndCenter)
    }

    // 3. LED Base Flange Collar (Lip Ring with Cathode Flat Index Notch)
    val flangeZ = baseZ + 5.2f * scale
    val flangeCenter = project3D(cx, cy, flangeZ)
    val flangeRadius = 10.5f * scale

    // Flange Body with flat notch on left (cathode) side
    val flangePath = Path()
    val numFlangeSegments = 24
    var firstPoint = true
    for (i in 0 until numFlangeSegments) {
        val angle = (i * 360.0 / numFlangeSegments * Math.PI / 180.0).toFloat()
        var fx = cx + flangeRadius * kotlin.math.cos(angle)
        val fy = cy + flangeRadius * kotlin.math.sin(angle)
        // Flat chord on cathode side (x < cx - flangeRadius * 0.72f)
        if (fx < cx - flangeRadius * 0.72f) {
            fx = cx - flangeRadius * 0.72f
        }
        val fp = project3D(fx, fy, flangeZ)
        if (firstPoint) {
            flangePath.moveTo(fp.x, fp.y)
            firstPoint = false
        } else {
            flangePath.lineTo(fp.x, fp.y)
        }
    }
    flangePath.close()

    val flangeColor = if (isLit) colorTint.copy(alpha = 0.88f) else Color(0xFF4A0810)
    drawPath(path = flangePath, color = flangeColor)
    drawPath(path = flangePath, color = Color(0xFF263238), style = Stroke(1.2f * scale))

    // 4. Cylindrical Transparent Epoxy Barrel Body
    val barrelZ = baseZ + 12f * scale
    val barrelCenter = project3D(cx, cy, barrelZ)
    val barrelRadius = 8.8f * scale
    val barrelColor = if (isLit) colorTint.copy(alpha = 0.95f) else Color(0xFF40050B)
    drawCircle(color = barrelColor, radius = barrelRadius, center = barrelCenter)
    drawCircle(color = Color(0xFF1E2124).copy(alpha = 0.6f), radius = barrelRadius, center = barrelCenter, style = Stroke(1.1f * scale))

    // 5. VISIBLE INTERNAL LEADFRAME (Inside Transparent Epoxy Resin)
    val leadframeZ = baseZ + 11.5f * scale
    val anvilStem = project3D(cx - 3.2f * scale, cy, leadframeZ - 3.5f * scale)
    val anvilTop = project3D(cx - 1.2f * scale, cy, leadframeZ + 3.0f * scale)
    val anvilLeft = project3D(cx - 4.5f * scale, cy, leadframeZ + 2.5f * scale)
    val anvilRight = project3D(cx + 0.6f * scale, cy, leadframeZ + 2.5f * scale)

    // Stamped Metal Anvil Flag (Cathode)
    val anvilPath = Path().apply {
        moveTo(anvilStem.x, anvilStem.y)
        lineTo(anvilLeft.x, anvilLeft.y)
        lineTo(anvilTop.x, anvilTop.y)
        lineTo(anvilRight.x, anvilRight.y)
        close()
    }
    drawPath(path = anvilPath, color = Color(0xFF90A4AE))
    drawPath(path = anvilPath, color = Color(0xFFECEFF1), style = Stroke(0.8f * scale))

    // Reflective Die Cup Cavity inside the Anvil
    val dieCupCenter = project3D(cx - 1.2f * scale, cy, leadframeZ + 2.0f * scale)
    drawCircle(color = Color(0xFF546E7A), radius = 2.4f * scale, center = dieCupCenter)
    drawCircle(color = Color(0xFFCFD8DC), radius = 2.0f * scale, center = dieCupCenter, style = Stroke(0.6f * scale))

    // Semiconductor LED Silicon Die Chip
    val dieChipColor = if (isLit) Color.White else Color(0xFF263238)
    drawCircle(color = dieChipColor, radius = 1.4f * scale, center = dieCupCenter)

    // Anode Post (Vertical Rod on Right)
    val postBottom = project3D(cx + 3.2f * scale, cy, leadframeZ - 3.5f * scale)
    val postTop = project3D(cx + 3.2f * scale, cy, leadframeZ + 2.8f * scale)
    drawLine(color = Color(0xFFB0BEC5), start = postBottom, end = postTop, strokeWidth = 1.8f * scale, cap = StrokeCap.Round)

    // Arched Microscopic Gold Bond Wire (from Post to Die)
    val wireApex = project3D(cx + 1.0f * scale, cy, leadframeZ + 4.2f * scale)
    val bondWirePath = Path().apply {
        moveTo(postTop.x, postTop.y)
        quadraticBezierTo(wireApex.x, wireApex.y, dieCupCenter.x, dieCupCenter.y)
    }
    drawPath(path = bondWirePath, color = Color(0xFFFFD54F), style = Stroke(0.9f * scale))

    // 6. Hemispherical Optical Dome Top
    val domeZ = baseZ + 19.5f * scale
    val domeCenter = project3D(cx, cy, domeZ)
    val domeRadius = 9.4f * scale

    if (isLit) {
        // Multi-tier Photonic Radiant Bloom Aura
        drawCircle(color = colorTint.copy(alpha = 0.22f), radius = 36f * scale, center = domeCenter)
        drawCircle(color = colorTint.copy(alpha = 0.55f), radius = 22f * scale, center = domeCenter)
        drawCircle(color = colorTint, radius = domeRadius, center = domeCenter)
        // Incandescent Core Luminescence
        drawCircle(color = Color(0xFFFFCDD2), radius = 4.8f * scale, center = domeCenter)
        drawCircle(color = Color.White, radius = 2.6f * scale, center = domeCenter)

        // Primary Specular Reflection Crescent Arc
        drawArc(
            color = Color.White.copy(alpha = 0.95f),
            startAngle = -140f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(domeCenter.x - domeRadius * 0.72f, domeCenter.y - domeRadius * 0.72f),
            size = androidx.compose.ui.geometry.Size(domeRadius * 1.44f, domeRadius * 1.44f),
            style = Stroke(2.2f * scale, cap = StrokeCap.Round)
        )
    } else {
        // Dark Translucent Ruby Epoxy Body
        drawCircle(color = Color(0xFF4C060E), radius = domeRadius, center = domeCenter)

        // Primary Specular Reflection Highlight Arc
        drawArc(
            color = Color.White.copy(alpha = 0.65f),
            startAngle = -140f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(domeCenter.x - domeRadius * 0.72f, domeCenter.y - domeRadius * 0.72f),
            size = androidx.compose.ui.geometry.Size(domeRadius * 1.44f, domeRadius * 1.44f),
            style = Stroke(1.6f * scale, cap = StrokeCap.Round)
        )
        // Secondary Subtle Rim Glint
        drawArc(
            color = Color.White.copy(alpha = 0.25f),
            startAngle = 40f,
            sweepAngle = 50f,
            useCenter = false,
            topLeft = Offset(domeCenter.x - domeRadius * 0.75f, domeCenter.y - domeRadius * 0.75f),
            size = androidx.compose.ui.geometry.Size(domeRadius * 1.5f, domeRadius * 1.5f),
            style = Stroke(1.0f * scale, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun Circuit3DCanvas(
    components: List<CircuitComponent>,
    wires: List<CircuitWire>,
    selectedComponentId: String? = null,
    isSimulating: Boolean,
    onToggleSimulate: () -> Unit,
    onStepSimulate: () -> Unit,
    onResetSimulation: () -> Unit,
    onComponentSelect: (String?) -> Unit,
    onComponentToggleSwitch: (String) -> Unit,
    onComponentDoubleClick: ((String) -> Unit)? = null,
    onSwitchTo2D: () -> Unit,
    isSignalFlowMode: Boolean = true,
    onToggleSignalFlowMode: (() -> Unit)? = null,
    board3DStyle: Board3DStyle = Board3DStyle.PCB_LAB,
    onToggleBoard3DStyle: (() -> Unit)? = null,
    is3DMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Camera Viewport Angles & Transformations
    var yawDeg by remember { mutableFloatStateOf(-35f) }
    var pitchDeg by remember { mutableFloatStateOf(35f) }
    var zoomScale by remember { mutableFloatStateOf(1.05f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Detailed 3D Control States
    var touchMode by remember { mutableStateOf(TouchMode3D.ORBIT) }
    var isAutoRotating by remember { mutableStateOf(false) }
    var autoRotateSpeed by remember { mutableFloatStateOf(0.4f) }
    var autoRotateClockwise by remember { mutableStateOf(true) }
    var showPcbGrid by remember { mutableStateOf(true) }
    var showPcbSubstrate by remember { mutableStateOf(true) }
    var showDropShadows by remember { mutableStateOf(true) }
    var showChipBodies by remember { mutableStateOf(true) }
    var showPinLabels by remember { mutableStateOf(true) }
    var wireElevationLevel by remember { mutableFloatStateOf(10f) } // 4f, 10f, 18f
    var isDetailedControlsOpen by remember { mutableStateOf(false) }

    // 360° Cinematic Auto-Rotate Turntable
    LaunchedEffect(isAutoRotating, autoRotateSpeed, autoRotateClockwise) {
        if (isAutoRotating) {
            while (isActive) {
                withFrameMillis {
                    val delta = if (autoRotateClockwise) autoRotateSpeed else -autoRotateSpeed
                    yawDeg = (yawDeg + delta) % 360f
                }
            }
        }
    }

    // Double-tap tracking for 3D gates
    var lastTapTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var lastTappedCompId by remember { mutableStateOf<String?>(null) }
    var lastTapScreenPos by remember { mutableStateOf(Offset.Zero) }

    // Pulse wave cycle for wires and clock chips
    val infiniteTransition = rememberInfiniteTransition(label = "3d_pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_phase"
    )

    // Cached Text Paints
    val chipTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val gridTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(100, 100, 160, 140)
            textSize = 8f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
    }
    val trainerLabelPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(40, 52, 65)
            textSize = 8f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val trainerWhiteHeaderPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val trainerWhiteSubPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(220, 240, 230)
            textSize = 6.5f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val trainerPinNumPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(235, 245, 240)
            textSize = 5.2f
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val trainerChipPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(240, 240, 245)
            textSize = 6.2f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val trainerTitleLargePaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.LEFT
        }
    }
    val trainerModelPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }
    val trainerFooterPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 6.8f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val trainerSwitchLocalStates = remember { mutableStateMapOf<Int, Boolean>() }
    var isPulserPressed by remember { mutableStateOf(false) }
    var isTrainerPowerOn by remember { mutableStateOf(true) }

    val currentComponents by rememberUpdatedState(components)
    val currentWires by rememberUpdatedState(wires)
    val currentSelectedId by rememberUpdatedState(selectedComponentId)
    val currentOnSelect by rememberUpdatedState(onComponentSelect)
    val currentOnComponentDoubleClick by rememberUpdatedState(onComponentDoubleClick)
    val currentOnToggle by rememberUpdatedState(onComponentToggleSwitch)

    // Center of circuit board calculation
    val boardBounds = remember(components, board3DStyle) {
        if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) {
            val minX = if (components.isEmpty()) 40f else (components.minOf { it.x } - 60f).coerceAtMost(40f)
            val maxX = if (components.isEmpty()) 700f else (components.maxOf { it.x + 120f } + 60f).coerceAtLeast(700f)
            val minY = if (components.isEmpty()) 40f else (components.minOf { it.y } - 60f).coerceAtMost(40f)
            val maxY = if (components.isEmpty()) 620f else (components.maxOf { it.y + 100f } + 60f).coerceAtLeast(620f)
            Pair(Offset(minX, minY), Offset(maxX, maxY))
        } else {
            if (components.isEmpty()) {
                Pair(Offset(100f, 100f), Offset(600f, 500f))
            } else {
                val minX = components.minOf { it.x } - 80f
                val maxX = components.maxOf { it.x + 120f } + 80f
                val minY = components.minOf { it.y } - 80f
                val maxY = components.maxOf { it.y + 100f } + 80f
                Pair(Offset(minX, minY), Offset(maxX, maxY))
            }
        }
    }

    val boardCenter = Offset(
        (boardBounds.first.x + boardBounds.second.x) / 2f,
        (boardBounds.first.y + boardBounds.second.y) / 2f
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LabDarkBg)
            .testTag("circuit_3d_canvas")
    ) {
        // Main 3D Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(touchMode) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startScreenPos = down.position
                        var prevScreenPos = down.position
                        val startYaw = yawDeg
                        val startPitch = pitchDeg
                        val startPan = panOffset
                        val startZoom = zoomScale

                        var hasMovedPastSlop = false
                        val touchSlop = viewConfiguration.touchSlop
                        var isMultiTouch = false
                        var lastDistance = 0f
                        var lastCentroid = Offset.Zero

                        while (true) {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }

                            if (activePointers.isEmpty()) break

                            if (activePointers.size >= 2) {
                                isMultiTouch = true
                                val p0 = activePointers[0].position
                                val p1 = activePointers[1].position
                                val currentDistance = (p0 - p1).getDistance()
                                val currentCentroid = (p0 + p1) / 2f

                                if (lastDistance > 0f) {
                                    val pinchFactor = currentDistance / lastDistance
                                    zoomScale = (zoomScale * pinchFactor).coerceIn(0.35f, 3.5f)
                                }
                                if (lastCentroid != Offset.Zero) {
                                    val delta = currentCentroid - lastCentroid
                                    panOffset += delta
                                }

                                lastDistance = currentDistance
                                lastCentroid = currentCentroid
                                event.changes.forEach { it.consume() }
                            } else if (!isMultiTouch && activePointers.size == 1) {
                                val pointer = activePointers[0]
                                val currentScreenPos = pointer.position
                                val totalDelta = currentScreenPos - startScreenPos

                                if (!hasMovedPastSlop && totalDelta.getDistance() > touchSlop) {
                                    hasMovedPastSlop = true
                                    // Disable auto-rotate when user manually interacts
                                    if (isAutoRotating) isAutoRotating = false
                                }

                                if (hasMovedPastSlop) {
                                    val frameDelta = currentScreenPos - prevScreenPos
                                    if (touchMode == TouchMode3D.ORBIT) {
                                        // 1-finger Orbit rotates 3D Azimuth & Elevation
                                        yawDeg = (yawDeg + frameDelta.x * 0.45f) % 360f
                                        pitchDeg = (pitchDeg - frameDelta.y * 0.35f).coerceIn(12f, 85f)
                                    } else {
                                        // 1-finger Pan translates viewport
                                        panOffset += frameDelta
                                    }
                                    pointer.consume()
                                }
                                prevScreenPos = currentScreenPos
                            }
                        }

                        // Touch Tap detection
                        if (!isMultiTouch && !hasMovedPastSlop) {
                            val screenCenter = Offset(size.width / 2f, size.height / 2f)
                            val yawRad = (yawDeg * PI / 180.0).toFloat()
                            val pitchRad = (pitchDeg * PI / 180.0).toFloat()
                            val cosY = cos(yawRad)
                            val sinY = sin(yawRad)
                            val sinP = sin(pitchRad)

                            var tappedCompId: String? = null
                            for (comp in currentComponents.asReversed()) {
                                val def = ComponentRegistry.get(comp.type)
                                val w = def?.width ?: 64f
                                val h = def?.height ?: 48f

                                val cX = comp.x + w / 2f
                                val cY = comp.y + h / 2f
                                val rX = (cX - boardCenter.x) * cosY - (cY - boardCenter.y) * sinY
                                val rY = (cX - boardCenter.x) * sinY + (cY - boardCenter.y) * cosY
                                val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                val projY = screenCenter.y + panOffset.y + (rY * sinP - 18f * cos(pitchRad)) * zoomScale

                                val tapDist = (startScreenPos - Offset(projX, projY)).getDistance()
                                val hitRadius = (max(w, h) * 0.75f * zoomScale).coerceIn(28f, 75f)

                                if (tapDist <= hitRadius) {
                                    tappedCompId = comp.id
                                    break
                                }
                            }

                            if (tappedCompId != null) {
                                val now = System.currentTimeMillis()
                                val isDoubleTap = (lastTappedCompId == tappedCompId || (startScreenPos - lastTapScreenPos).getDistance() < 60f) &&
                                        (now - lastTapTime < 450L)

                                if (isDoubleTap) {
                                    lastTapTime = 0L
                                    lastTappedCompId = null
                                    isDetailedControlsOpen = false
                                    currentOnSelect(tappedCompId)
                                    AudioEngine.playClickSound()
                                    currentOnComponentDoubleClick?.invoke(tappedCompId)
                                } else {
                                    lastTapTime = now
                                    lastTappedCompId = tappedCompId
                                    lastTapScreenPos = startScreenPos
                                    val comp = currentComponents.find { it.id == tappedCompId }
                                    if (comp?.type == "SWITCH") {
                                        currentOnToggle(tappedCompId)
                                    }
                                    currentOnSelect(tappedCompId)
                                    AudioEngine.playClickSound()
                                }
                            } else {
                                var handledTrainerTap = false
                                if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) {
                                    val bx0 = boardBounds.first.x
                                    val bx1 = boardBounds.second.x
                                    val by0 = boardBounds.first.y
                                    val by1 = boardBounds.second.y
                                    val bw = bx1 - bx0

                                    // 1. Check vertical logic input toggle switches on the left column (S1..S8)
                                    val swCount = 8
                                    val leftColX = bx0 + 36f
                                    val topSwY = by0 + 44f
                                    val botSwY = by1 - 65f
                                    val swSpacingY = (botSwY - topSwY) / (swCount - 1)
                                    val circuitSwitches = currentComponents.filter { it.type == "SWITCH" }

                                    for (i in 0 until swCount) {
                                        val sy = topSwY + i * swSpacingY
                                        val rX = (leftColX - boardCenter.x) * cosY - (sy - boardCenter.y) * sinY
                                        val rY = (leftColX - boardCenter.x) * sinY + (sy - boardCenter.y) * cosY
                                        val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                        val projY = screenCenter.y + panOffset.y + (rY * sinP - 8f * cos(pitchRad)) * zoomScale
                                        val dist = (startScreenPos - Offset(projX, projY)).getDistance()
                                        if (dist <= 26f * zoomScale.coerceIn(0.7f, 1.8f)) {
                                            val currentSt = trainerSwitchLocalStates[i] ?: (circuitSwitches.getOrNull(i)?.switchState ?: false)
                                            val newSt = !currentSt
                                            trainerSwitchLocalStates[i] = newSt
                                            val mapped = circuitSwitches.getOrNull(i)
                                            if (mapped != null) {
                                                currentOnToggle(mapped.id)
                                            }
                                            AudioEngine.playClickSound()
                                            handledTrainerTap = true
                                            break
                                        }
                                    }

                                    // 2. Check Power On Switch at bottom-left
                                    if (!handledTrainerTap) {
                                        val pwrX = bx0 + 36f
                                        val pwrY = by1 - 28f
                                        val rX = (pwrX - boardCenter.x) * cosY - (pwrY - boardCenter.y) * sinY
                                        val rY = (pwrX - boardCenter.x) * sinY + (pwrY - boardCenter.y) * cosY
                                        val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                        val projY = screenCenter.y + panOffset.y + (rY * sinP - 8f * cos(pitchRad)) * zoomScale
                                        val dist = (startScreenPos - Offset(projX, projY)).getDistance()
                                        if (dist <= 28f * zoomScale.coerceIn(0.7f, 1.8f)) {
                                            isTrainerPowerOn = !isTrainerPowerOn
                                            AudioEngine.playClickSound()
                                            handledTrainerTap = true
                                        }
                                    }

                                    // 3. Check 5 Gate IC Chips in center
                                    if (!handledTrainerTap) {
                                        val logicComps = currentComponents.filter { it.type != "SWITCH" && it.type != "LED" && it.type != "DIGITAL_PROBE" }
                                        val gateAreaX0 = bx0 + 64f
                                        val gateAreaX1 = bx1 - 64f
                                        val colW = (gateAreaX1 - gateAreaX0) / 5f
                                        val chipCenterY = by0 + (by1 - by0) * 0.45f
                                        for (cIdx in 0 until 5) {
                                            val cx = gateAreaX0 + cIdx * colW + colW / 2f
                                            val rX = (cx - boardCenter.x) * cosY - (chipCenterY - boardCenter.y) * sinY
                                            val rY = (cx - boardCenter.x) * sinY + (chipCenterY - boardCenter.y) * cosY
                                            val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                            val projY = screenCenter.y + panOffset.y + (rY * sinP - 10f * cos(pitchRad)) * zoomScale
                                            val dist = (startScreenPos - Offset(projX, projY)).getDistance()
                                            if (dist <= 36f * zoomScale.coerceIn(0.7f, 1.8f)) {
                                                val comp = logicComps.getOrNull(cIdx)
                                                if (comp != null) {
                                                    currentOnSelect(comp.id)
                                                    AudioEngine.playClickSound()
                                                    handledTrainerTap = true
                                                }
                                                break
                                            }
                                        }
                                    }
                                }
                                if (!handledTrainerTap) {
                                    currentOnSelect(null)
                                }
                            }
                        }
                    }
                }
        ) {
            val screenCenter = Offset(size.width / 2f, size.height / 2f)
            val yawRad = (yawDeg * PI / 180.0).toFloat()
            val pitchRad = (pitchDeg * PI / 180.0).toFloat()
            val cosY = cos(yawRad)
            val sinY = sin(yawRad)
            val sinP = sin(pitchRad)
            val cosP = cos(pitchRad)

            fun project3D(worldX: Float, worldY: Float, worldZ: Float = 0f): Offset {
                val dx = worldX - boardCenter.x
                val dy = worldY - boardCenter.y
                val rx = dx * cosY - dy * sinY
                val ry = dx * sinY + dy * cosY
                val px = screenCenter.x + panOffset.x + rx * zoomScale
                val py = screenCenter.y + panOffset.y + (ry * sinP - worldZ * cosP) * zoomScale
                return Offset(px, py)
            }

            val bx0 = boardBounds.first.x
            val by0 = boardBounds.first.y
            val bx1 = boardBounds.second.x
            val by1 = boardBounds.second.y
            val boardW = bx1 - bx0
            val boardH = by1 - by0

            // 1. Render 3D PCB Substrate Slab
            if (showPcbSubstrate) {
                val boardThickness = 12f

                val b0 = project3D(bx0, by0, -boardThickness)
                val b1 = project3D(bx1, by0, -boardThickness)
                val b2 = project3D(bx1, by1, -boardThickness)
                val b3 = project3D(bx0, by1, -boardThickness)

                val t0 = project3D(bx0, by0, 0f)
                val t1 = project3D(bx1, by0, 0f)
                val t2 = project3D(bx1, by1, 0f)
                val t3 = project3D(bx0, by1, 0f)

                // PCB Drop Shadow
                if (showDropShadows) {
                    drawPath(
                        path = Path().apply {
                            moveTo(b0.x, b0.y + 14f)
                            lineTo(b1.x, b1.y + 14f)
                            lineTo(b2.x, b2.y + 14f)
                            lineTo(b3.x, b3.y + 14f)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.45f)
                    )
                }

            // Front Edge of PCB
            drawPath(
                path = Path().apply {
                    moveTo(t3.x, t3.y)
                    lineTo(t2.x, t2.y)
                    lineTo(b2.x, b2.y)
                    lineTo(b3.x, b3.y)
                    close()
                },
                color = Color(0xFF0F3025)
            )

            // Right Edge of PCB
            drawPath(
                path = Path().apply {
                    moveTo(t1.x, t1.y)
                    lineTo(t2.x, t2.y)
                    lineTo(b2.x, b2.y)
                    lineTo(b1.x, b1.y)
                    close()
                },
                color = Color(0xFF0A221A)
            )

            if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) {
                // =========================================================================================
                // AUTHENTIC PHY-41 LOGIC GATES TRAINER APPARATUS (FAITHFUL TO USER REFERENCE PHOTO)
                // =========================================================================================
                val bw = bx1 - bx0
                val bh = by1 - by0
                val chassisMargin = 22f
                val cx0 = bx0 - chassisMargin
                val cy0 = by0 - chassisMargin
                val cx1 = bx1 + chassisMargin
                val cy1 = by1 + chassisMargin

                // 1. Industrial Powder-Coated Metal Console Chassis (Silver / Grey Steel Enclosure)
                val cb0 = project3D(cx0, cy0, -20f)
                val cb1 = project3D(cx1, cy0, -20f)
                val cb2 = project3D(cx1, cy1, -20f)
                val cb3 = project3D(cx0, cy1, -20f)

                val ct0 = project3D(cx0, cy0, 0f)
                val ct1 = project3D(cx1, cy0, 0f)
                val ct2 = project3D(cx1, cy1, 0f)
                val ct3 = project3D(cx0, cy1, 0f)

                // Chassis Drop Shadow
                if (showDropShadows) {
                    drawPath(
                        path = Path().apply {
                            moveTo(cb0.x, cb0.y + 14f)
                            lineTo(cb1.x, cb1.y + 14f)
                            lineTo(cb2.x, cb2.y + 14f)
                            lineTo(cb3.x, cb3.y + 14f)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.52f)
                    )
                }

                // Front Slanted Metal Bevel Wall of Chassis
                drawPath(
                    path = Path().apply {
                        moveTo(ct3.x, ct3.y)
                        lineTo(ct2.x, ct2.y)
                        lineTo(cb2.x, cb2.y)
                        lineTo(cb3.x, cb3.y)
                        close()
                    },
                    color = Color(0xFF90A4AE) // Industrial powder-coated grey
                )
                drawPath(
                    path = Path().apply {
                        moveTo(ct3.x, ct3.y)
                        lineTo(ct2.x, ct2.y)
                        lineTo(cb2.x, cb2.y)
                        lineTo(cb3.x, cb3.y)
                        close()
                    },
                    color = Color(0xFF607D8B),
                    style = Stroke(1.5f)
                )

                // Right Side Metal Wall of Chassis
                drawPath(
                    path = Path().apply {
                        moveTo(ct1.x, ct1.y)
                        lineTo(ct2.x, ct2.y)
                        lineTo(cb2.x, cb2.y)
                        lineTo(cb1.x, cb1.y)
                        close()
                    },
                    color = Color(0xFF78909C) // Shadow side of metal chassis
                )

                // Top Metal Rim Framing the Green Faceplate
                drawPath(
                    path = Path().apply {
                        moveTo(ct0.x, ct0.y)
                        lineTo(ct1.x, ct1.y)
                        lineTo(ct2.x, ct2.y)
                        lineTo(ct3.x, ct3.y)
                        close()
                    },
                    color = Color(0xFFB0BEC5)
                )

                // 2. Main Industrial Emerald Green PCB Faceplate
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFF0D3B2E) // Authentic lab green soldermask
                )

                // Crisp White Outer Silkscreen Frame Border
                val pFrame0 = project3D(bx0 + 4f, by0 + 4f, 0.5f)
                val pFrame1 = project3D(bx1 - 4f, by0 + 4f, 0.5f)
                val pFrame2 = project3D(bx1 - 4f, by1 - 4f, 0.5f)
                val pFrame3 = project3D(bx0 + 4f, by1 - 4f, 0.5f)
                drawPath(
                    path = Path().apply {
                        moveTo(pFrame0.x, pFrame0.y)
                        lineTo(pFrame1.x, pFrame1.y)
                        lineTo(pFrame2.x, pFrame2.y)
                        lineTo(pFrame3.x, pFrame3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.95f),
                    style = Stroke(2f)
                )

                // 4 Corner Chrome Mounting Screws & Standoffs
                val screwPositions = listOf(
                    Offset(bx0 + 10f, by0 + 10f),
                    Offset(bx1 - 10f, by0 + 10f),
                    Offset(bx0 + 10f, by1 - 10f),
                    Offset(bx1 - 10f, by1 - 10f),
                    Offset(boardCenter.x, by0 + 8f),
                    Offset(boardCenter.x, by1 - 8f)
                )
                for (sc in screwPositions) {
                    val sProj = project3D(sc.x, sc.y, 1f)
                    drawCircle(color = Color(0xFFCFD8DC), radius = 4.4f, center = sProj)
                    drawCircle(color = Color(0xFF78909C), radius = 3.4f, center = sProj)
                    drawLine(color = Color(0xFF263238), start = Offset(sProj.x - 2.2f, sProj.y), end = Offset(sProj.x + 2.2f, sProj.y), strokeWidth = 1f)
                    drawLine(color = Color(0xFF263238), start = Offset(sProj.x, sProj.y - 2.2f), end = Offset(sProj.x, sProj.y + 2.2f), strokeWidth = 1f)
                }

                // =========================================================================================
                // AUTHENTIC SILKSCREEN BRANDING (FROM USER'S PHY-41 LAB TRAINER REFERENCE PHOTO)
                // =========================================================================================
                // Top-Left: "Polytech" logo and crest
                val polytechPos = project3D(bx0 + 26f, by0 + 18f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("Polytech", polytechPos.x, polytechPos.y, trainerTitleLargePaint)
                // Stylized wave crest next to brand
                val emblemCenter = project3D(bx0 + 15f, by0 + 14f, 0.8f)
                drawCircle(color = Color.White, radius = 5.2f, center = emblemCenter, style = Stroke(1.2f))
                drawLine(color = Color.White, start = Offset(emblemCenter.x - 3f, emblemCenter.y), end = Offset(emblemCenter.x + 3f, emblemCenter.y), strokeWidth = 1.2f)

                // Top-Right: "PHY-41"
                val phyPos = project3D(bx1 - 42f, by0 + 18f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("PHY-41", phyPos.x, phyPos.y, trainerModelPaint)

                // Bottom-Left: "LOGIC GATES TRAINER"
                val trainerTitlePos = project3D(bx0 + 84f, by1 - 8f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("LOGIC GATES TRAINER", trainerTitlePos.x, trainerTitlePos.y, trainerTitleLargePaint)

                // Bottom-Right: "HYDERABAD, PHONE:27537493,27547483"
                val phonePos = project3D(bx1 - 100f, by1 - 8f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("HYDERABAD, PHONE:27537493,27547483", phonePos.x, phonePos.y, trainerFooterPaint)

                // =========================================================================================
                // LEFT COLUMN: 8 VERTICAL INPUT TOGGLE SWITCHES + POWER ON CONTROL
                // =========================================================================================
                val inColW = 54f
                val inX0 = bx0 + 8f
                val inX1 = bx0 + inColW
                val inTopY = by0 + 26f
                val inBotY = by1 - 48f

                // Left Column Silkscreen Box
                val inBox0 = project3D(inX0, inTopY, 0.6f)
                val inBox1 = project3D(inX1, inTopY, 0.6f)
                val inBox2 = project3D(inX1, inBotY, 0.6f)
                val inBox3 = project3D(inX0, inBotY, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(inBox0.x, inBox0.y)
                        lineTo(inBox1.x, inBox1.y)
                        lineTo(inBox2.x, inBox2.y)
                        lineTo(inBox3.x, inBox3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(1.4f)
                )
                val inHeaderPos = project3D((inX0 + inX1) / 2f, inTopY + 10f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("INPUTS", inHeaderPos.x, inHeaderPos.y, trainerWhiteHeaderPaint)

                // 8 Vertical Metal Toggle Switches (1..8) with Turret Posts and 1/0 Silkscreen
                val numInSwitches = 8
                val swStepY = (inBotY - (inTopY + 16f)) / numInSwitches
                val circuitSwitches = currentComponents.filter { it.type == "SWITCH" }

                for (i in 0 until numInSwitches) {
                    val swY = inTopY + 20f + i * swStepY
                    val swX = inX0 + 20f
                    val sockX = inX0 + 40f
                    val swNum = "${i + 1}"

                    val mappedSw = circuitSwitches.getOrNull(i)
                    val isSwOn = trainerSwitchLocalStates[i] ?: (mappedSw?.switchState ?: false)

                    // Silkscreen "1" (top/UP) and "0" (bottom/DOWN)
                    val lbl1Pos = project3D(swX, swY - 8f, 0.8f)
                    val lbl0Pos = project3D(swX, swY + 9f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("1", lbl1Pos.x, lbl1Pos.y, trainerPinNumPaint)
                    drawContext.canvas.nativeCanvas.drawText("0", lbl0Pos.x, lbl0Pos.y, trainerPinNumPaint)

                    // Authentic Daier ASW-23-101 Metal Low Voltage Toggle Switch
                    drawASW23101ToggleSwitch(
                        cx = swX,
                        cy = swY,
                        isOn = isSwOn,
                        scale = 0.85f,
                        showChassisBox = false,
                        project3D = ::project3D
                    )

                    // Silver Turret Binding Post Socket for Patch Cord
                    val sockCenter = project3D(sockX, swY, 1.6f)
                    drawCircle(color = Color.White, radius = 4.6f, center = sockCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFCFD8DC), radius = 3.6f, center = sockCenter)
                    drawCircle(color = Color(0xFF1E2124), radius = 1.8f, center = sockCenter)

                    // Switch index label 1..8
                    val numPos = project3D(inX0 + 7f, swY + 2f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(swNum, numPos.x, numPos.y, trainerPinNumPaint)
                }

                // --- POWER SECTION AT BOTTOM LEFT ---
                val pwrBox0 = project3D(inX0, inBotY + 4f, 0.6f)
                val pwrBox1 = project3D(inX1, inBotY + 4f, 0.6f)
                val pwrBox2 = project3D(inX1, by1 - 8f, 0.6f)
                val pwrBox3 = project3D(inX0, by1 - 8f, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(pwrBox0.x, pwrBox0.y)
                        lineTo(pwrBox1.x, pwrBox1.y)
                        lineTo(pwrBox2.x, pwrBox2.y)
                        lineTo(pwrBox3.x, pwrBox3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(1.4f)
                )

                // POWER ON Toggle Switch & Illuminated Red Pilot Lamp
                val pwrSwX = inX0 + 16f
                val pwrLedX = inX0 + 38f
                val pwrY = (inBotY + 4f + by1 - 8f) / 2f + 4f

                // Authentic Daier ASW-23-101 Heavy-Duty Power Toggle Switch
                drawASW23101ToggleSwitch(
                    cx = pwrSwX,
                    cy = pwrY,
                    isOn = isTrainerPowerOn,
                    scale = 0.92f,
                    showChassisBox = false,
                    project3D = ::project3D
                )

                // Red Pilot Indicator Lamp (Authentic 3D Through-Hole Red LED Diode)
                draw3DThroughHoleLEDDiode(
                    cx = pwrLedX,
                    cy = pwrY,
                    isLit = isTrainerPowerOn,
                    scale = 0.88f,
                    baseZ = 2.0f,
                    showLeads = false,
                    label = "POWER",
                    project3D = ::project3D
                )

                val pwrLbl = project3D((inX0 + inX1) / 2f, inBotY + 12f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("POWER ON", pwrLbl.x, pwrLbl.y, trainerPinNumPaint)

                // =========================================================================================
                // RIGHT COLUMN: 8 OUTPUT RED CYLINDRICAL DOME LED DIODES
                // =========================================================================================
                val outColW = 54f
                val outX0 = bx1 - outColW
                val outX1 = bx1 - 8f
                val outTopY = by0 + 26f
                val outBotY = by1 - 10f

                // Right Column Silkscreen Box
                val outBox0 = project3D(outX0, outTopY, 0.6f)
                val outBox1 = project3D(outX1, outTopY, 0.6f)
                val outBox2 = project3D(outX1, outBotY, 0.6f)
                val outBox3 = project3D(outX0, outBotY, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(outBox0.x, outBox0.y)
                        lineTo(outBox1.x, outBox1.y)
                        lineTo(outBox2.x, outBox2.y)
                        lineTo(outBox3.x, outBox3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.9f),
                    style = Stroke(1.4f)
                )
                val outHeaderPos = project3D((outX0 + outX1) / 2f, outTopY + 10f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("OUTPUTS", outHeaderPos.x, outHeaderPos.y, trainerWhiteHeaderPaint)

                // 8 Red Cylindrical Dome LED Diodes + Turret Sockets (1..8)
                val numOutLeds = 8
                val ledStepY = (outBotY - (outTopY + 16f)) / numOutLeds
                val circuitOutputs = currentComponents.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }

                for (i in 0 until numOutLeds) {
                    val ledY = outTopY + 20f + i * ledStepY
                    val ledX = outX1 - 20f
                    val sockX = outX1 - 40f
                    val ledNum = "${i + 1}"

                    val mappedOut = circuitOutputs.getOrNull(i)
                    // The top LED (i == 0) is connected via red patch wire in the photo and is lit bright red!
                    val isLit = if (i == 0) {
                        isTrainerPowerOn
                    } else {
                        isTrainerPowerOn && mappedOut != null &&
                            ((mappedOut.inputValues["IN"] ?: mappedOut.outputValues["OUT"] ?: DigitalState.LOW) == DigitalState.HIGH)
                    }

                    // Silver Turret Banana Socket for Patch Cord
                    val sockCenter = project3D(sockX, ledY, 1.6f)
                    drawCircle(color = Color.White, radius = 4.6f, center = sockCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFCFD8DC), radius = 3.6f, center = sockCenter)
                    drawCircle(color = Color(0xFF1E2124), radius = 1.8f, center = sockCenter)

                    // Output Index Label 1..8
                    val numPos = project3D(outX1 - 7f, ledY + 2f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(ledNum, numPos.x, numPos.y, trainerPinNumPaint)

                    // Authentic 3D Through-Hole Red Cylindrical Dome LED Diode (Faithful to Reference Photo)
                    draw3DThroughHoleLEDDiode(
                        cx = ledX,
                        cy = ledY,
                        isLit = isLit,
                        scale = 0.88f,
                        baseZ = 1.6f,
                        showLeads = false,
                        label = ledNum,
                        project3D = ::project3D
                    )
                }

                // =========================================================================================
                // CENTER SECTION: 5 LOGIC GATE COLUMNS (AND, OR, NAND, NOR, EX OR) WITH 14-PIN DIP ICs
                // =========================================================================================
                val centerAreaX0 = bx0 + 62f
                val centerAreaX1 = bx1 - 62f
                val colCount = 5
                val colWidth = (centerAreaX1 - centerAreaX0) / colCount
                val gateAreaY0 = by0 + 26f
                val gateAreaY1 = by1 - 64f

                val gateColTitles = listOf("AND GATES", "OR GATES", "NAND GATES", "NOR GATES", "EX OR GATES")
                val gateIcPartNumbers = listOf("7408", "7432", "7400", "7402", "7486")
                val gateIcMfgLabels = listOf("MM74HC08N", "MM74HC32N", "TC74HC00P", "TC74HC02P", "SN74LS86N")

                for (cIdx in 0 until colCount) {
                    val colX0 = centerAreaX0 + cIdx * colWidth
                    val colX1 = colX0 + colWidth
                    val colMidX = (colX0 + colX1) / 2f

                    // White Silkscreen Column Boundary Box
                    val cBox0 = project3D(colX0 + 2f, gateAreaY0, 0.6f)
                    val cBox1 = project3D(colX1 - 2f, gateAreaY0, 0.6f)
                    val cBox2 = project3D(colX1 - 2f, gateAreaY1, 0.6f)
                    val cBox3 = project3D(colX0 + 2f, gateAreaY1, 0.6f)
                    drawPath(
                        path = Path().apply {
                            moveTo(cBox0.x, cBox0.y)
                            lineTo(cBox1.x, cBox1.y)
                            lineTo(cBox2.x, cBox2.y)
                            lineTo(cBox3.x, cBox3.y)
                            close()
                        },
                        color = Color.White.copy(alpha = 0.85f),
                        style = Stroke(1.2f)
                    )

                    // Column Header Title
                    val titlePos = project3D(colMidX, gateAreaY0 + 10f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(gateColTitles[cIdx], titlePos.x, titlePos.y, trainerWhiteHeaderPaint)

                    // 4 Logic Gates Schematic Symbols per Column (2 above the IC, 2 below the IC)
                    val numGatesInCol = 4
                    val gateSpacing = (gateAreaY1 - (gateAreaY0 + 18f)) / numGatesInCol

                    for (gIdx in 0 until numGatesInCol) {
                        val gy = gateAreaY0 + 24f + gIdx * gateSpacing
                        val symW = colWidth * 0.36f
                        val symH = 14f

                        // Inputs A and B Silver Turret Sockets on Left
                        val inSockA = project3D(colX0 + 7f, gy - 4f, 1.4f)
                        val inSockB = project3D(colX0 + 7f, gy + 4f, 1.4f)
                        drawCircle(color = Color.White, radius = 3.6f, center = inSockA, style = Stroke(0.6f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 2.8f, center = inSockA)
                        drawCircle(color = Color(0xFF1E2124), radius = 1.3f, center = inSockA)

                        drawCircle(color = Color.White, radius = 3.6f, center = inSockB, style = Stroke(0.6f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 2.8f, center = inSockB)
                        drawCircle(color = Color(0xFF1E2124), radius = 1.3f, center = inSockB)

                        // Output Y Silver Turret Socket on Right
                        val outSockY = project3D(colX1 - 7f, gy, 1.4f)
                        drawCircle(color = Color.White, radius = 3.6f, center = outSockY, style = Stroke(0.6f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 2.8f, center = outSockY)
                        drawCircle(color = Color(0xFF1E2124), radius = 1.3f, center = outSockY)

                        // Schematic Silkscreen Trace Lines into Gate Symbol
                        val symLeft = colMidX - symW / 2f
                        val symRight = colMidX + symW / 2f
                        val traceInA0 = project3D(colX0 + 11f, gy - 4f, 0.6f)
                        val traceInA1 = project3D(symLeft, gy - 4f, 0.6f)
                        val traceInB0 = project3D(colX0 + 11f, gy + 4f, 0.6f)
                        val traceInB1 = project3D(symLeft, gy + 4f, 0.6f)
                        val traceOut0 = project3D(symRight, gy, 0.6f)
                        val traceOut1 = project3D(colX1 - 11f, gy, 0.6f)

                        drawLine(color = Color.White.copy(alpha = 0.9f), start = traceInA0, end = traceInA1, strokeWidth = 1f)
                        drawLine(color = Color.White.copy(alpha = 0.9f), start = traceInB0, end = traceInB1, strokeWidth = 1f)
                        drawLine(color = Color.White.copy(alpha = 0.9f), start = traceOut0, end = traceOut1, strokeWidth = 1f)

                        // Draw Gate Schematic Symbol based on gate type
                        val pTopL = project3D(symLeft, gy - symH / 2f, 0.6f)
                        val pBotL = project3D(symLeft, gy + symH / 2f, 0.6f)
                        val pTip = project3D(symRight - if (cIdx == 2 || cIdx == 3) 4f else 0f, gy, 0.6f)

                        when (cIdx) {
                            0 -> {
                                // AND Gate (Flat back, curved nose)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pBotL, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = project3D(symLeft + symW * 0.4f, gy - symH / 2f, 0.6f), strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = project3D(symLeft + symW * 0.4f, gy + symH / 2f, 0.6f), strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = project3D(symLeft + symW * 0.4f, gy - symH / 2f, 0.6f), end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = project3D(symLeft + symW * 0.4f, gy + symH / 2f, 0.6f), end = pTip, strokeWidth = 1.4f)
                            }
                            1 -> {
                                // OR Gate (Curved back, curved pointed tip)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = project3D(symLeft + 3f, gy, 0.6f), strokeWidth = 1.2f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = project3D(symLeft + 3f, gy, 0.6f), strokeWidth = 1.2f)
                            }
                            2 -> {
                                // NAND Gate (AND shape + Inversion Bubble)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pBotL, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = pTip, strokeWidth = 1.4f)
                                val bubbleCenter = project3D(symRight - 2f, gy, 0.6f)
                                drawCircle(color = Color.White, radius = 2.4f, center = bubbleCenter, style = Stroke(1.2f))
                            }
                            3 -> {
                                // NOR Gate (OR shape + Inversion Bubble)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = pTip, strokeWidth = 1.4f)
                                val bubbleCenter = project3D(symRight - 2f, gy, 0.6f)
                                drawCircle(color = Color.White, radius = 2.4f, center = bubbleCenter, style = Stroke(1.2f))
                            }
                            4 -> {
                                // EX OR Gate (XOR dual curved input line + OR body)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pTopL, end = pTip, strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pBotL, end = pTip, strokeWidth = 1.4f)
                                val pXorL1 = project3D(symLeft - 3.5f, gy - symH / 2f, 0.6f)
                                val pXorL2 = project3D(symLeft - 3.5f, gy + symH / 2f, 0.6f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pXorL1, end = project3D(symLeft - 1f, gy, 0.6f), strokeWidth = 1.4f)
                                drawLine(color = Color.White.copy(alpha = 0.95f), start = pXorL2, end = project3D(symLeft - 1f, gy, 0.6f), strokeWidth = 1.4f)
                            }
                        }
                    }

                    // Authentic 14-Pin Dual In-Line Package (DIP) IC Seated in Center of Each Gate Column
                    val icCenterY = (gateAreaY0 + gateAreaY1) / 2f
                    val icW = colWidth * 0.38f
                    val icH = 34f
                    val icElev = 5.5f

                    // IC Drop Shadow
                    val icS0 = project3D(colMidX - icW / 2f, icCenterY - icH / 2f, 0.5f)
                    val icS1 = project3D(colMidX + icW / 2f, icCenterY - icH / 2f, 0.5f)
                    val icS2 = project3D(colMidX + icW / 2f, icCenterY + icH / 2f, 0.5f)
                    val icS3 = project3D(colMidX - icW / 2f, icCenterY + icH / 2f, 0.5f)
                    drawPath(
                        path = Path().apply {
                            moveTo(icS0.x, icS0.y + 2f)
                            lineTo(icS1.x, icS1.y + 2f)
                            lineTo(icS2.x, icS2.y + 2f)
                            lineTo(icS3.x, icS3.y + 2f)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.45f)
                    )

                    // 14 Chrome Metallic DIP Pins (7 on Left, 7 on Right)
                    for (p in 0..6) {
                        val frac = (p + 0.5f) / 7f
                        val py = (icCenterY - icH / 2f) + frac * icH
                        val pinL_Top = project3D(colMidX - icW / 2f, py, icElev * 0.65f)
                        val pinL_Bot = project3D(colMidX - icW / 2f - 3.5f, py, 0.5f)
                        drawLine(color = Color(0xFFCFD8DC), start = pinL_Top, end = pinL_Bot, strokeWidth = 1.8f)

                        val pinR_Top = project3D(colMidX + icW / 2f, py, icElev * 0.65f)
                        val pinR_Bot = project3D(colMidX + icW / 2f + 3.5f, py, 0.5f)
                        drawLine(color = Color(0xFFCFD8DC), start = pinR_Top, end = pinR_Bot, strokeWidth = 1.8f)
                    }

                    // IC Body (Matte Black Epoxy Package)
                    val icP0 = project3D(colMidX - icW / 2f, icCenterY - icH / 2f, icElev)
                    val icP1 = project3D(colMidX + icW / 2f, icCenterY - icH / 2f, icElev)
                    val icP2 = project3D(colMidX + icW / 2f, icCenterY + icH / 2f, icElev)
                    val icP3 = project3D(colMidX - icW / 2f, icCenterY + icH / 2f, icElev)

                    drawPath(
                        path = Path().apply {
                            moveTo(icP0.x, icP0.y)
                            lineTo(icP1.x, icP1.y)
                            lineTo(icP2.x, icP2.y)
                            lineTo(icP3.x, icP3.y)
                            close()
                        },
                        color = Color(0xFF1E2124) // Textured matte black epoxy
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(icP0.x, icP0.y)
                            lineTo(icP1.x, icP1.y)
                            lineTo(icP2.x, icP2.y)
                            lineTo(icP3.x, icP3.y)
                            close()
                        },
                        color = Color(0xFF455A64),
                        style = Stroke(0.8f)
                    )

                    // Pin 1 Index Notch at Top of Chip
                    val notchCenter = project3D(colMidX, icCenterY - icH / 2f, icElev)
                    drawCircle(color = Color(0xFF111111), radius = 2.2f, center = notchCenter)

                    // Laser-etched Part Number markings on IC (e.g., 7408, 7432...)
                    val icTextPos = project3D(colMidX, icCenterY + 2f, icElev + 0.5f)
                    drawContext.canvas.nativeCanvas.drawText(gateIcPartNumbers[cIdx], icTextPos.x, icTextPos.y, trainerChipPaint)

                    val icSubTextPos = project3D(colMidX, icCenterY + 11f, icElev + 0.5f)
                    drawContext.canvas.nativeCanvas.drawText(gateIcMfgLabels[cIdx].take(7), icSubTextPos.x, icSubTextPos.y, trainerPinNumPaint)
                }

                // =========================================================================================
                // BOTTOM ROW: NOT GATES (7404), 4-INPUT AND GATES (7420), AND SPARE 16-PIN SOCKET
                // =========================================================================================
                val botRowY0 = by1 - 60f
                val botRowY1 = by1 - 14f

                // --- 1. NOT GATES (Hex Inverter with 7404 IC) on Left of bottom row ---
                val notX0 = centerAreaX0 + 2f
                val notX1 = centerAreaX0 + (centerAreaX1 - centerAreaX0) * 0.40f
                val notMidX = (notX0 + notX1) / 2f

                val notBox0 = project3D(notX0, botRowY0, 0.6f)
                val notBox1 = project3D(notX1, botRowY0, 0.6f)
                val notBox2 = project3D(notX1, botRowY1, 0.6f)
                val notBox3 = project3D(notX0, botRowY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(notBox0.x, notBox0.y)
                        lineTo(notBox1.x, notBox1.y)
                        lineTo(notBox2.x, notBox2.y)
                        lineTo(notBox3.x, notBox3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.2f)
                )
                val notTitlePos = project3D(notMidX, botRowY0 + 9f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("NOT GATES", notTitlePos.x, notTitlePos.y, trainerWhiteHeaderPaint)

                // 7404 IC in NOT GATES section
                val notIcP0 = project3D(notMidX - 16f, botRowY0 + 16f, 4.5f)
                val notIcP1 = project3D(notMidX + 16f, botRowY0 + 16f, 4.5f)
                val notIcP2 = project3D(notMidX + 16f, botRowY1 - 8f, 4.5f)
                val notIcP3 = project3D(notMidX - 16f, botRowY1 - 8f, 4.5f)
                drawPath(
                    path = Path().apply {
                        moveTo(notIcP0.x, notIcP0.y)
                        lineTo(notIcP1.x, notIcP1.y)
                        lineTo(notIcP2.x, notIcP2.y)
                        lineTo(notIcP3.x, notIcP3.y)
                        close()
                    },
                    color = Color(0xFF1E2124)
                )
                val notIcText = project3D(notMidX, (botRowY0 + 16f + botRowY1 - 8f) / 2f + 2f, 5f)
                drawContext.canvas.nativeCanvas.drawText("7404", notIcText.x, notIcText.y, trainerChipPaint)

                // NOT Gate Sockets
                val notSockIn = project3D(notX0 + 8f, (botRowY0 + botRowY1) / 2f, 1.4f)
                val notSockOut = project3D(notX1 - 8f, (botRowY0 + botRowY1) / 2f, 1.4f)
                drawCircle(color = Color.White, radius = 3.6f, center = notSockIn, style = Stroke(0.6f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 2.8f, center = notSockIn)
                drawCircle(color = Color.White, radius = 3.6f, center = notSockOut, style = Stroke(0.6f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 2.8f, center = notSockOut)

                // --- 2. 4 INPUT AND GATES (with 7420 IC) in Center of bottom row ---
                val and4X0 = notX1 + 4f
                val and4X1 = centerAreaX0 + (centerAreaX1 - centerAreaX0) * 0.74f
                val and4MidX = (and4X0 + and4X1) / 2f

                val and4Box0 = project3D(and4X0, botRowY0, 0.6f)
                val and4Box1 = project3D(and4X1, botRowY0, 0.6f)
                val and4Box2 = project3D(and4X1, botRowY1, 0.6f)
                val and4Box3 = project3D(and4X0, botRowY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(and4Box0.x, and4Box0.y)
                        lineTo(and4Box1.x, and4Box1.y)
                        lineTo(and4Box2.x, and4Box2.y)
                        lineTo(and4Box3.x, and4Box3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.2f)
                )
                val and4TitlePos = project3D(and4MidX, botRowY0 + 9f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("4 INPUT AND GATES", and4TitlePos.x, and4TitlePos.y, trainerWhiteHeaderPaint)

                // 7420 IC in 4-INPUT AND section
                val and4IcP0 = project3D(and4MidX - 16f, botRowY0 + 16f, 4.5f)
                val and4IcP1 = project3D(and4MidX + 16f, botRowY0 + 16f, 4.5f)
                val and4IcP2 = project3D(and4MidX + 16f, botRowY1 - 8f, 4.5f)
                val and4IcP3 = project3D(and4MidX - 16f, botRowY1 - 8f, 4.5f)
                drawPath(
                    path = Path().apply {
                        moveTo(and4IcP0.x, and4IcP0.y)
                        lineTo(and4IcP1.x, and4IcP1.y)
                        lineTo(and4IcP2.x, and4IcP2.y)
                        lineTo(and4IcP3.x, and4IcP3.y)
                        close()
                    },
                    color = Color(0xFF1E2124)
                )
                val and4IcText = project3D(and4MidX, (botRowY0 + 16f + botRowY1 - 8f) / 2f + 2f, 5f)
                drawContext.canvas.nativeCanvas.drawText("7420", and4IcText.x, and4IcText.y, trainerChipPaint)

                // --- 3. SPARE 16-PIN IC SOCKET on Right of bottom row ---
                val spareX0 = and4X1 + 4f
                val spareX1 = centerAreaX1 - 2f
                val spareMidX = (spareX0 + spareX1) / 2f

                val spBox0 = project3D(spareX0, botRowY0, 0.6f)
                val spBox1 = project3D(spareX1, botRowY0, 0.6f)
                val spBox2 = project3D(spareX1, botRowY1, 0.6f)
                val spBox3 = project3D(spareX0, botRowY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(spBox0.x, spBox0.y)
                        lineTo(spBox1.x, spBox1.y)
                        lineTo(spBox2.x, spBox2.y)
                        lineTo(spBox3.x, spBox3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.2f)
                )
                val spTitlePos = project3D(spareMidX, botRowY0 + 9f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("SPARE SOCKET", spTitlePos.x, spTitlePos.y, trainerWhiteHeaderPaint)

                // 16-Pin DIP Socket Receptacles (8 on top row, 8 on bottom row)
                for (sIdx in 0 until 8) {
                    val sx = spareX0 + 8f + sIdx * ((spareX1 - spareX0 - 16f) / 7f)
                    val sTop = project3D(sx, botRowY0 + 18f, 2f)
                    val sBot = project3D(sx, botRowY1 - 10f, 2f)
                    drawCircle(color = Color(0xFFCFD8DC), radius = 2.2f, center = sTop)
                    drawCircle(color = Color(0xFF111111), radius = 1.1f, center = sTop)
                    drawCircle(color = Color(0xFFCFD8DC), radius = 2.2f, center = sBot)
                    drawCircle(color = Color(0xFF111111), radius = 1.1f, center = sBot)
                }

                // =========================================================================================
                // REALISTIC 3D PATCH JUMPER WIRES (CONNECTING SWITCHES, GATES & TOP RED GLOWING LED)
                // =========================================================================================
                // Wire 1: Black patch wire from Input Switch 1 Turret Socket to Gate 1 (AND) Input A
                val w1Start = project3D(inX0 + 40f, inTopY + 20f, 3.5f)
                val w1End = project3D(centerAreaX0 + 7f, gateAreaY0 + 20f, 3.5f)
                val w1Mid = project3D((inX0 + 40f + centerAreaX0 + 7f) / 2f, inTopY + 14f, 18f)

                // Wire 1 Drop Shadow
                drawLine(color = Color.Black.copy(alpha = 0.35f), start = Offset(w1Start.x, w1Start.y + 4f), end = Offset(w1End.x, w1End.y + 4f), strokeWidth = 3f, cap = StrokeCap.Round)
                // Wire 1 Black Insulated Flexible Cable
                drawLine(color = Color(0xFF1E2124), start = w1Start, end = w1Mid, strokeWidth = 3.8f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF1E2124), start = w1Mid, end = w1End, strokeWidth = 3.8f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF546E7A), start = w1Start, end = w1Mid, strokeWidth = 1.2f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF546E7A), start = w1Mid, end = w1End, strokeWidth = 1.2f, cap = StrokeCap.Round)
                // Silver Banana Plug Ends
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w1Start)
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w1End)

                // Wire 2: Black patch wire from Input Switch 2 Turret Socket to Gate 1 (AND) Input B
                val w2Start = project3D(inX0 + 40f, inTopY + 20f + swStepY, 3.5f)
                val w2End = project3D(centerAreaX0 + 7f, gateAreaY0 + 28f, 3.5f)
                val w2Mid = project3D((inX0 + 40f + centerAreaX0 + 7f) / 2f, inTopY + 28f, 16f)

                drawLine(color = Color.Black.copy(alpha = 0.35f), start = Offset(w2Start.x, w2Start.y + 4f), end = Offset(w2End.x, w2End.y + 4f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF1E2124), start = w2Start, end = w2Mid, strokeWidth = 3.8f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF1E2124), start = w2Mid, end = w2End, strokeWidth = 3.8f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF546E7A), start = w2Start, end = w2Mid, strokeWidth = 1.2f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF546E7A), start = w2Mid, end = w2End, strokeWidth = 1.2f, cap = StrokeCap.Round)
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w2Start)
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w2End)

                // Wire 3: Signature RED patch wire from Gate 1 Output to Top Output Red LED Turret Socket (L1)
                // (Matches the exact patch setup from the user's reference photograph!)
                val w3Start = project3D(centerAreaX0 + colWidth - 7f, gateAreaY0 + 24f, 3.5f)
                val w3End = project3D(outX1 - 40f, outTopY + 20f, 3.5f)
                val w3Mid = project3D((centerAreaX0 + colWidth + outX1 - 40f) / 2f, gateAreaY0 + 12f, 24f)

                // Wire 3 Drop Shadow
                drawLine(color = Color.Black.copy(alpha = 0.35f), start = Offset(w3Start.x, w3Start.y + 5f), end = Offset(w3End.x, w3End.y + 5f), strokeWidth = 3.2f, cap = StrokeCap.Round)
                // Wire 3 Vivid Red Insulated Flexible Jumper Cable
                drawLine(color = Color(0xFFB71C1C), start = w3Start, end = w3Mid, strokeWidth = 4.2f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFFB71C1C), start = w3Mid, end = w3End, strokeWidth = 4.2f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFFFF1744), start = w3Start, end = w3Mid, strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFFFF1744), start = w3Mid, end = w3End, strokeWidth = 2.4f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFFFFCDD2).copy(alpha = 0.85f), start = w3Start, end = w3Mid, strokeWidth = 1f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFFFFCDD2).copy(alpha = 0.85f), start = w3Mid, end = w3End, strokeWidth = 1f, cap = StrokeCap.Round)

                // Silver Banana Plugs with Red Strain Relief Collars
                drawCircle(color = Color(0xFFD32F2F), radius = 5.2f, center = w3Start)
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w3Start)
                drawCircle(color = Color(0xFFD32F2F), radius = 5.2f, center = w3End)
                drawCircle(color = Color(0xFFECEFF1), radius = 3.5f, center = w3End)

                // Inline Signal Indicator Red LED Diode on Patch Jumper
                draw3DThroughHoleLEDDiode(
                    cx = (centerAreaX0 + colWidth + outX1 - 40f) / 2f,
                    cy = gateAreaY0 + 12f,
                    isLit = isTrainerPowerOn,
                    scale = 0.72f,
                    baseZ = 20f,
                    showLeads = false,
                    label = "",
                    project3D = ::project3D
                )
            } else {
                // =========================================================================================
                // AUTHENTIC 3D SOLDERLESS BREADBOARD & PCB WORKBENCH SUBSTRATE
                // =========================================================================================
                // 1. Solderless Breadboard Plastic Surface (Warm Off-White / Ivory Substrate)
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFFEFECE6) // Authentic ivory/cream ABS breadboard plastic
                )

                // Aluminum Baseplate Perimeter Rim
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFF90A4AE).copy(alpha = 0.85f),
                    style = Stroke(width = 2.2f)
                )

                val bWidth = bx1 - bx0
                val bHeight = by1 - by0
                val midTroughY = (by0 + by1) / 2f

                // 2. Central IC Divider Trough / Notch (Depression separating row banks A-E and F-J)
                val trL0 = project3D(bx0 + 20f, midTroughY - 7f, 0.2f)
                val trR0 = project3D(bx1 - 20f, midTroughY - 7f, 0.2f)
                val trR1 = project3D(bx1 - 20f, midTroughY + 7f, 0.2f)
                val trL1 = project3D(bx0 + 20f, midTroughY + 7f, 0.2f)
                drawPath(
                    path = Path().apply {
                        moveTo(trL0.x, trL0.y)
                        lineTo(trR0.x, trR0.y)
                        lineTo(trR1.x, trR1.y)
                        lineTo(trL1.x, trL1.y)
                        close()
                    },
                    color = Color(0xFFCFD8DC) // Recessed divider channel
                )
                drawLine(color = Color(0xFF90A4AE), start = trL0, end = trR0, strokeWidth = 1f)
                drawLine(color = Color(0xFF90A4AE), start = trL1, end = trR1, strokeWidth = 1f)

                // 3. Power Distribution Bus Rails (Top & Bottom Red/Blue Lines)
                // Top Power Rail (Red +VCC, Blue -GND)
                val topRedA = project3D(bx0 + 30f, by0 + 22f, 0.4f)
                val topRedB = project3D(bx1 - 30f, by0 + 22f, 0.4f)
                drawLine(color = Color(0xFFFF1744).copy(alpha = 0.85f), start = topRedA, end = topRedB, strokeWidth = 2.2f)

                val topBlueA = project3D(bx0 + 30f, by0 + 34f, 0.4f)
                val topBlueB = project3D(bx1 - 30f, by0 + 34f, 0.4f)
                drawLine(color = Color(0xFF1E88E5).copy(alpha = 0.85f), start = topBlueA, end = topBlueB, strokeWidth = 2.2f)

                // Bottom Power Rail (Red +VCC, Blue -GND)
                val botBlueA = project3D(bx0 + 30f, by1 - 34f, 0.4f)
                val botBlueB = project3D(bx1 - 30f, by1 - 34f, 0.4f)
                drawLine(color = Color(0xFF1E88E5).copy(alpha = 0.85f), start = botBlueA, end = botBlueB, strokeWidth = 2.2f)

                val botRedA = project3D(bx0 + 30f, by1 - 22f, 0.4f)
                val botRedB = project3D(bx1 - 30f, by1 - 22f, 0.4f)
                drawLine(color = Color(0xFFFF1744).copy(alpha = 0.85f), start = botRedA, end = botRedB, strokeWidth = 2.2f)

                // 4. Solderless Breadboard Tie-Point Contact Pin Grid (Square holes with silver spring clips)
                if (showPcbGrid) {
                    val tieStepX = 35f
                    val tieStepY = 28f
                    var tx = bx0 + 40f
                    var colNum = 1
                    while (tx < bx1 - 35f) {
                        // Tie point rows in top bank (rows A-E) and bottom bank (rows F-J)
                        listOf(
                            by0 + 52f, by0 + 52f + tieStepY, by0 + 52f + tieStepY * 2f,
                            midTroughY + 18f, midTroughY + 18f + tieStepY, midTroughY + 18f + tieStepY * 2f
                        ).forEach { ty ->
                            val tp = project3D(tx, ty, 0.3f)
                            // Square contact hole with nickel silver spring clip inside
                            drawRect(
                                color = Color(0xFF263238),
                                topLeft = Offset(tp.x - 2.5f, tp.y - 2.5f),
                                size = androidx.compose.ui.geometry.Size(5f, 5f)
                            )
                            drawRect(
                                color = Color(0xFFCFD8DC),
                                topLeft = Offset(tp.x - 1.2f, tp.y - 1.2f),
                                size = androidx.compose.ui.geometry.Size(2.4f, 2.4f)
                            )
                        }

                        // Column number silkscreen (1, 5, 10, 15...)
                        if (colNum % 5 == 1) {
                            val colLblPos = project3D(tx, midTroughY + 3f, 0.5f)
                            drawContext.canvas.nativeCanvas.drawText("$colNum", colLblPos.x, colLblPos.y, trainerPinNumPaint)
                        }

                        tx += tieStepX
                        colNum += 5
                    }
                }

                // Breadboard Header Silkscreen Brand
                drawContext.canvas.nativeCanvas.drawText("LOGICLAB CAD • 3D BREADBOARD & PCB WORKBENCH", t0.x + 14f, t0.y + 18f, gridTextPaint)
            }
        }

        // 2. Render 3D Elevated Circuit Cables (Wires)
        if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) {
            val bw = bx1 - bx0
            val bh = by1 - by0
            val circuitSwitches = currentComponents.filter { it.type == "SWITCH" }
            val circuitOutputs = currentComponents.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }
            val logicComps = currentComponents.filter { it.type != "SWITCH" && it.type != "LED" && it.type != "DIGITAL_PROBE" }

            val topY0 = by0 + 12f
            val splitX = bx0 + bw * 0.60f
            val ledX0 = bx0 + 20f
            val ledX1 = splitX - 16f
            val ledSpacing = (ledX1 - ledX0) / (12 - 1)

            val inputX0 = bx0 + 10f
            val inputX1 = bx0 + bw * 0.58f
            val swSpacing = (inputX1 - inputX0) / (12 + 1)
            val swSockY = by1 - 70f + 14f

            val bayY0 = by0 + 94f
            val bayY1 = by1 - 88f
            val bayW = (bw - 20f) / 5f
            val zifW = bayW * 0.40f
            val zifH = (bayY1 - bayY0) * 0.68f
            val zifY = (bayY0 + bayY1) / 2f + 4f
            val slotSpacing = zifH / (8 + 1)

            for ((wIdx, w) in currentWires.withIndex()) {
                val fromComp = currentComponents.find { it.id == w.fromComponentId } ?: continue
                val toComp = currentComponents.find { it.id == w.toComponentId } ?: continue

                val startPos: Offset = when (fromComp.type) {
                    "SWITCH" -> {
                        val sIdx = circuitSwitches.indexOfFirst { it.id == fromComp.id }.let { if (it >= 0) it else 0 }
                        val sx = inputX0 + (sIdx + 1) * swSpacing
                        Offset(sx, swSockY)
                    }
                    "CLOCK", "OSCILLATOR" -> {
                        val clkX0 = bx0 + bw * 0.60f
                        val clkX1 = bx0 + bw * 0.74f
                        val colSpacing = (clkX1 - clkX0) / 4f
                        Offset(clkX0 + colSpacing * 2f, by1 - 70f + 16f)
                    }
                    else -> {
                        val bIdx = logicComps.indexOfFirst { it.id == fromComp.id }.let { if (it >= 0) it else 0 }
                        val bayLeft = bx0 + 10f + bIdx * bayW
                        val bayCenter = bayLeft + bayW / 2f
                        val pinRightX = bayCenter + zifW / 2f + 14f
                        val py = zifY - zifH / 2f + 3 * slotSpacing
                        Offset(pinRightX, py)
                    }
                }

                val endPos: Offset = when (toComp.type) {
                    "LED", "DIGITAL_PROBE" -> {
                        val oIdx = circuitOutputs.indexOfFirst { it.id == toComp.id }.let { if (it >= 0) it else 0 }
                        val lx = ledX0 + oIdx * ledSpacing
                        Offset(lx, topY0 + 46f)
                    }
                    else -> {
                        val bIdx = logicComps.indexOfFirst { it.id == toComp.id }.let { if (it >= 0) it else 0 }
                        val bayLeft = bx0 + 10f + bIdx * bayW
                        val bayCenter = bayLeft + bayW / 2f
                        val pinLeftX = bayCenter - zifW / 2f - 14f
                        val py = zifY - zifH / 2f + ((wIdx % 2) + 1) * slotSpacing
                        Offset(pinLeftX, py)
                    }
                }

                val isHigh = w.state == DigitalState.HIGH
                val patchColor = when (wIdx % 5) {
                    0 -> if (isHigh) Color(0xFFFF1744) else Color(0xFFC62828)
                    1 -> Color(0xFF1E88E5)
                    2 -> Color(0xFFFFB300)
                    3 -> Color(0xFF00C853)
                    else -> Color(0xFF212121)
                }

                val wp1 = project3D(startPos.x, startPos.y, 2f)
                val wp2 = project3D(endPos.x, endPos.y, 2f)
                val midX = (startPos.x + endPos.x) / 2f
                val midY = (startPos.y + endPos.y) / 2f
                val arcElevation = (30f + (wIdx % 4) * 8f)
                val wpMid = project3D(midX, midY, arcElevation)

                // Banana plug boots
                drawCircle(color = patchColor, radius = 4.8f, center = wp1)
                drawCircle(color = Color(0xFFCFD8DC), radius = 2f, center = wp1)
                drawCircle(color = patchColor, radius = 4.8f, center = wp2)
                drawCircle(color = Color(0xFFCFD8DC), radius = 2f, center = wp2)

                // Arched cable with shadow
                val wirePath = Path().apply {
                    moveTo(wp1.x, wp1.y)
                    quadraticTo(wpMid.x, wpMid.y, wp2.x, wp2.y)
                }
                if (showDropShadows) {
                    val sp1 = project3D(startPos.x, startPos.y, 0.5f)
                    val sp2 = project3D(endPos.x, endPos.y, 0.5f)
                    val spMid = project3D(midX, midY, 0.5f)
                    val shadowPath = Path().apply {
                        moveTo(sp1.x, sp1.y)
                        quadraticTo(spMid.x, spMid.y, sp2.x, sp2.y)
                    }
                    drawPath(shadowPath, color = Color.Black.copy(alpha = 0.35f), style = Stroke(width = 4f, cap = StrokeCap.Round))
                }
                drawLine(color = Color(0xFF212121), start = wp1, end = wpMid, strokeWidth = 4.6f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF212121), start = wpMid, end = wp2, strokeWidth = 4.6f, cap = StrokeCap.Round)
                drawPath(wirePath, color = patchColor, style = Stroke(width = 3.4f, cap = StrokeCap.Round))
                drawPath(wirePath, color = Color.White.copy(alpha = 0.45f), style = Stroke(width = 1f, cap = StrokeCap.Round))

                if (isHigh) {
                    drawCircle(color = Color.White, radius = 2f, center = wpMid)
                }
            }
        } else {
            for (w in currentWires) {
                val fromComp = currentComponents.find { it.id == w.fromComponentId } ?: continue
                val toComp = currentComponents.find { it.id == w.toComponentId } ?: continue
                val fromDef = ComponentRegistry.get(fromComp.type) ?: continue
                val toDef = ComponentRegistry.get(toComp.type) ?: continue
                val fromPort = (fromDef.outputs + fromDef.inputs).find { 
                    it.id == w.fromPortId || (it.id == "OUT" && (w.fromPortId == "CLK" || w.fromPortId == "Q")) || (it.id == "CLK" && w.fromPortId == "OUT") || (it.id == "INV" && (w.fromPortId == "CLK'" || w.fromPortId == "CLK_BAR"))
                } ?: continue
                val toPort = (toDef.inputs + toDef.outputs).find { 
                    it.id == w.toPortId || (it.id == "OUT" && (w.toPortId == "CLK" || w.toPortId == "Q")) || (it.id == "CLK" && w.toPortId == "OUT") || (it.id == "INV" && (w.toPortId == "CLK'" || w.toPortId == "CLK_BAR"))
                } ?: continue

                val p1x = fromComp.x + fromDef.width * fromPort.relativeX
                val p1y = fromComp.y + fromDef.height * fromPort.relativeY
                val p2x = toComp.x + toDef.width * toPort.relativeX
                val p2y = toComp.y + toDef.height * toPort.relativeY

                val isHigh = w.state == DigitalState.HIGH
                val isLow = w.state == DigitalState.LOW

                // Dynamic wire color respecting Signal Flow mode
                val wireColor = if (isSignalFlowMode) {
                    when (w.state) {
                        DigitalState.HIGH -> SignalFlowHigh
                        DigitalState.LOW -> SignalFlowLow
                        DigitalState.UNKNOWN -> SignalUnknown
                    }
                } else {
                    if (isHigh) SignalHigh else Color(0xFF388BFD)
                }

                // Project endpoints in 3D
                val wp1 = project3D(p1x, p1y, wireElevationLevel)
                val wp2 = project3D(p2x, p2y, wireElevationLevel)

                // Wire ground shadow on PCB
                if (showDropShadows) {
                    val sp1 = project3D(p1x, p1y, 0.5f)
                    val sp2 = project3D(p2x, p2y, 0.5f)
                    val shadowColor = if (isSignalFlowMode && isHigh) {
                        SignalFlowHigh.copy(alpha = 0.28f)
                    } else {
                        Color.Black.copy(alpha = 0.4f)
                    }

                    drawLine(
                        color = shadowColor,
                        start = sp1,
                        end = sp2,
                        strokeWidth = 3.5f
                    )
                }

                // Elevated 3D Cable with Core Highlight
                drawLine(
                    color = wireColor.copy(alpha = 0.9f),
                    start = wp1,
                    end = wp2,
                    strokeWidth = 3.8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.55f),
                    start = wp1,
                    end = wp2,
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )

                // Terminal mounting posts connecting elevated wire to PCB pad
                drawLine(
                    color = Color(0xFFAAAAAA),
                    start = project3D(p1x, p1y, 0f),
                    end = wp1,
                    strokeWidth = 2.2f
                )
                drawLine(
                    color = Color(0xFFAAAAAA),
                    start = project3D(p2x, p2y, 0f),
                    end = wp2,
                    strokeWidth = 2.2f
                )

                // Traveling 3D electron pulse beads
                if (isSimulating) {
                    if (isHigh) {
                        listOf(pulsePhase, (pulsePhase + 0.5f) % 1.0f).forEach { t ->
                            val pulsePos = Offset(
                                wp1.x + (wp2.x - wp1.x) * t,
                                wp1.y + (wp2.y - wp1.y) * t
                            )
                            drawCircle(
                                color = SignalFlowHigh.copy(alpha = 0.5f),
                                radius = 5f,
                                center = pulsePos
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.8f,
                                center = pulsePos
                            )
                        }
                    }
                }
            }
        }

            // 3. Render 3D Extruded Integrated Circuits (DIP Packages)
            // Sort back-to-front for proper depth occlusion
            val sortedComponents = currentComponents.sortedBy { comp ->
                (comp.x - boardCenter.x) * sinY + (comp.y - boardCenter.y) * cosY
            }

            if (board3DStyle == Board3DStyle.PCB_LAB) {
                for (comp in sortedComponents) {
                val def = ComponentRegistry.get(comp.type)
                val w = def?.width ?: 64f
                val h = def?.height ?: 48f
                val chipHeight = 16f
                val isSelected = comp.id == currentSelectedId
                val centerTop = project3D(comp.x + w / 2f, comp.y + h / 2f, chipHeight)
                val isChip = comp.type != "SWITCH" && comp.type != "LED" && comp.type != "CONST_HIGH" && comp.type != "CONST_LOW"

                if (isChip) {
                    // Top face corners
                    val ct0 = project3D(comp.x, comp.y, chipHeight)
                    val ct1 = project3D(comp.x + w, comp.y, chipHeight)
                    val ct2 = project3D(comp.x + w, comp.y + h, chipHeight)
                    val ct3 = project3D(comp.x, comp.y + h, chipHeight)

                    // Base corners on PCB
                    val cb0 = project3D(comp.x, comp.y, 0f)
                    val cb1 = project3D(comp.x + w, comp.y, 0f)
                    val cb2 = project3D(comp.x + w, comp.y + h, 0f)
                    val cb3 = project3D(comp.x, comp.y + h, 0f)

                    // Chip drop shadow on PCB
                    drawPath(
                        path = Path().apply {
                            moveTo(cb0.x, cb0.y + 4f)
                            lineTo(cb1.x, cb1.y + 4f)
                            lineTo(cb2.x, cb2.y + 4f)
                            lineTo(cb3.x, cb3.y + 4f)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.5f)
                    )

                    // Chrome DIP Metallic Pins
                    val numPins = 4
                    for (p in 0 until numPins) {
                        val frac = (p + 0.5f) / numPins
                        // Left pin
                        val lTop = project3D(comp.x, comp.y + h * frac, chipHeight * 0.6f)
                        val lBot = project3D(comp.x - 6f, comp.y + h * frac, 0f)
                        drawLine(color = Color(0xFFC0C0C0), start = lTop, end = lBot, strokeWidth = 2.4f)
                        drawCircle(color = Color(0xFFD4AF37), radius = 2.5f, center = lBot)

                        // Right pin
                        val rTop = project3D(comp.x + w, comp.y + h * frac, chipHeight * 0.6f)
                        val rBot = project3D(comp.x + w + 6f, comp.y + h * frac, 0f)
                        drawLine(color = Color(0xFFC0C0C0), start = rTop, end = rBot, strokeWidth = 2.4f)
                        drawCircle(color = Color(0xFFD4AF37), radius = 2.5f, center = rBot)
                    }

                    // Front 3D Extruded Bevel Face
                    drawPath(
                        path = Path().apply {
                            moveTo(ct3.x, ct3.y)
                            lineTo(ct2.x, ct2.y)
                            lineTo(cb2.x, cb2.y)
                            lineTo(cb3.x, cb3.y)
                            close()
                        },
                        color = Color(0xFF141920)
                    )

                    // Right 3D Extruded Bevel Face
                    drawPath(
                        path = Path().apply {
                            moveTo(ct1.x, ct1.y)
                            lineTo(ct2.x, ct2.y)
                            lineTo(cb2.x, cb2.y)
                            lineTo(cb1.x, cb1.y)
                            close()
                        },
                        color = Color(0xFF0F1318)
                    )

                    // Top Face of DIP IC (Matte Epoxy Black with subtle sheen)
                    val topBodyColor = when (comp.type) {
                        "OSCILLATOR" -> Color(0xFF2E2413)
                        else -> Color(0xFF1E2530)
                    }

                    drawPath(
                        path = Path().apply {
                            moveTo(ct0.x, ct0.y)
                            lineTo(ct1.x, ct1.y)
                            lineTo(ct2.x, ct2.y)
                            lineTo(ct3.x, ct3.y)
                            close()
                        },
                        color = topBodyColor
                    )

                    // Selected State: Glowing Cyan Holographic Outline
                    if (isSelected) {
                        drawPath(
                            path = Path().apply {
                                moveTo(ct0.x, ct0.y)
                                lineTo(ct1.x, ct1.y)
                                lineTo(ct2.x, ct2.y)
                                lineTo(ct3.x, ct3.y)
                                close()
                            },
                            color = Color(0xFF00E5FF),
                            style = Stroke(width = 3.5f)
                        )
                        drawCircle(color = Color.White, radius = 4f, center = ct0)
                        drawCircle(color = Color.White, radius = 4f, center = ct1)
                        drawCircle(color = Color.White, radius = 4f, center = ct2)
                        drawCircle(color = Color.White, radius = 4f, center = ct3)
                    } else {
                        drawPath(
                            path = Path().apply {
                                moveTo(ct0.x, ct0.y)
                                lineTo(ct1.x, ct1.y)
                                lineTo(ct2.x, ct2.y)
                                lineTo(ct3.x, ct3.y)
                                close()
                            },
                            color = Color(0xFF384A60),
                            style = Stroke(width = 1.2f)
                        )
                    }

                    // Alignment Notch at pin 1 end
                    val notchCenter = Offset((ct0.x + ct1.x) / 2f, (ct0.y + ct1.y) / 2f)
                    drawCircle(color = Color(0xFF0A0D11), radius = 3.5f, center = notchCenter)

                    // Chip Text Silkscreen
                    val textCenter = Offset(
                        (ct0.x + ct1.x + ct2.x + ct3.x) / 4f,
                        (ct0.y + ct1.y + ct2.y + ct3.y) / 4f + 3f
                    )
                    val labelText = comp.label.ifBlank { def?.name ?: comp.type }
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText.take(9),
                        textCenter.x,
                        textCenter.y,
                        chipTextPaint
                    )
                } else {
                    // Discrete Components (Standalone Metal Switches & 5mm Red LEDs on PCB)
                    val padL = project3D(comp.x + 10f, comp.y + h / 2f, 0.5f)
                    val padR = project3D(comp.x + w - 10f, comp.y + h / 2f, 0.5f)
                    drawCircle(color = Color(0xFFD4AF37), radius = 4.2f, center = padL)
                    drawCircle(color = Color(0xFF1B2A38), radius = 2f, center = padL)
                    drawCircle(color = Color(0xFFD4AF37), radius = 4.2f, center = padR)
                    drawCircle(color = Color(0xFF1B2A38), radius = 2f, center = padR)

                    if (isSelected) {
                        val selC = project3D(comp.x + w / 2f, comp.y + h / 2f, 0.5f)
                        drawCircle(color = Color(0xFF00E5FF), radius = 24f, center = selC, style = Stroke(2.5f))
                    }
                }

                // Component specific 3D physical widgets
                when (comp.type) {
                    "SWITCH" -> {
                        // Authentic Daier ASW-23-101 Metal Low Voltage Toggle Switch Mounted on PCB
                        val switchCenterX = comp.x + w / 2f
                        val switchCenterY = comp.y + h / 2f
                        drawASW23101ToggleSwitch(
                            cx = switchCenterX,
                            cy = switchCenterY,
                            isOn = comp.switchState,
                            scale = 1.25f,
                            showChassisBox = true,
                            project3D = ::project3D
                        )

                        // Status Silkscreen Readout
                        val swBase = project3D(switchCenterX, switchCenterY, 8.5f * 1.25f)
                        val statusText = if (comp.switchState) "1 (ON)" else "0 (OFF)"
                        val statusPaint = Paint().apply {
                            color = if (comp.switchState) android.graphics.Color.rgb(0, 230, 118) else android.graphics.Color.LTGRAY
                            textSize = 10f
                            typeface = Typeface.MONOSPACE
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(statusText, swBase.x, swBase.y + 24f, statusPaint)
                    }
                    "LED", "DIODE" -> {
                        // Authentic 5mm Through-Hole Red Cylindrical Dome LED Diode (with internal leadframe, anvil, post, die cup, and bond wire)
                        val isHigh = comp.inputValues.values.any { it == DigitalState.HIGH }
                        val diodeCenter = Offset(comp.x + w / 2f, comp.y + h / 2f)

                        draw3DThroughHoleLEDDiode(
                            cx = diodeCenter.x,
                            cy = diodeCenter.y,
                            isLit = isHigh,
                            scale = 1.25f,
                            baseZ = 0.5f,
                            showLeads = true,
                            label = "LED",
                            project3D = ::project3D
                        )

                        // Red LED Silkscreen Label
                        val ledLblPos = project3D(diodeCenter.x, diodeCenter.y + 20f, 0.8f)
                        drawContext.canvas.nativeCanvas.drawText("DIODE (LED)", ledLblPos.x, ledLblPos.y, trainerPinNumPaint)
                    }
                    "CONST_HIGH", "CONST_LOW" -> {
                        val isHigh = comp.type == "CONST_HIGH"
                        val switchCenterX = comp.x + w / 2f
                        val switchCenterY = comp.y + h / 2f

                        // Authentic ASW-23-101 Heavy Duty Fixed Power Switch
                        drawASW23101ToggleSwitch(
                            cx = switchCenterX,
                            cy = switchCenterY,
                            isOn = isHigh,
                            scale = 1.15f,
                            showChassisBox = true,
                            project3D = ::project3D
                        )

                        val swBase = project3D(switchCenterX, switchCenterY, 8.5f * 1.15f)
                        val statusText = if (isHigh) "+5V (HIGH)" else "GND (LOW)"
                        val statusPaint = Paint().apply {
                            color = if (isHigh) android.graphics.Color.rgb(0, 230, 118) else android.graphics.Color.LTGRAY
                            textSize = 8.5f
                            typeface = Typeface.MONOSPACE
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(statusText, swBase.x, swBase.y + 22f, statusPaint)
                    }
                    "OSCILLATOR" -> {
                        val oscHigh = comp.switchState
                        val barColor = if (oscHigh) Color(0xFFFFB300) else Color(0xFF554422)
                        drawCircle(color = barColor, radius = 6f, center = centerTop)
                    }
                    "DIGITAL_PROBE" -> {
                        val isHigh = comp.inputValues.values.any { it == DigitalState.HIGH }
                        val probeCenter = Offset(comp.x + w / 2f, comp.y + h / 2f)

                        // 3D Emerald Probe LED Diode with Internal Leadframe & Bloom
                        draw3DThroughHoleLEDDiode(
                            cx = probeCenter.x,
                            cy = probeCenter.y,
                            isLit = isHigh,
                            colorTint = Color(0xFF00E676), // Emerald probe luminescence
                            scale = 1.15f,
                            baseZ = 0.5f,
                            showLeads = true,
                            label = "PROBE",
                            project3D = ::project3D
                        )

                        val probeText = if (isHigh) "LOGIC 1" else "LOGIC 0"
                        val pPaint = Paint().apply {
                            color = if (isHigh) android.graphics.Color.rgb(0, 230, 118) else android.graphics.Color.LTGRAY
                            textSize = 8.5f
                            typeface = Typeface.DEFAULT_BOLD
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(probeText, probeCenter.x, probeCenter.y + 19f, pPaint)
                    }
                }
            }
        }
        }

        // ==========================================
        // DETAILED 3D HUD CONTROLS OVERLAY
        // ==========================================

        // 1. TOP HEADER: Telemetry + Quick Controls
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live 3D Camera Telemetry Badge
            Surface(
                color = LabCardBg.copy(alpha = 0.88f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isSimulating) SignalHigh else Color(0xFFFFB300))
                    )
                    Text(
                        text = "3D • Yaw: ${yawDeg.roundToInt()}° • Pitch: ${pitchDeg.roundToInt()}° • ${(zoomScale * 100).roundToInt()}%",
                        color = LabTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top Right Controls Strip: [Detailed Controls] [Signal Flow] [Auto-Spin] [2D Grid]
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Detailed 3D Studio Controls Toggle Button
                Surface(
                    color = if (isDetailedControlsOpen) LabAccentBlue.copy(alpha = 0.25f) else LabCardBg.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDetailedControlsOpen) LabAccentBlue else LabBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isDetailedControlsOpen = !isDetailedControlsOpen }
                        .testTag("3d_detailed_controls_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Detailed 3D Controls",
                            tint = if (isDetailedControlsOpen) LabAccentBlue else LabTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isDetailedControlsOpen) "Controls ⚙" else "Controls",
                            color = if (isDetailedControlsOpen) LabAccentBlue else LabTextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Signal Flow Mode Toggle Button
                Surface(
                    color = if (isSignalFlowMode) SignalFlowHigh.copy(alpha = 0.22f) else LabCardBg.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSignalFlowMode) SignalFlowHigh else LabBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleSignalFlowMode?.invoke() }
                        .testTag("3d_signal_flow_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSignalFlowMode) SignalFlowHigh else LabTextTertiary)
                        )
                        Text(
                            text = if (isSignalFlowMode) "Flow ON" else "Flow",
                            color = if (isSignalFlowMode) SignalFlowHigh else LabTextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Auto-Spin Turntable Toggle Button
                Surface(
                    color = if (isAutoRotating) LabAccentBlue.copy(alpha = 0.25f) else LabCardBg.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAutoRotating) LabAccentBlue else LabBorder
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { isAutoRotating = !isAutoRotating }
                        .testTag("3d_auto_rotate_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MotionPhotosOn,
                            contentDescription = "Auto Spin",
                            tint = if (isAutoRotating) LabAccentBlue else LabTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isAutoRotating) "Spin ON" else "Spin",
                            color = if (isAutoRotating) LabAccentBlue else LabTextPrimary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // IC Trainer Kit vs PCB Lab 3D Mode Toggle Pill (Prominent Alternative Option in 3D)
                Surface(
                    color = LabCardBg.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.2.dp,
                        if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) Color(0xFFFFB300) else Color(0xFF00E676)
                    ),
                    modifier = Modifier.testTag("3d_board_style_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // PCB Lab Segment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (board3DStyle == Board3DStyle.PCB_LAB) Color(0xFF00E676).copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { if (board3DStyle != Board3DStyle.PCB_LAB) onToggleBoard3DStyle?.invoke() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("option_pcb_lab")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "⚡", fontSize = 9.sp)
                                Text(
                                    text = "PCB Lab",
                                    color = if (board3DStyle == Board3DStyle.PCB_LAB) Color(0xFF00E676) else LabTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (board3DStyle == Board3DStyle.PCB_LAB) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // Trainer Kit Segment
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) Color(0xFFFFB300).copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { if (board3DStyle != Board3DStyle.IC_TRAINER_KIT) onToggleBoard3DStyle?.invoke() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                .testTag("option_trainer_kit")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = "🎛️", fontSize = 9.sp)
                                Text(
                                    text = "Trainer Kit",
                                    color = if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) Color(0xFFFFB300) else LabTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Switch to 2D Grid Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LabAccentBlue)
                        .clickable { onSwitchTo2D() }
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                        .testTag("switch_to_2d_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                        Text(text = "2D Grid", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. TOP-CENTER CAMERA ANGLE PRESETS (ISO, TOP, FRONT, SIDE)
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 38.dp)
                .testTag("3d_view_presets"),
            color = LabPanelBg.copy(alpha = 0.92f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(2.5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CameraPreset3D.entries.forEach { preset ->
                    val isActive = kotlin.math.abs(yawDeg - preset.yaw) < 8f && kotlin.math.abs(pitchDeg - preset.pitch) < 8f
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (isActive) LabAccentBlue else Color.Transparent)
                            .clickable {
                                isAutoRotating = false
                                yawDeg = preset.yaw
                                pitchDeg = preset.pitch
                            }
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.label,
                            color = if (isActive) Color.White else LabTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3. BOTTOM-RIGHT DETAILED CAMERA CONTROLLER & GIZMO
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Touch Mode Switcher: [Orbit 3D 🔄] vs [Pan View ✋]
            Surface(
                color = LabCardBg.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
                modifier = Modifier.testTag("3d_touch_mode_btn")
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (touchMode == TouchMode3D.ORBIT) LabAccentBlue else Color.Transparent)
                            .clickable { touchMode = TouchMode3D.ORBIT }
                            .padding(horizontal = 6.dp, vertical = 3.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.RotateRight,
                                contentDescription = "Orbit",
                                tint = if (touchMode == TouchMode3D.ORBIT) Color.White else LabTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Orbit",
                                color = if (touchMode == TouchMode3D.ORBIT) Color.White else LabTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (touchMode == TouchMode3D.PAN) LabAccentBlue else Color.Transparent)
                            .clickable { touchMode = TouchMode3D.PAN }
                            .padding(horizontal = 6.dp, vertical = 3.5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PanTool,
                                contentDescription = "Pan",
                                tint = if (touchMode == TouchMode3D.PAN) Color.White else LabTextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Pan",
                                color = if (touchMode == TouchMode3D.PAN) Color.White else LabTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Directional 3D Navigation D-Pad & Camera Gizmo
            Surface(
                color = LabCardBg.copy(alpha = 0.94f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
                shadowElevation = 4.dp,
                modifier = Modifier.testTag("3d_dpad")
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Tilt Up
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LabDarkBg)
                            .clickable {
                                isAutoRotating = false
                                pitchDeg = (pitchDeg + 10f).coerceIn(12f, 85f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Tilt Up", tint = LabTextPrimary, modifier = Modifier.size(16.dp))
                    }

                    // Middle Row: Orbit Left - Center/Reset - Orbit Right
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Orbit Left
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LabDarkBg)
                                .clickable {
                                    isAutoRotating = false
                                    yawDeg = (yawDeg - 15f) % 360f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Orbit Left", tint = LabTextPrimary, modifier = Modifier.size(16.dp))
                        }

                        // Center: Reset to ISO view & center pan
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LabAccentBlue.copy(alpha = 0.25f))
                                .clickable {
                                    isAutoRotating = false
                                    yawDeg = -35f
                                    pitchDeg = 35f
                                    zoomScale = 1.05f
                                    panOffset = Offset.Zero
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Reset Camera", tint = LabAccentBlue, modifier = Modifier.size(12.dp))
                        }

                        // Orbit Right
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(LabDarkBg)
                                .clickable {
                                    isAutoRotating = false
                                    yawDeg = (yawDeg + 15f) % 360f
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Orbit Right", tint = LabTextPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Tilt Down
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LabDarkBg)
                            .clickable {
                                isAutoRotating = false
                                pitchDeg = (pitchDeg - 10f).coerceIn(12f, 85f)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Tilt Down", tint = LabTextPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Detailed Zoom Controls & Percentage Display
            Surface(
                color = LabCardBg.copy(alpha = 0.92f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
                modifier = Modifier.testTag("3d_zoom_controls")
            ) {
                Row(
                    modifier = Modifier.padding(2.5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Zoom Out
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LabDarkBg)
                            .clickable { zoomScale = (zoomScale / 1.15f).coerceAtLeast(0.35f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = LabTextPrimary, modifier = Modifier.size(13.dp))
                    }

                    Text(
                        text = "${(zoomScale * 100).roundToInt()}%",
                        color = LabTextPrimary,
                        fontSize = 9.5.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    // Zoom In
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(LabDarkBg)
                            .clickable { zoomScale = (zoomScale * 1.15f).coerceAtMost(3.5f) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = LabTextPrimary, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }

        // 4. BOTTOM-LEFT SIMULATION CONTROLS & PCB LAYER SETTINGS
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp, start = 8.dp),
            color = LabPanelBg.copy(alpha = 0.92f),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder)
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Simulation
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isSimulating) SignalHigh.copy(alpha = 0.2f) else LabCardBg)
                        .border(1.dp, if (isSimulating) SignalHigh else LabBorder, RoundedCornerShape(5.dp))
                        .clickable { onToggleSimulate() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSimulating) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Simulate",
                        tint = if (isSimulating) SignalHigh else Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Step Clock
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(LabCardBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(5.dp))
                        .clickable { onStepSimulate() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Step",
                        tint = LabTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Reset Circuit
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(LabCardBg)
                        .border(1.dp, LabBorder, RoundedCornerShape(5.dp))
                        .clickable { onResetSimulation() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        tint = LabTextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .width(1.dp)
                        .background(LabBorder)
                )

                // PCB Silkscreen Grid Toggle
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (showPcbGrid) LabAccentBlue.copy(alpha = 0.2f) else LabCardBg)
                        .border(1.dp, if (showPcbGrid) LabAccentBlue else LabBorder, RoundedCornerShape(5.dp))
                        .clickable { showPcbGrid = !showPcbGrid },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "PCB Grid",
                        tint = if (showPcbGrid) LabAccentBlue else LabTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 5. COMPREHENSIVE DETAILED 3D STUDIO CONTROLS MODAL / PANEL
        if (isDetailedControlsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { isDetailedControlsOpen = false }
            )
        }

        AnimatedVisibility(
            visible = isDetailedControlsOpen,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(14.dp)
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .fillMaxWidth(0.92f)
                    .heightIn(max = 440.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.2.dp, LabAccentBlue.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .testTag("detailed_3d_controls_panel"),
                color = LabPanelBg,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LabAccentBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = LabAccentBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "3D STUDIO CONTROLS",
                                    color = LabTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                                Text(
                                    text = "Camera, Shading & Geometry Inspector",
                                    color = LabTextSecondary,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LabDarkBg)
                                .clickable { isDetailedControlsOpen = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LabTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    // SECTION 1: Camera Presets
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "CAMERA ANGLE PRESETS",
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CameraPreset3D.entries.forEach { preset ->
                                val isActive = kotlin.math.abs(yawDeg - preset.yaw) < 8f && kotlin.math.abs(pitchDeg - preset.pitch) < 8f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isActive) LabAccentBlue else LabCardBg)
                                        .border(1.dp, if (isActive) LabAccentBlue else LabBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            isAutoRotating = false
                                            yawDeg = preset.yaw
                                            pitchDeg = preset.pitch
                                        }
                                        .padding(vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = preset.label,
                                        color = if (isActive) Color.White else LabTextSecondary,
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // SECTION 2: Fine Sliders (Yaw, Pitch, Zoom)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "VIEWPORT ORIENTATION & ZOOM",
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Yaw / Azimuth Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Azimuth (Yaw):", color = LabTextSecondary, fontSize = 10.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(3.dp))
                                            .clickable { yawDeg = (yawDeg - 15f) % 360f }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("-15°", color = LabTextSecondary, fontSize = 8.5.sp)
                                    }
                                    Text(
                                        text = "${yawDeg.roundToInt()}°",
                                        color = LabTextPrimary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(3.dp))
                                            .clickable { yawDeg = (yawDeg + 15f) % 360f }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("+15°", color = LabTextSecondary, fontSize = 8.5.sp)
                                    }
                                }
                            }
                            Slider(
                                value = yawDeg,
                                onValueChange = {
                                    isAutoRotating = false
                                    yawDeg = it
                                },
                                valueRange = -180f..180f,
                                colors = SliderDefaults.colors(
                                    thumbColor = LabAccentBlue,
                                    activeTrackColor = LabAccentBlue,
                                    inactiveTrackColor = LabDarkBg
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }

                        // Pitch / Elevation Slider
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Elevation (Pitch):", color = LabTextSecondary, fontSize = 10.sp)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(3.dp))
                                            .clickable { pitchDeg = (pitchDeg - 10f).coerceIn(12f, 85f) }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("-10°", color = LabTextSecondary, fontSize = 8.5.sp)
                                    }
                                    Text(
                                        text = "${pitchDeg.roundToInt()}°",
                                        color = LabTextPrimary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(3.dp))
                                            .clickable { pitchDeg = (pitchDeg + 10f).coerceIn(12f, 85f) }
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("+10°", color = LabTextSecondary, fontSize = 8.5.sp)
                                    }
                                }
                            }
                            Slider(
                                value = pitchDeg,
                                onValueChange = {
                                    isAutoRotating = false
                                    pitchDeg = it
                                },
                                valueRange = 12f..85f,
                                colors = SliderDefaults.colors(
                                    thumbColor = LabAccentBlue,
                                    activeTrackColor = LabAccentBlue,
                                    inactiveTrackColor = LabDarkBg
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                        }

                        // Zoom Slider & Quick Zoom Presets
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Camera Zoom:", color = LabTextSecondary, fontSize = 10.sp)
                                Text(
                                    text = "${(zoomScale * 100).roundToInt()}%",
                                    color = LabTextPrimary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = zoomScale,
                                onValueChange = { zoomScale = it },
                                valueRange = 0.35f..3.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = LabAccentBlue,
                                    activeTrackColor = LabAccentBlue,
                                    inactiveTrackColor = LabDarkBg
                                ),
                                modifier = Modifier.height(20.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0.5f to "50%", 1.0f to "100%", 1.5f to "150%").forEach { (scale, lbl) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                                            .clickable { zoomScale = scale }
                                            .padding(vertical = 3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = lbl, color = LabTextSecondary, fontSize = 8.5.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LabAccentBlue.copy(alpha = 0.2f))
                                        .border(1.dp, LabAccentBlue, RoundedCornerShape(4.dp))
                                        .clickable {
                                            zoomScale = 1.05f
                                            panOffset = Offset.Zero
                                            yawDeg = -35f
                                            pitchDeg = 35f
                                        }
                                        .padding(vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Reset View", color = LabAccentBlue, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // SECTION 3: Signal Flow & Wire 3D Elevation
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "SIGNAL FLOW & 3D WIRE STANDOFF",
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        // Signal Flow Mode Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(LabCardBg)
                                .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Signal Flow Coloring",
                                    color = LabTextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SignalFlowHigh))
                                    Text(text = "1=High", color = SignalFlowHigh, fontSize = 8.5.sp)
                                    Text(text = "•", color = LabTextTertiary, fontSize = 8.5.sp)
                                    Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(SignalFlowLow))
                                    Text(text = "0=Off", color = LabTextSecondary, fontSize = 8.5.sp)
                                }
                            }
                            Switch(
                                checked = isSignalFlowMode,
                                onCheckedChange = { onToggleSignalFlowMode?.invoke() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = SignalFlowHigh,
                                    uncheckedThumbColor = LabTextTertiary,
                                    uncheckedTrackColor = LabDarkBg
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Wire 3D Standoff Elevation
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(text = "Wire Standoff Arch Height:", color = LabTextSecondary, fontSize = 10.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(4f to "Flat (4dp)", 10f to "Normal (10dp)", 18f to "High Arch (18dp)").forEach { (lvl, lbl) ->
                                    val isSelected = wireElevationLevel == lvl
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (isSelected) LabAccentBlue else LabCardBg)
                                            .border(1.dp, if (isSelected) LabAccentBlue else LabBorder, RoundedCornerShape(5.dp))
                                            .clickable { wireElevationLevel = lvl }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = lbl,
                                            color = if (isSelected) Color.White else LabTextSecondary,
                                            fontSize = 8.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 4: Turntable / Auto-Spin Controls
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "360° TURNTABLE AUTO-SPIN",
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(LabCardBg)
                                .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Continuous Auto-Rotation",
                                color = LabTextPrimary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Switch(
                                checked = isAutoRotating,
                                onCheckedChange = { isAutoRotating = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = LabAccentBlue,
                                    uncheckedThumbColor = LabTextTertiary,
                                    uncheckedTrackColor = LabDarkBg
                                ),
                                modifier = Modifier.height(24.dp)
                            )
                        }

                        // Speed and Direction
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf(0.2f to "0.5x Slow", 0.4f to "1.0x Normal", 0.9f to "2.0x Fast").forEach { (spd, lbl) ->
                                val isSelected = autoRotateSpeed == spd
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) LabAccentBlue.copy(alpha = 0.25f) else LabCardBg)
                                        .border(1.dp, if (isSelected) LabAccentBlue else LabBorder, RoundedCornerShape(4.dp))
                                        .clickable { autoRotateSpeed = spd }
                                        .padding(vertical = 3.5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = lbl,
                                        color = if (isSelected) LabAccentBlue else LabTextSecondary,
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LabCardBg)
                                    .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                                    .clickable { autoRotateClockwise = !autoRotateClockwise }
                                    .padding(vertical = 3.5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (autoRotateClockwise) "Dir: CW ↻" else "Dir: CCW ↺",
                                    color = LabTextPrimary,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // SECTION 5: 3D Layer Visibility Toggles
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = "3D LAYER VISIBILITY",
                            color = LabAccentBlue,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Silkscreen Grid Toggle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (showPcbGrid) LabAccentBlue.copy(alpha = 0.2f) else LabCardBg)
                                    .border(1.dp, if (showPcbGrid) LabAccentBlue else LabBorder, RoundedCornerShape(5.dp))
                                    .clickable { showPcbGrid = !showPcbGrid }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (showPcbGrid) "Grid: ON" else "Grid: OFF",
                                    color = if (showPcbGrid) LabAccentBlue else LabTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Substrate Toggle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (showPcbSubstrate) LabAccentBlue.copy(alpha = 0.2f) else LabCardBg)
                                    .border(1.dp, if (showPcbSubstrate) LabAccentBlue else LabBorder, RoundedCornerShape(5.dp))
                                    .clickable { showPcbSubstrate = !showPcbSubstrate }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (showPcbSubstrate) "PCB: ON" else "PCB: OFF",
                                    color = if (showPcbSubstrate) LabAccentBlue else LabTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Shadows Toggle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(if (showDropShadows) LabAccentBlue.copy(alpha = 0.2f) else LabCardBg)
                                    .border(1.dp, if (showDropShadows) LabAccentBlue else LabBorder, RoundedCornerShape(5.dp))
                                    .clickable { showDropShadows = !showDropShadows }
                                    .padding(vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (showDropShadows) "Shadows: ON" else "Shadows: OFF",
                                    color = if (showDropShadows) LabAccentBlue else LabTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Done Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(7.dp))
                            .background(LabAccentBlue)
                            .clickable { isDetailedControlsOpen = false }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Apply & Return to 3D View",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
