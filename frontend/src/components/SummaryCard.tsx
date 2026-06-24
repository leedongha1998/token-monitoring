import { useEffect, useState } from "react";
import { fetchBudget } from "../api/budget";
import { fetchSummaryStats } from "../api/stats";
import type { SummaryStats } from "../types/stats";

interface Props {
  from: string;
  to: string;
  projectId?: number;
}

export function SummaryCard({ from, to, projectId }: Props) {
  const [data, setData] = useState<SummaryStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [budgetUsd, setBudgetUsd] = useState<number | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchSummaryStats({ from, to, projectId })
      .then(setData)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : "알 수 없는 오류"),
      )
      .finally(() => setLoading(false));
  }, [from, to, projectId]);

  useEffect(() => {
    if (!projectId) {
      setBudgetUsd(null);
      return;
    }
    const now = new Date();
    const yearMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
    fetchBudget(projectId, yearMonth)
      .then((b) => setBudgetUsd(b ? b.monthlyBudgetUsd : null))
      .catch(() => setBudgetUsd(null));
  }, [projectId]);

  if (loading) return <p className="text-gray-500 text-sm">요약 통계 로딩 중...</p>;
  if (error) return <p className="text-red-500 text-sm">오류: {error}</p>;
  if (!data) return null;

  const usedCost = parseFloat(data.totalCost);
  const usagePct = budgetUsd ? Math.min((usedCost / budgetUsd) * 100, 100) : null;

  return (
    <div className="border border-gray-200 rounded-lg p-5 mb-6">
      <div className="flex gap-8 flex-wrap mb-4">
        <div>
          <div className="text-xs text-gray-500">총 입력 토큰</div>
          <div className="text-2xl font-semibold">
            {data.totalInputTokens.toLocaleString()}
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-500">총 출력 토큰</div>
          <div className="text-2xl font-semibold">
            {data.totalOutputTokens.toLocaleString()}
          </div>
        </div>
        <div>
          <div className="text-xs text-gray-500">총 비용 (USD)</div>
          <div className="text-2xl font-semibold">${data.totalCost}</div>
        </div>
        {budgetUsd !== null && (
          <div>
            <div className="text-xs text-gray-500">월 예산 (USD)</div>
            <div className="text-2xl font-semibold">${budgetUsd.toFixed(2)}</div>
          </div>
        )}
      </div>
      {usagePct !== null && (
        <div>
          <div className="flex justify-between text-xs text-gray-500 mb-1">
            <span>예산 사용률</span>
            <span>{usagePct.toFixed(1)}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className={`h-2 rounded-full transition-all ${usagePct > 80 ? "bg-orange-500" : "bg-blue-500"}`}
              style={{ width: `${usagePct}%` }}
            />
          </div>
        </div>
      )}
    </div>
  );
}
