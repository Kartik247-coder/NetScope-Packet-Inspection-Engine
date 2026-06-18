package com.dpi.parser;

import com.dpi.util.ByteUtils;

/**
 * Parses TCP header and extracts ports and flags.
 */
public final class TCPParser {

    public static final int MIN_TCP_HEADER_LEN = 20;
    public static final int FLAG_FIN = 0x01;
    public static final int FLAG_SYN = 0x02;
    public static final int FLAG_RST = 0x04;
    public static final int FLAG_PSH = 0x08;
    public static final int FLAG_ACK = 0x10;
    public static final int FLAG_URG = 0x20;

    private TCPParser() {}

    /**
     * Parse TCP header. Returns offset of payload (after TCP header), or -1 on error.
     */
    public static int parse(byte[] data, int offset, int length, Packet.Builder builder) {
        if (data == null || offset + MIN_TCP_HEADER_LEN > length) return -1;
        int srcPort = ByteUtils.readUint16BE(data, offset);
        int dstPort = ByteUtils.readUint16BE(data, offset + 2);
        int dataOffset = (data[offset + 12] >> 4) & 0x0F;
        int headerLen = dataOffset * 4;
        if (headerLen < MIN_TCP_HEADER_LEN || offset + headerLen > length) return -1;
        int flags = data[offset + 13] & 0xFF;

        builder.transportOffset(offset);
        builder.srcPort(srcPort);
        builder.dstPort(dstPort);
        builder.tcpFlags(flags);
        return offset + headerLen;
    }
}
