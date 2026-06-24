import type { AlertItem } from "../types/alert";

const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export async function fetchAlerts(projectId: number): Promise<AlertItem[]> {
  const res = await fetch(`${BASE}/v1/alerts?projectId=${projectId}`);
  if (!res.ok) return [];
  return res.json();
}

export async function markAlertRead(alertId: number): Promise<void> {
  await fetch(`${BASE}/v1/alerts/${alertId}/read`, { method: "POST" });
}
