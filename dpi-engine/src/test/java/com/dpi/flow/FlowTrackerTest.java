package com.dpi.flow;

import com.dpi.classification.AppType;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for flow tracking (5-tuple, getOrCreate, classify).
 */
class FlowTrackerTest {

    @Test
    void getOrCreateReturnsSameFlowForSameTuple() {
        FlowTracker tracker = new FlowTracker(1000);
        int src = (1 << 24) | 10;
        int dst = (2 << 24) | 10;
        FiveTuple tuple = new FiveTuple(src, dst, 12345, 443, (byte) 6);
        Flow f1 = tracker.getOrCreate(tuple);
        Flow f2 = tracker.getOrCreate(tuple);
        assertSame(f1, f2);
    }

    @Test
    void getOrCreateReverseTupleReturnsSameFlow() {
        FlowTracker tracker = new FlowTracker(1000);
        int src = (1 << 24) | 10;
        int dst = (2 << 24) | 10;
        FiveTuple tuple = new FiveTuple(src, dst, 12345, 443, (byte) 6);
        Flow f1 = tracker.getOrCreate(tuple);
        Flow f2 = tracker.getOrCreate(tuple.reverse());
        assertSame(f1, f2);
    }

    @Test
    void classifyUpdatesFlow() {
        FlowTracker tracker = new FlowTracker(1000);
        int src = (1 << 24) | 10;
        int dst = (2 << 24) | 10;
        FiveTuple tuple = new FiveTuple(src, dst, 12345, 443, (byte) 6);
        Flow flow = tracker.getOrCreate(tuple);
        tracker.classify(flow, AppType.YOUTUBE, "www.youtube.com");
        assertEquals(AppType.YOUTUBE, flow.getAppType());
        assertEquals("www.youtube.com", flow.getSni());
    }

    @Test
    void blockMarksFlowBlocked() {
        FlowTracker tracker = new FlowTracker(1000);
        int src = (1 << 24) | 10;
        int dst = (2 << 24) | 10;
        FiveTuple tuple = new FiveTuple(src, dst, 12345, 443, (byte) 6);
        Flow flow = tracker.getOrCreate(tuple);
        tracker.block(flow);
        assertTrue(flow.isBlocked());
    }

    @Test
    void getAllFlowsReturnsActiveFlows() {
        FlowTracker tracker = new FlowTracker(1000);
        int a = (1 << 24) | 10, b = (2 << 24) | 10, c = (3 << 24) | 10, d = (4 << 24) | 10;
        tracker.getOrCreate(new FiveTuple(a, b, 11111, 443, (byte) 6));
        tracker.getOrCreate(new FiveTuple(c, d, 22222, 80, (byte) 6));
        List<Flow> all = tracker.getAllFlows();
        assertEquals(2, all.size());
    }
}
