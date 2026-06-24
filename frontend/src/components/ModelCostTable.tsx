import type { SessionEfficiency } from "../types/advisor";

interface Props {
  data: SessionEfficiency[];
}

export function ModelCostTable({ data }: Props) {
  if (data.length === 0) {
    return null;
  }

  const sorted = [...data].sort(
    (a, b) => parseFloat(b.costUsd) - parseFloat(a.costUsd)
  );

  const totalCost = sorted.reduce((sum, d) => sum + parseFloat(d.costUsd), 0);

  return (
    <div className="overflow-x-auto mt-4">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="bg-gray-50">
            <th className="text-left px-3 py-2 border border-gray-200 font-medium text-gray-600">
              세션 ID
            </th>
            <th className="text-left px-3 py-2 border border-gray-200 font-medium text-gray-600">
              모델
            </th>
            <th className="text-right px-3 py-2 border border-gray-200 font-medium text-gray-600">
              입력
            </th>
            <th className="text-right px-3 py-2 border border-gray-200 font-medium text-gray-600">
              출력
            </th>
            <th className="text-right px-3 py-2 border border-gray-200 font-medium text-gray-600">
              비용
            </th>
          </tr>
        </thead>
        <tbody>
          {sorted.map((d, i) => (
            <tr key={i} className="hover:bg-gray-50">
              <td className="px-3 py-2 border border-gray-200 font-mono text-xs max-w-xs truncate">
                {d.sessionId}
              </td>
              <td className="px-3 py-2 border border-gray-200 font-mono text-xs">{d.model}</td>
              <td className="px-3 py-2 border border-gray-200 text-right text-xs">
                {d.totalInputTokens.toLocaleString()}
              </td>
              <td className="px-3 py-2 border border-gray-200 text-right text-xs">
                {d.totalOutputTokens.toLocaleString()}
              </td>
              <td className="px-3 py-2 border border-gray-200 text-right font-mono text-xs">
                ${parseFloat(d.costUsd).toFixed(6)}
              </td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="bg-gray-50 font-semibold">
            <td colSpan={4} className="px-3 py-2 border border-gray-200 text-right text-sm">
              합계
            </td>
            <td className="px-3 py-2 border border-gray-200 text-right font-mono text-sm">
              ${totalCost.toFixed(6)}
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
}
