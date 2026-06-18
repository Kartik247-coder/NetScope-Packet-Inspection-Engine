import React, { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import ActiveFlowsTable from "../components/tables/ActiveFlowsTable.jsx";
import { getFlows } from "../services/api.js";

function useQuery() {
  const { search } = useLocation();
  return new URLSearchParams(search);
}

export default function Flows() {
  const [flows, setFlows] = useState([]);
  const [refreshing, setRefreshing] = useState(false);
  const q = useQuery();
  const defaultShowBlocked = q.get("blocked") === "1";

  useEffect(() => {
    let alive = true;
    const poll = async () => {
      setRefreshing(true);
      try {
        const f = await getFlows();
        if (!alive) return;
        setFlows(Array.isArray(f) ? f : []);
        window.dispatchEvent(new CustomEvent("dpi:refresh", { detail: { time: new Date().toLocaleTimeString() } }));
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

  return (
    <div className="space-y-4">
      <div className="text-xs text-slate-200/70">
        {refreshing ? "Refreshing…" : "Live"} (updates every 2s)
      </div>
      <ActiveFlowsTable flows={flows} defaultShowBlocked={defaultShowBlocked} />
    </div>
  );
}

