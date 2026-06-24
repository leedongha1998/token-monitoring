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

function downloadCsv(events: EventItem[]) {
  const headers = ["ID", "발생시각", "모델", "입력 토큰", "출력 토큰", "비용 (USD)", "프롬프트 요약"];
  const rows = events.map((ev) => [
    ev.id,
    ev.occurredAt,
    ev.model,
    ev.inputTokens,
    ev.outputTokens,
    ev.cost ?? "",
    ev.promptSummary ?? "",
  ]);
  const csv = [headers, ...rows]
    .map((r) => r.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(","))
    .join("\n");
  const blob = new Blob(["﻿" + csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = `events-${new Date().toISOString().slice(0, 10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

interface Props {
  projectId?: number;
}

export function EventList({ projectId }: Props) {
  const [from, setFrom] = useState(sevenDaysAgoIso());
  const [to, setTo] = useState(todayIso());
  const [modelFilter, setModelFilter] = useState("");
  const [page, setPage] = useState(0);

  const [events, setEvents] = useState<EventItem[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [availableModels, setAvailableModels] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const SIZE = 50;

  useEffect(() => {
    setPage(0);
  }, [projectId]);

  useEffect(() => {
    setLoading(true);
    setError(null);
    fetchEvents({
      from: localDateToInstantStart(from),
      to: localDateToInstantEnd(to),
      projectId,
      model: modelFilter || undefined,
      page,
      size: SIZE,
    })
      .then((data) => {
        setEvents(data.content);
        setTotalElements(data.totalElements);
        setTotalPages(data.totalPages);
        if (!modelFilter) {
          const seen = [...new Set(data.content.map((e) => e.model))].sort();
          setAvailableModels((prev) =>
            [...new Set([...prev, ...seen])].sort(),
          );
        }
      })
      .catch((e: unknown) => setError(String(e)))
      .finally(() => setLoading(false));
  }, [from, to, projectId, modelFilter, page]);

  function handleSearch() {
    setPage(0);
    setAvailableModels([]);
    setModelFilter("");
  }

  const visibleEvents = events.filter((ev) => ev.model !== "<synthetic>");

  return (
    <div>
      <div className="flex flex-wrap gap-3 items-center mb-5">
        <label className="text-sm">
          시작일
          <input
            type="date"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
          />
        </label>
        <label className="text-sm">
          종료일
          <input
            type="date"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
          />
        </label>
        <label className="text-sm">
          모델
          <select
            value={modelFilter}
            onChange={(e) => {
              setModelFilter(e.target.value);
              setPage(0);
            }}
            className="ml-2 px-2 py-1 text-sm border border-gray-300 rounded"
          >
            <option value="">전체</option>
            {availableModels.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>
        </label>
        <button
          onClick={handleSearch}
          className="px-4 py-1.5 text-sm border border-gray-300 rounded hover:bg-gray-50 cursor-pointer"
        >
          조회
        </button>
        <button
          onClick={() => downloadCsv(visibleEvents)}
          disabled={visibleEvents.length === 0}
          className="px-4 py-1.5 text-sm bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
        >
          CSV 내보내기
        </button>
      </div>

      {loading && <p className="text-gray-400 text-sm">로딩 중…</p>}
      {error && <p className="text-red-500 text-sm">{error}</p>}

      {!loading && !error && (
        <>
          <p className="text-xs text-gray-500 mb-2">
            총 <strong>{totalElements}</strong>건
          </p>
          <table className="w-full border-collapse text-sm">
            <thead>
              <tr className="bg-gray-50">
                {["발생시각", "모델", "입력 토큰", "출력 토큰", "비용 (USD)", "프롬프트 요약"].map((h) => (
                  <th
                    key={h}
                    className="px-3 py-2 text-left border-b border-gray-200 font-semibold"
                  >
                    {h}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visibleEvents.length === 0 && (
                <tr>
                  <td
                    colSpan={6}
                    className="px-3 py-5 text-center text-gray-400"
                  >
                    데이터가 없습니다.
                  </td>
                </tr>
              )}
              {visibleEvents.map((ev) => (
                <tr key={ev.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="px-3 py-1.5">
                    {new Date(ev.occurredAt).toLocaleString("ko-KR")}
                  </td>
                  <td className="px-3 py-1.5">{ev.model}</td>
                  <td className="px-3 py-1.5">{ev.inputTokens.toLocaleString()}</td>
                  <td className="px-3 py-1.5">{ev.outputTokens.toLocaleString()}</td>
                  <td className="px-3 py-1.5 font-mono text-xs">
                    {ev.cost != null ? `$${parseFloat(ev.cost).toFixed(4)}` : "—"}
                  </td>
                  <td
                    className="px-3 py-1.5 max-w-xs overflow-hidden text-ellipsis whitespace-nowrap"
                    style={{ color: ev.promptSummary ? "#333" : "#aaa" }}
                    title={ev.promptSummary ?? ""}
                  >
                    {ev.promptSummary ?? "—"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="flex gap-2 mt-4 items-center">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-3 py-1 text-sm border border-gray-300 rounded disabled:opacity-40 hover:bg-gray-50 cursor-pointer"
              >
                이전
              </button>
              <span className="text-sm text-gray-600">
                {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1 text-sm border border-gray-300 rounded disabled:opacity-40 hover:bg-gray-50 cursor-pointer"
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
