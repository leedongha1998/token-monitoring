export interface DailyStats {
  projectId: number;
  date: string;
  model: string;
  totalInputTokens: number;
  totalOutputTokens: number;
  totalCost: string;
}

export interface SummaryStats {
  totalInputTokens: number;
  totalOutputTokens: number;
  totalCost: string;
}
