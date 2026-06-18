package com.dpi.analytics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe traffic statistics for reporting.
 */
public class TrafficStats {

    private final AtomicLong totalPackets = new AtomicLong(0);
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicLong forwardedPackets = new AtomicLong(0);
    private final AtomicLong droppedPackets = new AtomicLong(0);
    private final AtomicLong tcpPackets = new AtomicLong(0);
    private final AtomicLong udpPackets = new AtomicLong(0);
    private final AtomicLong otherPackets = new AtomicLong(0);
    private final long startTimeNanos = System.nanoTime();

    public void recordPacket(long bytes, boolean tcp, boolean udp, boolean forwarded) {
        totalPackets.incrementAndGet();
        totalBytes.addAndGet(bytes);
        if (tcp) tcpPackets.incrementAndGet();
        else if (udp) udpPackets.incrementAndGet();
        else otherPackets.incrementAndGet();
        if (forwarded) forwardedPackets.incrementAndGet();
        else droppedPackets.incrementAndGet();
    }

    public long getTotalPackets() { return totalPackets.get(); }
    public long getTotalBytes() { return totalBytes.get(); }
    public long getForwardedPackets() { return forwardedPackets.get(); }
    public long getDroppedPackets() { return droppedPackets.get(); }
    public long getTcpPackets() { return tcpPackets.get(); }
    public long getUdpPackets() { return udpPackets.get(); }
    public long getOtherPackets() { return otherPackets.get(); }

    public double getPacketsPerSecond() {
        long elapsed = System.nanoTime() - startTimeNanos;
        if (elapsed <= 0) return 0;
        return totalPackets.get() * 1_000_000_000.0 / elapsed;
    }

    public void reset() {
        totalPackets.set(0);
        totalBytes.set(0);
        forwardedPackets.set(0);
        droppedPackets.set(0);
        tcpPackets.set(0);
        udpPackets.set(0);
        otherPackets.set(0);
    }
}
