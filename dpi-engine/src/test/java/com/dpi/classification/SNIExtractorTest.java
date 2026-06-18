package com.dpi.classification;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TLS SNI extraction from Client Hello.
 */
class SNIExtractorTest {

    @Test
    void isTlsClientHelloDetectsValid() {
        byte[] hello = buildMinimalClientHello("www.example.com");
        assertTrue(SNIExtractor.isTlsClientHello(hello, 0, hello.length));
    }

    @Test
    void extractSniFromClientHello() {
        byte[] hello = buildMinimalClientHello("www.youtube.com");
        Optional<String> sni = SNIExtractor.extract(hello);
        assertTrue(sni.isPresent());
        assertEquals("www.youtube.com", sni.get());
    }

    @Test
    void extractReturnsEmptyForNonTls() {
        byte[] random = new byte[100];
        assertFalse(SNIExtractor.extract(random).isPresent());
    }

    @Test
    void extractReturnsEmptyForNullOrEmpty() {
        assertFalse(SNIExtractor.extract(null).isPresent());
        assertFalse(SNIExtractor.extract(new byte[0]).isPresent());
    }

    private static byte[] buildMinimalClientHello(String hostname) {
        byte[] host = hostname.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int sniDataLen = 2 + 1 + 2 + host.length;
        int extLen = 4 + sniDataLen;
        int extensionsLen = extLen;
        int handshakeLen = 2 + 32 + 1 + 0 + 2 + 0 + 1 + 0 + 2 + extensionsLen;
        int recordLen = 4 + handshakeLen;
        byte[] out = new byte[5 + 4 + handshakeLen];
        int off = 0;
        out[off++] = 0x16;
        out[off++] = 0x03;
        out[off++] = 0x01;
        out[off++] = (byte) (recordLen >> 8);
        out[off++] = (byte) recordLen;
        out[off++] = 0x01;
        out[off++] = (byte) (handshakeLen >> 16);
        out[off++] = (byte) (handshakeLen >> 8);
        out[off++] = (byte) handshakeLen;
        off += 2 + 32 + 1 + 0 + 2 + 0 + 1 + 0;
        out[off++] = (byte) (extensionsLen >> 8);
        out[off++] = (byte) extensionsLen;
        out[off++] = 0x00;
        out[off++] = 0x00;
        out[off++] = (byte) (sniDataLen >> 8);
        out[off++] = (byte) sniDataLen;
        out[off++] = (byte) ((3 + host.length) >> 8);
        out[off++] = (byte) (3 + host.length);
        out[off++] = 0x00;
        out[off++] = (byte) (host.length >> 8);
        out[off++] = (byte) host.length;
        System.arraycopy(host, 0, out, off, host.length);
        return out;
    }
}
