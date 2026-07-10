import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Business,
  CreateBusinessRequest,
  MembershipPlan,
  MembershipPlanRequest,
} from './business.models';

@Injectable({ providedIn: 'root' })
export class BusinessService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/business`;

  create(request: CreateBusinessRequest): Observable<Business> {
    return this.http.post<Business>(this.baseUrl, request);
  }

  listAll(): Observable<Business[]> {
    return this.http.get<Business[]>(this.baseUrl);
  }

  getMyBusiness(): Observable<Business> {
    return this.http.get<Business>(`${this.baseUrl}/me`);
  }

  listPlans(): Observable<MembershipPlan[]> {
    return this.http.get<MembershipPlan[]>(`${this.baseUrl}/plans`);
  }

  createPlan(request: MembershipPlanRequest): Observable<MembershipPlan> {
    return this.http.post<MembershipPlan>(`${this.baseUrl}/plans`, request);
  }

  updatePlan(planId: string, request: MembershipPlanRequest): Observable<MembershipPlan> {
    return this.http.put<MembershipPlan>(`${this.baseUrl}/plans/${planId}`, request);
  }

  deletePlan(planId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/plans/${planId}`);
  }
}
