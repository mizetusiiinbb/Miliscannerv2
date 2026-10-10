package com.miliconfig.ipv6scanner

import com.miliconfig.ipv6scanner.scanner.IPv6Utils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IPv6UtilsTest {

    @Test
    fun testValidIPv6Detection() {
        assertTrue(IPv6Utils.isValidIPv6("2001:4860:4860::8888"))
        assertTrue(IPv6Utils.isValidIPv6("::1"))
        assertTrue(IPv6Utils.isValidIPv6("fe80::1"))
        assertTrue(IPv6Utils.isValidIPv6("[2606:4700:4700::1111]"))
        assertTrue(IPv6Utils.isValidIPv6("2001:0db8:85a3:0000:0000:8a2e:0370:7334"))

        assertFalse(IPv6Utils.isValidIPv6("192.168.1.1"))
        assertFalse(IPv6Utils.isValidIPv6("localhost"))
        assertFalse(IPv6Utils.isValidIPv6("2001:xyz::1"))
        assertFalse(IPv6Utils.isValidIPv6(""))
    }

    @Test
    fun testParsePortsSingleAndRanges() {
        val res1 = IPv6Utils.parsePorts("80, 443, 8080")
        assertTrue(res1.isSuccess)
        assertEquals(listOf(80, 443, 8080), res1.getOrThrow())

        val res2 = IPv6Utils.parsePorts("8000-8003, 22")
        assertTrue(res2.isSuccess)
        assertEquals(listOf(22, 8000, 8001, 8002, 8003), res2.getOrThrow())

        val resInvalid = IPv6Utils.parsePorts("0, 70000")
        assertTrue(resInvalid.isFailure)

        val resEmpty = IPv6Utils.parsePorts("   ")
        assertTrue(resEmpty.isFailure)
    }

    @Test
    fun testCidrExpansionWithinSafeguards() {
        // /126 has 4 hosts
        val res126 = IPv6Utils.expandCidr("2001:db8::/126")
        assertTrue(res126.isSuccess)
        val list126 = res126.getOrThrow()
        assertEquals(4, list126.size)

        // /128 has 1 host
        val res128 = IPv6Utils.expandCidr("2001:db8::1/128")
        assertTrue(res128.isSuccess)
        assertEquals(1, res128.getOrThrow().size)

        // /120 has 256 hosts (allowed upper bound)
        val res120 = IPv6Utils.expandCidr("2001:db8::/120")
        assertTrue(res120.isSuccess)
        assertEquals(256, res120.getOrThrow().size)
    }

    @Test
    fun testCidrSafeguardRejectsLargeSubnets() {
        // /64 has 1.8 * 10^19 hosts - MUST be rejected!
        val res64 = IPv6Utils.expandCidr("2001:db8::/64")
        assertTrue(res64.isFailure)
        val msg64 = res64.exceptionOrNull()?.message ?: ""
        assertTrue(msg64.contains("safeguard"))

        // /119 has 512 hosts - exceeds max 256 limit!
        val res119 = IPv6Utils.expandCidr("2001:db8::/119")
        assertTrue(res119.isFailure)
    }

    @Test
    fun testBatchTargetParsing() {
        val content = """
            # This is a comment
            2001:4860:4860::8888,53
            [2606:4700:4700::1111]:443
            2001:db8::1
            invalid_host
        """.trimIndent()

        val defaultPorts = listOf(80, 8080)
        val targets = IPv6Utils.parseBatchTargets(content, defaultPorts)

        // 1 explicit port (53) + 1 bracketed port (443) + 2 default ports for 2001:db8::1 = 4 targets
        assertEquals(4, targets.size)
        assertTrue(targets.any { it.host == "2001:4860:4860::8888" && it.port == 53 })
        assertTrue(targets.any { it.host == "2606:4700:4700::1111" && it.port == 443 })
        assertTrue(targets.any { it.host == "2001:db8::1" && it.port == 80 })
        assertTrue(targets.any { it.host == "2001:db8::1" && it.port == 8080 })
    }
}
