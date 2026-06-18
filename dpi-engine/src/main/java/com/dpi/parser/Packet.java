package com.dpi.parser;

/**
 * Internal representation of a parsed packet for processing through the DPI pipeline.
 */
public final class Packet {

    private final long packetId;
    private final byte[] rawData;
    private final int ethOffset;
    private final int ipOffset;
    private final int transportOffset;
    private final int payloadOffset;
    private final int payloadLength;
    private final long timestampSec;
    private final int timestampUsec;

    // Parsed L2/L3/L4
    private final String srcMac;
    private final String dstMac;
    private final int etherType;
    private final String srcIp;
    private final String dstIp;
    private final byte protocol;
    private final int srcPort;
    private final int dstPort;
    private final int tcpFlags;

    private Packet(Builder b) {
        this.packetId = b.packetId;
        this.rawData = b.rawData;
        this.ethOffset = b.ethOffset;
        this.ipOffset = b.ipOffset;
        this.transportOffset = b.transportOffset;
        this.payloadOffset = b.payloadOffset;
        this.payloadLength = b.payloadLength;
        this.timestampSec = b.timestampSec;
        this.timestampUsec = b.timestampUsec;
        this.srcMac = b.srcMac;
        this.dstMac = b.dstMac;
        this.etherType = b.etherType;
        this.srcIp = b.srcIp;
        this.dstIp = b.dstIp;
        this.protocol = b.protocol;
        this.srcPort = b.srcPort;
        this.dstPort = b.dstPort;
        this.tcpFlags = b.tcpFlags;
    }

    public long getPacketId() { return packetId; }
    public byte[] getRawData() { return rawData; }
    public int getEthOffset() { return ethOffset; }
    public int getIpOffset() { return ipOffset; }
    public int getTransportOffset() { return transportOffset; }
    public int getPayloadOffset() { return payloadOffset; }
    public int getPayloadLength() { return payloadLength; }
    public long getTimestampSec() { return timestampSec; }
    public int getTimestampUsec() { return timestampUsec; }
    public String getSrcMac() { return srcMac; }
    public String getDstMac() { return dstMac; }
    public int getEtherType() { return etherType; }
    public String getSrcIp() { return srcIp; }
    public String getDstIp() { return dstIp; }
    public byte getProtocol() { return protocol; }
    public int getSrcPort() { return srcPort; }
    public int getDstPort() { return dstPort; }
    public int getTcpFlags() { return tcpFlags; }

    /** Payload bytes (after L4 header); may be empty, never null. */
    public byte[] getPayload() {
        if (payloadLength <= 0 || rawData == null || payloadOffset + payloadLength > rawData.length)
            return new byte[0];
        byte[] copy = new byte[payloadLength];
        System.arraycopy(rawData, payloadOffset, copy, 0, payloadLength);
        return copy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long packetId;
        private byte[] rawData;
        private int ethOffset, ipOffset, transportOffset, payloadOffset, payloadLength;
        private long timestampSec;
        private int timestampUsec;
        private String srcMac = "", dstMac = "";
        private int etherType;
        private String srcIp = "", dstIp = "";
        private byte protocol;
        private int srcPort, dstPort;
        private int tcpFlags;

        public Builder packetId(long id) { this.packetId = id; return this; }
        public Builder rawData(byte[] d) { this.rawData = d; return this; }
        public Builder ethOffset(int o) { this.ethOffset = o; return this; }
        public Builder ipOffset(int o) { this.ipOffset = o; return this; }
        public Builder transportOffset(int o) { this.transportOffset = o; return this; }
        public Builder payloadOffset(int o) { this.payloadOffset = o; return this; }
        public Builder payloadLength(int l) { this.payloadLength = l; return this; }
        public Builder timestampSec(long s) { this.timestampSec = s; return this; }
        public Builder timestampUsec(int u) { this.timestampUsec = u; return this; }
        public Builder srcMac(String s) { this.srcMac = s; return this; }
        public Builder dstMac(String s) { this.dstMac = s; return this; }
        public Builder etherType(int e) { this.etherType = e; return this; }
        public Builder srcIp(String s) { this.srcIp = s; return this; }
        public Builder dstIp(String s) { this.dstIp = s; return this; }
        public Builder protocol(byte p) { this.protocol = p; return this; }
        public Builder srcPort(int p) { this.srcPort = p; return this; }
        public Builder dstPort(int p) { this.dstPort = p; return this; }
        public Builder tcpFlags(int f) { this.tcpFlags = f; return this; }

        public Packet build() { return new Packet(this); }
    }
}
