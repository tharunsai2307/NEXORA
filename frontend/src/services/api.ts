import axios, { AxiosInstance } from 'axios';
import { BackendHealthResponse, SystemInfo } from '../types/system';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

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
};
