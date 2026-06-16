import { useState, useEffect, useCallback } from "react";
import type { Project, ApiKey, IssuedKey } from "../types/project";
import {
  fetchProjects,
  createProject,
  fetchApiKeys,
  issueApiKey,
  deactivateApiKey,
} from "../api/projects";

export function ProjectManagement() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [newName, setNewName] = useState("");
  const [newDesc, setNewDesc] = useState("");
  const [creating, setCreating] = useState(false);

  const [expandedProjectId, setExpandedProjectId] = useState<number | null>(null);
  const [apiKeys, setApiKeys] = useState<Record<number, ApiKey[]>>({});
  const [issuedKey, setIssuedKey] = useState<IssuedKey | null>(null);
  const [issuedForProject, setIssuedForProject] = useState<number | null>(null);

  const loadProjects = useCallback(async () => {
    try {
      setLoading(true);
      const data = await fetchProjects();
      setProjects(data);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadProjects();
  }, [loadProjects]);

  const handleCreate = async () => {
    if (!newName.trim()) return;
    setCreating(true);
    try {
      await createProject(newName.trim(), newDesc.trim());
      setNewName("");
      setNewDesc("");
      await loadProjects();
    } catch (e) {
      setError(String(e));
    } finally {
      setCreating(false);
    }
  };

  const handleToggle = async (projectId: number) => {
    if (expandedProjectId === projectId) {
      setExpandedProjectId(null);
      return;
    }
    setExpandedProjectId(projectId);
    if (!apiKeys[projectId]) {
      try {
        const keys = await fetchApiKeys(projectId);
        setApiKeys((prev) => ({ ...prev, [projectId]: keys }));
      } catch (e) {
        setError(String(e));
      }
    }
  };

  const handleIssueKey = async (projectId: number) => {
    try {
      const issued = await issueApiKey(projectId);
      setIssuedKey(issued);
      setIssuedForProject(projectId);
      const keys = await fetchApiKeys(projectId);
      setApiKeys((prev) => ({ ...prev, [projectId]: keys }));
    } catch (e) {
      setError(String(e));
    }
  };

  const handleDeactivate = async (projectId: number, keyId: number) => {
    try {
      await deactivateApiKey(keyId);
      const keys = await fetchApiKeys(projectId);
      setApiKeys((prev) => ({ ...prev, [projectId]: keys }));
    } catch (e) {
      setError(String(e));
    }
  };

  if (loading) return <div style={{ padding: 16, color: "#666" }}>로딩 중...</div>;

  return (
    <div>
      {error && (
        <div
          style={{
            color: "#721c24",
            marginBottom: 12,
            padding: "8px 12px",
            background: "#f8d7da",
            border: "1px solid #f5c6cb",
            borderRadius: 4,
            fontSize: 13,
          }}
        >
          오류: {error}
          <button
            onClick={() => setError(null)}
            style={{ marginLeft: 8, cursor: "pointer", background: "none", border: "none" }}
          >
            ×
          </button>
        </div>
      )}

      <div
        style={{
          background: "#f8f9fa",
          padding: 16,
          borderRadius: 8,
          marginBottom: 24,
          border: "1px solid #e9ecef",
        }}
      >
        <h3 style={{ fontSize: 14, fontWeight: 600, marginBottom: 12 }}>새 프로젝트 만들기</h3>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <input
            type="text"
            placeholder="프로젝트 이름 *"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            style={{
              padding: "6px 10px",
              fontSize: 14,
              border: "1px solid #ddd",
              borderRadius: 4,
              width: 200,
            }}
          />
          <input
            type="text"
            placeholder="설명 (선택)"
            value={newDesc}
            onChange={(e) => setNewDesc(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            style={{
              padding: "6px 10px",
              fontSize: 14,
              border: "1px solid #ddd",
              borderRadius: 4,
              width: 260,
            }}
          />
          <button
            onClick={handleCreate}
            disabled={creating || !newName.trim()}
            style={{
              padding: "6px 18px",
              fontSize: 14,
              background: "#0066cc",
              color: "#fff",
              border: "none",
              borderRadius: 4,
              cursor: creating || !newName.trim() ? "not-allowed" : "pointer",
              opacity: creating || !newName.trim() ? 0.6 : 1,
            }}
          >
            {creating ? "생성 중..." : "생성"}
          </button>
        </div>
      </div>

      {projects.length === 0 ? (
        <div style={{ color: "#666", fontSize: 14 }}>
          프로젝트가 없습니다. 위에서 첫 번째 프로젝트를 만들어보세요.
        </div>
      ) : (
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          {projects.map((project) => (
            <div
              key={project.id}
              style={{ border: "1px solid #e0e0e0", borderRadius: 8, overflow: "hidden" }}
            >
              <div
                style={{
                  padding: "12px 16px",
                  background: "#fff",
                  display: "flex",
                  alignItems: "center",
                  gap: 12,
                }}
              >
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: 600, fontSize: 15 }}>{project.name}</div>
                  {project.description && (
                    <div style={{ fontSize: 13, color: "#666", marginTop: 2 }}>
                      {project.description}
                    </div>
                  )}
                  <div
                    style={{
                      fontSize: 12,
                      color: project.active ? "#28a745" : "#dc3545",
                      marginTop: 4,
                    }}
                  >
                    {project.active ? "활성" : "비활성"} · ID: {project.id}
                  </div>
                </div>
                <button
                  onClick={() => handleToggle(project.id)}
                  style={{
                    padding: "5px 14px",
                    fontSize: 13,
                    background: expandedProjectId === project.id ? "#e9ecef" : "#fff",
                    border: "1px solid #ddd",
                    borderRadius: 4,
                    cursor: "pointer",
                  }}
                >
                  {expandedProjectId === project.id ? "닫기" : "API 키 관리"}
                </button>
              </div>

              {expandedProjectId === project.id && (
                <div
                  style={{
                    padding: "14px 16px",
                    background: "#fafafa",
                    borderTop: "1px solid #e0e0e0",
                  }}
                >
                  {issuedForProject === project.id && issuedKey && (
                    <div
                      style={{
                        marginBottom: 14,
                        padding: "10px 14px",
                        background: "#fff3cd",
                        border: "1px solid #ffc107",
                        borderRadius: 6,
                      }}
                    >
                      <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 6 }}>
                        발급된 API 키 — 이 화면에서만 표시됩니다
                      </div>
                      <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                        <code
                          style={{
                            fontSize: 12,
                            background: "#fff",
                            padding: "4px 8px",
                            borderRadius: 4,
                            border: "1px solid #ddd",
                            flex: 1,
                            wordBreak: "break-all",
                          }}
                        >
                          {issuedKey.plainKey}
                        </code>
                        <button
                          onClick={() => navigator.clipboard.writeText(issuedKey.plainKey)}
                          style={{
                            padding: "4px 10px",
                            fontSize: 12,
                            border: "1px solid #ddd",
                            borderRadius: 4,
                            cursor: "pointer",
                            background: "#fff",
                            whiteSpace: "nowrap",
                          }}
                        >
                          복사
                        </button>
                        <button
                          onClick={() => {
                            setIssuedKey(null);
                            setIssuedForProject(null);
                          }}
                          style={{
                            padding: "4px 8px",
                            fontSize: 14,
                            border: "none",
                            background: "none",
                            cursor: "pointer",
                            color: "#666",
                          }}
                        >
                          ×
                        </button>
                      </div>
                    </div>
                  )}

                  {!apiKeys[project.id] || apiKeys[project.id].length === 0 ? (
                    <div style={{ fontSize: 13, color: "#666", marginBottom: 10 }}>
                      발급된 API 키가 없습니다.
                    </div>
                  ) : (
                    <table
                      style={{
                        width: "100%",
                        borderCollapse: "collapse",
                        marginBottom: 12,
                        fontSize: 13,
                      }}
                    >
                      <thead>
                        <tr style={{ textAlign: "left", borderBottom: "1px solid #e0e0e0" }}>
                          <th style={{ padding: "4px 8px", fontWeight: 600 }}>Prefix</th>
                          <th style={{ padding: "4px 8px", fontWeight: 600 }}>상태</th>
                          <th style={{ padding: "4px 8px", fontWeight: 600 }}>발급일</th>
                          <th style={{ padding: "4px 8px" }}></th>
                        </tr>
                      </thead>
                      <tbody>
                        {apiKeys[project.id].map((key) => (
                          <tr key={key.id} style={{ borderBottom: "1px solid #f0f0f0" }}>
                            <td style={{ padding: "6px 8px", fontFamily: "monospace" }}>
                              {key.prefix}****
                            </td>
                            <td
                              style={{
                                padding: "6px 8px",
                                color: key.active ? "#28a745" : "#999",
                              }}
                            >
                              {key.active ? "활성" : "비활성"}
                            </td>
                            <td style={{ padding: "6px 8px", color: "#666" }}>
                              {new Date(key.createdAt).toLocaleDateString("ko-KR")}
                            </td>
                            <td style={{ padding: "6px 8px" }}>
                              {key.active && (
                                <button
                                  onClick={() => handleDeactivate(project.id, key.id)}
                                  style={{
                                    padding: "3px 8px",
                                    fontSize: 12,
                                    background: "#fff",
                                    border: "1px solid #dc3545",
                                    color: "#dc3545",
                                    borderRadius: 4,
                                    cursor: "pointer",
                                  }}
                                >
                                  비활성화
                                </button>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  )}

                  <button
                    onClick={() => handleIssueKey(project.id)}
                    style={{
                      padding: "5px 14px",
                      fontSize: 13,
                      background: "#28a745",
                      color: "#fff",
                      border: "none",
                      borderRadius: 4,
                      cursor: "pointer",
                    }}
                  >
                    + 새 키 발급
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
