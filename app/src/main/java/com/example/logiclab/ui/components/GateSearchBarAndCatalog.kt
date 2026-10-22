package com.example.logiclab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
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
import com.example.ui.theme.SignalHigh

/**
 * Gate Search Bar & Interactive Catalog Header.
 * Features:
 * 1. Fast instant search across all logic gates, oscillators, arithmetic, and sequential logic.
 * 2. Visual Gate Catalog accessible directly from the search bar.
 * 3. Quick-add draggable/clickable chips with fluid animated transitions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateSearchBarAndCatalog(
    onAddComponent: (ComponentDefinition) -> Unit,
    onOpenCatalog: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isCatalogSheetOpen by remember { mutableStateOf(false) }
    var isChipsRowVisible by remember { mutableStateOf(true) }

    val allComponents = remember { ComponentRegistry.definitions.values.toList() }

    // Filtered components based on search query
    val filteredComponents = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            // Default common gates including Digital Oscillator
            listOf(
                "AND", "OR", "NOT", "NAND", "NOR", "XOR",
                "OSCILLATOR", "SWITCH", "LED", "CLOCK",
                "DIGITAL_PROBE", "SR_LATCH", "D_LATCH",
                "HALF_ADDER", "FULL_ADDER", "D_FLIP_FLOP", "MUX_4_1"
            ).mapNotNull { ComponentRegistry.get(it) }
        } else {
            val query = searchQuery.trim().lowercase()
            allComponents.filter { def ->
                def.name.lowercase().contains(query) ||
                def.type.lowercase().contains(query) ||
                def.description.lowercase().contains(query) ||
                def.booleanFormula.lowercase().contains(query) ||
                def.category.name.lowercase().contains(query)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("gate_search_and_catalog_root"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Search Bar & Catalog Trigger Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LabPanelBg.copy(alpha = 0.95f))
                .border(1.dp, LabBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Search Input Field
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F151F))
                    .border(1.dp, Color(0xFF1E2B3E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Gates",
                    tint = LabAccentBlue,
                    modifier = Modifier.size(14.dp)
                )

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = LabTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(LabAccentBlue),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("gate_search_input"),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search gates (AND, Latches, OSC...)",
                                color = LabTextTertiary,
                                fontSize = 10.5.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .clickable { searchQuery = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear Search",
                            tint = LabTextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Catalog Button: Direct access to the categorized gate library
            val catInteraction = remember { MutableInteractionSource() }
            val isCatPressed by catInteraction.collectIsPressedAsState()
            val catScale by animateFloatAsState(
                targetValue = if (isCatPressed) 0.92f else 1.0f,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "catScale"
            )

            Row(
                modifier = Modifier
                    .scale(catScale)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF162436))
                    .border(1.dp, LabAccentBlue.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .clickable(
                        interactionSource = catInteraction,
                        indication = null,
                        onClick = { onOpenCatalog?.invoke() ?: run { isCatalogSheetOpen = true } }
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("open_gate_catalog_btn"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Gate Catalog",
                    tint = LabAccentBlue,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Catalog",
                    color = LabTextPrimary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Collapse / Expand toggle button for quick chips row
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF162436))
                    .border(1.dp, Color(0xFF223247), RoundedCornerShape(6.dp))
                    .clickable { isChipsRowVisible = !isChipsRowVisible }
                    .padding(horizontal = 6.dp, vertical = 5.dp)
                    .testTag("toggle_gate_chips_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isChipsRowVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isChipsRowVisible) "Collapse Quick Gates" else "Expand Quick Gates",
                    tint = LabTextSecondary,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        // Horizontal Quick-Add Gate Chips Row
        AnimatedVisibility(
            visible = isChipsRowVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredComponents.forEach { def ->
                    QuickGateChip(
                        definition = def,
                        onClickAdd = { onAddComponent(def) }
                    )
                }

                if (filteredComponents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF131A24))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "No gates found for \"$searchQuery\"",
                            color = LabTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }

    // Full Gate Catalog Modal Bottom Sheet (fallback when no central handler provided)
    if (onOpenCatalog == null && isCatalogSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isCatalogSheetOpen = false },
            sheetState = sheetState,
            containerColor = Color(0xFF0F151F),
            contentColor = LabTextPrimary,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            FullGateCatalogSheet(
                onSelectComponent = { def ->
                    onAddComponent(def)
                    isCatalogSheetOpen = false
                },
                onDismiss = { isCatalogSheetOpen = false }
            )
        }
    }
}

@Composable
private fun QuickGateChip(
    definition: ComponentDefinition,
    onClickAdd: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipScale"
    )

    val badgeColor = when (definition.type) {
        "OSCILLATOR", "CLOCK" -> Color(0xFFFFB300)
        "SWITCH" -> SignalHigh
        "LED", "DIGITAL_PROBE" -> Color(0xFF00E676)
        else -> LabAccentBlue
    }

    Row(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF141C28))
            .border(1.dp, Color(0xFF223247), RoundedCornerShape(7.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClickAdd
            )
            .padding(horizontal = 7.dp, vertical = 4.dp)
            .testTag("gate_chip_${definition.type}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // Mini Abbreviation Badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(badgeColor.copy(alpha = 0.2f))
                .border(0.8.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            val abbr = definition.type.take(2).uppercase()
            Text(
                text = abbr,
                color = badgeColor,
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Gate Name
        Text(
            text = definition.name,
            color = LabTextPrimary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
        )

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add",
            tint = LabTextTertiary,
            modifier = Modifier.size(11.dp)
        )
    }
}

/**
 * Categorized Full Gate Catalog Sheet
 */
