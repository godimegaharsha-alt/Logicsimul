package com.example.logiclab.ui.canvas

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Viewport State representing camera orientation, projection parameters,
 * and 3D extrusion levels interpolated between 2D Grid Schematic and 3D Isometric View.
 */
data class ViewportInterpolatedState(
    val transitionProgress: Float, // 0.0f (2D Grid) to 1.0f (3D Isometric)
    val pitchDeg: Float,           // 89.5° (overhead) -> 45.0° (isometric)
    val yawDeg: Float,             // 0.0° -> 42.0°
    val cameraDistance: Float,     // Camera orbit distance / zoom
    val panOffset: Offset,         // Panning translation
    val extrusionScale: Float,     // 0.02f (flat on board) -> 1.0f (full 3D height)
    val wireArcScale: Float,       // 0.0f (flat PCB trace) -> 1.0f (3D parabolic arc)
    val gridBlend: Float,          // 0.0f (Technical Grid) -> 1.0f (FR4 Solder Mask PCB)
    val fovDegrees: Float,         // 18.0° (Ortho-like telephoto) -> 45.0° (Perspective)
    val isIsometric: Boolean
) {
    /**
     * Eye vector coordinates in 3D space looking at target (panOffset.x, panOffset.y, 0).
     */
    fun calculateEyePosition(targetX: Float, targetY: Float): FloatArray {
        val pitchRad = (pitchDeg * PI / 180.0).toFloat()
        val yawRad = (yawDeg * PI / 180.0).toFloat()

        val eyeX = targetX + cameraDistance * cos(pitchRad) * sin(yawRad)
        val eyeY = targetY + cameraDistance * cos(pitchRad) * cos(yawRad)
        val eyeZ = cameraDistance * sin(pitchRad)
        return floatArrayOf(eyeX, eyeY, eyeZ)
    }
}

/**
 * Camera View Angle Presets.
 */
enum class ViewportPreset(
    val label: String,
    val pitch: Float,
    val yaw: Float,
    val distanceMultiplier: Float,
    val is3D: Boolean
) {
    TWO_D_GRID("2D GRID", 89.5f, 0.0f, 1.4f, false),
    ISOMETRIC_45("ISO 45°", 45.0f, 42.0f, 1.0f, true),
    TOP_CAD("TOP 3D", 82.0f, 0.0f, 1.15f, true),
    SIDE_PROFILE("SIDE 65°", 25.0f, 60.0f, 0.92f, true),
    RESET("RESET", 45.0f, 42.0f, 1.0f, true)
}

/**
 * Animation Controller that smoothly interpolates the viewport state
 * between the 2D grid schematic and an isometric 3D projection for logic circuits.
 */
