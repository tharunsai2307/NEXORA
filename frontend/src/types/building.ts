export type BuildingType =
  | 'EDUCATIONAL'
  | 'COMMERCIAL'
  | 'RESIDENTIAL'
  | 'INDUSTRIAL'
  | 'HEALTHCARE'
  | 'OTHER';

export type BuildingStatus = 'ACTIVE' | 'MAINTENANCE' | 'INACTIVE' | 'EMERGENCY';

export type FloorStatus = 'ACTIVE' | 'MAINTENANCE' | 'RESTRICTED' | 'EVACUATING' | 'INACTIVE';

export type RoomType =
  | 'CLASSROOM'
  | 'LABORATORY'
  | 'OFFICE'
  | 'CONFERENCE_ROOM'
  | 'CORRIDOR'
  | 'STAIRCASE'
  | 'LOBBY'
  | 'CAFETERIA'
  | 'SERVER_ROOM'
  | 'STORAGE_ROOM'
  | 'EMERGENCY_EXIT_AREA'
  | 'OTHER';

export type SafetyStatus = 'NORMAL' | 'WARNING' | 'CRITICAL';

export interface Building {
  id: string;
  name: string;
  buildingCode: string;
  buildingType: BuildingType;
  address: string;
  totalFloors: number;
  status: BuildingStatus;
  description?: string;
  roomCount: number;
  totalCapacity: number;
  currentOccupancy: number;
  createdAt: string;
  updatedAt: string;
}

export interface BuildingPayload {
  name: string;
  buildingCode: string;
  buildingType: BuildingType;
  address: string;
  status?: BuildingStatus;
  description?: string;
}

export interface Floor {
  id: string;
  buildingId: string;
  floorName: string;
  floorNumber: number;
  description?: string;
  layoutWidth: number;
  layoutDepth: number;
  status: FloorStatus;
  roomCount: number;
  totalCapacity: number;
  currentOccupancy: number;
  createdAt: string;
  updatedAt: string;
}

export interface FloorPayload {
  floorName: string;
  floorNumber: number;
  description?: string;
  layoutWidth?: number;
  layoutDepth?: number;
  status?: FloorStatus;
}

export interface Room {
  id: string;
  floorId: string;
  floorNumber: number;
  buildingId: string;
  roomName: string;
  roomCode: string;
  roomType: RoomType;
  description?: string;
  maximumCapacity: number;
  currentOccupancy: number;
  safetyStatus: SafetyStatus;
  posX: number;
  posY: number;
  posZ: number;
  width: number;
  depth: number;
  height: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RoomPayload {
  roomName: string;
  roomCode: string;
  roomType: RoomType;
  description?: string;
  maximumCapacity: number;
  currentOccupancy?: number;
  safetyStatus?: SafetyStatus;
  posX?: number;
  posY?: number;
  posZ?: number;
  width?: number;
  depth?: number;
  height?: number;
  active?: boolean;
}

export interface DashboardStats {
  totalBuildings: number;
  totalFloors: number;
  totalRooms: number;
  totalCapacity: number;
  currentOccupancy: number;
  occupancyPercentage: number;
  activeUsers: number;
  safetySummary: {
    NORMAL: number;
    WARNING: number;
    CRITICAL: number;
  };
}

