package com.miliconfig.ipv6scanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miliconfig.ipv6scanner.R
import com.miliconfig.ipv6scanner.ui.ScannerViewModel
import com.miliconfig.ipv6scanner.ui.components.AppHeader
import com.miliconfig.ipv6scanner.ui.components.GlassCard
import com.miliconfig.ipv6scanner.ui.theme.BgDark
import com.miliconfig.ipv6scanner.ui.theme.BrandGreenPrimary
import com.miliconfig.ipv6scanner.ui.theme.GlassBorderSubtle
import com.miliconfig.ipv6scanner.ui.theme.StatusOpen
import com.miliconfig.ipv6scanner.ui.theme.StatusRefused
import com.miliconfig.ipv6scanner.ui.theme.TextPrimary
import com.miliconfig.ipv6scanner.ui.theme.TextSecondary

@Composable
fun DiagnosticsScreen(viewModel: ScannerViewModel) {
    val diagInfo by viewModel.diagnosticsInfo.collectAsState()
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppHeader()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.diag_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Button(
                    onClick = { viewModel.runDiagnostics() },
                    enabled = !isDiagnosing,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isDiagnosing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BgDark,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = BgDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.diag_btn_test), color = BgDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Internet Reachability Probe Card
        item {
            val isReachable = diagInfo.isInternetReachable == true
            val borderColor = if (isReachable) StatusOpen.copy(alpha = 0.5f) else StatusRefused.copy(alpha = 0.5f)

            GlassCard(borderColor = borderColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isReachable) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isReachable) StatusOpen else StatusRefused,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.diag_connectivity_test),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isReachable) {
                                stringResource(R.string.diag_reachable, diagInfo.probeLatencyMs ?: 0)
                            } else {
                                stringResource(R.string.diag_unreachable)
                            },
                            color = if (isReachable) StatusOpen else StatusRefused,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Global IPv6 Unicast Card
        item {
            GlassCard {
                Column {
                    Text(
                        text = stringResource(R.string.diag_global_ipv6),
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (diagInfo.globalAddresses.isEmpty()) {
                        Text(
                            text = "No Global Unicast IPv6 assigned by carrier or Wi-Fi.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        diagInfo.globalAddresses.forEach { addr ->
                            Text(
                                text = addr,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Local IPv6 Link-Local Card
        item {
            GlassCard {
                Column {
                    Text(
                        text = stringResource(R.string.diag_local_ipv6),
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (diagInfo.localAddresses.isEmpty()) {
                        Text(
                            text = stringResource(R.string.diag_no_ipv6),
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        diagInfo.localAddresses.forEach { addr ->
                            Text(
                                text = addr,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Interfaces Card
        item {
            GlassCard {
                Column {
                    Text(
                        text = stringResource(R.string.diag_interfaces),
                        fontWeight = FontWeight.Bold,
                        color = BrandGreenPrimary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (diagInfo.activeInterfaces.isEmpty()) {
                        Text(
                            text = "No network interfaces currently active.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        diagInfo.activeInterfaces.forEach { iface ->
                            Text(
                                text = "• $iface",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
