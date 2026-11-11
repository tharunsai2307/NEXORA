export type IncidentCategory =
  | 'FIRE'
  | 'SMOKE'
  | 'HIGH_TEMPERATURE'
  | 'POOR_AIR_QUALITY'
  | 'OVERCROWDING'
  | 'MANUAL_EMERGENCY'
  | 'OTHER';

export type IncidentSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type IncidentStatus =
  | 'DETECTED'
  | 'ACTIVE'
  | 'ACKNOWLEDGED'
  | 'RESPONDING'
  | 'RESOLVED'
  | 'CLOSED';

export type IncidentSource = 'SENSOR' | 'AI' | 'MANUAL';

export interface IncidentActivity {
  id: string;
  action: string;
  details: string;
  performedBy: string;
  performedByName: string;
  createdAt: string;
}

export interface Incident {
  id: string;
  referenceNumber: string;
  buildingId: string;
  buildingName?: string;
  floorId?: string;
  floorName?: string;
  floorNumber?: number;
  roomId?: string;
  roomName?: string;
  roomCode?: string;
  category: IncidentCategory;
  severity: IncidentSeverity;
  status: IncidentStatus;
  detectionSource: IncidentSource;
  title: string;
  description?: string;
  assignedOfficerId?: string;
  assignedOfficerName?: string;
  detectedAt: string;
  acknowledgedAt?: string;
  resolvedAt?: string;
  closedAt?: string;
  activities?: IncidentActivity[];
}

export type EvacuationNodeType = 'ROOM' | 'CORRIDOR' | 'STAIRCASE' | 'EMERGENCY_EXIT';
export type HazardLevel = 'NONE' | 'LOW' | 'HIGH' | 'CRITICAL';
export type RouteStatus = 'SAFE' | 'CAUTION' | 'NO_SAFE_ROUTE';

export interface RouteWaypoint {
  nodeId: string;
  nodeName: string;
  nodeType: EvacuationNodeType;
  floorNumber: number;
  posX: number;
  posY: number;
  posZ: number;
}

export interface EvacuationRoute {
  id: string;
  sessionId?: string;
  buildingId: string;
  originRoomId: string;
  originRoomName: string;
  originNodeId: string;
  destinationExitId?: string;
  destinationExitName?: string;
  orderedWaypoints: RouteWaypoint[];
  totalDistanceMeters: number;
  routeStatus: RouteStatus;
  floorTransitions?: string;
  calculationReason?: string;
  calculatedAt: string;
}

export interface EvacuationNode {
  id: string;
  buildingId: string;
  floorId: string;
  floorNumber: number;
  roomId?: string;
  roomName?: string;
  nodeName: string;
  nodeCode: string;
  nodeType: EvacuationNodeType;
  posX: number;
  posY: number;
  posZ: number;
  isSafe: boolean;
  isBlocked: boolean;
}

export interface EvacuationConnection {
  id: string;
  buildingId: string;
  fromNodeId: string;
  fromNodeName: string;
  toNodeId: string;
  toNodeName: string;
  distanceMeters: number;
  hazardLevel: HazardLevel;
  isBlocked: boolean;
  isAccessible: boolean;
  bidirectional: boolean;
}

export interface EvacuationGraph {
  buildingId: string;
  nodes: EvacuationNode[];
  connections: EvacuationConnection[];
}

export type RoomEvacuationState =
  | 'NOT_STARTED'
  | 'EVACUATING'
  | 'CLEARED'
  | 'BLOCKED'
  | 'NEEDS_ASSISTANCE';

export interface EvacuationRoomStatus {
  id: string;
  roomId: string;
  roomName: string;
  roomCode: string;
  floorNumber: number;
  status: RoomEvacuationState;
  initialOccupancy: number;
  remainingOccupants: number;
  occupantsNeedingAssistance: number;
  clearedAt?: string;
  notes?: string;
}

export type EvacuationSessionStatus =
  | 'NOT_STARTED'
  | 'ACTIVE'
  | 'PAUSED'
  | 'COMPLETED'
  | 'CANCELLED';

export interface EvacuationSession {
  id: string;
  incidentId?: string;
  buildingId: string;
  buildingName?: string;
  sessionStatus: EvacuationSessionStatus;
  sessionName: string;
  initiatedBy: string;
  notes?: string;
  totalRooms: number;
  clearedRooms: number;
  evacuatingRooms: number;
  blockedRooms: number;
  totalInitialOccupants: number;
  remainingOccupants: number;
  progressPercentage: number;
  startedAt?: string;
  endedAt?: string;
  roomStatuses: EvacuationRoomStatus[];
}

export interface BuildingEmergencyStatus {
  buildingId: string;
  buildingName: string;
  emergencyActive: boolean;
  activeIncidentCount: number;
  criticalIncidentCount: number;
  activeSession?: EvacuationSession;
  totalOccupantsAtRisk: number;
  totalRoomsCleared: number;
  totalRoomsEvacuating: number;
  safeExitsCount: number;
  blockedRoutesCount: number;
  activeIncidents: Incident[];
}

export interface CreateIncidentPayload {
  buildingId: string;
  floorId?: string;
  roomId?: string;
  category: IncidentCategory;
  severity: IncidentSeverity;
  detectionSource?: IncidentSource;
  title: string;
  description?: string;
}

export interface UpdateIncidentStatusPayload {
  status: IncidentStatus;
  notes?: string;
}

export interface StartEvacuationPayload {
  incidentId?: string;
  sessionName?: string;
  notes?: string;
}
