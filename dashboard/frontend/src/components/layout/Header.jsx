import React from "react";
import { Menu, Moon, Sun, RefreshCw } from "lucide-react";

export default function Header({ title, onOpenSidebar, lastUpdated, isRefreshing, onToggleTheme, theme }) {
  return (
    <header className="sticky top-0 z-30 border-b border-white/10 bg-[#0b1220]/80 backdrop-blur">
      <div className="flex items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <button
            className="rounded-xl p-2 text-slate-200/80 hover:bg-white/5 hover:text-white lg:hidden"
            onClick={onOpenSidebar}
            aria-label="Open sidebar"
          >
            <Menu size={18} />
          </button>
          <div>
            <div className="text-sm font-semibold text-slate-100">{title}</div>
            <div className="text-xs text-slate-300/70">
              {lastUpdated ? `Last updated: ${lastUpdated}` : "Waiting for data…"}
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-2 rounded-xl border border-white/10 bg-white/5 px-3 py-2 text-xs text-slate-200/80">
            <RefreshCw size={14} className={isRefreshing ? "animate-spin" : ""} />
            <span>{isRefreshing ? "Refreshing" : "Live"}</span>
          </div>

          <button
            className="rounded-xl border border-white/10 bg-white/5 p-2 text-slate-200/80 hover:bg-white/10 hover:text-white"
            onClick={onToggleTheme}
            aria-label="Toggle dark mode"
            title="Toggle theme"
          >
            {theme === "dark" ? <Moon size={16} /> : <Sun size={16} />}
          </button>
        </div>
      </div>
    </header>
  );
}

