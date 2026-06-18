package com.dpi.processing;

import com.dpi.analytics.PerformanceMonitor;
import com.dpi.analytics.TrafficStats;
import com.dpi.capture.PacketCaptureService;
import com.dpi.flow.FlowTracker;
import com.dpi.parser.Packet;
import com.dpi.parser.PacketParser;
import com.dpi.rules.RuleManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pool of worker threads that parse and process packets from a shared queue.
 */
public class WorkerPool {

    private static final Logger log = LoggerFactory.getLogger(WorkerPool.class);

    private final BlockingQueue<PacketCaptureService.RawPacket> inputQueue;
    private final int numWorkers;
    private final FlowTracker flowTracker;
    private final RuleManager ruleManager;
    private final TrafficStats stats;
    private final PerformanceMonitor performanceMonitor;
    private final AtomicLong packetId = new AtomicLong(0);
    private ExecutorService executor;
    private volatile boolean running;

    public WorkerPool(BlockingQueue<PacketCaptureService.RawPacket> inputQueue, int numWorkers,
                      FlowTracker flowTracker, RuleManager ruleManager, TrafficStats stats,
                      PerformanceMonitor performanceMonitor) {
        this.inputQueue = inputQueue;
        this.numWorkers = numWorkers;
        this.flowTracker = flowTracker;
        this.ruleManager = ruleManager;
        this.stats = stats != null ? stats : new TrafficStats();
        this.performanceMonitor = performanceMonitor;
    }

    public void start() {
        if (running) return;
        running = true;
        executor = Executors.newFixedThreadPool(numWorkers);
        for (int i = 0; i < numWorkers; i++) {
            int workerId = i;
            executor.submit(() -> runWorker(workerId));
        }
        log.info("Worker pool started with {} workers", numWorkers);
    }

    private void runWorker(int workerId) {
        PacketProcessor processor = new PacketProcessor(flowTracker, ruleManager);
        while (running) {
            try {
                PacketCaptureService.RawPacket raw = inputQueue.poll(100, TimeUnit.MILLISECONDS);
                if (raw == null) continue;
                long id = packetId.incrementAndGet();
                Packet packet = PacketParser.parse(raw.getData(), id, raw.getTimestampSec(), raw.getTimestampUsec());
                int len = raw.getData() != null ? raw.getData().length : 0;
                boolean tcp = len >= 34 && (raw.getData()[23] & 0xFF) == 6;
                boolean udp = len >= 34 && (raw.getData()[23] & 0xFF) == 17;
                long latencyNanos = raw.getEnqueueTimeNanos() > 0 ? (System.nanoTime() - raw.getEnqueueTimeNanos()) : -1;
                if (packet == null) {
                    stats.recordPacket(len, tcp, udp, true);
                    if (performanceMonitor != null) performanceMonitor.onPacketProcessed(len, true, latencyNanos);
                    continue;
                }
                boolean forward = processor.process(packet);
                stats.recordPacket(len, tcp, udp, forward);
                if (performanceMonitor != null) performanceMonitor.onPacketProcessed(len, forward, latencyNanos);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("Worker {} error: {}", workerId, e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
