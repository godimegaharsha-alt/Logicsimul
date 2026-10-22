package com.example.logiclab.gl

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewConfiguration
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Custom GLSurfaceView hosting the hardware-accelerated 3D circuit renderer.
 * Handles touch gestures (orbit rotate, pan, pinch zoom, and switch tap interactions).
 */
@SuppressLint("ClickableViewAccessibility")
class CircuitGLSurfaceView(
    context: Context,
    val renderer: CircuitGLRenderer = CircuitGLRenderer(context)
) : GLSurfaceView(context) {

    var onSwitchToggled: ((String) -> Unit)? = null
    var onCameraDistanceChanged: ((Float) -> Unit)? = null
    var onComponentSelected: ((String?) -> Unit)? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private var previousX = 0f
    private var previousY = 0f
    private var downX = 0f
    private var downY = 0f
    private var isDragging = false

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor > 0f) {
                val newDist = (renderer.cameraDistance / scaleFactor).coerceIn(100f, 3500f)
                renderer.cameraDistance = newDist
                onCameraDistanceChanged?.invoke(newDist)
            }
            return true
        }
    })

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    fun updateCircuit(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        isSimulating: Boolean,
        board3DStyle: com.example.logiclab.model.Board3DStyle = com.example.logiclab.model.Board3DStyle.PCB_LAB
    ) {
        renderer.components = components
        renderer.wires = wires
        renderer.isSimulating = isSimulating
        renderer.board3DStyle = board3DStyle
    }

    fun setBoard3DStyle(style: com.example.logiclab.model.Board3DStyle) {
        renderer.board3DStyle = style
    }

    fun setTransitionProgress(progress: Float) {
        renderer.transitionProgress = progress
    }

    fun setCameraPreset(pitch: Float, yaw: Float, distance: Float) {
        renderer.cameraPitch = pitch
        renderer.cameraYaw = yaw
        renderer.cameraDistance = distance
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = x
                downY = y
                previousX = x
                previousY = y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY
                val distFromDown = sqrt((x - downX) * (x - downX) + (y - downY) * (y - downY))

                if (distFromDown > touchSlop) {
                    isDragging = true
                }

                if (event.pointerCount == 1 && isDragging) {
                    // 1 Finger: Orbit camera (Yaw and Pitch)
                    // If in 3D mode, rotate pitch & yaw
                    if (renderer.transitionProgress > 0.05f) {
                        renderer.cameraYaw = (renderer.cameraYaw + dx * 0.4f).let {
                            if (it > 180f) it - 360f else if (it < -180f) it + 360f else it
                        }
                        renderer.cameraPitch = (renderer.cameraPitch - dy * 0.3f).coerceIn(15f, 85f)
                    } else {
                        // In 2D Grid mode, pan the view
                        renderer.panTargetX -= dx * 1.2f
                        renderer.panTargetY += dy * 1.2f
                    }
                } else if (event.pointerCount >= 2 && !scaleDetector.isInProgress) {
                    // 2 Fingers pan (only when not actively pinching to zoom)
                    renderer.panTargetX -= dx * 0.8f
                    renderer.panTargetY += dy * 0.8f
                }

                previousX = x
                previousY = y
            }
            MotionEvent.ACTION_UP -> {
                val distFromDown = sqrt((x - downX) * (x - downX) + (y - downY) * (y - downY))
                val isTap = !isDragging || distFromDown <= touchSlop * 1.2f
                if (isTap && event.pointerCount == 1) {
                    val hitComp = renderer.hitTestComponent(x, y)
                    if (hitComp != null) {
                        onComponentSelected?.invoke(hitComp.id)
                        if (hitComp.type == "SWITCH") {
                            onSwitchToggled?.invoke(hitComp.id)
                        }
                    } else {
                        onComponentSelected?.invoke(null)
                    }
                }
            }
        }
        return true
    }

    fun zoomIn() {
        val newDist = (renderer.cameraDistance * 0.82f).coerceIn(100f, 3500f)
        renderer.cameraDistance = newDist
        onCameraDistanceChanged?.invoke(newDist)
    }

    fun zoomOut() {
        val newDist = (renderer.cameraDistance * 1.22f).coerceIn(100f, 3500f)
        renderer.cameraDistance = newDist
        onCameraDistanceChanged?.invoke(newDist)
    }

    fun resetZoom() {
        renderer.cameraDistance = 980f
        renderer.panTargetX = 0f
        renderer.panTargetY = 0f
        onCameraDistanceChanged?.invoke(980f)
    }
}
