import type { ModelSwitchAdvice } from "../types/advisor";

const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export async function fetchModelSwitchAdvice(projectId: number): Promise<ModelSwitchAdvice[]> {
  const res = await fetch(`${BASE}/v1/advisor/model-switch?projectId=${projectId}`);
  if (!res.ok) return [];
  return res.json();
}
