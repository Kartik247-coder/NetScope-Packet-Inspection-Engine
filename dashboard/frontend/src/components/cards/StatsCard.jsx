import React from "react";

export default function StatsCard({ title, value, icon: Icon, trend, trendLabel }) {
  const trendColor =
    trend === "up" ? "text-emerald-300" : trend === "down" ? "text-rose-300" : "text-slate-200/70";

  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div className="flex items-start justify-between">
        <div>
          <div className="text-xs text-slate-200/70">{title}</div>
          <div className="mt-2 text-2xl font-semibold tracking-tight text-slate-100">
            {value ?? "-"}
          </div>
        </div>
        {Icon && (
          <div className="rounded-xl border border-white/10 bg-white/5 p-2 text-slate-100">
            <Icon size={18} />
          </div>
        )}
      </div>

      <div className={`mt-3 text-xs ${trendColor}`}>
        {trendLabel || "—"}
      </div>
    </div>
  );
}

