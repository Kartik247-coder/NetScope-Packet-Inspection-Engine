import React from "react";
import { LineChart, Line, CartesianGrid, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";

export default function TrafficChart({ data }) {
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="text-sm font-semibold text-slate-100">Traffic</div>
          <div className="text-xs text-slate-200/70">Packets/sec (updates every 2s)</div>
        </div>
      </div>

      <div className="mt-3 h-[280px]">
        <ResponsiveContainer width="100%" height="100%">
          <LineChart data={data || []}>
            <CartesianGrid stroke="rgba(255,255,255,0.08)" />
            <XAxis dataKey="t" tick={{ fill: "rgba(226,232,240,0.65)", fontSize: 11 }} />
            <YAxis tick={{ fill: "rgba(226,232,240,0.65)", fontSize: 11 }} />
            <Tooltip
              contentStyle={{
                background: "#0b1220",
                border: "1px solid rgba(255,255,255,0.12)",
                borderRadius: 12,
                color: "#e2e8f0"
              }}
            />
            <Line type="monotone" dataKey="pps" stroke="#7dd3fc" strokeWidth={2} dot={false} />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

