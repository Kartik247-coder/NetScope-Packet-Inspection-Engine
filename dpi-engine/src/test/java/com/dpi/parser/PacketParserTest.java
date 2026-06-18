package com.dpi.parser;

import com.dpi.flow.FiveTuple;
import com.dpi.util.ByteUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for packet parsing (Ethernet, IP, TCP).
 */
class PacketParserTest {

    @Test
    void parseMinimalEthernetIpTcp() {
        byte[] raw = buildMinimalIpTcpPacket();
        Packet p = PacketParser.parse(raw, 1, 0, 0);
        assertNotNull(p);
        assertEquals(1, p.getPacketId());
        assertEquals("10.0.0.1", p.getSrcIp());
        assertEquals("10.0.0.2", p.getDstIp());
        assertEquals(80, p.getSrcPort());
        assertEquals(443, p.getDstPort());
        assertEquals(6, p.getProtocol());
    }

    @Test
    void toFiveTuple() {
        byte[] raw = buildMinimalIpTcpPacket();
        Packet p = PacketParser.parse(raw, 1, 0, 0);
        FiveTuple tuple = PacketParser.toFiveTuple(p);
        assertNotNull(tuple);
        assertEquals("10.0.0.1", ByteUtils.ipv4ToString(tuple.getSrcIp()));
        assertEquals("10.0.0.2", ByteUtils.ipv4ToString(tuple.getDstIp()));
        assertEquals(80, tuple.getSrcPort());
        assertEquals(443, tuple.getDstPort());
        assertEquals(6, tuple.getProtocol());
    }

    @Test
    void parseNullOrShortReturnsNull() {
        assertNull(PacketParser.parse(null, 1, 0, 0));
        assertNull(PacketParser.parse(new byte[10], 1, 0, 0));
    }

    private static byte[] buildMinimalIpTcpPacket() {
        byte[] eth = new byte[14];
        eth[12] = 0x08;
        eth[13] = 0x00;  // IPv4
        byte[] ip = new byte[20];
        ip[0] = 0x45;
        ip[9] = 6;  // TCP
        ip[12] = 10;  ip[13] = 0;  ip[14] = 0;  ip[15] = 1;   // src 10.0.0.1
        ip[16] = 10;  ip[17] = 0;  ip[18] = 0;  ip[19] = 2;   // dst 10.0.0.2
        byte[] tcp = new byte[20];
        tcp[0] = 0x00; tcp[1] = 0x50;   // src port 80
        tcp[2] = 0x01; tcp[3] = (byte) 0xbb;  // dst port 443
        tcp[12] = 0x50; tcp[13] = 0x02;  // data offset 5, flags
        byte[] out = new byte[14 + 20 + 20];
        System.arraycopy(eth, 0, out, 0, 14);
        System.arraycopy(ip, 0, out, 14, 20);
        System.arraycopy(tcp, 0, out, 34, 20);
        out[16] = (byte) (40 >> 8);
        out[17] = (byte) 40;
        return out;
    }
}
