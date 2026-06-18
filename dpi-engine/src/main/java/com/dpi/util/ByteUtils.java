package com.dpi.util;

import java.nio.ByteOrder;

/**
 * Utilities for reading network byte order (big-endian) values from byte arrays.
 */
public final class ByteUtils {

    private ByteUtils() {}

    /**
     * Read unsigned 16-bit value in big-endian order.
     */
    public static int readUint16BE(byte[] data, int offset) {
        if (data == null || offset + 2 > data.length) {
            throw new IllegalArgumentException("Insufficient data for uint16");
        }
        int b1 = data[offset] & 0xFF;
        int b2 = data[offset + 1] & 0xFF;
        return (b1 << 8) | b2;
    }

    /**
     * Read unsigned 24-bit value in big-endian order.
     */
    public static int readUint24BE(byte[] data, int offset) {
        if (data == null || offset + 3 > data.length) {
            throw new IllegalArgumentException("Insufficient data for uint24");
        }
        int b1 = data[offset] & 0xFF;
        int b2 = data[offset + 1] & 0xFF;
        int b3 = data[offset + 2] & 0xFF;
        return (b1 << 16) | (b2 << 8) | b3;
    }

    /**
     * Read unsigned 32-bit value in big-endian order.
     */
    public static long readUint32BE(byte[] data, int offset) {
        if (data == null || offset + 4 > data.length) {
            throw new IllegalArgumentException("Insufficient data for uint32");
        }
        long b1 = data[offset] & 0xFFL;
        long b2 = data[offset + 1] & 0xFFL;
        long b3 = data[offset + 2] & 0xFFL;
        long b4 = data[offset + 3] & 0xFFL;
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    /**
     * Convert IPv4 address (4 bytes, network order) to 32-bit value.
     */
    public static int ipv4ToInt(byte[] data, int offset) {
        if (data == null || offset + 4 > data.length) {
            throw new IllegalArgumentException("Insufficient data for IPv4");
        }
        return (data[offset] & 0xFF)
                | ((data[offset + 1] & 0xFF) << 8)
                | ((data[offset + 2] & 0xFF) << 16)
                | ((data[offset + 3] & 0xFF) << 24);
    }

    /**
     * Convert 32-bit IPv4 to dotted-decimal string.
     */
    public static String ipv4ToString(int ip) {
        return ((ip >> 0) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
    }

    /**
     * Safe read of uint16 without throwing; returns -1 if insufficient data.
     */
    public static int readUint16BESafe(byte[] data, int offset) {
        if (data == null || offset + 2 > data.length) return -1;
        return readUint16BE(data, offset);
    }

    /**
     * Safe read of uint24 without throwing; returns -1 if insufficient data.
     */
    public static int readUint24BESafe(byte[] data, int offset) {
        if (data == null || offset + 3 > data.length) return -1;
        return readUint24BE(data, offset);
    }
}
