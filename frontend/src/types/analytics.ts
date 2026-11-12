export type PostMortemStatus = 'DRAFT' | 'REVIEWED' | 'FINALIZED';

export type ReportType =
  | 'EXECUTIVE_SUMMARY'
  | 'ENVIRONMENTAL_AUDIT'
  | 'OCCUPANCY_INTELLIGENCE'
  | 'AI_DIAGNOSTICS'
  | 'INCIDENT_LOG'
  | 'EVACUATION_REVIEW'
  | 'POST_MORTEM';

export type ReportFormat = 'PDF' | 'CSV';

export interface AnalyticsOverviewResponse {
  totalReadings: number;
  totalIncidents: number;
  criticalIncidents: number;
  averageOccupancy: number;
  peakOccupancy: number;
  averageTemperature: number;
  averageSmokeLevel: number;
  averageAirQuality: number;
  averageHumidity: number;
  totalAnomaliesDetected: number;
  highRiskRoomsCount: number;
  totalEvacuationSessions: number;
  averageEvacuationDurationSeconds: number;
  systemSafetyScore: number;
}

export interface SensorTimeSeriesPointDto {
  timestamp: string;
  avgValue: number;
  minValue: number;
  maxValue: number;
  count: number;
}

export interface RoomMetricComparisonDto {
  roomId: string;
  roomNumber: string;
  roomName: string;
  avgValue: number;
  peakValue: number;
  violationCount: number;
}

export interface SensorAnalyticsResponse {
  sensorType: string;
  totalReadings: number;
  avgValue: number;
  minValue: number;
  maxValue: number;
  thresholdViolations: number;
  timeSeries: SensorTimeSeriesPointDto[];
  roomComparisons: RoomMetricComparisonDto[];
}

export interface HourlyOccupancyDto {
  hour: number;
  avgOccupancy: number;
  peakOccupancy: number;
}

export interface FloorOccupancyDto {
  floorId: string;
  floorNumber: number;
  floorName: string;
  occupancy: number;
  capacity: number;
  utilizationPercent: number;
}

export interface RoomOccupancyDto {
  roomId: string;
  roomNumber: string;
  roomName: string;
  occupancy: number;
  capacity: number;
  utilizationPercent: number;
}

export interface OccupancyComparisonPointDto {
  timestamp: string;
  roomId: string;
  roomNumber: string;
  actualOccupancy: number;
  predictedOccupancy: number;
  variance: number;
}

export interface OccupancyAnalyticsResponse {
  currentTotalOccupancy: number;
  averageDailyOccupancy: number;
  peakOccupancy: number;
  peakHour: number;
  overallUtilizationPercent: number;
  hourlyDistribution: HourlyOccupancyDto[];
  floorDistribution: FloorOccupancyDto[];
  roomOccupancy: RoomOccupancyDto[];
  actualVsPredicted: OccupancyComparisonPointDto[];
}

export interface AnomalyRoomSummaryDto {
  roomId: string;
  roomNumber: string;
  roomName: string;
  anomalyCount: number;
  highestSeverity: string;
  lastAnomalyAt?: string;
}

export interface AnomalyTrendPointDto {
  timeLabel: string;
  anomalyCount: number;
  avgScore: number;
}

export interface AiModelSummaryDto {
  modelType: string;
  version: string;
  status: string;
  accuracy?: number;
}

export interface AiAnalyticsResponse {
  totalAnomalies: number;
  criticalRiskRooms: number;
  highRiskRooms: number;
  normalRooms: number;
  avgAnomalyScore: number;
  riskLevelDistribution: Record<string, number>;
  topAnomalyRooms: AnomalyRoomSummaryDto[];
  anomaliesOverTime: AnomalyTrendPointDto[];
  modelMetadata: AiModelSummaryDto[];
}

export interface IncidentTrendPointDto {
  timeLabel: string;
  count: number;
  criticalCount: number;
}

export interface IncidentHotspotDto {
  roomId: string;
  roomNumber: string;
  roomName: string;
  incidentCount: number;
  mostFrequentCategory: string;
}

export interface IncidentAnalyticsResponse {
  totalIncidents: number;
  activeIncidents: number;
  resolvedIncidents: number;
  meanTimeToAcknowledgeMinutes: number;
  meanTimeToResolveMinutes: number;
  severityDistribution: Record<string, number>;
  categoryDistribution: Record<string, number>;
  incidentsOverTime: IncidentTrendPointDto[];
  hotspotRooms: IncidentHotspotDto[];
}

export interface EvacuationClearancePointDto {
  elapsedMinutes: number;
  clearedRooms: number;
  remainingRooms: number;
  personsEvacuated: number;
}

export interface EvacuationAnalyticsResponse {
  totalSessions: number;
  completedSessions: number;
  activeSessions: number;
  averageDurationSeconds: number;
  totalPersonsEvacuated: number;
  assistanceRequestsHandled: number;
  blockedConnectionsEncountered: number;
  clearanceTimeline: EvacuationClearancePointDto[];
}

export interface HistoricalDataPointDto {
  id: string;
  timestamp: string;
  recordType: string;
  buildingId?: string;
  buildingName?: string;
  floorNumber?: number;
  roomId?: string;
  roomNumber?: string;
  metricName: string;
  metricValue: string;
  severityOrStatus: string;
  details?: string;
}

export interface HistoricalQueryResponse {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  records: HistoricalDataPointDto[];
}

export interface PostMortemRequest {
  reportTitle?: string;
  reportStatus?: PostMortemStatus;
  executiveSummary?: string;
  incidentOverview?: string;
  timelineNarrative?: string;
  sensorAnalysis?: string;
  aiAssessmentSummary?: string;
  responseEvaluation?: string;
  evacuationSummary?: string;
  rootCauseAssessment?: string;
  correctiveActions?: string;
  preventiveRecommendations?: string;
  reviewedBy?: string;
}

export interface PostMortemResponse {
  id: string;
  incidentId: string;
  incidentReferenceNumber?: string;
  incidentCategory?: string;
  incidentSeverity?: string;
  incidentTitle?: string;
  buildingId?: string;
  buildingName?: string;
  floorName?: string;
  roomNumber?: string;
  reportTitle: string;
  reportStatus: PostMortemStatus;
  authorId?: string;
  authorName?: string;
  executiveSummary?: string;
  incidentOverview?: string;
  timelineNarrative?: string;
  sensorAnalysis?: string;
  aiAssessmentSummary?: string;
  responseEvaluation?: string;
  evacuationSummary?: string;
  rootCauseAssessment?: string;
  correctiveActions?: string;
  preventiveRecommendations?: string;
  reviewedBy?: string;
  reviewedAt?: string;
  finalizedAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface GeneratedReportResponse {
  id: string;
  reportType: ReportType;
  buildingId?: string;
  buildingName?: string;
  reportName: string;
  reportFormat: ReportFormat;
  dateRangeStart?: string;
  dateRangeEnd?: string;
  generatedBy: string;
  summaryMetadata?: string;
  createdAt: string;
}

