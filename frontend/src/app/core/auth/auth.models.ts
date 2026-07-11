export type Role = 'SUPER_ADMIN' | 'BUSINESS_ADMIN' | 'STAFF' | 'MEMBER';
export type ClientStatus = 'PENDING' | 'ACTIVE' | 'REJECTED';

export interface UserResponse {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  businessId: string | null;
  status: ClientStatus;
  paid: boolean;
}

export interface AuthResponse {
  accessToken: string | null;
  refreshToken: string | null;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface UpdateClientRequest {
  status?: ClientStatus;
  paid?: boolean;
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
