export interface BudgetInfo {
  id: number;
  projectId: number;
  yearMonth: string;
  monthlyBudgetUsd: number;
}

export async function fetchBudget(
  projectId: number,
  yearMonth: string,
): Promise<BudgetInfo | null> {
  const res = await fetch(
    `/v1/projects/${projectId}/budget?yearMonth=${yearMonth}`,
  );
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`예산 조회 실패: ${res.status}`);
  return res.json() as Promise<BudgetInfo>;
}

export async function setBudget(
  projectId: number,
  yearMonth: string,
  monthlyBudgetUsd: number,
): Promise<BudgetInfo> {
  const res = await fetch(`/v1/projects/${projectId}/budget`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ yearMonth, monthlyBudgetUsd }),
  });
  if (!res.ok) throw new Error(`예산 설정 실패: ${res.status}`);
  return res.json() as Promise<BudgetInfo>;
}
