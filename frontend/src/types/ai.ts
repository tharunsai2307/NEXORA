export type AnomalySeverity = 'NORMAL' | 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type RiskLevel = 'NORMAL' | 'WARNING' | 'HIGH_RISK' | 'CRITICAL';

export interface AnomalyDetection {
  id: string;
  room: {
    id: string;
    roomName: string;
    roomCode: string;
    floor?: {
      id: string;
      floorNumber: number;
      building?: {
        id: string;
        name: string;
      };
    };
  };
  anomalyScore: number;
  severity: AnomalySeverity;
  anomalyDetected: boolean;
  contributingFeatures?: string;
  explanation: string;
  modelVersion: string;
  detectedAt: string;
}

export interface AiPrediction {
  id: string;
  room: {
    id: string;
    roomName: string;
    roomCode: string;
    floor?: {
      id: string;
      floorNumber: number;
      building?: {
        id: string;
        name: string;
      };
    };
  };
  predictionType: string;
  predictedValue: number;
  predictionHorizonMinutes: number;
  confidenceOrScore?: number;
  modelVersion: string;
  predictionTimestamp: string;
  createdAt: string;
}

export interface RiskAssessment {
  id: string;
  room: {
    id: string;
    roomName: string;
    roomCode: string;
    floor?: {
      id: string;
      floorNumber: number;
      building?: {
        id: string;
        name: string;
      };
    };
  };
  riskScore: number;
  riskLevel: RiskLevel;
  temperatureScore: number;
  smokeScore: number;
  airQualityScore: number;
  occupancyScore: number;
  anomalyScore: number;
  contributingFactors?: string;
  explanation: string;
  assessedAt: string;
}

export interface RoomAiOverview {
  roomId: string;
  roomName: string;
  roomNumber: string;
  floorId: string;
  floorNumber: number;
  buildingId: string;
  currentOccupancy: number;
  capacity: number;
  currentUtilizationPct: number;
  riskScore: number;
  riskLevel: RiskLevel;
  predictedOccupancy: number;
  predictedUtilizationPct: number;
  anomalyDetected: boolean;
  anomalySeverity: AnomalySeverity;
  anomalyScore: number;
  explanation: string;
  sensorSnapshots: Record<string, number>;
  lastEvaluatedAt: string;
}

export interface BuildingAiSummary {
  buildingId: string;
  buildingName: string;
  totalRoomsAnalyzed: number;
  averageRiskScore: number;
  overallRiskLevel: RiskLevel;
  activeAnomaliesCount: number;
  criticalRoomsCount: number;
  highRiskRoomsCount: number;
  warningRoomsCount: number;
  normalRoomsCount: number;
  severityBreakdown: Record<string, number>;
  lastAnalysisTimestamp: string;
}

export interface AiModelMetadata {
  model_name: string;
  model_version: string;
  model_type: string;
  training_dataset_size: number;
  training_timestamp?: string;
  performance_metrics: Record<string, any>;
  model_status: string;
}

export interface AiModelStatus {
  models: AiModelMetadata[];
  total_models: number;
  system_status: string;
}
