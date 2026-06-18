import React, { useState } from "react";
import Sidebar from "./Sidebar.jsx";
import Header from "./Header.jsx";

export default function DashboardLayout({ title, children, lastUpdated, isRefreshing, onToggleTheme, theme }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-[#0b1220]">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="lg:pl-64">
        <Header
          title={title}
          onOpenSidebar={() => setSidebarOpen(true)}
          lastUpdated={lastUpdated}
          isRefreshing={isRefreshing}
          onToggleTheme={onToggleTheme}
          theme={theme}
        />
        <main className="px-4 pb-10 pt-6 sm:px-6 lg:px-8">
          {children}
        </main>
      </div>
    </div>
  );
}

