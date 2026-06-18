package com.dpi.flow;

import com.dpi.classification.AppType;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a tracked network flow with classification and statistics.
 */
public class Flow {

    private final FiveTuple tuple;
    private volatile AppType appType;
    private volatile String sni;
    private volatile boolean blocked;

    private final AtomicLong packetsIn = new AtomicLong(0);
    private final AtomicLong packetsOut = new AtomicLong(0);
    private final AtomicLong bytesIn = new AtomicLong(0);
    private final AtomicLong bytesOut = new AtomicLong(0);

    private volatile Instant firstSeen;
    private volatile Instant lastSeen;

    public Flow(FiveTuple tuple) {
        this.tuple = tuple;
        this.appType = AppType.UNKNOWN;
        this.sni = "";
        this.blocked = false;
        Instant now = Instant.now();
        this.firstSeen = now;
        this.lastSeen = now;
    }

    public FiveTuple getTuple() { return tuple; }
    public AppType getAppType() { return appType; }
    public void setAppType(AppType appType) { this.appType = appType; }
    public String getSni() { return sni; }
    public void setSni(String sni) { this.sni = sni != null ? sni : ""; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public long getPacketsIn() { return packetsIn.get(); }
    public long getPacketsOut() { return packetsOut.get(); }
    public long getBytesIn() { return bytesIn.get(); }
    public long getBytesOut() { return bytesOut.get(); }

    public void addPacketIn(long bytes) {
        packetsIn.incrementAndGet();
        bytesIn.addAndGet(bytes);
        lastSeen = Instant.now();
    }

    public void addPacketOut(long bytes) {
        packetsOut.incrementAndGet();
        bytesOut.addAndGet(bytes);
        lastSeen = Instant.now();
    }

    public long getTotalPackets() {
        return packetsIn.get() + packetsOut.get();
    }

    public Instant getFirstSeen() { return firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
}
