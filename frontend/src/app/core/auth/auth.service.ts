import { HttpClient } from '@angular/common/http';
import { Injectable, computed, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  UpdateClientRequest,
  UserResponse,
} from './auth.models';

const ACCESS_TOKEN_KEY = 'appgym.accessToken';
const REFRESH_TOKEN_KEY = 'appgym.refreshToken';
const USER_KEY = 'appgym.user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSignal = signal<UserResponse | null>(this.readStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);

  constructor(private readonly http: HttpClient) {}

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, request).pipe(
      tap((response) => {
        // Un socio (MEMBER) recien registrado queda PENDING de aprobacion y no
        // recibe tokens: no hay sesion que guardar todavia.
        if (response.accessToken) {
          this.storeSession(response);
        }
      }),
    );
  }

  listClients(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${environment.apiUrl}/auth/clients`);
  }

  updateClient(clientId: string, request: UpdateClientRequest): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${environment.apiUrl}/auth/clients/${clientId}`, request);
  }

  deleteClient(clientId: string): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/auth/clients/${clientId}`);
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/login`, request)
      .pipe(tap((response) => this.storeSession(response)));
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<AuthResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(tap((response) => this.storeSession(response)));
  }

  fetchCurrentUser(): Observable<UserResponse> {
    return this.http
      .get<UserResponse>(`${environment.apiUrl}/auth/me`)
      .pipe(tap((user) => this.storeUser(user)));
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private storeSession(response: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, response.accessToken!);
    localStorage.setItem(REFRESH_TOKEN_KEY, response.refreshToken!);
    this.storeUser(response.user);
  }

  private storeUser(user: UserResponse): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.currentUserSignal.set(user);
  }

  private readStoredUser(): UserResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as UserResponse) : null;
  }
}
