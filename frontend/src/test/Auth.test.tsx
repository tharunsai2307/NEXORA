import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { RoleGuard } from '../components/auth/RoleGuard';
import { AuthProvider } from '../context/AuthContext';
import * as apiModule from '../services/api';

vi.mock('../services/api', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    getMe: vi.fn(),
    logout: vi.fn(),
  },
  systemService: {
    getSystemInfo: vi.fn(),
  },
  apiClient: {},
}));

describe('Authentication Components', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
  });

  describe('LoginPage Component', () => {
    it('renders login credentials form fields', () => {
      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/login']}>
            <LoginPage />
          </MemoryRouter>
        </AuthProvider>
      );

      expect(screen.getByText('Sign in to control platform')).toBeInTheDocument();
      expect(screen.getByLabelText(/Corporate Email/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Access Password/i)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Authenticate Session/i })).toBeInTheDocument();
      expect(screen.getByRole('link', { name: /Register as Building Manager/i })).toBeInTheDocument();
    });

    it('populates demo credentials when role buttons are clicked', () => {
      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/login']}>
            <LoginPage />
          </MemoryRouter>
        </AuthProvider>
      );

      const adminButton = screen.getByRole('button', { name: /^Admin$/i });
      fireEvent.click(adminButton);

      const emailInput = screen.getByLabelText(/Corporate Email/i) as HTMLInputElement;
      expect(emailInput.value).toBe('admin@nexora.com');
    });

    it('submits login payload and delegates to authApi', async () => {
      const mockResponse = {
        accessToken: 'mock-jwt-token',
        tokenType: 'Bearer',
        expiresInSeconds: 86400,
        user: {
          id: 'usr-1',
          fullName: 'Test Admin',
          email: 'admin@nexora.com',
          role: 'SYSTEM_ADMIN' as const,
          status: 'ACTIVE' as const,
          createdAt: '2026-03-30T00:00:00Z',
          updatedAt: '2026-03-30T00:00:00Z',
        },
      };

      vi.mocked(apiModule.authApi.login).mockResolvedValue(mockResponse);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/login']}>
            <LoginPage />
          </MemoryRouter>
        </AuthProvider>
      );

      fireEvent.change(screen.getByLabelText(/Corporate Email/i), {
        target: { value: 'admin@nexora.com' },
      });
      fireEvent.change(screen.getByLabelText(/Access Password/i), {
        target: { value: 'Admin@1234' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Authenticate Session/i }));

      await waitFor(() => {
        expect(apiModule.authApi.login).toHaveBeenCalledWith({
          email: 'admin@nexora.com',
          password: 'Admin@1234',
        });
      });
    });

    it('displays error alert on invalid credentials', async () => {
      vi.mocked(apiModule.authApi.login).mockRejectedValue({
        response: {
          data: {
            message: 'Invalid credentials or inactive account',
          },
        },
      });

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/login']}>
            <LoginPage />
          </MemoryRouter>
        </AuthProvider>
      );

      fireEvent.change(screen.getByLabelText(/Corporate Email/i), {
        target: { value: 'wrong@nexora.com' },
      });
      fireEvent.change(screen.getByLabelText(/Access Password/i), {
        target: { value: 'Wrong@1234' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Authenticate Session/i }));

      expect(
        await screen.findByText('Invalid credentials or inactive account')
      ).toBeInTheDocument();
    });
  });

  describe('RegisterPage Component', () => {
    it('validates password mismatch before submission', async () => {
      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/register']}>
            <RegisterPage />
          </MemoryRouter>
        </AuthProvider>
      );

      fireEvent.change(screen.getByLabelText(/Full Legal Name/i), {
        target: { value: 'Alice Smith' },
      });
      fireEvent.change(screen.getByLabelText(/Corporate Email/i), {
        target: { value: 'alice@nexora.com' },
      });
      fireEvent.change(screen.getByLabelText(/^Password/i), {
        target: { value: 'Password123' },
      });
      fireEvent.change(screen.getByLabelText(/Confirm Password/i), {
        target: { value: 'Mismatch999' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Complete Registration/i }));

      expect(
        await screen.findByText('Password and confirmation password do not match.')
      ).toBeInTheDocument();
      expect(apiModule.authApi.register).not.toHaveBeenCalled();
    });

    it('validates minimum password length', async () => {
      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/register']}>
            <RegisterPage />
          </MemoryRouter>
        </AuthProvider>
      );

      fireEvent.change(screen.getByLabelText(/Full Legal Name/i), {
        target: { value: 'Alice Smith' },
      });
      fireEvent.change(screen.getByLabelText(/Corporate Email/i), {
        target: { value: 'alice@nexora.com' },
      });
      fireEvent.change(screen.getByLabelText(/^Password/i), {
        target: { value: 'short' },
      });
      fireEvent.change(screen.getByLabelText(/Confirm Password/i), {
        target: { value: 'short' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Complete Registration/i }));

      expect(
        await screen.findByText('Password must contain at least 8 characters.')
      ).toBeInTheDocument();
    });
  });

  describe('RoleGuard Component', () => {
    it('displays access restricted message when user role does not match clearance', async () => {
      const managerUser = {
        id: 'usr-2',
        fullName: 'Bob Manager',
        email: 'bob@nexora.com',
        role: 'BUILDING_MANAGER' as const,
        status: 'ACTIVE' as const,
        createdAt: '2026-03-30T00:00:00Z',
        updatedAt: '2026-03-30T00:00:00Z',
      };

      localStorage.setItem('nexora_token', 'token-mgr');
      localStorage.setItem('nexora_user', JSON.stringify(managerUser));
      vi.mocked(apiModule.authApi.getMe).mockResolvedValue(managerUser);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/admin-secret']}>
            <Routes>
              <Route
                path="/admin-secret"
                element={
                  <RoleGuard allowedRoles={['SYSTEM_ADMIN']}>
                    <div>Secret Admin Data</div>
                  </RoleGuard>
                }
              />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Access Restricted')).toBeInTheDocument();
      });
      expect(screen.getByText('BUILDING_MANAGER')).toBeInTheDocument();
      expect(screen.queryByText('Secret Admin Data')).not.toBeInTheDocument();
    });

    it('renders protected children when user possesses required role', async () => {
      const adminUser = {
        id: 'usr-1',
        fullName: 'Alice Admin',
        email: 'admin@nexora.com',
        role: 'SYSTEM_ADMIN' as const,
        status: 'ACTIVE' as const,
        createdAt: '2026-03-30T00:00:00Z',
        updatedAt: '2026-03-30T00:00:00Z',
      };

      localStorage.setItem('nexora_token', 'token-admin');
      localStorage.setItem('nexora_user', JSON.stringify(adminUser));
      vi.mocked(apiModule.authApi.getMe).mockResolvedValue(adminUser);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/admin-secret']}>
            <Routes>
              <Route
                path="/admin-secret"
                element={
                  <RoleGuard allowedRoles={['SYSTEM_ADMIN']}>
                    <div>Secret Admin Data</div>
                  </RoleGuard>
                }
              />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      );

      await waitFor(() => {
        expect(screen.getByText('Secret Admin Data')).toBeInTheDocument();
      });
    });
  });
});
