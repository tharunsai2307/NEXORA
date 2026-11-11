export type SessionStatus = 'RUNNING' | 'PAUSED' | 'STOPPED';

export type ScenarioType =
  | 'NORMAL'
  | 'TEMPERATURE_ANOMALY'
  | 'SMOKE_EVENT'
  | 'POOR_AIR_QUALITY'
  | 'HIGH_OCCUPANCY'
  | 'CUSTOM';

export type ScenarioStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export interface SimulationSession {
  id: string;
  buildingId: string;
  buildingName: string;
  buildingCode: string;
  sessionName: string;
  startedBy?: string | null;
  status: SessionStatus;
  scenarioType: ScenarioType;
  simulationIntervalSeconds: number;
  activeSensorCount: number;
  startedAt: string;
  pausedAt?: string | null;
  stoppedAt?: string | null;
}

export interface SimulationScenario {
  id: string;
  sessionId: string;
  scenarioName: string;
  scenarioType: ScenarioType;
  targetConfiguration?: string | null;
  durationSeconds: number;
  status: ScenarioStatus;
  startedAt: string;
  endedAt?: string | null;
}

export interface StartSimulationPayload {
  buildingId: string;
  sessionName?: string;
  scenarioType?: ScenarioType;
  simulationIntervalSeconds?: number;
}

export interface ScenarioPayload {
  scenarioType: ScenarioType;
  scenarioName?: string;
  targetConfiguration?: string;
  durationSeconds?: number;
}

export interface ScenarioDefinition {
  type: ScenarioType;
  name: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' | 'VARIABLE';
}
