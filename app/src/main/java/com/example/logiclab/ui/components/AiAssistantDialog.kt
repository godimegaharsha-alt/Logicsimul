package com.example.logiclab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logiclab.ai.LabAssistantService
import com.example.logiclab.data.CircuitData
import com.example.logiclab.model.CircuitComponent
import com.example.logiclab.model.CircuitWire
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalHigh
import kotlinx.coroutines.launch

@Composable
fun AiAssistantDialog(
    components: List<CircuitComponent>,
    wires: List<CircuitWire>,
    selectedComponentId: String?,
    onApplySynthesizedCircuit: (CircuitData) -> Unit,
    onDismiss: () -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Dialog Entrance Spring Animation
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        ),
        label = "aiDialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(180),
        label = "aiDialogAlpha"
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
                .testTag("ai_assistant_dialog"),
            shape = RoundedCornerShape(14.dp),
            color = LabPanelBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0x22388BFD), RoundedCornerShape(6.dp))
                                .border(1.dp, LabAccentBlue.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = LabAccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "LAB ASSISTANT",
                                color = LabTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Technical analysis, pin tracing & circuit synthesis",
                                color = LabTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_assistant_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LabTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isLoading = true
                            statusMessage = "Analyzing circuit topology and propagation states..."
                            scope.launch {
                                responseText = LabAssistantService.explainCircuit(components, wires, selectedComponentId)
                                isLoading = false
                                statusMessage = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LabTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = LabAccentBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Explain Circuit", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedComponentId != null) {
                                isLoading = true
                                statusMessage = "Tracing back signals for $selectedComponentId..."
                                scope.launch {
                                    val comp = components.find { it.id == selectedComponentId }
                                    val portId = comp?.outputValues?.keys?.firstOrNull() ?: "Y"
                                    responseText = LabAssistantService.explainWhyOutputState(components, wires, selectedComponentId, portId)
                                    isLoading = false
                                    statusMessage = null
                                }
                            } else {
                                responseText = "Select a component or gate on the schematic canvas first to trace why its output is HIGH or LOW."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LabTextPrimary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = SignalHigh)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Trace Selected", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Natural Language Synthesis Input
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            "e.g. 'Build a 4:1 multiplexer with switches and LED' or 'Build a full adder'",
                            fontSize = 12.sp,
                            color = LabTextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_prompt_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = LabCardBg,
                        unfocusedContainerColor = LabCardBg,
                        focusedBorderColor = LabAccentBlue,
                        unfocusedBorderColor = LabBorder,
                        focusedTextColor = LabTextPrimary,
                        unfocusedTextColor = LabTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (promptInput.isNotBlank()) {
                                isLoading = true
                                statusMessage = "Synthesizing circuit layout & wiring..."
                                scope.launch {
                                    val result = LabAssistantService.buildCircuitFromDescription(promptInput)
                                    isLoading = false
                                    statusMessage = null
                                    if (result.isSuccess) {
                                        val circuit = result.getOrThrow()
                                        onApplySynthesizedCircuit(circuit)
                                        responseText = "Successfully synthesized circuit with ${circuit.components.size} components and ${circuit.wires.size} wires. Placed on schematic canvas!"
                                    } else {
                                        responseText = "Error generating circuit: ${result.exceptionOrNull()?.message}"
                                    }
                                }
                            }
                        },
                        enabled = !isLoading && promptInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = LabAccentBlue),
                        modifier = Modifier.testTag("synthesize_circuit_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Synthesize Circuit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status or Response output area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 220.dp)
                        .background(LabCardBg, RoundedCornerShape(6.dp))
                        .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
                        .padding(12.dp)
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = LabAccentBlue,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = statusMessage ?: "Processing...",
                                color = LabTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else if (responseText != null) {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(scrollState)
                        ) {
                            Text(
                                text = responseText!!,
                                color = LabTextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Enter a query or select an action above.",
                                color = LabTextSecondary,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Explains signals directly from simulator truth states.",
                                color = LabTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
