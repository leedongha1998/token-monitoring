import { useEffect, useState } from "react";
import { fetchBudget } from "./api/budget";
import { fetchSessionEfficiency } from "./api/sessionEfficiency";
import { fetchSummaryStats } from "./api/stats";
import { AdvisorCard } from "./components/AdvisorCard";
import { AlertBell } from "./components/AlertBell";
import { DailyChart } from "./components/DailyChart";
import { DailyStatsTable } from "./components/DailyStatsTable";
import { EventList } from "./components/EventList";
import { ModelCostTable } from "./components/ModelCostTable";
import { ModelPieChart } from "./components/ModelPieChart";
import { ProjectManagement } from "./components/ProjectManagement";
import { ProjectSelector } from "./components/ProjectSelector";
import { SessionEfficiencyChart } from "./components/SessionEfficiencyChart";
import { SummaryCard } from "./components/SummaryCard";
import type { SessionEfficiency } from "./types/advisor";

type Tab = "dashboard" | "analysis" | "projects" | "events";

const TABS: [Tab, string][] = [
  ["dashboard", "대시보드"],
  ["analysis", "모델 분석"],
  ["projects", "프로젝트 관리"],
  ["events", "이벤트 목록"],
];

function todayStr() {
  return new Date().toISOString().slice(0, 10);
}

function thirtyDaysAgoStr() {
  const d = new Date();
  d.setDate(d.getDate() - 30);
  return d.toISOString().slice(0, 10);
}

function currentYearMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

function firstDayOfCurrentMonth() {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-01`;
}

interface BudgetAlert {
  projectName: string;
  budget: number;
  usagePct: number;
  usedCost: number;
}

export default function App() {
  const [tab, setTab] = useState<Tab>("dashboard");
  const [from, setFrom] = useState(thirtyDaysAgoStr());
  const [to, setTo] = useState(todayStr());
  const [model, setModel] = useState("");
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(undefined);
  const [selectedProjectName, setSelectedProjectName] = useState<string>("");
  const [budgetAlert, setBudgetAlert] = useState<BudgetAlert | null>(null);
  const [sessionData, setSessionData] = useState<SessionEfficiency[]>([]);

  useEffect(() => {
    if (!selectedProjectId) {
      setBudgetAlert(null);
      return;
    }
    const capturedName = selectedProjectName;
    const ym = currentYearMonth();
    const firstDay = firstDayOfCurrentMonth();
    const today = todayStr();
    Promise.all([
      fetchBudget(selectedProjectId, ym),
      fetchSummaryStats({ from: firstDay, to: today, projectId: selectedProjectId }),
    ])
      .then(([budget, summary]) => {
        if (!budget) {
          setBudgetAlert(null);
          return;
        }
        const usedCost = parseFloat(summary.totalCost);
        const usagePct = (usedCost / budget.monthlyBudgetUsd) * 100;
        if (usagePct > 80) {
          setBudgetAlert({
            projectName: capturedName,
            budget: budget.monthlyBudgetUsd,
            usagePct,
            usedCost,
          });
        } else {
          setBudgetAlert(null);
        }
      })
      .catch(() => setBudgetAlert(null));
  }, [selectedProjectId, selectedProjectName]);

  useEffect(() => {
    if (!selectedProjectId) {
      setSessionData([]);
      return;
    }
    fetchSessionEfficiency(selectedProjectId, from, to)
      .then(setSessionData)
      .catch(() => setSessionData([]));
  }, [selectedProjectId, from, to]);

  const showDateFilter = tab === "dashboard" || tab === "analysis";

  return (
    <div className="max-w-5xl mx-auto px-4 py-6 font-sans">
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold">Token Monitoring Dashboard</h1>
        {selectedProjectId != null && <AlertBell projectId={selectedProjectId} />}
      </div>

      {budgetAlert && (
        <div className="bg-orange-50 border border-orange-400 text-orange-800 rounded-lg px-4 py-3 mb-5 text-sm font-medium">
          {budgetAlert.projectName ? `${budgetAlert.projectName}: ` : ""}
          월 예산 ${budgetAlert.budget.toFixed(2)}의 {budgetAlert.usagePct.toFixed(1)}% 사용 중 ($
          {budgetAlert.usedCost.toFixed(4)})
        </div>
      )}

      <div className="flex mb-6 border-b-2 border-gray-200">
        {TABS.map(([id, label]) => (
          <button
            key={id}
            onClick={() => setTab(id)}
            className={[
              "px-5 py-2 text-sm border-0 border-b-2 -mb-px bg-transparent cursor-pointer transition-colors",
              tab === id
                ? "border-blue-600 text-blue-600 font-semibold"
                : "border-transparent text-gray-500 hover:text-gray-700",
            ].join(" ")}
          >
            {label}
          </button>
        ))}
      </div>

      {showDateFilter && (
        <div className="flex flex-wrap gap-3 items-center mb-6">
          <label className="text-sm">
            시작일
            <input
              type="date"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
            />
          </label>
          <label className="text-sm">
            종료일
            <input
              type="date"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
            />
          </label>
          {tab === "dashboard" && (
            <label className="text-sm">
              모델 필터
              <input
                type="text"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                placeholder="예: claude-sonnet-4-5"
                className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded w-48"
              />
            </label>
          )}
          <ProjectSelector
            selectedProjectId={selectedProjectId}
            onProjectChange={(id, name) => {
              setSelectedProjectId(id);
              setSelectedProjectName(name ?? "");
            }}
          />
        </div>
      )}

      {tab === "dashboard" && (
        <>
          <h2 className="text-base font-semibold mb-3">요약</h2>
          <SummaryCard from={from} to={to} projectId={selectedProjectId} />

          <h2 className="text-base font-semibold mb-3 mt-6">일별 비용 추이</h2>
          <DailyChart
            from={from}
            to={to}
            model={model || undefined}
            projectId={selectedProjectId}
          />

          <h2 className="text-base font-semibold mb-3 mt-6">일별 통계</h2>
          <DailyStatsTable
            from={from}
            to={to}
            model={model || undefined}
            projectId={selectedProjectId}
          />

          {selectedProjectId != null && (
            <>
              <h2 className="text-base font-semibold mb-3 mt-6">세션별 토큰 효율</h2>
              <SessionEfficiencyChart data={sessionData} />
              <ModelCostTable data={sessionData} />

              <h2 className="text-base font-semibold mb-3 mt-6">모델 전환 절감 제안</h2>
              <AdvisorCard projectId={selectedProjectId} />
            </>
          )}
        </>
      )}

      {tab === "analysis" && (
        <>
          <h2 className="text-base font-semibold mb-3">모델별 비용 비중</h2>
          <ModelPieChart from={from} to={to} projectId={selectedProjectId} />
        </>
      )}

      {tab === "projects" && <ProjectManagement />}

      {tab === "events" && <EventList projectId={selectedProjectId} />}
    </div>
  );
}
