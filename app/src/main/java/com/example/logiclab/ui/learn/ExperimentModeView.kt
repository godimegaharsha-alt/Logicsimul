package com.example.logiclab.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.learn.LabExperiment
import com.example.logiclab.learn.LabExperiments
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabDarkBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalHigh

@Composable
fun ExperimentModeView(
    onLoadExperimentInSimulator: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedExp by remember { mutableStateOf(LabExperiments.list.first()) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LabDarkBg)
            .testTag("experiment_mode_view")
    ) {
        // Experiment selector pills
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(LabPanelBg)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LabExperiments.list) { exp ->
                val isSelected = exp.id == selectedExp.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedExp = exp },
                    label = { Text(exp.title.split(":").first(), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LabAccentBlue,
                        selectedLabelColor = Color.White,
                        containerColor = LabCardBg,
                        labelColor = LabTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = LabBorder,
                        selectedBorderColor = LabAccentBlue,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        // Experiment details
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedExp.title,
                        color = LabTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Laboratory Practical Guide",
                        color = LabAccentBlue,
                        fontSize = 12.sp
                    )
                }

                if (selectedExp.starterCircuitId != null) {
                    Button(
                        onClick = { onLoadExperimentInSimulator(selectedExp.starterCircuitId!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = LabAccentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("load_experiment_button")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Load in Lab",
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Objective
            SectionCard(title = "OBJECTIVE") {
                Text(
                    text = selectedExp.objective,
                    color = LabTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Components Required
            SectionCard(title = "COMPONENTS & APPARATUS REQUIRED") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    selectedExp.componentsRequired.forEach { comp ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("•", color = LabAccentBlue, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(comp, color = LabTextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Procedure
            SectionCard(title = "STEP-BY-STEP PROCEDURE") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedExp.procedure.forEach { step ->
                        Text(
                            text = step,
                            color = LabTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Expected Result
            SectionCard(title = "THEORETICAL EXPECTED RESULT") {
                Text(
                    text = selectedExp.expectedResult,
                    color = LabTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Observation Table
            SectionCard(title = "OBSERVATION & VERIFICATION TABLE") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LabDarkBg, RoundedCornerShape(4.dp))
                        .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LabPanelBg)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("INPUT CONDITIONS", color = LabTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("EXPECTED OUTPUT", color = LabAccentBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = LabBorder, thickness = 1.dp)

                    selectedExp.observationTable.forEachIndexed { idx, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(row.inputValues, color = LabTextPrimary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(row.expectedOutput, color = SignalHigh, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        if (idx < selectedExp.observationTable.lastIndex) {
                            HorizontalDivider(color = LabBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conclusion
            SectionCard(title = "CONCLUSION") {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SignalHigh,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedExp.conclusion,
                        color = LabTextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
