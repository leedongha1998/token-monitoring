export interface AlertItem {
  id: number;
  projectId: number;
  alertType: string;
  message: string;
  triggeredAt: string;
  isRead: boolean;
}
