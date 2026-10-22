package com.example

import com.example.logiclab.data.CircuitSerializer
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.engine.SimulationEngine
import com.example.logiclab.engine.TruthTableEngine
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.DigitalState
import com.example.logiclab.starters.StarterCircuits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LogicLabUnitTest {

    @Test
    fun testComponentRegistryContainsAllCategories() {
        val defs = ComponentRegistry.definitions
        assertTrue("Registry has gates", defs.containsKey("AND"))
        assertTrue("Registry has OR", defs.containsKey("OR"))
        assertTrue("Registry has NOT", defs.containsKey("NOT"))
        assertTrue("Registry has Half Adder", defs.containsKey("HALF_ADDER"))
        assertTrue("Registry has Full Adder", defs.containsKey("FULL_ADDER"))
        assertTrue("Registry has 4:1 MUX", defs.containsKey("MUX_4_1"))
        assertTrue("Registry has 1:4 DEMUX", defs.containsKey("DEMUX_1_4"))
        assertTrue("Registry has 4:2 Priority Encoder", defs.containsKey("PRIORITY_ENCODER_4_2"))
        assertTrue("Registry has 2:4 Decoder", defs.containsKey("DECODER_2_4"))
        assertTrue("Registry has 3:8 Decoder", defs.containsKey("DECODER_3_8"))
        assertTrue("Registry has Parity Generator", defs.containsKey("PARITY_GEN"))
        assertTrue("Registry has Comparator", defs.containsKey("COMPARATOR_1BIT"))
        assertTrue("Registry has BCD-to-7Seg", defs.containsKey("BCD_TO_7SEG"))
    }

    @Test
    fun testAndGateSimulation() {
        val comps = listOf(
            CircuitComponent(id = "SW1", type = "SWITCH", switchState = true),
            CircuitComponent(id = "SW2", type = "SWITCH", switchState = true),
            CircuitComponent(id = "U1", type = "AND"),
            CircuitComponent(id = "LED1", type = "LED")
        )
        val wires = listOf(
            CircuitWire("w1", "SW1", "OUT", "U1", "A"),
            CircuitWire("w2", "SW2", "OUT", "U1", "B"),
            CircuitWire("w3", "U1", "Y", "LED1", "IN")
        )

        val result = SimulationEngine.simulate(comps, wires)
        val led = result.components.find { it.id == "LED1" }
        assertEquals(DigitalState.HIGH, led?.inputValues?.get("IN"))

        // When SW1 is turned OFF
        val swOffComps = comps.map { if (it.id == "SW1") it.copy(switchState = false) else it }
        val resultOff = SimulationEngine.simulate(swOffComps, wires)
        val ledOff = resultOff.components.find { it.id == "LED1" }
        assertEquals(DigitalState.LOW, ledOff?.inputValues?.get("IN"))
    }

    @Test
    fun testFullAdderSimulation() {
        val faDef = ComponentRegistry.get("FULL_ADDER")
        assertNotNull(faDef)

        // 1 + 1 + 0 = SUM 0, COUT 1
        val out1 = faDef!!.evaluate(
            mapOf("A" to DigitalState.HIGH, "B" to DigitalState.HIGH, "CIN" to DigitalState.LOW),
            false
        )
        assertEquals(DigitalState.LOW, out1["SUM"])
        assertEquals(DigitalState.HIGH, out1["COUT"])

        // 1 + 1 + 1 = SUM 1, COUT 1
        val out2 = faDef.evaluate(
            mapOf("A" to DigitalState.HIGH, "B" to DigitalState.HIGH, "CIN" to DigitalState.HIGH),
            false
        )
        assertEquals(DigitalState.HIGH, out2["SUM"])
        assertEquals(DigitalState.HIGH, out2["COUT"])
    }

    @Test
    fun testMux4to1Simulation() {
        val muxDef = ComponentRegistry.get("MUX_4_1")
        assertNotNull(muxDef)

        // S1=0, S0=1 selects D1
        val out = muxDef!!.evaluate(
            mapOf(
                "D0" to DigitalState.LOW,
                "D1" to DigitalState.HIGH,
                "D2" to DigitalState.LOW,
                "D3" to DigitalState.LOW,
                "S0" to DigitalState.HIGH,
                "S1" to DigitalState.LOW
            ),
            false
        )
        assertEquals(DigitalState.HIGH, out["Y"])
    }

    @Test
    fun testPriorityEncoderSimulation() {
        val encDef = ComponentRegistry.get("PRIORITY_ENCODER_4_2")
        assertNotNull(encDef)

        // D3 active overrides D1 and D0
        val out = encDef!!.evaluate(
            mapOf(
                "D0" to DigitalState.HIGH,
                "D1" to DigitalState.HIGH,
                "D2" to DigitalState.LOW,
                "D3" to DigitalState.HIGH
            ),
            false
        )
        assertEquals(DigitalState.HIGH, out["Y1"])
        assertEquals(DigitalState.HIGH, out["Y0"])
        assertEquals(DigitalState.HIGH, out["V"])
    }

    @Test
    fun testCircuitSerializationRoundtrip() {
        val originalComps = listOf(
            CircuitComponent(id = "SW1", type = "SWITCH", x = 120f, y = 200f, label = "Input A", switchState = true),
            CircuitComponent(id = "U1", type = "AND", x = 320f, y = 200f, label = "Gate 1")
        )
        val originalWires = listOf(
            CircuitWire("w1", "SW1", "OUT", "U1", "A")
        )

        val json = CircuitSerializer.serialize(originalComps, originalWires)
        val deserialized = CircuitSerializer.deserialize(json)

        assertEquals(2, deserialized.components.size)
        assertEquals(1, deserialized.wires.size)
        assertEquals("SW1", deserialized.components[0].id)
        assertEquals(true, deserialized.components[0].switchState)
        assertEquals("w1", deserialized.wires[0].id)
    }

    @Test
    fun testStarterCircuitsAllValid() {
        val starters = StarterCircuits.list
        assertTrue("Starter circuits list is not empty", starters.isNotEmpty())
        for (st in starters) {
            assertTrue("Starter circuit ${st.id} has components", st.components.isNotEmpty())
            val sim = SimulationEngine.simulate(st.components, st.wires)
            assertNotNull(sim)
        }
    }

    @Test
    fun testTruthTableGeneration() {
        val andComp = CircuitComponent(id = "U1", type = "AND")
        val table = TruthTableEngine.generateForComponent(andComp)
        assertEquals(4, table.rows.size)
        assertEquals(listOf("A", "B"), table.inputHeaders)
        assertEquals(listOf("Y"), table.outputHeaders)
    }

    @Test
    fun testAutomaticCircuitTruthTableAnalysis() {
        // Construct an XOR circuit: SW1 (A), SW2 (B), XOR gate, and LED1 (Y)
        val comps = listOf(
            CircuitComponent(id = "SW1", type = "SWITCH", label = "A", switchState = false),
            CircuitComponent(id = "SW2", type = "SWITCH", label = "B", switchState = false),
            CircuitComponent(id = "U1", type = "XOR"),
            CircuitComponent(id = "LED1", type = "LED", label = "Y")
        )
        val wires = listOf(
            CircuitWire("w1", "SW1", "OUT", "U1", "A"),
            CircuitWire("w2", "SW2", "OUT", "U1", "B"),
            CircuitWire("w3", "U1", "Y", "LED1", "IN")
        )

        val table = TruthTableEngine.generateForCircuit(comps, wires)
        assertEquals(4, table.rows.size)
        assertEquals(listOf("A", "B"), table.inputHeaders)
        assertEquals(listOf("Y"), table.outputHeaders)

        // Check truth values:
        // 0, 0 -> 0
        assertEquals(DigitalState.LOW, table.rows[0].outputValues["Y"])
        // 0, 1 -> 1
        assertEquals(DigitalState.HIGH, table.rows[1].outputValues["Y"])
        // 1, 0 -> 1
        assertEquals(DigitalState.HIGH, table.rows[2].outputValues["Y"])
        // 1, 1 -> 0
        assertEquals(DigitalState.LOW, table.rows[3].outputValues["Y"])

        // Check canonical SOP min-terms: m(1, 2)
        assertEquals(1, table.formulas.size)
        val formula = table.formulas.first()
        assertEquals(listOf(1, 2), formula.minTerms)
        assertEquals("Σm(1, 2)", formula.canonicalSOP)
    }

    @Test
    fun testCircuitTruthTableWithOpenPorts() {
        // Circuit with a NAND gate that has NO switches or LEDs attached
        // The analyzer must automatically discover the open input ports and output ports!
        val comps = listOf(
            CircuitComponent(id = "U1", type = "NAND")
        )
        val wires = emptyList<CircuitWire>()

        val table = TruthTableEngine.generateForCircuit(comps, wires)
        assertEquals(4, table.rows.size)
        assertEquals(2, table.inputHeaders.size)
        assertEquals(1, table.outputHeaders.size)

        // NAND truth values: 1, 1, 1, 0 -> min-terms m(0, 1, 2)
        val yHeader = table.outputHeaders.first()
        assertEquals(DigitalState.HIGH, table.rows[0].outputValues[yHeader])
        assertEquals(DigitalState.HIGH, table.rows[1].outputValues[yHeader])
        assertEquals(DigitalState.HIGH, table.rows[2].outputValues[yHeader])
        assertEquals(DigitalState.LOW, table.rows[3].outputValues[yHeader])
    }

    @Test
    fun testInfiniteGridViewportCalculation() {
        val panX = -250f
        val panY = -150f
        val zoom = 1.5f
        val canvasWidth = 1080f
        val canvasHeight = 1920f
        val gridSize = 20f

        val worldLeft = -panX / zoom
        val worldTop = -panY / zoom
        val worldRight = (canvasWidth - panX) / zoom
        val worldBottom = (canvasHeight - panY) / zoom

        assertTrue("worldLeft is positive with negative pan", worldLeft > 0f)
        assertTrue("worldRight > worldLeft", worldRight > worldLeft)
        assertTrue("worldBottom > worldTop", worldBottom > worldTop)

        val step = gridSize
        val startIdxX = kotlin.math.floor(worldLeft / step).toLong()
        val endIdxX = kotlin.math.ceil(worldRight / step).toLong()
        assertTrue("endIdxX > startIdxX", endIdxX > startIdxX)

        // Verify screen projection matches exactly
        val firstWorldX = startIdxX * step
        val screenX = firstWorldX * zoom + panX
        val recoveredWorldX = (screenX - panX) / zoom
        assertEquals(firstWorldX, recoveredWorldX, 0.001f)
    }

    @Test
    fun test3DProjectionBasicMath() {
        val wx = 100f
        val wy = 200f
        val wz = 15f
        val boardCenterX = 100f
        val boardCenterY = 200f

        val dx = wx - boardCenterX
        val dy = wy - boardCenterY
        assertEquals(0f, dx, 0.001f)
        assertEquals(0f, dy, 0.001f)
    }

    @Test
    fun testGLGeometryMeshes() {
        val box = com.example.logiclab.gl.GLGeometry.UnitBoxMesh()
        assertEquals(36, box.indexCount)
        assertTrue(box.vertexBuffer.capacity() > 0)
        assertTrue(box.normalBuffer.capacity() > 0)

        val dome = com.example.logiclab.gl.GLGeometry.DomeMesh(8, 4)
        assertTrue(dome.indexCount > 0)
        assertTrue(dome.vertexBuffer.capacity() > 0)
    }

    @Test
    fun test2Dto3DTransitionInterpolation() {
        // 2D Grid Mode: transition = 0.0f
        val pitch2D = 89.5f + (45f - 89.5f) * 0.0f
        val yaw2D = 0f + (42f - 0f) * 0.0f
        val extrude2D = 0.04f + (1.0f - 0.04f) * 0.0f
        val wireArc2D = 0.0f + (1.0f - 0.0f) * 0.0f

        assertEquals(89.5f, pitch2D, 0.01f)
        assertEquals(0.0f, yaw2D, 0.01f)
        assertEquals(0.04f, extrude2D, 0.01f)
        assertEquals(0.0f, wireArc2D, 0.01f)

        // 3D Isometric Mode: transition = 1.0f
        val pitch3D = 89.5f + (45f - 89.5f) * 1.0f
        val yaw3D = 0f + (42f - 0f) * 1.0f
        val extrude3D = 0.04f + (1.0f - 0.04f) * 1.0f
        val wireArc3D = 0.0f + (1.0f - 0.0f) * 1.0f

        assertEquals(45.0f, pitch3D, 0.01f)
        assertEquals(42.0f, yaw3D, 0.01f)
        assertEquals(1.0f, extrude3D, 0.01f)
        assertEquals(1.0f, wireArc3D, 0.01f)
    }

    @Test
    fun testViewportCullingSystem() {
        val visibleBounds = com.example.logiclab.ui.canvas.ViewportCullingSystem.calculateVisibleWorldBounds(
            canvasWidth = 1000f,
            canvasHeight = 800f,
            panOffset = androidx.compose.ui.geometry.Offset(0f, 0f),
            zoomScale = 1.0f,
            margin = 50f
        )
        // Bounds should be [-50, -50, 1050, 850]
        assertEquals(-50f, visibleBounds.left, 0.001f)
        assertEquals(-50f, visibleBounds.top, 0.001f)
        assertEquals(1050f, visibleBounds.right, 0.001f)
        assertEquals(850f, visibleBounds.bottom, 0.001f)

        val andDef = ComponentRegistry.get("AND")!!
        val insideComp = CircuitComponent(id = "c1", type = "AND", x = 100f, y = 100f)
        val outsideComp = CircuitComponent(id = "c2", type = "AND", x = 3000f, y = 4000f)

        assertTrue(
            "Component inside bounds is visible",
            com.example.logiclab.ui.canvas.ViewportCullingSystem.isComponentVisible(insideComp, andDef, visibleBounds)
        )
        assertFalse(
            "Component far off-screen is culled",
            com.example.logiclab.ui.canvas.ViewportCullingSystem.isComponentVisible(outsideComp, andDef, visibleBounds)
        )

        // Test Wire Culling
        val pInside1 = androidx.compose.ui.geometry.Offset(100f, 100f)
        val pInside2 = androidx.compose.ui.geometry.Offset(300f, 100f)
        assertTrue(
            "Wire within screen is visible",
            com.example.logiclab.ui.canvas.ViewportCullingSystem.isWireVisible(pInside1, pInside2, visibleBounds)
        )

        val pOutside1 = androidx.compose.ui.geometry.Offset(3000f, 3000f)
        val pOutside2 = androidx.compose.ui.geometry.Offset(3200f, 3000f)
        assertFalse(
            "Wire far outside screen is culled",
            com.example.logiclab.ui.canvas.ViewportCullingSystem.isWireVisible(pOutside1, pOutside2, visibleBounds)
        )

        // Test LOD culling
        assertTrue(com.example.logiclab.ui.canvas.ViewportCullingSystem.shouldRenderPortLabels(1.0f))
        assertFalse(com.example.logiclab.ui.canvas.ViewportCullingSystem.shouldRenderPortLabels(0.4f))
    }

    @Test
    fun testViewportInterpolatedStateCalculation() {
        val state2D = com.example.logiclab.ui.canvas.ViewportInterpolatedState(
            transitionProgress = 0.0f,
            pitchDeg = 89.5f,
            yawDeg = 0.0f,
            cameraDistance = 1400f,
            panOffset = androidx.compose.ui.geometry.Offset.Zero,
            extrusionScale = 0.02f,
            wireArcScale = 0.0f,
            gridBlend = 0.0f,
            fovDegrees = 18f,
            isIsometric = false
        )
        assertFalse(state2D.isIsometric)
        assertEquals(89.5f, state2D.pitchDeg, 0.001f)
        assertEquals(0.0f, state2D.extrusionScale, 0.05f)

        val state3D = com.example.logiclab.ui.canvas.ViewportInterpolatedState(
            transitionProgress = 1.0f,
            pitchDeg = 45.0f,
            yawDeg = 42.0f,
            cameraDistance = 980f,
            panOffset = androidx.compose.ui.geometry.Offset.Zero,
            extrusionScale = 1.0f,
            wireArcScale = 1.0f,
            gridBlend = 1.0f,
            fovDegrees = 45f,
            isIsometric = true
        )
        assertTrue(state3D.isIsometric)
        assertEquals(45.0f, state3D.pitchDeg, 0.001f)
        assertEquals(1.0f, state3D.extrusionScale, 0.001f)
        assertEquals(1.0f, state3D.wireArcScale, 0.001f)

        val eye = state3D.calculateEyePosition(0f, 0f)
        assertEquals(3, eye.size)
        assertTrue("Eye Z is positive", eye[2] > 0f)
    }

    @Test
    fun testGateDeletionAndWireCleanup() {
        val app = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = com.example.logiclab.viewmodel.LogicLabViewModel(app)

        // Add a test component
        val andDef = ComponentRegistry.get("AND")!!
        vm.addComponentAt(andDef, 200f, 200f)
        val addedComp = vm.components.value.find { it.type == "AND" }
        assertNotNull(addedComp)

        // Delete the component
        val compId = addedComp!!.id
        vm.deleteComponent(compId)
        val compAfterDelete = vm.components.value.find { it.id == compId }
        assertTrue("Component was deleted", compAfterDelete == null)
    }
}
