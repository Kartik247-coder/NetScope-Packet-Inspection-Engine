package com.dpi.rules;

import com.dpi.classification.AppType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe rule manager for domain, IP, app, and port blocking.
 * Supports dynamic reload from JSON.
 */
public class RuleManager {

    private static final Logger log = LoggerFactory.getLogger(RuleManager.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final Set<Integer> blockedIps = ConcurrentHashMap.newKeySet();
    private final Set<AppType> blockedApps = ConcurrentHashMap.newKeySet();
    private final Set<String> blockedDomains = ConcurrentHashMap.newKeySet();
    private final List<String> domainPatterns = Collections.synchronizedList(new ArrayList<>());
    private final Set<Integer> blockedPorts = ConcurrentHashMap.newKeySet();

    public void blockIp(int ip) {
        blockedIps.add(ip);
        log.info("Blocked IP: {}", intToIp(ip));
    }

    public void blockIp(String ip) {
        blockIp(parseIp(ip));
    }

    public void unblockIp(int ip) {
        blockedIps.remove(ip);
    }

    public void unblockIp(String ip) {
        unblockIp(parseIp(ip));
    }

    public boolean isIpBlocked(int ip) {
        return blockedIps.contains(ip);
    }

    public Set<String> getBlockedIps() {
        Set<String> out = new HashSet<>();
        for (Integer ip : blockedIps) out.add(intToIp(ip));
        return out;
    }

    public void blockApp(AppType app) {
        blockedApps.add(app);
        log.info("Blocked app: {}", app);
    }

    public void blockApp(String appName) {
        try {
            blockApp(AppType.valueOf(appName.toUpperCase(Locale.ROOT).replace(" ", "_")));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown app type: {}", appName);
        }
    }

    public void unblockApp(AppType app) {
        blockedApps.remove(app);
    }

    public boolean isAppBlocked(AppType app) {
        return blockedApps.contains(app);
    }

    public Set<AppType> getBlockedApps() {
        return new HashSet<>(blockedApps);
    }

    public void blockDomain(String domain) {
        if (domain != null && domain.contains("*")) {
            domainPatterns.add(domain);
        } else if (domain != null) {
            blockedDomains.add(domain.toLowerCase(Locale.ROOT));
        }
        log.info("Blocked domain: {}", domain);
    }

    public void unblockDomain(String domain) {
        if (domain != null && domain.contains("*")) {
            domainPatterns.remove(domain);
        } else if (domain != null) {
            blockedDomains.remove(domain.toLowerCase(Locale.ROOT));
        }
    }

    public Set<String> getBlockedDomains() {
        Set<String> out = new HashSet<>();
        out.addAll(blockedDomains);
        synchronized (domainPatterns) {
            out.addAll(domainPatterns);
        }
        return out;
    }

    public boolean isDomainBlocked(String domain) {
        if (domain == null || domain.isEmpty()) return false;
        String lower = domain.toLowerCase(Locale.ROOT);
        if (blockedDomains.contains(lower)) return true;
        for (String block : blockedDomains) {
            String b = block.toLowerCase(Locale.ROOT);
            if (lower.equals(b)) return true;
            if (lower.endsWith("." + b) || lower.startsWith(b + ".") || lower.contains("." + b + ".")) return true;
        }
        for (String pattern : new ArrayList<>(domainPatterns)) {
            if (domainMatchesPattern(lower, pattern.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }

    private static boolean domainMatchesPattern(String domain, String pattern) {
        if (pattern.length() >= 2 && pattern.startsWith("*.")) {
            String suffix = pattern.substring(1);
            if (domain.endsWith(suffix) || domain.equals(pattern.substring(2))) return true;
        }
        return false;
    }

    public void blockPort(int port) {
        blockedPorts.add(port);
    }

    public void unblockPort(int port) {
        blockedPorts.remove(port);
    }

    public boolean isPortBlocked(int port) {
        return blockedPorts.contains(port);
    }

    /**
     * Returns reason if the given criteria should be blocked, else empty.
     */
    public Optional<BlockReason> shouldBlock(int srcIp, int dstPort, AppType app, String domain) {
        if (isIpBlocked(srcIp)) return Optional.of(new BlockReason(BlockReason.Type.IP, intToIp(srcIp)));
        if (isPortBlocked(dstPort)) return Optional.of(new BlockReason(BlockReason.Type.PORT, String.valueOf(dstPort)));
        if (app != null && isAppBlocked(app)) return Optional.of(new BlockReason(BlockReason.Type.APP, app.toString()));
        if (domain != null && !domain.isEmpty() && isDomainBlocked(domain)) return Optional.of(new BlockReason(BlockReason.Type.DOMAIN, domain));
        return Optional.empty();
    }

    public void loadFromJson(Path path) throws IOException {
        String content = Files.readString(path);
        RuleConfig config = JSON.readValue(content, RuleConfig.class);
        clearAll();
        for (String ip : config.getBlockedIps()) blockIp(ip);
        for (String app : config.getBlockedApps()) blockApp(app);
        for (String domain : config.getBlockedDomains()) blockDomain(domain);
        for (Integer port : config.getBlockedPorts()) blockPort(port);
        log.info("Rules loaded from {}", path);
    }

    public void clearAll() {
        blockedIps.clear();
        blockedApps.clear();
        blockedDomains.clear();
        domainPatterns.clear();
        blockedPorts.clear();
    }

    public static final class BlockReason {
        public enum Type { IP, APP, DOMAIN, PORT }
        public final Type type;
        public final String detail;
        public BlockReason(Type type, String detail) {
            this.type = type;
            this.detail = detail;
        }
    }

    private static int parseIp(String ip) {
        if (ip == null || ip.isEmpty()) return 0;
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) return 0;
        int result = 0;
        for (int i = 0; i < 4; i++) {
            try {
                int octet = Integer.parseInt(parts[i].trim());
                if (octet < 0 || octet > 255) return 0;
                result |= (octet << (i * 8));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return result;
    }

    private static String intToIp(int ip) {
        return ((ip >> 0) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }
}
