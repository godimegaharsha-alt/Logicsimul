package com.example.logiclab.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.logiclab.audio.AudioEngine
import com.example.ui.theme.AppTheme
import com.example.ui.theme.getLogicLabColors

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onSelectTheme: (AppTheme) -> Unit,
    onDismiss: () -> Unit
) {
    val activeColors = getLogicLabColors(currentTheme)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 295.dp)
                .fillMaxWidth(0.78f)
                .heightIn(max = 360.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.2.dp, activeColors.accentPrimary.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                .testTag("theme_selection_dialog"),
            color = activeColors.panelBg,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(activeColors.accentPrimary.copy(alpha = 0.2f))
                                .border(1.dp, activeColors.accentPrimary.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes",
                                tint = activeColors.accentPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Workspace Themes",
                                color = activeColors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personalize your CAD lab aesthetics",
                                color = activeColors.textSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("close_theme_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = activeColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Theme Options List
                AppTheme.entries.forEach { theme ->
                    val isSelected = theme == currentTheme
                    val themePalette = getLogicLabColors(theme)

                    val cardBorderColor by animateColorAsState(
                        targetValue = if (isSelected) theme.primaryColor else activeColors.border,
                        animationSpec = tween(200),
                        label = "cardBorderColor"
                    )
                    val cardBgColor by animateColorAsState(
                        targetValue = if (isSelected) themePalette.cardBg else activeColors.cardBg.copy(alpha = 0.6f),
                        animationSpec = tween(200),
                        label = "cardBgColor"
                    )

                    Surface(
                        color = cardBgColor,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            cardBorderColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                AudioEngine.playClickSound()
                                onSelectTheme(theme)
                            }
                            .testTag("theme_card_${theme.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Selection indicator circle
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) theme.primaryColor else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) theme.primaryColor else activeColors.textTertiary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = themePalette.onAccent,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = theme.title,
                                            color = if (isSelected) activeColors.textPrimary else activeColors.textSecondary,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(if (theme.isDark) Color(0x33000000) else Color(0x33FFFFFF))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (theme.isDark) "DARK" else "LIGHT",
                                                color = activeColors.textTertiary,
                                                fontSize = 7.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Text(
                                        text = theme.subtitle,
                                        color = activeColors.textTertiary,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            // 4 Color Swatches Preview
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorSwatch(themePalette.canvasBg, "Canvas")
                                ColorSwatch(themePalette.panelBg, "Panel")
                                ColorSwatch(theme.primaryColor, "Primary")
                                ColorSwatch(themePalette.signalHigh, "Signal")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Done Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("apply_theme_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = activeColors.accentPrimary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Done",
                        color = activeColors.onAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, label: String) {
    Box(
        modifier = Modifier
            .size(13.dp)
            .clip(CircleShape)
            .background(color)
            .border(0.6.dp, Color.White.copy(alpha = 0.3f), CircleShape)
    )
}