@Stable
class ViewportAnimationController(
    initialIs3D: Boolean = false,
    private val coroutineScope: CoroutineScope
) {
    private val animatable = Animatable(if (initialIs3D) 1.0f else 0.0f)

    // Current camera target overrides (for user interactive drag/pinch during 3D)
    var customPitch by mutableStateOf<Float?>(null)
    var customYaw by mutableStateOf<Float?>(null)
    var customDistance by mutableStateOf<Float?>(null)
    var customPan by mutableStateOf(Offset.Zero)

    // Active state derived from animatable progress
    val progress: Float get() = animatable.value
    val isAnimating: Boolean get() = animatable.isRunning
    val is3DMode: Boolean get() = progress >= 0.5f

    /**
     * Computes the smoothly interpolated viewport state for the current animation frame.
     */
    val currentState: ViewportInterpolatedState
        get() = interpolate(progress)

    /**
     * Interpolates all viewport parameters continuously for any progress t in [0.0, 1.0].
     */
    fun interpolate(t: Float): ViewportInterpolatedState {
        val clampedT = t.coerceIn(0f, 1f)

        // Interpolate Pitch: from 89.5° (2D straight down) to 45.0° (or custom user pitch in 3D)
        val target3DPitch = customPitch ?: 45.0f
        val pitch = lerp(89.5f, target3DPitch, clampedT)

        // Interpolate Yaw: from 0.0° (north-aligned 2D) to 42.0° (or custom user yaw in 3D)
        val target3DYaw = customYaw ?: 42.0f
        val yaw = lerp(0.0f, target3DYaw, clampedT)

        // Interpolate Camera Distance / Zoom
        val baseDist = customDistance ?: 980f
        val distance = lerp(baseDist * 1.5f, baseDist, clampedT)

        // Component 3D Height Extrusion: flat on 2D board -> fully extruded 3D geometry
        val extrusion = lerp(0.02f, 1.0f, clampedT)

        // Wire Arc Height: flat 2D traces -> 3D elevated parabolic jumpers
        val wireArc = lerp(0.0f, 1.0f, clampedT)

        // Grid Style: 0.0f (technical schematic grid) -> 1.0f (3D FR4 dark green solder mask)
        val grid = clampedT

        // Field of View: narrow telephoto (ortho-like) -> wide perspective depth
        val fov = lerp(18.0f, 45.0f, clampedT)

        return ViewportInterpolatedState(
            transitionProgress = clampedT,
            pitchDeg = pitch,
            yawDeg = yaw,
            cameraDistance = distance,
            panOffset = customPan,
            extrusionScale = extrusion,
            wireArcScale = wireArc,
            gridBlend = grid,
            fovDegrees = fov,
            isIsometric = clampedT >= 0.5f
        )
    }

    /**
     * Smoothly animates the viewport into 3D Isometric View.
     */
    fun animateTo3D(
        durationMs: Int = 680,
        easing: Easing = FastOutSlowInEasing,
        onFinished: (() -> Unit)? = null
    ) {
        coroutineScope.launch {
            animatable.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = durationMs, easing = easing)
            )
            onFinished?.invoke()
        }
    }

    /**
     * Smoothly animates the viewport into 2D Technical Grid View.
     */
    fun animateTo2D(
        durationMs: Int = 680,
        easing: Easing = FastOutSlowInEasing,
        onFinished: (() -> Unit)? = null
    ) {
        coroutineScope.launch {
            animatable.animateTo(
                targetValue = 0.0f,
                animationSpec = tween(durationMillis = durationMs, easing = easing)
            )
            onFinished?.invoke()
        }
    }

    /**
     * Smoothly animates the viewport to target 2D or 3D mode.
     */
    fun animateTo(
        targetIs3D: Boolean,
        durationMs: Int = 680,
        easing: Easing = FastOutSlowInEasing,
        onFinished: (() -> Unit)? = null
    ) {
        if (targetIs3D) {
            animateTo3D(durationMs, easing, onFinished)
        } else {
            animateTo2D(durationMs, easing, onFinished)
        }
    }

    /**
     * Toggles between 2D and 3D with animation.
     */
    fun toggle2D3D(durationMs: Int = 680, onFinished: (() -> Unit)? = null) {
        if (is3DMode) {
            animateTo2D(durationMs, onFinished = onFinished)
        } else {
            animateTo3D(durationMs, onFinished = onFinished)
        }
    }

    /**
     * Smoothly animates camera to a predefined viewpoint preset.
     */
    fun animateToPreset(preset: ViewportPreset, durationMs: Int = 550) {
        customPitch = preset.pitch
        customYaw = preset.yaw
        if (!preset.is3D) {
            animateTo2D(durationMs)
        } else {
            animateTo3D(durationMs)
        }
    }

    /**
     * Snaps instantly to 2D or 3D without animation.
     */
    fun snapTo(is3D: Boolean) {
        coroutineScope.launch {
            animatable.snapTo(if (is3D) 1.0f else 0.0f)
        }
    }

    private fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }
}

/**
 * Composable hook to instantiate and remember a ViewportAnimationController.
 */
@Composable
fun rememberViewportAnimationController(
    initialIs3D: Boolean = false
): ViewportAnimationController {
    val scope = rememberCoroutineScope()
    return remember(initialIs3D) {
        ViewportAnimationController(initialIs3D, scope)
    }
}
