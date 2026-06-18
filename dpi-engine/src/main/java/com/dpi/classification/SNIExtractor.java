package com.dpi.classification;

import com.dpi.util.ByteUtils;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Extracts Server Name Indication (SNI) from TLS Client Hello payload.
 */
public final class SNIExtractor {

    private static final int CONTENT_TYPE_HANDSHAKE = 0x16;
    private static final int HANDSHAKE_CLIENT_HELLO = 0x01;
    private static final int EXTENSION_SNI = 0x0000;
    private static final int SNI_TYPE_HOSTNAME = 0x00;

    private SNIExtractor() {}

    public static boolean isTlsClientHello(byte[] payload, int offset, int length) {
        if (payload == null || length < 9 || offset + 9 > payload.length) return false;
        if ((payload[offset] & 0xFF) != CONTENT_TYPE_HANDSHAKE) return false;
        int version = ByteUtils.readUint16BESafe(payload, offset + 1);
        if (version < 0x0300 || version > 0x0304) return false;
        int recordLen = ByteUtils.readUint16BESafe(payload, offset + 3);
        if (recordLen < 0 || offset + 5 + recordLen > offset + length) return false;
        if ((payload[offset + 5] & 0xFF) != HANDSHAKE_CLIENT_HELLO) return false;
        return true;
    }

    public static Optional<String> extract(byte[] payload, int offset, int length) {
        if (payload == null || !isTlsClientHello(payload, offset, length)) return Optional.empty();
        int off = offset + 5;
        int len = length - 5;
        if (len < 4) return Optional.empty();
        int handshakeLength = ByteUtils.readUint24BESafe(payload, off + 1);
        if (handshakeLength < 0) return Optional.empty();
        off += 4;
        off += 2;  // client version
        off += 32; // random
        if (off >= offset + length) return Optional.empty();
        int sessionIdLen = payload[off] & 0xFF;
        off += 1 + sessionIdLen;
        if (off + 2 > offset + length) return Optional.empty();
        int cipherSuitesLen = ByteUtils.readUint16BE(payload, off);
        off += 2 + cipherSuitesLen;
        if (off >= offset + length) return Optional.empty();
        int compLen = payload[off] & 0xFF;
        off += 1 + compLen;
        if (off + 2 > offset + length) return Optional.empty();
        int extensionsLen = ByteUtils.readUint16BE(payload, off);
        off += 2;
        int extensionsEnd = Math.min(off + extensionsLen, offset + length);

        while (off + 4 <= extensionsEnd) {
            int extType = ByteUtils.readUint16BE(payload, off);
            int extLen = ByteUtils.readUint16BE(payload, off + 2);
            off += 4;
            if (off + extLen > extensionsEnd) break;
            if (extType == EXTENSION_SNI && extLen >= 5) {
                int sniListLen = ByteUtils.readUint16BE(payload, off);
                if (sniListLen < 3) { off += extLen; continue; }
                int sniType = payload[off + 2] & 0xFF;
                int sniLen = ByteUtils.readUint16BE(payload, off + 3);
                if (sniType != SNI_TYPE_HOSTNAME || sniLen > extLen - 5) { off += extLen; continue; }
                String host = new String(payload, off + 5, sniLen, StandardCharsets.UTF_8);
                return Optional.of(host);
            }
            off += extLen;
        }
        return Optional.empty();
    }

    public static Optional<String> extract(byte[] payload) {
        if (payload == null || payload.length == 0) return Optional.empty();
        return extract(payload, 0, payload.length);
    }
}
