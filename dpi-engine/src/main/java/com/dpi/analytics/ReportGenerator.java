package com.dpi.analytics;

import com.dpi.classification.AppType;
import com.dpi.flow.Flow;
import com.dpi.flow.FlowTracker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates traffic analytics and summary reports.
 */
public final class ReportGenerator {

    private ReportGenerator() {}

    public static String generateReport(FlowTracker flowTracker, TrafficStats stats) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔══════════════════════════════════════════════════════════════╗\n");
        sb.append("║               DPI ENGINE - TRAFFIC REPORT                    ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Total Packets:     %10d                              ║\n", stats.getTotalPackets()));
        sb.append(String.format("║ Total Bytes:       %10d                              ║\n", stats.getTotalBytes()));
        sb.append(String.format("║ Forwarded:         %10d                              ║\n", stats.getForwardedPackets()));
        sb.append(String.format("║ Dropped/Blocked:   %10d                              ║\n", stats.getDroppedPackets()));
        sb.append(String.format("║ Processing rate:   %8.1f packets/sec                  ║\n", stats.getPacketsPerSecond()));
        sb.append("╠══════════════════════════════════════════════════════════════╣\n");
        sb.append("║ Active Flows:      ").append(String.format("%10d", flowTracker.getActiveCount())).append("                              ║\n");
        sb.append("║ Classified:        ").append(String.format("%10d", flowTracker.getClassifiedCount())).append("                              ║\n");
        sb.append("║ Blocked:           ").append(String.format("%10d", flowTracker.getBlockedCount())).append("                              ║\n");
        sb.append("╚══════════════════════════════════════════════════════════════╝\n");

        Map<AppType, Long> appCounts = new HashMap<>();
        List<Flow> flows = flowTracker.getAllFlows();
        long totalClassified = 0;
        for (Flow f : flows) {
            if (f.getAppType() != AppType.UNKNOWN) {
                appCounts.merge(f.getAppType(), 1L, Long::sum);
                totalClassified++;
            }
        }
        if (!appCounts.isEmpty()) {
            sb.append("\n--- Application distribution (by flow count) ---\n");
            final long total = totalClassified;
            appCounts.entrySet().stream()
                    .sorted(Map.Entry.<AppType, Long>comparingByValue().reversed())
                    .limit(15)
                    .forEach(e -> {
                        double pct = total > 0 ? 100.0 * e.getValue() / total : 0;
                        sb.append(String.format("  %-20s %6d (%5.1f%%)\n", e.getKey(), e.getValue(), pct));
                    });
        }

        Map<String, Long> domainCounts = flows.stream()
                .filter(f -> f.getSni() != null && !f.getSni().isEmpty())
                .collect(Collectors.groupingBy(Flow::getSni, Collectors.counting()));
        if (!domainCounts.isEmpty()) {
            sb.append("\n--- Top domains (SNI) ---\n");
            domainCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .forEach(e -> sb.append(String.format("  %-40s %d\n", truncate(e.getKey(), 38), e.getValue())));
        }
        return sb.toString();
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
