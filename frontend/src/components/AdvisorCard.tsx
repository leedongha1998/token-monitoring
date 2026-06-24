import { useEffect, useState } from "react";
import { fetchModelSwitchAdvice } from "../api/advisor";
import type { ModelSwitchAdvice } from "../types/advisor";

interface Props {
  projectId: number;
}

export function AdvisorCard({ projectId }: Props) {
  const [advice, setAdvice] = useState<ModelSwitchAdvice[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    fetchModelSwitchAdvice(projectId)
      .then(setAdvice)
      .catch(() => setAdvice([]))
      .finally(() => setLoading(false));
  }, [projectId]);

  if (loading) {
    return <div className="text-sm text-gray-400 py-4">절감 제안 로딩 중...</div>;
  }

  if (advice.length === 0) {
    return (
      <div className="text-sm text-gray-400 py-4 text-center">
        최근 30일간 전환 가능한 모델 데이터가 없습니다.
      </div>
    );
  }

  const totalSavings = advice.reduce((sum, a) => sum + parseFloat(a.projectedSavings), 0);

  return (
    <div>
      <div className="bg-green-50 border border-green-200 rounded-lg px-4 py-3 mb-4">
        <p className="text-sm text-green-700 font-medium">
          모델 전환 시 월 최대 <span className="font-bold">${totalSavings.toFixed(4)}</span> 절감
          가능합니다
        </p>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full text-sm border-collapse">
          <thead>
            <tr className="bg-gray-50">
              <th className="text-left px-3 py-2 border border-gray-200 font-medium text-gray-600">
                현재 모델
              </th>
              <th className="text-left px-3 py-2 border border-gray-200 font-medium text-gray-600">
                추천 모델
              </th>
              <th className="text-right px-3 py-2 border border-gray-200 font-medium text-gray-600">
                현재 비용
              </th>
              <th className="text-right px-3 py-2 border border-gray-200 font-medium text-gray-600">
                예상 절감
              </th>
            </tr>
          </thead>
          <tbody>
            {advice.map((a, i) => (
              <tr key={i} className="hover:bg-gray-50">
                <td className="px-3 py-2 border border-gray-200 font-mono text-xs">{a.currentModel}</td>
                <td className="px-3 py-2 border border-gray-200 font-mono text-xs text-green-700">
                  {a.suggestedModel}
                </td>
                <td className="px-3 py-2 border border-gray-200 text-right">
                  ${parseFloat(a.currentMonthlyCost).toFixed(4)}
                </td>
                <td className="px-3 py-2 border border-gray-200 text-right font-semibold text-green-600">
                  -${parseFloat(a.projectedSavings).toFixed(4)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
