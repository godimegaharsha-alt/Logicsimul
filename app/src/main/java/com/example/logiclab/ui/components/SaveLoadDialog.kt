package com.example.logiclab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logiclab.data.CircuitEntity
import com.example.logiclab.starters.StarterCircuit
import com.example.logiclab.starters.StarterCircuits
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SaveLoadDialog(
    savedCircuits: List<CircuitEntity>,
    onSaveCircuit: (name: String, desc: String) -> Unit,
    onLoadSavedCircuit: (CircuitEntity) -> Unit,
    onDeleteSavedCircuit: (Long) -> Unit,
    onLoadStarterCircuit: (StarterCircuit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Starters, 1: My Saved, 2: Save Current
    var circuitName by remember { mutableStateOf("") }
    var circuitDesc by remember { mutableStateOf("") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }

    // Dialog Entrance Spring Animation
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        ),
        label = "saveDialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(180),
        label = "saveDialogAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.78f)
                .heightIn(max = 370.dp)
                .graphicsLayer {
                    scaleX = dialogScale
                    scaleY = dialogScale
                    alpha = dialogAlpha
                }
                .clip(RoundedCornerShape(14.dp))
                .border(1.2.dp, LabAccentBlue.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .testTag("save_load_dialog"),
            shape = RoundedCornerShape(14.dp),
            color = LabPanelBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CIRCUIT REPOSITORY",
                            color = LabTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Starters, student designs & schematic archives",
                            color = LabTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_repo_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LabTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = LabCardBg,
                    contentColor = LabAccentBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = LabAccentBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Starters (${StarterCircuits.list.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("My Saved (${savedCircuits.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Save Current", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Starters list
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(StarterCircuits.list) { starter ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(LabCardBg)
                                        .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            onLoadStarterCircuit(starter)
                                            onDismiss()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = starter.name,
                                                color = LabTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = starter.category,
                                                color = LabAccentBlue,
                                                fontSize = 10.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = starter.description,
                                            color = LabTextSecondary,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Components: ${starter.components.size} | Connections: ${starter.wires.size}",
                                            color = LabTextTertiary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = "Load",
                                        tint = LabAccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // My Saved circuits list
                        if (savedCircuits.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved circuits yet. Switch to 'Save Current' tab to save your design.",
                                    color = LabTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedCircuits) { circuit ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(LabCardBg)
                                            .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                onLoadSavedCircuit(circuit)
                                                onDismiss()
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = circuit.name,
                                                color = LabTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (circuit.description.isNotBlank()) {
                                                Text(
                                                    text = circuit.description,
                                                    color = LabTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "Saved on ${dateFormat.format(Date(circuit.updatedAt))}",
                                                color = LabTextTertiary,
                                                fontSize = 10.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { onDeleteSavedCircuit(circuit.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = SignalError.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Save Current form
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Save Active Schematic",
                                color = LabTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = circuitName,
                                onValueChange = { circuitName = it },
                                label = { Text("Circuit Name (e.g. 4-bit Ripple Carry Adder)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_circuit_name_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = LabCardBg,
                                    unfocusedContainerColor = LabCardBg,
                                    focusedBorderColor = LabAccentBlue,
                                    unfocusedBorderColor = LabBorder,
                                    focusedTextColor = LabTextPrimary,
                                    unfocusedTextColor = LabTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = circuitDesc,
                                onValueChange = { circuitDesc = it },
                                label = { Text("Description / Lab Notes") },
                                maxLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_circuit_desc_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = LabCardBg,
                                    unfocusedContainerColor = LabCardBg,
                                    focusedBorderColor = LabAccentBlue,
                                    unfocusedBorderColor = LabBorder,
                                    focusedTextColor = LabTextPrimary,
                                    unfocusedTextColor = LabTextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (circuitName.isNotBlank()) {
                                        onSaveCircuit(circuitName, circuitDesc)
                                        selectedTab = 1
                                    }
                                },
                                enabled = circuitName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = LabAccentBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_save_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Circuit Locally")
                            }
                        }
                    }
                }
            }
        }
    }
}
