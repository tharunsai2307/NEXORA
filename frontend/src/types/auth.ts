export type Role = 'SYSTEM_ADMIN' | 'BUILDING_MANAGER' | 'SAFETY_OFFICER';

export interface User {
  id: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  role: Role;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
  confirmPassword: string;
  phoneNumber?: string;
}

