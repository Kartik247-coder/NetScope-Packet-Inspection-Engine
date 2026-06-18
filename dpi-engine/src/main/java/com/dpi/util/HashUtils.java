package com.dpi.util;

import com.dpi.flow.FiveTuple;

/**
 * Hashing utilities for 5-tuple and other DPI structures.
 */
public final class HashUtils {

    private static final int FNV_OFFSET = 0x811c9dc5;
    private static final int FNV_PRIME = 0x01000193;

    private HashUtils() {}

    /**
     * FNV-1a style hash for FiveTuple to distribute flows across workers.
     */
    public static int hashFiveTuple(FiveTuple tuple) {
        int h = FNV_OFFSET;
        h = fnv1a(h, tuple.getSrcIp());
        h = fnv1a(h, tuple.getDstIp());
        h = fnv1a(h, tuple.getSrcPort() & 0xFFFF);
        h = fnv1a(h, tuple.getDstPort() & 0xFFFF);
        h = fnv1a(h, tuple.getProtocol() & 0xFF);
        return h;
    }

    private static int fnv1a(int current, int value) {
        current ^= (value & 0xFF);
        current *= FNV_PRIME;
        current ^= ((value >> 8) & 0xFF);
        current *= FNV_PRIME;
        current ^= ((value >> 16) & 0xFF);
        current *= FNV_PRIME;
        current ^= ((value >> 24) & 0xFF);
        current *= FNV_PRIME;
        return current;
    }
}
