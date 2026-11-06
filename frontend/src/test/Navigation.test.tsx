import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
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

describe('Authenticated Navigation Routes', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();

    localStorage.setItem('nexora_token', 'mock.jwt.token');
    localStorage.setItem('nexora_user', JSON.stringify(mockUser));
    vi.mocked(apiModule.authApi.getMe).mockResolvedValue(mockUser);

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

    vi.mocked(apiModule.buildingApi.getBuildings).mockResolvedValue([]);
    vi.mocked(apiModule.userApi.listUsers).mockResolvedValue([mockUser]);
  });

  it('navigates to Buildings route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Buildings/i });
    fireEvent.click(link);
    expect(await screen.findByText('Building Fleet Operations')).toBeInTheDocument();
  });

  it('navigates to Sensors route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Sensors/i });
    fireEvent.click(link);
    expect(await screen.findByText('Virtual IoT Sensor Registry')).toBeInTheDocument();
  });

  it('navigates to Digital Twin route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Digital Twin/i });
    fireEvent.click(link);
    expect(await screen.findByText('Interactive 3D Digital Twin Viewer')).toBeInTheDocument();
  });

  it('navigates to Occupancy route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Occupancy/i });
    fireEvent.click(link);
    expect(await screen.findByText('Edge-AI Occupancy Prediction & Analytics')).toBeInTheDocument();
  });

  it('navigates to Safety route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Safety/i });
    fireEvent.click(link);
    expect(await screen.findByText('Environmental Safety & Anomaly Detection')).toBeInTheDocument();
  });

  it('navigates to Emergency route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Emergency/i });
    fireEvent.click(link);
    expect(await screen.findByText('Emergency Response & Evacuation Routing')).toBeInTheDocument();
  });

  it('navigates to Analytics route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Analytics/i });
    fireEvent.click(link);
    expect(await screen.findByText('Historical Telemetry & Building Analytics')).toBeInTheDocument();
  });

  it('navigates to Settings route when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /Settings/i });
    fireEvent.click(link);
    expect(await screen.findByText('Platform & Gateway Configuration')).toBeInTheDocument();
  });

  it('navigates to User Directory for SYSTEM_ADMIN when clicked', async () => {
    render(<App />);
    await waitFor(() => expect(screen.getByText('Phase 2 of 7')).toBeInTheDocument());

    const link = screen.getByRole('link', { name: /User Directory/i });
    fireEvent.click(link);
    expect(await screen.findByText('User & RBAC Administration')).toBeInTheDocument();
  });
});
