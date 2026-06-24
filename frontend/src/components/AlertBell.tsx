import { useEffect, useRef, useState } from "react";
import { fetchAlerts, markAlertRead } from "../api/alerts";
import type { AlertItem } from "../types/alert";

const ALERT_TYPE_LABELS: Record<string, string> = {
  SESSION_SPIKE: "세션 스파이크",
  INEFFICIENT_MODEL: "비효율 모델",
  BUDGET_BURN_RATE: "예산 소진 경보",
  ABNORMAL_MULTI_TURN: "비정상 멀티턴",
};

interface Props {
  projectId: number;
}

export function AlertBell({ projectId }: Props) {
  const [alerts, setAlerts] = useState<AlertItem[]>([]);
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    fetchAlerts(projectId).then(setAlerts).catch(() => setAlerts([]));
  }, [projectId]);

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  const unread = alerts.filter((a) => !a.isRead).length;

  async function handleAlertClick(alert: AlertItem) {
    if (!alert.isRead) {
      await markAlertRead(alert.id);
      setAlerts((prev) => prev.map((a) => (a.id === alert.id ? { ...a, isRead: true } : a)));
    }
  }

  return (
    <div ref={ref} className="relative">
      <button
        onClick={() => setOpen((v) => !v)}
        className="relative p-2 rounded-full hover:bg-gray-100 transition-colors"
        aria-label="알림"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {unread > 0 && (
          <span className="absolute -top-0.5 -right-0.5 bg-red-500 text-white text-xs rounded-full w-4 h-4 flex items-center justify-center font-bold">
            {unread > 9 ? "9+" : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-1 w-80 bg-white border border-gray-200 rounded-lg shadow-lg z-50 max-h-96 overflow-y-auto">
          <div className="px-4 py-2 border-b border-gray-100 text-sm font-semibold text-gray-700">
            알림 {unread > 0 && <span className="text-red-500">({unread}개 미확인)</span>}
          </div>
          {alerts.length === 0 ? (
            <div className="px-4 py-6 text-sm text-gray-400 text-center">알림이 없습니다</div>
          ) : (
            alerts.map((alert) => (
              <button
                key={alert.id}
                onClick={() => handleAlertClick(alert)}
                className={[
                  "w-full text-left px-4 py-3 border-b border-gray-50 hover:bg-gray-50 transition-colors",
                  alert.isRead ? "opacity-60" : "bg-blue-50",
                ].join(" ")}
              >
                <div className="flex items-start gap-2">
                  <span className="text-xs font-medium text-blue-600 mt-0.5 shrink-0">
                    {ALERT_TYPE_LABELS[alert.alertType] ?? alert.alertType}
                  </span>
                  {!alert.isRead && (
                    <span className="w-1.5 h-1.5 rounded-full bg-red-500 mt-1.5 shrink-0" />
                  )}
                </div>
                <p className="text-xs text-gray-700 mt-1 leading-relaxed">{alert.message}</p>
                <p className="text-xs text-gray-400 mt-1">
                  {new Date(alert.triggeredAt).toLocaleString("ko-KR")}
                </p>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
