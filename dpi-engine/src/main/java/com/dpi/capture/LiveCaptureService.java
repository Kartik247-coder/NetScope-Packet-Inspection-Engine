package com.dpi.capture;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Captures live packets from a network interface using Pcap4J.
 */
public class LiveCaptureService {

    private static final Logger log = LoggerFactory.getLogger(LiveCaptureService.class);
    private static final int SNAPLEN = 65536;
    private static final int READ_TIMEOUT_MS = 1000;

    private final String interfaceName;
    private PcapHandle handle;
    private Thread captureThread;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicLong packetCount = new AtomicLong(0);
    private final PacketConsumer consumer;

    @FunctionalInterface
    public interface PacketConsumer {
        void accept(PacketCaptureService.RawPacket raw);
    }

    public LiveCaptureService(String interfaceName, PacketConsumer consumer) {
        this.interfaceName = interfaceName;
        this.consumer = consumer != null ? consumer : (p) -> {};
    }

    /**
     * Start capturing on the configured interface.
     */
    public void start() throws PcapNativeException {
        if (running.get()) return;
        PcapNetworkInterface nif = Pcaps.getDevByName(interfaceName);
        if (nif == null) {
            throw new PcapNativeException("Interface not found: " + interfaceName);
        }
        handle = nif.openLive(SNAPLEN, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, READ_TIMEOUT_MS);
        running.set(true);
        captureThread = new Thread(this::runLoop, "live-capture-" + interfaceName);
        captureThread.setDaemon(true);
        captureThread.start();
        log.info("Live capture started on {}", interfaceName);
    }

    private void runLoop() {
        while (running.get() && handle != null && handle.isOpen()) {
            try {
                org.pcap4j.packet.Packet packet = handle.getNextPacketEx();
                if (packet == null) continue;
                byte[] rawData = packet.getRawData();
                if (rawData == null) continue;
                long tsSec = System.currentTimeMillis() / 1000;
                int tsUsec = (int) ((System.currentTimeMillis() % 1000) * 1000);
                packetCount.incrementAndGet();
                consumer.accept(new PacketCaptureService.RawPacket(rawData, tsSec, tsUsec));
            } catch (org.pcap4j.core.PcapNativeException | org.pcap4j.core.NotOpenException e) {
                if (running.get()) log.warn("Capture error: {}", e.getMessage());
                break;
            } catch (Exception e) {
                if (running.get() && !(e.getMessage() != null && e.getMessage().contains("Timeout"))) {
                    log.warn("Capture error: {}", e.getMessage());
                }
            }
        }
    }

    public void stop() {
        running.set(false);
        if (captureThread != null) {
            try {
                captureThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            captureThread = null;
        }
        if (handle != null) {
            try {
                handle.close();
            } catch (Exception e) {
                log.warn("Error closing handle: {}", e.getMessage());
            }
            handle = null;
        }
        log.info("Live capture stopped");
    }

    public boolean isRunning() {
        return running.get();
    }

    public long getPacketCount() {
        return packetCount.get();
    }

    /**
     * List available network interface names (e.g. eth0, wlan0).
     */
    public static java.util.List<PcapNetworkInterface> listInterfaces() throws PcapNativeException {
        return Pcaps.findAllDevs();
    }
}
