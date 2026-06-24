import { useEffect, useState } from "react";
import { fetchDailyStats } from "../api/stats";
import type { DailyStats } from "../types/stats";

interface Props {
  from: string;
  to: string;
  projectId?: number;
}

const MODEL_COLORS = [
  "#4f86f7",
  "#f7964f",
  "#4fc74f",
  "#c74f4f",
  "#9b4fc7",
  "#4fc7c7",
  "#c7c74f",
];

const CX = 180;
const CY = 180;
const R = 150;

export function ModelPieChart({ from, to, projectId }: Props) {
  const [rows, setRows] = useState<DailyStats[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchDailyStats({ from, to, projectId })
      .then(setRows)
      .catch((e: unknown) =>
        setError(e instanceof Error ? e.message : "알 수 없는 오류"),
      )
      .finally(() => setLoading(false));
  }, [from, to, projectId]);

  if (loading) return <p className="text-gray-500 text-sm">파이 차트 로딩 중...</p>;
  if (error) return <p className="text-red-500 text-sm">오류: {error}</p>;

  const modelCosts = new Map<string, number>();
  for (const row of rows) {
    modelCosts.set(row.model, (modelCosts.get(row.model) ?? 0) + parseFloat(row.totalCost));
  }

  const entries = Array.from(modelCosts.entries())
    .filter(([, cost]) => cost > 0)
    .sort((a, b) => b[1] - a[1]);

  if (entries.length === 0) {
    return <p className="text-gray-400">데이터 없음</p>;
  }

  const total = entries.reduce((sum, [, c]) => sum + c, 0);

  let angle = -Math.PI / 2;
  const slices = entries.map(([model, cost], i) => {
    const fraction = cost / total;
    const startAngle = angle;
    const endAngle = angle + fraction * 2 * Math.PI;
    angle = endAngle;

    const x1 = CX + R * Math.cos(startAngle);
    const y1 = CY + R * Math.sin(startAngle);
    const x2 = CX + R * Math.cos(endAngle);
    const y2 = CY + R * Math.sin(endAngle);
    const largeArc = fraction > 0.5 ? 1 : 0;

    const path = `M ${CX} ${CY} L ${x1.toFixed(2)} ${y1.toFixed(2)} A ${R} ${R} 0 ${largeArc} 1 ${x2.toFixed(2)} ${y2.toFixed(2)} Z`;

    return { model, cost, fraction, path, color: MODEL_COLORS[i % MODEL_COLORS.length] };
  });

  return (
    <div className="flex flex-wrap gap-10 mb-6">
      <svg width={360} height={360} style={{ display: "block", flexShrink: 0 }}>
        {slices.map(({ model, cost, fraction, path, color }) => (
          <path key={model} d={path} fill={color} stroke="white" strokeWidth={2}>
            <title>{`${model}: $${cost.toFixed(4)} (${(fraction * 100).toFixed(1)}%)`}</title>
          </path>
        ))}
      </svg>

      <div className="flex flex-col gap-2 mt-4 text-sm min-w-[240px]">
        {slices.map(({ model, cost, fraction, color }) => (
          <div key={model} className="flex items-center gap-2">
            <span
              className="inline-block w-3 h-3 rounded-sm flex-shrink-0"
              style={{ background: color }}
            />
            <span className="text-gray-700 flex-1 truncate">{model}</span>
            <span className="text-gray-500">${cost.toFixed(4)}</span>
            <span className="text-gray-400 w-14 text-right">
              {(fraction * 100).toFixed(1)}%
            </span>
          </div>
        ))}
        <div className="border-t border-gray-200 pt-2 mt-1 font-semibold text-gray-700">
          합계: ${total.toFixed(4)}
        </div>
      </div>
    </div>
  );
}
