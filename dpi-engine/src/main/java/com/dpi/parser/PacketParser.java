package com.dpi.parser;

import com.dpi.flow.FiveTuple;
import com.dpi.util.ByteUtils;

/**
 * Orchestrates parsing of Ethernet, IP, TCP/UDP and builds an internal Packet and FiveTuple.
 */
public final class PacketParser {

    private PacketParser() {}

    /**
     * Parse raw packet bytes into an internal Packet. Returns null if parsing fails.
     */
    public static Packet parse(byte[] rawData, long packetId, long timestampSec, int timestampUsec) {
        if (rawData == null || rawData.length < EthernetParser.ETH_HEADER_LEN) return null;
        int len = rawData.length;
        int offset = 0;

        Packet.Builder builder = Packet.builder()
                .packetId(packetId)
                .rawData(rawData)
                .timestampSec(timestampSec)
                .timestampUsec(timestampUsec);

        int etherType = EthernetParser.parse(rawData, offset, len, builder);
        if (etherType < 0) return null;
        offset = EthernetParser.ETH_HEADER_LEN;

        if (etherType != EthernetParser.ETHERTYPE_IPv4) {
            builder.payloadOffset(offset);
            builder.payloadLength(Math.max(0, len - offset));
            return builder.build();
        }

        int ipStart = offset;
        int ipNext = IPParser.parse(rawData, offset, len, builder);
        if (ipNext < 0) return null;
        offset = ipNext;
        byte protocol = rawData[ipStart + 9];
        int payloadStart;
        if (protocol == IPParser.PROTOCOL_TCP) {
            payloadStart = TCPParser.parse(rawData, offset, len, builder);
        } else if (protocol == IPParser.PROTOCOL_UDP) {
            payloadStart = UDPParser.parse(rawData, offset, len, builder);
        } else {
            builder.transportOffset(offset);
            builder.srcPort(0);
            builder.dstPort(0);
            builder.tcpFlags(0);
            payloadStart = offset;
        }

        if (payloadStart < 0) return null;
        builder.payloadOffset(payloadStart);
        builder.payloadLength(Math.max(0, len - payloadStart));
        return builder.build();
    }

    /**
     * Build FiveTuple from parsed packet (IPv4 only). Returns null if not IP.
     */
    public static FiveTuple toFiveTuple(Packet packet) {
        if (packet == null || packet.getSrcIp() == null || packet.getSrcIp().isEmpty()) return null;
        int srcIp = parseIpString(packet.getSrcIp());
        int dstIp = parseIpString(packet.getDstIp());
        if (srcIp == 0 && packet.getSrcIp().equals("0.0.0.0")) srcIp = 0;
        if (dstIp == 0 && packet.getDstIp().equals("0.0.0.0")) dstIp = 0;
        return new FiveTuple(srcIp, dstIp, packet.getSrcPort(), packet.getDstPort(), packet.getProtocol());
    }

    private static int parseIpString(String ip) {
        if (ip == null || ip.isEmpty()) return 0;
        String[] parts = ip.split("\\.");
        if (parts.length != 4) return 0;
        int result = 0;
        for (int i = 0; i < 4; i++) {
            try {
                int octet = Integer.parseInt(parts[i].trim());
                if (octet < 0 || octet > 255) return 0;
                result |= (octet << (i * 8));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return result;
    }
}
