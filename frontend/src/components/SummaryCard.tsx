import { useEffect, useState } from "react";
import { fetchSummaryStats } from "../api/stats";
import type { SummaryStats } from "../types/stats";

interface Props {
  from: string;
  to: string;
}

export function SummaryCard({ from, to }: Props) {
  const [data, setData] = useState<SummaryStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchSummaryStats({ from, to })
      .then(setData)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : "알 수 없는 오류"),
      )
      .finally(() => setLoading(false));
  }, [from, to]);

  if (loading) return <p>요약 통계 로딩 중...</p>;
  if (error) return <p style={{ color: "red" }}>오류: {error}</p>;
  if (!data) return null;

  return (
    <div
      style={{
        border: "1px solid #ddd",
        borderRadius: 8,
        padding: 20,
        marginBottom: 24,
        display: "flex",
        gap: 32,
      }}
    >
      <div>
        <div style={{ fontSize: 12, color: "#666" }}>총 입력 토큰</div>
        <div style={{ fontSize: 24, fontWeight: 600 }}>
          {data.totalInputTokens.toLocaleString()}
        </div>
      </div>
      <div>
        <div style={{ fontSize: 12, color: "#666" }}>총 출력 토큰</div>
        <div style={{ fontSize: 24, fontWeight: 600 }}>
          {data.totalOutputTokens.toLocaleString()}
        </div>
      </div>
      <div>
        <div style={{ fontSize: 12, color: "#666" }}>총 비용 (USD)</div>
        <div style={{ fontSize: 24, fontWeight: 600 }}>${data.totalCost}</div>
      </div>
    </div>
  );
}
