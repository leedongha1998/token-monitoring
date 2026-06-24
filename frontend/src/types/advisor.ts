export interface ModelSwitchAdvice {
  currentModel: string;
  suggestedModel: string;
  currentMonthlyCost: string;
  projectedSavings: string;
  monthlyInputTokens: number;
  monthlyOutputTokens: number;
}

export interface SessionEfficiency {
  sessionId: string;
  sessionDate: string;
  model: string;
  totalInputTokens: number;
  totalOutputTokens: number;
  costUsd: string;
}
