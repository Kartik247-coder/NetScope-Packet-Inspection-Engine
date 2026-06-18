package com.dpi.classification;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Extracts Host header from HTTP request payload.
 */
public final class HTTPHostExtractor {

    private static final byte[] GET = "GET ".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] POST = "POST".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] HOST_PREFIX = "Host:".getBytes(StandardCharsets.US_ASCII);

    private HTTPHostExtractor() {}

    public static boolean isHttpRequest(byte[] payload, int offset, int length) {
        if (payload == null || length < 4 || offset + 4 > payload.length) return false;
        return match(payload, offset, GET) || match(payload, offset, POST)
                || (payload[offset] == 'H' && payload[offset + 1] == 'E' && payload[offset + 2] == 'A' && payload[offset + 3] == 'D')
                || (payload[offset] == 'P' && payload[offset + 1] == 'U' && payload[offset + 2] == 'T' && payload[offset + 3] == ' ');
    }

    public static Optional<String> extract(byte[] payload, int offset, int length) {
        if (payload == null || !isHttpRequest(payload, offset, length)) return Optional.empty();
        int end = offset + length;
        for (int i = offset; i + 6 < end; i++) {
            if (!matchIgnoreCase(payload, i, HOST_PREFIX)) continue;
            int start = i + 5;
            while (start < end && (payload[start] == ' ' || payload[start] == '\t')) start++;
            int lineEnd = start;
            while (lineEnd < end && payload[lineEnd] != '\r' && payload[lineEnd] != '\n') lineEnd++;
            if (lineEnd > start) {
                String host = new String(payload, start, lineEnd - start, StandardCharsets.UTF_8).trim();
                int colon = host.indexOf(':');
                if (colon > 0) host = host.substring(0, colon);
                return Optional.of(host);
            }
        }
        return Optional.empty();
    }

    public static Optional<String> extract(byte[] payload) {
        if (payload == null || payload.length == 0) return Optional.empty();
        return extract(payload, 0, payload.length);
    }

    private static boolean match(byte[] a, int off, byte[] b) {
        if (a == null || b == null || off + b.length > a.length) return false;
        for (int i = 0; i < b.length; i++) if (a[off + i] != b[i]) return false;
        return true;
    }

    private static boolean matchIgnoreCase(byte[] a, int off, byte[] b) {
        if (a == null || b == null || off + b.length > a.length) return false;
        for (int i = 0; i < b.length; i++) {
            byte c1 = a[off + i];
            byte c2 = b[i];
            if (c1 >= 'A' && c1 <= 'Z') c1 += 32;
            if (c2 >= 'A' && c2 <= 'Z') c2 += 32;
            if (c1 != c2) return false;
        }
        return true;
    }
}
