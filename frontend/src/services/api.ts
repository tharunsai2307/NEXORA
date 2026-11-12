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
import {
  Sensor,
  CreateSensorPayload,
  UpdateSensorPayload,
  SensorReading,
  SensorStats,
  SensorType,
  SensorStatus,
} from '../types/sensor';
import {
  SimulationSession,
  SimulationScenario,
  StartSimulationPayload,
  ScenarioPayload,
  ScenarioDefinition,
} from '../types/simulation';
import {
  AnomalyDetection,
  AiPrediction,
  RiskAssessment,
  RoomAiOverview,
  BuildingAiSummary,
  AiModelMetadata,
  AiModelStatus,
} from '../types/ai';
import {
  DigitalTwinBuilding,
  DigitalTwinRoom,
  RoomLayoutUpdatePayload,
} from '../types/digitaltwin';
import {
  Incident,
  IncidentSeverity,
  IncidentStatus,
  IncidentCategory,
  CreateIncidentPayload,
  UpdateIncidentStatusPayload,
  EvacuationSession,
  StartEvacuationPayload,
  BuildingEmergencyStatus,
  EvacuationGraph,
  EvacuationRoute,
  EvacuationConnection,
} from '../types/emergency';
import {
  AnalyticsOverviewResponse,
  SensorAnalyticsResponse,
  OccupancyAnalyticsResponse,
  AiAnalyticsResponse,
  IncidentAnalyticsResponse,
  EvacuationAnalyticsResponse,
  HistoricalQueryResponse,
  PostMortemRequest,
  PostMortemResponse,
  GeneratedReportResponse,
  ReportType,
  ReportFormat,
} from '../types/analytics';

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

export const sensorApi = {
  async getSensors(
    buildingId?: string,
    floorId?: string,
    roomId?: string,
    type?: SensorType,
    status?: SensorStatus
  ): Promise<Sensor[]> {
    const params = new URLSearchParams();
    if (buildingId) params.append('buildingId', buildingId);
    if (floorId) params.append('floorId', floorId);
    if (roomId) params.append('roomId', roomId);
    if (type) params.append('type', type);
    if (status) params.append('status', status);

    const response = await apiClient.get<ApiResponse<Sensor[]>>('/sensors', { params });
    return response.data.data;
  },

  async getSensorsByRoom(roomId: string): Promise<Sensor[]> {
    const response = await apiClient.get<ApiResponse<Sensor[]>>(`/rooms/${roomId}/sensors`);
    return response.data.data;
  },

  async getSensor(id: string): Promise<Sensor> {
    const response = await apiClient.get<ApiResponse<Sensor>>(`/sensors/${id}`);
    return response.data.data;
  },

  async createSensor(roomId: string, payload: CreateSensorPayload): Promise<Sensor> {
    const response = await apiClient.post<ApiResponse<Sensor>>(`/rooms/${roomId}/sensors`, payload);
    return response.data.data;
  },

  async provisionRoomSensors(roomId: string): Promise<Sensor[]> {
    const response = await apiClient.post<ApiResponse<Sensor[]>>(`/rooms/${roomId}/sensors/provision`);
    return response.data.data;
  },

  async provisionBuildingSensors(buildingId: string): Promise<Sensor[]> {
    const response = await apiClient.post<ApiResponse<Sensor[]>>(`/buildings/${buildingId}/sensors/provision`);
    return response.data.data;
  },

  async updateSensor(id: string, payload: UpdateSensorPayload): Promise<Sensor> {
    const response = await apiClient.put<ApiResponse<Sensor>>(`/sensors/${id}`, payload);
    return response.data.data;
  },

  async updateStatus(id: string, status: SensorStatus): Promise<Sensor> {
    const response = await apiClient.patch<ApiResponse<Sensor>>(`/sensors/${id}/status`, { status });
    return response.data.data;
  },

  async deleteSensor(id: string): Promise<void> {
    await apiClient.delete(`/sensors/${id}`);
  },

  async recordManualReading(sensorId: string, readingValue: number): Promise<SensorReading> {
    const response = await apiClient.post<ApiResponse<SensorReading>>(`/sensors/${sensorId}/readings/manual`, {
      readingValue,
    });
    return response.data.data;
  },

  async getSensorStats(): Promise<SensorStats> {
    const response = await apiClient.get<ApiResponse<SensorStats>>('/sensors/stats');
    return response.data.data;
  },
};

