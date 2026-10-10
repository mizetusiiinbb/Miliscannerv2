package com.miliconfig.ipv6scanner.scanner

import com.miliconfig.ipv6scanner.model.NetworkDiagnosticsInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket

class DiagnosticsEngine {

    suspend fun runDiagnostics(): NetworkDiagnosticsInfo = withContext(Dispatchers.IO) {
        val localAddresses = mutableListOf<String>()
        val globalAddresses = mutableListOf<String>()
        val activeInterfaces = mutableListOf<String>()

        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue

                val addrs = iface.inetAddresses
                var hasIpv6OnIface = false
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (addr is Inet6Address) {
                        hasIpv6OnIface = true
                        val host = IPv6Utils.cleanAddress(addr.hostAddress ?: "")
                        if (addr.isLinkLocalAddress) {
                            localAddresses.add("${iface.name}: $host (Link-Local)")
                        } else if (!addr.isLoopbackAddress && !addr.isSiteLocalAddress) {
                            globalAddresses.add("${iface.name}: $host")
                        } else {
                            localAddresses.add("${iface.name}: $host")
                        }
                    }
                }
                if (hasIpv6OnIface) {
                    activeInterfaces.add("${iface.name} (${iface.displayName})")
                }
            }
        } catch (_: Exception) {
        }

        val hasIpv6 = globalAddresses.isNotEmpty() || localAddresses.isNotEmpty()

        // Test internet reachability to well-known public IPv6 targets
        val probeTargets = listOf(
            "2606:4700:4700::1111" to 53, // Cloudflare IPv6 DNS
            "2001:4860:4860::8888" to 53, // Google IPv6 DNS
            "2606:4700:4700::1001" to 443 // Cloudflare IPv6 HTTPS
        )

        var reachable = false
        var lowestLatency: Long? = null

        for ((targetHost, port) in probeTargets) {
            val latency = probeTcp(targetHost, port, 2500)
            if (latency != null) {
                reachable = true
                lowestLatency = if (lowestLatency == null) latency else minOf(lowestLatency, latency)
                break
            }
        }

        NetworkDiagnosticsInfo(
            hasIpv6 = hasIpv6,
            localAddresses = localAddresses,
            globalAddresses = globalAddresses,
            activeInterfaces = activeInterfaces,
            isInternetReachable = reachable,
            probeLatencyMs = lowestLatency,
            lastCheckedTimestamp = System.currentTimeMillis()
        )
    }

    private fun probeTcp(host: String, port: Int, timeoutMs: Int): Long? {
        val start = System.currentTimeMillis()
        var socket: Socket? = null
        return try {
            socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeoutMs)
            val elapsed = System.currentTimeMillis() - start
            elapsed
        } catch (_: Exception) {
            null
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }
}
