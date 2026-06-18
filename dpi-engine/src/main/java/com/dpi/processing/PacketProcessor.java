package com.dpi.processing;

import com.dpi.classification.AppType;
import com.dpi.classification.TrafficClassifier;
import com.dpi.flow.Flow;
import com.dpi.flow.FlowTracker;
import com.dpi.flow.FiveTuple;
import com.dpi.parser.Packet;
import com.dpi.parser.PacketParser;
import com.dpi.rules.RuleManager;
import com.dpi.util.ByteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Processes a single parsed packet: flow tracking, classification, rule check.
 */
public class PacketProcessor {

    private static final Logger log = LoggerFactory.getLogger(PacketProcessor.class);

    private final FlowTracker flowTracker;
    private final RuleManager ruleManager;

    public PacketProcessor(FlowTracker flowTracker, RuleManager ruleManager) {
        this.flowTracker = flowTracker;
        this.ruleManager = ruleManager;
    }

    /**
     * Process packet: update flow, classify if needed, apply rules. Returns true if packet should be forwarded.
     */
    public boolean process(Packet packet) {
        if (packet == null) return true;
        FiveTuple tuple = PacketParser.toFiveTuple(packet);
        if (tuple == null) return true;

        Flow flow = flowTracker.getOrCreate(tuple);
        boolean outbound = isOutbound(packet, tuple);
        flowTracker.update(flow, packet.getRawData() != null ? packet.getRawData().length : 0, outbound);

        Optional<String> domain = TrafficClassifier.extractDomain(packet);
        if (domain.isPresent() && (flow.getSni() == null || flow.getSni().isEmpty())) {
            AppType app = AppType.fromSni(domain.get());
            flowTracker.classify(flow, app, domain.get());
            log.debug("Classified flow {} as {} (SNI: {})", tuple, app, domain.get());
        }
        if (flow.getAppType() == AppType.UNKNOWN) {
            AppType app = TrafficClassifier.classify(packet);
            if (app != AppType.UNKNOWN) {
                flowTracker.classify(flow, app, flow.getSni());
            }
        }

        Optional<RuleManager.BlockReason> block = ruleManager.shouldBlock(
                tuple.getSrcIp(), tuple.getDstPort(), flow.getAppType(),
                flow.getSni() != null ? flow.getSni() : domain.orElse(null));
        if (block.isPresent()) {
            flowTracker.block(flow);
            log.info("Blocked packet: {} reason={} detail={}", tuple, block.get().type, block.get().detail);
            return false;
        }
        return true;
    }

    private boolean isOutbound(Packet packet, FiveTuple tuple) {
        return packet.getSrcPort() == tuple.getSrcPort() && packet.getSrcIp().equals(
                ByteUtils.ipv4ToString(tuple.getSrcIp()));
    }
}
