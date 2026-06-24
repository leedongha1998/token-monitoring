import type { SessionEfficiency } from "../types/advisor";

interface Props {
  data: SessionEfficiency[];
}

const CHART_W = 600;
const CHART_H = 200;
const PAD = { top: 16, right: 16, bottom: 40, left: 60 };
const INNER_W = CHART_W - PAD.left - PAD.right;
const INNER_H = CHART_H - PAD.top - PAD.bottom;

export function SessionEfficiencyChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <div className="text-sm text-gray-400 py-8 text-center">
        세션 데이터가 없습니다
      </div>
    );
  }

  const sorted = [...data].sort(
    (a, b) => new Date(a.sessionDate).getTime() - new Date(b.sessionDate).getTime()
  );
  const totals = sorted.map((d) => d.totalInputTokens + d.totalOutputTokens);
  const maxTokens = Math.max(...totals, 1);

  const points = sorted.map((_, i) => {
    const x = PAD.left + (i / Math.max(sorted.length - 1, 1)) * INNER_W;
    const y = PAD.top + INNER_H - (totals[i] / maxTokens) * INNER_H;
    return { x, y, d: sorted[i], total: totals[i] };
  });

  const polyline = points.map((p) => `${p.x},${p.y}`).join(" ");

  return (
    <div className="overflow-x-auto">
      <svg
        viewBox={`0 0 ${CHART_W} ${CHART_H}`}
        className="w-full"
        style={{ minWidth: 320, maxHeight: 220 }}
        aria-label="세션 토큰 효율 차트"
      >
        <line
          x1={PAD.left}
          y1={PAD.top}
          x2={PAD.left}
          y2={PAD.top + INNER_H}
          stroke="#e5e7eb"
          strokeWidth={1}
        />
        <line
          x1={PAD.left}
          y1={PAD.top + INNER_H}
          x2={PAD.left + INNER_W}
          y2={PAD.top + INNER_H}
          stroke="#e5e7eb"
          strokeWidth={1}
        />
        {[0, 0.25, 0.5, 0.75, 1].map((ratio) => {
          const y = PAD.top + INNER_H - ratio * INNER_H;
          return (
            <g key={ratio}>
              <line
                x1={PAD.left}
                y1={y}
                x2={PAD.left + INNER_W}
                y2={y}
                stroke="#f3f4f6"
                strokeWidth={1}
              />
              <text
                x={PAD.left - 6}
                y={y + 4}
                textAnchor="end"
                fontSize={9}
                fill="#9ca3af"
              >
                {Math.round(ratio * maxTokens).toLocaleString()}
              </text>
            </g>
          );
        })}
        <polyline
          points={polyline}
          fill="none"
          stroke="#3b82f6"
          strokeWidth={2}
          strokeLinejoin="round"
        />
        {points.map((p, i) => (
          <g key={i}>
            <circle cx={p.x} cy={p.y} r={3} fill="#3b82f6" />
            <title>
              {new Date(p.d.sessionDate).toLocaleDateString("ko-KR")} — {p.d.model} —{" "}
              {p.total.toLocaleString()} tokens
            </title>
          </g>
        ))}
        {sorted.length <= 10 &&
          points.map((p, i) => (
            <text
              key={i}
              x={p.x}
              y={PAD.top + INNER_H + 14}
              textAnchor="middle"
              fontSize={9}
              fill="#6b7280"
            >
              {new Date(p.d.sessionDate).toLocaleDateString("ko-KR", {
                month: "numeric",
                day: "numeric",
              })}
            </text>
          ))}
      </svg>
    </div>
  );
}
