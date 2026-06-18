package com.dpi.classification;

import com.dpi.parser.Packet;

import java.util.Optional;

/**
 * Classifies packet payload (TLS SNI, HTTP Host) and maps to AppType.
 */
public final class TrafficClassifier {

    private TrafficClassifier() {}

    /**
     * Extract domain/host from packet payload (TLS SNI or HTTP Host).
     */
    public static Optional<String> extractDomain(Packet packet) {
        if (packet == null) return Optional.empty();
        byte[] payload = packet.getPayload();
        if (payload == null || payload.length == 0) return Optional.empty();
        Optional<String> sni = SNIExtractor.extract(payload);
        if (sni.isPresent()) return sni;
        return HTTPHostExtractor.extract(payload);
    }

    /**
     * Classify packet to AppType using extracted domain or port heuristics.
     */
    public static AppType classify(Packet packet) {
        Optional<String> domain = extractDomain(packet);
        if (domain.isPresent()) return AppType.fromSni(domain.get());
        int dstPort = packet.getDstPort();
        if (dstPort == 80) return AppType.HTTP;
        if (dstPort == 443) return AppType.HTTPS;
        if (dstPort == 53) return AppType.DNS;
        return AppType.UNKNOWN;
    }
}
