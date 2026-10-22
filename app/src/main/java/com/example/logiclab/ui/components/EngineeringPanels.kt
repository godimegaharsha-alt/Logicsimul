package com.example.logiclab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logiclab.engine.TruthTableData
import com.example.logiclab.model.CircuitDiagnostic
import com.example.logiclab.model.DiagnosticSeverity
import com.example.logiclab.model.DigitalState
import com.example.ui.theme.LabAccentBlue
import com.example.ui.theme.LabBorder
import com.example.ui.theme.LabCardBg
import com.example.ui.theme.LabPanelBg
import com.example.ui.theme.LabTextPrimary
import com.example.ui.theme.LabTextSecondary
import com.example.ui.theme.LabTextTertiary
import com.example.ui.theme.SignalError
import com.example.ui.theme.SignalHigh
import com.example.ui.theme.SignalLow
import com.example.ui.theme.SignalUnknown



@Composable
fun DiagnosticsPanel(
    diagnostics: List<CircuitDiagnostic>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LabPanelBg)
            .padding(12.dp)
            .testTag("diagnostics_panel")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CIRCUIT DIAGNOSTICS & VERIFICATION",
                color = LabTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            val errCount = diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
            val warnCount = diagnostics.count { it.severity == DiagnosticSeverity.WARNING }
            Text(
                text = "$errCount errors | $warnCount warnings",
                color = if (errCount > 0) SignalError else if (warnCount > 0) SignalUnknown else SignalHigh,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (diagnostics.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1F3FB950), RoundedCornerShape(4.dp))
                    .border(1.dp, Color(0x443FB950), RoundedCornerShape(4.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SignalHigh,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Circuit is healthy. No shorts, floaters, or race conditions detected.",
                    color = SignalHigh,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(diagnostics) { diag ->
                    val (icon, color, bg) = when (diag.severity) {
                        DiagnosticSeverity.ERROR -> Triple(Icons.Default.Warning, SignalError, Color(0x22F85149))
                        DiagnosticSeverity.WARNING -> Triple(Icons.Default.Warning, SignalUnknown, Color(0x22D29922))
                        DiagnosticSeverity.INFO -> Triple(Icons.Default.Info, LabAccentBlue, Color(0x22388BFD))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bg, RoundedCornerShape(4.dp))
                            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = diag.message,
                            color = LabTextPrimary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
