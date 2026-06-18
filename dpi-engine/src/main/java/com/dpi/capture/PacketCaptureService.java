package com.dpi.capture;

import org.pcap4j.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reads packets from an offline PCAP file using Pcap4J.
 */
public class PacketCaptureService {

    private static final Logger log = LoggerFactory.getLogger(PacketCaptureService.class);
    private static final int SNAPLEN = 65536;
    private static final int READ_TIMEOUT_MS = 1000;

    private final Path pcapPath;
    private PcapHandle handle;
    private final AtomicLong packetCount = new AtomicLong(0);

    public PacketCaptureService(Path pcapPath) {
        this.pcapPath = pcapPath;
    }

    /**
     * Open the PCAP file for reading.
     */
    public void open() throws PcapNativeException, NotOpenException {
        if (handle != null) return;
        handle = Pcaps.openOffline(pcapPath.toAbsolutePath().toString());
        log.info("Opened PCAP file: {}", pcapPath);
    }

    /**
     * Read the next packet. Returns null on EOF or error.
     */
    public RawPacket readNext() {
        if (handle == null || !handle.isOpen()) return null;
        try {
            org.pcap4j.packet.Packet packet = handle.getNextPacketEx();
            if (packet == null) return null;
            byte[] rawData = packet.getRawData();
            if (rawData == null) return null;
            long tsSec = 0;
            int tsUsec = 0;
            packetCount.incrementAndGet();
            return new RawPacket(rawData, tsSec, tsUsec);
        } catch (EOFException e) {
            log.debug("End of PCAP file");
            return null;
        } catch (TimeoutException e) {
            return null;
        } catch (Exception e) {
            log.warn("Error reading packet: {}", e.getMessage());
            return null;
        }
    }

    public boolean isOpen() {
        return handle != null && handle.isOpen();
    }

    public void close() {
        if (handle != null) {
            try {
                handle.close();
            } catch (Exception e) {
                log.warn("Error closing handle: {}", e.getMessage());
            }
            handle = null;
        }
    }

    public long getPacketCount() {
        return packetCount.get();
    }

    /**
     * Simple holder for raw packet data and timestamp.
     */
    public static final class RawPacket {
        private final byte[] data;
        private final long timestampSec;
        private final int timestampUsec;
        private final long enqueueTimeNanos;

        public RawPacket(byte[] data, long timestampSec, int timestampUsec) {
            this(data, timestampSec, timestampUsec, System.nanoTime());
        }

        public RawPacket(byte[] data, long timestampSec, int timestampUsec, long enqueueTimeNanos) {
            this.data = data;
            this.timestampSec = timestampSec;
            this.timestampUsec = timestampUsec;
            this.enqueueTimeNanos = enqueueTimeNanos;
        }

        public byte[] getData() { return data; }
        public long getTimestampSec() { return timestampSec; }
        public int getTimestampUsec() { return timestampUsec; }
        public long getEnqueueTimeNanos() { return enqueueTimeNanos; }
    }
}
