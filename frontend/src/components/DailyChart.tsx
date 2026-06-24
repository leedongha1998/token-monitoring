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
const CHART_HEIGHT = 220;
const PAD_LEFT = 72;
const PAD_RIGHT = 16;
const PAD_TOP = 16;
const PAD_BOTTOM = 40;

const AUTO_REFRESH_INTERVAL_MS = 30_000;

const MODEL_COLORS = [
  "#4f86f7",
  "#f7964f",
  "#4fc74f",
  "#c74f4f",
  "#9b4fc7",
  "#4fc7c7",
  "#c7c74f",
];

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

  if (loading) return <p className="text-gray-500 text-sm">차트 로딩 중...</p>;
  if (error) return <p className="text-red-500 text-sm">오류: {error}</p>;
  if (rows.length === 0)
    return <p className="text-gray-400 mb-6">차트 데이터 없음</p>;

  const dateModelMap = new Map<string, Map<string, number>>();
  for (const row of rows) {
    if (!dateModelMap.has(row.date)) dateModelMap.set(row.date, new Map());
    const mm = dateModelMap.get(row.date)!;
    mm.set(row.model, (mm.get(row.model) ?? 0) + parseFloat(row.totalCost));
  }

  const allModels = Array.from(new Set(rows.map((r) => r.model))).sort();
  const dates = Array.from(dateModelMap.keys()).sort();
  const maxCost = Math.max(
    ...dates.map((d) =>
      Array.from(dateModelMap.get(d)!.values()).reduce((a, b) => a + b, 0),
    ),
    0.0001,
  );

  const plotW = CHART_WIDTH - PAD_LEFT - PAD_RIGHT;
  const plotH = CHART_HEIGHT - PAD_TOP - PAD_BOTTOM;
  const barWidth = Math.max(4, Math.floor(plotW / dates.length) - 2);

  return (
    <div className="mb-6 overflow-x-auto">
      <svg
        width={CHART_WIDTH}
        height={CHART_HEIGHT}
        style={{ display: "block", maxWidth: "100%" }}
      >
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
              <text x={PAD_LEFT - 6} y={y + 4} textAnchor="end" fontSize={10} fill="#999">
                ${val}
              </text>
            </g>
          );
        })}

        {dates.map((date, i) => {
          const mm = dateModelMap.get(date)!;
          const x =
            PAD_LEFT +
            (i / dates.length) * plotW +
            (plotW / dates.length - barWidth) / 2;
          const showLabel =
            dates.length <= 14 || i % Math.ceil(dates.length / 14) === 0;

          let accumulated = 0;
          const segments = allModels
            .filter((m) => (mm.get(m) ?? 0) > 0)
            .map((m) => {
              const cost = mm.get(m)!;
              const barH = (cost / maxCost) * plotH;
              const y = PAD_TOP + plotH - accumulated - barH;
              accumulated += barH;
              return { m, cost, barH, y };
            });

          return (
            <g key={date}>
              {segments.map(({ m, cost, barH, y }) => (
                <rect
                  key={m}
                  x={x}
                  y={y}
                  width={barWidth}
                  height={barH}
                  fill={MODEL_COLORS[allModels.indexOf(m) % MODEL_COLORS.length]}
                  rx={2}
                >
                  <title>{`${date} · ${m}: $${cost.toFixed(4)}`}</title>
                </rect>
              ))}
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

        <line
          x1={PAD_LEFT}
          x2={CHART_WIDTH - PAD_RIGHT}
          y1={PAD_TOP + plotH}
          y2={PAD_TOP + plotH}
          stroke="#ccc"
          strokeWidth={1}
        />
      </svg>

      {allModels.length > 0 && (
        <div className="flex flex-wrap gap-3 mt-2 text-xs text-gray-600">
          {allModels.map((m, i) => (
            <div key={m} className="flex items-center gap-1">
              <span
                className="inline-block w-3 h-3 rounded-sm"
                style={{ background: MODEL_COLORS[i % MODEL_COLORS.length] }}
              />
              {m}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
