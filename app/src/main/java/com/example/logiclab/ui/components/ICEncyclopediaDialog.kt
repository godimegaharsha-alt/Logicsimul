package com.example.logiclab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.logiclab.data.ICDatasheet
import com.example.logiclab.data.ICDatasheetRegistry
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.ComponentDefinition
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh

/**
 * 7400-Series Integrated Circuit Encyclopedia & DIP Pinout Visualizer.
 * Complete offline engineering datasheet reference with interactive pinouts,
 * electrical characteristics, truth tables, and one-tap circuit instantiation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ICEncyclopediaDialog(
    onAddComponent: (ComponentDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val allDatasheets = remember { ICDatasheetRegistry.datasheets }
    var selectedIC by remember { mutableStateOf(allDatasheets.first()) }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) allDatasheets
        else {
            val q = searchQuery.trim().lowercase()
            allDatasheets.filter {
                it.partNumber.lowercase().contains(q) ||
                it.title.lowercase().contains(q) ||
                it.description.lowercase().contains(q)
            }
        }
    }

    // Dialog Entrance Spring Animation
    var isEntered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isEntered = true }
    val dialogScale by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        ),
        label = "icDialogScale"
    )
    val dialogAlpha by animateFloatAsState(
        targetValue = if (isEntered) 1f else 0f,
        animationSpec = tween(180),
        label = "icDialogAlpha"
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth(0.78f)
            .heightIn(max = 370.dp)
            .graphicsLayer {
                scaleX = dialogScale
                scaleY = dialogScale
                alpha = dialogAlpha
            }
            .clip(RoundedCornerShape(16.dp))
            .background(LabPanelBg)
            .border(1.2.dp, LabAccentBlue.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .testTag("ic_encyclopedia_dialog")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Hero Banner Image with dynamic visual asset matching IC category
            val bannerRes = when {
                selectedIC.title.contains("Arithmetic", ignoreCase = true) || selectedIC.title.contains("Adder", ignoreCase = true) || selectedIC.title.contains("Decoder", ignoreCase = true) -> R.drawable.img_cpu_die_macro
                selectedIC.title.contains("Flip-Flop", ignoreCase = true) || selectedIC.title.contains("Counter", ignoreCase = true) || selectedIC.partNumber.contains("555", ignoreCase = true) -> R.drawable.img_ic_workbench
                else -> R.drawable.img_lab_pcb_banner
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Image(
                    painter = painterResource(id = bannerRes),
                    contentDescription = "IC Lab Visualizer Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(72.dp)
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, LabPanelBg)
                            )
                        )
                )

                // Header title row overlaid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = LabAccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "7400 IC Encyclopedia & Pinouts",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Search Bar & IC Chips Selector Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Search Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0F151F))
                        .border(1.dp, Color(0xFF1E2B3E), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = LabAccentBlue,
                        modifier = Modifier.size(13.dp)
                    )
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(color = LabTextPrimary, fontSize = 11.sp),
                        cursorBrush = SolidColor(LabAccentBlue),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search 7400 ICs (7400, 7408, 7474, 555...)",
                                    color = LabTextTertiary,
                                    fontSize = 10.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                // Horizontal IC part number chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList) { ic ->
                        val isSelected = ic.partNumber == selectedIC.partNumber
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else LabCardBg)
                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else LabBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedIC = ic }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("ic_chip_${ic.partNumber}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ic.partNumber,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else LabTextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Selected IC Detailed Datasheet Content (Scrollable)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // IC Title & Add to Schematic Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${selectedIC.partNumber}: ${selectedIC.title}",
                                color = LabTextPrimary,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${selectedIC.family} • ${selectedIC.packageType} (${selectedIC.pinCount} pins)",
                                color = LabAccentBlue,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Add to Circuit Button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    val def = ComponentRegistry.get(selectedIC.equivalentComponentType)
                                    if (def != null) {
                                        onAddComponent(def)
                                        onDismiss()
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("ic_add_to_circuit_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(12.dp))
                            Text(text = "Place IC", color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    // Interactive DIP Package Pinout Graphic
                    DIPPackagePinoutView(ic = selectedIC)
                }

                item {
                    // Technical Description & Boolean Formula
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F151F))
                            .border(1.dp, Color(0xFF1E2B3E), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Functional Description:", color = LabTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = selectedIC.description, color = LabTextPrimary, fontSize = 10.sp)

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Logic Transfer Function:", color = LabTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = selectedIC.booleanFormula,
                            color = LabAccentBlue,
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    // Electrical Characteristics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MetricCard(label = "PROP DELAY", value = selectedIC.propDelay, modifier = Modifier.weight(1f))
                        MetricCard(label = "SUPPLY VOLT", value = selectedIC.supplyVoltage, modifier = Modifier.weight(1f))
                        MetricCard(label = "POWER DISS", value = selectedIC.powerDissipation, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    // Truth Table Matrix
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F151F))
                            .border(1.dp, Color(0xFF1E2B3E), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Functional Truth Table:", color = LabTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)

                        // Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF16212E))
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            selectedIC.truthTableHeaders.forEach { h ->
                                Text(text = h, color = LabAccentBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        // Rows
                        selectedIC.truthTableRows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                row.forEach { cell ->
                                    val cellColor = when (cell) {
                                        "1", "HIGH" -> SignalHigh
                                        "0", "LOW" -> LabTextSecondary
                                        else -> Color(0xFFFFB300)
                                    }
                                    Text(text = cell, color = cellColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DIPPackagePinoutView(ic: ICDatasheet) {
    val totalPins = ic.pinCount
    val halfPins = totalPins / 2
    val leftPins = ic.pins.filter { it.pinNumber <= halfPins }
    val rightPins = ic.pins.filter { it.pinNumber > halfPins }.reversed() // Pin 14 down to Pin 8

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B1017))
            .border(1.dp, Color(0xFF1C2A3A), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // DIP Top Notch Marker
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                .background(Color(0xFF1E2B3E))
        )
        Spacer(modifier = Modifier.height(4.dp))

        // DIP Chip Body with Left & Right Pins
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Pins Column (Pins 1..7 or 1..8)
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.End
            ) {
                leftPins.forEach { pin ->
                    PinBadge(pin = pin, isLeft = true)
                }
            }

            // Center DIP IC Body
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height((halfPins * 22).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF161E28))
                    .border(1.dp, Color(0xFF2B3A4E), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = ic.partNumber,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = ic.packageType,
                        color = LabTextTertiary,
                        fontSize = 7.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right Pins Column (Pins 14..8 or 16..9)
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.Start
            ) {
                rightPins.forEach { pin ->
                    PinBadge(pin = pin, isLeft = false)
                }
            }
        }
    }
}

@Composable
private fun PinBadge(pin: com.example.logiclab.data.ICPin, isLeft: Boolean) {
    val pinColor = when {
        pin.isPower -> SignalError
        pin.isGround -> Color(0xFF78909C)
        pin.name.contains("Y") || pin.name.contains("OUT") || pin.name.contains("Q") -> SignalHigh
        else -> LabAccentBlue
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isLeft) Arrangement.End else Arrangement.Start
    ) {
        if (isLeft) {
            Text(
                text = "${pin.pinNumber}: ${pin.name}",
                color = pinColor,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(3.dp)
                    .background(Color(0xFF90A4AE))
            )
        } else {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(3.dp)
                    .background(Color(0xFF90A4AE))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${pin.name} :${pin.pinNumber}",
                color = pinColor,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF101722))
            .border(1.dp, Color(0xFF1E2D3E), RoundedCornerShape(6.dp))
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, color = LabTextTertiary, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(text = value, color = Color(0xFF58A6FF), fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
