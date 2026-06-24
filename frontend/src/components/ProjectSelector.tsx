import { useEffect, useState } from "react";
import { fetchProjects } from "../api/projects";
import type { Project } from "../types/project";

interface Props {
  selectedProjectId?: number;
  onProjectChange: (projectId: number | undefined, projectName?: string) => void;
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
          onProjectChange(data[0].id, data[0].name);
        }
      })
      .catch((e: unknown) => setError(String(e)))
      .finally(() => setLoading(false));
    // onProjectChange intentionally omitted: only run on mount to set default
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) {
    return (
      <label className="text-sm">
        프로젝트
        <select
          disabled
          className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
        >
          <option>로딩 중…</option>
        </select>
      </label>
    );
  }

  if (error) {
    return <span className="text-sm text-red-500">프로젝트 오류: {error}</span>;
  }

  return (
    <label className="text-sm">
      프로젝트
      <select
        value={selectedProjectId ?? ""}
        onChange={(e) => {
          const val = e.target.value;
          if (val === "") {
            onProjectChange(undefined, undefined);
          } else {
            const id = Number(val);
            const project = projects.find((p) => p.id === id);
            onProjectChange(id, project?.name);
          }
        }}
        className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
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
