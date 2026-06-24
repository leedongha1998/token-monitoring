import type { EventPage } from "../types/events";

export async function fetchEvents(params: {
  from: string;
  to: string;
  projectId?: number;
  model?: string;
  page?: number;
  size?: number;
}): Promise<EventPage> {
  const query = new URLSearchParams({ from: params.from, to: params.to });
  if (params.projectId !== undefined)
    query.set("projectId", String(params.projectId));
  if (params.model) query.set("model", params.model);
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 50));
  const res = await fetch(`/v1/events?${query}`);
  if (!res.ok) throw new Error(`이벤트 조회 실패: ${res.status}`);
  return res.json();
}
