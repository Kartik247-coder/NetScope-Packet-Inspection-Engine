import React from "react";
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from "recharts";

const COLORS = ["#a78bfa", "#7dd3fc", "#34d399", "#fb7185", "#fbbf24", "#60a5fa", "#f472b6", "#c084fc"];

export default function AppDistributionChart({ data }) {
  const safe = (data || []).slice(0, 8);
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div>
        <div className="text-sm font-semibold text-slate-100">Application distribution</div>
        <div className="text-xs text-slate-200/70">Top apps by active flows</div>
      </div>
      <div className="mt-3 h-[280px]">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie data={safe} dataKey="value" nameKey="name" innerRadius={60} outerRadius={100} paddingAngle={2}>
              {safe.map((_, idx) => (
                <Cell key={idx} fill={COLORS[idx % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{
                background: "#0b1220",
                border: "1px solid rgba(255,255,255,0.12)",
                borderRadius: 12,
                color: "#e2e8f0"
              }}
            />
            <Legend />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

