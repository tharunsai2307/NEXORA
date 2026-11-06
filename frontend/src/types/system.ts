export type ConnectionStatus = 'Connected' | 'Disconnected' | 'Checking';

export interface SubsystemStatus {
  backend: ConnectionStatus;
  aiEngine: ConnectionStatus;
  database: ConnectionStatus;
  lastChecked: string | null;
  responseTimeMs: number | null;
  error?: string | null;
}

export interface SystemInfo {
  applicationName: string;
  applicationVersion: string;
  backendStatus: string;
  databaseConnectivityStatus: string;
  aiServiceConnectivityStatus: string;
  environment: string;
  subsystemDetails: Record<string, string>;
  timestamp: string;
}

export interface BackendHealthResponse {
  status: string;
  service: string;
}