@Composable
fun FullGateCatalogSheet(
    onSelectComponent: (ComponentDefinition) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var catalogSearch by remember { mutableStateOf("") }

    val categories = remember {
        listOf(
            "ALL" to null,
            "GATES" to ComponentCategory.BASIC_GATES,
            "OSC / CLOCK" to ComponentCategory.SEQUENTIAL,
            "I/O" to ComponentCategory.IO,
            "ARITHMETIC" to ComponentCategory.ARITHMETIC,
            "SEQUENTIAL" to ComponentCategory.SEQUENTIAL
        )
    }

    val allDefs = remember { ComponentRegistry.definitions.values.toList() }

    val displayedDefs = remember(selectedCategoryIndex, catalogSearch) {
        val catFilter = categories[selectedCategoryIndex].second
        val query = catalogSearch.trim().lowercase()

        allDefs.filter { def ->
            val matchesCategory = when (selectedCategoryIndex) {
                0 -> true // ALL
                2 -> def.type == "OSCILLATOR" || def.type == "CLOCK" // OSC / CLOCK
                else -> def.category == catFilter
            }
            val matchesQuery = query.isEmpty() ||
                def.name.lowercase().contains(query) ||
                def.type.lowercase().contains(query) ||
                def.description.lowercase().contains(query) ||
                def.booleanFormula.lowercase().contains(query)

            matchesCategory && matchesQuery
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("full_gate_catalog_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "GATE & COMPONENT CATALOG",
                    color = LabTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Tap any component to instantiate on circuit canvas",
                    color = LabTextSecondary,
                    fontSize = 10.sp
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A2332))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Catalog",
                    tint = LabTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Catalog Search Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141D2B))
                .border(1.dp, Color(0xFF223247), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = LabAccentBlue,
                modifier = Modifier.size(15.dp)
            )
            BasicTextField(
                value = catalogSearch,
                onValueChange = { catalogSearch = it },
                singleLine = true,
                textStyle = TextStyle(color = LabTextPrimary, fontSize = 11.5.sp),
                cursorBrush = SolidColor(LabAccentBlue),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (catalogSearch.isEmpty()) {
                        Text(
                            text = "Filter catalog by name, formula, or type...",
                            color = LabTextTertiary,
                            fontSize = 11.sp
                        )
                    }
                    innerTextField()
                }
            )
            if (catalogSearch.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = LabTextSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { catalogSearch = "" }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = Color.Transparent,
            contentColor = LabAccentBlue,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryIndex]),
                    height = 2.dp,
                    color = LabAccentBlue
                )
            },
            divider = {}
        ) {
            categories.forEachIndexed { index, (name, _) ->
                Tab(
                    selected = selectedCategoryIndex == index,
                    onClick = { selectedCategoryIndex = index },
                    text = {
                        Text(
                            text = name,
                            fontSize = 10.sp,
                            fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategoryIndex == index) LabAccentBlue else LabTextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Catalog Component List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(displayedDefs, key = { it.type }) { def ->
                CatalogItemCard(
                    definition = def,
                    onAdd = { onSelectComponent(def) }
                )
            }
        }
    }
}

@Composable
private fun CatalogItemCard(
    definition: ComponentDefinition,
    onAdd: () -> Unit
) {
    val badgeColor = when (definition.type) {
        "OSCILLATOR", "CLOCK" -> Color(0xFFFFB300)
        "SWITCH" -> SignalHigh
        "LED", "DIGITAL_PROBE" -> Color(0xFF00E676)
        else -> LabAccentBlue
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF131A26))
            .border(1.dp, Color(0xFF1E2A3A), RoundedCornerShape(8.dp))
            .clickable { onAdd() }
            .padding(10.dp)
            .testTag("catalog_item_${definition.type}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type Icon Box
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = definition.type.take(3),
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = definition.name,
                            color = LabTextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (definition.booleanFormula.isNotBlank()) {
                            Text(
                                text = definition.booleanFormula,
                                color = LabAccentBlue,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = definition.description,
                        color = LabTextSecondary,
                        fontSize = 9.5.sp,
                        maxLines = 2
                    )
                }
            }

            // Quick Add Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color(0xFF1E2F45))
                    .border(1.dp, LabAccentBlue.copy(alpha = 0.5f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = LabAccentBlue,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Add",
                        color = LabTextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
