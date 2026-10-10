package com.miliconfig.ipv6scanner.data

import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.model.ScanResult
import com.miliconfig.ipv6scanner.model.ScanTarget
import kotlinx.coroutines.flow.Flow

class ScanHistoryRepository(private val dao: ScanHistoryDao) {

    val allSessions: Flow<List<ScanSessionEntity>> = dao.getAllSessionsFlow()

    suspend fun saveScanSession(
        targetSummary: String,
        results: List<ScanResult>,
        durationMs: Long
    ): Long {
        val openCount = results.count { it.status == PortStatus.OPEN }
        val session = ScanSessionEntity(
            timestamp = System.currentTimeMillis(),
            targetSummary = targetSummary,
            totalTargets = results.size,
            openCount = openCount,
            durationMs = durationMs
        )
        val sessionId = dao.insertSession(session)
        val resultEntities = results.map { r ->
            ScanResultEntity(
                sessionId = sessionId,
                host = r.target.host,
                port = r.target.port,
                status = r.status.name,
                latencyMs = r.latencyMs,
                detailMessage = r.detailMessage,
                timestamp = r.timestamp
            )
        }
        dao.insertResults(resultEntities)
        return sessionId
    }

    suspend fun getResultsForSession(sessionId: Long): List<ScanResult> {
        val entities = dao.getResultsForSession(sessionId)
        return entities.map { e ->
            val status = try {
                PortStatus.valueOf(e.status)
            } catch (_: Exception) {
                PortStatus.UNKNOWN
            }
            ScanResult(
                target = ScanTarget(e.host, e.port),
                status = status,
                latencyMs = e.latencyMs,
                timestamp = e.timestamp,
                detailMessage = e.detailMessage
            )
        }
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }

    suspend fun clearAll() {
        dao.clearAllSessions()
    }
}
