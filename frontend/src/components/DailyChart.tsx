import { useEffect, useState } from "react";
import { fetchDailyStats } from "../api/stats";
import type { DailyStats } from "../types/stats";

interface Props {
  from: string;
  to: string;
  model?: string;
  projectId?: number;
}

const CHART_WIDTH = 880;
const CHART_HEIGHT = 200;
const PAD_LEFT = 72;
const PAD_RIGHT = 16;
const PAD_TOP = 16;
const PAD_BOTTOM = 40;

const AUTO_REFRESH_INTERVAL_MS = 30_000;

function getTodayDateString(): string {
  return new Date().toISOString().slice(0, 10);
}

function isToday(from: string, to: string): boolean {
  const today = getTodayDateString();
  return from === today && to === today;
}

export function DailyChart({ from, to, model, projectId }: Props) {
  const [rows, setRows] = useState<DailyStats[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchDailyStats({ from, to, model, projectId })
      .then(setRows)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : "알 수 없는 오류"),
      )
      .finally(() => setLoading(false));
  }, [from, to, model, projectId]);

  useEffect(() => {
    if (!isToday(from, to)) return;

    const intervalId = setInterval(() => {
      fetchDailyStats({ from, to, model, projectId })
        .then(setRows)
        .catch(() => {
          // 자동 새로고침 실패는 조용히 무시 — 다음 주기에 재시도
        });
    }, AUTO_REFRESH_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [from, to, model, projectId]);

  if (loading) return <p>차트 로딩 중...</p>;
  if (error) return <p style={{ color: "red" }}>오류: {error}</p>;
  if (rows.length === 0)
    return (
      <p style={{ color: "#999", marginBottom: 24 }}>차트 데이터 없음</p>
    );

  const costByDate = new Map<string, number>();
  for (const row of rows) {
    const prev = costByDate.get(row.date) ?? 0;
    costByDate.set(row.date, prev + parseFloat(row.totalCost));
  }

  const dates = Array.from(costByDate.keys()).sort();
  const costs = dates.map((d) => costByDate.get(d) ?? 0);
  const maxCost = Math.max(...costs, 0.0001);

  const plotW = CHART_WIDTH - PAD_LEFT - PAD_RIGHT;
  const plotH = CHART_HEIGHT - PAD_TOP - PAD_BOTTOM;
  const barWidth = Math.max(4, Math.floor(plotW / dates.length) - 2);

  return (
    <div style={{ marginBottom: 24, overflowX: "auto" }}>
      <svg
        width={CHART_WIDTH}
        height={CHART_HEIGHT}
        style={{ display: "block", maxWidth: "100%" }}
      >
        {/* Y축 그리드 및 레이블 */}
        {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
          const y = PAD_TOP + plotH * (1 - ratio);
          const val = (maxCost * ratio).toFixed(4);
          return (
            <g key={ratio}>
              <line
                x1={PAD_LEFT}
                x2={CHART_WIDTH - PAD_RIGHT}
                y1={y}
                y2={y}
                stroke="#eee"
                strokeWidth={1}
              />
              <text
                x={PAD_LEFT - 6}
                y={y + 4}
                textAnchor="end"
                fontSize={10}
                fill="#999"
              >
                ${val}
              </text>
            </g>
          );
        })}

        {/* 막대 */}
        {dates.map((date, i) => {
          const cost = costs[i];
          const barH = (cost / maxCost) * plotH;
          const x = PAD_LEFT + (i / dates.length) * plotW + (plotW / dates.length - barWidth) / 2;
          const y = PAD_TOP + plotH - barH;
          const showLabel = dates.length <= 14 || i % Math.ceil(dates.length / 14) === 0;
          return (
            <g key={date}>
              <rect
                x={x}
                y={y}
                width={barWidth}
                height={barH}
                fill="#4f86f7"
                rx={2}
              >
                <title>{date}: ${cost.toFixed(4)}</title>
              </rect>
              {showLabel && (
                <text
                  x={x + barWidth / 2}
                  y={CHART_HEIGHT - PAD_BOTTOM + 14}
                  textAnchor="middle"
                  fontSize={10}
                  fill="#666"
                >
                  {date.slice(5)}
                </text>
              )}
            </g>
          );
        })}

        {/* X축 */}
        <line
          x1={PAD_LEFT}
          x2={CHART_WIDTH - PAD_RIGHT}
          y1={PAD_TOP + plotH}
          y2={PAD_TOP + plotH}
          stroke="#ccc"
          strokeWidth={1}
        />
      </svg>
    </div>
  );
}
