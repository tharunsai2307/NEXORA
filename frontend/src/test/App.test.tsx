import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from '../App';
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
    getBackendHealth: vi.fn(),
    getAiHealth: vi.fn(),
    getDatabaseHealth: vi.fn(),
    getDashboardStats: vi.fn(),
  },
  buildingApi: {
    getBuildings: vi.fn(),
    getBuilding: vi.fn(),
  },
  floorApi: {
    getFloors: vi.fn(),
  },
  roomApi: {
    getRooms: vi.fn(),
  },
  userApi: {
    listUsers: vi.fn(),
  },
  apiClient: {},
}));

describe('Application Bootstrap and Authentication Protection', () => {
  beforeEach(() => {
    window.history.pushState({}, '', '/');
    localStorage.clear();
    vi.clearAllMocks();

    vi.mocked(apiModule.systemService.getSystemInfo).mockResolvedValue({
      applicationName: 'smart-building-backend',
      applicationVersion: '1.0.0-SNAPSHOT',
      backendStatus: 'UP',
      databaseConnectivityStatus: 'UP',
      aiServiceConnectivityStatus: 'UP',
      environment: 'dev',
      subsystemDetails: {},
      timestamp: new Date().toISOString(),
    });

    vi.mocked(apiModule.systemService.getDashboardStats).mockResolvedValue({
      totalBuildings: 1,
      totalFloors: 3,
      totalRooms: 7,
      totalCapacity: 340,
      currentOccupancy: 42,
      occupancyPercentage: 12.35,
      activeAlerts: 0,
      safetySummary: {
        NORMAL: 7,
        WARNING: 0,
        CRITICAL: 0,
      },
    });
  });

  it('redirects unauthenticated users to the login portal', async () => {
    render(<App />);

    expect(await screen.findByText('NEXORA')).toBeInTheDocument();
    expect(screen.getByText('Sign in to control platform')).toBeInTheDocument();
    expect(screen.getByText('Authenticate Session')).toBeInTheDocument();
    expect(screen.getByText('Select Preconfigured Demo Role')).toBeInTheDocument();
  });

  it('renders application shell and sidebar when user is authenticated', async () => {
    const mockUser = {
      id: 'usr-admin-1',
      fullName: 'System Administrator',
      email: 'admin@nexora.com',
      role: 'SYSTEM_ADMIN' as const,
      phoneNumber: '+1-555-0100',
      status: 'ACTIVE' as const,
      createdAt: '2026-03-30T00:00:00Z',
      updatedAt: '2026-03-30T00:00:00Z',
    };

    localStorage.setItem('nexora_token', 'mock.jwt.token');
    localStorage.setItem('nexora_user', JSON.stringify(mockUser));
    vi.mocked(apiModule.authApi.getMe).mockResolvedValue(mockUser);

    render(<App />);

    await waitFor(() => {
      expect(screen.getByText('Phase 7 of 7')).toBeInTheDocument();
    });

    expect(screen.getByText('Navigation Console')).toBeInTheDocument();
    expect(screen.getByText('User Directory')).toBeInTheDocument();
    expect(screen.getAllByText('System Administrator').length).toBeGreaterThanOrEqual(1);
  });
});