export const readingApi = {
  async getSensorReadings(sensorId: string, page = 0, size = 20): Promise<{ content: SensorReading[]; totalElements: number }> {
    const response = await apiClient.get<ApiResponse<{ content: SensorReading[]; totalElements: number }>>(
      `/sensors/${sensorId}/readings?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  async getSensorHistory(sensorId: string, start?: string, end?: string): Promise<SensorReading[]> {
    const params = new URLSearchParams();
    if (start) params.append('start', start);
    if (end) params.append('end', end);

    const response = await apiClient.get<ApiResponse<SensorReading[]>>(
      `/sensors/${sensorId}/readings/history`,
      { params }
    );
    return response.data.data;
  },

  async getLatestRoomReadings(roomId: string): Promise<SensorReading[]> {
    const response = await apiClient.get<ApiResponse<SensorReading[]>>(`/rooms/${roomId}/readings/latest`);
    return response.data.data;
  },

  async getLatestBuildingReadings(buildingId: string): Promise<SensorReading[]> {
    const response = await apiClient.get<ApiResponse<SensorReading[]>>(`/buildings/${buildingId}/readings/latest`);
    return response.data.data;
  },
};

export const simulationApi = {
  async startSimulation(payload: StartSimulationPayload): Promise<SimulationSession> {
    const response = await apiClient.post<ApiResponse<SimulationSession>>('/simulations/start', payload);
    return response.data.data;
  },

  async pauseSimulation(sessionId: string): Promise<SimulationSession> {
    const response = await apiClient.post<ApiResponse<SimulationSession>>(`/simulations/${sessionId}/pause`);
    return response.data.data;
  },

  async resumeSimulation(sessionId: string): Promise<SimulationSession> {
    const response = await apiClient.post<ApiResponse<SimulationSession>>(`/simulations/${sessionId}/resume`);
    return response.data.data;
  },

  async stopSimulation(sessionId: string): Promise<SimulationSession> {
    const response = await apiClient.post<ApiResponse<SimulationSession>>(`/simulations/${sessionId}/stop`);
    return response.data.data;
  },

  async getActiveSimulation(buildingId?: string): Promise<SimulationSession | null> {
    const params = buildingId ? `?buildingId=${buildingId}` : '';
    const response = await apiClient.get<ApiResponse<SimulationSession | null>>(`/simulations/active${params}`);
    return response.data.data;
  },

  async getSimulationHistory(): Promise<SimulationSession[]> {
    const response = await apiClient.get<ApiResponse<SimulationSession[]>>('/simulations/history');
    return response.data.data;
  },

  async getAvailableScenarios(): Promise<ScenarioDefinition[]> {
    const response = await apiClient.get<ApiResponse<ScenarioDefinition[]>>('/simulations/scenarios');
    return response.data.data;
  },

  async activateScenario(sessionId: string, payload: ScenarioPayload): Promise<SimulationScenario> {
    const response = await apiClient.post<ApiResponse<SimulationScenario>>(
      `/simulations/${sessionId}/scenarios`,
      payload
    );
    return response.data.data;
  },

  async stopScenario(scenarioId: string): Promise<SimulationScenario> {
    const response = await apiClient.post<ApiResponse<SimulationScenario>>(`/scenarios/${scenarioId}/stop`);
    return response.data.data;
  },
};

export const aiApi = {
  async analyzeRoom(roomId: string): Promise<RoomAiOverview> {
    const response = await apiClient.post<ApiResponse<RoomAiOverview>>(`/ai/rooms/${roomId}/analyze`);
    return response.data.data;
  },

  async analyzeBuilding(buildingId: string): Promise<BuildingAiSummary> {
    const response = await apiClient.post<ApiResponse<BuildingAiSummary>>(`/ai/buildings/${buildingId}/analyze`);
    return response.data.data;
  },

  async getRoomAiOverview(roomId: string): Promise<RoomAiOverview> {
    const response = await apiClient.get<ApiResponse<RoomAiOverview>>(`/ai/rooms/${roomId}/latest`);
    return response.data.data;
  },

  async getBuildingAiSummary(buildingId: string): Promise<BuildingAiSummary> {
    const response = await apiClient.get<ApiResponse<BuildingAiSummary>>(`/ai/buildings/${buildingId}/summary`);
    return response.data.data;
  },

  async getAnomalies(params?: {
    buildingId?: string;
    floorId?: string;
    roomId?: string;
    severity?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: AnomalyDetection[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<ApiResponse<{ content: AnomalyDetection[]; totalElements: number; totalPages: number }>>('/ai/anomalies', { params });
    return response.data.data;
  },

  async getPredictions(params?: {
    buildingId?: string;
    roomId?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: AiPrediction[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<ApiResponse<{ content: AiPrediction[]; totalElements: number; totalPages: number }>>('/ai/predictions', { params });
    return response.data.data;
  },

  async getRisks(params?: {
    buildingId?: string;
    riskLevel?: string;
    page?: number;
    size?: number;
  }): Promise<{ content: RiskAssessment[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<ApiResponse<{ content: RiskAssessment[]; totalElements: number; totalPages: number }>>('/ai/risks', { params });
    return response.data.data;
  },

  async getModels(): Promise<AiModelMetadata[]> {
    const response = await apiClient.get<ApiResponse<AiModelMetadata[]>>('/ai/models');
    return response.data.data;
  },

  async getModelStatus(): Promise<AiModelStatus> {
    const response = await apiClient.get<ApiResponse<AiModelStatus>>('/ai/models/status');
    return response.data.data;
  },

  async trainModels(payload?: {
    synthetic_samples?: number;
    contamination?: number;
    force_retrain?: boolean;
  }): Promise<any> {
    const response = await apiClient.post<ApiResponse<any>>('/ai/models/train', payload || {});
    return response.data.data;
  },
};

export const digitalTwinApi = {
  async getBuildingTwin(buildingId: string): Promise<DigitalTwinBuilding> {
    const response = await apiClient.get<ApiResponse<DigitalTwinBuilding>>(`/digital-twin/buildings/${buildingId}`);
    return response.data.data;
  },

  async updateRoomLayout(roomId: string, payload: RoomLayoutUpdatePayload): Promise<DigitalTwinRoom> {
    const response = await apiClient.put<ApiResponse<DigitalTwinRoom>>(`/digital-twin/rooms/${roomId}/layout`, payload);
    return response.data.data;
  },
};

export const incidentApi = {
  async createIncident(payload: CreateIncidentPayload): Promise<Incident> {
    const response = await apiClient.post<ApiResponse<Incident>>('/incidents', payload);
    return response.data.data;
  },

  async getIncident(id: string): Promise<Incident> {
    const response = await apiClient.get<ApiResponse<Incident>>(`/incidents/${id}`);
    return response.data.data;
  },

  async getIncidents(params?: {
    buildingId?: string;
    status?: IncidentStatus;
    severity?: IncidentSeverity;
    category?: IncidentCategory;
    page?: number;
    size?: number;
  }): Promise<{ content: Incident[]; totalElements: number; totalPages: number }> {
    const response = await apiClient.get<ApiResponse<{ content: Incident[]; totalElements: number; totalPages: number }>>('/incidents', { params });
    return response.data.data;
  },

  async getActiveBuildingIncidents(buildingId: string): Promise<Incident[]> {
    const response = await apiClient.get<ApiResponse<Incident[]>>(`/incidents/buildings/${buildingId}/active`);
    return response.data.data;
  },

  async updateStatus(id: string, payload: UpdateIncidentStatusPayload): Promise<Incident> {
    const response = await apiClient.patch<ApiResponse<Incident>>(`/incidents/${id}/status`, payload);
    return response.data.data;
  },

  async assignIncident(id: string, userId: string, notes?: string): Promise<Incident> {
    const response = await apiClient.post<ApiResponse<Incident>>(`/incidents/${id}/assign`, { userId, notes });
    return response.data.data;
  },

  async acknowledgeIncident(id: string): Promise<Incident> {
    const response = await apiClient.post<ApiResponse<Incident>>(`/incidents/${id}/acknowledge`);
    return response.data.data;
  },

  async resolveIncident(id: string, notes?: string): Promise<Incident> {
    const response = await apiClient.post<ApiResponse<Incident>>(`/incidents/${id}/resolve`, null, {
      params: notes ? { notes } : undefined,
    });
    return response.data.data;
  },
};

export const emergencyApi = {
  async getBuildingEmergencyStatus(buildingId: string): Promise<BuildingEmergencyStatus> {
    const response = await apiClient.get<ApiResponse<BuildingEmergencyStatus>>(`/emergency/buildings/${buildingId}/status`);
    return response.data.data;
  },

  async activateEmergency(buildingId: string, reason?: string): Promise<BuildingEmergencyStatus> {
    const response = await apiClient.post<ApiResponse<BuildingEmergencyStatus>>(`/emergency/buildings/${buildingId}/activate`, { reason });
    return response.data.data;
  },
};

export const evacuationApi = {
  async startEvacuation(buildingId: string, payload?: StartEvacuationPayload): Promise<EvacuationSession> {
    const response = await apiClient.post<ApiResponse<EvacuationSession>>(`/evacuations/buildings/${buildingId}/start`, payload || {});
    return response.data.data;
  },

  async getSession(id: string): Promise<EvacuationSession> {
    const response = await apiClient.get<ApiResponse<EvacuationSession>>(`/evacuations/${id}`);
    return response.data.data;
  },

  async pauseEvacuation(id: string): Promise<EvacuationSession> {
    const response = await apiClient.post<ApiResponse<EvacuationSession>>(`/evacuations/${id}/pause`);
    return response.data.data;
  },

  async resumeEvacuation(id: string): Promise<EvacuationSession> {
    const response = await apiClient.post<ApiResponse<EvacuationSession>>(`/evacuations/${id}/resume`);
    return response.data.data;
  },

  async cancelEvacuation(id: string): Promise<EvacuationSession> {
    const response = await apiClient.post<ApiResponse<EvacuationSession>>(`/evacuations/${id}/cancel`);
    return response.data.data;
  },

  async stepEvacuation(id: string): Promise<EvacuationSession> {
    const response = await apiClient.post<ApiResponse<EvacuationSession>>(`/evacuations/${id}/step`);
    return response.data.data;
  },

  async getActiveSession(buildingId: string): Promise<EvacuationSession | null> {
    const response = await apiClient.get<ApiResponse<EvacuationSession>>(`/evacuations/buildings/${buildingId}/active`);
    return response.data.data;
  },

  async getBuildingSessions(buildingId: string): Promise<EvacuationSession[]> {
    const response = await apiClient.get<ApiResponse<EvacuationSession[]>>(`/evacuations/buildings/${buildingId}/sessions`);
    return response.data.data;
  },

  async getBuildingGraph(buildingId: string): Promise<EvacuationGraph> {
    const response = await apiClient.get<ApiResponse<EvacuationGraph>>(`/evacuation/buildings/${buildingId}/graph`);
    return response.data.data;
  },

  async toggleConnectionBlock(connectionId: string): Promise<EvacuationConnection> {
    const response = await apiClient.post<ApiResponse<EvacuationConnection>>(`/evacuation/connections/${connectionId}/toggle-block`);
    return response.data.data;
  },

  async getBuildingRoutes(buildingId: string): Promise<EvacuationRoute[]> {
    const response = await apiClient.get<ApiResponse<EvacuationRoute[]>>(`/evacuation/buildings/${buildingId}/routes`);
    return response.data.data;
  },

  async recalculateRoutes(buildingId: string): Promise<EvacuationRoute[]> {
    const response = await apiClient.post<ApiResponse<EvacuationRoute[]>>(`/evacuation/buildings/${buildingId}/routes/recalculate`);
    return response.data.data;
  },
};

export const analyticsApi = {
  async getOverview(params?: { buildingId?: string; start?: string; end?: string }): Promise<AnalyticsOverviewResponse> {
    const response = await apiClient.get<ApiResponse<AnalyticsOverviewResponse>>('/analytics/overview', { params });
    return response.data.data;
  },

  async getSensorAnalytics(params?: {
    buildingId?: string;
    roomId?: string;
    sensorType?: string;
    start?: string;
    end?: string;
  }): Promise<SensorAnalyticsResponse> {
    const response = await apiClient.get<ApiResponse<SensorAnalyticsResponse>>('/analytics/sensors', { params });
    return response.data.data;
  },

  async getOccupancyAnalytics(params?: { buildingId?: string; start?: string; end?: string }): Promise<OccupancyAnalyticsResponse> {
    const response = await apiClient.get<ApiResponse<OccupancyAnalyticsResponse>>('/analytics/occupancy', { params });
    return response.data.data;
  },

  async getAiAnalytics(params?: { buildingId?: string; start?: string; end?: string }): Promise<AiAnalyticsResponse> {
    const response = await apiClient.get<ApiResponse<AiAnalyticsResponse>>('/analytics/ai', { params });
    return response.data.data;
  },

  async getIncidentAnalytics(params?: { buildingId?: string; start?: string; end?: string }): Promise<IncidentAnalyticsResponse> {
    const response = await apiClient.get<ApiResponse<IncidentAnalyticsResponse>>('/analytics/incidents', { params });
    return response.data.data;
  },

  async getEvacuationAnalytics(params?: { buildingId?: string; start?: string; end?: string }): Promise<EvacuationAnalyticsResponse> {
    const response = await apiClient.get<ApiResponse<EvacuationAnalyticsResponse>>('/analytics/evacuations', { params });
    return response.data.data;
  },

  async queryHistory(params?: {
    buildingId?: string;
    recordType?: string;
    start?: string;
    end?: string;
    page?: number;
    size?: number;
  }): Promise<HistoricalQueryResponse> {
    const response = await apiClient.get<ApiResponse<HistoricalQueryResponse>>('/analytics/history', { params });
    return response.data.data;
  },
};

export const reportsApi = {
  async listReports(buildingId?: string): Promise<GeneratedReportResponse[]> {
    const response = await apiClient.get<ApiResponse<GeneratedReportResponse[]>>('/reports', { params: { buildingId } });
    return response.data.data;
  },

  async exportReport(params: {
    reportType: ReportType;
    buildingId?: string;
    format: ReportFormat;
    start?: string;
    end?: string;
  }): Promise<Blob> {
    const response = await apiClient.get('/reports/export', {
      params,
      responseType: 'blob',
    });
    return response.data;
  },

  async getIncidentPostMortem(incidentId: string): Promise<PostMortemResponse> {
    const response = await apiClient.get<ApiResponse<PostMortemResponse>>(`/reports/incidents/${incidentId}/postmortem`);
    return response.data.data;
  },

  async saveIncidentPostMortem(incidentId: string, payload: PostMortemRequest): Promise<PostMortemResponse> {
    const response = await apiClient.post<ApiResponse<PostMortemResponse>>(`/reports/incidents/${incidentId}/postmortem`, payload);
    return response.data.data;
  },

  async finalizeIncidentPostMortem(incidentId: string): Promise<PostMortemResponse> {
    const response = await apiClient.post<ApiResponse<PostMortemResponse>>(`/reports/incidents/${incidentId}/postmortem/finalize`);
    return response.data.data;
  },

  async listPostMortems(status?: string): Promise<PostMortemResponse[]> {
    const response = await apiClient.get<ApiResponse<PostMortemResponse[]>>('/reports/postmortems', { params: { status } });
    return response.data.data;
  },

  async exportPostMortem(postMortemId: string, format: ReportFormat = 'PDF'): Promise<Blob> {
    const response = await apiClient.get(`/reports/postmortems/${postMortemId}/export`, {
      params: { format },
      responseType: 'blob',
    });
    return response.data;
  },
};
