import { useState } from "react";
import { DailyStatsTable } from "./components/DailyStatsTable";
import { SummaryCard } from "./components/SummaryCard";

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function thirtyDaysAgoStr() {
  const d = new Date();
  d.setDate(d.getDate() - 30);
  return d.toISOString().slice(0, 10);
}

export default function App() {
  const [from, setFrom] = useState(thirtyDaysAgoStr());
  const [to, setTo] = useState(todayStr());

  return (
    <div style={{ maxWidth: 960, margin: "0 auto", padding: "24px 16px", fontFamily: "sans-serif" }}>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 20 }}>
        Token Monitoring Dashboard
      </h1>

      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 24 }}>
        <label style={{ fontSize: 14 }}>
          시작일
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
          />
        </label>
        <label style={{ fontSize: 14 }}>
          종료일
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
          />
        </label>
      </div>

      <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>요약</h2>
      <SummaryCard from={from} to={to} />

      <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>일별 통계</h2>
      <DailyStatsTable from={from} to={to} />
    </div>
  );
}
