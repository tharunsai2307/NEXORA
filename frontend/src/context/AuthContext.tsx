import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, Role, LoginPayload, RegisterPayload } from '../types/auth';
import { authApi } from '../services/api';

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
  hasRole: (roles: Role | Role[]) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('nexora_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('nexora_token'));
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const verifyAuth = async () => {
      const storedToken = localStorage.getItem('nexora_token');
      if (storedToken) {
        try {
          const currentUser = await authApi.getMe();
          setUser(currentUser);
          localStorage.setItem('nexora_user', JSON.stringify(currentUser));
        } catch {
          // Token expired or invalid
          localStorage.removeItem('nexora_token');
          localStorage.removeItem('nexora_user');
          setUser(null);
          setToken(null);
        }
      }
      setIsLoading(false);
    };

    verifyAuth();
  }, []);

  const login = async (payload: LoginPayload) => {
    const response = await authApi.login(payload);
    localStorage.setItem('nexora_token', response.accessToken);
    localStorage.setItem('nexora_user', JSON.stringify(response.user));
    setToken(response.accessToken);
    setUser(response.user);
  };

  const register = async (payload: RegisterPayload) => {
    const response = await authApi.register(payload);
    localStorage.setItem('nexora_token', response.accessToken);
    localStorage.setItem('nexora_user', JSON.stringify(response.user));
    setToken(response.accessToken);
    setUser(response.user);
  };

  const logout = () => {
    authApi.logout().catch(() => {});
    localStorage.removeItem('nexora_token');
    localStorage.removeItem('nexora_user');
    setUser(null);
    setToken(null);
  };

  const hasRole = (roles: Role | Role[]): boolean => {
    if (!user) return false;
    const roleList = Array.isArray(roles) ? roles : [roles];
    return roleList.includes(user.role);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        register,
        logout,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

