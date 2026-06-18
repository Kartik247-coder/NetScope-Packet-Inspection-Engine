import React, { useEffect, useMemo, useState } from "react";
import { Routes, Route, useLocation } from "react-router-dom";
import DashboardLayout from "./components/layout/DashboardLayout.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Flows from "./pages/Flows.jsx";

function getTitle(pathname) {
  if (pathname.startsWith("/flows")) return "Active Flows";
  return "DPI Network Monitor";
}

export default function App() {
  const location = useLocation();
  const [lastUpdated, setLastUpdated] = useState(null);
  const [refreshing, setRefreshing] = useState(false);
  const [theme, setTheme] = useState(() => localStorage.getItem("theme") || "dark");

  useEffect(() => {
    document.documentElement.classList.toggle("dark", theme === "dark");
    localStorage.setItem("theme", theme);
  }, [theme]);

  const title = useMemo(() => getTitle(location.pathname), [location.pathname]);

  // naive refresh indicator: based on polling events fired by pages via window events
  useEffect(() => {
    const onRefresh = () => {
      setRefreshing(true);
      setLastUpdated(new Date().toLocaleTimeString());
      setTimeout(() => setRefreshing(false), 350);
    };
    window.addEventListener("dpi:refresh", onRefresh);
    return () => window.removeEventListener("dpi:refresh", onRefresh);
  }, []);

  return (
    <DashboardLayout
      title={title}
      lastUpdated={lastUpdated}
      isRefreshing={refreshing}
      theme={theme}
      onToggleTheme={() => setTheme(t => (t === "dark" ? "light" : "dark"))}
    >
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/flows" element={<Flows />} />
      </Routes>
    </DashboardLayout>
  );
}

