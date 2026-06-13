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

  if (loading) return <p>일별 통계 로딩 중...</p>;
  if (error) return <p style={{ color: "red" }}>오류: {error}</p>;

  return (
    <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 14 }}>
      <thead>
        <tr style={{ background: "#f5f5f5", textAlign: "left" }}>
          <th style={thStyle}>날짜</th>
          <th style={thStyle}>프로젝트 ID</th>
          <th style={thStyle}>모델</th>
          <th style={thStyle}>입력 토큰</th>
          <th style={thStyle}>출력 토큰</th>
          <th style={thStyle}>비용 (USD)</th>
        </tr>
      </thead>
      <tbody>
        {rows.length === 0 ? (
          <tr>
            <td colSpan={6} style={{ padding: 16, textAlign: "center", color: "#999" }}>
              데이터 없음
            </td>
          </tr>
        ) : (
          rows.map((row) => (
            <tr key={`${row.date}-${row.projectId}-${row.model}`} style={{ borderBottom: "1px solid #eee" }}>
              <td style={tdStyle}>{row.date}</td>
              <td style={tdStyle}>{row.projectId}</td>
              <td style={tdStyle}>{row.model}</td>
              <td style={tdStyle}>{row.totalInputTokens.toLocaleString()}</td>
              <td style={tdStyle}>{row.totalOutputTokens.toLocaleString()}</td>
              <td style={tdStyle}>${row.totalCost}</td>
            </tr>
          ))
        )}
      </tbody>
    </table>
  );
}

const thStyle: React.CSSProperties = { padding: "10px 12px", fontWeight: 600 };
const tdStyle: React.CSSProperties = { padding: "10px 12px" };
