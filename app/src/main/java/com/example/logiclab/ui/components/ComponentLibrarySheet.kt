package com.example.logiclab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.engine.ComponentRegistry
import com.example.logiclab.model.ComponentCategory
import com.example.logiclab.model.ComponentDefinition
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary

@Composable
fun ComponentLibrarySheet(
    onSelectComponent: (ComponentDefinition) -> Unit,
    onDismiss: () -> Unit,
    onOpenKMap: (() -> Unit)? = null,
    onOpenICEncyclopedia: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ComponentCategory?>(null) }

    val allDefinitions = remember { ComponentRegistry.definitions.values.toList() }

    val filteredList = remember(searchQuery, selectedCategory) {
        allDefinitions.filter { def ->
            val matchQuery = searchQuery.isBlank() ||
                    def.name.contains(searchQuery, ignoreCase = true) ||
                    def.type.contains(searchQuery, ignoreCase = true) ||
                    def.booleanFormula.contains(searchQuery, ignoreCase = true)

            val matchCategory = selectedCategory == null || def.category == selectedCategory
            matchQuery && matchCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.70f)
            .background(LabPanelBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("component_library_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMPONENT LIBRARY",
                    color = LabTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tap to place into active schematic",
                    color = LabTextSecondary,
                    fontSize = 12.sp
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_library_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Library",
                    tint = LabTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search gates, adders, mux, decoders...", fontSize = 13.sp, color = LabTextTertiary) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = LabTextSecondary, modifier = Modifier.size(18.dp))
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = LabCardBg,
                unfocusedContainerColor = LabCardBg,
                focusedBorderColor = LabAccentBlue,
                unfocusedBorderColor = LabBorder,
                focusedTextColor = LabTextPrimary,
                unfocusedTextColor = LabTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("component_search_field")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Engineering Solvers Strip
        if (onOpenKMap != null || onOpenICEncyclopedia != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onOpenKMap != null) {
                    Surface(
                        color = Color(0x22388BFD),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LabAccentBlue.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenKMap() }
                            .testTag("lib_open_kmap_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚡ K-Map Solver",
                                color = LabTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (onOpenICEncyclopedia != null) {
                    Surface(
                        color = Color(0x223FB950),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3FB950).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenICEncyclopedia() }
                            .testTag("lib_open_ic_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "📖 7400 IC Pins",
                                color = LabTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${allDefinitions.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = LabCardBg,
                        labelColor = LabTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = LabBorder,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = selectedCategory == null
                    )
                )
            }
            items(ComponentCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                    label = { Text(cat.title, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = LabCardBg,
                        labelColor = LabTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = LabBorder,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = selectedCategory == cat
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Components List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredList) { def ->
                ComponentItemRow(
                    definition = def,
                    onClick = {
                        onSelectComponent(def)
                        onDismiss()
                    }
                )
            }
        }
    }
}

@Composable
fun ComponentItemRow(
    definition: ComponentDefinition,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LabCardBg)
            .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("comp_item_${definition.type}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tag icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(LabPanelBg, RoundedCornerShape(4.dp))
                .border(1.dp, LabBorder, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = definition.type.take(4),
                color = LabAccentBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = definition.name,
                    color = LabTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = definition.category.title,
                    color = LabTextTertiary,
                    fontSize = 10.sp
                )
            }
            if (definition.booleanFormula.isNotBlank()) {
                Text(
                    text = definition.booleanFormula,
                    color = LabTextSecondary,
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Text(
                text = "In: ${definition.inputs.size}  |  Out: ${definition.outputs.size}",
                color = LabTextTertiary,
                fontSize = 10.sp
            )
        }

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add component",
            tint = LabAccentBlue,
            modifier = Modifier.size(20.dp)
        )
    }
}
