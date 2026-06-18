package com.dpi;

import com.dpi.analytics.ReportGenerator;
import com.dpi.analytics.PerformanceMonitor;
import com.dpi.analytics.TrafficStats;
import com.dpi.api.StatsServer;
import com.dpi.capture.LiveCaptureService;
import com.dpi.capture.PacketCaptureService;
import com.dpi.flow.FlowTracker;
import com.dpi.processing.WorkerPool;
import com.dpi.rules.RuleManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entry point for the DPI engine. Supports PCAP file analysis and live capture.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int QUEUE_CAPACITY = 10_000;
    private static final int NUM_WORKERS = 4;
    private static final String DEFAULT_RULES = "rules.json";
    private static final int DEFAULT_API_PORT = 8080;

    public static void main(String[] args) {
        String pcapFile = null;
        String liveInterface = null;
        String rulesPath = DEFAULT_RULES;
        int limit = -1;
        int apiPort = DEFAULT_API_PORT;
        boolean serve = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--pcap":
                    if (i + 1 < args.length) pcapFile = args[++i];
                    break;
                case "--live":
                    if (i + 1 < args.length) liveInterface = args[++i];
                    break;
                case "--rules":
                    if (i + 1 < args.length) rulesPath = args[++i];
                    break;
                case "--limit":
                    if (i + 1 < args.length) limit = Integer.parseInt(args[++i]);
                    break;
                case "--api-port":
                    if (i + 1 < args.length) apiPort = Integer.parseInt(args[++i]);
                    break;
                case "--serve":
                    serve = true;
                    break;
                case "--help":
                    printUsage();
                    return;
                default:
                    if (pcapFile == null && !args[i].startsWith("-")) pcapFile = args[i];
                    break;
            }
        }

        if (pcapFile == null && liveInterface == null) {
            System.err.println("Specify --pcap <file> or --live <interface> (e.g. eth0, wlan0)");
            printUsage();
            System.exit(1);
        }

        RuleManager ruleManager = new RuleManager();
        Path rules = Paths.get(rulesPath);
        if (rules.toFile().exists()) {
            try {
                ruleManager.loadFromJson(rules);
            } catch (Exception e) {
                log.warn("Could not load rules from {}: {}", rulesPath, e.getMessage());
            }
        }

        FlowTracker flowTracker = new FlowTracker(100_000);
        BlockingQueue<PacketCaptureService.RawPacket> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        TrafficStats stats = new TrafficStats();
        PerformanceMonitor perf = new PerformanceMonitor(flowTracker);
        perf.start();
        StatsServer statsServer = new StatsServer(apiPort, perf, flowTracker, ruleManager);
        statsServer.start();

        WorkerPool workerPool = new WorkerPool(queue, NUM_WORKERS, flowTracker, ruleManager, stats, perf);
        workerPool.start();

        try {
            if (pcapFile != null) {
                runPcap(Paths.get(pcapFile), queue, perf, limit);
            } else {
                runLive(liveInterface, queue, perf, limit);
            }
        } finally {
            workerPool.stop();
        }

        System.out.println(ReportGenerator.generateReport(flowTracker, stats));

        if (serve) {
            log.info("Serving API on port {} (Ctrl+C to stop).", apiPort);
            try {
                while (true) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        statsServer.stop();
        perf.stop();
    }

    private static void runPcap(Path pcapPath, BlockingQueue<PacketCaptureService.RawPacket> queue,
                                PerformanceMonitor perf, int limit) {
        PacketCaptureService capture = new PacketCaptureService(pcapPath);
        try {
            capture.open();
            int count = 0;
            while (true) {
                PacketCaptureService.RawPacket raw = capture.readNext();
                if (raw == null) break;
                try {
                    if (!queue.offer(raw, 2, TimeUnit.SECONDS)) {
                        log.warn("Queue full, dropping packet");
                        if (perf != null) perf.onIngressDrop(raw.getData() != null ? raw.getData().length : 0);
                        continue;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                count++;
                if (limit > 0 && count >= limit) break;
            }
            log.info("Read {} packets from PCAP", count);
        } catch (Exception e) {
            log.error("PCAP read failed: {}", e.getMessage());
        } finally {
            capture.close();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void runLive(String iface, BlockingQueue<PacketCaptureService.RawPacket> queue,
                                PerformanceMonitor perf, int limit) {
        AtomicInteger count = new AtomicInteger(0);
        LiveCaptureService live = new LiveCaptureService(iface, raw -> {
            try {
                if (queue.offer(raw, 100, TimeUnit.MILLISECONDS)) {
                    // processed by workers
                } else {
                    if (perf != null) perf.onIngressDrop(raw.getData() != null ? raw.getData().length : 0);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            count.incrementAndGet();
        });
        try {
            live.start();
            if (limit > 0) {
                while (count.get() < limit) {
                    Thread.sleep(500);
                }
            } else {
                // Run continuously until Ctrl+C / interrupt
                while (true) {
                    Thread.sleep(1000);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Live capture failed: {}", e.getMessage());
        } finally {
            live.stop();
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar dpi-engine.jar [options]");
        System.out.println("  --pcap <file>     Read packets from PCAP file");
        System.out.println("  --live <iface>    Live capture on interface (e.g. eth0, wlan0)");
        System.out.println("  --rules <file>    JSON rules file (default: rules.json)");
        System.out.println("  --limit <n>       Stop after n packets (optional)");
        System.out.println("  --api-port <p>    Stats API port (default: 8080)");
        System.out.println("  --serve           Keep API running after processing finishes (PCAP mode)");
        System.out.println("Example: java -jar dpi-engine.jar --pcap capture.pcap --rules rules.json");
    }

}
