package com.miliconfig.ipv6scanner

import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.model.ScanResult
import com.miliconfig.ipv6scanner.model.ScanTarget
import com.miliconfig.ipv6scanner.util.ExportHelper
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportHelperTest {

    private val sampleResults = listOf(
        ScanResult(
            target = ScanTarget("2001:4860:4860::8888", 53),
            status = PortStatus.OPEN,
            latencyMs = 18L,
            timestamp = 1700000000000L
        ),
        ScanResult(
            target = ScanTarget("2001:4860:4860::8888", 80),
            status = PortStatus.REFUSED,
            latencyMs = 12L,
            timestamp = 1700000000000L,
            detailMessage = "Connection refused"
        )
    )

    @Test
    fun testCsvGeneration() {
        val csv = ExportHelper.toCsv(sampleResults)
        assertTrue(csv.startsWith("Target,Port,Status,LatencyMs,Detail,Timestamp"))
        assertTrue(csv.contains("2001:4860:4860::8888,53,OPEN,18"))
        assertTrue(csv.contains("2001:4860:4860::8888,80,REFUSED,12"))
    }

    @Test
    fun testJsonGeneration() {
        val json = ExportHelper.toJson(sampleResults)
        assertTrue(json.startsWith("[\n"))
        assertTrue(json.contains("\"host\": \"2001:4860:4860::8888\""))
        assertTrue(json.contains("\"port\": 53"))
        assertTrue(json.contains("\"status\": \"OPEN\""))
        assertTrue(json.endsWith("]\n"))
    }

    @Test
    fun testTxtReportGeneration() {
        val report = ExportHelper.toTxtReport("2001:4860:4860::8888", sampleResults, 30L)
        assertTrue(report.contains("MILICONFIG IPV6 SCANNER PRO"))
        assertTrue(report.contains("Milad Jahani"))
        assertTrue(report.contains("@miliconfig"))
        assertTrue(report.contains("OPEN PORTS BREAKDOWN"))
        assertTrue(report.contains("53"))
    }
}
