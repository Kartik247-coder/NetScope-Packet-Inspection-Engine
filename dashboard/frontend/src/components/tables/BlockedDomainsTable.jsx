import React from "react";

export default function BlockedDomainsTable({ blockedEvents }) {
  const events = Array.isArray(blockedEvents) ? blockedEvents : [];
  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div>
        <div className="text-sm font-semibold text-slate-100">Blocked traffic</div>
        <div className="text-xs text-slate-200/70">Recent block events (derived from flows polling)</div>
      </div>

      <div className="mt-4 overflow-auto rounded-xl border border-white/10">
        <table className="min-w-full text-left text-sm">
          <thead className="sticky top-0 bg-[#0b1220] text-xs text-slate-200/70">
            <tr>
              <th className="px-3 py-3">Domain</th>
              <th className="px-3 py-3">Reason</th>
              <th className="px-3 py-3">Timestamp</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {events.length === 0 ? (
              <tr>
                <td colSpan={3} className="px-3 py-6 text-center text-slate-200/70">No blocked events yet.</td>
              </tr>
            ) : events.map((e, idx) => (
              <tr key={idx} className="hover:bg-white/5">
                <td className="px-3 py-3 font-mono text-xs text-slate-100">{e.domain}</td>
                <td className="px-3 py-3 text-slate-100">{e.reason}</td>
                <td className="px-3 py-3 text-slate-200/80">{e.timestamp}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

