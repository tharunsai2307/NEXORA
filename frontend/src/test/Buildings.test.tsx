import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { BuildingsPage } from '../pages/BuildingsPage';
import { BuildingDetailPage } from '../pages/BuildingDetailPage';
import { AuthProvider } from '../context/AuthContext';
import * as apiModule from '../services/api';

vi.mock('../services/api', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    getMe: vi.fn(),
    logout: vi.fn(),
  },
  buildingApi: {
    getBuildings: vi.fn(),
    getBuilding: vi.fn(),
    createBuilding: vi.fn(),
    deleteBuilding: vi.fn(),
    getAssignedUsers: vi.fn(),
    assignUser: vi.fn(),
    unassignUser: vi.fn(),
  },
  floorApi: {
    getFloors: vi.fn(),
    getFloor: vi.fn(),
    createFloor: vi.fn(),
    deleteFloor: vi.fn(),
  },
  roomApi: {
    getRooms: vi.fn(),
    getRoom: vi.fn(),
  },
  userApi: {
    listUsers: vi.fn(),
  },
  apiClient: {},
}));

const mockAdmin = {
  id: 'usr-1',
  fullName: 'Alice Admin',
  email: 'admin@nexora.com',
  role: 'SYSTEM_ADMIN' as const,
  status: 'ACTIVE' as const,
  createdAt: '2026-03-30T00:00:00Z',
  updatedAt: '2026-03-30T00:00:00Z',
};

const mockBuilding = {
  id: 'bld-001',
  buildingCode: 'NEX-TWR-01',
  name: 'Nexora Tower One',
  address: '100 Innovation Boulevard',
  buildingType: 'COMMERCIAL' as const,
  status: 'ACTIVE' as const,
  totalFloors: 3,
  totalRooms: 7,
  totalCapacity: 340,
  currentOccupancy: 42,
  createdAt: '2026-03-30T00:00:00Z',
  updatedAt: '2026-03-30T00:00:00Z',
  assignedUsers: [],
};

const mockFloors = [
  {
    id: 'flr-001',
    buildingId: 'bld-001',
    floorNumber: 1,
    floorName: 'Ground Lobby & Reception',
    status: 'ACTIVE' as const,
    layoutWidth: 60.0,
    layoutDepth: 40.0,
    roomCount: 2,
    totalCapacity: 50,
    currentOccupancy: 10,
    createdAt: '2026-03-30T00:00:00Z',
    updatedAt: '2026-03-30T00:00:00Z',
  },
];

describe('Building Management Components', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();

    localStorage.setItem('nexora_token', 'token-admin');
    localStorage.setItem('nexora_user', JSON.stringify(mockAdmin));
    vi.mocked(apiModule.authApi.getMe).mockResolvedValue(mockAdmin);
    vi.mocked(apiModule.buildingApi.getAssignedUsers).mockResolvedValue([]);
  });

  describe('BuildingsPage Component', () => {
    it('fetches and displays registered building topologies', async () => {
      vi.mocked(apiModule.buildingApi.getBuildings).mockResolvedValue([mockBuilding]);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/buildings']}>
            <BuildingsPage />
          </MemoryRouter>
        </AuthProvider>
      );

      expect(await screen.findByText('Building Fleet Operations')).toBeInTheDocument();
      expect(screen.getByText('Nexora Tower One')).toBeInTheDocument();
      expect(screen.getByText('NEX-TWR-01')).toBeInTheDocument();
      expect(screen.getByText('COMMERCIAL')).toBeInTheDocument();
    });

    it('opens create building modal when Register Building button is clicked', async () => {
      vi.mocked(apiModule.buildingApi.getBuildings).mockResolvedValue([]);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/buildings']}>
            <BuildingsPage />
          </MemoryRouter>
        </AuthProvider>
      );

      expect(await screen.findByText('Building Fleet Operations')).toBeInTheDocument();

      const registerBtn = screen.getByRole('button', { name: /Register Building/i });
      fireEvent.click(registerBtn);

      expect(screen.getByText('Register New Building Facility')).toBeInTheDocument();
      expect(screen.getByPlaceholderText('Nexora Tower Gamma')).toBeInTheDocument();
      expect(screen.getByPlaceholderText('NEX-TWR-03')).toBeInTheDocument();
    });
  });

  describe('BuildingDetailPage Component', () => {
    it('fetches and displays building details with floor hierarchy', async () => {
      vi.mocked(apiModule.buildingApi.getBuilding).mockResolvedValue(mockBuilding);
      vi.mocked(apiModule.floorApi.getFloors).mockResolvedValue(mockFloors);
      vi.mocked(apiModule.userApi.listUsers).mockResolvedValue([mockAdmin]);

      render(
        <AuthProvider>
          <MemoryRouter initialEntries={['/buildings/bld-001']}>
            <Routes>
              <Route path="/buildings/:buildingId" element={<BuildingDetailPage />} />
            </Routes>
          </MemoryRouter>
        </AuthProvider>
      );

      expect(await screen.findByText('Nexora Tower One')).toBeInTheDocument();
      expect(screen.getByText('Ground Lobby & Reception')).toBeInTheDocument();
      expect(screen.getByText('LEVEL 1')).toBeInTheDocument();
      expect(screen.getByText('Vertical Floor Hierarchy')).toBeInTheDocument();
    });
  });
});
