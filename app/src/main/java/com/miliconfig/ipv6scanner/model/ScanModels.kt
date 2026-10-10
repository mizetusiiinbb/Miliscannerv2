package com.miliconfig.ipv6scanner.model

enum class PortStatus {
    OPEN,
    REFUSED,
    TIMEOUT,
    UNREACHABLE,
    UNKNOWN
}

data class ScanTarget(
    val host: String,
    val port: Int,
    val label: String? = null
) {
    val formattedAddress: String
        get() = if (host.contains(":") && !host.startsWith("[")) "[$host]:$port" else "$host:$port"
}

data class ScanResult(
    val target: ScanTarget,
    val status: PortStatus,
    val latencyMs: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val detailMessage: String? = null
)

data class ScanConfig(
    val timeoutMs: Int = 1500,
    val concurrency: Int = 20,
    val ports: List<Int> = listOf(80, 443, 8080, 8443)
)

data class ScanProgress(
    val scannedCount: Int = 0,
    val totalCount: Int = 0,
    val openCount: Int = 0,
    val isRunning: Boolean = false,
    val currentTarget: String? = null,
    val elapsedTimeMs: Long = 0L
) {
    val percentage: Int
        get() = if (totalCount > 0) ((scannedCount.toFloat() / totalCount) * 100).toInt() else 0
}

data class NetworkDiagnosticsInfo(
    val hasIpv6: Boolean = false,
    val localAddresses: List<String> = emptyList(),
    val globalAddresses: List<String> = emptyList(),
    val activeInterfaces: List<String> = emptyList(),
    val isInternetReachable: Boolean? = null,
    val probeLatencyMs: Long? = null,
    val lastCheckedTimestamp: Long = 0L
)
