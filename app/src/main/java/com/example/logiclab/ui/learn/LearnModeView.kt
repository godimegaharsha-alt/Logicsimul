package com.example.logiclab.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.logiclab.learn.LearnTopic
import com.example.logiclab.learn.LearnTopics
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
fun LearnModeView(
    onOpenStarterCircuit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTopic by remember { mutableStateOf(LearnTopics.topics.first()) }
    var showAnswer by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LabDarkBg)
            .testTag("learn_mode_view")
    ) {
        // Top selector bar of topics
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(LabPanelBg)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(LearnTopics.topics) { topic ->
                val isSelected = topic.id == selectedTopic.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTopic = topic
                        showAnswer = false
                    },
                    label = { Text(topic.title, fontSize = 12.sp) },
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

        // Main content area
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = selectedTopic.title.uppercase(),
                        color = LabTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Digital Electronics Curriculum",
                        color = LabAccentBlue,
                        fontSize = 11.5.sp
                    )
                }

                if (selectedTopic.starterCircuitId != null) {
                    Button(
                        onClick = { onOpenStarterCircuit(selectedTopic.starterCircuitId!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = LabAccentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("open_topic_sim_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Simulate",
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Concept Card
            SectionCard(title = "1. CONCEPT & FUNDAMENTALS") {
                Text(
                    text = selectedTopic.concept,
                    color = LabTextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Symbol & I/O
            SectionCard(title = "2. SYMBOL & TERMINALS") {
                Text(
                    text = "Symbol:",
                    color = LabTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedTopic.symbolDescription,
                    color = LabTextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Port Specifications:",
                    color = LabTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedTopic.inputsOutputs,
                    color = LabTextPrimary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Boolean Expressions
            SectionCard(title = "3. BOOLEAN LOGIC EQUATIONS") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LabDarkBg, RoundedCornerShape(4.dp))
                        .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = selectedTopic.booleanExpression,
                        color = SignalHigh,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Truth Table Summary
            SectionCard(title = "4. TRUTH TABLE SUMMARY") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LabDarkBg, RoundedCornerShape(4.dp))
                        .border(1.dp, LabBorder, RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = selectedTopic.truthTableSummary,
                        color = LabTextPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Practical Applications
            SectionCard(title = "5. INDUSTRIAL / HARDWARE APPLICATIONS") {
                Text(
                    text = selectedTopic.practicalApplication,
                    color = LabTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Practice Question
            SectionCard(title = "6. LABORATORY PRACTICE QUESTION") {
                Text(
                    text = selectedTopic.practiceQuestion,
                    color = LabTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { showAnswer = !showAnswer },
                    border = androidx.compose.foundation.BorderStroke(1.dp, LabBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LabAccentBlue)
                ) {
                    Icon(
                        imageVector = if (showAnswer) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showAnswer) "Hide Solution" else "Reveal Solution", fontSize = 11.sp)
                }

                if (showAnswer) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x1F388BFD), RoundedCornerShape(4.dp))
                            .border(1.dp, LabAccentBlue.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = selectedTopic.practiceAnswer,
                            color = LabTextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(LabPanelBg)
            .border(1.dp, LabBorder, RoundedCornerShape(6.dp))
            .padding(14.dp)
    ) {
        Text(
            text = title,
            color = LabAccentBlue,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}
