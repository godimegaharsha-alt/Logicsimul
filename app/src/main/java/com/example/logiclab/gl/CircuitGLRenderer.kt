package com.example.logiclab.gl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.Board3DStyle
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * OpenGL ES 2.0 Hardware-Accelerated 3D Renderer for Digital Circuits.
 * Renders circuit components, IC packages, switches, LEDs, and 3D jumper cables
 * with smooth animated transition between 2D technical grid and 3D isometric CAD view.
 */
class CircuitGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // Shader Programs
    private var phongProgram = 0
    private var unlitProgram = 0
    private var pointProgram = 0

    // Phong Uniform & Attribute Handles
    private var uMVPMatrixHandle = 0
    private var uModelMatrixHandle = 0
    private var uNormalMatrixHandle = 0
    private var uLightPosHandle = 0
    private var uViewPosHandle = 0
    private var uEmissionHandle = 0
    private var uAmbientStrengthHandle = 0
    private var uSpecularStrengthHandle = 0
    private var aPositionHandle = 0
    private var aNormalHandle = 0
    private var uColorHandle = 0

    // Selected Component Highlighting
    @Volatile var selectedComponentId: String? = null

    // Unlit Uniform Handles
    private var uUnlitMVPHandle = 0
    private var uUnlitAlphaHandle = 0
    private var aUnlitPosHandle = 0
    private var uUnlitColorHandle = 0

    // Point Uniform Handles
    private var uPointMVPHandle = 0
    private var uPointSizeHandle = 0
    private var aPointPosHandle = 0
    private var uPointColorHandle = 0

    // Meshes
    private lateinit var unitBox: GLGeometry.UnitBoxMesh
    private lateinit var domeMesh: GLGeometry.DomeMesh
    private lateinit var cylinderMesh: GLGeometry.CylinderMesh

    // 3D Style (PCB Lab vs IC Trainer Kit)
    @Volatile var board3DStyle: Board3DStyle = Board3DStyle.PCB_LAB

    // Matrices
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)
    private val normalMatrix = FloatArray(9)
    private val tempMatrix = FloatArray(16)

    // Camera & Viewport State
    private var viewportWidth = 1080
    private var viewportHeight = 1920

    // Interactive Camera Parameters
    var cameraPitch = 45f       // 15° to 89.5°
    var cameraYaw = 42f         // -180° to 180°
    var cameraDistance = 980f   // Zoom distance
    var panTargetX = 0f         // Board center target offset X
    var panTargetY = 0f         // Board center target offset Y

    // Animated Transition State (0.0f = pure 2D technical grid, 1.0f = pure 3D isometric view)
    var transitionProgress = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    // Circuit Data (Updated on UI/simulation events)
    @Volatile var components: List<CircuitComponent> = emptyList()
    @Volatile var wires: List<CircuitWire> = emptyList()
    @Volatile var isSimulating: Boolean = true

    // Pulse animation timer for 3D wires
    private var animationTime = 0f
    private var lastFrameTimeNs = System.nanoTime()

    // FPS Counter
    var currentFps = 60
    private var frameCount = 0
    private var fpsTimer = System.currentTimeMillis()

    // Technical Grid Buffer
    private var gridBuffer: FloatBuffer? = null
    private var gridPointCount = 0
    private var lastBoardBounds = FloatArray(4) // minX, minY, maxX, maxY

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.047f, 0.063f, 0.090f, 1.0f) // #0C1017 Lab Dark BG
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthFunc(GLES20.GL_LEQUAL)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Initialize Shaders
        phongProgram = GLShaderUtils.createProgram(
            GLShaderUtils.VERTEX_SHADER_PHONG,
            GLShaderUtils.FRAGMENT_SHADER_PHONG
        )
        uMVPMatrixHandle = GLES20.glGetUniformLocation(phongProgram, "uMVPMatrix")
        uModelMatrixHandle = GLES20.glGetUniformLocation(phongProgram, "uModelMatrix")
        uNormalMatrixHandle = GLES20.glGetUniformLocation(phongProgram, "uNormalMatrix")
        uLightPosHandle = GLES20.glGetUniformLocation(phongProgram, "uLightPos")
        uViewPosHandle = GLES20.glGetUniformLocation(phongProgram, "uViewPos")
        uEmissionHandle = GLES20.glGetUniformLocation(phongProgram, "uEmission")
        uAmbientStrengthHandle = GLES20.glGetUniformLocation(phongProgram, "uAmbientStrength")
        uSpecularStrengthHandle = GLES20.glGetUniformLocation(phongProgram, "uSpecularStrength")
        aPositionHandle = GLES20.glGetAttribLocation(phongProgram, "aPosition")
        aNormalHandle = GLES20.glGetAttribLocation(phongProgram, "aNormal")
        uColorHandle = GLES20.glGetUniformLocation(phongProgram, "uColor")

        unlitProgram = GLShaderUtils.createProgram(
            GLShaderUtils.VERTEX_SHADER_UNLIT,
            GLShaderUtils.FRAGMENT_SHADER_UNLIT
        )
        uUnlitMVPHandle = GLES20.glGetUniformLocation(unlitProgram, "uMVPMatrix")
        uUnlitAlphaHandle = GLES20.glGetUniformLocation(unlitProgram, "uAlphaMultiplier")
        aUnlitPosHandle = GLES20.glGetAttribLocation(unlitProgram, "aPosition")
        uUnlitColorHandle = GLES20.glGetUniformLocation(unlitProgram, "uColor")

        pointProgram = GLShaderUtils.createProgram(
            GLShaderUtils.VERTEX_SHADER_POINT,
            GLShaderUtils.FRAGMENT_SHADER_POINT
        )
        uPointMVPHandle = GLES20.glGetUniformLocation(pointProgram, "uMVPMatrix")
        uPointSizeHandle = GLES20.glGetUniformLocation(pointProgram, "uPointSize")
        aPointPosHandle = GLES20.glGetAttribLocation(pointProgram, "aPosition")
        uPointColorHandle = GLES20.glGetUniformLocation(pointProgram, "uColor")

        // Initialize Meshes
        unitBox = GLGeometry.UnitBoxMesh()
        domeMesh = GLGeometry.DomeMesh(16, 8)
        cylinderMesh = GLGeometry.CylinderMesh(18)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // Compute delta time for pulse animation & FPS
        val now = System.nanoTime()
        val dt = (now - lastFrameTimeNs) / 1_000_000_000f
        lastFrameTimeNs = now
        animationTime += dt

        frameCount++
        if (System.currentTimeMillis() - fpsTimer >= 1000) {
            currentFps = frameCount
            frameCount = 0
            fpsTimer = System.currentTimeMillis()
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Calculate circuit bounds
        val currentComps = components
        val currentWires = wires

        val minX: Float
        val maxX: Float
        val minY: Float
        val maxY: Float
        if (currentComps.isNotEmpty()) {
            minX = currentComps.minOf { it.x } - 100f
            maxX = currentComps.maxOf { it.x + 140f } + 100f
            minY = currentComps.minOf { it.y } - 100f
            maxY = currentComps.maxOf { it.y + 120f } + 100f
        } else {
            minX = 0f
            maxX = 800f
            minY = 0f
            maxY = 600f
        }
        val boardCenterX = (minX + maxX) / 2f + panTargetX
        val boardCenterY = (minY + maxY) / 2f + panTargetY
        val boardWidth = max(900f, maxX - minX)
        val boardHeight = max(700f, maxY - minY)

        // Interpolate camera parameters between 2D Top-Down and 3D Isometric
        // 2D View: pitch = 89.5f (looking straight down), yaw = 0f (north aligned)
        // 3D View: pitch = cameraPitch (~45°), yaw = cameraYaw (~42°)
        val effectivePitch = lerp(89.5f, cameraPitch, transitionProgress)
        val effectiveYaw = lerp(0f, cameraYaw, transitionProgress)
        val extrusionScale = lerp(0.04f, 1.0f, transitionProgress) // Extrude components in 3D
        val wireArcScale = lerp(0.0f, 1.0f, transitionProgress)    // Arch wires in 3D

        // Setup Projection Matrix (Blend from Ortho-like Telephoto to 3D Perspective)
        val aspect = viewportWidth.toFloat() / max(1f, viewportHeight.toFloat())
        val fovY = lerp(20f, 45f, transitionProgress)
        val near = 10f
        val far = 5000f
        Matrix.perspectiveM(projectionMatrix, 0, fovY, aspect, near, far)

        // Setup View Matrix (Orbit Camera around center)
        val pitchRad = (effectivePitch * PI / 180.0).toFloat()
        val yawRad = (effectiveYaw * PI / 180.0).toFloat()

        val eyeDist = cameraDistance * lerp(1.6f, 1.0f, transitionProgress)
        val eyeX = boardCenterX + eyeDist * cos(pitchRad) * sin(yawRad)
        val eyeY = boardCenterY + eyeDist * cos(pitchRad) * cos(yawRad)
        val eyeZ = eyeDist * sin(pitchRad)

        val targetX = boardCenterX
        val targetY = boardCenterY
        val targetZ = 0f

        val upX = 0f
        val upY = if (effectivePitch > 89f) 1f else 0f
        val upZ = if (effectivePitch > 89f) 0f else 1f

        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, targetX, targetY, targetZ, upX, upY, upZ)

        // 1. Render 3D PCB Board Platform
        renderPCBBoard(boardCenterX, boardCenterY, boardWidth, boardHeight, eyeX, eyeY, eyeZ, extrusionScale)

        // 2. Render Technical Dotted Grid on Board Surface
        renderTechnicalGrid(boardCenterX, boardCenterY, boardWidth, boardHeight)

        // 3. Render Circuit Components (DIP chips, switches, LEDs, 7-seg displays)
        for (comp in currentComps) {
            renderComponent3D(comp, eyeX, eyeY, eyeZ, extrusionScale)
        }

        // 4. Render 3D Jumper Cables and Flowing Electron Pulses
        renderWires3D(currentComps, currentWires, wireArcScale)
    }

    private fun renderPCBBoard(
        cx: Float, cy: Float, width: Float, height: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val thickness = 18f * extrusionScale
        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 300f, cy - 400f, 600f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, 0.0f)

        if (board3DStyle == Board3DStyle.IC_TRAINER_KIT) {
            // ============================================================
            // DIGITAL IC TRAINER KIT APPARATUS WORKBENCH
            // ============================================================
            // 1. Solid Pine/Oak Wooden Briefcase Chassis Frame (#C08344)
            GLES20.glUniform1f(uAmbientStrengthHandle, 0.45f)
            GLES20.glUniform1f(uSpecularStrengthHandle, 0.35f)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, cy, -thickness / 2f)
            Matrix.scaleM(modelMatrix, 0, width + 50f, height + 50f, thickness + 6f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.75f, 0.51f, 0.27f, 1.0f) // Warm golden pine wood

            GLES20.glEnableVertexAttribArray(aPositionHandle)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
            GLES20.glEnableVertexAttribArray(aNormalHandle)
            GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // 2. Educational Soldermask Deep Green Faceplate (#0E4A35)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, cy, -thickness / 2f + 2f)
            Matrix.scaleM(modelMatrix, 0, width + 30f, height + 30f, thickness)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.055f, 0.29f, 0.208f, 1.0f) // Deep trainer green faceplate
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // 3. 5 Cyan/Teal ZIF Sockets (Turquoise #009688)
            val bbWidth = width * 0.88f
            val bbHeight = height * 0.72f
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, cy, 3f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, bbWidth, bbHeight, 6f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.0f, 0.59f, 0.53f, 1.0f) // Cyan/Teal ZIF sockets
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // 4. Center Breadboard IC Trench / Groove
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, cy, 4.5f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, bbWidth, 12f, 4f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.22f, 0.24f, 0.28f, 1.0f) // Breadboard divider trench
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // 5. Red (+5V) and Blue (GND) Power Bus Strips along Breadboard Edges
            val busTopY = cy - bbHeight / 2f + 8f
            val busBotY = cy + bbHeight / 2f - 8f

            // Red VCC line (+5V)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, busTopY, 4.5f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, bbWidth - 20f, 3f, 3f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.88f, 0.15f, 0.15f, 1.0f) // Red VCC bus
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // Blue GND line
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, busBotY, 4.5f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, bbWidth - 20f, 3f, 3f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.15f, 0.35f, 0.88f, 1.0f) // Blue GND bus
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

            // 6. DC Power Supply 4mm Banana Binding Posts (+5V Red, GND Black, -5V Blue, +12V Yellow)
            val postStartX = cx - width * 0.38f
            val postY = cy - height * 0.42f
            val postColors = listOf(
                floatArrayOf(0.92f, 0.12f, 0.12f, 1f), // +5V Red
                floatArrayOf(0.12f, 0.12f, 0.14f, 1f), // GND Black
                floatArrayOf(0.12f, 0.45f, 0.95f, 1f), // -5V Blue
                floatArrayOf(0.95f, 0.82f, 0.12f, 1f)  // +12V Yellow
            )
            for (i in postColors.indices) {
                val px = postStartX + i * 28f
                // Metal collar
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, px, postY, 6f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 14f, 14f, 12f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                val c = postColors[i]
                GLES20.glUniform4f(uColorHandle, c[0], c[1], c[2], c[3])
                GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.vertexBuffer)
                GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.normalBuffer)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)
            }

            // 7. Logic Monitor LED Diode Bank (8 LEDs along top right panel)
            val ledStartX = cx - width * 0.05f
            val ledY = cy - height * 0.42f
            val circuitOutputs = components.filter { it.type == "LED" || it.type == "DIGITAL_PROBE" }
            for (i in 0 until 8) {
                val lx = ledStartX + i * 22f
                val isHigh = circuitOutputs.getOrNull(i)?.let {
                    (it.inputValues["IN"] ?: it.outputValues["OUT"] ?: DigitalState.LOW) == DigitalState.HIGH
                } ?: false

                // Metallic Bezel Collar
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, lx, ledY, 5f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 10f, 10f, 6f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glUniform4f(uColorHandle, 0.25f, 0.28f, 0.32f, 1f)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

                // Dome Lens with Photonic Glow
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, lx, ledY, 9f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 7f, 7f, 8f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

                GLES20.glUniform1f(uEmissionHandle, if (isHigh) 1.0f else 0.05f)
                if (isHigh) {
                    GLES20.glUniform4f(uColorHandle, 0.0f, 0.95f, 0.45f, 1.0f) // Bright glowing emerald
                } else {
                    GLES20.glUniform4f(uColorHandle, 0.08f, 0.22f, 0.12f, 1.0f) // Dark emerald
                }
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)
                GLES20.glUniform1f(uEmissionHandle, 0.0f)
            }

            // 8. Logic Input Metal Toggle Lever Switch Bank (8 Switches along bottom panel)
            val swStartX = cx - width * 0.38f
            val swY = cy + height * 0.42f
            val circuitSwitches = components.filter { it.type == "SWITCH" }
            for (i in 0 until 8) {
                val sx = swStartX + i * 26f
                val isSwOn = circuitSwitches.getOrNull(i)?.switchState ?: false

                // Bushing Collar
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, sx, swY, 5f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 11f, 11f, 6f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glUniform4f(uColorHandle, 0.75f, 0.78f, 0.82f, 1f) // Chrome nut
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

                // Metal Lever Arm
                val tiltAngle = if (isSwOn) 25f else -25f
                val leverLength = 16f * extrusionScale
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, sx, swY, 8f * extrusionScale)
                Matrix.rotateM(modelMatrix, 0, tiltAngle * extrusionScale, 0f, 1f, 0f)
                Matrix.translateM(modelMatrix, 0, 0f, 0f, leverLength / 2f)
                Matrix.scaleM(modelMatrix, 0, 3.8f, 3.8f, leverLength)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glUniform1f(uSpecularStrengthHandle, 0.95f)
                GLES20.glUniform4f(uColorHandle, 0.92f, 0.94f, 0.98f, 1f) // Solid chrome bat
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

                // Chrome Bat Tip
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, sx, swY, 8f * extrusionScale)
                Matrix.rotateM(modelMatrix, 0, tiltAngle * extrusionScale, 0f, 1f, 0f)
                Matrix.translateM(modelMatrix, 0, 0f, 0f, leverLength)
                Matrix.scaleM(modelMatrix, 0, 5.5f, 5.5f, 5.5f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
            }
        } else {
            // Traditional PCB Solder Mask
            GLES20.glUniform1f(uAmbientStrengthHandle, 0.5f)
            GLES20.glUniform1f(uSpecularStrengthHandle, 0.35f)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx, cy, -thickness / 2f)
            Matrix.scaleM(modelMatrix, 0, width, height, thickness)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glUniform4f(uColorHandle, 0.055f, 0.142f, 0.102f, 1.0f) // FR4 Dark Green
            GLES20.glEnableVertexAttribArray(aPositionHandle)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
            GLES20.glEnableVertexAttribArray(aNormalHandle)
            GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
        }

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderTechnicalGrid(cx: Float, cy: Float, width: Float, height: Float) {
        val halfW = width / 2f
        val halfH = height / 2f
        val minX = cx - halfW + 20f
        val maxX = cx + halfW - 20f
        val minY = cy - halfH + 20f
        val maxY = cy + halfH - 20f

        // Rebuild buffer if board dimensions changed
        if (gridBuffer == null ||
            lastBoardBounds[0] != minX || lastBoardBounds[1] != minY ||
            lastBoardBounds[2] != maxX || lastBoardBounds[3] != maxY
        ) {
            val step = 40f
            val coords = mutableListOf<Float>()
            var x = minX
            while (x <= maxX) {
                var y = minY
                while (y <= maxY) {
                    coords.add(x)
                    coords.add(y)
                    coords.add(0.2f) // slightly above board surface to prevent z-fighting
                    y += step
                }
                x += step
            }
            gridPointCount = coords.size / 3
            gridBuffer = GLGeometry.createFloatBuffer(coords.toFloatArray())
            lastBoardBounds[0] = minX
            lastBoardBounds[1] = minY
            lastBoardBounds[2] = maxX
            lastBoardBounds[3] = maxY
        }

        val buf = gridBuffer ?: return
        GLES20.glUseProgram(pointProgram)

        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(uPointMVPHandle, 1, false, mvpMatrix, 0)

        // Point size & color: in 2D technical mode dots are crisp and distinct
        val pSize = lerp(5.0f, 4.0f, transitionProgress)
        GLES20.glUniform1f(uPointSizeHandle, pSize)

        // Green-tinted grid dot color
        val alpha = lerp(0.85f, 0.55f, transitionProgress)
        GLES20.glUniform4f(uPointColorHandle, 0.22f, 0.44f, 0.35f, alpha)

        GLES20.glEnableVertexAttribArray(aPointPosHandle)
        GLES20.glVertexAttribPointer(aPointPosHandle, 3, GLES20.GL_FLOAT, false, 0, buf)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, gridPointCount)

        GLES20.glDisableVertexAttribArray(aPointPosHandle)
    }

    private fun renderComponent3D(
        comp: CircuitComponent, eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val def = ComponentRegistry.definitions[comp.type]
        val w = def?.width?.toFloat() ?: 100f
        val h = def?.height?.toFloat() ?: 70f
        val cx = comp.x + w / 2f
        val cy = comp.y + h / 2f

        val isSelected = comp.id == selectedComponentId
        if (isSelected) {
            renderSelectionHighlight3D(cx, cy, w + 16f, h + 16f, extrusionScale)
        }

        when (comp.type) {
            "SWITCH" -> renderSwitch3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
            "LED" -> renderLed3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
            "SEVEN_SEGMENT" -> renderSevenSegment3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
            "OSCILLATOR", "CLOCK" -> renderOscillator3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
            "BUZZER" -> renderBuzzer3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
            else -> renderChip3D(comp, cx, cy, w, h, eyeX, eyeY, eyeZ, extrusionScale)
        }
    }

    private fun renderBuzzer3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val buzzerH = 22f * extrusionScale
        val isHigh = comp.inputValues["IN"] == DigitalState.HIGH

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 150f, cy - 200f, 400f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, if (isHigh) 0.85f else 0.0f)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.45f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.5f)

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, buzzerH / 2f)
        Matrix.scaleM(modelMatrix, 0, w - 8f, h - 8f, buzzerH)

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        if (isHigh) {
            GLES20.glUniform4f(uColorHandle, 1.0f, 0.72f, 0.0f, 1.0f) // Glowing Amber when beeping
        } else {
            GLES20.glUniform4f(uColorHandle, 0.14f, 0.18f, 0.24f, 1.0f) // Dark Matte Transducer
        }

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderOscillator3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val oscHeight = 22f * extrusionScale

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 150f, cy - 200f, 400f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, 0.0f)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.45f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.65f)

        // 1. Crystal Oscillator Metal Can / DIP Body (Silver/Chrome Metal)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, oscHeight / 2f)
        Matrix.scaleM(modelMatrix, 0, w - 6f, h - 6f, oscHeight)

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)

        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glUniform4f(uColorHandle, 0.68f, 0.72f, 0.78f, 1.0f)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        // 2. Metallic pins
        if (extrusionScale > 0.2f) {
            GLES20.glUniform4f(uColorHandle, 0.85f, 0.88f, 0.92f, 1.0f)
            val pinCount = 3
            val pinStep = (h - 16f) / (pinCount - 1)
            for (i in 0 until pinCount) {
                val py = comp.y + 8f + i * pinStep

                // Left pin
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, comp.x - 2f, py, 4f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 8f, 4f, 8f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

                // Right pin
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, comp.x + w + 2f, py, 4f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 8f, 4f, 8f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
            }
        }

        // 3. Pulsing Clock Wave LED Indicator on Top
        val isTogglingHigh = comp.switchState
        GLES20.glUniform1f(uEmissionHandle, if (isTogglingHigh) 1.0f else 0.1f)
        if (isTogglingHigh) {
            GLES20.glUniform4f(uColorHandle, 1.0f, 0.72f, 0.0f, 1.0f) // Bright Amber/Gold clock pulse
        } else {
            GLES20.glUniform4f(uColorHandle, 0.35f, 0.25f, 0.05f, 1.0f)
        }

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, oscHeight + 2f * extrusionScale)
        Matrix.scaleM(modelMatrix, 0, 14f, 8f, 4f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderSelectionHighlight3D(cx: Float, cy: Float, w: Float, h: Float, extrusionScale: Float) {
        GLES20.glUseProgram(unlitProgram)
        GLES20.glUniform1f(uUnlitAlphaHandle, 0.85f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, 1.2f)
        Matrix.scaleM(modelMatrix, 0, w, h, 2.5f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uUnlitMVPHandle, 1, false, mvpMatrix, 0)

        // Neon Blue selection highlight
        GLES20.glUniform4f(uUnlitColorHandle, 0.22f, 0.65f, 1.0f, 0.75f)
        GLES20.glEnableVertexAttribArray(aUnlitPosHandle)
        GLES20.glVertexAttribPointer(aUnlitPosHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
        GLES20.glDisableVertexAttribArray(aUnlitPosHandle)
    }

    private fun renderChip3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val chipHeight = 18f * extrusionScale

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 150f, cy - 200f, 400f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, 0.0f)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.4f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.5f)

        // 1. Black Epoxy DIP Body #1E232A
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, chipHeight / 2f)
        Matrix.scaleM(modelMatrix, 0, w - 8f, h - 8f, chipHeight)

        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)

        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glUniform4f(uColorHandle, 0.10f, 0.12f, 0.15f, 1.0f)

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        // 2. Metallic Silver IC Leads / Pins on edges
        if (extrusionScale > 0.2f) {
            GLES20.glUniform4f(uColorHandle, 0.75f, 0.78f, 0.82f, 1.0f)
            val pinCount = 4
            val pinStep = (h - 16f) / (pinCount - 1)
            for (i in 0 until pinCount) {
                val py = comp.y + 8f + i * pinStep

                // Left pin
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, comp.x - 2f, py, 4f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 8f, 4f, 8f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

                // Right pin
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.translateM(modelMatrix, 0, comp.x + w + 2f, py, 4f * extrusionScale)
                Matrix.scaleM(modelMatrix, 0, 8f, 4f, 8f * extrusionScale)
                Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
                GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
                GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
                GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
                GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
            }
        }

        // 3. Status LED on IC top surface (Glows green if active)
        val hasHighOutput = comp.outputValues.values.any { it == DigitalState.HIGH }
        GLES20.glUniform1f(uEmissionHandle, if (hasHighOutput) 0.85f else 0.0f)
        if (hasHighOutput) {
            GLES20.glUniform4f(uColorHandle, 0.0f, 0.95f, 0.45f, 1.0f)
        } else {
            GLES20.glUniform4f(uColorHandle, 0.25f, 0.3f, 0.35f, 1.0f)
        }

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx + (w / 2f) - 16f, cy - (h / 2f) + 16f, chipHeight + 2f * extrusionScale)
        Matrix.scaleM(modelMatrix, 0, 5f, 5f, 4f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderSwitch3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val baseH = 12f * extrusionScale

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 100f, cy - 150f, 350f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, 0.0f)

        // 1. Switch Bezel / Sub-plate (Anodized Aluminum / Dark Bezel)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.45f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.5f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, baseH / 2f)
        Matrix.scaleM(modelMatrix, 0, w, h, baseH)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glUniform4f(uColorHandle, 0.16f, 0.20f, 0.26f, 1.0f)

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        // 2. Threaded Metallic Collar / Bushing with Hex Lock Nut
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.85f)
        GLES20.glUniform4f(uColorHandle, 0.82f, 0.85f, 0.90f, 1.0f) // Chrome silver bushing
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, baseH + 4f * extrusionScale)
        Matrix.scaleM(modelMatrix, 0, 16f, 16f, 8f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.vertexBuffer)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

        // 3. Physical Chrome Metal Toggle Lever / Bat Arm (Tilts +28° for ON, -28° for OFF)
        val tiltAngle = if (comp.switchState) 28f else -28f
        val leverLength = 26f * extrusionScale
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, baseH + 7f * extrusionScale)
        Matrix.rotateM(modelMatrix, 0, tiltAngle * extrusionScale, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, leverLength / 2f)
        Matrix.scaleM(modelMatrix, 0, 4.8f, 4.8f, leverLength)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        // High-shine chrome lever
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.95f)
        GLES20.glUniform4f(uColorHandle, 0.92f, 0.94f, 0.98f, 1.0f)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

        // 4. Spherical / Rounded Metallic Bat Handle Tip
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, baseH + 7f * extrusionScale)
        Matrix.rotateM(modelMatrix, 0, tiltAngle * extrusionScale, 0f, 1f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, leverLength)
        Matrix.scaleM(modelMatrix, 0, 7.5f, 7.5f, 7.5f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.vertexBuffer)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, domeMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, domeMesh.indexBuffer)

        // 5. Live Status Indicator LED (Glows Green if ON)
        GLES20.glUniform1f(uEmissionHandle, if (comp.switchState) 0.95f else 0.05f)
        if (comp.switchState) {
            GLES20.glUniform4f(uColorHandle, 0.0f, 0.98f, 0.45f, 1.0f) // Bright emerald
        } else {
            GLES20.glUniform4f(uColorHandle, 0.22f, 0.26f, 0.30f, 1.0f)
        }
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy + 18f, baseH + 3f * extrusionScale)
        Matrix.scaleM(modelMatrix, 0, 7f, 7f, 4f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.vertexBuffer)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, domeMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, domeMesh.indexBuffer)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderLed3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val state = comp.inputValues["IN"] ?: DigitalState.UNKNOWN
        val isHigh = state == DigitalState.HIGH
        val isFloat = state == DigitalState.UNKNOWN

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 100f, cy - 100f, 300f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)

        // 1. Chrome Panel Bezel Mount Ring
        val baseH = 6f * extrusionScale
        GLES20.glUniform1f(uEmissionHandle, 0.0f)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.5f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.85f)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, baseH / 2f)
        Matrix.scaleM(modelMatrix, 0, 26f, 26f, baseH)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glUniform4f(uColorHandle, 0.78f, 0.82f, 0.88f, 1.0f) // Chrome metallic ring
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

        // 2. Protruding LED Base Flange / Rim (Cathode rim)
        val flangeZ = baseH + 2f * extrusionScale
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, flangeZ)
        Matrix.scaleM(modelMatrix, 0, 21f, 21f, 3.5f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        val ledColor = when {
            isHigh -> floatArrayOf(0.0f, 0.98f, 0.45f, 0.95f) // Vibrant emerald
            isFloat -> floatArrayOf(1.0f, 0.75f, 0.05f, 0.85f)  // Amber
            else -> floatArrayOf(0.18f, 0.28f, 0.22f, 0.75f)    // Dark glass
        }
        GLES20.glUniform1f(uEmissionHandle, if (isHigh) 0.6f else 0.0f)
        GLES20.glUniform4f(uColorHandle, ledColor[0], ledColor[1], ledColor[2], ledColor[3])
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

        // 3. 5mm Cylindrical Epoxy Lens Body
        val bodyZ = flangeZ + 7f * extrusionScale
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, bodyZ)
        Matrix.scaleM(modelMatrix, 0, 18f, 18f, 12f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glUniform1f(uEmissionHandle, if (isHigh) 0.85f else 0.02f)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

        // 4. Smooth Hemispherical Dome Top
        val domeZ = bodyZ + 6f * extrusionScale
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, domeZ)
        Matrix.scaleM(modelMatrix, 0, 18f, 18f, 15f * extrusionScale)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
        GLES20.glUniform1f(uEmissionHandle, if (isHigh) 1.0f else (if (isFloat) 0.35f else 0.05f))
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.vertexBuffer)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, domeMesh.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, domeMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, domeMesh.indexBuffer)

        // 5. Two Metallic Wire Lead Pins underneath
        if (extrusionScale > 0.2f) {
            GLES20.glUniform1f(uEmissionHandle, 0.0f)
            GLES20.glUniform4f(uColorHandle, 0.88f, 0.90f, 0.94f, 1.0f) // Silver leads
            // Anode pin (+, longer)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx - 4f, cy, -6f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, 1.8f, 1.8f, 12f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.vertexBuffer)
            GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, cylinderMesh.normalBuffer)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)

            // Cathode pin (-, shorter)
            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx + 4f, cy, -4f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, 1.8f, 1.8f, 8f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, cylinderMesh.indexCount, GLES20.GL_UNSIGNED_SHORT, cylinderMesh.indexBuffer)
        }

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderSevenSegment3D(
        comp: CircuitComponent, cx: Float, cy: Float, w: Float, h: Float,
        eyeX: Float, eyeY: Float, eyeZ: Float, extrusionScale: Float
    ) {
        val dispH = 14f * extrusionScale

        GLES20.glUseProgram(phongProgram)
        GLES20.glUniform3f(uLightPosHandle, cx - 100f, cy - 100f, 350f)
        GLES20.glUniform3f(uViewPosHandle, eyeX, eyeY, eyeZ)
        GLES20.glUniform1f(uEmissionHandle, 0.0f)
        GLES20.glUniform1f(uAmbientStrengthHandle, 0.45f)
        GLES20.glUniform1f(uSpecularStrengthHandle, 0.3f)

        // 1. Module Bezel
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, cx, cy, dispH / 2f)
        Matrix.scaleM(modelMatrix, 0, w, h, dispH)
        Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
        GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
        GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)

        GLES20.glUniform4f(uColorHandle, 0.10f, 0.12f, 0.15f, 1.0f)
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.vertexBuffer)
        GLES20.glEnableVertexAttribArray(aNormalHandle)
        GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, unitBox.normalBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)

        // 2. Render 7 Glowing LED Segments (a,b,c,d,e,f,g)
        val segs = listOf("A", "B", "C", "D", "E", "F", "G")
        // Offsets relative to center: dx, dy, width, height
        val segLayout = listOf(
            floatArrayOf(0f, -22f, 22f, 4f),   // A top
            floatArrayOf(12f, -11f, 4f, 18f),  // B top-right
            floatArrayOf(12f, 11f, 4f, 18f),   // C bottom-right
            floatArrayOf(0f, 22f, 22f, 4f),    // D bottom
            floatArrayOf(-12f, 11f, 4f, 18f),  // E bottom-left
            floatArrayOf(-12f, -11f, 4f, 18f), // F top-left
            floatArrayOf(0f, 0f, 22f, 4f)      // G middle
        )

        for (i in segs.indices) {
            val segName = segs[i]
            val active = comp.inputValues[segName] == DigitalState.HIGH
            val layout = segLayout[i]

            GLES20.glUniform1f(uEmissionHandle, if (active) 1.0f else 0.05f)
            if (active) {
                GLES20.glUniform4f(uColorHandle, 1.0f, 0.12f, 0.22f, 1.0f) // Neon Crimson Glow
            } else {
                GLES20.glUniform4f(uColorHandle, 0.20f, 0.22f, 0.25f, 0.8f) // Inactive Segment
            }

            Matrix.setIdentityM(modelMatrix, 0)
            Matrix.translateM(modelMatrix, 0, cx + layout[0], cy + layout[1], dispH + 2f * extrusionScale)
            Matrix.scaleM(modelMatrix, 0, layout[2], layout[3], 3f * extrusionScale)
            Matrix.multiplyMM(tempMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mvpMatrix, 0)
            GLES20.glUniformMatrix4fv(uModelMatrixHandle, 1, false, modelMatrix, 0)
            GLGeometry.computeNormalMatrix(modelMatrix, normalMatrix)
            GLES20.glUniformMatrix3fv(uNormalMatrixHandle, 1, false, normalMatrix, 0)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, unitBox.indexCount, GLES20.GL_UNSIGNED_SHORT, unitBox.indexBuffer)
        }

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aNormalHandle)
    }

    private fun renderWires3D(
        components: List<CircuitComponent>,
        wires: List<CircuitWire>,
        wireArcScale: Float
    ) {
        if (wires.isEmpty()) return

        GLES20.glUseProgram(unlitProgram)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
        GLES20.glUniformMatrix4fv(uUnlitMVPHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(uUnlitAlphaHandle, 1.0f)

        GLES20.glEnableVertexAttribArray(aUnlitPosHandle)

        // Render each jumper wire as an elevated 3D parabolic curve
        val numSegments = 24
        val curveCoords = FloatArray((numSegments + 1) * 3)

        for (wire in wires) {
            val fromComp = components.find { it.id == wire.fromComponentId } ?: continue
            val toComp = components.find { it.id == wire.toComponentId } ?: continue

            val fromDef = ComponentRegistry.definitions[fromComp.type] ?: continue
            val toDef = ComponentRegistry.definitions[toComp.type] ?: continue

            val fromPort = (fromDef.inputs + fromDef.outputs).find { it.name == wire.fromPortId || it.id == wire.fromPortId } ?: continue
            val toPort = (toDef.inputs + toDef.outputs).find { it.name == wire.toPortId || it.id == wire.toPortId } ?: continue

            val startX = fromComp.x + fromPort.relativeX * fromDef.width
            val startY = fromComp.y + fromPort.relativeY * fromDef.height

            val endX = toComp.x + toPort.relativeX * toDef.width
            val endY = toComp.y + toPort.relativeY * toDef.height

            // Calculate wire distance and peak arc height
            val dx = endX - startX
            val dy = endY - startY
            val dist = sqrt(dx * dx + dy * dy)
            val peakArcZ = (30f + dist * 0.15f) * wireArcScale

            // Color based on live digital signal state
            when (wire.state) {
                DigitalState.HIGH -> GLES20.glUniform4f(uUnlitColorHandle, 0.0f, 0.95f, 0.45f, 0.95f) // Vibrant Green
                DigitalState.LOW -> GLES20.glUniform4f(uUnlitColorHandle, 0.45f, 0.50f, 0.58f, 0.85f)  // Slate Gray
                DigitalState.UNKNOWN -> GLES20.glUniform4f(uUnlitColorHandle, 1.0f, 0.72f, 0.0f, 0.9f) // Amber
            }

            // Generate curve points
            for (i in 0..numSegments) {
                val t = i.toFloat() / numSegments
                val px = startX + t * dx
                val py = startY + t * dy
                // Parabola: z = 4 * peak * t * (1 - t)
                val pz = 4f * peakArcZ * t * (1f - t) + 4f

                curveCoords[i * 3] = px
                curveCoords[i * 3 + 1] = py
                curveCoords[i * 3 + 2] = pz
            }

            val lineBuf = GLGeometry.createFloatBuffer(curveCoords)
            GLES20.glVertexAttribPointer(aUnlitPosHandle, 3, GLES20.GL_FLOAT, false, 0, lineBuf)
            GLES20.glLineWidth(5.0f)
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, numSegments + 1)

            // Render animated flowing electron pulse if wire is active (HIGH)
            if (wire.state == DigitalState.HIGH && isSimulating) {
                val rawPhase = animationTime * 1.5f
                val pulsePhase = rawPhase - rawPhase.toInt().toFloat()
                val pulseX = startX + pulsePhase * dx
                val pulseY = startY + pulsePhase * dy
                val pulseZ = 4f * peakArcZ * pulsePhase * (1.0f - pulsePhase) + 4f

                GLES20.glUseProgram(pointProgram)
                GLES20.glUniformMatrix4fv(uPointMVPHandle, 1, false, mvpMatrix, 0)
                GLES20.glUniform1f(uPointSizeHandle, 12f)
                GLES20.glUniform4f(uPointColorHandle, 0.85f, 1.0f, 0.85f, 1.0f)

                val ptBuf = GLGeometry.createFloatBuffer(floatArrayOf(pulseX, pulseY, pulseZ))
                GLES20.glEnableVertexAttribArray(aPointPosHandle)
                GLES20.glVertexAttribPointer(aPointPosHandle, 3, GLES20.GL_FLOAT, false, 0, ptBuf)
                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
                GLES20.glDisableVertexAttribArray(aPointPosHandle)

                // Restore unlit program for remaining wires
                GLES20.glUseProgram(unlitProgram)
                GLES20.glEnableVertexAttribArray(aUnlitPosHandle)
            }
        }

        GLES20.glDisableVertexAttribArray(aUnlitPosHandle)
    }

    /**
     * Hit tests a screen touch position against ANY component on the 3D circuit board.
     * Returns the clicked component or null.
     */
    fun hitTestComponent(screenX: Float, screenY: Float): CircuitComponent? {
        val currentComps = components
        if (currentComps.isEmpty()) return null

        val aspect = viewportWidth.toFloat() / max(1f, viewportHeight.toFloat())
        val fovY = lerp(20f, 45f, transitionProgress)
        val pMatrix = FloatArray(16)
        Matrix.perspectiveM(pMatrix, 0, fovY, aspect, 10f, 5000f)

        val effectivePitch = lerp(89.5f, cameraPitch, transitionProgress)
        val effectiveYaw = lerp(0f, cameraYaw, transitionProgress)
        val pitchRad = (effectivePitch * PI / 180.0).toFloat()
        val yawRad = (effectiveYaw * PI / 180.0).toFloat()

        val minX: Float
        val maxX: Float
        val minY: Float
        val maxY: Float
        if (currentComps.isNotEmpty()) {
            minX = currentComps.minOf { it.x } - 100f
            maxX = currentComps.maxOf { it.x + 140f } + 100f
            minY = currentComps.minOf { it.y } - 100f
            maxY = currentComps.maxOf { it.y + 120f } + 100f
        } else {
            minX = 0f
            maxX = 800f
            minY = 0f
            maxY = 600f
        }
        val boardCenterX = (minX + maxX) / 2f + panTargetX
        val boardCenterY = (minY + maxY) / 2f + panTargetY

        val eyeDist = cameraDistance * lerp(1.6f, 1.0f, transitionProgress)
        val eyeX = boardCenterX + eyeDist * cos(pitchRad) * sin(yawRad)
        val eyeY = boardCenterY + eyeDist * cos(pitchRad) * cos(yawRad)
        val eyeZ = eyeDist * sin(pitchRad)

        val vMatrix = FloatArray(16)
        val upY = if (effectivePitch > 89f) 1f else 0f
        val upZ = if (effectivePitch > 89f) 0f else 1f
        Matrix.setLookAtM(vMatrix, 0, eyeX, eyeY, eyeZ, boardCenterX, boardCenterY, 0f, 0f, upY, upZ)

        val fullMatrix = FloatArray(16)
        Matrix.multiplyMM(fullMatrix, 0, pMatrix, 0, vMatrix, 0)

        val inVec = FloatArray(4)
        val outVec = FloatArray(4)

        var closestComp: CircuitComponent? = null
        var minDistance = Float.MAX_VALUE

        for (comp in currentComps) {
            val def = ComponentRegistry.get(comp.type)
            val w = def?.width?.toFloat() ?: 100f
            val h = def?.height?.toFloat() ?: 70f
            val cx = comp.x + w / 2f
            val cy = comp.y + h / 2f
            val cz = 10f

            inVec[0] = cx
            inVec[1] = cy
            inVec[2] = cz
            inVec[3] = 1.0f

            Matrix.multiplyMV(outVec, 0, fullMatrix, 0, inVec, 0)
            if (outVec[3] > 0f) {
                val ndcX = outVec[0] / outVec[3]
                val ndcY = outVec[1] / outVec[3]

                val sx = (ndcX + 1.0f) * 0.5f * viewportWidth
                val sy = (1.0f - ndcY) * 0.5f * viewportHeight

                val dist = sqrt((sx - screenX) * (sx - screenX) + (sy - screenY) * (sy - screenY))
                // Projected size based on camera distance and viewport for comfortable mobile finger hit testing
                val projW = (w / eyeDist) * viewportHeight * 0.7f
                val touchRadius = max(90f, projW)
                if (dist < touchRadius && dist < minDistance) {
                    minDistance = dist
                    closestComp = comp
                }
            }
        }
        return closestComp
    }

    fun hitTestSwitch(screenX: Float, screenY: Float): String? {
        val hit = hitTestComponent(screenX, screenY)
        return if (hit?.type == "SWITCH") hit.id else null
    }

    private fun lerp(start: Float, stop: Float, amount: Float): Float {
        return start + (stop - start) * amount
    }
}
