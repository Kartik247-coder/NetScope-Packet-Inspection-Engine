package com.dpi.flow;

import com.dpi.classification.AppType;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks active flows by 5-tuple using a thread-safe map.
 */
public class FlowTracker {

    private final ConcurrentHashMap<FiveTuple, Flow> flows = new ConcurrentHashMap<>();
    private final int maxFlows;
    private final AtomicLong totalSeen = new AtomicLong(0);
    private final AtomicLong classifiedCount = new AtomicLong(0);
    private final AtomicLong blockedCount = new AtomicLong(0);

    public FlowTracker() {
        this(100_000);
    }

    public FlowTracker(int maxFlows) {
        this.maxFlows = maxFlows;
    }

    public Flow getOrCreate(FiveTuple tuple) {
        Flow flow = flows.get(tuple);
        if (flow != null) return flow;
        flow = flows.get(tuple.reverse());
        if (flow != null) return flow;
        if (flows.size() >= maxFlows) {
            cleanupStale(Duration.ofMinutes(5));
        }
        flow = flows.computeIfAbsent(tuple, Flow::new);
        totalSeen.incrementAndGet();
        return flow;
    }

    public Flow get(FiveTuple tuple) {
        Flow f = flows.get(tuple);
        if (f != null) return f;
        return flows.get(tuple.reverse());
    }

    public void update(Flow flow, long packetSize, boolean outbound) {
        if (flow == null) return;
        if (outbound) flow.addPacketOut(packetSize);
        else flow.addPacketIn(packetSize);
    }

    public void classify(Flow flow, AppType appType, String sni) {
        if (flow == null) return;
        flow.setAppType(appType);
        flow.setSni(sni != null ? sni : "");
        classifiedCount.incrementAndGet();
    }

    public void block(Flow flow) {
        if (flow == null) return;
        flow.setBlocked(true);
        blockedCount.incrementAndGet();
    }

    public int cleanupStale(Duration timeout) {
        Instant cutoff = Instant.now().minus(timeout);
        List<FiveTuple> toRemove = new ArrayList<>();
        flows.forEach((tuple, flow) -> {
            if (flow.getLastSeen().isBefore(cutoff)) toRemove.add(tuple);
        });
        toRemove.forEach(flows::remove);
        return toRemove.size();
    }

    public List<Flow> getAllFlows() {
        return new ArrayList<>(flows.values());
    }

    public int getActiveCount() {
        return flows.size();
    }

    public long getTotalSeen() { return totalSeen.get(); }
    public long getClassifiedCount() { return classifiedCount.get(); }
    public long getBlockedCount() { return blockedCount.get(); }
}
