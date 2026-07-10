export type Role = 'SUPER_ADMIN' | 'BUSINESS_ADMIN' | 'STAFF' | 'MEMBER';

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  businessId: string | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
  role: Role;
  businessId?: string | null;
}
