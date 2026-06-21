import { useEffect, useState } from "react";
import { fetchProjects } from "../api/projects";
import type { Project } from "../types/project";

interface Props {
  selectedProjectId?: number;
  onProjectChange: (projectId: number | undefined) => void;
}

export function ProjectSelector({ selectedProjectId, onProjectChange }: Props) {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchProjects()
      .then((data) => {
        setProjects(data);
        if (data.length > 0) {
          onProjectChange(data[0].id);
        }
      })
      .catch((e: unknown) => setError(String(e)))
      .finally(() => setLoading(false));
    // onProjectChange intentionally omitted: only run on mount to set default
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) {
    return (
      <label style={{ fontSize: 14 }}>
        프로젝트
        <select
          disabled
          style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
        >
          <option>로딩 중…</option>
        </select>
      </label>
    );
  }

  if (error) {
    return (
      <span style={{ fontSize: 14, color: "red" }}>프로젝트 오류: {error}</span>
    );
  }

  return (
    <label style={{ fontSize: 14 }}>
      프로젝트
      <select
        value={selectedProjectId ?? ""}
        onChange={(e) => {
          const val = e.target.value;
          onProjectChange(val === "" ? undefined : Number(val));
        }}
        style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
      >
        <option value="">전체</option>
        {projects.map((p) => (
          <option key={p.id} value={p.id}>
            {p.name}
          </option>
        ))}
      </select>
    </label>
  );
}
