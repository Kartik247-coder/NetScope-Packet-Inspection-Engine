import React, { useEffect, useMemo, useRef, useState } from "react";
import { Activity, ArrowDownRight, ArrowUpRight, Layers, Package, ShieldAlert, TrendingUp } from "lucide-react";
import StatsCard from "../components/cards/StatsCard.jsx";
import TrafficChart from "../components/charts/TrafficChart.jsx";
import AppDistributionChart from "../components/charts/AppDistributionChart.jsx";
import BlockedDomainsTable from "../components/tables/BlockedDomainsTable.jsx";
import ActiveFlowsTable from "../components/tables/ActiveFlowsTable.jsx";
import { getFlows, getStats } from "../services/api.js";

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [flows, setFlows] = useState([]);
  const [history, setHistory] = useState([]);
  const [blockedEvents, setBlockedEvents] = useState([]);
  const [lastUpdated, setLastUpdated] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const prevRef = useRef(null);

  useEffect(() => {
    let alive = true;

    const poll = async () => {
      setRefreshing(true);
      try {
        const [s, f] = await Promise.all([getStats(), getFlows()]);
        if (!alive) return;
        setStats(s);
        setFlows(Array.isArray(f) ? f : []);
        const nowLabel = new Date().toLocaleTimeString();
        setLastUpdated(nowLabel);
        setHistory(prev => {
          const next = [...prev, { t: nowLabel, pps: Number(s.processingRate || 0) }];
          return next.slice(-60);
        });

        // derive blocked events from flow list
        const blockedNow = (Array.isArray(f) ? f : []).filter(x => x?.blocked && x?.domain);
        setBlockedEvents(prev => {
          const seen = new Set(prev.map(e => `${e.domain}|${e.reason}`));
          const next = [...prev];
          for (const b of blockedNow) {
            const reason = "Blocked by rules";
            const key = `${b.domain}|${reason}`;
            if (!seen.has(key)) {
              next.unshift({ domain: b.domain, reason, timestamp: new Date().toLocaleString() });
              seen.add(key);
            }
          }
          return next.slice(0, 50);
        });

        prevRef.current = s;
        window.dispatchEvent(new CustomEvent("dpi:refresh", { detail: { time: nowLabel } }));
      } finally {
        if (alive) setRefreshing(false);
      }
    };

    poll();
    const id = setInterval(poll, 2000);
    return () => {
      alive = false;
      clearInterval(id);
    };
  }, []);

  const topApps = useMemo(() => {
    const map = stats?.topApplications || {};
    return Object.entries(map).map(([name, value]) => ({ name, value: Number(value) }));
  }, [stats]);

  const trend = (key) => {
    const prev = prevRef.current?.[key];
    const cur = stats?.[key];
    if (prev == null || cur == null) return { dir: "flat", label: "—" };
    if (cur > prev) return { dir: "up", label: `+${cur - prev} since last refresh` };
    if (cur < prev) return { dir: "down", label: `-${prev - cur} since last refresh` };
    return { dir: "flat", label: "no change" };
  };

  const tProcessed = trend("packetsProcessed");
  const tDropped = trend("packetsDropped");

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <StatsCard
          title="Total Packets"
          value={stats?.packetsProcessed}
          icon={Package}
          trend={tProcessed.dir}
          trendLabel={tProcessed.label}
        />
        <StatsCard
          title="Forwarded"
          value={stats?.packetsForwarded}
          icon={TrendingUp}
          trend="flat"
          trendLabel="forwarded traffic"
        />
        <StatsCard
          title="Dropped"
          value={stats?.packetsDropped}
          icon={ShieldAlert}
          trend={tDropped.dir}
          trendLabel={tDropped.label}
        />
        <StatsCard
          title="Packets/sec"
          value={stats ? Number(stats.processingRate || 0).toFixed(1) : "-"}
          icon={Activity}
          trend="flat"
          trendLabel="rolling rate"
        />
        <StatsCard
          title="Active Flows"
          value={stats?.activeFlows}
          icon={Layers}
          trend="flat"
          trendLabel="current active"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <TrafficChart data={history} />
        <AppDistributionChart data={topApps} />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
        <BlockedDomainsTable blockedEvents={blockedEvents} />
        <ActiveFlowsTable flows={flows} />
      </div>
    </div>
  );
}

