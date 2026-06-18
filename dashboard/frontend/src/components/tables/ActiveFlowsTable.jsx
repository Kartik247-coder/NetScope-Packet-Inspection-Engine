import React, { useMemo, useState } from "react";

function classNames(...xs) {
  return xs.filter(Boolean).join(" ");
}

export default function ActiveFlowsTable({ flows, defaultShowBlocked = false }) {
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);
  const [showBlocked, setShowBlocked] = useState(defaultShowBlocked);

  const filtered = useMemo(() => {
    const list = Array.isArray(flows) ? flows : [];
    const q = query.trim().toLowerCase();
    let out = list;
    if (showBlocked) out = out.filter(f => !!f.blocked);
    if (!q) return out;
    return out.filter(f => JSON.stringify(f).toLowerCase().includes(q));
  }, [flows, query, showBlocked]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const safePage = Math.min(page, totalPages);
  const pageRows = filtered.slice((safePage - 1) * pageSize, safePage * pageSize);

  return (
    <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm font-semibold text-slate-100">Active flows</div>
          <div className="text-xs text-slate-200/70">Search + pagination</div>
        </div>

        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <label className="flex items-center gap-2 text-xs text-slate-200/80">
            <input type="checkbox" checked={showBlocked} onChange={e => setShowBlocked(e.target.checked)} />
            show only blocked
          </label>
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(1); }}
            placeholder="Search IP/app/domain…"
            className="w-full rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm text-slate-100 placeholder:text-slate-300/50 sm:w-64"
          />
          <select
            value={pageSize}
            onChange={(e) => { setPageSize(Number(e.target.value)); setPage(1); }}
            className="rounded-xl border border-white/10 bg-black/20 px-3 py-2 text-sm text-slate-100"
          >
            {[10, 20, 50, 100].map(n => <option key={n} value={n}>{n}/page</option>)}
          </select>
        </div>
      </div>

      <div className="mt-4 overflow-auto rounded-xl border border-white/10">
        <table className="min-w-full text-left text-sm">
          <thead className="sticky top-0 bg-[#0b1220] text-xs text-slate-200/70">
            <tr>
              <th className="px-3 py-3">Source</th>
              <th className="px-3 py-3">Destination</th>
              <th className="px-3 py-3">Protocol</th>
              <th className="px-3 py-3">App</th>
              <th className="px-3 py-3">Domain</th>
              <th className="px-3 py-3">Blocked</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-white/5">
            {pageRows.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-3 py-6 text-center text-slate-200/70">No flows.</td>
              </tr>
            ) : pageRows.map((f, idx) => (
              <tr key={idx} className="hover:bg-white/5">
                <td className="px-3 py-3 font-mono text-xs text-slate-100">{f.srcIP}:{f.srcPort}</td>
                <td className="px-3 py-3 font-mono text-xs text-slate-100">{f.dstIP}:{f.dstPort}</td>
                <td className="px-3 py-3 text-slate-100">{f.protocol}</td>
                <td className="px-3 py-3 text-slate-100">{f.application}</td>
                <td className="px-3 py-3 text-slate-100">{f.domain || ""}</td>
                <td className="px-3 py-3">
                  <span className={classNames("rounded-full px-2 py-1 text-xs",
                    f.blocked ? "bg-rose-500/20 text-rose-200 border border-rose-500/30" : "bg-emerald-500/15 text-emerald-200 border border-emerald-500/25"
                  )}>
                    {String(!!f.blocked)}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="text-xs text-slate-200/70">
          {filtered.length} flows
        </div>
        <div className="flex items-center gap-2">
          <button
            className="rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-xs text-slate-100 hover:bg-white/10 disabled:opacity-50"
            onClick={() => setPage(p => Math.max(1, p - 1))}
            disabled={safePage <= 1}
          >
            Prev
          </button>
          <div className="text-xs text-slate-200/70">
            Page {safePage} / {totalPages}
          </div>
          <button
            className="rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-xs text-slate-100 hover:bg-white/10 disabled:opacity-50"
            onClick={() => setPage(p => Math.min(totalPages, p + 1))}
            disabled={safePage >= totalPages}
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
}

