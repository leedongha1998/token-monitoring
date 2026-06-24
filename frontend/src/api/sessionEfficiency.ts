import type { SessionEfficiency } from "../types/advisor";

const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export async function fetchSessionEfficiency(
  projectId: number,
  from: string,
  to: string
): Promise<SessionEfficiency[]> {
  const res = await fetch(
    `${BASE}/v1/stats/session-efficiency?projectId=${projectId}&from=${from}&to=${to}`
  );
  if (!res.ok) return [];
  return res.json();
}
