package com.miliconfig.ipv6scanner.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miliconfig.ipv6scanner.data.ScanHistoryRepository
import com.miliconfig.ipv6scanner.data.ScanSessionEntity
import com.miliconfig.ipv6scanner.model.NetworkDiagnosticsInfo
import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.model.ScanConfig
import com.miliconfig.ipv6scanner.model.ScanProgress
import com.miliconfig.ipv6scanner.model.ScanResult
import com.miliconfig.ipv6scanner.model.ScanTarget
import com.miliconfig.ipv6scanner.scanner.DiagnosticsEngine
import com.miliconfig.ipv6scanner.scanner.IPv6Utils
import com.miliconfig.ipv6scanner.scanner.TcpSocketScanner
import com.miliconfig.ipv6scanner.util.ExportHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TargetMode {
    SINGLE, CIDR, BATCH
}

enum class PortPreset {
    WEB, REMOTE, DNS, MAIL, VPN, CUSTOM
}

enum class ExportFormat {
    CSV, JSON, TXT
}

class ScannerViewModel(
    private val historyRepository: ScanHistoryRepository
) : ViewModel() {

    private val scanner = TcpSocketScanner()
    private val diagnosticsEngine = DiagnosticsEngine()
    private var scanJob: Job? = null

    // Target inputs
    val targetMode = MutableStateFlow(TargetMode.SINGLE)
    val singleHostInput = MutableStateFlow("2001:4860:4860::8888")
    val cidrInput = MutableStateFlow("2001:db8::/124")
    val batchInput = MutableStateFlow("")

    // Ports
    val portPreset = MutableStateFlow(PortPreset.WEB)
    val customPortsInput = MutableStateFlow("80, 443, 8080, 8443")

    // Tuning
    val timeoutMs = MutableStateFlow(1500)
    val concurrency = MutableStateFlow(20)

    // Scan State
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    private val _scanResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val scanResults: StateFlow<List<ScanResult>> = _scanResults.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Diagnostics State
    private val _diagnosticsInfo = MutableStateFlow(NetworkDiagnosticsInfo())
    val diagnosticsInfo: StateFlow<NetworkDiagnosticsInfo> = _diagnosticsInfo.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    // History
    val historySessions = historyRepository.allSessions
    private val _selectedSessionResults = MutableStateFlow<List<ScanResult>>(emptyList())
    val selectedSessionResults: StateFlow<List<ScanResult>> = _selectedSessionResults.asStateFlow()

    init {
        runDiagnostics()
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun getResolvedPorts(): Result<List<Int>> {
        return when (portPreset.value) {
            PortPreset.WEB -> Result.success(listOf(80, 443, 8080, 8443))
            PortPreset.REMOTE -> Result.success(listOf(22, 2222, 3389))
            PortPreset.DNS -> Result.success(listOf(53, 853))
            PortPreset.MAIL -> Result.success(listOf(25, 465, 587, 993, 995))
            PortPreset.VPN -> Result.success(listOf(443, 1194, 51820))
            PortPreset.CUSTOM -> IPv6Utils.parsePorts(customPortsInput.value)
        }
    }

    fun startScan() {
        if (_scanProgress.value.isRunning) return
        _errorMessage.value = null

        val portsResult = getResolvedPorts()
        if (portsResult.isFailure) {
            _errorMessage.value = portsResult.exceptionOrNull()?.message ?: "Invalid ports"
            return
        }
        val ports = portsResult.getOrThrow()

        val targets = mutableListOf<ScanTarget>()
        var targetSummary = ""

        when (targetMode.value) {
            TargetMode.SINGLE -> {
                val host = IPv6Utils.cleanAddress(singleHostInput.value)
                if (!IPv6Utils.isValidIPv6(host)) {
                    _errorMessage.value = "Invalid IPv6 address format: $host"
                    return
                }
                targetSummary = host
                for (p in ports) {
                    targets.add(ScanTarget(host, p))
                }
            }
            TargetMode.CIDR -> {
                val cidrExp = IPv6Utils.expandCidr(cidrInput.value)
                if (cidrExp.isFailure) {
                    _errorMessage.value = cidrExp.exceptionOrNull()?.message ?: "CIDR error"
                    return
                }
                val ips = cidrExp.getOrThrow()
                targetSummary = cidrInput.value.trim()
                for (ip in ips) {
                    for (p in ports) {
                        targets.add(ScanTarget(ip, p))
                    }
                }
            }
            TargetMode.BATCH -> {
                val parsed = IPv6Utils.parseBatchTargets(batchInput.value, ports)
                if (parsed.isEmpty()) {
                    _errorMessage.value = "No valid targets found in list"
                    return
                }
                targetSummary = "Batch (${parsed.size} probes)"
                targets.addAll(parsed)
            }
        }

        if (targets.isEmpty()) {
            _errorMessage.value = "Target list is empty"
            return
        }

        _scanResults.value = emptyList()
        val config = ScanConfig(
            timeoutMs = timeoutMs.value,
            concurrency = concurrency.value,
            ports = ports
        )

        scanJob = viewModelScope.launch {
            val liveList = mutableListOf<ScanResult>()
            val scanStartTime = System.currentTimeMillis()

            try {
                scanner.scanTargetsFlow(targets, config) { progress ->
                    _scanProgress.value = progress
                }.collect { result ->
                    liveList.add(result)
                    _scanResults.value = liveList.toList()
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Cancelled
            } finally {
                val duration = System.currentTimeMillis() - scanStartTime
                _scanProgress.update {
                    it.copy(isRunning = false, elapsedTimeMs = duration)
                }
                if (liveList.isNotEmpty()) {
                    historyRepository.saveScanSession(
                        targetSummary = targetSummary,
                        results = liveList,
                        durationMs = duration
                    )
                }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _scanProgress.update { it.copy(isRunning = false) }
    }

    fun clearResults() {
        if (!_scanProgress.value.isRunning) {
            _scanResults.value = emptyList()
            _scanProgress.value = ScanProgress()
        }
    }

    fun runDiagnostics() {
        if (_isDiagnosing.value) return
        _isDiagnosing.value = true
        viewModelScope.launch {
            try {
                _diagnosticsInfo.value = diagnosticsEngine.runDiagnostics()
            } finally {
                _isDiagnosing.value = false
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            historyRepository.deleteSession(sessionId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
        }
    }

    fun loadSessionResults(sessionId: Long) {
        viewModelScope.launch {
            _selectedSessionResults.value = historyRepository.getResultsForSession(sessionId)
        }
    }

    fun exportCurrentResults(context: Context, format: ExportFormat) {
        val results = _scanResults.value
        if (results.isEmpty()) return

        val duration = _scanProgress.value.elapsedTimeMs
        val targetSummary = when (targetMode.value) {
            TargetMode.SINGLE -> singleHostInput.value
            TargetMode.CIDR -> cidrInput.value
            TargetMode.BATCH -> "Batch Export"
        }

        val content = when (format) {
            ExportFormat.CSV -> ExportHelper.toCsv(results)
            ExportFormat.JSON -> ExportHelper.toJson(results)
            ExportFormat.TXT -> ExportHelper.toTxtReport(targetSummary, results, duration)
        }

        val title = "Miliconfig_IPv6_Scan_${System.currentTimeMillis()}"
        ExportHelper.shareContent(context, title, content)
    }
}
