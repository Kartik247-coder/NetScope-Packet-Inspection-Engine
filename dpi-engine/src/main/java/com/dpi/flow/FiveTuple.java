package com.dpi.flow;

import com.dpi.util.ByteUtils;

import java.util.Objects;

/**
 * Five-tuple that uniquely identifies a network flow:
 * (srcIP, dstIP, srcPort, dstPort, protocol).
 */
public final class FiveTuple {

    private final int srcIp;
    private final int dstIp;
    private final int srcPort;
    private final int dstPort;
    private final byte protocol;

    public FiveTuple(int srcIp, int dstIp, int srcPort, int dstPort, byte protocol) {
        this.srcIp = srcIp;
        this.dstIp = dstIp;
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.protocol = protocol;
    }

    public int getSrcIp() { return srcIp; }
    public int getDstIp() { return dstIp; }
    public int getSrcPort() { return srcPort; }
    public int getDstPort() { return dstPort; }
    public byte getProtocol() { return protocol; }

    /**
     * Reverse tuple for matching bidirectional flows (swap src/dst).
     */
    public FiveTuple reverse() {
        return new FiveTuple(dstIp, srcIp, dstPort, srcPort, protocol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiveTuple fiveTuple = (FiveTuple) o;
        return srcIp == fiveTuple.srcIp
                && dstIp == fiveTuple.dstIp
                && srcPort == fiveTuple.srcPort
                && dstPort == fiveTuple.dstPort
                && protocol == fiveTuple.protocol;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcIp, dstIp, srcPort, dstPort, protocol);
    }

    @Override
    public String toString() {
        return ByteUtils.ipv4ToString(srcIp) + ":" + srcPort
                + " -> "
                + ByteUtils.ipv4ToString(dstIp) + ":" + dstPort
                + " (" + (protocol == 6 ? "TCP" : protocol == 17 ? "UDP" : "?") + ")";
    }
}
