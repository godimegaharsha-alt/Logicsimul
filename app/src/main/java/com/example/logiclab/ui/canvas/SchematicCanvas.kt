package com.example.logiclab.ui.canvas

import android.graphics.Paint
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import com.example.ui.theme.SignalError
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.audio.AudioEngine
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.ComponentCategory
import com.example.logiclab.ui.components.GateSearchBarAndCatalog
import com.example.logiclab.model.ComponentDefinition
import com.example.logiclab.model.DigitalState
import com.example.logiclab.model.Port
import com.example.logiclab.model.PortType
import com.example.ui.theme.GridDotColor
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCanvasBg
import com.example.ui.theme.LogicLabDesign
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown
import com.example.ui.theme.SignalFlowHigh
import com.example.ui.theme.SignalFlowLow
import kotlin.math.roundToInt

data class WireCreationState(
    val fromComponentId: String,
    val fromPortId: String,
    val startWorldPos: Offset,
    val currentWorldPos: Offset
)

data class DraggingNewGateState(
    val definition: ComponentDefinition,
    val currentScreenPos: Offset,
    val snappedWorldPos: Offset
)

@Composable
fun SchematicCanvas(
    components: List<CircuitComponent>,
    wires: List<CircuitWire>,
    selectedComponentId: String?,
    selectedWireId: String?,
    snapToGrid: Boolean,
    gridSize: Float = 20f,
    panOffset: Offset,
    zoomScale: Float,
    isSimulating: Boolean,
    onPanZoomChange: (Offset, Float) -> Unit,
    onComponentSelect: (String?) -> Unit,
    onComponentMove: (String, Float, Float) -> Unit,
    onComponentToggleSwitch: (String) -> Unit,
    onWireSelect: (String?) -> Unit = {},
    onWireCreate: (fromCompId: String, fromPortId: String, toCompId: String, toPortId: String) -> Unit,
    onComponentDoubleClick: ((String) -> Unit)? = null,
    isWireDrawingMode: Boolean = false,
    onToggleWireDrawingMode: (() -> Unit)? = null,
    canUndo: Boolean = false,
    canRedo: Boolean = false,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    onDropNewComponent: ((ComponentDefinition, Float, Float) -> Unit)? = null,
    onOpenFullLibrary: (() -> Unit)? = null,
    isSignalFlowMode: Boolean = true,
    onToggleSignalFlowMode: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var pendingWire by remember { mutableStateOf<WireCreationState?>(null) }
    var draggingExistingCompId by remember { mutableStateOf<String?>(null) }
    var draggingNewGate by remember { mutableStateOf<DraggingNewGateState?>(null) }
    var isPaletteExpanded by remember { mutableStateOf(true) }

    // Tap-to-wire state: first tapped pin waiting for second tapped pin
    var tappedSourcePort by remember { mutableStateOf<Pair<CircuitComponent, Port>?>(null) }

    // Double-tap tracking state
    var lastTapTime by remember { androidx.compose.runtime.mutableLongStateOf(0L) }
    var lastTappedCompId by remember { mutableStateOf<String?>(null) }
    var lastTapScreenPos by remember { mutableStateOf(Offset.Zero) }

    // Subtle signal animation pulse phase (0.0f .. 1.0f)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulsePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Keep latest state references so gesture loop on Canvas is NEVER cancelled mid-gesture
    val currentPanOffset by rememberUpdatedState(panOffset)
    val currentZoomScale by rememberUpdatedState(zoomScale)
    val currentComponents by rememberUpdatedState(components)
    val currentWires by rememberUpdatedState(wires)
    val currentSnapToGrid by rememberUpdatedState(snapToGrid)
    val currentOnPanZoomChange by rememberUpdatedState(onPanZoomChange)
    val currentOnComponentSelect by rememberUpdatedState(onComponentSelect)
    val currentOnComponentDoubleClick by rememberUpdatedState(onComponentDoubleClick)
    val currentOnComponentMove by rememberUpdatedState(onComponentMove)
    val currentOnComponentToggleSwitch by rememberUpdatedState(onComponentToggleSwitch)
    val currentOnWireSelect by rememberUpdatedState(onWireSelect)
    val currentOnWireCreate by rememberUpdatedState(onWireCreate)
    val currentIsWireDrawingMode by rememberUpdatedState(isWireDrawingMode)

    fun screenToWorld(screenOffset: Offset, pan: Offset = currentPanOffset, zoom: Float = currentZoomScale): Offset {
        return (screenOffset - pan) / zoom
    }

    fun worldToScreen(worldOffset: Offset, pan: Offset = currentPanOffset, zoom: Float = currentZoomScale): Offset {
        return worldOffset * zoom + pan
    }

    fun snap(value: Float, snapEnabled: Boolean = currentSnapToGrid): Float {
        return if (snapEnabled) (Math.round(value / gridSize) * gridSize) else value
    }

    // Port world positions lookup with 90-degree rotation support
    fun getPortPosition(comp: CircuitComponent, port: Port): Offset {
        val def = ComponentRegistry.get(comp.type) ?: return Offset(comp.x, comp.y)
        val cx = comp.x + def.width / 2f
        val cy = comp.y + def.height / 2f
        val unrotX = comp.x + def.width * port.relativeX
        val unrotY = comp.y + def.height * port.relativeY
        if (comp.rotation == 0) return Offset(unrotX, unrotY)

        val rad = Math.toRadians(comp.rotation.toDouble())
        val cosA = kotlin.math.cos(rad).toFloat()
        val sinA = kotlin.math.sin(rad).toFloat()
        val dx = unrotX - cx
        val dy = unrotY - cy
        val rx = cx + (dx * cosA - dy * sinA)
        val ry = cy + (dx * sinA + dy * cosA)
        return Offset(rx, ry)
    }

    fun findPortAt(worldPos: Offset, radius: Float = 20f): Pair<CircuitComponent, Port>? {
        for (comp in currentComponents) {
            val def = ComponentRegistry.get(comp.type) ?: continue
            val allPorts = def.inputs + def.outputs
            for (p in allPorts) {
                val pPos = getPortPosition(comp, p)
                if ((pPos - worldPos).getDistance() <= radius) {
                    return Pair(comp, p)
                }
            }
        }
        return null
    }

    fun findComponentAt(worldPos: Offset, pad: Float = 14f): CircuitComponent? {
        for (comp in currentComponents.asReversed()) {
            val def = ComponentRegistry.get(comp.type) ?: continue
            val cx = comp.x + def.width / 2f
            val cy = comp.y + def.height / 2f
            val isRot90 = (comp.rotation % 180 != 0)
            val effW = if (isRot90) def.height else def.width
            val effH = if (isRot90) def.width else def.height
            val halfW = effW / 2f + pad
            val halfH = effH / 2f + pad
            if (worldPos.x >= cx - halfW && worldPos.x <= cx + halfW &&
                worldPos.y >= cy - halfH && worldPos.y <= cy + halfH
            ) {
                return comp
            }
        }
        return null
    }

    fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
        val l2 = (b - a).getDistanceSquared()
        if (l2 == 0f) return (p - a).getDistance()
        val t = (((p.x - a.x) * (b.x - a.x) + (p.y - a.y) * (b.y - a.y)) / l2).coerceIn(0f, 1f)
        val projection = Offset(a.x + t * (b.x - a.x), a.y + t * (b.y - a.y))
        return (p - projection).getDistance()
    }

    fun findWireAt(worldPos: Offset, radius: Float = 18f): CircuitWire? {
        for (w in currentWires.asReversed()) {
            val fromComp = currentComponents.find { it.id == w.fromComponentId } ?: continue
            val toComp = currentComponents.find { it.id == w.toComponentId } ?: continue
            val fromDef = ComponentRegistry.get(fromComp.type) ?: continue
            val toDef = ComponentRegistry.get(toComp.type) ?: continue
            val fromPort = (fromDef.outputs + fromDef.inputs).find { it.id == w.fromPortId } ?: continue
            val toPort = (toDef.inputs + toDef.outputs).find { it.id == w.toPortId } ?: continue

            val p1 = getPortPosition(fromComp, fromPort)
            val p2 = getPortPosition(toComp, toPort)
            val midX = (p1.x + p2.x) / 2f

            val d1 = distanceToSegment(worldPos, p1, Offset(midX, p1.y))
            val d2 = distanceToSegment(worldPos, Offset(midX, p1.y), Offset(midX, p2.y))
            val d3 = distanceToSegment(worldPos, Offset(midX, p2.y), p2)

            if (minOf(d1, d2, d3) <= radius) {
                return w
            }
        }
        return null
    }

    val labColors = LogicLabDesign.colors

    // Quick common gates for the dock palette
    val paletteGates = remember {
        listOf(
            "AND", "OR", "NOT", "NAND", "NOR", "XOR",
            "SWITCH", "LED", "CLOCK", "OSCILLATOR", "DIGITAL_PROBE",
            "SR_LATCH", "D_LATCH",
            "HALF_ADDER", "FULL_ADDER", "MUX_4_1", "DECODER_2_4"
        ).mapNotNull { ComponentRegistry.get(it) }
    }

    // Cached Text Paints to eliminate garbage collection allocations during 60fps canvas render
    val textPaint = remember(labColors.textPrimary) {
        Paint().apply {
            color = labColors.textPrimary.toArgb()
            textSize = 10.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val portPaint = remember(labColors.textSecondary) {
        Paint().apply {
            color = labColors.textSecondary.toArgb()
            textSize = 8.5f
            isAntiAlias = true
        }
    }
    val swOnPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(63, 185, 80)
            textSize = 9.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val swOffPaint = remember {
        Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 9.5f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val probeHighPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(63, 185, 80)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val probeLowPaint = remember {
        Paint().apply {
            color = android.graphics.Color.LTGRAY
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val probeUnknownPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(210, 153, 34)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }
    val hintPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(88, 166, 255)
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(labColors.canvasBg)
            .testTag("schematic_canvas")
    ) {
        // Main Schematic Drawing Canvas with gesture input
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startScreenPos = down.position
                        var prevScreenPos = down.position
                        val startPan = currentPanOffset
                        val startZoom = currentZoomScale
                        val startWorldPos = screenToWorld(startScreenPos, startPan, startZoom)

                        // Hit test on touch DOWN
                        // Generous, zoom-adaptive hit-testing for components
                        val compHitPad = (24f / startZoom).coerceIn(16f, 48f)
                        val hitComp = findComponentAt(startWorldPos, pad = compHitPad)
                        val initialCompPos = hitComp?.let { Offset(snap(it.x, currentSnapToGrid), snap(it.y, currentSnapToGrid)) }

                        // Port hit-testing: if wire drawing mode is active or pin 1 tapped, use generous radius
                        val portRadius = if (currentIsWireDrawingMode || tappedSourcePort != null) {
                            (32f / startZoom).coerceIn(20f, 48f)
                        } else if (hitComp == null) {
                            (24f / startZoom).coerceIn(14f, 32f)
                        } else {
                            (15f / startZoom).coerceIn(9f, 22f)
                        }
                        val hitPort = findPortAt(startWorldPos, radius = portRadius)

                        var hasMovedPastSlop = false
                        var maxMovementDist = 0f
                        val dragThreshold = (viewConfiguration.touchSlop * 1.5f).coerceAtLeast(18f)
                        var isMultiTouch = false
                        var lastDistance = 0f
                        var lastCentroid = Offset.Zero
                        var activeDragCompId: String? = null
                        var accumulatedPan = startPan

                        while (true) {
                            val event = awaitPointerEvent()
                            val activePointers = event.changes.filter { it.pressed }

                            if (activePointers.isEmpty()) {
                                break
                            }

                            if (activePointers.size >= 2) {
                                // Multi-touch Pinch to Zoom & Two-finger Pan!
                                isMultiTouch = true
                                val p0 = activePointers[0].position
                                val p1 = activePointers[1].position
                                val currentDistance = (p0 - p1).getDistance()
                                val currentCentroid = (p0 + p1) / 2f

                                if (lastDistance > 0f && lastCentroid != Offset.Zero) {
                                    val zoomFactor = currentDistance / lastDistance
                                    val targetZoom = (currentZoomScale * zoomFactor).coerceIn(0.35f, 3.5f)
                                    val panDelta = currentCentroid - lastCentroid
                                    // Focal-point zoom: keeps the circuit section under the user's fingers stationary
                                    val newPan = currentCentroid - (currentCentroid - currentPanOffset) * (targetZoom / currentZoomScale) + panDelta
                                    accumulatedPan = newPan
                                    currentOnPanZoomChange(newPan, targetZoom)
                                }

                                lastDistance = currentDistance
                                lastCentroid = currentCentroid
                                event.changes.forEach { it.consume() }

                                // Cancel single-touch component drag or wire drag
                                pendingWire = null
                                draggingExistingCompId = null
                                activeDragCompId = null
                            } else if (!isMultiTouch && activePointers.size == 1) {
                                val pointer = activePointers[0]
                                val currentScreenPos = pointer.position
                                val totalDelta = currentScreenPos - startScreenPos
                                val dist = totalDelta.getDistance()
                                if (dist > maxMovementDist) {
                                    maxMovementDist = dist
                                }

                                if (!hasMovedPastSlop && dist > dragThreshold) {
                                    hasMovedPastSlop = true
                                    if (hitPort != null) {
                                        // Start dragging wire from port terminal
                                        val startPos = getPortPosition(hitPort.first, hitPort.second)
                                        pendingWire = WireCreationState(
                                            hitPort.first.id,
                                            hitPort.second.id,
                                            startPos,
                                            startWorldPos
                                        )
                                        tappedSourcePort = null
                                    } else if (hitComp != null) {
                                        // Start dragging component across 2D grid
                                        activeDragCompId = hitComp.id
                                        draggingExistingCompId = hitComp.id
                                        currentOnComponentSelect(hitComp.id)
                                        currentOnWireSelect(null)
                                    }
                                }

                                if (hasMovedPastSlop) {
                                    if (pendingWire != null) {
                                        // Dragging wire terminal to connect
                                        val currentWorldPos = screenToWorld(currentScreenPos, currentPanOffset, currentZoomScale)
                                        pendingWire = pendingWire!!.copy(currentWorldPos = currentWorldPos)
                                        pointer.consume()
                                    } else if (activeDragCompId != null && hitComp != null && initialCompPos != null) {
                                        // Dragging logic gate on 2D grid smoothly
                                        val deltaWorld = (currentScreenPos - startScreenPos) / currentZoomScale
                                        val rawX = initialCompPos.x + deltaWorld.x
                                        val rawY = initialCompPos.y + deltaWorld.y
                                        val snappedX = snap(rawX, currentSnapToGrid)
                                        val snappedY = snap(rawY, currentSnapToGrid)
                                        currentOnComponentMove(activeDragCompId!!, snappedX, snappedY)
                                        pointer.consume()
                                    } else {
                                        // Dragging empty canvas space -> Pan 2D grid smoothly
                                        val frameDelta = currentScreenPos - prevScreenPos
                                        accumulatedPan += frameDelta
                                        currentOnPanZoomChange(accumulatedPan, currentZoomScale)
                                        pointer.consume()
                                    }
                                }

                                prevScreenPos = currentScreenPos
                            }
                        }

                        // Touch Release logic
                        if (!isMultiTouch) {
                            val isTap = maxMovementDist <= dragThreshold || !hasMovedPastSlop
                            if (isTap) {
                                // Priority 1: Tap on Port Pin Terminal (Tap-to-Wire Drawing Mode)
                                if (hitPort != null) {
                                    val (comp, port) = hitPort
                                    if (tappedSourcePort == null) {
                                        // First pin tapped!
                                        tappedSourcePort = Pair(comp, port)
                                        currentOnComponentSelect(comp.id)
                                        AudioEngine.playClickSound()
                                    } else {
                                        // Second pin tapped!
                                        val (srcComp, srcPort) = tappedSourcePort!!
                                        if (srcComp.id == comp.id && srcPort.id == port.id) {
                                            // Tapped same pin again -> Deselect/cancel
                                            tappedSourcePort = null
                                            AudioEngine.playClickSound()
                                        } else {
                                            // Connect two pins with visible wire!
                                            currentOnWireCreate(srcComp.id, srcPort.id, comp.id, port.id)
                                            AudioEngine.playClickSound()
                                            tappedSourcePort = null
                                        }
                                    }
                                    pendingWire = null
                                } else if (hitComp != null) {
                                    // Tapped inside component body (not on port terminal)
                                    if (tappedSourcePort != null) {
                                        tappedSourcePort = null // cancel pending pin wire
                                    }
                                    val now = System.currentTimeMillis()
                                    val isDoubleTap = (lastTappedCompId == hitComp.id || (startScreenPos - lastTapScreenPos).getDistance() < 60f) &&
                                            (now - lastTapTime < 450L)

                                    if (isDoubleTap) {
                                        // DOUBLE-CLICK: Reveal full gate info dialog & delete option!
                                        lastTapTime = 0L
                                        lastTappedCompId = null
                                        currentOnComponentSelect(hitComp.id)
                                        currentOnWireSelect(null)
                                        pendingWire = null
                                        AudioEngine.playClickSound()
                                        currentOnComponentDoubleClick?.invoke(hitComp.id)
                                    } else {
                                        // SINGLE-CLICK: Select gate, show toolbar with info & delete option!
                                        lastTapTime = now
                                        lastTappedCompId = hitComp.id
                                        lastTapScreenPos = startScreenPos
                                        if (hitComp.type == "SWITCH" || hitComp.type == "OSCILLATOR" || hitComp.type == "CLOCK") {
                                            currentOnComponentToggleSwitch(hitComp.id)
                                        }
                                        currentOnComponentSelect(hitComp.id)
                                        currentOnWireSelect(null)
                                        pendingWire = null
                                        AudioEngine.playClickSound()
                                    }
                                } else {
                                    // Tapped empty canvas or wire
                                    if (tappedSourcePort != null) {
                                        tappedSourcePort = null // cancel pending pin selection
                                    }
                                    val hitWire = findWireAt(startWorldPos, radius = (18f / currentZoomScale).coerceIn(12f, 28f))
                                    if (hitWire != null) {
                                        currentOnWireSelect(hitWire.id)
                                        currentOnComponentSelect(null)
                                    } else {
                                        currentOnComponentSelect(null)
                                        currentOnWireSelect(null)
                                    }
                                }
                            } else {
                                // Released after DRAG
                                if (pendingWire != null) {
                                    val dropPort = findPortAt(pendingWire!!.currentWorldPos, radius = (28f / currentZoomScale).coerceIn(16f, 36f))
                                    if (dropPort != null) {
                                        val (comp, port) = dropPort
                                        val from = pendingWire!!
                                        if (from.fromComponentId != comp.id || from.fromPortId != port.id) {
                                            currentOnWireCreate(from.fromComponentId, from.fromPortId, comp.id, port.id)
                                        }
                                    }
                                    pendingWire = null
                                }
                                draggingExistingCompId = null
                                activeDragCompId = null
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Calculate visible world bounds for clipping-aware culling
            val visibleWorldBounds = ViewportCullingSystem.calculateVisibleWorldBounds(
                canvasWidth = canvasWidth,
                canvasHeight = canvasHeight,
                panOffset = panOffset,
                zoomScale = zoomScale,
                margin = 80f
            )
            val renderPortLabels = ViewportCullingSystem.shouldRenderPortLabels(zoomScale)
            val renderCompLabels = ViewportCullingSystem.shouldRenderComponentLabels(zoomScale)

            clipRect(0f, 0f, canvasWidth, canvasHeight) {
                // 1. Efficient Infinite Technical Grid Background
                // Scales and offsets precisely with viewport panOffset and zoomScale
                val worldLeft = -panOffset.x / zoomScale
                val worldTop = -panOffset.y / zoomScale
                val worldRight = (canvasWidth - panOffset.x) / zoomScale
                val worldBottom = (canvasHeight - panOffset.y) / zoomScale

            // Scale-adaptive Level-Of-Detail to maintain 60fps and prevent clutter
            val lodMultiplier = when {
                zoomScale < 0.25f -> 4
                zoomScale < 0.5f -> 2
                else -> 1
            }
            val stepWorld = gridSize * lodMultiplier

            val startIdxX = (kotlin.math.floor(worldLeft / stepWorld)).toLong() - 1L
            val endIdxX = (kotlin.math.ceil(worldRight / stepWorld)).toLong() + 1L
            val startIdxY = (kotlin.math.floor(worldTop / stepWorld)).toLong() - 1L
            val endIdxY = (kotlin.math.ceil(worldBottom / stepWorld)).toLong() + 1L

            // Pre-allocate point batches for single hardware-accelerated GPU draw calls
            val minorPoints = ArrayList<Offset>()
            val majorPoints = ArrayList<Offset>()

            var ix = startIdxX
            while (ix <= endIdxX) {
                val wx = ix * stepWorld
                val sx = wx * zoomScale + panOffset.x
                val isMajorX = (ix % (5L / lodMultiplier).coerceAtLeast(1L) == 0L)

                var iy = startIdxY
                while (iy <= endIdxY) {
                    val wy = iy * stepWorld
                    val sy = wy * zoomScale + panOffset.y
                    val isMajorY = (iy % (5L / lodMultiplier).coerceAtLeast(1L) == 0L)

                    val pt = Offset(sx, sy)
                    if (isMajorX && isMajorY) {
                        majorPoints.add(pt)
                    } else {
                        minorPoints.add(pt)
                    }
                    iy++
                }
                ix++
            }

            // Draw minor technical grid dots in batch
            val minorRadius = (1.4f * zoomScale).coerceIn(1.1f, 2.2f)
            drawPoints(
                points = minorPoints,
                pointMode = PointMode.Points,
                color = labColors.gridDotColor,
                strokeWidth = minorRadius * 2f,
                cap = StrokeCap.Round
            )

            // Draw major grid accents with slightly brighter tone and crisp radius
            val majorRadius = (2.2f * zoomScale).coerceIn(1.8f, 3.2f)
            drawPoints(
                points = majorPoints,
                pointMode = PointMode.Points,
                color = labColors.accentPrimary.copy(alpha = 0.45f),
                strokeWidth = majorRadius * 2f,
                cap = StrokeCap.Round
            )

            // Technical Origin Coordinate Axes (World X=0 and World Y=0)
            val originScreenX = panOffset.x
            val originScreenY = panOffset.y

            // Subtle vertical datum axis (X = 0)
            if (originScreenX in -10f..(canvasWidth + 10f)) {
                drawLine(
                    color = LabAccentBlue.copy(alpha = 0.22f),
                    start = Offset(originScreenX, 0f),
                    end = Offset(originScreenX, canvasHeight),
                    strokeWidth = 1f
                )
            }

            // Subtle horizontal datum axis (Y = 0)
            if (originScreenY in -10f..(canvasHeight + 10f)) {
                drawLine(
                    color = LabAccentBlue.copy(alpha = 0.22f),
                    start = Offset(0f, originScreenY),
                    end = Offset(canvasWidth, originScreenY),
                    strokeWidth = 1f
                )
            }

            // Technical Datum Origin Marker (0,0) if in viewport
            if (originScreenX in -20f..(canvasWidth + 20f) && originScreenY in -20f..(canvasHeight + 20f)) {
                drawCircle(
                    color = LabAccentBlue.copy(alpha = 0.6f),
                    radius = 4f,
                    center = Offset(originScreenX, originScreenY),
                    style = Stroke(width = 1.2f)
                )
                drawLine(
                    color = LabAccentBlue,
                    start = Offset(originScreenX - 6f, originScreenY),
                    end = Offset(originScreenX + 6f, originScreenY),
                    strokeWidth = 1.2f
                )
                drawLine(
                    color = LabAccentBlue,
                    start = Offset(originScreenX, originScreenY - 6f),
                    end = Offset(originScreenX, originScreenY + 6f),
                    strokeWidth = 1.2f
                )
            }

            // Transform Canvas into World Coordinates
            drawIntoCanvas { canvas ->
                canvas.save()
                canvas.translate(panOffset.x, panOffset.y)
                canvas.scale(zoomScale, zoomScale)

                // 2. Draw Magnetic Snap Guides & Alignment Crosshairs
                val activeDragComp = components.find { it.id == draggingExistingCompId }
                if (activeDragComp != null) {
                    val def = ComponentRegistry.get(activeDragComp.type)
                    if (def != null) {
                        // Highlight snapped grid boundary
                        val snapRect = Path().apply {
                            addRect(
                                androidx.compose.ui.geometry.Rect(
                                    activeDragComp.x - 2f,
                                    activeDragComp.y - 2f,
                                    activeDragComp.x + def.width + 2f,
                                    activeDragComp.y + def.height + 2f
                                )
                            )
                        }
                        drawPath(snapRect, LabAccentBlue.copy(alpha = 0.65f), style = Stroke(width = 1.5f))

                        // Corner grid snap crosshairs
                        val corners = listOf(
                            Offset(activeDragComp.x, activeDragComp.y),
                            Offset(activeDragComp.x + def.width, activeDragComp.y),
                            Offset(activeDragComp.x, activeDragComp.y + def.height),
                            Offset(activeDragComp.x + def.width, activeDragComp.y + def.height)
                        )
                        for (c in corners) {
                            drawLine(SignalHigh, Offset(c.x - 5f, c.y), Offset(c.x + 5f, c.y), strokeWidth = 1.5f)
                            drawLine(SignalHigh, Offset(c.x, c.y - 5f), Offset(c.x, c.y + 5f), strokeWidth = 1.5f)
                        }

                        // Magnetic Alignment Guidelines across other logic gates
                        for (other in components) {
                            if (other.id == activeDragComp.id) continue
                            val otherDef = ComponentRegistry.get(other.type) ?: continue

                            // Vertical Column Alignment (matching X coordinates)
                            if (kotlin.math.abs(other.x - activeDragComp.x) < 2f) {
                                drawLine(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.55f),
                                    start = Offset(activeDragComp.x, minOf(other.y, activeDragComp.y) - 60f),
                                    end = Offset(activeDragComp.x, maxOf(other.y + otherDef.height, activeDragComp.y + def.height) + 60f),
                                    strokeWidth = 1.2f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                )
                            }
                            // Horizontal Row Alignment (matching Y coordinates)
                            if (kotlin.math.abs(other.y - activeDragComp.y) < 2f) {
                                drawLine(
                                    color = Color(0xFF00E5FF).copy(alpha = 0.55f),
                                    start = Offset(minOf(other.x, activeDragComp.x) - 60f, activeDragComp.y),
                                    end = Offset(maxOf(other.x + otherDef.width, activeDragComp.x + def.width) + 60f, activeDragComp.y),
                                    strokeWidth = 1.2f,
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
                                )
                            }
                        }

                        // Snapped Coordinates Floating Tooltip
                        val gridX = (activeDragComp.x / gridSize).roundToInt()
                        val gridY = (activeDragComp.y / gridSize).roundToInt()
                        val coordBadge = "GRID [X: $gridX, Y: $gridY] • (${activeDragComp.x.toInt()}, ${activeDragComp.y.toInt()})"
                        val badgePaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 10f
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.MONOSPACE
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        val badgeBg = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(220, 15, 23, 35)
                            style = android.graphics.Paint.Style.FILL
                        }
                        val textWidth = badgePaint.measureText(coordBadge)
                        val badgeCenterX = activeDragComp.x + def.width / 2f
                        val badgeY = activeDragComp.y - 10f
                        canvas.nativeCanvas.drawRoundRect(
                            badgeCenterX - textWidth / 2f - 6f,
                            badgeY - 14f,
                            badgeCenterX + textWidth / 2f + 6f,
                            badgeY + 4f,
                            4f,
                            4f,
                            badgeBg
                        )
                        canvas.nativeCanvas.drawText(coordBadge, badgeCenterX, badgeY, badgePaint)
                    }
                }

                // 3. Draw Wires (Stepped Manhattan orthogonal routing) with clipping-aware culling
                for (w in wires) {
                    val fromComp = components.find { it.id == w.fromComponentId } ?: continue
                    val toComp = components.find { it.id == w.toComponentId } ?: continue
                    val fromDef = ComponentRegistry.get(fromComp.type) ?: continue
                    val toDef = ComponentRegistry.get(toComp.type) ?: continue

                    val fromPort = (fromDef.outputs + fromDef.inputs).find { it.id == w.fromPortId } ?: continue
                    val toPort = (toDef.inputs + toDef.outputs).find { it.id == w.toPortId } ?: continue

                    val p1 = getPortPosition(fromComp, fromPort)
                    val p2 = getPortPosition(toComp, toPort)

                    // Clipping-aware wire culling: skip drawing wires completely off-screen
                    if (!ViewportCullingSystem.isWireVisible(p1, p2, visibleWorldBounds)) {
                        continue
                    }

                    val isHigh = w.state == DigitalState.HIGH
                    val isLow = w.state == DigitalState.LOW
                    val wireColor = when (w.state) {
                        DigitalState.HIGH -> if (isSignalFlowMode) SignalFlowHigh else SignalHigh
                        DigitalState.LOW -> if (isSignalFlowMode) SignalFlowLow else SignalLow
                        DigitalState.UNKNOWN -> SignalUnknown
                    }
                    val strokeW = if (w.id == selectedWireId) 3.8f else 2.2f

                    // Manhattan orthogonal routing
                    val midX = (p1.x + p2.x) / 2
                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(midX, p1.y)
                        lineTo(midX, p2.y)
                        lineTo(p2.x, p2.y)
                    }

                    // Glow behind selected wire or active high signal
                    if (w.id == selectedWireId) {
                        drawPath(path, Color(0xFF58A6FF).copy(alpha = 0.4f), style = Stroke(width = strokeW + 4f, cap = StrokeCap.Round))
                    } else if (isSignalFlowMode && isHigh) {
                        drawPath(path, SignalFlowHigh.copy(alpha = 0.22f), style = Stroke(width = strokeW + 3f, cap = StrokeCap.Round))
                    }

                    drawPath(path, wireColor, style = Stroke(width = strokeW, cap = StrokeCap.Round))

                    // Terminal connection dots
                    drawCircle(wireColor, radius = 3.6f, center = p1)
                    drawCircle(wireColor, radius = 3.6f, center = p2)

                    // Animated neon signal pulses traveling along wire during simulation (only for active HIGH logic)
                    if (isSimulating && isHigh) {
                        listOf(pulsePhase, (pulsePhase + 0.5f) % 1.0f).forEach { t ->
                            val pulsePos = when {
                                t < 0.33f -> {
                                    val subT = t / 0.33f
                                    Offset(p1.x + (midX - p1.x) * subT, p1.y)
                                }
                                t < 0.66f -> {
                                    val subT = (t - 0.33f) / 0.33f
                                    Offset(midX, p1.y + (p2.y - p1.y) * subT)
                                }
                                else -> {
                                    val subT = (t - 0.66f) / 0.34f
                                    Offset(midX + (p2.x - midX) * subT, p2.y)
                                }
                            }
                            // Glowing emerald green electron packet
                            drawCircle(SignalFlowHigh.copy(alpha = 0.5f), radius = 5.2f, center = pulsePos)
                            drawCircle(Color.White, radius = 2.6f, center = pulsePos)
                        }
                    }
                }

                // 4. Draw Pending Wire being dragged
                if (pendingWire != null) {
                    val p1 = pendingWire!!.startWorldPos
                    val p2 = pendingWire!!.currentWorldPos
                    val midX = (p1.x + p2.x) / 2
                    val pendingPath = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(midX, p1.y)
                        lineTo(midX, p2.y)
                        lineTo(p2.x, p2.y)
                    }
                    drawPath(pendingPath, Color(0xFF58A6FF), style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                    drawCircle(Color(0xFF58A6FF), radius = 5f, center = p2)

                    // Target port snap halo if within range
                    val nearbyPort = findPortAt(p2, radius = 32f / zoomScale)
                    if (nearbyPort != null) {
                        val snapPortPos = getPortPosition(nearbyPort.first, nearbyPort.second)
                        drawCircle(SignalHigh, radius = 9f, center = snapPortPos, style = Stroke(width = 2f))
                    }
                }

                // 5. Draw Components & Symbols with clipping-aware culling
                for (comp in components) {
                    val def = ComponentRegistry.get(comp.type) ?: continue

                    // Clipping-aware component culling: skip off-screen components
                    if (!ViewportCullingSystem.isComponentVisible(comp, def, visibleWorldBounds)) {
                        continue
                    }

                    val isSelected = comp.id == selectedComponentId

                    // Draw component graphical symbol
                    GateSymbols.drawComponent(this, comp, def, isSelected)

                    // Draw Component Label (LOD aware)
                    if (renderCompLabels) {
                        val labelText = comp.label.ifBlank { def.name }
                        canvas.nativeCanvas.drawText(
                            labelText,
                            comp.x + def.width / 2,
                            comp.y - 6f,
                            textPaint
                        )
                    }

                    // Draw ports and pin labels
                    val allPorts = def.inputs + def.outputs
                    for (port in allPorts) {
                        val pos = getPortPosition(comp, port)
                        val portState = if (port.type == PortType.INPUT) {
                            comp.inputValues[port.id] ?: DigitalState.UNKNOWN
                        } else {
                            comp.outputValues[port.id] ?: DigitalState.UNKNOWN
                        }
                        val portDotColor = when (portState) {
                            DigitalState.HIGH -> if (isSignalFlowMode) SignalFlowHigh else SignalHigh
                            DigitalState.LOW -> if (isSignalFlowMode) SignalFlowLow else SignalLow
                            DigitalState.UNKNOWN -> SignalUnknown
                        }

                        val isTappedSource = (tappedSourcePort?.first?.id == comp.id && tappedSourcePort?.second?.id == port.id)

                        // Highlight when in drawing mode or when a pin is waiting for connection
                        if (isTappedSource) {
                            // Bright pulsating ring on the selected 1st pin
                            drawCircle(
                                color = SignalHigh.copy(alpha = 0.35f + 0.35f * pulsePhase),
                                radius = 13f + 4f * pulsePhase,
                                center = pos
                            )
                            drawCircle(
                                color = SignalHigh,
                                radius = 8.5f,
                                center = pos,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f)
                            )
                        } else if (currentIsWireDrawingMode || tappedSourcePort != null) {
                            // Pulsing target halo around all connectable gate terminals
                            drawCircle(
                                color = Color(0xFF58A6FF).copy(alpha = 0.3f + 0.25f * pulsePhase),
                                radius = 8f + 2f * pulsePhase,
                                center = pos,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4f)
                            )
                        }

                        // Terminal lead stub
                        drawCircle(if (isTappedSource) SignalHigh else portDotColor, radius = if (isTappedSource) 5.5f else 3.6f, center = pos)

                        // Port label (LOD aware: culled when zoomed out)
                        if (renderPortLabels &&
                            def.category != ComponentCategory.BASIC_GATES &&
                            comp.type != "SWITCH" && comp.type != "LED" && comp.type != "CLOCK" && comp.type != "CONST_HIGH" && comp.type != "CONST_LOW"
                        ) {
                            val isLeft = port.relativeX < 0.5f
                            portPaint.textAlign = if (isLeft) Paint.Align.LEFT else Paint.Align.RIGHT
                            val labelX = if (isLeft) pos.x + 6f else pos.x - 6f
                            canvas.nativeCanvas.drawText(port.name, labelX, pos.y + 3.5f, portPaint)
                        }
                    }

                    // For Switch: draw status badge
                    if (comp.type == "SWITCH") {
                        val swText = if (comp.switchState) "ON [1]" else "OFF [0]"
                        val swP = if (comp.switchState) swOnPaint else swOffPaint
                        canvas.nativeCanvas.drawText(swText, comp.x + def.width / 2, comp.y + def.height + 13f, swP)
                    }

                    // For Digital Probe: draw state text
                    if (comp.type == "DIGITAL_PROBE") {
                        val probeState = comp.inputValues["IN"] ?: DigitalState.UNKNOWN
                        val prPaint = when (probeState) {
                            DigitalState.HIGH -> probeHighPaint
                            DigitalState.LOW -> probeLowPaint
                            DigitalState.UNKNOWN -> probeUnknownPaint
                        }
                        canvas.nativeCanvas.drawText(probeState.symbol, comp.x + def.width / 2, comp.y + def.height / 2 + 5f, prPaint)
                    }

                    // If selected on grid, draw glowing boundary, corner brackets, and double-tap action hint
                    if (isSelected) {
                        val selPad = 8f
                        val l = comp.x - selPad
                        val t = comp.y - selPad
                        val r = comp.x + def.width + selPad
                        val b = comp.y + def.height + selPad

                        // Glowing animated breathing halo
                        val breathAlpha = 0.12f + 0.10f * kotlin.math.sin(pulsePhase * kotlin.math.PI.toFloat())
                        drawRoundRect(
                            color = LabAccentBlue.copy(alpha = breathAlpha),
                            topLeft = Offset(l - 2f, t - 2f),
                            size = androidx.compose.ui.geometry.Size(r - l + 4f, b - t + 4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                        )
                        // Crisp selection border
                        drawRoundRect(
                            color = LabAccentBlue,
                            topLeft = Offset(l, t),
                            size = androidx.compose.ui.geometry.Size(r - l, b - t),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
                            style = Stroke(width = 2.2f)
                        )

                        // Corner accent brackets
                        val bracketLen = 9f
                        drawLine(Color.White, Offset(l, t), Offset(l + bracketLen, t), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(l, t), Offset(l, t + bracketLen), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(r, t), Offset(r - bracketLen, t), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(r, t), Offset(r, t + bracketLen), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(l, b), Offset(l + bracketLen, b), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(l, b), Offset(l, b - bracketLen), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(r, b), Offset(r - bracketLen, b), strokeWidth = 2.4f)
                        drawLine(Color.White, Offset(r, b), Offset(r, b - bracketLen), strokeWidth = 2.4f)

                        // Subtitle hint: Double-tap for info & delete
                        val hintY = if (comp.type == "SWITCH") comp.y + def.height + 25f else comp.y + def.height + 15f
                        canvas.nativeCanvas.drawText("Double-tap for info & delete", comp.x + def.width / 2, hintY, hintPaint)
                    }
                }

                // 6. Draw Ghost of New Gate being dragged onto 2D grid from palette
                if (draggingNewGate != null) {
                    val def = draggingNewGate!!.definition
                    val pos = draggingNewGate!!.snappedWorldPos
                    val ghostComp = CircuitComponent(
                        id = "preview",
                        type = def.type,
                        x = pos.x,
                        y = pos.y,
                        label = def.name
                    )

                    // Draw ghost grid crosshair
                    drawLine(
                        color = Color(0xFF58A6FF).copy(alpha = 0.4f),
                        start = Offset(pos.x - 80f, pos.y),
                        end = Offset(pos.x + def.width + 80f, pos.y),
                        strokeWidth = 1.2f
                    )
                    drawLine(
                        color = Color(0xFF58A6FF).copy(alpha = 0.4f),
                        start = Offset(pos.x, pos.y - 80f),
                        end = Offset(pos.x, pos.y + def.height + 80f),
                        strokeWidth = 1.2f
                    )

                    // Draw ghost component
                    GateSymbols.drawComponent(this, ghostComp, def, isSelected = true)

                    // Draw drop coordinate badge
                    val coordText = "(${pos.x.roundToInt()}, ${pos.y.roundToInt()})"
                    canvas.nativeCanvas.drawText(coordText, pos.x + def.width / 2, pos.y - 8f, textPaint)
                }

                canvas.restore()
            }
            } // close clipRect
        }

        // 1. Engineering CAD HUD Telemetry Overlay (Theme-based, TopStart)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 8.dp, start = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = LabCardBg.copy(alpha = 0.90f),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
                modifier = Modifier.testTag("2d_cad_telemetry_hud")
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
                            .background(SignalHigh)
                    )
                    Text(
                        text = "2D CAD • Zoom ${(zoomScale * 100).roundToInt()}% • Gates: ${components.size} • Wires: ${wires.size}${if (snapToGrid) " • SNAP" else ""}",
                        color = LabTextSecondary,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Floating Wire Drawing Mode Status Banner (Centered, zero collision)
        AnimatedVisibility(
            visible = currentIsWireDrawingMode || tappedSourcePort != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        ) {
            Surface(
                color = LabPanelBg.copy(alpha = 0.95f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (tappedSourcePort != null) SignalHigh else LabAccentBlue
                ),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp)
                    .testTag("wire_drawing_banner")
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (tappedSourcePort != null) SignalHigh else LabAccentBlue)
                    )
                    Text(
                        text = if (tappedSourcePort != null) {
                            "Pin 1: ${tappedSourcePort!!.first.label.ifBlank { tappedSourcePort!!.first.type }} (${tappedSourcePort!!.second.name}) → Tap destination"
                        } else {
                            "Tap two pins to wire"
                        },
                        color = LabTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = if (tappedSourcePort != null) "Cancel" else "Exit",
                        color = SignalError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                tappedSourcePort = null
                                if (currentIsWireDrawingMode) {
                                    onToggleWireDrawingMode?.invoke()
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("cancel_wire_drawing_btn")
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableGateChip(
    definition: ComponentDefinition,
    onTapAdd: () -> Unit,
    onDragProgress: (Offset) -> Unit,
    onDragFinish: () -> Unit,
    onDragCancel: () -> Unit
) {
    var isDraggingThis by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .shadow(2.dp, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .background(if (isDraggingThis) LabAccentBlue.copy(alpha = 0.3f) else LabCardBg)
            .border(1.dp, if (isDraggingThis) LabAccentBlue else LabBorder, RoundedCornerShape(6.dp))
            .pointerInput(definition) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val startPos = down.position
                    var movedPastSlop = false
                    val touchSlop = viewConfiguration.touchSlop

                    while (true) {
                        val event = awaitPointerEvent()
                        val active = event.changes.filter { it.pressed }
                        if (active.isEmpty()) break

                        val pointer = active[0]
                        val totalDelta = pointer.position - startPos

                        if (!movedPastSlop && totalDelta.getDistance() > touchSlop) {
                            movedPastSlop = true
                            isDraggingThis = true
                        }

                        if (movedPastSlop) {
                            // Track global drag position
                            onDragProgress(pointer.position)
                            pointer.consume()
                        }
                    }

                    if (movedPastSlop) {
                        isDraggingThis = false
                        onDragFinish()
                    } else {
                        // Regular Tap adds to viewport center
                        onTapAdd()
                    }
                }
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("palette_chip_${definition.type.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1B2838)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = definition.type.take(2),
                    color = LabAccentBlue,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = definition.name,
                color = LabTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
