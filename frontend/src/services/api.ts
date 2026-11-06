import axios, { AxiosInstance } from 'axios';
import { BackendHealthResponse, SystemInfo } from '../types/system';
import { AuthResponse, LoginPayload, RegisterPayload, User, Role } from '../types/auth';
import {
  Building,
  BuildingPayload,
  Floor,
  FloorPayload,
  Room,
  RoomPayload,
  DashboardStats,
  BuildingStatus,
  BuildingType,
} from '../types/building';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 8000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

// Interceptor to attach JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('nexora_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor to handle unauthenticated 401s
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Do not clear on login failure attempts
      if (!error.config.url?.includes('/auth/login')) {
        localStorage.removeItem('nexora_token');
        localStorage.removeItem('nexora_user');
      }
    }
    return Promise.reject(error);
  }
);

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export const systemService = {
  async getBackendHealth(): Promise<BackendHealthResponse> {
    const response = await apiClient.get<BackendHealthResponse>('/health');
    return response.data;
  },

  async getAiHealth(): Promise<{ status: string; service: string }> {
    const response = await apiClient.get<{ status: string; service: string }>('/health/ai');
    return response.data;
  },

  async getDatabaseHealth(): Promise<{ status: string; database: string }> {
    const response = await apiClient.get<{ status: string; database: string }>('/health/database');
    return response.data;
  },

  async getSystemInfo(): Promise<SystemInfo> {
    const response = await apiClient.get<SystemInfo>('/system/info');
    return response.data;
  },

  async getDashboardStats(): Promise<DashboardStats> {
    const response = await apiClient.get<ApiResponse<DashboardStats>>('/system/dashboard-stats');
    return response.data.data;
  },
};

export const authApi = {
  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', payload);
    return response.data.data;
  },

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', payload);
    return response.data.data;
  },

  async logout(): Promise<void> {
    await apiClient.post('/auth/logout');
  },

  async getMe(): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>('/auth/me');
    return response.data.data;
  },
};

export const userApi = {
  async listUsers(query?: string, role?: Role, active?: boolean): Promise<User[]> {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (role) params.append('role', role);
    if (active !== undefined) params.append('active', String(active));

    const response = await apiClient.get<ApiResponse<User[]>>('/users', { params });
    return response.data.data;
  },

  async getUser(id: string): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>(`/users/${id}`);
    return response.data.data;
  },

  async updateRole(id: string, role: Role): Promise<User> {
    const response = await apiClient.patch<ApiResponse<User>>(`/users/${id}/role`, { role });
    return response.data.data;
  },

  async updateStatus(id: string, active: boolean): Promise<User> {
    const response = await apiClient.patch<ApiResponse<User>>(`/users/${id}/status`, { active });
    return response.data.data;
  },
};

export const buildingApi = {
  async getBuildings(query?: string, status?: BuildingStatus, type?: BuildingType): Promise<Building[]> {
    const params = new URLSearchParams();
    if (query) params.append('query', query);
    if (status) params.append('status', status);
    if (type) params.append('type', type);

    const response = await apiClient.get<ApiResponse<Building[]>>('/buildings', { params });
    return response.data.data;
  },

  async getBuilding(id: string): Promise<Building> {
    const response = await apiClient.get<ApiResponse<Building>>(`/buildings/${id}`);
    return response.data.data;
  },

  async createBuilding(payload: BuildingPayload): Promise<Building> {
    const response = await apiClient.post<ApiResponse<Building>>('/buildings', payload);
    return response.data.data;
  },

  async updateBuilding(id: string, payload: BuildingPayload): Promise<Building> {
    const response = await apiClient.put<ApiResponse<Building>>(`/buildings/${id}`, payload);
    return response.data.data;
  },

  async deleteBuilding(id: string): Promise<void> {
    await apiClient.delete(`/buildings/${id}`);
  },

  async getAssignedUsers(buildingId: string): Promise<User[]> {
    const response = await apiClient.get<ApiResponse<User[]>>(`/buildings/${buildingId}/users`);
    return response.data.data;
  },

  async assignUser(buildingId: string, userId: string): Promise<void> {
    await apiClient.post(`/buildings/${buildingId}/users`, { userId });
  },

  async unassignUser(buildingId: string, userId: string): Promise<void> {
    await apiClient.delete(`/buildings/${buildingId}/users/${userId}`);
  },
};

export const floorApi = {
  async getFloors(buildingId: string): Promise<Floor[]> {
    const response = await apiClient.get<ApiResponse<Floor[]>>(`/buildings/${buildingId}/floors`);
    return response.data.data;
  },

  async getFloor(id: string): Promise<Floor> {
    const response = await apiClient.get<ApiResponse<Floor>>(`/floors/${id}`);
    return response.data.data;
  },

  async createFloor(buildingId: string, payload: FloorPayload): Promise<Floor> {
    const response = await apiClient.post<ApiResponse<Floor>>(`/buildings/${buildingId}/floors`, payload);
    return response.data.data;
  },

  async updateFloor(id: string, payload: FloorPayload): Promise<Floor> {
    const response = await apiClient.put<ApiResponse<Floor>>(`/floors/${id}`, payload);
    return response.data.data;
  },

  async deleteFloor(id: string): Promise<void> {
    await apiClient.delete(`/floors/${id}`);
  },
};

export const roomApi = {
  async getRooms(floorId: string): Promise<Room[]> {
    const response = await apiClient.get<ApiResponse<Room[]>>(`/floors/${floorId}/rooms`);
    return response.data.data;
  },

  async getRoom(id: string): Promise<Room> {
    const response = await apiClient.get<ApiResponse<Room>>(`/rooms/${id}`);
    return response.data.data;
  },

  async createRoom(floorId: string, payload: RoomPayload): Promise<Room> {
    const response = await apiClient.post<ApiResponse<Room>>(`/floors/${floorId}/rooms`, payload);
    return response.data.data;
  },

  async updateRoom(id: string, payload: RoomPayload): Promise<Room> {
    const response = await apiClient.put<ApiResponse<Room>>(`/rooms/${id}`, payload);
    return response.data.data;
  },

  async deleteRoom(id: string): Promise<void> {
    await apiClient.delete(`/rooms/${id}`);
  },
};
