import React from "react";
import { NavLink } from "react-router-dom";
import { Activity, Layers, ShieldAlert, X } from "lucide-react";

const nav = [
  { to: "/", label: "Dashboard", icon: Activity },
  { to: "/flows", label: "Active Flows", icon: Layers },
  { to: "/flows?blocked=1", label: "Blocked Traffic", icon: ShieldAlert }
];

function LinkItem({ to, label, icon: Icon, onClick }) {
  return (
    <NavLink
      to={to}
      onClick={onClick}
      className={({ isActive }) =>
        [
          "flex items-center gap-3 rounded-xl px-3 py-2 text-sm transition",
          isActive ? "bg-white/10 text-white" : "text-slate-200/80 hover:bg-white/5 hover:text-white"
        ].join(" ")
      }
      end={to === "/"}
    >
      <Icon size={18} className="opacity-90" />
      <span className="font-medium">{label}</span>
    </NavLink>
  );
}

export default function Sidebar({ open, onClose }) {
  return (
    <>
      {/* mobile overlay */}
      <div
        className={[
          "fixed inset-0 z-40 bg-black/40 backdrop-blur-sm lg:hidden",
          open ? "block" : "hidden"
        ].join(" ")}
        onClick={onClose}
      />

      <aside
        className={[
          "fixed inset-y-0 left-0 z-50 w-64 border-r border-white/10 bg-[#070d18] px-4 py-4 lg:block",
          open ? "block" : "hidden"
        ].join(" ")}
      >
        <div className="mb-6 flex items-center justify-between">
          <div>
            <div className="text-sm font-semibold tracking-wide text-slate-100">DPI Network Monitor</div>
            <div className="text-xs text-slate-300/70">Real-time traffic analytics</div>
          </div>
          <button
            className="rounded-lg p-2 text-slate-200/80 hover:bg-white/5 hover:text-white lg:hidden"
            onClick={onClose}
            aria-label="Close sidebar"
          >
            <X size={18} />
          </button>
        </div>

        <nav className="space-y-1">
          {nav.map(item => (
            <LinkItem key={item.to} {...item} onClick={onClose} />
          ))}
        </nav>

        <div className="mt-8 rounded-xl border border-white/10 bg-white/5 p-3 text-xs text-slate-200/70">
          Engine API: <span className="font-mono text-slate-100">localhost:8080</span>
        </div>
      </aside>
    </>
  );
}

