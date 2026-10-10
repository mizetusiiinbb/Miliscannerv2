package com.miliconfig.ipv6scanner.util

import android.content.Context
import android.content.Intent
import com.miliconfig.ipv6scanner.model.PortStatus
import com.miliconfig.ipv6scanner.model.ScanResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun toCsv(results: List<ScanResult>): String {
        val sb = StringBuilder()
        sb.append("Target,Port,Status,LatencyMs,Detail,Timestamp\n")
        for (r in results) {
            val hostEscaped = "\"${r.target.host.replace("\"", "\"\"")}\""
            val detailEscaped = "\"${(r.detailMessage ?: "").replace("\"", "\"\"")}\""
            val dateStr = dateFormat.format(Date(r.timestamp))
            sb.append("$hostEscaped,${r.target.port},${r.status.name},${r.latencyMs},$detailEscaped,\"$dateStr\"\n")
        }
        return sb.toString()
    }

    fun toJson(results: List<ScanResult>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        results.forEachIndexed { index, r ->
            sb.append("  {\n")
            sb.append("    \"host\": \"${escapeJson(r.target.host)}\",\n")
            sb.append("    \"port\": ${r.target.port},\n")
            sb.append("    \"status\": \"${r.status.name}\",\n")
            sb.append("    \"latencyMs\": ${r.latencyMs},\n")
            sb.append("    \"detail\": ${if (r.detailMessage != null) "\"${escapeJson(r.detailMessage)}\"" else "null"},\n")
            sb.append("    \"timestamp\": ${r.timestamp}\n")
            sb.append("  }${if (index < results.size - 1) "," else ""}\n")
        }
        sb.append("]\n")
        return sb.toString()
    }

    fun toTxtReport(targetSummary: String, results: List<ScanResult>, durationMs: Long): String {
        val sb = StringBuilder()
        val now = dateFormat.format(Date())
        val openResults = results.filter { it.status == PortStatus.OPEN }
        val refusedResults = results.filter { it.status == PortStatus.REFUSED }
        val timeoutResults = results.filter { it.status == PortStatus.TIMEOUT }
        val unreachableResults = results.filter { it.status == PortStatus.UNREACHABLE }
        val unknownResults = results.filter { it.status == PortStatus.UNKNOWN }

        sb.append("================================================================\n")
        sb.append("       MILICONFIG IPV6 SCANNER PRO - AUDIT REPORT               \n")
        sb.append("       Developer: Milad Jahani | Brand: @miliconfig             \n")
        sb.append("================================================================\n\n")
        sb.append("Scan Date     : $now\n")
        sb.append("Target Scope  : $targetSummary\n")
        sb.append("Duration      : ${durationMs}ms\n")
        sb.append("Total Probed  : ${results.size}\n")
        sb.append("Open Ports    : ${openResults.size}\n")
        sb.append("Refused (RST) : ${refusedResults.size}\n")
        sb.append("Timeouts      : ${timeoutResults.size}\n")
        sb.append("Unreachable   : ${unreachableResults.size}\n")
        sb.append("Unknown/Error : ${unknownResults.size}\n\n")

        sb.append("----------------------------------------------------------------\n")
        sb.append("OPEN PORTS BREAKDOWN:\n")
        sb.append("----------------------------------------------------------------\n")
        if (openResults.isEmpty()) {
            sb.append("No open ports discovered in this scan session.\n")
        } else {
            sb.append(String.format("%-42s %-8s %-12s\n", "TARGET ADDRESS", "PORT", "LATENCY"))
            sb.append(String.format("%-42s %-8s %-12s\n", "--------------", "----", "-------"))
            for (op in openResults) {
                sb.append(String.format("%-42s %-8d %d ms\n", op.target.host, op.target.port, op.latencyMs))
            }
        }

        sb.append("\n----------------------------------------------------------------\n")
        sb.append("FULL RESULT DETAILS:\n")
        sb.append("----------------------------------------------------------------\n")
        for (r in results) {
            sb.append("[${r.status.name}] ${r.target.formattedAddress} (${r.latencyMs} ms)")
            if (!r.detailMessage.isNullOrBlank()) {
                sb.append(" - ${r.detailMessage}")
            }
            sb.append("\n")
        }

        sb.append("\n================================================================\n")
        sb.append("Report generated securely on-device with zero external telemetry.\n")
        return sb.toString()
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun shareContent(context: Context, title: String, content: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            putExtra(Intent.EXTRA_TITLE, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
