export interface EventItem {
  id: number;
  model: string;
  inputTokens: number;
  outputTokens: number;
  occurredAt: string;
  promptSummary: string | null;
  cost?: string | null;
}

export interface EventPage {
  content: EventItem[];
  totalElements: number;
  totalPages: number;
  number: number;
}
