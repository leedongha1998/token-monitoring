import type { DailyStats, SummaryStats } from "../types/stats";

export async function fetchDailyStats(params: {
  from: string;
  to: string;
  projectId?: number;
  model?: string;
}): Promise<DailyStats[]> {
  const query = new URLSearchParams({ from: params.from, to: params.to });
  if (params.projectId !== undefined)
    query.set("projectId", String(params.projectId));
  if (params.model) query.set("model", params.model);
  const res = await fetch(`/v1/stats/daily?${query}`);
  if (!res.ok) throw new Error(`일별 통계 조회 실패: ${res.status}`);
  return res.json() as Promise<DailyStats[]>;
}

export async function fetchSummaryStats(params: {
  from: string;
  to: string;
}): Promise<SummaryStats> {
  const query = new URLSearchParams({ from: params.from, to: params.to });
  const res = await fetch(`/v1/stats/summary?${query}`);
  if (!res.ok) throw new Error(`요약 통계 조회 실패: ${res.status}`);
  return res.json() as Promise<SummaryStats>;
}
