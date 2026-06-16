import { useState } from "react";
import { DailyChart } from "./components/DailyChart";
import { DailyStatsTable } from "./components/DailyStatsTable";
import { EventList } from "./components/EventList";
import { ProjectManagement } from "./components/ProjectManagement";
import { SummaryCard } from "./components/SummaryCard";

type Tab = "dashboard" | "projects" | "events";

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function thirtyDaysAgoStr() {
  const d = new Date();
  d.setDate(d.getDate() - 30);
  return d.toISOString().slice(0, 10);
}

const TABS: [Tab, string][] = [
  ["dashboard", "대시보드"],
  ["projects", "프로젝트 관리"],
  ["events", "이벤트 목록"],
];

export default function App() {
  const [tab, setTab] = useState<Tab>("dashboard");
  const [from, setFrom] = useState(thirtyDaysAgoStr());
  const [to, setTo] = useState(todayStr());
  const [model, setModel] = useState("");
  const [projectId, setProjectId] = useState("");

  return (
    <div style={{ maxWidth: 960, margin: "0 auto", padding: "24px 16px", fontFamily: "sans-serif" }}>
      <h1 style={{ fontSize: 22, fontWeight: 700, marginBottom: 20 }}>
        Token Monitoring Dashboard
      </h1>

      <div style={{ display: "flex", marginBottom: 24, borderBottom: "2px solid #e0e0e0" }}>
        {TABS.map(([id, label]) => (
          <button
            key={id}
            onClick={() => setTab(id)}
            style={{
              padding: "8px 20px",
              fontSize: 14,
              fontWeight: tab === id ? 600 : 400,
              background: "none",
              border: "none",
              borderBottom: tab === id ? "2px solid #0066cc" : "2px solid transparent",
              marginBottom: -2,
              cursor: "pointer",
              color: tab === id ? "#0066cc" : "#555",
            }}
          >
            {label}
          </button>
        ))}
      </div>

      {tab === "dashboard" && (
        <>
          <div
            style={{
              display: "flex",
              gap: 12,
              alignItems: "center",
              marginBottom: 24,
              flexWrap: "wrap",
            }}
          >
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
            <label style={{ fontSize: 14 }}>
              모델 필터
              <input
                type="text"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                placeholder="예: claude-sonnet-4-5"
                style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14, width: 200 }}
              />
            </label>
            <label style={{ fontSize: 14 }}>
              프로젝트 ID
              <input
                type="number"
                value={projectId}
                onChange={(e) => setProjectId(e.target.value)}
                placeholder="전체"
                style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14, width: 80 }}
              />
            </label>
          </div>

          <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>요약</h2>
          <SummaryCard from={from} to={to} />

          <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>일별 비용 추이</h2>
          <DailyChart
            from={from}
            to={to}
            model={model || undefined}
            projectId={projectId ? Number(projectId) : undefined}
          />

          <h2 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>일별 통계</h2>
          <DailyStatsTable
            from={from}
            to={to}
            model={model || undefined}
            projectId={projectId ? Number(projectId) : undefined}
          />
        </>
      )}

      {tab === "projects" && <ProjectManagement />}

      {tab === "events" && <EventList />}
    </div>
  );
}
