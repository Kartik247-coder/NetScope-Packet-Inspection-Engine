package com.dpi.parser;

import com.dpi.util.ByteUtils;

/**
 * Parses Ethernet (L2) header and extracts EtherType.
 */
public final class EthernetParser {

    public static final int ETH_HEADER_LEN = 14;
    public static final int ETHERTYPE_IPv4 = 0x0800;
    public static final int ETHERTYPE_IPv6 = 0x86DD;
    public static final int ETHERTYPE_ARP = 0x0806;

    private EthernetParser() {}

    /**
     * Parse Ethernet header. Returns EtherType (e.g. 0x0800 for IPv4) or -1 on error.
     */
    public static int parse(byte[] data, int offset, int length, Packet.Builder builder) {
        if (data == null || length < offset + ETH_HEADER_LEN) return -1;
        int etherType = ByteUtils.readUint16BESafe(data, offset + 12);
        if (etherType < 0) return -1;
        builder.dstMac(macToString(data, offset));
        builder.srcMac(macToString(data, offset + 6));
        builder.etherType(etherType);
        builder.ethOffset(offset);
        return etherType;
    }

    public static String macToString(byte[] data, int offset) {
        if (data == null || offset + 6 > data.length) return "";
        StringBuilder sb = new StringBuilder(17);
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append(':');
            sb.append(String.format("%02x", data[offset + i] & 0xFF));
        }
        return sb.toString();
    }
}
