package com.example.logiclab.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.logiclab.model.DiagnosticSeverity
import com.example.logiclab.ui.canvas.Circuit3DCanvas
import com.example.logiclab.ui.canvas.SchematicCanvas
import com.example.logiclab.ui.components.AiAssistantDialog
import com.example.logiclab.ui.components.AppMode
import com.example.logiclab.ui.components.ComponentLibrarySheet
import com.example.logiclab.ui.components.DiagnosticsPanel
import com.example.logiclab.ui.components.GateDetailsDialog
import com.example.logiclab.ui.components.GateSearchBarAndCatalog
import com.example.logiclab.ui.components.ICEncyclopediaDialog
import com.example.logiclab.ui.components.KMapDialog
import com.example.logiclab.ui.components.LabTopBar
import com.example.logiclab.ui.components.SaveLoadDialog
import com.example.logiclab.ui.components.SelectionToolbar
import com.example.logiclab.ui.components.ThemeSelectionDialog
import com.example.logiclab.ui.components.OscilloscopeView
import com.example.logiclab.ui.components.TimingDiagramView
import com.example.logiclab.ui.components.TruthTableViewer
import com.example.logiclab.ui.learn.ExperimentModeView
import com.example.logiclab.ui.learn.LearnModeView
import com.example.logiclab.viewmodel.ActiveDialog
import com.example.logiclab.viewmodel.BottomPanelType
import com.example.logiclab.viewmodel.LogicLabViewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.LogicLabTheme
import com.example.ui.theme.getLogicLabColors
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalUnknown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: LogicLabViewModel = viewModel()
) {
    val currentMode by viewModel.currentMode.collectAsState()
    val components by viewModel.components.collectAsState()
    val wires by viewModel.wires.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val selectedCompId by viewModel.selectedComponentId.collectAsState()
    val selectedWireId by viewModel.selectedWireId.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val bottomPanel by viewModel.bottomPanel.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val isWireDrawingMode by viewModel.isWireDrawingMode.collectAsState()
    val isSignalFlowMode by viewModel.isSignalFlowMode.collectAsState()
    val is3DMode by viewModel.is3DMode.collectAsState()
    val board3DStyle by viewModel.board3DStyle.collectAsState()

    val panOffset by viewModel.panOffset.collectAsState()
    val zoomScale by viewModel.zoomScale.collectAsState()
    val snapToGrid by viewModel.snapToGrid.collectAsState()

    val truthTableData by viewModel.truthTableData.collectAsState()
    val signalChannels by viewModel.signalChannels.collectAsState()
    val timeStepCount by viewModel.timeStepCount.collectAsState()
    val probedWireIds by viewModel.probedWireIds.collectAsState()
    val wireWaveforms by viewModel.wireWaveforms.collectAsState()
    val savedCircuits by viewModel.savedCircuits.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val themeColors = getLogicLabColors(currentTheme)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedComponent = components.find { it.id == selectedCompId }
    val errorCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
    val warningCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }

    LogicLabTheme(theme = currentTheme) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                LabTopBar(
                    currentMode = currentMode,
                    onModeChange = { viewModel.setMode(it) },
                    isSimulating = isSimulating,
                    onToggleSimulate = { viewModel.toggleSimulating() },
                    onStepSimulate = { viewModel.stepClock() },
                    onResetSimulation = { viewModel.resetSimulation() },
                    onNewCircuit = { viewModel.clearCircuit() },
                    onOpenRepo = { viewModel.openSaveLoad() },
                    onSaveCircuit = { viewModel.openSaveLoad() },
                    onUndo = { viewModel.undo() },
                    onRedo = { viewModel.redo() },
                    canUndo = canUndo,
                    canRedo = canRedo,
                    isWireDrawingMode = isWireDrawingMode,
                    onToggleWireDrawingMode = { viewModel.toggleWireDrawingMode() },
                    isSignalFlowMode = isSignalFlowMode,
                    onToggleSignalFlowMode = { viewModel.toggleSignalFlowMode() },
                    onOpenAiAssistant = { viewModel.openAiAssistant() },
                    is3DMode = is3DMode,
                    onToggle3DMode = { viewModel.toggle3DMode() },
                    board3DStyle = board3DStyle,
                    onToggleBoard3DStyle = { viewModel.toggleBoard3DStyle() },
                    onOpenThemeSelector = { viewModel.openThemeSelector() }
                )
            },
            containerColor = themeColors.darkBg
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            when (currentMode) {
                AppMode.SIMULATOR -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Top Layer: Gate Search Bar & Direct Catalog Access
                        // Docked cleanly at the top to eliminate any overlapping with canvas or HUDs
                        GateSearchBarAndCatalog(
                            onAddComponent = { def ->
                                viewModel.addComponent(def)
                            },
                            onOpenCatalog = { viewModel.openComponentLibrary() }
                        )

                        // Canvas Area with Floating Toolbars and Engineering Drawer
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Crossfade(
                                targetState = is3DMode,
                                animationSpec = tween(400),
                                label = "canvas_2d_3d_crossfade"
                            ) { mode3D ->
                                if (mode3D) {
                                    Circuit3DCanvas(
                                        components = components,
                                        wires = wires,
                                        selectedComponentId = selectedCompId,
                                        isSimulating = isSimulating,
                                        onToggleSimulate = { viewModel.toggleSimulating() },
                                        onStepSimulate = { viewModel.runSimulationStep() },
                                        onResetSimulation = { viewModel.resetSimulation() },
                                        onComponentSelect = { viewModel.selectComponent(it) },
                                        onComponentToggleSwitch = { viewModel.toggleSwitch(it) },
                                        onComponentDoubleClick = { compId ->
                                            viewModel.selectComponent(compId)
                                            viewModel.openGateDetails(compId)
                                        },
                                        onSwitchTo2D = { viewModel.set3DMode(false) },
                                        isSignalFlowMode = isSignalFlowMode,
                                        onToggleSignalFlowMode = { viewModel.toggleSignalFlowMode() },
                                        board3DStyle = board3DStyle,
                                        onToggleBoard3DStyle = { viewModel.toggleBoard3DStyle() },
                                        is3DMode = mode3D,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    SchematicCanvas(
                                        components = components,
                                        wires = wires,
                                        selectedComponentId = selectedCompId,
                                        selectedWireId = selectedWireId,
                                        snapToGrid = snapToGrid,
                                        panOffset = panOffset,
                                        zoomScale = zoomScale,
                                        isSimulating = isSimulating,
                                        onPanZoomChange = { newPan, newZoom ->
                                            viewModel.panOffset.value = newPan
                                            viewModel.zoomScale.value = newZoom
                                        },
                                        onComponentSelect = { viewModel.selectComponent(it) },
                                        onComponentDoubleClick = { compId ->
                                            viewModel.selectComponent(compId)
                                            viewModel.openGateDetails(compId)
                                        },
                                        onComponentMove = { id, x, y -> viewModel.moveComponent(id, x, y) },
                                        onComponentToggleSwitch = { viewModel.toggleSwitch(it) },
                                        onWireSelect = { viewModel.selectWire(it) },
                                        onWireCreate = { fromC, fromP, toC, toP ->
                                            viewModel.addWire(fromC, fromP, toC, toP)
                                        },
                                        isWireDrawingMode = isWireDrawingMode,
                                        onToggleWireDrawingMode = { viewModel.toggleWireDrawingMode() },
                                        canUndo = canUndo,
                                        canRedo = canRedo,
                                        onUndo = { viewModel.undo() },
                                        onRedo = { viewModel.redo() },
                                        onDropNewComponent = { def, x, y ->
                                            viewModel.addComponentAt(def, x, y)
                                        },
                                        onOpenFullLibrary = {
                                            viewModel.openComponentLibrary()
                                        },
                                        isSignalFlowMode = isSignalFlowMode,
                                        onToggleSignalFlowMode = { viewModel.toggleSignalFlowMode() },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                    var isTruthTableExpanded by remember { mutableStateOf(false) }
                    val panelDrawerHeight by animateDpAsState(
                        targetValue = if (isTruthTableExpanded && bottomPanel == BottomPanelType.TRUTH_TABLE) 180.dp
                                      else if (bottomPanel == BottomPanelType.OSCILLOSCOPE) 220.dp
                                      else if (bottomPanel == BottomPanelType.TIMING_DIAGRAM) 160.dp
                                      else 140.dp,
                        animationSpec = tween(300),
                        label = "panelHeight"
                    )

                    // Floating Gate Information & Action Toolbar
                    // Automatically hidden when bottom drawer is active to prevent overlapping windows
                    if (bottomPanel == BottomPanelType.NONE) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(
                                    bottom = 44.dp,
                                    start = 8.dp,
                                    end = 8.dp
                                )
                        ) {
                            SelectionToolbar(
                                selectedComponent = selectedComponent,
                                selectedWireId = selectedWireId,
                                onDeleteSelected = { viewModel.deleteSelected() },
                                onDuplicateSelected = { viewModel.duplicateSelected() },
                                onRotateSelected = { viewModel.rotateSelected() },
                                onToggleSelectedSwitch = {
                                    if (selectedCompId != null) viewModel.toggleSwitch(selectedCompId!!)
                                },
                                onOpenDetails = {
                                    if (selectedCompId != null) {
                                        viewModel.openGateDetails(selectedCompId!!)
                                    }
                                },
                                onOpenWaveforms = {
                                    viewModel.setBottomPanel(BottomPanelType.TIMING_DIAGRAM)
                                },
                                onProbeWire = { wireId ->
                                    viewModel.openOscilloscopeForWire(wireId)
                                },
                                onDismiss = {
                                    viewModel.selectComponent(null)
                                    viewModel.selectWire(null)
                                }
                            )
                        }

                        // Sleek, compact Floating Action Button to add components
                        val fabInteraction = remember { MutableInteractionSource() }
                        val fabPressed by fabInteraction.collectIsPressedAsState()
                        val fabScale by animateFloatAsState(
                            targetValue = if (fabPressed) 0.88f else 1.0f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "fabScale"
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(bottom = 44.dp, end = 10.dp)
                                .size(34.dp)
                                .scale(fabScale)
                                .clip(CircleShape)
                                .background(LabAccentBlue)
                                .clickable(
                                    interactionSource = fabInteraction,
                                    indication = null,
                                    onClick = { viewModel.openComponentLibrary() }
                                )
                                .testTag("add_component_fab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Component", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Bottom Engineering Dock (Truth Table / Diagnostics / Timing Diagram)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        // Expandable Panel Drawer
                        AnimatedVisibility(
                            visible = bottomPanel != BottomPanelType.NONE,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(panelDrawerHeight)
                                    .background(LabPanelBg)
                                    .border(1.dp, LabBorder)
                            ) {
                                when (bottomPanel) {
                                    BottomPanelType.TRUTH_TABLE -> TruthTableViewer(
                                        truthTable = truthTableData,
                                        onApplyRow = { row -> viewModel.applyTruthTableRow(row) },
                                        onModeChange = { mode -> viewModel.setTruthTableMode(mode) },
                                        isExpanded = isTruthTableExpanded,
                                        onToggleExpand = { isTruthTableExpanded = !isTruthTableExpanded }
                                    )
                                    BottomPanelType.DIAGNOSTICS -> DiagnosticsPanel(diagnostics = diagnostics)
                                    BottomPanelType.TIMING_DIAGRAM -> TimingDiagramView(
                                        channels = signalChannels,
                                        timeStepCount = timeStepCount,
                                        selectedComponent = selectedComponent
                                    )
                                    BottomPanelType.OSCILLOSCOPE -> OscilloscopeView(
                                        wires = wires,
                                        components = components,
                                        selectedWireId = selectedWireId,
                                        probedWireIds = probedWireIds,
                                        wireWaveforms = wireWaveforms,
                                        timeStepCount = timeStepCount,
                                        onToggleWireProbe = { wireId -> viewModel.toggleWireProbe(wireId) },
                                        onAddWireProbe = { wireId -> viewModel.addWireProbe(wireId) },
                                        onRemoveWireProbe = { wireId -> viewModel.removeWireProbe(wireId) },
                                        onClearProbes = { viewModel.clearWireProbes() }
                                    )
                                    BottomPanelType.NONE -> {}
                                }
                            }
                        }

                        // Dock Button Bar (Small & Sleek)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.panelBg)
                                .navigationBarsPadding()
                                .border(1.dp, themeColors.border)
                                .padding(horizontal = 4.dp, vertical = 2.5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DockTabButton(
                                    label = "Truth",
                                    icon = Icons.Default.TableChart,
                                    isActive = bottomPanel == BottomPanelType.TRUTH_TABLE,
                                    onClick = {
                                        viewModel.setBottomPanel(
                                            if (bottomPanel == BottomPanelType.TRUTH_TABLE) BottomPanelType.NONE else BottomPanelType.TRUTH_TABLE
                                        )
                                    },
                                    tag = "dock_truth_table_btn"
                                )
                                DockTabButton(
                                    label = "Diagnostics",
                                    icon = Icons.Default.Warning,
                                    badgeCount = errorCount + warningCount,
                                    badgeColor = if (errorCount > 0) SignalError else SignalUnknown,
                                    isActive = bottomPanel == BottomPanelType.DIAGNOSTICS,
                                    onClick = {
                                        viewModel.setBottomPanel(
                                            if (bottomPanel == BottomPanelType.DIAGNOSTICS) BottomPanelType.NONE else BottomPanelType.DIAGNOSTICS
                                        )
                                    },
                                    tag = "dock_diagnostics_btn"
                                )
                                DockTabButton(
                                    label = "Timing",
                                    icon = Icons.Default.Analytics,
                                    isActive = bottomPanel == BottomPanelType.TIMING_DIAGRAM,
                                    onClick = {
                                        viewModel.setBottomPanel(
                                            if (bottomPanel == BottomPanelType.TIMING_DIAGRAM) BottomPanelType.NONE else BottomPanelType.TIMING_DIAGRAM
                                        )
                                    },
                                    tag = "dock_timing_btn"
                                )
                                DockTabButton(
                                    label = "Scope",
                                    icon = Icons.Default.ShowChart,
                                    isActive = bottomPanel == BottomPanelType.OSCILLOSCOPE,
                                    badgeCount = if (selectedWireId != null || probedWireIds.isNotEmpty()) probedWireIds.size.coerceAtLeast(1) else 0,
                                    badgeColor = Color(0xFF00E5FF),
                                    onClick = {
                                        viewModel.setBottomPanel(
                                            if (bottomPanel == BottomPanelType.OSCILLOSCOPE) BottomPanelType.NONE else BottomPanelType.OSCILLOSCOPE
                                        )
                                    },
                                    tag = "dock_oscilloscope_btn"
                                )
                            }

                            // Snap to grid toggle (Small with animated transition)
                            val snapBg by animateColorAsState(
                                targetValue = if (snapToGrid) Color(0x33388BFD) else Color.Transparent,
                                animationSpec = tween(200),
                                label = "snapBg"
                            )
                            val snapColor by animateColorAsState(
                                targetValue = if (snapToGrid) LabAccentBlue else LabTextSecondary,
                                animationSpec = tween(200),
                                label = "snapColor"
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(snapBg)
                                    .clickable { viewModel.snapToGrid.value = !snapToGrid }
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = null,
                                    tint = snapColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.5.dp))
                                Text(
                                    text = if (snapToGrid) "Snap ON" else "Snap OFF",
                                    color = snapColor,
                                    fontSize = 8.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                } // close Box
            } // close Column
        }
        AppMode.LEARN -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                LearnModeView(
                    onOpenStarterCircuit = { circuitId ->
                        viewModel.loadStarterCircuitById(circuitId)
                    }
                )
            }
        }
        AppMode.EXPERIMENTS -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                ExperimentModeView(
                    onLoadExperimentInSimulator = { circuitId ->
                        viewModel.loadStarterCircuitById(circuitId)
                    }
                )
            }
        }
    }
        }
    }

    // Unified Single-Active Dialog & Sheet Renderer (Guarantees ZERO overlapping dialog boxes)
    when (val dialog = activeDialog) {
        is ActiveDialog.None -> {}
        is ActiveDialog.ComponentLibrary -> {
            ModalBottomSheet(
                onDismissRequest = { viewModel.closeDialog() },
                sheetState = sheetState,
                containerColor = LabPanelBg,
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                ComponentLibrarySheet(
                    onSelectComponent = { def ->
                        viewModel.addComponent(def)
                        viewModel.closeDialog()
                    },
                    onDismiss = { viewModel.closeDialog() },
                    onOpenKMap = { viewModel.openKMap() },
                    onOpenICEncyclopedia = { viewModel.openICEncyclopedia() }
                )
            }
        }
        is ActiveDialog.AiAssistant -> {
            AiAssistantDialog(
                components = components,
                wires = wires,
                selectedComponentId = selectedCompId,
                onApplySynthesizedCircuit = { circuitData ->
                    viewModel.applySynthesizedCircuit(circuitData)
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is ActiveDialog.SaveLoad -> {
            SaveLoadDialog(
                savedCircuits = savedCircuits,
                onSaveCircuit = { name, desc ->
                    viewModel.saveCurrentCircuit(name, desc)
                },
                onLoadSavedCircuit = { entity ->
                    viewModel.loadSavedCircuit(entity)
                },
                onDeleteSavedCircuit = { id ->
                    viewModel.deleteSavedCircuit(id)
                },
                onLoadStarterCircuit = { starter ->
                    viewModel.loadStarterCircuit(starter)
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is ActiveDialog.GateDetails -> {
            val activeDetailsGate = components.find { it.id == dialog.componentId }
            if (activeDetailsGate != null) {
                GateDetailsDialog(
                    component = activeDetailsGate,
                    onDelete = {
                        viewModel.deleteComponent(activeDetailsGate.id)
                        viewModel.closeDialog()
                    },
                    onDuplicate = {
                        viewModel.duplicateSelected()
                    },
                    onRotate = {
                        viewModel.rotateSelected()
                    },
                    onToggleSwitch = {
                        viewModel.toggleSwitch(activeDetailsGate.id)
                    },
                    onDismiss = {
                        viewModel.closeDialog()
                    }
                )
            }
        }
        is ActiveDialog.KMap -> {
            KMapDialog(
                onApplySynthesizedCircuit = { circuitData ->
                    viewModel.applySynthesizedCircuit(circuitData)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is ActiveDialog.ICEncyclopedia -> {
            ICEncyclopediaDialog(
                onAddComponent = { def ->
                    viewModel.addComponent(def)
                    viewModel.closeDialog()
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
        is ActiveDialog.ThemeSelector -> {
            ThemeSelectionDialog(
                currentTheme = currentTheme,
                onSelectTheme = { selectedTheme ->
                    viewModel.setTheme(selectedTheme)
                },
                onDismiss = { viewModel.closeDialog() }
            )
        }
    }
    }
}

@Composable
fun DockTabButton(
    label: String,
    icon: ImageVector,
    badgeCount: Int = 0,
    badgeColor: Color = SignalHigh,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tabScale"
    )
    val tabBg by animateColorAsState(
        targetValue = if (isActive) Color(0x33388BFD) else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val tabBorder by animateColorAsState(
        targetValue = if (isActive) LabAccentBlue.copy(alpha = 0.6f) else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBorder"
    )
    val tabIconColor by animateColorAsState(
        targetValue = if (isActive) LabAccentBlue else LabTextSecondary,
        animationSpec = tween(200),
        label = "tabIcon"
    )
    val tabTextColor by animateColorAsState(
        targetValue = if (isActive) LabTextPrimary else LabTextSecondary,
        animationSpec = tween(200),
        label = "tabText"
    )

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(3.dp))
            .background(tabBg)
            .border(1.dp, tabBorder, RoundedCornerShape(3.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 2.5.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tabIconColor,
            modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(2.5.dp))
        Text(
            text = label,
            color = tabTextColor,
            fontSize = 8.8.sp,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        if (badgeCount > 0) {
            Spacer(modifier = Modifier.width(2.5.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeColor)
                    .padding(horizontal = 3.5.dp, vertical = 0.5.dp)
            ) {
                Text(
                    text = "$badgeCount",
                    color = Color.Black,
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
