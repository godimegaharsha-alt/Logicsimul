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

    val coroutineScope = rememberCoroutineScope()
    val trainerSwitchLocalStates = remember { mutableStateMapOf<Int, Boolean>() }
    var isPulserPressed by remember { mutableStateOf(false) }

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

                                    // 1. Check all 12 toggle switches in INPUT SECTION (S12..S1)
                                    val swCount = 12
                                    val inputX0 = bx0 + 12f
                                    val inputX1 = bx0 + bw * 0.58f
                                    val swSpacing = (inputX1 - inputX0) / (swCount + 1)
                                    val swY = by1 - 30f
                                    val circuitSwitches = currentComponents.filter { it.type == "SWITCH" }

                                    for (i in 0 until swCount) {
                                        val sx = inputX0 + (i + 1) * swSpacing
                                        val rX = (sx - boardCenter.x) * cosY - (swY - boardCenter.y) * sinY
                                        val rY = (sx - boardCenter.x) * sinY + (swY - boardCenter.y) * cosY
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

                                    // 2. Check Pulser Pushbutton in Bottom-Right
                                    if (!handledTrainerTap) {
                                        val pulserX = bx0 + bw * 0.925f
                                        val pulserY = by1 - 36f
                                        val rX = (pulserX - boardCenter.x) * cosY - (pulserY - boardCenter.y) * sinY
                                        val rY = (pulserX - boardCenter.x) * sinY + (pulserY - boardCenter.y) * cosY
                                        val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                        val projY = screenCenter.y + panOffset.y + (rY * sinP - 10f * cos(pitchRad)) * zoomScale
                                        val dist = (startScreenPos - Offset(projX, projY)).getDistance()
                                        if (dist <= 28f * zoomScale.coerceIn(0.7f, 1.8f)) {
                                            isPulserPressed = true
                                            coroutineScope.launch {
                                                delay(180L)
                                                isPulserPressed = false
                                            }
                                            val clkComp = currentComponents.find { it.type == "CLOCK" || it.type == "OSCILLATOR" }
                                            if (clkComp != null) {
                                                currentOnToggle(clkComp.id)
                                            }
                                            AudioEngine.playClickSound()
                                            handledTrainerTap = true
                                        }
                                    }

                                    // 3. Check 5 IC Bays (U1..U5)
                                    if (!handledTrainerTap) {
                                        val logicComps = currentComponents.filter { it.type != "SWITCH" && it.type != "LED" && it.type != "DIGITAL_PROBE" }
                                        val bayAreaY0 = by0 + 100f
                                        val bayAreaY1 = by1 - 92f
                                        val bayW = (bw - 20f) / 5f
                                        for (bIdx in 0 until 5) {
                                            val bX = bx0 + 10f + bIdx * bayW + bayW / 2f
                                            val bY = (bayAreaY0 + bayAreaY1) / 2f
                                            val rX = (bX - boardCenter.x) * cosY - (bY - boardCenter.y) * sinY
                                            val rY = (bX - boardCenter.x) * sinY + (bY - boardCenter.y) * cosY
                                            val projX = screenCenter.x + panOffset.x + rX * zoomScale
                                            val projY = screenCenter.y + panOffset.y + (rY * sinP - 10f * cos(pitchRad)) * zoomScale
                                            val dist = (startScreenPos - Offset(projX, projY)).getDistance()
                                            if (dist <= 38f * zoomScale.coerceIn(0.7f, 1.8f)) {
                                                val comp = logicComps.getOrNull(bIdx)
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
                // AUTHENTIC DIGITAL IC TRAINER KIT APPARATUS IN WOODEN BRIEFCASE (FROM REFERENCE IMAGE)
                // =========================================================================================
                val bw = bx1 - bx0
                val bh = by1 - by0
                val woodMargin = 26f
                val wx0 = bx0 - woodMargin
                val wy0 = by0 - woodMargin
                val wx1 = bx1 + woodMargin
                val wy1 = by1 + woodMargin

                // 1. Wooden Briefcase Base Box (Solid Timber Chassis)
                val wb0 = project3D(wx0, wy0, -22f)
                val wb1 = project3D(wx1, wy0, -22f)
                val wb2 = project3D(wx1, wy1, -22f)
                val wb3 = project3D(wx0, wy1, -22f)

                val wt0 = project3D(wx0, wy0, 0f)
                val wt1 = project3D(wx1, wy0, 0f)
                val wt2 = project3D(wx1, wy1, 0f)
                val wt3 = project3D(wx0, wy1, 0f)

                // Suitcase Drop Shadow
                if (showDropShadows) {
                    drawPath(
                        path = Path().apply {
                            moveTo(wb0.x, wb0.y + 16f)
                            lineTo(wb1.x, wb1.y + 16f)
                            lineTo(wb2.x, wb2.y + 16f)
                            lineTo(wb3.x, wb3.y + 16f)
                            close()
                        },
                        color = Color.Black.copy(alpha = 0.55f)
                    )
                }

                // Front Wooden Wall of Suitcase
                drawPath(
                    path = Path().apply {
                        moveTo(wt3.x, wt3.y)
                        lineTo(wt2.x, wt2.y)
                        lineTo(wb2.x, wb2.y)
                        lineTo(wb3.x, wb3.y)
                        close()
                    },
                    color = Color(0xFFC08344) // Warm pine wood
                )
                drawPath(
                    path = Path().apply {
                        moveTo(wt3.x, wt3.y)
                        lineTo(wt2.x, wt2.y)
                        lineTo(wb2.x, wb2.y)
                        lineTo(wb3.x, wb3.y)
                        close()
                    },
                    color = Color(0xFF825220),
                    style = Stroke(1.5f)
                )

                // Right Wooden Wall of Suitcase
                drawPath(
                    path = Path().apply {
                        moveTo(wt1.x, wt1.y)
                        lineTo(wt2.x, wt2.y)
                        lineTo(wb2.x, wb2.y)
                        lineTo(wb1.x, wb1.y)
                        close()
                    },
                    color = Color(0xFF8E5624) // Shadow wood side
                )

                // Top Wooden Bevel Framing the Faceplate
                drawPath(
                    path = Path().apply {
                        moveTo(wt0.x, wt0.y)
                        lineTo(wt1.x, wt1.y)
                        lineTo(wt2.x, wt2.y)
                        lineTo(wt3.x, wt3.y)
                        close()
                    },
                    color = Color(0xFFCFA163)
                )

                // 2. Open Wooden Suitcase Lid with Deep Green Felt/Velvet Lining (Angled Up & Back)
                val lt0 = project3D(wx0, wy0, 0f)
                val lt1 = project3D(wx1, wy0, 0f)
                val ltTop0 = project3D(wx0, wy0 - 130f, 155f)
                val ltTop1 = project3D(wx1, wy0 - 130f, 155f)

                // Outer Wooden Lid Shell
                drawPath(
                    path = Path().apply {
                        moveTo(lt0.x, lt0.y)
                        lineTo(lt1.x, lt1.y)
                        lineTo(ltTop1.x, ltTop1.y)
                        lineTo(ltTop0.x, ltTop0.y)
                        close()
                    },
                    color = Color(0xFFB57A3D)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(lt0.x, lt0.y)
                        lineTo(lt1.x, lt1.y)
                        lineTo(ltTop1.x, ltTop1.y)
                        lineTo(ltTop0.x, ltTop0.y)
                        close()
                    },
                    color = Color(0xFF7A4A1C),
                    style = Stroke(1.8f)
                )

                // Inner Recessed Deep Green Velvet/Felt Lining in the Lid
                val lidIn0 = project3D(wx0 + 10f, wy0 - 5f, 3f)
                val lidIn1 = project3D(wx1 - 10f, wy0 - 5f, 3f)
                val lidInTop1 = project3D(wx1 - 10f, wy0 - 124f, 150f)
                val lidInTop0 = project3D(wx0 + 10f, wy0 - 124f, 150f)

                drawPath(
                    path = Path().apply {
                        moveTo(lidIn0.x, lidIn0.y)
                        lineTo(lidIn1.x, lidIn1.y)
                        lineTo(lidInTop1.x, lidInTop1.y)
                        lineTo(lidInTop0.x, lidInTop0.y)
                        close()
                    },
                    color = Color(0xFF0C3825) // Authentic green velvet lining
                )
                drawPath(
                    path = Path().apply {
                        moveTo(lidIn0.x, lidIn0.y)
                        lineTo(lidIn1.x, lidIn1.y)
                        lineTo(lidInTop1.x, lidInTop1.y)
                        lineTo(lidInTop0.x, lidInTop0.y)
                        close()
                    },
                    color = Color(0xFF051C13),
                    style = Stroke(1.2f)
                )

                // Two Brass Hinges along Top Wooden Edge
                val h1 = project3D(wx0 + (wx1 - wx0) * 0.22f, wy0, 0.5f)
                val h2 = project3D(wx0 + (wx1 - wx0) * 0.78f, wy0, 0.5f)
                drawCircle(color = Color(0xFFD4AF37), radius = 5.5f, center = h1)
                drawCircle(color = Color(0xFFD4AF37), radius = 5.5f, center = h2)
                drawCircle(color = Color(0xFF422C0A), radius = 1.6f, center = h1)
                drawCircle(color = Color(0xFF422C0A), radius = 1.6f, center = h2)

                // Front Black Textured Carrying Handle on Front Wooden Wall
                val handleCenter = project3D(boardCenter.x, wy1 + 4f, -11f)
                val hMountL = project3D(boardCenter.x - 24f, wy1 + 2f, -11f)
                val hMountR = project3D(boardCenter.x + 24f, wy1 + 2f, -11f)
                drawCircle(color = Color(0xFFCFD8DC), radius = 4f, center = hMountL)
                drawCircle(color = Color(0xFFCFD8DC), radius = 4f, center = hMountR)
                drawLine(color = Color(0xFF1E2124), start = hMountL, end = hMountR, strokeWidth = 5.2f, cap = StrokeCap.Round)
                drawLine(color = Color(0xFF424242), start = hMountL, end = hMountR, strokeWidth = 2.4f, cap = StrokeCap.Round)

                // 3. Main Industrial Deep Green PCB Faceplate
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFF0E4A35) // Authentic trainer kit green soldermask
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
                    style = Stroke(2.2f)
                )

                // 4 Corner Chrome Mounting Screws & Standoffs
                val screwPositions = listOf(
                    Offset(bx0 + 12f, by0 + 12f),
                    Offset(bx1 - 12f, by0 + 12f),
                    Offset(bx0 + 12f, by1 - 12f),
                    Offset(bx1 - 12f, by1 - 12f),
                    Offset(boardCenter.x, by1 - 12f),
                    Offset(boardCenter.x, by0 + 12f)
                )
                for (sc in screwPositions) {
                    val sProj = project3D(sc.x, sc.y, 1f)
                    drawCircle(color = Color(0xFFCFD8DC), radius = 4.6f, center = sProj)
                    drawCircle(color = Color(0xFF78909C), radius = 3.6f, center = sProj)
                    drawLine(color = Color(0xFF263238), start = Offset(sProj.x - 2.5f, sProj.y), end = Offset(sProj.x + 2.5f, sProj.y), strokeWidth = 1f)
                    drawLine(color = Color(0xFF263238), start = Offset(sProj.x, sProj.y - 2.5f), end = Offset(sProj.x, sProj.y + 2.5f), strokeWidth = 1f)
                }

                // =========================================================================================
                // SECTION A: TOP ROW (OUTPUT SECTION + 7 SEGMENT DISPLAY SECTION)
                // =========================================================================================
                val topY0 = by0 + 12f
                val topY1 = by0 + 72f
                val splitX = bx0 + bw * 0.60f

                // --- A1. OUTPUT SECTION (Left) ---
                val outBoxP0 = project3D(bx0 + 10f, topY0, 0.6f)
                val outBoxP1 = project3D(splitX - 6f, topY0, 0.6f)
                val outBoxP2 = project3D(splitX - 6f, topY1, 0.6f)
                val outBoxP3 = project3D(bx0 + 10f, topY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(outBoxP0.x, outBoxP0.y)
                        lineTo(outBoxP1.x, outBoxP1.y)
                        lineTo(outBoxP2.x, outBoxP2.y)
                        lineTo(outBoxP3.x, outBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val outTitlePos = project3D((bx0 + 10f + splitX - 6f) / 2f, topY0 + 9f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("OUTPUT SECTION", outTitlePos.x, outTitlePos.y, trainerWhiteHeaderPaint)

                // 12 Output LED Indicators + 12 Output Terminal Sockets (L12..L1, OP12..OP1)
                val ledCount = 12
                val ledX0 = bx0 + 20f
                val ledX1 = splitX - 16f
                val ledSpacing = (ledX1 - ledX0) / (ledCount - 1)
                val circuitOutputs = currentComponents.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }

                for (i in 0 until ledCount) {
                    val lx = ledX0 + i * ledSpacing
                    val ledY = topY0 + 24f
                    val sockY = topY0 + 46f
                    val outLabel = "L${ledCount - i}"
                    val sockLabel = "OP${ledCount - i}"

                    val mappedOutput = circuitOutputs.getOrNull(i)
                    val isHigh = when {
                        mappedOutput != null -> (mappedOutput.inputValues["IN"] ?: mappedOutput.outputValues["OUT"] ?: DigitalState.LOW) == DigitalState.HIGH
                        else -> false
                    }

                    // Output LED Diode
                    val ledCenter = project3D(lx, ledY, 2.5f)
                    val domeCenter = project3D(lx, ledY, 6.5f)
                    drawCircle(color = Color(0xFF212529), radius = 5.8f, center = ledCenter)
                    drawCircle(color = Color(0xFFB0BEC5), radius = 4.8f, center = ledCenter, style = Stroke(1f))

                    if (isHigh) {
                        // Brilliant glowing red photonic emission bloom
                        drawCircle(color = Color(0xFFFF1744).copy(alpha = 0.25f), radius = 15f, center = domeCenter)
                        drawCircle(color = Color(0xFFFF1744).copy(alpha = 0.65f), radius = 8f, center = domeCenter)
                        drawCircle(color = Color(0xFFFF1744), radius = 4.2f, center = domeCenter)
                        drawCircle(color = Color(0xFFFFEBEE), radius = 1.8f, center = domeCenter)
                        drawCircle(color = Color.White, radius = 0.9f, center = Offset(domeCenter.x - 1f, domeCenter.y - 1f))
                    } else {
                        // Deep dark unlit ruby
                        drawCircle(color = Color(0xFF450A12), radius = 4.2f, center = domeCenter)
                        drawCircle(color = Color.White.copy(alpha = 0.25f), radius = 1.1f, center = Offset(domeCenter.x - 1f, domeCenter.y - 1f))
                    }

                    // Terminal Banana Socket OP12..OP1
                    val sockCenter = project3D(lx, sockY, 1.5f)
                    drawCircle(color = Color.White, radius = 5f, center = sockCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFB0BEC5), radius = 4f, center = sockCenter)
                    drawCircle(color = Color(0xFF1A1A1A), radius = 2.2f, center = sockCenter)

                    // Text labels
                    val lblPos = project3D(lx, topY0 + 33f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(outLabel, lblPos.x, lblPos.y, trainerPinNumPaint)
                    val sLblPos = project3D(lx, topY0 + 55f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(sockLabel, sLblPos.x, sLblPos.y, trainerPinNumPaint)
                }

                // --- A2. 7 SEGMENT DISPLAY SECTION (Right) ---
                val segBoxP0 = project3D(splitX + 6f, topY0, 0.6f)
                val segBoxP1 = project3D(bx1 - 10f, topY0, 0.6f)
                val segBoxP2 = project3D(bx1 - 10f, topY1, 0.6f)
                val segBoxP3 = project3D(splitX + 6f, topY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(segBoxP0.x, segBoxP0.y)
                        lineTo(segBoxP1.x, segBoxP1.y)
                        lineTo(segBoxP2.x, segBoxP2.y)
                        lineTo(segBoxP3.x, segBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val segTitlePos = project3D((splitX + 6f + bx1 - 10f) / 2f, topY0 + 9f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("7 SEGMENT DISPLAY SECTION", segTitlePos.x, segTitlePos.y, trainerWhiteHeaderPaint)

                // Two Dual 7-Segment Modules (Digit 1 and Digit 2)
                val segWidth = (bx1 - 10f - (splitX + 6f))
                val d1X = splitX + 6f + segWidth * 0.32f
                val d2X = splitX + 6f + segWidth * 0.72f
                val segCenters = listOf(d1X, d2X)

                for ((dIdx, dcX) in segCenters.withIndex()) {
                    val sP0 = project3D(dcX - 16f, topY0 + 16f, 3f)
                    val sP1 = project3D(dcX + 16f, topY0 + 16f, 3f)
                    val sP2 = project3D(dcX + 16f, topY0 + 44f, 3f)
                    val sP3 = project3D(dcX - 16f, topY0 + 44f, 3f)

                    // Gray package bezel
                    drawPath(
                        path = Path().apply {
                            moveTo(sP0.x, sP0.y)
                            lineTo(sP1.x, sP1.y)
                            lineTo(sP2.x, sP2.y)
                            lineTo(sP3.x, sP3.y)
                            close()
                        },
                        color = Color(0xFF263238)
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(sP0.x, sP0.y)
                            lineTo(sP1.x, sP1.y)
                            lineTo(sP2.x, sP2.y)
                            lineTo(sP3.x, sP3.y)
                            close()
                        },
                        color = Color(0xFF90A4AE),
                        style = Stroke(1f)
                    )

                    // 7 Segments (Displaying active digits or test patterns)
                    val segCenter = project3D(dcX, topY0 + 30f, 4f)
                    // Lit red segments
                    drawCircle(color = Color(0xFFFF1744).copy(alpha = 0.2f), radius = 10f, center = segCenter)
                    // Digits "8" silhouette with lit red LED bars
                    val segBarColor = if (dIdx == 0) Color(0xFFFF1744) else Color(0xFFFF1744).copy(alpha = 0.85f)
                    val pTop = project3D(dcX, topY0 + 20f, 4f)
                    val pMid = project3D(dcX, topY0 + 30f, 4f)
                    val pBot = project3D(dcX, topY0 + 40f, 4f)
                    drawLine(color = segBarColor, start = Offset(pTop.x - 7f, pTop.y), end = Offset(pTop.x + 7f, pTop.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pMid.x - 6f, pMid.y), end = Offset(pMid.x + 6f, pMid.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pBot.x - 7f, pBot.y), end = Offset(pBot.x + 7f, pBot.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pTop.x - 7f, pTop.y), end = Offset(pMid.x - 6f, pMid.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pTop.x + 7f, pTop.y), end = Offset(pMid.x + 6f, pMid.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pMid.x - 6f, pMid.y), end = Offset(pBot.x - 7f, pBot.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    drawLine(color = segBarColor, start = Offset(pMid.x + 6f, pMid.y), end = Offset(pBot.x + 7f, pBot.y), strokeWidth = 2.4f, cap = StrokeCap.Round)
                    // Decimal point DP
                    drawCircle(color = segBarColor, radius = 1.6f, center = Offset(pBot.x + 9f, pBot.y))

                    // Breakout terminal sockets below digit (a, b, c, d, e, f, g, dp)
                    val pins = listOf("a", "b", "c", "d")
                    for ((pIdx, pName) in pins.withIndex()) {
                        val px = dcX - 10f + pIdx * 7f
                        val sCenter = project3D(px, topY0 + 52f, 1.5f)
                        drawCircle(color = Color.White, radius = 3.2f, center = sCenter, style = Stroke(0.6f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 2.4f, center = sCenter)
                        drawCircle(color = Color(0xFF111111), radius = 1.2f, center = sCenter)
                        val pLbl = project3D(px, topY0 + 60f, 0.8f)
                        drawContext.canvas.nativeCanvas.drawText(pName, pLbl.x, pLbl.y, trainerPinNumPaint)
                    }
                }

                // =========================================================================================
                // SECTION B: UPPER POWER DISTRIBUTION RAIL (VCC [+])
                // =========================================================================================
                val vccRailY = by0 + 82f
                val vccLineP0 = project3D(bx0 + 14f, vccRailY, 0.5f)
                val vccLineP1 = project3D(bx1 - 14f, vccRailY, 0.5f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = vccLineP0, end = vccLineP1, strokeWidth = 1.4f)

                // 4 VCC Sockets across the bays
                for (pIdx in 0..3) {
                    val vx = bx0 + 60f + pIdx * ((bw - 120f) / 3f)
                    val vccCenter = project3D(vx, vccRailY, 2f)
                    drawCircle(color = Color(0xFFD32F2F), radius = 6.2f, center = vccCenter) // Red VCC ring
                    drawCircle(color = Color(0xFFFFCDD2), radius = 4.6f, center = vccCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFF1A1A1A), radius = 2.4f, center = vccCenter)
                    val vLbl = project3D(vx - 16f, vccRailY + 3f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("VCC", vLbl.x, vLbl.y, trainerWhiteSubPaint)
                    val plusLbl = project3D(vx + 16f, vccRailY + 3f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("+", plusLbl.x, plusLbl.y, trainerWhiteSubPaint)
                }

                // =========================================================================================
                // SECTION C: 5 IC BAYS (U1, U2, U3, U4, U5) WITH CYAN ZIF SOCKETS
                // =========================================================================================
                val bayY0 = by0 + 94f
                val bayY1 = by1 - 88f
                val bayCount = 5
                val bayW = (bw - 20f) / bayCount
                val logicComps = currentComponents.filter { it.type != "SWITCH" && it.type != "LED" && it.type != "DIGITAL_PROBE" }

                // Default IC part labels for the 5 bays (reflecting real educational trainer apparatus)
                val defaultPartNames = listOf("7408 (AND)", "7432 (OR)", "7404 (NOT)", "7400 (NAND)", "7486 (XOR)")

                for (bIdx in 0 until bayCount) {
                    val bayLeft = bx0 + 10f + bIdx * bayW
                    val bayRight = bayLeft + bayW
                    val bayCenter = (bayLeft + bayRight) / 2f

                    // White Silkscreen Divider Line between bays
                    if (bIdx > 0) {
                        val d0 = project3D(bayLeft, bayY0, 0.5f)
                        val d1 = project3D(bayLeft, bayY1, 0.5f)
                        drawLine(color = Color.White.copy(alpha = 0.85f), start = d0, end = d1, strokeWidth = 1.2f)
                    }

                    // Silkscreen Bay Identifier: U1, U2, U3, U4, U5
                    val bayLabelPos = project3D(bayCenter, bayY0 + 12f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("U${bIdx + 1}", bayLabelPos.x, bayLabelPos.y, trainerWhiteHeaderPaint)

                    // 1. Signature Cyan/Teal (Turquoise) 16-Pin ZIF Socket (Zero Insertion Force)
                    val zifW = bayW * 0.40f
                    val zifH = (bayY1 - bayY0) * 0.68f
                    val zifY = (bayY0 + bayY1) / 2f + 4f
                    val zifElev = 4f

                    val zP0 = project3D(bayCenter - zifW / 2f, zifY - zifH / 2f, zifElev)
                    val zP1 = project3D(bayCenter + zifW / 2f, zifY - zifH / 2f, zifElev)
                    val zP2 = project3D(bayCenter + zifW / 2f, zifY + zifH / 2f, zifElev)
                    val zP3 = project3D(bayCenter - zifW / 2f, zifY + zifH / 2f, zifElev)

                    // ZIF Socket Body in Cyan/Turquoise
                    drawPath(
                        path = Path().apply {
                            moveTo(zP0.x, zP0.y)
                            lineTo(zP1.x, zP1.y)
                            lineTo(zP2.x, zP2.y)
                            lineTo(zP3.x, zP3.y)
                            close()
                        },
                        color = Color(0xFF009688) // Real ZIF socket cyan/turquoise
                    )
                    drawPath(
                        path = Path().apply {
                            moveTo(zP0.x, zP0.y)
                            lineTo(zP1.x, zP1.y)
                            lineTo(zP2.x, zP2.y)
                            lineTo(zP3.x, zP3.y)
                            close()
                        },
                        color = Color(0xFF80CBC4),
                        style = Stroke(1.2f)
                    )

                    // Silver ZIF Locking Lever at Top-Left of the Socket
                    val leverBase = project3D(bayCenter - zifW / 2f - 2f, zifY - zifH / 2f + 6f, zifElev + 2f)
                    val leverTip = project3D(bayCenter - zifW / 2f - 9f, zifY - zifH / 2f - 14f, zifElev + 14f)
                    drawLine(color = Color(0xFFCFD8DC), start = leverBase, end = leverTip, strokeWidth = 3f, cap = StrokeCap.Round)
                    drawLine(color = Color.White, start = leverBase, end = leverTip, strokeWidth = 1f, cap = StrokeCap.Round)
                    drawCircle(color = Color(0xFFECEFF1), radius = 3.4f, center = leverTip)

                    // 16 Dual In-Line Socket Contact Slots (8 on left, 8 on right)
                    val slotCount = 8
                    val slotSpacing = zifH / (slotCount + 1)
                    for (sIdx in 0 until slotCount) {
                        val sy = zifY - zifH / 2f + (sIdx + 1) * slotSpacing
                        val slCenter = project3D(bayCenter - zifW * 0.32f, sy, zifElev + 0.5f)
                        val srCenter = project3D(bayCenter + zifW * 0.32f, sy, zifElev + 0.5f)
                        drawCircle(color = Color(0xFF004D40), radius = 1.8f, center = slCenter)
                        drawCircle(color = Color(0xFF004D40), radius = 1.8f, center = srCenter)
                    }

                    // 2. Seated DIP IC Chip inside the ZIF Socket
                    val icW = zifW * 0.72f
                    val icH = zifH * 0.88f
                    val icElev = zifElev + 7f
                    val icP0 = project3D(bayCenter - icW / 2f, zifY - icH / 2f, icElev)
                    val icP1 = project3D(bayCenter + icW / 2f, zifY - icH / 2f, icElev)
                    val icP2 = project3D(bayCenter + icW / 2f, zifY + icH / 2f, icElev)
                    val icP3 = project3D(bayCenter - icW / 2f, zifY + icH / 2f, icElev)

                    // DIP Molded Black Body
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
                        color = Color(0xFF37474F),
                        style = Stroke(1f)
                    )

                    // Pin 1 Index Notch at Top of Chip
                    val notchCenter = project3D(bayCenter, zifY - icH / 2f, icElev)
                    drawCircle(color = Color(0xFF111111), radius = 3f, center = notchCenter)

                    // Laser-etched Part Markings on the IC
                    val mappedLogic = logicComps.getOrNull(bIdx)
                    val chipPartText = mappedLogic?.label?.ifBlank { null } ?: mappedLogic?.type?.let { "$it GATE" } ?: defaultPartNames[bIdx]
                    val textCenter = project3D(bayCenter, zifY, icElev + 1f)
                    drawContext.canvas.nativeCanvas.drawText(chipPartText, textCenter.x, textCenter.y + 2f, trainerChipPaint)

                    // 3. Pin Breakout Terminal Sockets (Pins 1..8 on Left, Pins 16..9 on Right)
                    val pinLeftX = bayCenter - zifW / 2f - 14f
                    val pinRightX = bayCenter + zifW / 2f + 14f

                    for (pIdx in 0 until slotCount) {
                        val py = zifY - zifH / 2f + (pIdx + 1) * slotSpacing
                        val pinNumLeft = "${pIdx + 1}"
                        val pinNumRight = "${16 - pIdx}"

                        // Left Pin Socket
                        val sockL = project3D(pinLeftX, py, 1.5f)
                        drawCircle(color = Color.White, radius = 4f, center = sockL, style = Stroke(0.8f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 3f, center = sockL)
                        drawCircle(color = Color(0xFF111111), radius = 1.6f, center = sockL)
                        val numPosL = project3D(pinLeftX - 7f, py + 2f, 0.8f)
                        drawContext.canvas.nativeCanvas.drawText(pinNumLeft, numPosL.x, numPosL.y, trainerPinNumPaint)

                        // Right Pin Socket
                        val sockR = project3D(pinRightX, py, 1.5f)
                        drawCircle(color = Color.White, radius = 4f, center = sockR, style = Stroke(0.8f))
                        drawCircle(color = Color(0xFFCFD8DC), radius = 3f, center = sockR)
                        drawCircle(color = Color(0xFF111111), radius = 1.6f, center = sockR)
                        val numPosR = project3D(pinRightX + 7f, py + 2f, 0.8f)
                        drawContext.canvas.nativeCanvas.drawText(pinNumRight, numPosR.x, numPosR.y, trainerPinNumPaint)
                    }
                }

                // =========================================================================================
                // SECTION D: LOWER POWER DISTRIBUTION RAIL (GND [-])
                // =========================================================================================
                val gndRailY = by1 - 80f
                val gndLineP0 = project3D(bx0 + 14f, gndRailY, 0.5f)
                val gndLineP1 = project3D(bx1 - 14f, gndRailY, 0.5f)
                drawLine(color = Color.White.copy(alpha = 0.85f), start = gndLineP0, end = gndLineP1, strokeWidth = 1.4f)

                // 4 GND Sockets across the bays
                for (pIdx in 0..3) {
                    val gx = bx0 + 60f + pIdx * ((bw - 120f) / 3f)
                    val gndCenter = project3D(gx, gndRailY, 2f)
                    drawCircle(color = Color(0xFF212121), radius = 6.2f, center = gndCenter) // Black GND ring
                    drawCircle(color = Color(0xFF757575), radius = 4.6f, center = gndCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFCFD8DC), radius = 2.4f, center = gndCenter)
                    val gLbl = project3D(gx - 16f, gndRailY + 3f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("GND", gLbl.x, gLbl.y, trainerWhiteSubPaint)
                    val minusLbl = project3D(gx + 16f, gndRailY + 3f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText("-", minusLbl.x, minusLbl.y, trainerWhiteSubPaint)
                }

                // =========================================================================================
                // SECTION E: BOTTOM ROW (INPUT SECTION, CLOCK SECTION, LOGIC PROBE, PULSER)
                // =========================================================================================
                val botY0 = by1 - 70f
                val botY1 = by1 - 12f

                // --- E1. INPUT SECTION (Left 58% width) ---
                val inputX0 = bx0 + 10f
                val inputX1 = bx0 + bw * 0.58f
                val inBoxP0 = project3D(inputX0, botY0, 0.6f)
                val inBoxP1 = project3D(inputX1, botY0, 0.6f)
                val inBoxP2 = project3D(inputX1, botY1, 0.6f)
                val inBoxP3 = project3D(inputX0, botY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(inBoxP0.x, inBoxP0.y)
                        lineTo(inBoxP1.x, inBoxP1.y)
                        lineTo(inBoxP2.x, inBoxP2.y)
                        lineTo(inBoxP3.x, inBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val inTitlePos = project3D((inputX0 + inputX1) / 2f, botY1 - 4f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("INPUT SECTION", inTitlePos.x, inTitlePos.y, trainerWhiteHeaderPaint)

                // 12 Input Metal Toggle Switches with Red Bat Caps + Sockets (S12..S1, IP12..IP1)
                val swCount = 12
                val swSpacing = (inputX1 - inputX0) / (swCount + 1)
                val swY = by1 - 32f
                val swSockY = botY0 + 14f
                val circuitSwitches = currentComponents.filter { it.type == "SWITCH" }

                for (i in 0 until swCount) {
                    val sx = inputX0 + (i + 1) * swSpacing
                    val swLabel = "S${swCount - i}"
                    val sockLabel = "IP${swCount - i}"

                    val mappedSw = circuitSwitches.getOrNull(i)
                    val isSwOn = trainerSwitchLocalStates[i] ?: (mappedSw?.switchState ?: false)

                    // Input Terminal Banana Socket IP12..IP1
                    val sockCenter = project3D(sx, swSockY, 1.5f)
                    drawCircle(color = Color.White, radius = 4.4f, center = sockCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFCFD8DC), radius = 3.4f, center = sockCenter)
                    drawCircle(color = Color(0xFF111111), radius = 1.8f, center = sockCenter)
                    val sLblPos = project3D(sx, swSockY - 6f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(sockLabel, sLblPos.x, sLblPos.y, trainerPinNumPaint)

                    // Metal Lever Toggle Switch with Chrome Collar and RED Vinyl Cap
                    val swBase = project3D(sx, swY, 2f)
                    val tiltY = if (isSwOn) -7.5f else 7.5f // Up is ON, Down is OFF
                    val leverTip = project3D(sx, swY + tiltY, 16f)

                    // Threaded Hex Nut & Collar
                    drawCircle(color = Color(0xFF78909C), radius = 7.2f, center = swBase)
                    drawCircle(color = Color(0xFFCFD8DC), radius = 5.2f, center = swBase)
                    drawCircle(color = Color(0xFF263238), radius = 3.6f, center = swBase)

                    // Solid Metal Lever Arm
                    drawLine(color = Color(0xFF263238), start = swBase, end = leverTip, strokeWidth = 3.6f, cap = StrokeCap.Round)
                    drawLine(color = Color(0xFFECEFF1), start = swBase, end = leverTip, strokeWidth = 2.4f, cap = StrokeCap.Round)

                    // Signature RED Vinyl Bat Tip Cap (Exactly matching the user's reference photo!)
                    drawCircle(color = Color(0xFFB71C1C), radius = 4.8f, center = leverTip)
                    drawCircle(color = Color(0xFFE53935), radius = 3.8f, center = leverTip)
                    drawCircle(color = Color(0xFFFF8A80), radius = 1.5f, center = Offset(leverTip.x - 1f, leverTip.y - 1f))

                    // Active Green Glow Status Ring around switch when ON
                    if (isSwOn) {
                        drawCircle(color = Color(0xFF00E676).copy(alpha = 0.6f), radius = 6.2f, center = swBase, style = Stroke(1.4f))
                    }

                    // Silkscreen Label S12..S1
                    val swLblPos = project3D(sx, swY + 11f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(swLabel, swLblPos.x, swLblPos.y, trainerPinNumPaint)
                }

                // --- E2. CLOCK SECTION (60% to 75% width) ---
                val clkX0 = bx0 + bw * 0.60f
                val clkX1 = bx0 + bw * 0.74f
                val clkBoxP0 = project3D(clkX0, botY0, 0.6f)
                val clkBoxP1 = project3D(clkX1, botY0, 0.6f)
                val clkBoxP2 = project3D(clkX1, botY1, 0.6f)
                val clkBoxP3 = project3D(clkX0, botY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(clkBoxP0.x, clkBoxP0.y)
                        lineTo(clkBoxP1.x, clkBoxP1.y)
                        lineTo(clkBoxP2.x, clkBoxP2.y)
                        lineTo(clkBoxP3.x, clkBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val clkTitlePos = project3D((clkX0 + clkX1) / 2f, botY1 - 4f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("CLOCK SECTION", clkTitlePos.x, clkTitlePos.y, trainerWhiteHeaderPaint)

                // 6 Frequency Sockets (0.1Hz, 1Hz, 10Hz, 100Hz, 200Hz, 500Hz) + Blinking Clock LED
                val clkFreqs = listOf("0.1Hz", "1Hz", "10Hz", "100Hz", "200Hz", "500Hz")
                val freqCols = 3
                val colSpacing = (clkX1 - clkX0) / (freqCols + 1)
                for (fIdx in 0 until 6) {
                    val col = fIdx % 3
                    val row = fIdx / 3
                    val fx = clkX0 + (col + 1) * colSpacing
                    val fy = botY0 + 16f + row * 18f

                    val sCenter = project3D(fx, fy, 1.5f)
                    drawCircle(color = Color.White, radius = 3.6f, center = sCenter, style = Stroke(0.8f))
                    drawCircle(color = Color(0xFFCFD8DC), radius = 2.6f, center = sCenter)
                    drawCircle(color = Color(0xFF111111), radius = 1.4f, center = sCenter)

                    val fLbl = project3D(fx, fy - 5f, 0.8f)
                    drawContext.canvas.nativeCanvas.drawText(clkFreqs[fIdx], fLbl.x, fLbl.y, trainerPinNumPaint)
                }

                // Blinking Clock Diode at center
                val clkLedCenter = project3D((clkX0 + clkX1) / 2f, botY1 - 14f, 3f)
                drawCircle(color = Color(0xFFFFB300).copy(alpha = 0.4f), radius = 8f, center = clkLedCenter)
                drawCircle(color = Color(0xFFFFC107), radius = 3.5f, center = clkLedCenter)
                drawCircle(color = Color.White, radius = 1.2f, center = clkLedCenter)

                // --- E3. LOGIC PROBE SECTION (75% to 85% width) ---
                val probeX0 = bx0 + bw * 0.755f
                val probeX1 = bx0 + bw * 0.855f
                val pbBoxP0 = project3D(probeX0, botY0, 0.6f)
                val pbBoxP1 = project3D(probeX1, botY0, 0.6f)
                val pbBoxP2 = project3D(probeX1, botY1, 0.6f)
                val pbBoxP3 = project3D(probeX0, botY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(pbBoxP0.x, pbBoxP0.y)
                        lineTo(pbBoxP1.x, pbBoxP1.y)
                        lineTo(pbBoxP2.x, pbBoxP2.y)
                        lineTo(pbBoxP3.x, pbBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val probeTitlePos = project3D((probeX0 + probeX1) / 2f, botY1 - 4f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("LOGIC PROBE", probeTitlePos.x, probeTitlePos.y, trainerWhiteHeaderPaint)

                // Dual High (H) / Low (L) LEDs and Input Socket
                val probeMidX = (probeX0 + probeX1) / 2f
                val ledHCenter = project3D(probeMidX - 9f, botY0 + 20f, 3f)
                val ledLCenter = project3D(probeMidX + 9f, botY0 + 20f, 3f)
                // Red H LED
                drawCircle(color = Color(0xFFFF1744), radius = 3.6f, center = ledHCenter)
                drawCircle(color = Color.White, radius = 1f, center = ledHCenter)
                val hLbl = project3D(probeMidX - 9f, botY0 + 30f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("H", hLbl.x, hLbl.y, trainerPinNumPaint)

                // Green L LED
                drawCircle(color = Color(0xFF00E676), radius = 3.6f, center = ledLCenter)
                drawCircle(color = Color.White, radius = 1f, center = ledLCenter)
                val lLbl = project3D(probeMidX + 9f, botY0 + 30f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("L", lLbl.x, lLbl.y, trainerPinNumPaint)

                // Probe Socket
                val pbSockCenter = project3D(probeMidX, botY0 + 42f, 1.5f)
                drawCircle(color = Color.White, radius = 4.4f, center = pbSockCenter, style = Stroke(0.8f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 3.2f, center = pbSockCenter)
                drawCircle(color = Color(0xFF111111), radius = 1.8f, center = pbSockCenter)

                // --- E4. PULSER SECTION (87% to 98% width) ---
                val pulserX0 = bx0 + bw * 0.87f
                val pulserX1 = bx1 - 10f
                val pulBoxP0 = project3D(pulserX0, botY0, 0.6f)
                val pulBoxP1 = project3D(pulserX1, botY0, 0.6f)
                val pulBoxP2 = project3D(pulserX1, botY1, 0.6f)
                val pulBoxP3 = project3D(pulserX0, botY1, 0.6f)
                drawPath(
                    path = Path().apply {
                        moveTo(pulBoxP0.x, pulBoxP0.y)
                        lineTo(pulBoxP1.x, pulBoxP1.y)
                        lineTo(pulBoxP2.x, pulBoxP2.y)
                        lineTo(pulBoxP3.x, pulBoxP3.y)
                        close()
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    style = Stroke(1.4f)
                )
                val pulserTitlePos = project3D((pulserX0 + pulserX1) / 2f, botY1 - 4f, 0.8f)
                drawContext.canvas.nativeCanvas.drawText("PULSER", pulserTitlePos.x, pulserTitlePos.y, trainerWhiteHeaderPaint)

                // Big Round Tactile RED Pushbutton with Chrome Collar
                val pulserMidX = (pulserX0 + pulserX1) / 2f
                val pulserY = botY0 + 32f
                val btnElev = if (isPulserPressed) 3f else 7f
                val btnBase = project3D(pulserMidX, pulserY, 2f)
                val btnTop = project3D(pulserMidX, pulserY, btnElev)

                // Chrome beveled collar
                drawCircle(color = Color(0xFFCFD8DC), radius = 14f, center = btnBase)
                drawCircle(color = Color(0xFF78909C), radius = 12f, center = btnBase)
                drawCircle(color = Color(0xFF263238), radius = 10f, center = btnBase)

                // Red tactile button dome
                drawCircle(color = Color(0xFFB71C1C), radius = 9.5f, center = btnTop)
                drawCircle(color = Color(0xFFE53935), radius = 8.5f, center = btnTop)
                drawCircle(color = Color(0xFFFFCDD2).copy(alpha = 0.8f), radius = 3.2f, center = Offset(btnTop.x - 2f, btnTop.y - 2f))

                // Pulse sockets (Active-High and Active-Low inverted)
                val pulSock1 = project3D(pulserMidX - 9f, botY0 + 13f, 1.5f)
                val pulSock2 = project3D(pulserMidX + 9f, botY0 + 13f, 1.5f)
                drawCircle(color = Color.White, radius = 3.6f, center = pulSock1, style = Stroke(0.8f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 2.6f, center = pulSock1)
                drawCircle(color = Color(0xFF111111), radius = 1.4f, center = pulSock1)
                drawCircle(color = Color.White, radius = 3.6f, center = pulSock2, style = Stroke(0.8f))
                drawCircle(color = Color(0xFFCFD8DC), radius = 2.6f, center = pulSock2)
                drawCircle(color = Color(0xFF111111), radius = 1.4f, center = pulSock2)
            } else {
                // Top PCB FR-4 Solder Mask Surface
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFF133E30)
                )

                // Gold/Copper Plated Bevel Perimeter
                drawPath(
                    path = Path().apply {
                        moveTo(t0.x, t0.y)
                        lineTo(t1.x, t1.y)
                        lineTo(t2.x, t2.y)
                        lineTo(t3.x, t3.y)
                        close()
                    },
                    color = Color(0xFFD4AF37).copy(alpha = 0.6f),
                    style = Stroke(width = 1.6f)
                )

                // 3D PCB Silkscreen Grid Lines (Toggleable)
                if (showPcbGrid) {
                    val gridStep = 40f
                    var gx = bx0 + gridStep
                    while (gx < bx1) {
                        val pA = project3D(gx, by0, 0f)
                        val pB = project3D(gx, by1, 0f)
                        drawLine(
                            color = Color(0xFF1C5240).copy(alpha = 0.5f),
                            start = pA,
                            end = pB,
                            strokeWidth = 0.9f
                        )
                        gx += gridStep
                    }
                    var gy = by0 + gridStep
                    while (gy < by1) {
                        val pA = project3D(bx0, gy, 0f)
                        val pB = project3D(bx1, gy, 0f)
                        drawLine(
                            color = Color(0xFF1C5240).copy(alpha = 0.5f),
                            start = pA,
                            end = pB,
                            strokeWidth = 0.9f
                        )
                        gy += gridStep
                    }
                }

                // Silkscreen text
                drawContext.canvas.nativeCanvas.drawText("LOGICLAB CAD • 3D PCB ENGINE", t0.x + 10f, t0.y + 18f, gridTextPaint)
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
                    "SWITCH" -> Color(0xFF1B2A38)
                    "LED" -> Color(0xFF15261D)
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
                val centerTop = Offset(
                    (ct0.x + ct1.x + ct2.x + ct3.x) / 4f,
                    (ct0.y + ct1.y + ct2.y + ct3.y) / 4f + 3f
                )
                val labelText = comp.label.ifBlank { def?.name ?: comp.type }
                drawContext.canvas.nativeCanvas.drawText(
                    labelText.take(9),
                    centerTop.x,
                    centerTop.y,
                    chipTextPaint
                )

                // Component specific 3D physical widgets
                when (comp.type) {
                    "SWITCH" -> {
                        // Realistic Metal Lever / Toggle Switch
                        val swBase = project3D(comp.x + w / 2f, comp.y + h / 2f, chipHeight)
                        val tiltY = if (comp.switchState) 12f else -12f
                        val leverTip = project3D(comp.x + w / 2f, comp.y + h / 2f + tiltY, chipHeight + 22f)

                        // 1. Threaded Chrome Hex Mounting Nut & Bushing Collar
                        drawCircle(color = Color(0xFF6E7781), radius = 13.5f, center = swBase)
                        drawCircle(color = Color(0xFFD0D7DE), radius = 11f, center = swBase)
                        drawCircle(color = Color(0xFF24292F), radius = 7.5f, center = swBase)
                        drawCircle(color = Color(0xFFAFB8C1), radius = 5.5f, center = swBase)

                        // 2. Solid Metal Lever Bat / Arm with Shaded Chrome Cylindrical Body
                        // Dark cast shadow under bat
                        drawLine(
                            color = Color(0xFF424A53),
                            start = swBase,
                            end = leverTip,
                            strokeWidth = 6.5f,
                            cap = StrokeCap.Round
                        )
                        // Main chrome metallic body
                        drawLine(
                            color = Color(0xFFD0D7DE),
                            start = swBase,
                            end = leverTip,
                            strokeWidth = 4.8f,
                            cap = StrokeCap.Round
                        )
                        // Specular white highlight along top edge
                        drawLine(
                            color = Color.White.copy(alpha = 0.9f),
                            start = swBase,
                            end = leverTip,
                            strokeWidth = 1.6f,
                            cap = StrokeCap.Round
                        )

                        // 3. Spherical Polished Chrome Bat Tip with Specular Glint
                        drawCircle(color = Color(0xFF57606A), radius = 7.5f, center = leverTip)
                        drawCircle(color = Color(0xFFF6F8FA), radius = 6.2f, center = leverTip)
                        drawCircle(color = Color.White, radius = 2.4f, center = Offset(leverTip.x - 1.8f, leverTip.y - 1.8f))

                        // Active State Status Collar Ring
                        if (comp.switchState) {
                            drawCircle(color = SignalHigh.copy(alpha = 0.45f), radius = 10.5f, center = swBase, style = Stroke(width = 2.5f))
                            drawCircle(color = SignalHigh, radius = 3.5f, center = leverTip)
                        } else {
                            drawCircle(color = Color(0xFF38465B), radius = 2.5f, center = leverTip)
                        }

                        // Physical ON/OFF Engraved Marker
                        val statusText = if (comp.switchState) "ON [1]" else "OFF [0]"
                        val statusPaint = Paint().apply {
                            color = if (comp.switchState) android.graphics.Color.rgb(0, 230, 118) else android.graphics.Color.LTGRAY
                            textSize = 9.5f
                            typeface = Typeface.MONOSPACE
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(statusText, swBase.x, swBase.y + 19f, statusPaint)
                    }
                    "LED" -> {
                        // Authentic 3D Through-Hole LED Diode with Internal Reflector & Photonic Glow
                        val isHigh = comp.inputValues.values.any { it == DigitalState.HIGH }
                        val diodeCenter = Offset(comp.x + w / 2f, comp.y + h / 2f)

                        // 1. Through-Hole Metal Wire Leads descending to PCB
                        val leadL1 = project3D(diodeCenter.x - 4f, diodeCenter.y, 1f)
                        val leadL2 = project3D(diodeCenter.x + 4f, diodeCenter.y, 1f)
                        val leadT1 = project3D(diodeCenter.x - 4f, diodeCenter.y, chipHeight + 2f)
                        val leadT2 = project3D(diodeCenter.x + 4f, diodeCenter.y, chipHeight + 2f)
                        drawLine(color = Color(0xFF8C959F), start = leadL1, end = leadT1, strokeWidth = 2.2f)
                        drawLine(color = Color(0xFF8C959F), start = leadL2, end = leadT2, strokeWidth = 2.2f)

                        // 2. Under-Diode Ambient Glow Pool on PCB when Active
                        val gndCenter = project3D(diodeCenter.x, diodeCenter.y, 0.5f)
                        if (isHigh) {
                            drawCircle(color = SignalHigh.copy(alpha = 0.32f), radius = 24f, center = gndCenter)
                            drawCircle(color = SignalHigh.copy(alpha = 0.55f), radius = 13f, center = gndCenter)
                        }

                        // 3. LED Base Flange Collar (Lip Ring)
                        val flangeCenter = project3D(diodeCenter.x, diodeCenter.y, chipHeight + 3f)
                        val flangeColor = if (isHigh) SignalHigh.copy(alpha = 0.85f) else Color(0xFF1E3825)
                        drawCircle(color = flangeColor, radius = 11f, center = flangeCenter)
                        drawCircle(color = Color(0xFF24292F), radius = 11.2f, center = flangeCenter, style = Stroke(1.2f))

                        // 4. Cylindrical Lens Barrel Body
                        val barrelCenter = project3D(diodeCenter.x, diodeCenter.y, chipHeight + 9f)
                        val barrelColor = if (isHigh) SignalHigh else Color(0xFF163820)
                        drawCircle(color = barrelColor, radius = 9.5f, center = barrelCenter)

                        // 5. Hemispherical Convex Dome Top & Internal Semiconductor Die
                        val domeCenter = project3D(diodeCenter.x, diodeCenter.y, chipHeight + 17f)
                        if (isHigh) {
                            // Photonic Bloom Aura
                            drawCircle(color = SignalHigh.copy(alpha = 0.16f), radius = 30f, center = domeCenter)
                            drawCircle(color = SignalHigh.copy(alpha = 0.45f), radius = 18f, center = domeCenter)
                            // Vivid Glowing Dome
                            drawCircle(color = SignalHigh, radius = 10f, center = domeCenter)
                            // Incandescent Internal Die Core
                            drawCircle(color = Color(0xFFE8FFE8), radius = 4.5f, center = domeCenter)
                            drawCircle(color = Color.White, radius = 2.5f, center = domeCenter)
                            // Specular Reflection Curved Highlight
                            drawArc(
                                color = Color.White.copy(alpha = 0.9f),
                                startAngle = -135f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(domeCenter.x - 7.5f, domeCenter.y - 7.5f),
                                size = androidx.compose.ui.geometry.Size(15f, 15f),
                                style = Stroke(2f, cap = StrokeCap.Round)
                            )
                        } else {
                            // Dark Translucent Emerald Epoxy Body
                            drawCircle(color = Color(0xFF112E19), radius = 10f, center = domeCenter)
                            // Internal Anvil / Cathode Lead Frame Wire Silhouette
                            drawLine(
                                color = Color(0xFF57606A),
                                start = Offset(domeCenter.x - 3.5f, domeCenter.y + 3f),
                                end = Offset(domeCenter.x + 3.5f, domeCenter.y - 3f),
                                strokeWidth = 2.2f
                            )
                            // Soft Specular Gloss Highlight
                            drawArc(
                                color = Color.White.copy(alpha = 0.45f),
                                startAngle = -135f,
                                sweepAngle = 80f,
                                useCenter = false,
                                topLeft = Offset(domeCenter.x - 7.5f, domeCenter.y - 7.5f),
                                size = androidx.compose.ui.geometry.Size(15f, 15f),
                                style = Stroke(1.4f, cap = StrokeCap.Round)
                            )
                        }
                    }
                    "CONST_HIGH", "CONST_LOW" -> {
                        val isHigh = comp.type == "CONST_HIGH"
                        val swBase = project3D(comp.x + w / 2f, comp.y + h / 2f, chipHeight)
                        val tiltY = if (isHigh) 10f else -10f
                        val leverTip = project3D(comp.x + w / 2f, comp.y + h / 2f + tiltY, chipHeight + 20f)

                        // Threaded Chrome Hex Mounting Nut
                        drawCircle(color = Color(0xFF6E7781), radius = 11.5f, center = swBase)
                        drawCircle(color = Color(0xFF24292F), radius = 7.5f, center = swBase)
                        drawCircle(color = Color(0xFFAFB8C1), radius = 5.2f, center = swBase)

                        // Solid Chrome Metal Bat Arm
                        drawLine(color = Color(0xFF424A53), start = swBase, end = leverTip, strokeWidth = 5.5f, cap = StrokeCap.Round)
                        drawLine(color = Color(0xFFD0D7DE), start = swBase, end = leverTip, strokeWidth = 4.2f, cap = StrokeCap.Round)
                        drawLine(color = Color.White.copy(alpha = 0.9f), start = swBase, end = leverTip, strokeWidth = 1.4f, cap = StrokeCap.Round)

                        // Spherical Polished Chrome Bat Tip
                        drawCircle(color = Color(0xFF57606A), radius = 6.5f, center = leverTip)
                        drawCircle(color = Color(0xFFF6F8FA), radius = 5.4f, center = leverTip)
                        drawCircle(color = Color.White, radius = 2f, center = Offset(leverTip.x - 1.4f, leverTip.y - 1.4f))

                        if (isHigh) {
                            drawCircle(color = SignalHigh.copy(alpha = 0.45f), radius = 9.5f, center = swBase, style = Stroke(width = 2.2f))
                            drawCircle(color = SignalHigh, radius = 3.2f, center = leverTip)
                        } else {
                            drawCircle(color = Color(0xFF38465B), radius = 2.2f, center = leverTip)
                        }

                        val statusText = if (isHigh) "+5V (HIGH)" else "GND (LOW)"
                        val statusPaint = Paint().apply {
                            color = if (isHigh) android.graphics.Color.rgb(0, 230, 118) else android.graphics.Color.LTGRAY
                            textSize = 8.5f
                            typeface = Typeface.MONOSPACE
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(statusText, swBase.x, swBase.y + 18f, statusPaint)
                    }
                    "OSCILLATOR" -> {
                        val oscHigh = comp.switchState
                        val barColor = if (oscHigh) Color(0xFFFFB300) else Color(0xFF554422)
                        drawCircle(color = barColor, radius = 6f, center = centerTop)
                    }
                    "DIGITAL_PROBE" -> {
                        val isHigh = comp.inputValues.values.any { it == DigitalState.HIGH }
                        val probeCenter = project3D(comp.x + w / 2f, comp.y + h / 2f - 4f, chipHeight + 3f)
                        val domeCenter = project3D(comp.x + w / 2f, comp.y + h / 2f - 4f, chipHeight + 12f)

                        // 3D LED Diode Bezel Ring
                        drawCircle(color = Color(0xFF263238), radius = 9f, center = probeCenter)
                        drawCircle(color = Color(0xFFCFD8DC), radius = 7.5f, center = probeCenter, style = Stroke(1.2f))

                        // Photonic Dome Glow
                        if (isHigh) {
                            drawCircle(color = SignalHigh.copy(alpha = 0.22f), radius = 22f, center = domeCenter)
                            drawCircle(color = SignalHigh.copy(alpha = 0.55f), radius = 13f, center = domeCenter)
                            drawCircle(color = SignalHigh, radius = 7f, center = domeCenter)
                            drawCircle(color = Color(0xFFE8FFE8), radius = 3.2f, center = domeCenter)
                            drawCircle(color = Color.White, radius = 1.6f, center = domeCenter)
                        } else {
                            drawCircle(color = Color(0xFF14301B), radius = 7f, center = domeCenter)
                            drawCircle(color = Color.White.copy(alpha = 0.35f), radius = 1.6f, center = Offset(domeCenter.x - 1.6f, domeCenter.y - 1.6f))
                        }

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
