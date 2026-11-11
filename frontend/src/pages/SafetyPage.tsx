import React, { useState, useEffect, useCallback } from 'react';
import { buildingApi, floorApi, aiApi } from '../services/api';
import { Building, Floor } from '../types/building';
import { AnomalyDetection, RiskAssessment, RiskLevel } from '../types/ai';
import {
  ShieldCheck,
  AlertTriangle,
  AlertOctagon,
  Flame,
  Thermometer,
  Wind,
  Users,
  Activity,
  RefreshCw,
  Sparkles,
  Building2,
  Layers,
  Filter,
  CheckCircle2,
} from 'lucide-react';

export const SafetyPage: React.FC = () => {
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [floors, setFloors] = useState<Floor[]>([]);
  const [selectedFloorId, setSelectedFloorId] = useState<string>('ALL');
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [anomalies, setAnomalies] = useState<AnomalyDetection[]>([]);
  const [risks, setRisks] = useState<RiskAssessment[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [analyzing, setAnalyzing] = useState<boolean>(false);

  // 1. Initial Load of Buildings
  useEffect(() => {
    const fetchBuildings = async () => {
      try {
        const bList = await buildingApi.getBuildings();
        setBuildings(bList);
        if (bList.length > 0) {
          setSelectedBuildingId(bList[0].id);
        }
      } catch (err) {
        console.error('Failed to load buildings', err);
      }
    };
    fetchBuildings();
  }, []);

  // 2. Load Safety and Anomaly Telemetry
  const loadSafetyData = useCallback(async (bId: string) => {
    if (!bId) return;
    setLoading(true);
    try {
      const [fList, anomData, riskData] = await Promise.all([
        floorApi.getFloors(bId),
        aiApi.getAnomalies({ buildingId: bId, size: 50 }).catch(() => ({ content: [] })),
        aiApi.getRisks({ buildingId: bId, size: 50 }).catch(() => ({ content: [] })),
      ]);
      setFloors(fList);
      setAnomalies(anomData.content || []);
      setRisks(riskData.content || []);
    } catch (err) {
      console.error('Failed to load safety data', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (selectedBuildingId) {
      loadSafetyData(selectedBuildingId);
    }
  }, [selectedBuildingId, loadSafetyData]);

  const handleRunDiagnostics = async () => {
    if (!selectedBuildingId) return;
    setAnalyzing(true);
    try {
      await aiApi.analyzeBuilding(selectedBuildingId);
      await loadSafetyData(selectedBuildingId);
    } catch (err) {
      console.error('Failed to run safety analysis', err);
    } finally {
      setAnalyzing(false);
    }
  };

  // Filtered Anomalies
  const filteredAnomalies = anomalies.filter((a) => {
    const matchesSev = severityFilter === 'ALL' || a.severity === severityFilter;
    const matchesFloor = selectedFloorId === 'ALL' || a.room?.floor?.id === selectedFloorId;
    return matchesSev && matchesFloor;
  });

  // Calculate Risk Metrics
  const criticalCount = risks.filter((r) => r.riskLevel === 'CRITICAL').length;
  const highRiskCount = risks.filter((r) => r.riskLevel === 'HIGH_RISK').length;
  const warningCount = risks.filter((r) => r.riskLevel === 'WARNING').length;
  const normalCount = risks.filter((r) => r.riskLevel === 'NORMAL').length;
  const avgRiskScore =
    risks.length > 0
      ? Math.round((risks.reduce((acc, r) => acc + r.riskScore, 0) / risks.length) * 10) / 10
      : 12.0;

  const getRiskBadge = (level: RiskLevel) => {
    switch (level) {
      case 'CRITICAL':
        return (
          <span className="flex items-center gap-1 text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-red-500/20 text-red-400 border border-red-500/40 font-bold">
            <AlertOctagon className="w-3.5 h-3.5" /> CRITICAL
          </span>
        );
      case 'HIGH_RISK':
        return (
          <span className="flex items-center gap-1 text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-orange-500/20 text-orange-400 border border-orange-500/40 font-bold">
            <AlertTriangle className="w-3.5 h-3.5" /> HIGH RISK
          </span>
        );
      case 'WARNING':
        return (
          <span className="flex items-center gap-1 text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-amber-500/20 text-amber-400 border border-amber-500/40">
            <AlertTriangle className="w-3.5 h-3.5" /> WARNING
          </span>
        );
      case 'NORMAL':
        return (
          <span className="flex items-center gap-1 text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">
            <CheckCircle2 className="w-3.5 h-3.5" /> NORMAL
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl shadow-xl flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono text-emerald-400 uppercase tracking-widest mb-1">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>AI Safety Assessment & Anomaly Diagnostics</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            Building Safety & Environmental Risk Hub
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Isolation Forest multi-modal anomaly classification and explainable safety risk scoring with critical smoke override rules.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => selectedBuildingId && loadSafetyData(selectedBuildingId)}
            disabled={loading || analyzing}
            className="p-2.5 bg-[#0e1624] hover:bg-[#162234] border border-[#1e2e47] text-slate-300 rounded-xl transition-colors"
            title="Refresh Telemetry"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>

          <button
            onClick={handleRunDiagnostics}
            disabled={analyzing || loading}
            className="flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white text-xs font-bold rounded-xl shadow-lg shadow-emerald-950/40 transition-all disabled:opacity-50"
          >
            <Sparkles className={`w-4 h-4 ${analyzing ? 'animate-spin' : ''}`} />
            <span>{analyzing ? 'Evaluating Safety Models...' : 'Run Safety Diagnostics'}</span>
          </button>
        </div>
      </div>

      {/* Risk Metrics Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl">
          <div className="text-xs text-slate-400 font-medium">Average Risk Index</div>
          <div className="text-2xl font-bold font-mono mt-1 text-slate-100 flex items-baseline gap-2">
            <span>{avgRiskScore}</span>
            <span className="text-xs text-slate-400 font-normal">/ 100</span>
          </div>
          <div className="text-[11px] text-slate-400 mt-1">Multi-criteria weighted formula</div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl">
          <div className="text-xs text-slate-400 font-medium">Critical Rooms</div>
          <div className="text-2xl font-bold font-mono mt-1 text-red-400">{criticalCount}</div>
          <div className="text-[11px] text-red-400/80 mt-1">Immediate action required</div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl">
          <div className="text-xs text-slate-400 font-medium">High Risk Rooms</div>
          <div className="text-2xl font-bold font-mono mt-1 text-orange-400">{highRiskCount}</div>
          <div className="text-[11px] text-orange-400/80 mt-1">Hazard threshold breached</div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl">
          <div className="text-xs text-slate-400 font-medium">Warning Rooms</div>
          <div className="text-2xl font-bold font-mono mt-1 text-amber-400">{warningCount}</div>
          <div className="text-[11px] text-amber-400/80 mt-1">Elevated thermal or AQI</div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl">
          <div className="text-xs text-slate-400 font-medium">Normal / Safe Rooms</div>
          <div className="text-2xl font-bold font-mono mt-1 text-emerald-400">{normalCount}</div>
          <div className="text-[11px] text-emerald-400/80 mt-1">Optimal baseline climate</div>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-xl flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          {/* Building Selector */}
          <div className="flex items-center gap-2 bg-[#0e1624] border border-[#1e2e47] px-3 py-1.5 rounded-lg text-xs">
            <Building2 className="w-4 h-4 text-slate-400" />
            <select
              value={selectedBuildingId}
              onChange={(e) => setSelectedBuildingId(e.target.value)}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
            >
              {buildings.map((b) => (
                <option key={b.id} value={b.id} className="bg-[#0b121e]">
                  {b.name} ({b.buildingCode})
                </option>
              ))}
            </select>
          </div>

          {/* Floor Selector */}
          <div className="flex items-center gap-2 bg-[#0e1624] border border-[#1e2e47] px-3 py-1.5 rounded-lg text-xs">
            <Layers className="w-4 h-4 text-slate-400" />
            <select
              value={selectedFloorId}
              onChange={(e) => setSelectedFloorId(e.target.value)}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
            >
              <option value="ALL" className="bg-[#0b121e]">All Floors</option>
              {floors.map((fl) => (
                <option key={fl.id} value={fl.id} className="bg-[#0b121e]">
                  Floor {fl.floorNumber}: {fl.floorName}
                </option>
              ))}
            </select>
          </div>

          {/* Severity Filter */}
          <div className="flex items-center gap-2 bg-[#0e1624] border border-[#1e2e47] px-3 py-1.5 rounded-lg text-xs">
            <Filter className="w-4 h-4 text-slate-400" />
            <select
              value={severityFilter}
              onChange={(e) => setSeverityFilter(e.target.value)}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
            >
              <option value="ALL" className="bg-[#0b121e]">All Severities</option>
              <option value="CRITICAL" className="bg-[#0b121e]">CRITICAL</option>
              <option value="HIGH" className="bg-[#0b121e]">HIGH</option>
              <option value="MEDIUM" className="bg-[#0b121e]">MEDIUM</option>
              <option value="LOW" className="bg-[#0b121e]">LOW</option>
              <option value="NORMAL" className="bg-[#0b121e]">NORMAL</option>
            </select>
          </div>
        </div>

        <div className="text-xs text-slate-400 font-mono">
          Showing {filteredAnomalies.length} anomaly detections
        </div>
      </div>

      {/* Anomaly Detection Center Table */}
      <div className="bg-[#080d16] border border-[#1a2538] rounded-2xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-[#1a2538] flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Activity className="w-4 h-4 text-emerald-400" />
            <h2 className="text-sm font-bold text-slate-200">Isolation Forest Anomaly Stream</h2>
          </div>
          <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-purple-500/10 text-purple-400 border border-purple-500/30">
            ML Model: ISO-FOREST-v1.0.0
          </span>
        </div>

        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs flex flex-col items-center justify-center gap-3">
            <RefreshCw className="w-6 h-6 animate-spin text-emerald-400" />
            <span>Evaluating anomaly telemetry...</span>
          </div>
        ) : filteredAnomalies.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            No anomaly events found for the active filter selection.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="bg-[#0c1320] text-slate-400 font-mono text-[11px] uppercase tracking-wider border-b border-[#1a2538]">
                <tr>
                  <th className="py-3 px-4">Severity</th>
                  <th className="py-3 px-4">Room / Location</th>
                  <th className="py-3 px-4">Anomaly Score</th>
                  <th className="py-3 px-4">Explainable AI Diagnostic</th>
                  <th className="py-3 px-4">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#141e30]">
                {filteredAnomalies.map((anom) => (
                  <tr key={anom.id} className="hover:bg-[#0c1422] transition-colors">
                    <td className="py-3 px-4">
                      {getRiskBadge(anom.severity as RiskLevel)}
                    </td>
                    <td className="py-3 px-4">
                      <div className="font-bold text-slate-200">{anom.room?.roomName || 'Room'}</div>
                      <div className="text-[11px] text-slate-400 font-mono">
                        {anom.room?.roomCode || 'Code'} • Floor {anom.room?.floor?.floorNumber ?? 1}
                      </div>
                    </td>
                    <td className="py-3 px-4 font-mono font-bold">
                      <span
                        className={
                          anom.anomalyScore >= 0.7
                            ? 'text-red-400'
                            : anom.anomalyScore >= 0.5
                            ? 'text-amber-400'
                            : 'text-emerald-400'
                        }
                      >
                        {Math.round(anom.anomalyScore * 100)}%
                      </span>
                    </td>
                    <td className="py-3 px-4 max-w-md text-slate-300">
                      {anom.explanation}
                    </td>
                    <td className="py-3 px-4 font-mono text-[11px] text-slate-400 whitespace-nowrap">
                      {new Date(anom.detectedAt).toLocaleTimeString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Room Risk Assessment Cards */}
      <div>
        <h2 className="text-base font-bold text-slate-100 mb-4 flex items-center gap-2">
          <ShieldCheck className="w-5 h-5 text-emerald-400" />
          <span>Room-Level Explainable Risk Scores (R = 0.35T + 0.35S + 0.20A + 0.10O)</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {risks.slice(0, 9).map((r) => (
            <div
              key={r.id}
              className="p-5 bg-[#080d16] border border-[#1a2538] hover:border-emerald-500/40 rounded-2xl shadow-lg transition-all"
            >
              <div className="flex items-start justify-between mb-3">
                <div>
                  <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#101928] text-emerald-400 border border-[#1c2c44]">
                    {r.room?.roomCode || 'ROOM'}
                  </span>
                  <h3 className="text-sm font-bold text-slate-100 mt-1">{r.room?.roomName || 'Room'}</h3>
                </div>
                {getRiskBadge(r.riskLevel)}
              </div>

              {/* Score Indicator */}
              <div className="flex items-baseline gap-2 my-2">
                <span className="text-3xl font-extrabold font-mono text-slate-100">{r.riskScore}</span>
                <span className="text-xs text-slate-400">/ 100 Risk Index</span>
              </div>

              {/* Component Scores Bar Breakdown */}
              <div className="space-y-1.5 my-3 p-3 bg-[#0b121e] border border-[#162234] rounded-xl text-xs">
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-slate-400 flex items-center gap-1">
                    <Thermometer className="w-3 h-3 text-red-400" /> Temp Risk (0.35)
                  </span>
                  <span className="font-mono font-bold text-slate-200">{r.temperatureScore}</span>
                </div>
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-slate-400 flex items-center gap-1">
                    <Flame className="w-3 h-3 text-orange-400" /> Smoke Risk (0.35)
                  </span>
                  <span className="font-mono font-bold text-slate-200">{r.smokeScore}</span>
                </div>
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-slate-400 flex items-center gap-1">
                    <Wind className="w-3 h-3 text-teal-400" /> Air Quality Risk (0.20)
                  </span>
                  <span className="font-mono font-bold text-slate-200">{r.airQualityScore}</span>
                </div>
                <div className="flex justify-between items-center text-[11px]">
                  <span className="text-slate-400 flex items-center gap-1">
                    <Users className="w-3 h-3 text-blue-400" /> Occupancy Risk (0.10)
                  </span>
                  <span className="font-mono font-bold text-slate-200">{r.occupancyScore}</span>
                </div>
              </div>

              {/* Explanation Note */}
              <p className="text-[11px] text-slate-400 line-clamp-2 mt-2">
                {r.explanation}
              </p>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
