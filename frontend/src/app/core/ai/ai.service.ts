import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatMessage, ChatResponse, InsightsResponse, RecommendationResponse } from './ai.models';

@Injectable({ providedIn: 'root' })
export class AiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/ai`;

  chat(messages: ChatMessage[]): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl}/chat`, { messages });
  }

  recommend(): Observable<RecommendationResponse> {
    return this.http.get<RecommendationResponse>(`${this.baseUrl}/recommend`);
  }

  insights(): Observable<InsightsResponse> {
    return this.http.get<InsightsResponse>(`${this.baseUrl}/insights`);
  }
}
