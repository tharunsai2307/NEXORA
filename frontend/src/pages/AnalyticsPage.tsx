import React, { useState, useEffect } from 'react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  LineChart,
  Line,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import {
  BarChart3,
  Activity,
  Download,
  Building2,
  ShieldAlert,
  Users,
  Flame,
  Wind,
  Thermometer,
  CheckCircle2,
  Layers,
  FileCheck,
  RefreshCw,
  ChevronRight,
  ChevronLeft,
  FileDown,
  Cpu,
  Compass,
} from 'lucide-react';
import { analyticsApi, reportsApi, buildingApi, aiApi } from '../services/api';
import {
  AnalyticsOverviewResponse,
  SensorAnalyticsResponse,
  OccupancyAnalyticsResponse,
  AiAnalyticsResponse,
  IncidentAnalyticsResponse,
  EvacuationAnalyticsResponse,
  HistoricalQueryResponse,
  PostMortemResponse,
  PostMortemRequest,
  ReportType,
  ReportFormat,
  GeneratedReportResponse,
} from '../types/analytics';
import { Building } from '../types/building';
import { useAuth } from '../context/AuthContext';

export const AnalyticsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER']);

  // Navigation tabs
  const [activeTab, setActiveTab] = useState<
    'overview' | 'sensors' | 'occupancy' | 'ai' | 'incidents' | 'evacuation' | 'postmortems' | 'history'
  >('overview');

  // Filter state
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [dateRangeFilter, setDateRangeFilter] = useState<'24h' | '7d' | '30d' | 'all'>('7d');
  const [loading, setLoading] = useState<boolean>(true);
  const [exporting, setExporting] = useState<boolean>(false);

  // Tab Data States
  const [overview, setOverview] = useState<AnalyticsOverviewResponse | null>(null);
  const [selectedSensorType, setSelectedSensorType] = useState<string>('TEMPERATURE');
  const [sensorData, setSensorData] = useState<SensorAnalyticsResponse | null>(null);
  const [occupancyData, setOccupancyData] = useState<OccupancyAnalyticsResponse | null>(null);
  const [aiData, setAiData] = useState<AiAnalyticsResponse | null>(null);
  const [incidentData, setIncidentData] = useState<IncidentAnalyticsResponse | null>(null);
  const [evacuationData, setEvacuationData] = useState<EvacuationAnalyticsResponse | null>(null);

  // Post-Mortem Studio State
  const [postMortems, setPostMortems] = useState<PostMortemResponse[]>([]);
  const [selectedPostMortem, setSelectedPostMortem] = useState<PostMortemResponse | null>(null);
  const [editingPostMortem, setEditingPostMortem] = useState<PostMortemRequest>({});
  const [savingPostMortem, setSavingPostMortem] = useState<boolean>(false);
  const [postMortemStatusFilter, setPostMortemStatusFilter] = useState<string>('ALL');

  // Historical Explorer State
  const [historyData, setHistoryData] = useState<HistoricalQueryResponse | null>(null);
  const [historyRecordType, setHistoryRecordType] = useState<string>('ALL');
  const [historyPage, setHistoryPage] = useState<number>(0);

  // Reports Catalog
  const [catalogReports, setCatalogReports] = useState<GeneratedReportResponse[]>([]);

  // AI model retraining controls
  const [trainSamples, setTrainSamples] = useState<number>(2000);
  const [contamination, setContamination] = useState<number>(0.05);
  const [training, setTraining] = useState<boolean>(false);
  const [trainMessage, setTrainMessage] = useState<string | null>(null);

  // Compute start/end dates
  const getDateBounds = () => {
    const now = new Date();
    let start: Date;
    switch (dateRangeFilter) {
      case '24h':
        start = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        break;
      case '7d':
        start = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
        break;
      case '30d':
        start = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        break;
      case 'all':
      default:
        start = new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);
        break;
    }
    return {
      start: start.toISOString(),
      end: now.toISOString(),
    };
  };

  // Initial load: Buildings & Overview
  useEffect(() => {
    buildingApi.getBuildings().then((b) => {
      setBuildings(b);
      if (b.length > 0 && !selectedBuildingId) {
        setSelectedBuildingId(b[0].id);
      }
    }).catch(console.error);
  }, []);

  // Fetch active tab data
  const fetchData = async () => {
    setLoading(true);
    const { start, end } = getDateBounds();
    const bId = selectedBuildingId || undefined;

    try {
      if (activeTab === 'overview') {
        const [ov, cat] = await Promise.all([
          analyticsApi.getOverview({ buildingId: bId, start, end }),
          reportsApi.listReports(bId).catch(() => []),
        ]);
        setOverview(ov);
        setCatalogReports(cat);
      } else if (activeTab === 'sensors') {
        const s = await analyticsApi.getSensorAnalytics({
          buildingId: bId,
          sensorType: selectedSensorType,
          start,
          end,
        });
        setSensorData(s);
      } else if (activeTab === 'occupancy') {
        const o = await analyticsApi.getOccupancyAnalytics({ buildingId: bId, start, end });
        setOccupancyData(o);
      } else if (activeTab === 'ai') {
        const a = await analyticsApi.getAiAnalytics({ buildingId: bId, start, end });
        setAiData(a);
      } else if (activeTab === 'incidents') {
        const i = await analyticsApi.getIncidentAnalytics({ buildingId: bId, start, end });
        setIncidentData(i);
      } else if (activeTab === 'evacuation') {
        const e = await analyticsApi.getEvacuationAnalytics({ buildingId: bId, start, end });
        setEvacuationData(e);
      } else if (activeTab === 'postmortems') {
        const pms = await reportsApi.listPostMortems(postMortemStatusFilter);
        setPostMortems(pms);
        if (pms.length > 0 && !selectedPostMortem) {
          selectPostMortem(pms[0]);
        }
      } else if (activeTab === 'history') {
        const h = await analyticsApi.queryHistory({
          buildingId: bId,
          recordType: historyRecordType,
          start,
          end,
          page: historyPage,
          size: 25,
        });
        setHistoryData(h);
      }
    } catch (err) {
      console.error('Failed to load analytics data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [activeTab, selectedBuildingId, dateRangeFilter, selectedSensorType, historyPage, historyRecordType, postMortemStatusFilter]);

  const selectPostMortem = (pm: PostMortemResponse) => {
    setSelectedPostMortem(pm);
    setEditingPostMortem({
      reportTitle: pm.reportTitle,
      reportStatus: pm.reportStatus,
      executiveSummary: pm.executiveSummary,
      incidentOverview: pm.incidentOverview,
      timelineNarrative: pm.timelineNarrative,
      sensorAnalysis: pm.sensorAnalysis,
      aiAssessmentSummary: pm.aiAssessmentSummary,
      responseEvaluation: pm.responseEvaluation,
      evacuationSummary: pm.evacuationSummary,
      rootCauseAssessment: pm.rootCauseAssessment,
      correctiveActions: pm.correctiveActions,
      preventiveRecommendations: pm.preventiveRecommendations,
      reviewedBy: pm.reviewedBy,
    });
  };

  const downloadBlob = (blob: Blob, filename: string) => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
  };

  const handleExportStandardReport = async (reportType: ReportType, format: ReportFormat) => {
    setExporting(true);
    const { start, end } = getDateBounds();
    try {
      const blob = await reportsApi.exportReport({
        reportType,
        buildingId: selectedBuildingId || undefined,
        format,
        start,
        end,
      });
      const ext = format === 'PDF' ? 'pdf' : 'csv';
      downloadBlob(blob, `nexora-${reportType.toLowerCase()}-${Date.now()}.${ext}`);
    } catch (err) {
      console.error('Failed to export report', err);
    } finally {
      setExporting(false);
    }
  };

  const handleExportPostMortem = async (pmId: string, format: ReportFormat) => {
    try {
      const blob = await reportsApi.exportPostMortem(pmId, format);
      const ext = format === 'PDF' ? 'pdf' : 'csv';
      downloadBlob(blob, `post-mortem-${pmId.substring(0, 8)}.${ext}`);
    } catch (err) {
      console.error('Failed to export post-mortem', err);
    }
  };

  const handleSavePostMortem = async (newStatus?: 'DRAFT' | 'REVIEWED' | 'FINALIZED') => {
    if (!selectedPostMortem) return;
    setSavingPostMortem(true);
    try {
      const payload: PostMortemRequest = {
        ...editingPostMortem,
        reportStatus: newStatus || editingPostMortem.reportStatus || 'DRAFT',
      };
      if (newStatus === 'FINALIZED') {
        const updated = await reportsApi.finalizeIncidentPostMortem(selectedPostMortem.incidentId);
        selectPostMortem(updated);
      } else {
        const updated = await reportsApi.saveIncidentPostMortem(selectedPostMortem.incidentId, payload);
        selectPostMortem(updated);
      }
      const pms = await reportsApi.listPostMortems(postMortemStatusFilter);
      setPostMortems(pms);
    } catch (err) {
      console.error('Failed to save post-mortem', err);
    } finally {
      setSavingPostMortem(false);
    }
  };

  const handleRetrainAi = async () => {
    setTraining(true);
    setTrainMessage(null);
    try {
      await aiApi.trainModels({
        synthetic_samples: trainSamples,
        contamination: contamination,
        force_retrain: true,
      });
      setTrainMessage(`Model retraining completed successfully at ${new Date().toLocaleTimeString()}!`);
      const a = await analyticsApi.getAiAnalytics({ buildingId: selectedBuildingId || undefined });
      setAiData(a);
    } catch (err: any) {
      console.error('Failed to retrain models', err);
      setTrainMessage(`Retraining failed: ${err.message || 'Unknown error'}`);
    } finally {
      setTraining(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner Toolbar */}
      <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl shadow-xl flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/20 rounded-xl">
              <BarChart3 className="w-6 h-6 text-emerald-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-100 flex items-center gap-2">
                NEXORA Analytics & Reporting Engine
                <span className="text-xs px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-800 font-mono">
                  Phase 7 Complete
                </span>
              </h1>
              <p className="text-xs text-slate-400 mt-0.5">
                Unified cross-phase intelligence, predictive occupancy auditing, incident post-mortems, and regulatory exports
              </p>
            </div>
          </div>
        </div>

        {/* Filters and Controls */}
        <div className="flex flex-wrap items-center gap-3">
          {/* Building Selector */}
          <div className="flex items-center gap-1.5 bg-[#0e1624] px-3 py-1.5 rounded-lg border border-[#1e2e47]">
            <Building2 className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={selectedBuildingId}
              onChange={(e) => setSelectedBuildingId(e.target.value)}
              className="bg-transparent text-xs text-slate-200 focus:outline-none"
            >
              <option value="" className="bg-[#0e1624] text-slate-200">
                All Buildings (Portfolio)
              </option>
              {buildings.map((b) => (
                <option key={b.id} value={b.id} className="bg-[#0e1624] text-slate-200">
                  {b.name} ({b.buildingCode})
                </option>
              ))}
            </select>
          </div>

          {/* Date Range Filter */}
          <div className="flex items-center bg-[#0e1624] p-1 rounded-lg border border-[#1e2e47]">
            {(['24h', '7d', '30d', 'all'] as const).map((r) => (
              <button
                key={r}
                onClick={() => setDateRangeFilter(r)}
                className={`px-2.5 py-1 text-xs rounded font-medium transition-all ${
                  dateRangeFilter === r
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/40'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {r.toUpperCase()}
              </button>
            ))}
          </div>

          {/* Quick PDF/CSV Export Menu */}
          <div className="relative group">
            <button
              disabled={exporting}
              className="flex items-center gap-2 px-3.5 py-1.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-slate-900 font-semibold text-xs rounded-lg transition-all shadow-md shadow-emerald-950/40 disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5" />
              <span>{exporting ? 'Generating...' : 'Export Report'}</span>
            </button>
            <div className="absolute right-0 mt-1 w-56 bg-[#0e1624] border border-[#1e2e47] rounded-xl shadow-2xl p-2 hidden group-hover:block z-50">
              <div className="text-[10px] font-bold text-slate-400 uppercase tracking-wider px-2 py-1">
                Download PDF / CSV
              </div>
              <button
                onClick={() => handleExportStandardReport('EXECUTIVE_SUMMARY', 'PDF')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>Executive Summary</span>
                <span className="text-[10px] text-emerald-400 font-mono">PDF</span>
              </button>
              <button
                onClick={() => handleExportStandardReport('ENVIRONMENTAL_AUDIT', 'PDF')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>Environmental Audit</span>
                <span className="text-[10px] text-emerald-400 font-mono">PDF</span>
              </button>
              <button
                onClick={() => handleExportStandardReport('OCCUPANCY_INTELLIGENCE', 'PDF')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>Occupancy Intelligence</span>
                <span className="text-[10px] text-emerald-400 font-mono">PDF</span>
              </button>
              <button
                onClick={() => handleExportStandardReport('AI_DIAGNOSTICS', 'PDF')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>AI Diagnostics</span>
                <span className="text-[10px] text-emerald-400 font-mono">PDF</span>
              </button>
              <button
                onClick={() => handleExportStandardReport('INCIDENT_LOG', 'PDF')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>Incident Log</span>
                <span className="text-[10px] text-emerald-400 font-mono">PDF</span>
              </button>
              <div className="border-t border-[#1e2e47] my-1"></div>
              <button
                onClick={() => handleExportStandardReport('EXECUTIVE_SUMMARY', 'CSV')}
                className="w-full text-left px-2.5 py-1.5 text-xs text-slate-200 hover:bg-[#182337] rounded-lg flex items-center justify-between"
              >
                <span>Raw Metric Archive</span>
                <span className="text-[10px] text-cyan-400 font-mono">CSV</span>
              </button>
            </div>
          </div>

          <button
            onClick={fetchData}
            className="p-2 text-slate-400 hover:text-slate-100 hover:bg-[#111928] border border-[#1e2e47] rounded-lg transition-all"
            title="Refresh View"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </div>

      {/* Analytics Command Tabs */}
      <div className="flex overflow-x-auto gap-2 border-b border-[#1a2538] pb-1">
        {[
          { id: 'overview', label: 'Executive Dashboard', icon: BarChart3 },
          { id: 'sensors', label: 'Sensor Intelligence', icon: Thermometer },
          { id: 'occupancy', label: 'Occupancy & Forecast', icon: Users },
          { id: 'ai', label: 'Edge AI Diagnostics', icon: Cpu },
          { id: 'incidents', label: 'Incident Response & SLA', icon: ShieldAlert },
          { id: 'evacuation', label: 'Evacuation Drills', icon: Compass },
          { id: 'postmortems', label: 'Incident Post-Mortem Studio', icon: FileCheck },
          { id: 'history', label: 'Historical Data Explorer', icon: Layers },
        ].map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-4 py-2.5 text-xs font-medium rounded-t-xl transition-all whitespace-nowrap ${
                isActive
                  ? 'bg-[#0e1624] text-emerald-300 border-t border-l border-r border-[#1e2e47] border-b-2 border-b-emerald-400'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-[#0c121e]'
              }`}
            >
              <Icon className={`w-3.5 h-3.5 ${isActive ? 'text-emerald-400' : 'text-slate-400'}`} />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* TAB 1: EXECUTIVE DASHBOARD */}
      {activeTab === 'overview' && overview && (
        <div className="space-y-6">
          {/* Primary Metric KPI Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-3">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl relative overflow-hidden">
              <div className="text-[11px] text-slate-400 font-medium">System Safety Score</div>
              <div className="text-2xl font-bold font-mono text-emerald-400 mt-1 flex items-baseline gap-1">
                {overview.systemSafetyScore}
                <span className="text-xs text-slate-400 font-normal">/ 100</span>
              </div>
              <div className="text-[10px] text-emerald-500/80 mt-1 flex items-center gap-1">
                <CheckCircle2 className="w-2.5 h-2.5" /> High Confidence
              </div>
            </div>

            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <div className="text-[11px] text-slate-400 font-medium">Total Incidents</div>
              <div className="text-2xl font-bold font-mono text-slate-100 mt-1">{overview.totalIncidents}</div>
              <div className="text-[10px] text-slate-400 mt-1">
                <span className="text-red-400 font-semibold">{overview.criticalIncidents}</span> critical alerts
              </div>
            </div>

            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <div className="text-[11px] text-slate-400 font-medium">Average Occupancy</div>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">{overview.averageOccupancy}</div>
              <div className="text-[10px] text-slate-400 mt-1">Peak: {overview.peakOccupancy} persons</div>
            </div>

            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <div className="text-[11px] text-slate-400 font-medium">Telemetry Readings</div>
              <div className="text-2xl font-bold font-mono text-slate-100 mt-1">
                {overview.totalReadings.toLocaleString()}
              </div>
              <div className="text-[10px] text-emerald-400 mt-1">Active IoT Fleet</div>
            </div>

            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <div className="text-[11px] text-slate-400 font-medium">Edge AI Anomalies</div>
              <div className="text-2xl font-bold font-mono text-amber-400 mt-1">{overview.totalAnomaliesDetected}</div>
              <div className="text-[10px] text-slate-400 mt-1">{overview.highRiskRoomsCount} high-risk zones</div>
            </div>

            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <div className="text-[11px] text-slate-400 font-medium">Avg Evac Clearance</div>
              <div className="text-2xl font-bold font-mono text-purple-400 mt-1">
                {overview.averageEvacuationDurationSeconds}s
              </div>
              <div className="text-[10px] text-slate-400 mt-1">{overview.totalEvacuationSessions} drills executed</div>
            </div>
          </div>

          {/* Environmental Gauges Row */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
              <div>
                <span className="text-[11px] text-slate-400 flex items-center gap-1.5">
                  <Thermometer className="w-3.5 h-3.5 text-orange-400" /> Average Temperature
                </span>
                <div className="text-xl font-bold font-mono text-slate-100 mt-1">{overview.averageTemperature} °C</div>
                <div className="text-[10px] text-slate-400">Target Range: 20 - 24 °C</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-orange-500/10 border border-orange-500/20 flex items-center justify-center font-mono text-orange-400 text-xs font-bold">
                ENV
              </div>
            </div>

            <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
              <div>
                <span className="text-[11px] text-slate-400 flex items-center gap-1.5">
                  <Wind className="w-3.5 h-3.5 text-cyan-400" /> Air Quality (AQI)
                </span>
                <div className="text-xl font-bold font-mono text-slate-100 mt-1">{overview.averageAirQuality} AQI</div>
                <div className="text-[10px] text-emerald-400">Good Quality (&lt; 50)</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-cyan-500/10 border border-cyan-500/20 flex items-center justify-center font-mono text-cyan-400 text-xs font-bold">
                AQI
              </div>
            </div>

            <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
              <div>
                <span className="text-[11px] text-slate-400 flex items-center gap-1.5">
                  <Flame className="w-3.5 h-3.5 text-red-400" /> Smoke Index
                </span>
                <div className="text-xl font-bold font-mono text-slate-100 mt-1">{overview.averageSmokeLevel} ppm</div>
                <div className="text-[10px] text-slate-400">Threshold: 2.0 ppm</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-red-500/10 border border-red-500/20 flex items-center justify-center font-mono text-red-400 text-xs font-bold">
                PPM
              </div>
            </div>

            <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
              <div>
                <span className="text-[11px] text-slate-400 flex items-center gap-1.5">
                  <Activity className="w-3.5 h-3.5 text-blue-400" /> Relative Humidity
                </span>
                <div className="text-xl font-bold font-mono text-slate-100 mt-1">{overview.averageHumidity} %</div>
                <div className="text-[10px] text-slate-400">Standard: 40 - 60 %</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-blue-500/10 border border-blue-500/20 flex items-center justify-center font-mono text-blue-400 text-xs font-bold">
                RH
              </div>
            </div>
          </div>

          {/* Generated Reports Archive Catalog Table */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-bold text-slate-200">Generated Reports Catalog</h3>
                <p className="text-xs text-slate-400">Formal regulatory audit logs, post-mortems, and compliance snapshots</p>
              </div>
              <button
                onClick={() => handleExportStandardReport('EXECUTIVE_SUMMARY', 'PDF')}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-[#121c2e] hover:bg-[#1a273f] text-emerald-400 text-xs rounded-lg border border-emerald-500/30 transition-all"
              >
                <FileDown className="w-3.5 h-3.5" />
                <span>Generate New Executive PDF</span>
              </button>
            </div>

            {catalogReports.length === 0 ? (
              <div className="text-center py-8 text-xs text-slate-400 border border-dashed border-[#1a2538] rounded-xl">
                No formal reports generated yet. Click "Export Report" above to generate your first audit document.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-xs text-left">
                  <thead className="bg-[#0e1624] text-slate-400 uppercase text-[10px] font-semibold border-b border-[#1a2538]">
                    <tr>
                      <th className="py-2.5 px-4">Report Name</th>
                      <th className="py-2.5 px-3">Type</th>
                      <th className="py-2.5 px-3">Format</th>
                      <th className="py-2.5 px-3">Generated By</th>
                      <th className="py-2.5 px-3">Timestamp</th>
                      <th className="py-2.5 px-4 text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#152033]">
                    {catalogReports.slice(0, 8).map((r) => (
                      <tr key={r.id} className="hover:bg-[#0c121e] transition-colors">
                        <td className="py-2.5 px-4 font-medium text-slate-200">{r.reportName}</td>
                        <td className="py-2.5 px-3">
                          <span className="px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800 text-[10px] font-mono">
                            {r.reportType}
                          </span>
                        </td>
                        <td className="py-2.5 px-3">
                          <span className="font-mono text-slate-300 font-semibold">{r.reportFormat}</span>
                        </td>
                        <td className="py-2.5 px-3 text-slate-400">{r.generatedBy}</td>
                        <td className="py-2.5 px-3 text-slate-400 font-mono text-[11px]">
                          {new Date(r.createdAt).toLocaleString()}
                        </td>
                        <td className="py-2.5 px-4 text-right">
                          <button
                            onClick={() => handleExportStandardReport(r.reportType, r.reportFormat)}
                            className="text-emerald-400 hover:text-emerald-300 font-medium text-xs inline-flex items-center gap-1"
                          >
                            <Download className="w-3 h-3" /> Re-Download
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 2: SENSOR INTELLIGENCE */}
      {activeTab === 'sensors' && sensorData && (
        <div className="space-y-6">
          {/* Sensor Type Selector */}
          <div className="flex flex-wrap items-center gap-2">
            {[
              { type: 'TEMPERATURE', label: 'Temperature (°C)', icon: Thermometer },
              { type: 'HUMIDITY', label: 'Humidity (%)', icon: Activity },
              { type: 'SMOKE', label: 'Smoke Level (ppm)', icon: Flame },
              { type: 'AIR_QUALITY', label: 'Air Quality (AQI)', icon: Wind },
              { type: 'OCCUPANCY', label: 'Occupancy Flow', icon: Users },
            ].map((s) => {
              const Icon = s.icon;
              const isSelected = selectedSensorType === s.type;
              return (
                <button
                  key={s.type}
                  onClick={() => setSelectedSensorType(s.type)}
                  className={`flex items-center gap-2 px-3.5 py-2 text-xs font-medium rounded-xl border transition-all ${
                    isSelected
                      ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/40 shadow-lg shadow-emerald-950/30'
                      : 'bg-[#0a0f1d] text-slate-400 border-[#1a263d] hover:text-slate-200'
                  }`}
                >
                  <Icon className={`w-3.5 h-3.5 ${isSelected ? 'text-emerald-400' : 'text-slate-400'}`} />
                  <span>{s.label}</span>
                </button>
              );
            })}
          </div>

          {/* Metric KPI Banner */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Average Metric</span>
              <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">{sensorData.avgValue}</div>
              <div className="text-[10px] text-slate-400 mt-1">Across all deployed rooms</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Minimum Recorded</span>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">{sensorData.minValue}</div>
              <div className="text-[10px] text-slate-400 mt-1">Lowest reading in range</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Peak Recorded</span>
              <div className="text-2xl font-bold font-mono text-orange-400 mt-1">{sensorData.maxValue}</div>
              <div className="text-[10px] text-slate-400 mt-1">Highest spike detected</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Threshold Violations</span>
              <div className="text-2xl font-bold font-mono text-red-400 mt-1">{sensorData.thresholdViolations}</div>
              <div className="text-[10px] text-slate-400 mt-1">Safety trigger events</div>
            </div>
          </div>

          {/* Time Series Recharts LineChart */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h3 className="text-sm font-bold text-slate-200">
                  {selectedSensorType} Telemetry Historical Trajectory
                </h3>
                <p className="text-xs text-slate-400">Downsampled multi-sensor progression curve</p>
              </div>
              <div className="text-xs font-mono text-slate-400">{sensorData.totalReadings} data points</div>
            </div>

            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={sensorData.timeSeries}>
                  <defs>
                    <linearGradient id="sensorGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                      <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" stroke="#162234" />
                  <XAxis dataKey="timestamp" stroke="#64748b" tick={{ fontSize: 10 }} />
                  <YAxis stroke="#64748b" tick={{ fontSize: 10 }} />
                  <Tooltip
                    contentStyle={{ backgroundColor: '#0e1624', borderColor: '#1e2e47', borderRadius: '8px', fontSize: '11px' }}
                  />
                  <Area type="monotone" dataKey="avgValue" stroke="#10b981" strokeWidth={2} fillOpacity={1} fill="url(#sensorGrad)" name="Average" />
                  <Line type="monotone" dataKey="maxValue" stroke="#f59e0b" strokeWidth={1.5} dot={false} name="Peak" />
                  <Line type="monotone" dataKey="minValue" stroke="#06b6d4" strokeWidth={1.5} dot={false} name="Min" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Room-by-Room Environmental Violation Comparison Table */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <h3 className="text-sm font-bold text-slate-200 mb-4">Room Sensor Breakdown & Violations</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-[#0e1624] text-slate-400 uppercase text-[10px] font-semibold border-b border-[#1a2538]">
                  <tr>
                    <th className="py-2.5 px-4">Room Code</th>
                    <th className="py-2.5 px-4">Room Name</th>
                    <th className="py-2.5 px-3">Average Value</th>
                    <th className="py-2.5 px-3">Peak Value</th>
                    <th className="py-2.5 px-3">Threshold Violations</th>
                    <th className="py-2.5 px-4 text-right">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#152033]">
                  {sensorData.roomComparisons.map((r) => (
                    <tr key={r.roomId} className="hover:bg-[#0c121e]">
                      <td className="py-2.5 px-4 font-mono font-semibold text-slate-200">{r.roomNumber}</td>
                      <td className="py-2.5 px-4 text-slate-300">{r.roomName}</td>
                      <td className="py-2.5 px-3 font-mono text-emerald-400">{r.avgValue}</td>
                      <td className="py-2.5 px-3 font-mono text-orange-400">{r.peakValue}</td>
                      <td className="py-2.5 px-3 font-mono">
                        <span className={r.violationCount > 0 ? 'text-red-400 font-bold' : 'text-slate-400'}>
                          {r.violationCount}
                        </span>
                      </td>
                      <td className="py-2.5 px-4 text-right">
                        <span
                          className={`px-2 py-0.5 rounded text-[10px] font-mono ${
                            r.violationCount > 0
                              ? 'bg-red-950 text-red-400 border border-red-800'
                              : 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                          }`}
                        >
                          {r.violationCount > 0 ? 'ALERT' : 'OPTIMAL'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 3: OCCUPANCY & FORECAST */}
      {activeTab === 'occupancy' && occupancyData && (
        <div className="space-y-6">
          {/* Top Occupancy KPI Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Current Total Occupancy</span>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">
                {occupancyData.currentTotalOccupancy}
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Active individuals inside</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Design Utilization Rate</span>
              <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">
                {occupancyData.overallUtilizationPercent}%
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Overall capacity ratio</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Peak Daily Headcount</span>
              <div className="text-2xl font-bold font-mono text-orange-400 mt-1">
                {occupancyData.peakOccupancy}
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Peak Hour: {occupancyData.peakHour}:00</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Average Daily Flow</span>
              <div className="text-2xl font-bold font-mono text-purple-400 mt-1">
                {occupancyData.averageDailyOccupancy}
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Continuous occupancy baseline</div>
            </div>
          </div>

          {/* Diurnal Occupancy Distribution & Actual vs Predicted Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* 24-Hour Diurnal BarChart */}
            <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
              <h3 className="text-sm font-bold text-slate-200 mb-1">Diurnal Hourly Distribution (24-Hour Cycle)</h3>
              <p className="text-xs text-slate-400 mb-4">Traffic flow curve showing business hour peaks</p>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={occupancyData.hourlyDistribution}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#162234" />
                    <XAxis dataKey="hour" stroke="#64748b" tickFormatter={(h) => `${h}:00`} tick={{ fontSize: 9 }} />
                    <YAxis stroke="#64748b" tick={{ fontSize: 9 }} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0e1624', borderColor: '#1e2e47', borderRadius: '8px', fontSize: '11px' }}
                    />
                    <Bar dataKey="avgOccupancy" fill="#06b6d4" name="Avg Occupancy" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="peakOccupancy" fill="#3b82f6" name="Peak Occupancy" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Actual vs AI Predicted Occupancy Comparison */}
            <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
              <h3 className="text-sm font-bold text-slate-200 mb-1">Actual vs 15-Min AI Predicted Occupancy</h3>
              <p className="text-xs text-slate-400 mb-4">Random Forest model validation against ground-truth readings</p>
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={occupancyData.actualVsPredicted}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#162234" />
                    <XAxis dataKey="roomNumber" stroke="#64748b" tick={{ fontSize: 9 }} />
                    <YAxis stroke="#64748b" tick={{ fontSize: 9 }} />
                    <Tooltip
                      contentStyle={{ backgroundColor: '#0e1624', borderColor: '#1e2e47', borderRadius: '8px', fontSize: '11px' }}
                    />
                    <Legend />
                    <Line type="monotone" dataKey="actualOccupancy" stroke="#10b981" strokeWidth={2} name="Actual" />
                    <Line type="monotone" dataKey="predictedOccupancy" stroke="#f59e0b" strokeWidth={2} strokeDasharray="4 4" name="AI Forecast" />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* Floor Utilization Bar Table */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <h3 className="text-sm font-bold text-slate-200 mb-4">Floor Occupancy Capacity Utilization</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {occupancyData.floorDistribution.map((f) => (
                <div key={f.floorId} className="p-4 bg-[#0c121e] border border-[#1c283c] rounded-xl">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-slate-200 text-xs">{f.floorName}</span>
                    <span className="text-xs font-mono text-emerald-400 font-bold">{f.utilizationPercent}%</span>
                  </div>
                  <div className="w-full bg-[#182337] h-2 rounded-full mt-2 overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all ${
                        f.utilizationPercent > 80 ? 'bg-red-500' : f.utilizationPercent > 50 ? 'bg-amber-500' : 'bg-emerald-500'
                      }`}
                      style={{ width: `${Math.min(f.utilizationPercent, 100)}%` }}
                    ></div>
                  </div>
                  <div className="flex items-center justify-between text-[10px] text-slate-400 mt-2">
                    <span>Occupancy: {f.occupancy}</span>
                    <span>Capacity: {f.capacity}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* TAB 4: EDGE AI DIAGNOSTICS */}
      {activeTab === 'ai' && aiData && (
        <div className="space-y-6">
          {/* AI Metrics Row */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Total Anomalies Detected</span>
              <div className="text-2xl font-bold font-mono text-amber-400 mt-1">{aiData.totalAnomalies}</div>
              <div className="text-[10px] text-slate-400 mt-1">Multi-sensor pattern divergence</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Average Anomaly Score</span>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">{aiData.avgAnomalyScore}</div>
              <div className="text-[10px] text-slate-400 mt-1">Isolation Forest metric</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Critical Risk Rooms</span>
              <div className="text-2xl font-bold font-mono text-red-400 mt-1">{aiData.criticalRiskRooms}</div>
              <div className="text-[10px] text-slate-400 mt-1">Immediate intervention required</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">High Risk Zones</span>
              <div className="text-2xl font-bold font-mono text-orange-400 mt-1">{aiData.highRiskRooms}</div>
              <div className="text-[10px] text-slate-400 mt-1">Pre-incident warnings flagged</div>
            </div>
          </div>

          {/* Model Fleet & Retrain Workbench */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-6">
              <div>
                <h3 className="text-sm font-bold text-slate-200">Active Edge AI Model Fleet</h3>
                <p className="text-xs text-slate-400">Deployed machine learning inferencing pipeline</p>
              </div>

              {canManage && (
                <div className="flex flex-wrap items-center gap-3">
                  <div className="flex items-center gap-2 bg-[#0c121e] px-3 py-1.5 rounded-lg border border-[#1c283c]">
                    <span className="text-xs text-slate-400">Samples:</span>
                    <input
                      type="number"
                      value={trainSamples}
                      onChange={(e) => setTrainSamples(parseInt(e.target.value) || 1000)}
                      className="w-16 bg-transparent text-xs font-mono text-slate-200 focus:outline-none"
                    />
                  </div>
                  <div className="flex items-center gap-2 bg-[#0c121e] px-3 py-1.5 rounded-lg border border-[#1c283c]">
                    <span className="text-xs text-slate-400">Contamination:</span>
                    <input
                      type="number"
                      step="0.01"
                      value={contamination}
                      onChange={(e) => setContamination(parseFloat(e.target.value) || 0.05)}
                      className="w-14 bg-transparent text-xs font-mono text-slate-200 focus:outline-none"
                    />
                  </div>
                  <button
                    onClick={handleRetrainAi}
                    disabled={training}
                    className="flex items-center gap-1.5 px-3.5 py-1.5 bg-gradient-to-r from-emerald-600 to-cyan-600 text-slate-900 font-bold text-xs rounded-lg hover:opacity-95 transition-all disabled:opacity-50"
                  >
                    <RefreshCw className={`w-3.5 h-3.5 ${training ? 'animate-spin' : ''}`} />
                    <span>{training ? 'Retraining...' : 'Retrain Fleet'}</span>
                  </button>
                </div>
              )}
            </div>

            {trainMessage && (
              <div className="mb-4 p-3 bg-emerald-950/40 border border-emerald-500/40 rounded-xl text-xs text-emerald-300">
                {trainMessage}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {aiData.modelMetadata.map((m, idx) => (
                <div key={idx} className="p-4 bg-[#0c121e] border border-[#1c283c] rounded-xl">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-xs font-bold text-slate-200">{m.modelType}</span>
                    <span className="px-2 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800 text-[10px] font-mono">
                      {m.status}
                    </span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-2">Version: {m.version}</div>
                  <div className="text-xs font-mono text-cyan-400 font-semibold mt-1">
                    Score: {m.accuracy ? `${(m.accuracy * 100).toFixed(1)}%` : 'Active'}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Top Anomaly Rooms Table */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <h3 className="text-sm font-bold text-slate-200 mb-4">Top Anomaly Hotspot Rooms</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-xs text-left">
                <thead className="bg-[#0e1624] text-slate-400 uppercase text-[10px] font-semibold border-b border-[#1a2538]">
                  <tr>
                    <th className="py-2.5 px-4">Room Code</th>
                    <th className="py-2.5 px-4">Room Name</th>
                    <th className="py-2.5 px-3">Anomalies Detected</th>
                    <th className="py-2.5 px-3">Highest Severity</th>
                    <th className="py-2.5 px-3">Last Detected</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#152033]">
                  {aiData.topAnomalyRooms.map((r) => (
                    <tr key={r.roomId} className="hover:bg-[#0c121e]">
                      <td className="py-2.5 px-4 font-mono font-semibold text-slate-200">{r.roomNumber}</td>
                      <td className="py-2.5 px-4 text-slate-300">{r.roomName}</td>
                      <td className="py-2.5 px-3 font-mono text-amber-400 font-bold">{r.anomalyCount}</td>
                      <td className="py-2.5 px-3">
                        <span className="px-2 py-0.5 rounded bg-red-950 text-red-400 border border-red-800 text-[10px] font-mono">
                          {r.highestSeverity}
                        </span>
                      </td>
                      <td className="py-2.5 px-3 text-slate-400 font-mono text-[11px]">
                        {r.lastAnomalyAt ? new Date(r.lastAnomalyAt).toLocaleString() : 'N/A'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 5: INCIDENT RESPONSE & SLA */}
      {activeTab === 'incidents' && incidentData && (
        <div className="space-y-6">
          {/* Incident SLA KPI Cards */}
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Total Incidents</span>
              <div className="text-2xl font-bold font-mono text-slate-100 mt-1">{incidentData.totalIncidents}</div>
              <div className="text-[10px] text-slate-400 mt-1">Logged across facility</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Active Incidents</span>
              <div className="text-2xl font-bold font-mono text-red-400 mt-1">{incidentData.activeIncidents}</div>
              <div className="text-[10px] text-slate-400 mt-1">Under current response</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Resolved & Closed</span>
              <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">{incidentData.resolvedIncidents}</div>
              <div className="text-[10px] text-slate-400 mt-1">Containment confirmed</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">MTTA (Mean Ack Time)</span>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">
                {incidentData.meanTimeToAcknowledgeMinutes}m
              </div>
              <div className="text-[10px] text-emerald-400 mt-1">Target SLA: &lt; 5 mins</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">MTTR (Mean Resolve Time)</span>
              <div className="text-2xl font-bold font-mono text-purple-400 mt-1">
                {incidentData.meanTimeToResolveMinutes}m
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Target SLA: &lt; 30 mins</div>
            </div>
          </div>

          {/* Categorical Distribution Chart */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
              <h3 className="text-sm font-bold text-slate-200 mb-4">Incident Severity Breakdown</h3>
              <div className="space-y-3">
                {Object.entries(incidentData.severityDistribution).map(([sev, count]) => (
                  <div key={sev} className="flex items-center justify-between">
                    <span className="text-xs font-mono text-slate-300">{sev}</span>
                    <div className="flex items-center gap-3">
                      <div className="w-36 bg-[#162234] h-2 rounded-full overflow-hidden">
                        <div
                          className={`h-full ${
                            sev === 'CRITICAL' ? 'bg-red-500' : sev === 'HIGH' ? 'bg-orange-500' : sev === 'MEDIUM' ? 'bg-amber-500' : 'bg-emerald-500'
                          }`}
                          style={{
                            width: `${incidentData.totalIncidents > 0 ? (count / incidentData.totalIncidents) * 100 : 0}%`,
                          }}
                        ></div>
                      </div>
                      <span className="text-xs font-mono font-bold text-slate-200 w-8 text-right">{count}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
              <h3 className="text-sm font-bold text-slate-200 mb-4">Incident Hotspot Rooms</h3>
              <div className="space-y-2">
                {incidentData.hotspotRooms.length === 0 ? (
                  <div className="text-xs text-slate-400 py-6 text-center">No recurring hotspot zones.</div>
                ) : (
                  incidentData.hotspotRooms.slice(0, 5).map((h) => (
                    <div key={h.roomId} className="p-3 bg-[#0c121e] border border-[#1c283c] rounded-xl flex items-center justify-between">
                      <div>
                        <div className="text-xs font-bold text-slate-200">{h.roomName} ({h.roomNumber})</div>
                        <div className="text-[10px] text-slate-400">Most Frequent: {h.mostFrequentCategory}</div>
                      </div>
                      <span className="px-2.5 py-1 bg-red-950/60 border border-red-800 text-red-400 text-xs font-mono font-bold rounded-lg">
                        {h.incidentCount} incidents
                      </span>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* TAB 6: EVACUATION DRILLS */}
      {activeTab === 'evacuation' && evacuationData && (
        <div className="space-y-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Total Drills</span>
              <div className="text-2xl font-bold font-mono text-purple-400 mt-1">{evacuationData.totalSessions}</div>
              <div className="text-[10px] text-slate-400 mt-1">{evacuationData.completedSessions} completed</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Average Clearance</span>
              <div className="text-2xl font-bold font-mono text-emerald-400 mt-1">
                {evacuationData.averageDurationSeconds}s
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Benchmark: &lt; 180s</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Persons Evacuated</span>
              <div className="text-2xl font-bold font-mono text-cyan-400 mt-1">{evacuationData.totalPersonsEvacuated}</div>
              <div className="text-[10px] text-slate-400 mt-1">Safely routed to muster point</div>
            </div>
            <div className="p-4 bg-[#0a0f1d] border border-[#1a263d] rounded-xl">
              <span className="text-[11px] text-slate-400 font-medium">Dynamic Re-routes</span>
              <div className="text-2xl font-bold font-mono text-amber-400 mt-1">
                {evacuationData.blockedConnectionsEncountered}
              </div>
              <div className="text-[10px] text-slate-400 mt-1">Hazard corridor bypasses</div>
            </div>
          </div>

          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl">
            <h3 className="text-sm font-bold text-slate-200 mb-1">Clearance Progression Timeline Curve</h3>
            <p className="text-xs text-slate-400 mb-4">Minutes elapsed vs cumulative rooms cleared</p>
            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={evacuationData.clearanceTimeline}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#162234" />
                  <XAxis dataKey="elapsedMinutes" stroke="#64748b" tickFormatter={(m) => `${m}m`} />
                  <YAxis stroke="#64748b" />
                  <Tooltip contentStyle={{ backgroundColor: '#0e1624', borderColor: '#1e2e47', borderRadius: '8px', fontSize: '11px' }} />
                  <Area type="monotone" dataKey="clearedRooms" stroke="#10b981" fill="#10b981" fillOpacity={0.2} name="Cleared Rooms" />
                  <Line type="monotone" dataKey="remainingRooms" stroke="#ef4444" strokeWidth={2} name="Remaining Rooms" />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>
      )}

      {/* TAB 7: INCIDENT POST-MORTEM STUDIO */}
      {activeTab === 'postmortems' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Post-Mortem Report Selector List */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-sm font-bold text-slate-200">Post-Mortem Reports</h3>
                <p className="text-xs text-slate-400">Formal root cause & corrective audit reports</p>
              </div>
              <div className="flex items-center gap-1 bg-[#0c121e] p-1 rounded-lg border border-[#1c283c]">
                {(['ALL', 'DRAFT', 'FINALIZED'] as const).map((st) => (
                  <button
                    key={st}
                    onClick={() => setPostMortemStatusFilter(st)}
                    className={`px-2 py-0.5 text-[10px] rounded font-medium ${
                      postMortemStatusFilter === st
                        ? 'bg-emerald-500/20 text-emerald-300 font-bold'
                        : 'text-slate-400 hover:text-slate-200'
                    }`}
                  >
                    {st}
                  </button>
                ))}
              </div>
            </div>

            <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
              {postMortems.length === 0 ? (
                <div className="text-center py-10 text-xs text-slate-400 border border-dashed border-[#1a2538] rounded-xl">
                  No post-mortem reports found.
                </div>
              ) : (
                postMortems.map((pm) => {
                  const isSelected = selectedPostMortem?.id === pm.id;
                  return (
                    <div
                      key={pm.id}
                      onClick={() => selectPostMortem(pm)}
                      className={`p-3.5 rounded-xl border cursor-pointer transition-all ${
                        isSelected
                          ? 'bg-[#121c2e] border-emerald-500/40 shadow-lg shadow-emerald-950/20'
                          : 'bg-[#0a0f1d] border-[#1a263d] hover:bg-[#0e1624]'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-mono text-[11px] font-bold text-cyan-400">
                          {pm.incidentReferenceNumber || 'INCIDENT'}
                        </span>
                        <span
                          className={`text-[9px] font-mono px-1.5 py-0.5 rounded ${
                            pm.reportStatus === 'FINALIZED'
                              ? 'bg-emerald-950 text-emerald-400 border border-emerald-800'
                              : pm.reportStatus === 'REVIEWED'
                              ? 'bg-cyan-950 text-cyan-400 border border-cyan-800'
                              : 'bg-amber-950 text-amber-400 border border-amber-800'
                          }`}
                        >
                          {pm.reportStatus}
                        </span>
                      </div>
                      <h4 className="text-xs font-semibold text-slate-200 line-clamp-1">{pm.reportTitle}</h4>
                      <p className="text-[10px] text-slate-400 line-clamp-2 mt-1">{pm.executiveSummary}</p>
                      <div className="flex items-center justify-between text-[10px] text-slate-400 mt-2 pt-2 border-t border-[#162234]">
                        <span>Author: {pm.authorName || 'Safety Lead'}</span>
                        <span className="font-mono">{new Date(pm.updatedAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>

          {/* Interactive Post-Mortem Editor & Signoff */}
          <div className="lg:col-span-2 p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-5">
            {!selectedPostMortem ? (
              <div className="text-center py-20 text-xs text-slate-400">
                Select an incident post-mortem report from the list to view or edit.
              </div>
            ) : (
              <>
                {/* Header with status badge & action buttons */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-[#1a2538]">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-mono font-bold text-emerald-400">
                        {selectedPostMortem.incidentReferenceNumber}
                      </span>
                      <span className="text-slate-400">•</span>
                      <span className="text-xs text-slate-300">
                        {selectedPostMortem.buildingName} - Floor {selectedPostMortem.floorName} ({selectedPostMortem.roomNumber})
                      </span>
                    </div>
                    <input
                      type="text"
                      value={editingPostMortem.reportTitle || ''}
                      onChange={(e) => setEditingPostMortem({ ...editingPostMortem, reportTitle: e.target.value })}
                      className="text-base font-bold text-slate-100 bg-transparent border-b border-transparent hover:border-[#2a3a54] focus:border-emerald-500 focus:outline-none w-full mt-1"
                    />
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleExportPostMortem(selectedPostMortem.id, 'PDF')}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-[#121c2e] hover:bg-[#1a273f] text-emerald-300 border border-emerald-500/30 rounded-lg text-xs font-medium transition-all"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>PDF</span>
                    </button>
                    <button
                      onClick={() => handleExportPostMortem(selectedPostMortem.id, 'CSV')}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-[#121c2e] hover:bg-[#1a273f] text-cyan-300 border border-cyan-500/30 rounded-lg text-xs font-medium transition-all"
                    >
                      <Download className="w-3.5 h-3.5" />
                      <span>CSV</span>
                    </button>
                    {canManage && (
                      <>
                        <button
                          onClick={() => handleSavePostMortem('DRAFT')}
                          disabled={savingPostMortem}
                          className="px-3 py-1.5 bg-[#182337] hover:bg-[#22324e] text-slate-200 text-xs font-semibold rounded-lg transition-all"
                        >
                          Save Draft
                        </button>
                        {selectedPostMortem.reportStatus !== 'FINALIZED' && (
                          <button
                            onClick={() => handleSavePostMortem('FINALIZED')}
                            disabled={savingPostMortem}
                            className="px-3 py-1.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 text-slate-900 text-xs font-bold rounded-lg transition-all"
                          >
                            Finalize Report
                          </button>
                        )}
                      </>
                    )}
                  </div>
                </div>

                {/* Section Narrative Fields */}
                <div className="space-y-4 max-h-[600px] overflow-y-auto pr-2">
                  <div>
                    <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                      1. Executive Summary
                    </label>
                    <textarea
                      rows={3}
                      value={editingPostMortem.executiveSummary || ''}
                      onChange={(e) => setEditingPostMortem({ ...editingPostMortem, executiveSummary: e.target.value })}
                      className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                      2. Chronological Timeline Narrative
                    </label>
                    <textarea
                      rows={3}
                      value={editingPostMortem.timelineNarrative || ''}
                      onChange={(e) => setEditingPostMortem({ ...editingPostMortem, timelineNarrative: e.target.value })}
                      className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500 font-mono"
                    />
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                        3. IoT Sensor Telemetry Analysis
                      </label>
                      <textarea
                        rows={3}
                        value={editingPostMortem.sensorAnalysis || ''}
                        onChange={(e) => setEditingPostMortem({ ...editingPostMortem, sensorAnalysis: e.target.value })}
                        className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                        4. Edge-AI Threat Assessment
                      </label>
                      <textarea
                        rows={3}
                        value={editingPostMortem.aiAssessmentSummary || ''}
                        onChange={(e) => setEditingPostMortem({ ...editingPostMortem, aiAssessmentSummary: e.target.value })}
                        className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                      5. Root Cause Assessment
                    </label>
                    <textarea
                      rows={3}
                      value={editingPostMortem.rootCauseAssessment || ''}
                      onChange={(e) => setEditingPostMortem({ ...editingPostMortem, rootCauseAssessment: e.target.value })}
                      className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                    />
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                        6. Corrective Actions Required
                      </label>
                      <textarea
                        rows={3}
                        value={editingPostMortem.correctiveActions || ''}
                        onChange={(e) => setEditingPostMortem({ ...editingPostMortem, correctiveActions: e.target.value })}
                        className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                    <div>
                      <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
                        7. Preventive Recommendations & SLA Upgrades
                      </label>
                      <textarea
                        rows={3}
                        value={editingPostMortem.preventiveRecommendations || ''}
                        onChange={(e) => setEditingPostMortem({ ...editingPostMortem, preventiveRecommendations: e.target.value })}
                        className="w-full bg-[#0c121e] border border-[#1e2e47] rounded-xl p-3 text-xs text-slate-200 focus:outline-none focus:border-emerald-500"
                      />
                    </div>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* TAB 8: HISTORICAL DATA EXPLORER */}
      {activeTab === 'history' && historyData && (
        <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div>
              <h3 className="text-sm font-bold text-slate-200">Historical Raw Data Explorer</h3>
              <p className="text-xs text-slate-400">
                Paginated query access across {historyData.totalElements.toLocaleString()} archived telemetry and event records
              </p>
            </div>

            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 bg-[#0c121e] px-3 py-1.5 rounded-lg border border-[#1c283c]">
                <span className="text-xs text-slate-400">Record Type:</span>
                <select
                  value={historyRecordType}
                  onChange={(e) => {
                    setHistoryRecordType(e.target.value);
                    setHistoryPage(0);
                  }}
                  className="bg-transparent text-xs text-slate-200 focus:outline-none"
                >
                  <option value="ALL" className="bg-[#0c121e]">All Types</option>
                  <option value="SENSOR_READING" className="bg-[#0c121e]">Sensor Readings</option>
                  <option value="INCIDENT" className="bg-[#0c121e]">Incidents</option>
                  <option value="ANOMALY" className="bg-[#0c121e]">Edge AI Anomalies</option>
                </select>
              </div>

              <button
                onClick={() => handleExportStandardReport('EXECUTIVE_SUMMARY', 'CSV')}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-[#121c2e] hover:bg-[#1a273f] text-cyan-300 border border-cyan-500/30 rounded-lg text-xs font-medium"
              >
                <Download className="w-3.5 h-3.5" />
                <span>Export CSV</span>
              </button>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-[#0e1624] text-slate-400 uppercase text-[10px] font-semibold border-b border-[#1a2538]">
                <tr>
                  <th className="py-2.5 px-4">Timestamp</th>
                  <th className="py-2.5 px-3">Record Type</th>
                  <th className="py-2.5 px-3">Location</th>
                  <th className="py-2.5 px-3">Metric</th>
                  <th className="py-2.5 px-3">Value</th>
                  <th className="py-2.5 px-3">Severity / Status</th>
                  <th className="py-2.5 px-4">Details</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#152033]">
                {historyData.records.map((rec) => (
                  <tr key={rec.id} className="hover:bg-[#0c121e]">
                    <td className="py-2.5 px-4 font-mono text-slate-400 text-[11px] whitespace-nowrap">
                      {new Date(rec.timestamp).toLocaleString()}
                    </td>
                    <td className="py-2.5 px-3">
                      <span
                        className={`px-2 py-0.5 rounded text-[10px] font-mono ${
                          rec.recordType === 'INCIDENT'
                            ? 'bg-red-950 text-red-400 border border-red-800'
                            : rec.recordType === 'ANOMALY'
                            ? 'bg-amber-950 text-amber-400 border border-amber-800'
                            : 'bg-slate-900 text-slate-300 border border-slate-700'
                        }`}
                      >
                        {rec.recordType}
                      </span>
                    </td>
                    <td className="py-2.5 px-3 text-slate-300">
                      {rec.buildingName} - {rec.roomNumber}
                    </td>
                    <td className="py-2.5 px-3 font-semibold text-slate-200">{rec.metricName}</td>
                    <td className="py-2.5 px-3 font-mono text-emerald-400 font-bold">{rec.metricValue}</td>
                    <td className="py-2.5 px-3">
                      <span className="font-mono text-[10px] text-slate-300">{rec.severityOrStatus}</span>
                    </td>
                    <td className="py-2.5 px-4 text-slate-400 text-[11px] truncate max-w-xs">{rec.details || '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Pagination Controls */}
          <div className="flex items-center justify-between pt-4 border-t border-[#1a2538] text-xs">
            <span className="text-slate-400">
              Showing page {historyData.page + 1} of {Math.max(1, historyData.totalPages)} ({historyData.totalElements.toLocaleString()} records)
            </span>
            <div className="flex items-center gap-2">
              <button
                disabled={historyData.page === 0}
                onClick={() => setHistoryPage((p) => Math.max(0, p - 1))}
                className="flex items-center gap-1 px-3 py-1.5 bg-[#0e1624] text-slate-300 border border-[#1e2e47] rounded-lg disabled:opacity-40 hover:bg-[#152033]"
              >
                <ChevronLeft className="w-3.5 h-3.5" /> Previous
              </button>
              <button
                disabled={historyData.page + 1 >= historyData.totalPages}
                onClick={() => setHistoryPage((p) => p + 1)}
                className="flex items-center gap-1 px-3 py-1.5 bg-[#0e1624] text-slate-300 border border-[#1e2e47] rounded-lg disabled:opacity-40 hover:bg-[#152033]"
              >
                Next <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
