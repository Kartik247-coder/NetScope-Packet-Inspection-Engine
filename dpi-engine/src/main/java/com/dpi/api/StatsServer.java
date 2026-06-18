package com.dpi.api;

import com.dpi.analytics.PerformanceMonitor;
import com.dpi.classification.AppType;
import com.dpi.flow.Flow;
import com.dpi.flow.FlowTracker;
import com.dpi.rules.RuleManager;
import com.dpi.util.ByteUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Lightweight embedded HTTP server for real-time dashboard stats.
 *
 * Endpoints:
 * - GET /stats
 * - GET /flows
 */
public class StatsServer {

    private static final Logger log = LoggerFactory.getLogger(StatsServer.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final int port;
    private final PerformanceMonitor performanceMonitor;
    private final FlowTracker flowTracker;
    private final RuleManager ruleManager;

    private HttpServer server;

    public StatsServer(int port, PerformanceMonitor performanceMonitor, FlowTracker flowTracker, RuleManager ruleManager) {
        this.port = port;
        this.performanceMonitor = performanceMonitor;
        this.flowTracker = flowTracker;
        this.ruleManager = ruleManager;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            server.createContext("/stats", this::handleStats);
            server.createContext("/flows", this::handleFlows);
            server.setExecutor(Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "stats-server");
                t.setDaemon(true);
                return t;
            }));
            server.start();
            log.info("StatsServer started on http://localhost:{} (endpoints: /stats, /flows)", port);
        } catch (IOException e) {
            log.warn("Failed to start StatsServer on port {}: {}", port, e.getMessage());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private void handleStats(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            writeJson(ex, 405, Map.of("error", "Method not allowed"));
            return;
        }
        PerformanceMonitor.Snapshot snap = performanceMonitor != null ? performanceMonitor.snapshot() :
                new PerformanceMonitor.Snapshot(0, 0, 0, 0, 0, 0, flowTracker != null ? flowTracker.getActiveCount() : 0);

        Map<String, Integer> topApps = computeTopApps();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("packetsProcessed", snap.packetsProcessed);
        out.put("packetsForwarded", snap.packetsForwarded);
        out.put("packetsDropped", snap.packetsDropped);
        out.put("bytesProcessed", snap.bytesProcessed);
        out.put("activeFlows", snap.activeFlows);
        out.put("processingRate", snap.processingRate);
        out.put("averageLatencyMs", snap.averageLatencyMs);
        out.put("topApplications", topApps);
        if (ruleManager != null) {
            out.put("blockedDomains", new ArrayList<>(ruleManager.getBlockedDomains()));
            out.put("blockedApps", ruleManager.getBlockedApps().stream().map(Enum::name).toList());
            out.put("blockedIps", new ArrayList<>(ruleManager.getBlockedIps()));
        }

        writeJson(ex, 200, out);
    }

    private void handleFlows(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            writeJson(ex, 405, Map.of("error", "Method not allowed"));
            return;
        }
        List<Map<String, Object>> flows = new ArrayList<>();
        if (flowTracker != null) {
            for (Flow f : flowTracker.getAllFlows()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("srcIP", ByteUtils.ipv4ToString(f.getTuple().getSrcIp()));
                row.put("dstIP", ByteUtils.ipv4ToString(f.getTuple().getDstIp()));
                row.put("srcPort", f.getTuple().getSrcPort());
                row.put("dstPort", f.getTuple().getDstPort());
                row.put("protocol", f.getTuple().getProtocol() == 6 ? "TCP" : f.getTuple().getProtocol() == 17 ? "UDP" : String.valueOf(f.getTuple().getProtocol()));
                row.put("application", safeAppName(f.getAppType()));
                row.put("domain", f.getSni());
                row.put("blocked", f.isBlocked());
                flows.add(row);
            }
        }
        writeJson(ex, 200, flows);
    }

    private Map<String, Integer> computeTopApps() {
        Map<AppType, Integer> counts = new EnumMap<>(AppType.class);
        if (flowTracker != null) {
            for (Flow f : flowTracker.getAllFlows()) {
                AppType app = f.getAppType() != null ? f.getAppType() : AppType.UNKNOWN;
                counts.put(app, counts.getOrDefault(app, 0) + 1);
            }
        }
        List<Map.Entry<AppType, Integer>> entries = new ArrayList<>(counts.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        Map<String, Integer> top = new LinkedHashMap<>();
        for (int i = 0; i < Math.min(10, entries.size()); i++) {
            top.put(safeAppName(entries.get(i).getKey()), entries.get(i).getValue());
        }
        return top;
    }

    private static String safeAppName(AppType app) {
        if (app == null) return "UNKNOWN";
        return switch (app) {
            case UNKNOWN -> "UNKNOWN";
            default -> app.toString();
        };
    }

    private static void writeJson(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = JSON.writeValueAsBytes(body);
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", "application/json; charset=utf-8");
        h.set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}

