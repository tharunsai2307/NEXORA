export type DisplayMode =
  | 'DEFAULT'
  | 'RISK'
  | 'OCCUPANCY'
  | 'TEMPERATURE'
  | 'SMOKE'
  | 'AIR_QUALITY'
  | 'EMERGENCY';

export type CameraViewPreset =
  | 'PERSPECTIVE'
  | 'TOP'
  | 'FRONT'
  | 'SIDE'
  | 'RESET';

export interface DigitalTwinRoom {
  id: string;
  floorId: string;
  floorNumber: number;
  roomName: string;
  roomCode: string;
  roomType: string;
  description?: string;
  maximumCapacity: number;
  currentOccupancy: number;
  utilizationPercentage: number;
  posX: number;
  posY: number;
  posZ: number;
  width: number;
  depth: number;
  height: number;
  active: boolean;
  safetyStatus: string;
  riskScore: number;
  riskLevel: 'NORMAL' | 'WARNING' | 'HIGH_RISK' | 'CRITICAL';
  riskExplanation?: string;
  predictedOccupancy: number;
  anomalyDetected: boolean;
  anomalySeverity: string;
  anomalyScore: number;
  anomalyExplanation?: string;
  sensorReadings: Record<string, number>;
}

export interface DigitalTwinFloor {
  id: string;
  buildingId: string;
  floorName: string;
  floorNumber: number;
  description?: string;
  layoutWidth: number;
  layoutDepth: number;
  status: string;
  roomCount: number;
  totalCapacity: number;
  currentOccupancy: number;
  rooms: DigitalTwinRoom[];
}

export interface DigitalTwinBuilding {
  id: string;
  name: string;
  buildingCode: string;
  buildingType: string;
  address: string;
  totalFloors: number;
  status: string;
  roomCount: number;
  totalCapacity: number;
  currentOccupancy: number;
  overallRiskScore: number;
  overallRiskLevel: 'NORMAL' | 'WARNING' | 'HIGH_RISK' | 'CRITICAL';
  criticalRoomsCount: number;
  highRiskRoomsCount: number;
  warningRoomsCount: number;
  normalRoomsCount: number;
  floors: DigitalTwinFloor[];
}

export interface RoomLayoutUpdatePayload {
  posX: number;
  posY: number;
  posZ: number;
  width: number;
  depth: number;
  height: number;
}

