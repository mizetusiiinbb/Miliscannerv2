package com.miliconfig.ipv6scanner.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miliconfig.ipv6scanner.R
import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.ui.ExportFormat
import com.miliconfig.ipv6scanner.ui.PortPreset
import com.miliconfig.ipv6scanner.ui.ScannerViewModel
import com.miliconfig.ipv6scanner.ui.TargetMode
import com.miliconfig.ipv6scanner.ui.components.AppHeader
import com.miliconfig.ipv6scanner.ui.components.GlassCard
import com.miliconfig.ipv6scanner.ui.components.StatusBadge
import com.miliconfig.ipv6scanner.ui.theme.BgDark
import com.miliconfig.ipv6scanner.ui.theme.BgSurfaceDark
import com.miliconfig.ipv6scanner.ui.theme.BrandGreenDark
import com.miliconfig.ipv6scanner.ui.theme.BrandGreenGlow
import com.miliconfig.ipv6scanner.ui.theme.BrandGreenPrimary
import com.miliconfig.ipv6scanner.ui.theme.GlassBorder
import com.miliconfig.ipv6scanner.ui.theme.GlassBorderSubtle
import com.miliconfig.ipv6scanner.ui.theme.StatusOpen
import com.miliconfig.ipv6scanner.ui.theme.StatusRefused
import com.miliconfig.ipv6scanner.ui.theme.TextPrimary
import com.miliconfig.ipv6scanner.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScannerScreen(viewModel: ScannerViewModel) {
    val context = LocalContext.current
    val targetMode by viewModel.targetMode.collectAsState()
    val singleHost by viewModel.singleHostInput.collectAsState()
    val cidrInput by viewModel.cidrInput.collectAsState()
    val batchInput by viewModel.batchInput.collectAsState()

    val portPreset by viewModel.portPreset.collectAsState()
    val customPorts by viewModel.customPortsInput.collectAsState()

    val timeoutMs by viewModel.timeoutMs.collectAsState()
    val concurrency by viewModel.concurrency.collectAsState()

    val progress by viewModel.scanProgress.collectAsState()
    val results by viewModel.scanResults.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showTuning by remember { mutableStateOf(false) }

    // Error alert
    errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissError() },
            title = { Text("Scanner Notice", color = BrandGreenPrimary) },
            text = { Text(msg, color = TextPrimary) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissError() }) {
                    Text("OK", color = BrandGreenPrimary)
                }
            },
            containerColor = BgSurfaceDark
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(stringResource(R.string.export_dialog_title), color = BrandGreenPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            viewModel.exportCurrentResults(context, ExportFormat.CSV)
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_format_csv), color = TextPrimary)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.exportCurrentResults(context, ExportFormat.JSON)
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_format_json), color = TextPrimary)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.exportCurrentResults(context, ExportFormat.TXT)
                            showExportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_format_txt), color = TextPrimary)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = BgSurfaceDark
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppHeader()
        }

        // Target Mode Tabs
        item {
            GlassCard {
                Column {
                    TabRow(
                        selectedTabIndex = targetMode.ordinal,
                        containerColor = Color.Transparent,
                        contentColor = BrandGreenPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                Modifier.tabIndicatorOffset(tabPositions[targetMode.ordinal]),
                                color = BrandGreenPrimary,
                                height = 3.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = targetMode == TargetMode.SINGLE,
                            onClick = { viewModel.targetMode.value = TargetMode.SINGLE },
                            text = { Text(stringResource(R.string.target_mode_single), fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = targetMode == TargetMode.CIDR,
                            onClick = { viewModel.targetMode.value = TargetMode.CIDR },
                            text = { Text(stringResource(R.string.target_mode_cidr), fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = targetMode == TargetMode.BATCH,
                            onClick = { viewModel.targetMode.value = TargetMode.BATCH },
                            text = { Text(stringResource(R.string.target_mode_batch), fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    when (targetMode) {
                        TargetMode.SINGLE -> {
                            OutlinedTextField(
                                value = singleHost,
                                onValueChange = { viewModel.singleHostInput.value = it },
                                label = { Text(stringResource(R.string.input_ipv6_label)) },
                                placeholder = { Text(stringResource(R.string.input_ipv6_hint)) },
                                singleLine = true,
                                enabled = !progress.isRunning,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreenPrimary,
                                    unfocusedBorderColor = GlassBorderSubtle,
                                    focusedLabelColor = BrandGreenPrimary,
                                    cursorColor = BrandGreenPrimary
                                )
                            )
                        }
                        TargetMode.CIDR -> {
                            Column {
                                OutlinedTextField(
                                    value = cidrInput,
                                    onValueChange = { viewModel.cidrInput.value = it },
                                    label = { Text(stringResource(R.string.input_cidr_label)) },
                                    placeholder = { Text(stringResource(R.string.input_cidr_hint)) },
                                    singleLine = true,
                                    enabled = !progress.isRunning,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandGreenPrimary,
                                        unfocusedBorderColor = GlassBorderSubtle,
                                        focusedLabelColor = BrandGreenPrimary,
                                        cursorColor = BrandGreenPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Safeguard: Prefixes >= /120 allowed (max 256 hosts). Exhaustive /64 scanning disabled.",
                                    color = BrandGreenGlow,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        TargetMode.BATCH -> {
                            OutlinedTextField(
                                value = batchInput,
                                onValueChange = { viewModel.batchInput.value = it },
                                label = { Text(stringResource(R.string.input_batch_label)) },
                                placeholder = { Text(stringResource(R.string.input_batch_hint)) },
                                minLines = 3,
                                maxLines = 6,
                                enabled = !progress.isRunning,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreenPrimary,
                                    unfocusedBorderColor = GlassBorderSubtle,
                                    focusedLabelColor = BrandGreenPrimary,
                                    cursorColor = BrandGreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Port Presets
        item {
            GlassCard {
                Column {
                    Text(
                        text = stringResource(R.string.port_selection_label),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PortPreset.values().forEach { preset ->
                            val label = when (preset) {
                                PortPreset.WEB -> "Web (80, 443, 8080, 8443)"
                                PortPreset.REMOTE -> "Remote (22, 2222, 3389)"
                                PortPreset.DNS -> "DNS (53, 853)"
                                PortPreset.MAIL -> "Mail (25, 465, 587, 995)"
                                PortPreset.VPN -> "VPN (443, 1194, 51820)"
                                PortPreset.CUSTOM -> "Custom..."
                            }
                            FilterChip(
                                selected = portPreset == preset,
                                onClick = { if (!progress.isRunning) viewModel.portPreset.value = preset },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreenPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = BrandGreenPrimary
                                )
                            )
                        }
                    }

                    if (portPreset == PortPreset.CUSTOM) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = customPorts,
                            onValueChange = { viewModel.customPortsInput.value = it },
                            placeholder = { Text(stringResource(R.string.port_custom_hint)) },
                            singleLine = true,
                            enabled = !progress.isRunning,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreenPrimary,
                                unfocusedBorderColor = GlassBorderSubtle,
                                cursorColor = BrandGreenPrimary
                            )
                        )
                    }
                }
            }
        }

        // Engine Tuning Collapsible
        item {
            GlassCard {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTuning = !showTuning },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = BrandGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_title),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )
                        }
                        Text(
                            text = if (showTuning) "▲" else "▼",
                            color = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = showTuning) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Text(
                                text = "Socket Timeout: ${timeoutMs}ms",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Slider(
                                value = timeoutMs.toFloat(),
                                onValueChange = { viewModel.timeoutMs.value = it.toInt() },
                                valueRange = 200f..5000f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandGreenPrimary,
                                    activeTrackColor = BrandGreenPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Concurrency Limit: $concurrency parallel workers",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            Slider(
                                value = concurrency.toFloat(),
                                onValueChange = { viewModel.concurrency.value = it.toInt() },
                                valueRange = 5f..50f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = BrandGreenPrimary,
                                    activeTrackColor = BrandGreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Scan Action Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (progress.isRunning) {
                    Button(
                        onClick = { viewModel.stopScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRefused),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_stop_scan), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.startScan() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = BgDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.btn_start_scan),
                            fontWeight = FontWeight.Bold,
                            color = BgDark
                        )
                    }
                }

                if (results.isNotEmpty() && !progress.isRunning) {
                    OutlinedButton(
                        onClick = { showExportDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandGreenPrimary),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = BrandGreenPrimary)
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearResults() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = TextSecondary)
                    }
                }
            }
        }

        // Live Progress Card
        if (progress.isRunning || progress.totalCount > 0) {
            item {
                GlassCard(
                    borderColor = if (progress.isRunning) BrandGreenPrimary.copy(alpha = 0.5f) else GlassBorderSubtle
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scan Progress: ${progress.scannedCount}/${progress.totalCount} (${progress.percentage}%)",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .background(StatusOpen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${progress.openCount} Open",
                                    color = StatusOpen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { if (progress.totalCount > 0) progress.scannedCount.toFloat() / progress.totalCount else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = BrandGreenPrimary,
                            trackColor = BgDark
                        )

                        progress.currentTarget?.let { cur ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Probing: $cur",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Results Section Header
        if (results.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Results (${results.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "${results.count { it.status == PortStatus.OPEN }} Open",
                        color = StatusOpen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Results List
            items(results) { res ->
                GlassCard(
                    cornerRadius = 12.dp,
                    borderColor = if (res.status == PortStatus.OPEN) StatusOpen.copy(alpha = 0.4f) else GlassBorderSubtle
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = res.target.formattedAddress,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "RTT: ${res.latencyMs}ms" + if (res.detailMessage != null) " • ${res.detailMessage}" else "",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        StatusBadge(status = res.status)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
