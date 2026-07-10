export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatResponse {
  reply: string;
}

export interface RecommendationResponse {
  headline: string;
  suggestions: string[];
}

export interface InsightsResponse {
  summary: string;
  highlights: string[];
}
