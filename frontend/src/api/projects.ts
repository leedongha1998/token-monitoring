import type { Project, ApiKey, IssuedKey } from "../types/project";

export async function fetchProjects(): Promise<Project[]> {
  const res = await fetch("/v1/projects?size=100");
  if (!res.ok) throw new Error(`프로젝트 목록 조회 실패: ${res.status}`);
  const page = await res.json();
  return page.content as Project[];
}

export async function createProject(
  name: string,
  description: string
): Promise<Project> {
  const res = await fetch("/v1/projects", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, description }),
  });
  if (!res.ok) throw new Error(`프로젝트 생성 실패: ${res.status}`);
  return res.json();
}

export async function fetchApiKeys(projectId: number): Promise<ApiKey[]> {
  const res = await fetch(`/v1/projects/${projectId}/api-keys`);
  if (!res.ok) throw new Error(`API 키 목록 조회 실패: ${res.status}`);
  return res.json();
}

export async function issueApiKey(projectId: number): Promise<IssuedKey> {
  const res = await fetch(`/v1/projects/${projectId}/api-keys`, {
    method: "POST",
  });
  if (!res.ok) throw new Error(`API 키 발급 실패: ${res.status}`);
  return res.json();
}

export async function deactivateApiKey(keyId: number): Promise<void> {
  const res = await fetch(`/v1/api-keys/${keyId}`, { method: "DELETE" });
  if (!res.ok) throw new Error(`API 키 비활성화 실패: ${res.status}`);
}
