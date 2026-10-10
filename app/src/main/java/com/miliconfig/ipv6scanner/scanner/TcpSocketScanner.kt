package com.miliconfig.ipv6scanner.scanner

import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.model.ScanConfig
import com.miliconfig.ipv6scanner.model.ScanProgress
import com.miliconfig.ipv6scanner.model.ScanResult
import com.miliconfig.ipv6scanner.model.ScanTarget
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NoRouteToHostException
import java.net.PortUnreachableException
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.atomic.AtomicInteger

class TcpSocketScanner {

    suspend fun checkTarget(target: ScanTarget, timeoutMs: Int): ScanResult = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        val startTime = System.currentTimeMillis()
        var socket: Socket? = null

        try {
            val address = InetAddress.getByName(target.host)
            val socketAddress = InetSocketAddress(address, target.port)

            socket = Socket()
            // Do not use linger to avoid hanging close
            socket.soTimeout = timeoutMs
            socket.connect(socketAddress, timeoutMs)

            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.OPEN,
                latencyMs = latency,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: SocketTimeoutException) {
            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.TIMEOUT,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = "Connection timed out after ${timeoutMs}ms"
            )
        } catch (e: ConnectException) {
            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.REFUSED,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = e.message ?: "Connection refused by target"
            )
        } catch (e: NoRouteToHostException) {
            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.UNREACHABLE,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = "No route to host"
            )
        } catch (e: PortUnreachableException) {
            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.UNREACHABLE,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = "Port unreachable"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            val latency = System.currentTimeMillis() - startTime
            val msg = e.message?.lowercase() ?: ""
            val status = when {
                msg.contains("unreachable") || msg.contains("network is down") || msg.contains("no route") -> PortStatus.UNREACHABLE
                msg.contains("refused") || msg.contains("reset") -> PortStatus.REFUSED
                msg.contains("time") -> PortStatus.TIMEOUT
                else -> PortStatus.UNKNOWN
            }
            ScanResult(
                target = target,
                status = status,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = e.message
            )
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ScanResult(
                target = target,
                status = PortStatus.UNKNOWN,
                latencyMs = latency,
                timestamp = System.currentTimeMillis(),
                detailMessage = e.message
            )
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }

    fun scanTargetsFlow(
        targets: List<ScanTarget>,
        config: ScanConfig,
        onProgressUpdate: (ScanProgress) -> Unit
    ): Flow<ScanResult> = channelFlow {
        val totalCount = targets.size
        val scannedCount = AtomicInteger(0)
        val openCount = AtomicInteger(0)
        val startTime = System.currentTimeMillis()

        onProgressUpdate(
            ScanProgress(
                scannedCount = 0,
                totalCount = totalCount,
                openCount = 0,
                isRunning = true,
                elapsedTimeMs = 0L
            )
        )

        val semaphore = Semaphore(config.concurrency.coerceIn(1, 100))

        for (target in targets) {
            currentCoroutineContext().ensureActive()
            launch {
                semaphore.withPermit {
                    currentCoroutineContext().ensureActive()
                    val result = checkTarget(target, config.timeoutMs)
                    val currentScanned = scannedCount.incrementAndGet()
                    if (result.status == PortStatus.OPEN) {
                        openCount.incrementAndGet()
                    }

                    send(result)

                    onProgressUpdate(
                        ScanProgress(
                            scannedCount = currentScanned,
                            totalCount = totalCount,
                            openCount = openCount.get(),
                            isRunning = currentScanned < totalCount,
                            currentTarget = target.formattedAddress,
                            elapsedTimeMs = System.currentTimeMillis() - startTime
                        )
                    )
                }
            }
        }
    }
}
