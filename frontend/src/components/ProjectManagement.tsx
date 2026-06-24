import { useState, useEffect, useCallback } from "react";
import type { Project, ApiKey, IssuedKey } from "../types/project";
import {
  fetchProjects,
  createProject,
  fetchApiKeys,
  issueApiKey,
  deactivateApiKey,
} from "../api/projects";
import { fetchBudget, setBudget } from "../api/budget";
import type { BudgetInfo } from "../api/budget";

function currentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}`;
}

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

  const [budgets, setBudgets] = useState<Record<number, BudgetInfo | null>>({});
  const [budgetForm, setBudgetForm] = useState<Record<number, { yearMonth: string; amount: string }>>({});
  const [budgetSaving, setBudgetSaving] = useState<Record<number, boolean>>({});

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
    const ym = currentYearMonth();
    try {
      const budget = await fetchBudget(projectId, ym);
      setBudgets((prev) => ({ ...prev, [projectId]: budget }));
      setBudgetForm((prev) => ({
        ...prev,
        [projectId]: {
          yearMonth: ym,
          amount: budget ? String(budget.monthlyBudgetUsd) : "",
        },
      }));
    } catch {
      setBudgets((prev) => ({ ...prev, [projectId]: null }));
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

  const handleSetBudget = async (projectId: number) => {
    const form = budgetForm[projectId];
    if (!form?.yearMonth || !form?.amount) return;
    const amount = parseFloat(form.amount);
    if (isNaN(amount) || amount <= 0) return;
    setBudgetSaving((prev) => ({ ...prev, [projectId]: true }));
    try {
      const result = await setBudget(projectId, form.yearMonth, amount);
      setBudgets((prev) => ({ ...prev, [projectId]: result }));
    } catch (e) {
      setError(String(e));
    } finally {
      setBudgetSaving((prev) => ({ ...prev, [projectId]: false }));
    }
  };

  if (loading) return <div className="p-4 text-gray-500">로딩 중...</div>;

  return (
    <div>
      {error && (
        <div className="text-red-700 mb-3 px-3 py-2 bg-red-50 border border-red-200 rounded text-sm">
          오류: {error}
          <button
            onClick={() => setError(null)}
            className="ml-2 cursor-pointer bg-transparent border-0 text-red-500"
          >
            ×
          </button>
        </div>
      )}

      <div className="bg-gray-50 p-4 rounded-lg mb-6 border border-gray-200">
        <h3 className="text-sm font-semibold mb-3">새 프로젝트 만들기</h3>
        <div className="flex gap-2 flex-wrap">
          <input
            type="text"
            placeholder="프로젝트 이름 *"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            className="px-2.5 py-1.5 text-sm border border-gray-300 rounded w-48"
          />
          <input
            type="text"
            placeholder="설명 (선택)"
            value={newDesc}
            onChange={(e) => setNewDesc(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            className="px-2.5 py-1.5 text-sm border border-gray-300 rounded w-64"
          />
          <button
            onClick={handleCreate}
            disabled={creating || !newName.trim()}
            className="px-4 py-1.5 text-sm bg-blue-600 text-white border-0 rounded cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-700"
          >
            {creating ? "생성 중..." : "생성"}
          </button>
        </div>
      </div>

      {projects.length === 0 ? (
        <div className="text-gray-500 text-sm">
          프로젝트가 없습니다. 위에서 첫 번째 프로젝트를 만들어보세요.
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {projects.map((project) => (
            <div
              key={project.id}
              className="border border-gray-200 rounded-lg overflow-hidden"
            >
              <div className="px-4 py-3 bg-white flex items-center gap-3">
                <div className="flex-1">
                  <div className="font-semibold text-sm">{project.name}</div>
                  {project.description && (
                    <div className="text-xs text-gray-500 mt-0.5">{project.description}</div>
                  )}
                  <div
                    className={`text-xs mt-1 ${project.active ? "text-green-600" : "text-red-500"}`}
                  >
                    {project.active ? "활성" : "비활성"} · ID: {project.id}
                  </div>
                </div>
                <button
                  onClick={() => handleToggle(project.id)}
                  className={`px-3 py-1.5 text-xs border border-gray-300 rounded cursor-pointer hover:bg-gray-50 ${
                    expandedProjectId === project.id ? "bg-gray-100" : "bg-white"
                  }`}
                >
                  {expandedProjectId === project.id ? "닫기" : "API 키 / 예산 관리"}
                </button>
              </div>

              {expandedProjectId === project.id && (
                <div className="px-4 py-3 bg-gray-50 border-t border-gray-200">
                  {issuedForProject === project.id && issuedKey && (
                    <div className="mb-3 p-3 bg-yellow-50 border border-yellow-300 rounded">
                      <div className="text-xs font-semibold mb-1.5">
                        발급된 API 키 — 이 화면에서만 표시됩니다
                      </div>
                      <div className="flex items-center gap-2">
                        <code className="text-xs bg-white px-2 py-1 rounded border border-gray-200 flex-1 break-all">
                          {issuedKey.plainKey}
                        </code>
                        <button
                          onClick={() => navigator.clipboard.writeText(issuedKey.plainKey)}
                          className="px-2 py-1 text-xs border border-gray-300 rounded cursor-pointer bg-white whitespace-nowrap hover:bg-gray-50"
                        >
                          복사
                        </button>
                        <button
                          onClick={() => { setIssuedKey(null); setIssuedForProject(null); }}
                          className="text-sm text-gray-400 bg-transparent border-0 cursor-pointer"
                        >
                          ×
                        </button>
                      </div>
                    </div>
                  )}

                  {(!apiKeys[project.id] || apiKeys[project.id].length === 0) ? (
                    <div className="text-xs text-gray-500 mb-2">발급된 API 키가 없습니다.</div>
                  ) : (
                    <table className="w-full border-collapse mb-3 text-xs">
                      <thead>
                        <tr className="text-left border-b border-gray-200">
                          <th className="py-1 px-2 font-semibold">Prefix</th>
                          <th className="py-1 px-2 font-semibold">상태</th>
                          <th className="py-1 px-2 font-semibold">발급일</th>
                          <th className="py-1 px-2" />
                        </tr>
                      </thead>
                      <tbody>
                        {apiKeys[project.id].map((key) => (
                          <tr key={key.id} className="border-b border-gray-100">
                            <td className="py-1.5 px-2 font-mono">{key.prefix}****</td>
                            <td
                              className={`py-1.5 px-2 ${key.active ? "text-green-600" : "text-gray-400"}`}
                            >
                              {key.active ? "활성" : "비활성"}
                            </td>
                            <td className="py-1.5 px-2 text-gray-500">
                              {new Date(key.createdAt).toLocaleDateString("ko-KR")}
                            </td>
                            <td className="py-1.5 px-2">
                              {key.active && (
                                <button
                                  onClick={() => handleDeactivate(project.id, key.id)}
                                  className="px-2 py-0.5 text-xs bg-white border border-red-400 text-red-500 rounded cursor-pointer hover:bg-red-50"
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
                    className="px-3 py-1.5 text-xs bg-green-600 text-white border-0 rounded cursor-pointer hover:bg-green-700"
                  >
                    + 새 키 발급
                  </button>

                  <div className="mt-4 pt-3 border-t border-gray-200">
                    <h4 className="text-xs font-semibold mb-2 text-gray-700">월 예산 설정</h4>
                    {budgets[project.id] && (
                      <p className="text-xs text-gray-500 mb-1.5">
                        현재: {budgets[project.id]!.yearMonth} — ${budgets[project.id]!.monthlyBudgetUsd}
                      </p>
                    )}
                    <div className="flex gap-2 items-center flex-wrap">
                      <input
                        type="month"
                        value={budgetForm[project.id]?.yearMonth ?? currentYearMonth()}
                        onChange={(e) =>
                          setBudgetForm((prev) => ({
                            ...prev,
                            [project.id]: { ...prev[project.id], yearMonth: e.target.value },
                          }))
                        }
                        className="px-2 py-1 text-xs border border-gray-300 rounded"
                      />
                      <input
                        type="number"
                        step="0.01"
                        min="0.0001"
                        placeholder="예산 (USD)"
                        value={budgetForm[project.id]?.amount ?? ""}
                        onChange={(e) =>
                          setBudgetForm((prev) => ({
                            ...prev,
                            [project.id]: { ...prev[project.id], amount: e.target.value },
                          }))
                        }
                        className="px-2 py-1 text-xs border border-gray-300 rounded w-28"
                      />
                      <button
                        onClick={() => handleSetBudget(project.id)}
                        disabled={budgetSaving[project.id]}
                        className="px-3 py-1 text-xs bg-blue-600 text-white border-0 rounded cursor-pointer hover:bg-blue-700 disabled:opacity-50"
                      >
                        {budgetSaving[project.id] ? "저장 중..." : "저장"}
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
