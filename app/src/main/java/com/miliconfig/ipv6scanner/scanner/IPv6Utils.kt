package com.miliconfig.ipv6scanner.scanner

import com.miliconfig.ipv6scanner.model.ScanTarget
import java.math.BigInteger
import java.net.Inet6Address
import java.net.InetAddress

object IPv6Utils {

    const val MAX_ALLOWED_HOSTS_IN_SUBNET = 256
    const val MIN_ALLOWED_PREFIX_LENGTH = 120

    fun cleanAddress(raw: String): String {
        var trimmed = raw.trim()
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            trimmed = trimmed.substring(1, trimmed.length - 1)
        }
        val scopeIndex = trimmed.indexOf('%')
        if (scopeIndex != -1) {
            trimmed = trimmed.substring(0, scopeIndex)
        }
        return trimmed
    }

    fun isValidIPv6(address: String): Boolean {
        val cleaned = cleanAddress(address)
        if (cleaned.isEmpty() || !cleaned.contains(":")) return false
        return try {
            val inet = InetAddress.getByName(cleaned)
            inet is Inet6Address
        } catch (_: Exception) {
            false
        }
    }

    fun parsePorts(input: String): Result<List<Int>> {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Port input cannot be empty"))
        }

        val resultPorts = mutableSetOf<Int>()
        val segments = trimmed.split(",", " ")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (segment in segments) {
            if (segment.contains("-")) {
                val parts = segment.split("-")
                if (parts.size != 2) {
                    return Result.failure(IllegalArgumentException("Invalid port range syntax: $segment"))
                }
                val start = parts[0].toIntOrNull()
                val end = parts[1].toIntOrNull()
                if (start == null || end == null || start < 1 || end > 65535 || start > end) {
                    return Result.failure(IllegalArgumentException("Invalid port range: $segment (allowed 1-65535)"))
                }
                if ((end - start) > 1000) {
                    return Result.failure(IllegalArgumentException("Port range too large in: $segment (max 1000 ports per range)"))
                }
                for (p in start..end) {
                    resultPorts.add(p)
                }
            } else {
                val port = segment.toIntOrNull()
                if (port == null || port !in 1..65535) {
                    return Result.failure(IllegalArgumentException("Invalid port number: $segment (allowed 1-65535)"))
                }
                resultPorts.add(port)
            }
        }

        if (resultPorts.isEmpty()) {
            return Result.failure(IllegalArgumentException("No valid ports found"))
        }
        return Result.success(resultPorts.sorted())
    }

    fun expandCidr(cidr: String, maxAllowedHosts: Int = MAX_ALLOWED_HOSTS_IN_SUBNET): Result<List<String>> {
        val parts = cidr.trim().split("/")
        if (parts.size != 2) {
            return Result.failure(IllegalArgumentException("CIDR must be in format <IPv6>/<prefix> (e.g. 2001:db8::/124)"))
        }

        val baseIpStr = cleanAddress(parts[0])
        val prefix = parts[1].toIntOrNull()
            ?: return Result.failure(IllegalArgumentException("Invalid prefix length: ${parts[1]}"))

        if (prefix !in 0..128) {
            return Result.failure(IllegalArgumentException("IPv6 prefix length must be between 0 and 128"))
        }

        // Strict Safeguard Against Exhaustive Enumeration
        if (prefix < MIN_ALLOWED_PREFIX_LENGTH) {
            val totalHosts = BigInteger.valueOf(2).pow(128 - prefix)
            return Result.failure(
                IllegalArgumentException(
                    "Safety safeguard triggered: Prefix /$prefix spans $totalHosts addresses. " +
                    "To prevent network flooding and exhaustive enumeration of vast IPv6 address spaces, " +
                    "scans are limited to prefixes >= /$MIN_ALLOWED_PREFIX_LENGTH (maximum $maxAllowedHosts addresses)."
                )
            )
        }

        val baseInet = try {
            val parsed = InetAddress.getByName(baseIpStr)
            if (parsed !is Inet6Address) {
                return Result.failure(IllegalArgumentException("Base address is not a valid IPv6 address: $baseIpStr"))
            }
            parsed
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Invalid IPv6 address: $baseIpStr"))
        }

        val baseBytes = baseInet.address
        val baseBigInt = BigInteger(1, baseBytes)
        val hostBits = 128 - prefix
        val hostCount = 1 shl hostBits

        if (hostCount > maxAllowedHosts) {
            return Result.failure(
                IllegalArgumentException("Subnet contains $hostCount addresses, exceeding limit of $maxAllowedHosts.")
            )
        }

        // Calculate network start
        val mask = BigInteger.valueOf(2).pow(128).subtract(BigInteger.ONE)
            .shiftRight(hostBits).shiftLeft(hostBits)
        val networkStart = baseBigInt.and(mask)

        val resultList = mutableListOf<String>()
        for (i in 0 until hostCount) {
            val currentIpInt = networkStart.add(BigInteger.valueOf(i.toLong()))
            val ipBytes = currentIpInt.toByteArray()
            val paddedBytes = ByteArray(16)
            val srcOffset = if (ipBytes.size > 16) ipBytes.size - 16 else 0
            val destOffset = if (ipBytes.size < 16) 16 - ipBytes.size else 0
            val length = minOf(16, ipBytes.size - srcOffset)
            System.arraycopy(ipBytes, srcOffset, paddedBytes, destOffset, length)

            val inetAddr = InetAddress.getByAddress(paddedBytes)
            resultList.add(inetAddr.hostAddress ?: currentIpInt.toString(16))
        }

        return Result.success(resultList)
    }

    fun parseBatchTargets(content: String, defaultPorts: List<Int>): List<ScanTarget> {
        val targets = mutableListOf<ScanTarget>()
        val lines = content.lines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            // Support CSV: "target,port" or "target"
            val tokens = trimmed.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
            if (tokens.isEmpty()) continue

            val rawTarget = tokens[0]
            val explicitPort = if (tokens.size > 1) tokens[1].toIntOrNull() else null

            if (rawTarget.startsWith("[") && rawTarget.contains("]:")) {
                val closingBracket = rawTarget.indexOf("]:")
                val host = rawTarget.substring(1, closingBracket)
                val portStr = rawTarget.substring(closingBracket + 2)
                val port = portStr.toIntOrNull()
                if (isValidIPv6(host) && port != null && port in 1..65535) {
                    targets.add(ScanTarget(cleanAddress(host), port))
                }
            } else if (explicitPort != null && explicitPort in 1..65535 && isValidIPv6(rawTarget)) {
                targets.add(ScanTarget(cleanAddress(rawTarget), explicitPort))
            } else if (isValidIPv6(rawTarget)) {
                val cleanIp = cleanAddress(rawTarget)
                for (p in defaultPorts) {
                    targets.add(ScanTarget(cleanIp, p))
                }
            }
        }
        return targets
    }
}
