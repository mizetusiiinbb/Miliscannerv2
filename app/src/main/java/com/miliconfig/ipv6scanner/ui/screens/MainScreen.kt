package com.miliconfig.ipv6scanner.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.miliconfig.ipv6scanner.R
import com.miliconfig.ipv6scanner.ui.ScannerViewModel
import com.miliconfig.ipv6scanner.ui.theme.BgDark
import com.miliconfig.ipv6scanner.ui.theme.BgSurfaceDark
import com.miliconfig.ipv6scanner.ui.theme.BrandGreenPrimary
import com.miliconfig.ipv6scanner.ui.theme.GlassBorderSubtle
import com.miliconfig.ipv6scanner.ui.theme.TextPrimary
import com.miliconfig.ipv6scanner.ui.theme.TextSecondary

enum class AppTab {
    SCANNER, DIAGNOSTICS, HISTORY, ABOUT
}

@Composable
fun MainScreen(viewModel: ScannerViewModel) {
    var currentTab by remember { mutableStateOf(AppTab.SCANNER) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            NavigationBar(
                containerColor = BgSurfaceDark,
                tonalElevation = androidx.compose.ui.unit.dp.times(4)
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.SCANNER,
                    onClick = { currentTab = AppTab.SCANNER },
                    icon = { Icon(Icons.Default.Radar, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_scanner), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BgDark,
                        selectedTextColor = BrandGreenPrimary,
                        indicatorColor = BrandGreenPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.DIAGNOSTICS,
                    onClick = { currentTab = AppTab.DIAGNOSTICS },
                    icon = { Icon(Icons.Default.NetworkCheck, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_diagnostics), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BgDark,
                        selectedTextColor = BrandGreenPrimary,
                        indicatorColor = BrandGreenPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.HISTORY,
                    onClick = { currentTab = AppTab.HISTORY },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_history), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BgDark,
                        selectedTextColor = BrandGreenPrimary,
                        indicatorColor = BrandGreenPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.ABOUT,
                    onClick = { currentTab = AppTab.ABOUT },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_about), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = BgDark,
                        selectedTextColor = BrandGreenPrimary,
                        indicatorColor = BrandGreenPrimary,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.SCANNER -> ScannerScreen(viewModel = viewModel)
                AppTab.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                AppTab.HISTORY -> HistoryScreen(viewModel = viewModel)
                AppTab.ABOUT -> AboutScreen()
            }
        }
    }
}
