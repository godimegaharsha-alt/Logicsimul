package com.example.logiclab.viewmodel

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.logiclab.data.AppDatabase
import com.example.logiclab.data.CircuitData
import com.example.logiclab.data.CircuitEntity
import com.example.logiclab.data.CircuitRepository
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.engine.SimulationEngine
import com.example.logiclab.engine.SimulationResult
import com.example.logiclab.engine.TruthTableData
import com.example.logiclab.engine.TruthTableEngine
import com.example.logiclab.engine.TruthTableMode
import com.example.logiclab.engine.TruthTableRow
import com.example.logiclab.model.Board3DStyle
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitDiagnostic
import com.example.logiclab.model.CircuitWire
import com.example.logiclab.model.ComponentDefinition
import com.example.logiclab.model.DigitalState
import com.example.logiclab.starters.StarterCircuit
import com.example.logiclab.starters.StarterCircuits
import com.example.logiclab.ui.components.AppMode
import com.example.logiclab.ui.components.SignalChannel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.ui.theme.AppTheme
import java.util.UUID

sealed class ActiveDialog {
    data object None : ActiveDialog()
    data class GateDetails(val componentId: String) : ActiveDialog()
    data object AiAssistant : ActiveDialog()
    data object SaveLoad : ActiveDialog()
    data object ComponentLibrary : ActiveDialog()
    data object KMap : ActiveDialog()
    data object ICEncyclopedia : ActiveDialog()
    data object ThemeSelector : ActiveDialog()
}

enum class BottomPanelType {
    NONE,
    TRUTH_TABLE,
    DIAGNOSTICS,
    TIMING_DIAGRAM,
    OSCILLOSCOPE
}

data class CircuitSnapshot(
    val components: List<CircuitComponent>,
    val wires: List<CircuitWire>
)

class LogicLabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CircuitRepository by lazy {
        val db = AppDatabase.getDatabase(application)
        CircuitRepository(db.circuitDao())
    }

    val savedCircuits: StateFlow<List<CircuitEntity>> = repository.allCircuits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Circuit State
    private val _components = MutableStateFlow<List<CircuitComponent>>(emptyList())
    val components: StateFlow<List<CircuitComponent>> = _components.asStateFlow()

    private val _wires = MutableStateFlow<List<CircuitWire>>(emptyList())
    val wires: StateFlow<List<CircuitWire>> = _wires.asStateFlow()

    private val _diagnostics = MutableStateFlow<List<CircuitDiagnostic>>(emptyList())
    val diagnostics: StateFlow<List<CircuitDiagnostic>> = _diagnostics.asStateFlow()

    // Mode & UI States
    private val _currentMode = MutableStateFlow(AppMode.SIMULATOR)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    private val _bottomPanel = MutableStateFlow(BottomPanelType.NONE)
    val bottomPanel: StateFlow<BottomPanelType> = _bottomPanel.asStateFlow()

    private val _selectedComponentId = MutableStateFlow<String?>(null)
    val selectedComponentId: StateFlow<String?> = _selectedComponentId.asStateFlow()

    private val _selectedWireId = MutableStateFlow<String?>(null)
    val selectedWireId: StateFlow<String?> = _selectedWireId.asStateFlow()

    // Unified Single-Active Dialog State (Guarantees zero overlapping dialog boxes)
    private val _activeDialog = MutableStateFlow<ActiveDialog>(ActiveDialog.None)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    fun openGateDetails(componentId: String) {
        _activeDialog.value = ActiveDialog.GateDetails(componentId)
    }

    fun openAiAssistant() {
        _activeDialog.value = ActiveDialog.AiAssistant
    }

    fun openSaveLoad() {
        _activeDialog.value = ActiveDialog.SaveLoad
    }

    fun openComponentLibrary() {
        _activeDialog.value = ActiveDialog.ComponentLibrary
    }

    fun openKMap() {
        _activeDialog.value = ActiveDialog.KMap
    }

    fun openICEncyclopedia() {
        _activeDialog.value = ActiveDialog.ICEncyclopedia
    }

    fun closeDialog() {
        _activeDialog.value = ActiveDialog.None
    }

    val isLibraryOpen: StateFlow<Boolean> = _activeDialog.map { it is ActiveDialog.ComponentLibrary }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isAiAssistantOpen: StateFlow<Boolean> = _activeDialog.map { it is ActiveDialog.AiAssistant }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isSaveLoadOpen: StateFlow<Boolean> = _activeDialog.map { it is ActiveDialog.SaveLoad }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isKMapOpen: StateFlow<Boolean> = _activeDialog.map { it is ActiveDialog.KMap }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isICEncyclopediaOpen: StateFlow<Boolean> = _activeDialog.map { it is ActiveDialog.ICEncyclopedia }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setLibraryOpen(open: Boolean) {
        _activeDialog.value = if (open) ActiveDialog.ComponentLibrary else ActiveDialog.None
    }

    fun setAiAssistantOpen(open: Boolean) {
        _activeDialog.value = if (open) ActiveDialog.AiAssistant else ActiveDialog.None
    }

    fun setSaveLoadOpen(open: Boolean) {
        _activeDialog.value = if (open) ActiveDialog.SaveLoad else ActiveDialog.None
    }

    fun setKMapOpen(open: Boolean) {
        _activeDialog.value = if (open) ActiveDialog.KMap else ActiveDialog.None
    }

    fun setICEncyclopediaOpen(open: Boolean) {
        _activeDialog.value = if (open) ActiveDialog.ICEncyclopedia else ActiveDialog.None
    }

    private val _currentTheme = MutableStateFlow(AppTheme.CYBER_OBSIDIAN)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    fun openThemeSelector() {
        _activeDialog.value = ActiveDialog.ThemeSelector
    }

    private val _isOscilloscopeOpen = MutableStateFlow(false)
    val isOscilloscopeOpen: StateFlow<Boolean> = _isOscilloscopeOpen.asStateFlow()

    private val _isAudioMuted = MutableStateFlow(false)
    val isAudioMuted: StateFlow<Boolean> = _isAudioMuted.asStateFlow()

    fun toggleAudioMute() {
        val next = !_isAudioMuted.value
        _isAudioMuted.value = next
        com.example.logiclab.audio.AudioEngine.isMuted = next
        if (next) com.example.logiclab.audio.AudioEngine.stopContinuousTone()
    }

    fun setOscilloscopeOpen(open: Boolean) {
        _isOscilloscopeOpen.value = open
        if (open) {
            _bottomPanel.value = BottomPanelType.OSCILLOSCOPE
        }
    }

    // Probed Wires & Wire Waveforms for Live Oscilloscope
    private val _probedWireIds = MutableStateFlow<Set<String>>(emptySet())
    val probedWireIds: StateFlow<Set<String>> = _probedWireIds.asStateFlow()

    private val _wireWaveforms = MutableStateFlow<Map<String, List<DigitalState>>>(emptyMap())
    val wireWaveforms: StateFlow<Map<String, List<DigitalState>>> = _wireWaveforms.asStateFlow()

    fun toggleWireProbe(wireId: String) {
        val current = _probedWireIds.value.toMutableSet()
        if (current.contains(wireId)) {
            current.remove(wireId)
        } else {
            if (current.size >= 4) {
                current.remove(current.first())
            }
            current.add(wireId)
        }
        _probedWireIds.value = current
    }

    fun addWireProbe(wireId: String) {
        val current = _probedWireIds.value.toMutableSet()
        if (!current.contains(wireId)) {
            if (current.size >= 4) {
                current.remove(current.first())
            }
            current.add(wireId)
            _probedWireIds.value = current
        }
    }

    fun removeWireProbe(wireId: String) {
        val current = _probedWireIds.value.toMutableSet()
        if (current.remove(wireId)) {
            _probedWireIds.value = current
        }
    }

    fun clearWireProbes() {
        _probedWireIds.value = emptySet()
    }

    fun openOscilloscopeForWire(wireId: String) {
        _selectedWireId.value = wireId
        _selectedComponentId.value = null
        addWireProbe(wireId)
        _bottomPanel.value = BottomPanelType.OSCILLOSCOPE
    }

    // Simulation Engine Controls
    private val _isSimulating = MutableStateFlow(true)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // Workspace Canvas
    var panOffset = MutableStateFlow(Offset(100f, 100f))
    var zoomScale = MutableStateFlow(1.0f)
    var snapToGrid = MutableStateFlow(true)
    private val _is3DMode = MutableStateFlow(false)
    val is3DMode: StateFlow<Boolean> = _is3DMode.asStateFlow()

    private val _board3DStyle = MutableStateFlow(Board3DStyle.PCB_LAB)
    val board3DStyle: StateFlow<Board3DStyle> = _board3DStyle.asStateFlow()

    fun toggle3DMode() {
        _is3DMode.value = !_is3DMode.value
    }

    fun set3DMode(enabled: Boolean) {
        _is3DMode.value = enabled
    }

    fun setBoard3DStyle(style: Board3DStyle) {
        _board3DStyle.value = style
    }

    fun toggleBoard3DStyle() {
        _board3DStyle.value = if (_board3DStyle.value == Board3DStyle.PCB_LAB) {
            Board3DStyle.IC_TRAINER_KIT
        } else {
            Board3DStyle.PCB_LAB
        }
    }

    // Wire Drawing Mode (Tap two gate pins to create visible wire connection)
    private val _isWireDrawingMode = MutableStateFlow(false)
    val isWireDrawingMode: StateFlow<Boolean> = _isWireDrawingMode.asStateFlow()

    fun toggleWireDrawingMode() {
        _isWireDrawingMode.value = !_isWireDrawingMode.value
    }

    fun setWireDrawingMode(enabled: Boolean) {
        _isWireDrawingMode.value = enabled
    }

    // Signal Flow Mode (Visual flow mode highlighting wires in Green for High/1 and Red for Low/0)
    private val _isSignalFlowMode = MutableStateFlow(true)
    val isSignalFlowMode: StateFlow<Boolean> = _isSignalFlowMode.asStateFlow()

    fun toggleSignalFlowMode() {
        _isSignalFlowMode.value = !_isSignalFlowMode.value
    }

    fun setSignalFlowMode(enabled: Boolean) {
        _isSignalFlowMode.value = enabled
    }

    // Truth Table
    private val _truthTableMode = MutableStateFlow(TruthTableMode.CIRCUIT)
    val truthTableMode: StateFlow<TruthTableMode> = _truthTableMode.asStateFlow()

    private val _truthTableData = MutableStateFlow(TruthTableData("Truth Table", emptyList(), emptyList(), emptyList()))
    val truthTableData: StateFlow<TruthTableData> = _truthTableData.asStateFlow()

    // Timing Diagram History
    private val _signalChannels = MutableStateFlow<List<SignalChannel>>(emptyList())
    val signalChannels: StateFlow<List<SignalChannel>> = _signalChannels.asStateFlow()
    private val _timeStepCount = MutableStateFlow(0)
    val timeStepCount: StateFlow<Int> = _timeStepCount.asStateFlow()

    // Undo / Redo
    private val undoStack = mutableListOf<CircuitSnapshot>()
    private val redoStack = mutableListOf<CircuitSnapshot>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Load default starter circuit (AND Gate Verification)
        val defaultStarter = StarterCircuits.list.first()
        loadStarterCircuit(defaultStarter)
        startSimulationLoop()
    }

    private fun pushUndoSnapshot() {
        undoStack.add(CircuitSnapshot(_components.value, _wires.value))
        if (undoStack.size > 30) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(CircuitSnapshot(_components.value, _wires.value))
            val prev = undoStack.removeAt(undoStack.lastIndex)
            _components.value = prev.components
            _wires.value = prev.wires
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            runSimulationStep()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(CircuitSnapshot(_components.value, _wires.value))
            val next = redoStack.removeAt(redoStack.lastIndex)
            _components.value = next.components
            _wires.value = next.wires
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = redoStack.isNotEmpty()
            runSimulationStep()
        }
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                if (_isSimulating.value) {
                    advanceClocksAndSimulate()
                }
                delay(300)
            }
        }
    }

    fun stepClock() {
        advanceClocksAndSimulate()
    }

    fun advanceClocksAndSimulate() {
        // Toggle any CLOCK and OSCILLATOR component states
        val updatedComps = _components.value.map { comp ->
            if (comp.type == "CLOCK" || comp.type == "OSCILLATOR") {
                comp.copy(switchState = !comp.switchState)
            } else {
                comp
            }
        }
        _components.value = updatedComps
        runSimulationStep()
        recordTimingSnapshot()
    }

    fun runSimulationStep() {
        val result: SimulationResult = SimulationEngine.simulate(_components.value, _wires.value)
        _components.value = result.components
        _wires.value = result.wires
        _diagnostics.value = result.diagnostics
        updateTruthTable()

        // Capture wire states for oscilloscope live wave display
        val currentWireWaves = _wireWaveforms.value.toMutableMap()
        for (wire in result.wires) {
            val hist = (currentWireWaves[wire.id] ?: emptyList()).takeLast(80) + wire.state
            currentWireWaves[wire.id] = hist
        }
        _wireWaveforms.value = currentWireWaves

        // Sound Engine: Trigger piezo buzzer if any BUZZER component input is active HIGH
        val hasActiveBuzzer = result.components.any { it.type == "BUZZER" && it.inputValues["IN"] == DigitalState.HIGH }
        com.example.logiclab.audio.AudioEngine.setBuzzerActive(hasActiveBuzzer, 620f)
    }

    private fun recordTimingSnapshot() {
        val observedComps = _components.value.filter {
            it.type in listOf("CLOCK", "OSCILLATOR", "SWITCH", "LED", "DIGITAL_PROBE")
        }
        val currentChannels = _signalChannels.value.toMutableList()
        val step = _timeStepCount.value + 1
        _timeStepCount.value = step

        val newChannels = observedComps.map { comp ->
            val chName = comp.label.ifBlank { comp.id }
            val state = when (comp.type) {
                "CLOCK", "SWITCH" -> if (comp.switchState) DigitalState.HIGH else DigitalState.LOW
                "OSCILLATOR" -> comp.outputValues["OUT"] ?: comp.outputValues["CLK"] ?: if (comp.switchState) DigitalState.HIGH else DigitalState.LOW
                else -> comp.outputValues["Q"] ?: comp.outputValues["Y"] ?: comp.outputValues["OUT"] ?: comp.inputValues["IN"] ?: DigitalState.LOW
            }
            val existing = currentChannels.find { it.name == chName }
            val hist = (existing?.history ?: emptyList()).takeLast(35) + state
            SignalChannel(chName, hist)
        }
        _signalChannels.value = newChannels

        // Also record continuous wire history for oscilloscope
        val currentWireWaves = _wireWaveforms.value.toMutableMap()
        for (wire in _wires.value) {
            val hist = (currentWireWaves[wire.id] ?: emptyList()).takeLast(80) + wire.state
            currentWireWaves[wire.id] = hist
        }
        _wireWaveforms.value = currentWireWaves
    }

    fun setTruthTableMode(mode: TruthTableMode) {
        _truthTableMode.value = mode
        updateTruthTable()
    }

    fun applyTruthTableRow(row: TruthTableRow) {
        val table = _truthTableData.value
        val inputs = table.inputComponents
        if (inputs.isEmpty()) return

        pushUndoSnapshot()
        _components.value = _components.value.map { comp ->
            var updated = comp
            for (input in inputs) {
                if (input.componentId == comp.id && input.isSwitch) {
                    val desiredState = row.inputValues[input.displayName]
                    if (desiredState != null) {
                        updated = updated.copy(switchState = desiredState.isHigh())
                    }
                }
            }
            updated
        }
        runSimulationStep()
    }

    fun updateTruthTable() {
        val selId = _selectedComponentId.value
        val selComp = _components.value.find { it.id == selId }
        val mode = _truthTableMode.value

        val table = if (mode == TruthTableMode.SELECTED_COMPONENT && selComp != null && selComp.type !in listOf("LED", "DIGITAL_PROBE")) {
            TruthTableEngine.generateForComponent(selComp)
        } else {
            TruthTableEngine.generateForCircuit(_components.value, _wires.value)
        }
        _truthTableData.value = table
    }

    // Canvas & Component Operations
    fun setMode(mode: AppMode) {
        _currentMode.value = mode
    }

    fun setBottomPanel(panel: BottomPanelType) {
        _bottomPanel.value = if (_bottomPanel.value == panel) BottomPanelType.NONE else panel
        if (_bottomPanel.value == BottomPanelType.TRUTH_TABLE) {
            updateTruthTable()
        }
    }

    fun selectComponent(id: String?) {
        _selectedComponentId.value = id
        _selectedWireId.value = null
        updateTruthTable()
        if (id != null) {
            val comp = _components.value.find { it.id == id }
            if (comp?.type == "OSCILLATOR" || comp?.type == "CLOCK") {
                // When clicking on a digital oscillator, automatically visualize waves down below!
                _bottomPanel.value = BottomPanelType.TIMING_DIAGRAM
            }
        }
    }

    fun selectWire(id: String?) {
        _selectedWireId.value = id
        _selectedComponentId.value = null
        if (id != null && _bottomPanel.value == BottomPanelType.OSCILLOSCOPE) {
            addWireProbe(id)
        }
    }

    fun toggleSwitch(id: String) {
        com.example.logiclab.audio.AudioEngine.playClickSound()
        pushUndoSnapshot()
        _components.value = _components.value.map { comp ->
            if (comp.id == id) {
                comp.copy(switchState = !comp.switchState)
            } else {
                comp
            }
        }
        runSimulationStep()
    }

    fun snapCoord(value: Float, gridSize: Float = 20f): Float {
        return Math.round(value / gridSize) * gridSize
    }

    fun moveComponent(id: String, newX: Float, newY: Float) {
        val snappedX = if (snapToGrid.value) snapCoord(newX) else newX
        val snappedY = if (snapToGrid.value) snapCoord(newY) else newY
        _components.value = _components.value.map { comp ->
            if (comp.id == id) {
                comp.copy(x = snappedX, y = snappedY)
            } else {
                comp
            }
        }
    }

    fun addComponentAt(def: ComponentDefinition, worldX: Float, worldY: Float) {
        pushUndoSnapshot()
        val count = _components.value.count { it.type == def.type } + 1
        val newId = "${def.type}_$count"
        val snappedX = if (snapToGrid.value) snapCoord(worldX) else worldX
        val snappedY = if (snapToGrid.value) snapCoord(worldY) else worldY
        val newComp = CircuitComponent(
            id = newId,
            type = def.type,
            x = snappedX,
            y = snappedY,
            label = "${def.name} $count"
        )
        _components.value = _components.value + newComp
        _selectedComponentId.value = newComp.id
        runSimulationStep()
    }

    fun addComponent(def: ComponentDefinition) {
        val centerWorldX = (-panOffset.value.x + 300f) / zoomScale.value
        val centerWorldY = (-panOffset.value.y + 300f) / zoomScale.value
        var targetX = if (snapToGrid.value) snapCoord(centerWorldX.coerceAtLeast(60f)) else centerWorldX.coerceAtLeast(40f)
        var targetY = if (snapToGrid.value) snapCoord(centerWorldY.coerceAtLeast(60f)) else centerWorldY.coerceAtLeast(40f)

        // Stagger placement on grid if location is already occupied
        while (_components.value.any { kotlin.math.abs(it.x - targetX) < 14f && kotlin.math.abs(it.y - targetY) < 14f }) {
            targetX += 40f
            targetY += 40f
        }

        addComponentAt(def, targetX, targetY)
    }

    fun alignAllComponentsToGrid() {
        pushUndoSnapshot()
        _components.value = _components.value.map { comp ->
            comp.copy(
                x = snapCoord(comp.x),
                y = snapCoord(comp.y)
            )
        }
    }

    fun addWire(fromCompId: String, fromPortId: String, toCompId: String, toPortId: String) {
        // Prevent duplicate wires
        val exists = _wires.value.any {
            (it.fromComponentId == fromCompId && it.fromPortId == fromPortId && it.toComponentId == toCompId && it.toPortId == toPortId) ||
                    (it.fromComponentId == toCompId && it.fromPortId == toPortId && it.toComponentId == fromCompId && it.toPortId == fromPortId)
        }
        if (exists) return

        pushUndoSnapshot()
        val newWire = CircuitWire(
            id = "w_${UUID.randomUUID().toString().take(6)}",
            fromComponentId = fromCompId,
            fromPortId = fromPortId,
            toComponentId = toCompId,
            toPortId = toPortId
        )
        _wires.value = _wires.value + newWire
        runSimulationStep()
    }

    fun deleteSelected() {
        val compId = _selectedComponentId.value
        val wireId = _selectedWireId.value

        if (compId != null) {
            pushUndoSnapshot()
            _components.value = _components.value.filter { it.id != compId }
            _wires.value = _wires.value.filter { it.fromComponentId != compId && it.toComponentId != compId }
            _selectedComponentId.value = null
            runSimulationStep()
        } else if (wireId != null) {
            pushUndoSnapshot()
            _wires.value = _wires.value.filter { it.id != wireId }
            _selectedWireId.value = null
            runSimulationStep()
        }
    }

    fun deleteComponent(compId: String) {
        pushUndoSnapshot()
        _components.value = _components.value.filter { it.id != compId }
        _wires.value = _wires.value.filter { it.fromComponentId != compId && it.toComponentId != compId }
        if (_selectedComponentId.value == compId) {
            _selectedComponentId.value = null
        }
        runSimulationStep()
    }

    fun duplicateSelected() {
        val compId = _selectedComponentId.value ?: return
        val comp = _components.value.find { it.id == compId } ?: return
        pushUndoSnapshot()

        val def = ComponentRegistry.get(comp.type) ?: return
        val count = _components.value.count { it.type == comp.type } + 1
        val newComp = comp.copy(
            id = "${comp.type}_$count",
            x = comp.x + 40f,
            y = comp.y + 40f,
            label = "${comp.label} (Copy)"
        )
        _components.value = _components.value + newComp
        _selectedComponentId.value = newComp.id
        runSimulationStep()
    }

    fun rotateComponent(compId: String) {
        pushUndoSnapshot()
        _components.value = _components.value.map { comp ->
            if (comp.id == compId) {
                comp.copy(rotation = (comp.rotation + 90) % 360)
            } else {
                comp
            }
        }
        runSimulationStep()
    }

    fun rotateSelected() {
        val compId = _selectedComponentId.value ?: return
        rotateComponent(compId)
    }

    fun clearCircuit() {
        pushUndoSnapshot()
        _components.value = emptyList()
        _wires.value = emptyList()
        _selectedComponentId.value = null
        _selectedWireId.value = null
        runSimulationStep()
    }

    fun resetSimulation() {
        _components.value = _components.value.map { comp ->
            if (comp.type == "SWITCH") comp.copy(switchState = false) else comp
        }
        _timeStepCount.value = 0
        _signalChannels.value = emptyList()
        runSimulationStep()
    }

    fun toggleSimulating() {
        _isSimulating.value = !_isSimulating.value
    }

    fun loadStarterCircuit(starter: StarterCircuit) {
        pushUndoSnapshot()
        _components.value = starter.components
        _wires.value = starter.wires
        _selectedComponentId.value = null
        _selectedWireId.value = null
        panOffset.value = Offset(60f, 60f)
        zoomScale.value = 1.0f
        runSimulationStep()
    }

    fun loadStarterCircuitById(id: String) {
        val starter = StarterCircuits.list.find { it.id == id } ?: return
        loadStarterCircuit(starter)
        _currentMode.value = AppMode.SIMULATOR
    }

    fun applySynthesizedCircuit(data: CircuitData) {
        pushUndoSnapshot()
        _components.value = data.components
        _wires.value = data.wires
        _selectedComponentId.value = null
        _selectedWireId.value = null
        panOffset.value = Offset(60f, 60f)
        zoomScale.value = 1.0f
        runSimulationStep()
    }

    fun saveCurrentCircuit(name: String, desc: String) {
        viewModelScope.launch {
            repository.saveCircuit(name, desc, _components.value, _wires.value)
        }
    }

    fun loadSavedCircuit(entity: CircuitEntity) {
        viewModelScope.launch {
            val data = repository.loadCircuit(entity.id)
            if (data != null) {
                pushUndoSnapshot()
                _components.value = data.components
                _wires.value = data.wires
                _selectedComponentId.value = null
                _selectedWireId.value = null
                runSimulationStep()
            }
        }
    }

    fun deleteSavedCircuit(id: Long) {
        viewModelScope.launch {
            repository.deleteCircuit(id)
        }
    }
}
