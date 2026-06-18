package com.dpi.parser;

import com.dpi.util.ByteUtils;

/**
 * Parses UDP header and extracts ports.
 */
public final class UDPParser {

    public static final int UDP_HEADER_LEN = 8;

    private UDPParser() {}

    /**
     * Parse UDP header. Returns offset of payload (after UDP header), or -1 on error.
     */
    public static int parse(byte[] data, int offset, int length, Packet.Builder builder) {
        if (data == null || offset + UDP_HEADER_LEN > length) return -1;
        int srcPort = ByteUtils.readUint16BE(data, offset);
        int dstPort = ByteUtils.readUint16BE(data, offset + 2);

        builder.transportOffset(offset);
        builder.srcPort(srcPort);
        builder.dstPort(dstPort);
        builder.tcpFlags(0);
        return offset + UDP_HEADER_LEN;
    }
}
