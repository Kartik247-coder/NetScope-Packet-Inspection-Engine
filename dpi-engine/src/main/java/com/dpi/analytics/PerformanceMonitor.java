package com.dpi.analytics;

import com.dpi.flow.FlowTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Periodically prints DPI performance metrics and exposes thread-safe counters for the API server.
 */
public class PerformanceMonitor {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitor.class);

    private final AtomicLong packetsProcessed = new AtomicLong(0);
    private final AtomicLong packetsForwarded = new AtomicLong(0);
    private final AtomicLong packetsDropped = new AtomicLong(0);
    private final AtomicLong bytesProcessed = new AtomicLong(0);

    private final AtomicLong totalLatencyNanos = new AtomicLong(0);
    private final AtomicLong latencySamples = new AtomicLong(0);

    private final AtomicLong lastProcessed = new AtomicLong(0);
    private final AtomicLong lastPrintNanos = new AtomicLong(System.nanoTime());

    private final FlowTracker flowTracker;
    private final ScheduledExecutorService scheduler;
    private final Duration printInterval;

    public PerformanceMonitor(FlowTracker flowTracker) {
        this(flowTracker, Duration.ofSeconds(5));
    }

    public PerformanceMonitor(FlowTracker flowTracker, Duration printInterval) {
        this.flowTracker = flowTracker;
        this.printInterval = printInterval;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "perf-monitor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        long periodMs = Math.max(1000, printInterval.toMillis());
        scheduler.scheduleAtFixedRate(this::printSnapshot, periodMs, periodMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    /** Called by workers when a packet completes processing. */
    public void onPacketProcessed(long bytes, boolean forwarded, long latencyNanos) {
        packetsProcessed.incrementAndGet();
        bytesProcessed.addAndGet(Math.max(0, bytes));
        if (forwarded) packetsForwarded.incrementAndGet();
        else packetsDropped.incrementAndGet();
        if (latencyNanos >= 0) {
            totalLatencyNanos.addAndGet(latencyNanos);
            latencySamples.incrementAndGet();
        }
    }

    /** Called when packet is dropped before processing (e.g., queue full). */
    public void onIngressDrop(long bytes) {
        packetsDropped.incrementAndGet();
        bytesProcessed.addAndGet(Math.max(0, bytes));
    }

    public Snapshot snapshot() {
        long processed = packetsProcessed.get();
        long forwarded = packetsForwarded.get();
        long dropped = packetsDropped.get();
        long bytes = bytesProcessed.get();
        long samples = latencySamples.get();
        double avgLatencyMs = samples > 0 ? (totalLatencyNanos.get() / 1_000_000.0) / samples : 0.0;
        double rate = computeRate(processed);
        int activeFlows = flowTracker != null ? flowTracker.getActiveCount() : 0;
        return new Snapshot(processed, forwarded, dropped, bytes, rate, avgLatencyMs, activeFlows);
    }

    private double computeRate(long processedNow) {
        long now = System.nanoTime();
        long lastNow = lastPrintNanos.get();
        long lastProc = lastProcessed.get();
        long elapsed = now - lastNow;
        if (elapsed <= 0) return 0;
        return (processedNow - lastProc) * 1_000_000_000.0 / elapsed;
    }

    private void printSnapshot() {
        Snapshot s = snapshot();
        // update rate window
        lastProcessed.set(s.packetsProcessed);
        lastPrintNanos.set(System.nanoTime());

        String block =
                "\n===== DPI PERFORMANCE =====\n" +
                "Packets processed: " + s.packetsProcessed + "\n" +
                "Packets/sec: " + String.format("%.1f", s.processingRate) + "\n" +
                "Forwarded: " + s.packetsForwarded + "\n" +
                "Dropped: " + s.packetsDropped + "\n" +
                "Bytes processed: " + s.bytesProcessed + "\n" +
                "Avg latency: " + String.format("%.3f ms", s.averageLatencyMs) + "\n" +
                "Active flows: " + s.activeFlows + "\n" +
                "===========================\n";
        log.info(block);
    }

    public static final class Snapshot {
        public final long packetsProcessed;
        public final long packetsForwarded;
        public final long packetsDropped;
        public final long bytesProcessed;
        public final double processingRate;
        public final double averageLatencyMs;
        public final int activeFlows;

        public Snapshot(long packetsProcessed,
                        long packetsForwarded,
                        long packetsDropped,
                        long bytesProcessed,
                        double processingRate,
                        double averageLatencyMs,
                        int activeFlows) {
            this.packetsProcessed = packetsProcessed;
            this.packetsForwarded = packetsForwarded;
            this.packetsDropped = packetsDropped;
            this.bytesProcessed = bytesProcessed;
            this.processingRate = processingRate;
            this.averageLatencyMs = averageLatencyMs;
            this.activeFlows = activeFlows;
        }
    }
}

