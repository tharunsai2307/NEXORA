export type SensorType =
  | 'TEMPERATURE'
  | 'HUMIDITY'
  | 'SMOKE'
  | 'AIR_QUALITY'
  | 'OCCUPANCY';

export type SensorUnit = 'CELSIUS' | 'PERCENT' | 'PPM' | 'AQI' | 'PEOPLE';

export type SensorStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ERROR';

export type ReadingSource = 'SIMULATED' | 'MANUAL';

export interface Sensor {
  id: string;
  roomId: string;
  roomName: string;
  roomCode: string;
  floorId: string;
  floorName: string;
  floorNumber: number;
  buildingId: string;
  buildingName: string;
  sensorCode: string;
  sensorName: string;
  sensorType: SensorType;
  unit: SensorUnit;
  unitSymbol: string;
  status: SensorStatus;
  simulationIntervalSeconds: number;
  lastReadingValue?: number | null;
  lastReadingAt?: string | null;
  createdAt: string;
}

export interface CreateSensorPayload {
  sensorCode?: string;
  sensorName: string;
  sensorType: SensorType;
  unit?: SensorUnit;
  simulationIntervalSeconds?: number;
}

export interface UpdateSensorPayload {
  sensorName?: string;
  simulationIntervalSeconds?: number;
  status?: SensorStatus;
}

export interface SensorReading {
  id: string;
  sensorId: string;
  sensorCode: string;
  sensorName: string;
  sensorType: SensorType;
  unit: SensorUnit;
  unitSymbol: string;
  readingValue: number;
  recordedAt: string;
  readingSource: ReadingSource;
  scenarioId?: string | null;
}

export interface SensorStats {
  totalSensors: number;
  activeSensors: number;
  inactiveSensors: number;
  errorSensors: number;
  simulationRunning: boolean;
  activeSessionId?: string | null;
  distributionByType: Record<string, number>;
}
