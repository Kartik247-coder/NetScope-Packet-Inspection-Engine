package com.dpi.rules;

import com.dpi.classification.AppType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for rule matching (domain, IP, app, port).
 */
class RuleManagerTest {

    @Test
    void blockAndCheckDomain() {
        RuleManager rm = new RuleManager();
        rm.blockDomain("youtube");
        assertTrue(rm.isDomainBlocked("youtube.com"));
        assertTrue(rm.isDomainBlocked("www.youtube.com"));
        assertFalse(rm.isDomainBlocked("google.com"));
    }

    @Test
    void blockAndCheckIp() {
        RuleManager rm = new RuleManager();
        rm.blockIp("192.168.1.10");
        int ip = (192 & 0xFF) | ((168 & 0xFF) << 8) | ((1 & 0xFF) << 16) | ((10 & 0xFF) << 24);
        assertTrue(rm.isIpBlocked(ip));
        rm.unblockIp("192.168.1.10");
        assertFalse(rm.isIpBlocked(ip));
    }

    @Test
    void blockAndCheckApp() {
        RuleManager rm = new RuleManager();
        rm.blockApp(AppType.FACEBOOK);
        assertTrue(rm.isAppBlocked(AppType.FACEBOOK));
        assertFalse(rm.isAppBlocked(AppType.GOOGLE));
    }

    @Test
    void shouldBlockReturnsReasonForBlockedDomain() {
        RuleManager rm = new RuleManager();
        rm.blockDomain("tiktok");
        int anyIp = (10 << 24) | (0 << 16) | (0 << 8) | 1;
        Optional<RuleManager.BlockReason> r = rm.shouldBlock(anyIp, 443, AppType.UNKNOWN, "www.tiktok.com");
        assertTrue(r.isPresent());
        assertEquals(RuleManager.BlockReason.Type.DOMAIN, r.get().type);
        assertTrue(r.get().detail.contains("tiktok"));
    }

    @Test
    void shouldBlockReturnsEmptyForAllowed() {
        RuleManager rm = new RuleManager();
        int anyIp = (10 << 24) | (0 << 16) | (0 << 8) | 1;
        Optional<RuleManager.BlockReason> r = rm.shouldBlock(anyIp, 443, AppType.GOOGLE, "www.google.com");
        assertFalse(r.isPresent());
    }

    @Test
    void blockPort() {
        RuleManager rm = new RuleManager();
        rm.blockPort(8080);
        assertTrue(rm.isPortBlocked(8080));
    }
}
