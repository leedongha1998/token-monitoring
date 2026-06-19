import { useEffect, useState } from "react";
import { fetchEvents } from "../api/events";
import type { EventItem } from "../types/events";

function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

function sevenDaysAgoIso() {
  const d = new Date();
  d.setDate(d.getDate() - 7);
  return d.toISOString().slice(0, 10);
}

function localDateToInstantStart(dateStr: string): string {
  return dateStr + "T00:00:00Z";
}

function localDateToInstantEnd(dateStr: string): string {
  const d = new Date(dateStr + "T00:00:00Z");
  d.setDate(d.getDate() + 1);
  return d.toISOString();
}

export function EventList() {
  const [from, setFrom] = useState(sevenDaysAgoIso());
  const [to, setTo] = useState(todayIso());
  const [projectId, setProjectId] = useState("");
  const [page, setPage] = useState(0);

  const [events, setEvents] = useState<EventItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const SIZE = 50;

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchEvents({
      from: localDateToInstantStart(from),
      to: localDateToInstantEnd(to),
      projectId: projectId ? Number(projectId) : undefined,
      page,
      size: SIZE,
    })
      .then((data) => {
        setEvents(data.content);
        setTotalElements(data.totalElements);
        setTotalPages(data.totalPages);
      })
      .catch((e: unknown) => setError(String(e)))
      .finally(() => setLoading(false));
  }, [from, to, projectId, page]);

  function handleSearch() {
    setPage(0);
  }

  return (
    <div>
      <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 20, flexWrap: "wrap" }}>
        <label style={{ fontSize: 14 }}>
          시작일
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
          />
        </label>
        <label style={{ fontSize: 14 }}>
          종료일
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14 }}
          />
        </label>
        <label style={{ fontSize: 14 }}>
          프로젝트 ID
          <input
            type="number"
            value={projectId}
            onChange={(e) => setProjectId(e.target.value)}
            placeholder="전체"
            style={{ marginLeft: 8, padding: "4px 8px", fontSize: 14, width: 80 }}
          />
        </label>
        <button
          onClick={handleSearch}
          style={{ padding: "5px 16px", fontSize: 14, cursor: "pointer" }}
        >
          조회
        </button>
      </div>

      {loading && <p style={{ color: "#888", fontSize: 14 }}>로딩 중…</p>}
      {error && <p style={{ color: "red", fontSize: 14 }}>{error}</p>}

      {!loading && !error && (
        <>
          <p style={{ fontSize: 13, color: "#555", marginBottom: 8 }}>
            총 <strong>{totalElements}</strong>건
          </p>
          <table style={{ width: "100%", borderCollapse: "collapse", fontSize: 13 }}>
            <thead>
              <tr style={{ background: "#f5f5f5" }}>
                {["발생시각", "모델", "입력 토큰", "출력 토큰", "프롬프트 요약"].map((h) => (
                  <th
                    key={h}
                    style={{ padding: "8px 12px", textAlign: "left", borderBottom: "1px solid #ddd", fontWeight: 600 }}
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {events.length === 0 && (
                <tr>
                  <td colSpan={5} style={{ padding: "20px 12px", textAlign: "center", color: "#888" }}>
                    데이터가 없습니다.
                  </td>
                </tr>
              )}
              {events.map((ev) => (
                <tr key={ev.id} style={{ borderBottom: "1px solid #eee" }}>
                  <td style={{ padding: "7px 12px" }}>
                    {new Date(ev.occurredAt).toLocaleString("ko-KR")}
                  </td>
                  <td style={{ padding: "7px 12px" }}>{ev.model}</td>
                  <td style={{ padding: "7px 12px" }}>{ev.inputTokens.toLocaleString()}</td>
                  <td style={{ padding: "7px 12px" }}>{ev.outputTokens.toLocaleString()}</td>
                  <td
                    style={{
                      padding: "7px 12px",
                      maxWidth: 400,
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                      color: ev.promptSummary ? "#333" : "#aaa",
                    }}
                    title={ev.promptSummary ?? ""}
                  >
                    {ev.promptSummary ?? "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div style={{ display: "flex", gap: 8, marginTop: 16, alignItems: "center" }}>
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                style={{ padding: "4px 12px", cursor: page === 0 ? "default" : "pointer" }}
              >
                이전
              </button>
              <span style={{ fontSize: 13 }}>
                {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                style={{ padding: "4px 12px", cursor: page >= totalPages - 1 ? "default" : "pointer" }}
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
