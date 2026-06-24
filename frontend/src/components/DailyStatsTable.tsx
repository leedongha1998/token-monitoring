import { useEffect, useState } from "react";
import { fetchDailyStats } from "../api/stats";
import type { DailyStats } from "../types/stats";

interface Props {
  from: string;
  to: string;
  projectId?: number;
  model?: string;
}

export function DailyStatsTable({ from, to, projectId, model }: Props) {
  const [rows, setRows] = useState<DailyStats[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchDailyStats({ from, to, projectId, model })
      .then(setRows)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : "알 수 없는 오류"),
      )
      .finally(() => setLoading(false));
  }, [from, to, projectId, model]);

  if (loading) return <p className="text-gray-500 text-sm">일별 통계 로딩 중...</p>;
  if (error) return <p className="text-red-500 text-sm">오류: {error}</p>;

  return (
    <table className="w-full border-collapse text-sm">
      <thead>
        <tr className="bg-gray-50 text-left">
          {["날짜", "프로젝트 ID", "모델", "입력 토큰", "출력 토큰", "비용 (USD)"].map((h) => (
            <th key={h} className="px-3 py-2.5 font-semibold border-b border-gray-200">
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.length === 0 ? (
          <tr>
            <td colSpan={6} className="px-3 py-4 text-center text-gray-400">
              데이터 없음
            </td>
          </tr>
        ) : (
          rows.map((row) => (
            <tr
              key={`${row.date}-${row.projectId}-${row.model}`}
              className="border-b border-gray-100 hover:bg-gray-50"
            >
              <td className="px-3 py-2.5">{row.date}</td>
              <td className="px-3 py-2.5">{row.projectId}</td>
              <td className="px-3 py-2.5">{row.model}</td>
              <td className="px-3 py-2.5">{row.totalInputTokens.toLocaleString()}</td>
              <td className="px-3 py-2.5">{row.totalOutputTokens.toLocaleString()}</td>
              <td className="px-3 py-2.5">${row.totalCost}</td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}
