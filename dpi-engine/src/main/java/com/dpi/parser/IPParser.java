package com.dpi.parser;

import com.dpi.util.ByteUtils;

/**
 * Parses IPv4 header and extracts addresses and protocol.
 */
public final class IPParser {

    public static final int MIN_IP_HEADER_LEN = 20;
    public static final byte PROTOCOL_ICMP = 1;
    public static final byte PROTOCOL_TCP = 6;
    public static final byte PROTOCOL_UDP = 17;

    private IPParser() {}

    /**
     * Parse IPv4 header. Returns protocol (e.g. 6=TCP, 17=UDP) or -1 on error.
     */
    public static int parse(byte[] data, int offset, int length, Packet.Builder builder) {
        if (data == null || offset + MIN_IP_HEADER_LEN > length) return -1;
        int versionIhl = data[offset] & 0xFF;
        int version = (versionIhl >> 4) & 0x0F;
        int ihl = versionIhl & 0x0F;
        if (version != 4) return -1;
        int headerLen = ihl * 4;
        if (headerLen < MIN_IP_HEADER_LEN || offset + headerLen > length) return -1;

        byte protocol = data[offset + 9];
        int srcIp = ByteUtils.ipv4ToInt(data, offset + 12);
        int dstIp = ByteUtils.ipv4ToInt(data, offset + 16);

        builder.ipOffset(offset);
        builder.srcIp(ByteUtils.ipv4ToString(srcIp));
        builder.dstIp(ByteUtils.ipv4ToString(dstIp));
        builder.protocol(protocol);
        return offset + headerLen; // next offset after IP header
    }
}
