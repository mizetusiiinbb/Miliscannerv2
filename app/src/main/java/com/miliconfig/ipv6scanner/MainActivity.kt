package com.miliconfig.ipv6scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miliconfig.ipv6scanner.data.AppDatabase
import com.miliconfig.ipv6scanner.data.ScanHistoryRepository
import com.miliconfig.ipv6scanner.ui.ScannerViewModel
import com.miliconfig.ipv6scanner.ui.screens.MainScreen
import com.miliconfig.ipv6scanner.ui.theme.MiliconfigTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScannerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = AppDatabase.getInstance(applicationContext)
                val repository = ScanHistoryRepository(db.scanHistoryDao())
                return ScannerViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiliconfigTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
