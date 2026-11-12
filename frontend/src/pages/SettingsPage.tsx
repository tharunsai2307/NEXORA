import React, { useState, useEffect } from 'react';
import { systemService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  Settings,
  Server,
  Sliders,
  Database,
  Trash2,
  RefreshCw,
  CheckCircle2,
  AlertTriangle,
  Flame,
  Thermometer,
  Wind,
  Users,
  Activity,
  Boxes,
  RotateCcw,
} from 'lucide-react';

export const SettingsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('SYSTEM_ADMIN');
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);

  const [activeTab, setActiveTab] = useState<'gateways' | 'thresholds' | 'data' | 'display'>('data');
  const [loading, setLoading] = useState<boolean>(false);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  // System Health States
  const [backendHealth, setBackendHealth] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');
  const [aiHealth, setAiHealth] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');
  const [dbHealth, setDbHealth] = useState<'UP' | 'DOWN' | 'CHECKING'>('CHECKING');
  const [systemInfo, setSystemInfo] = useState<any>(null);

  // Threshold Configuration State (Loaded from localStorage or nominal defaults)
  const [tempThreshold, setTempThreshold] = useState<number>(() => {
    return parseFloat(localStorage.getItem('nexora_thresh_temp') || '45.0');
  });
  const [smokeThreshold, setSmokeThreshold] = useState<number>(() => {
    return parseFloat(localStorage.getItem('nexora_thresh_smoke') || '0.8');
  });
  const [aqiThreshold, setAqiThreshold] = useState<number>(() => {
    return parseInt(localStorage.getItem('nexora_thresh_aqi') || '150');
  });
  const [occupancyThreshold, setOccupancyThreshold] = useState<number>(() => {
    return parseInt(localStorage.getItem('nexora_thresh_occ') || '85');
  });
  const [pollInterval, setPollInterval] = useState<number>(() => {
    return parseInt(localStorage.getItem('nexora_poll_interval') || '5');
  });

  // Display Preferences
  const [showWireframe, setShowWireframe] = useState<boolean>(() => {
    return localStorage.getItem('nexora_wireframe') === 'true';
  });
  const [explodedGap, setExplodedGap] = useState<number>(() => {
    return parseFloat(localStorage.getItem('nexora_exploded_gap') || '3.0');
  });

  // Auto-dismiss notifications
  useEffect(() => {
    if (actionSuccess || actionError) {
      const timer = setTimeout(() => {
        setActionSuccess(null);
        setActionError(null);
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [actionSuccess, actionError]);

  // Check service health
  const checkHealth = async () => {
    setBackendHealth('CHECKING');
    setAiHealth('CHECKING');
    setDbHealth('CHECKING');

    try {
      const bRes = await systemService.getBackendHealth();
      setBackendHealth(bRes.status === 'UP' ? 'UP' : 'DOWN');
    } catch {
      setBackendHealth('DOWN');
    }

    try {
      const aRes = await systemService.getAiHealth();
      setAiHealth(aRes.status === 'UP' ? 'UP' : 'DOWN');
    } catch {
      setAiHealth('DOWN');
    }

    try {
      const dRes = await systemService.getDatabaseHealth();
      setDbHealth(dRes.status === 'UP' ? 'UP' : 'DOWN');
    } catch {
      setDbHealth('DOWN');
    }

    try {
      const sInfo = await systemService.getSystemInfo();
      setSystemInfo(sInfo);
    } catch {
      // Ignored
    }
  };

  useEffect(() => {
    checkHealth();
  }, []);

  // Save Threshold Settings
  const handleSaveThresholds = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem('nexora_thresh_temp', tempThreshold.toString());
    localStorage.setItem('nexora_thresh_smoke', smokeThreshold.toString());
    localStorage.setItem('nexora_thresh_aqi', aqiThreshold.toString());
    localStorage.setItem('nexora_thresh_occ', occupancyThreshold.toString());
    localStorage.setItem('nexora_poll_interval', pollInterval.toString());
    setActionSuccess('Telemetry alarm thresholds and sampling frequencies saved successfully.');
  };

  // Save Display Preferences
  const handleSaveDisplay = (e: React.FormEvent) => {
    e.preventDefault();
    localStorage.setItem('nexora_wireframe', showWireframe.toString());
    localStorage.setItem('nexora_exploded_gap', explodedGap.toString());
    setActionSuccess('Digital Twin visual preferences saved successfully.');
  };

  // Data Actions
  const handlePurgeTelemetry = async () => {
    if (!window.confirm('Are you sure you want to purge all historical sensor readings and generated anomalies?')) return;
    setLoading(true);
    setActionError(null);
    try {
      await systemService.purgeTelemetry();
      setActionSuccess('Telemetry history purged successfully. Database is clean.');
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to purge telemetry data.');
    } finally {
      setLoading(false);
    }
  };

  const handleClearFacilityData = async () => {
    if (!window.confirm('WARNING: This will permanently delete ALL buildings, floors, rooms, and sensors. Operator accounts will remain intact. Proceed?')) return;
    setLoading(true);
    setActionError(null);
    try {
      await systemService.clearFacilityData();
      setActionSuccess('All facility data, rooms, and sensors deleted. Clean slate active.');
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to clear facility data.');
    } finally {
      setLoading(false);
    }
  };

  const handleSeedBaseline = async () => {
    setLoading(true);
    setActionError(null);
    try {
      await systemService.seedBaselineData();
      setActionSuccess('Clean baseline facility and sensors populated successfully.');
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to populate facility data.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-950/60 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <Settings className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold text-slate-100">Platform Settings & Data Management</h1>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-950 text-emerald-400 border border-emerald-800">
                ACTIVE
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Gateway connectivity, safety threshold calibration, and spatial database lifecycle management.
            </p>
          </div>
        </div>

        <button
          onClick={checkHealth}
          className="flex items-center gap-2 px-3.5 py-2 text-xs font-medium text-slate-300 bg-[#0e1624] hover:bg-[#162234] border border-[#1e2e47] rounded-xl transition-all self-start md:self-auto"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Ping Services</span>
        </button>
      </div>

      {/* Notifications */}
      {actionSuccess && (
        <div className="p-4 bg-emerald-950/60 border border-emerald-800 rounded-xl text-xs text-emerald-300 flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{actionSuccess}</span>
        </div>
      )}
      {actionError && (
        <div className="p-4 bg-rose-950/60 border border-rose-800 rounded-xl text-xs text-rose-300 flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 text-rose-400 shrink-0" />
          <span>{actionError}</span>
        </div>
      )}

      {/* Navigation Tabs */}
      <div className="flex gap-2 border-b border-[#1a2538] pb-1 overflow-x-auto">
        <button
          onClick={() => setActiveTab('data')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold rounded-t-xl transition-all ${
            activeTab === 'data'
              ? 'bg-[#0e1624] text-emerald-400 border-t border-x border-[#1e2e47]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Database className="w-4 h-4" />
          <span>Data Lifecycle Management</span>
        </button>

        <button
          onClick={() => setActiveTab('thresholds')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold rounded-t-xl transition-all ${
            activeTab === 'thresholds'
              ? 'bg-[#0e1624] text-emerald-400 border-t border-x border-[#1e2e47]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Sliders className="w-4 h-4" />
          <span>Safety & Alarm Thresholds</span>
        </button>

        <button
          onClick={() => setActiveTab('gateways')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold rounded-t-xl transition-all ${
            activeTab === 'gateways'
              ? 'bg-[#0e1624] text-emerald-400 border-t border-x border-[#1e2e47]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Server className="w-4 h-4" />
          <span>Service Health & Gateways</span>
        </button>

        <button
          onClick={() => setActiveTab('display')}
          className={`flex items-center gap-2 px-4 py-2.5 text-xs font-semibold rounded-t-xl transition-all ${
            activeTab === 'display'
              ? 'bg-[#0e1624] text-emerald-400 border-t border-x border-[#1e2e47]'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Boxes className="w-4 h-4" />
          <span>Digital Twin Display</span>
        </button>
      </div>

      {/* TAB 1: DATA LIFECYCLE MANAGEMENT */}
      {activeTab === 'data' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Card 1: Purge Telemetry History */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 mb-4">
                <Activity className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-bold text-slate-100">Purge Telemetry History</h3>
              <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                Clears historical sensor readings, generated anomalies, and risk assessment logs. Your buildings, rooms, and sensors remain intact, but telemetry charts and incident logs reset to zero.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#1a2538]">
              <button
                onClick={handlePurgeTelemetry}
                disabled={loading || !canManage}
                className="w-full py-2.5 px-4 bg-amber-600/20 hover:bg-amber-600/30 border border-amber-500/40 text-amber-300 rounded-xl text-xs font-bold transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                <Trash2 className="w-4 h-4" />
                <span>Purge Telemetry History</span>
              </button>
            </div>
          </div>

          {/* Card 2: Clear All Facility Data (Blank Slate) */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 rounded-xl bg-rose-500/10 border border-rose-500/30 flex items-center justify-center text-rose-400 mb-4">
                <Trash2 className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-bold text-slate-100">Clear All Facility Data</h3>
              <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                Removes all seeded buildings, floors, rooms, and sensors from the database. Use this if you want an entirely empty, blank system so you can add your own real buildings and spatial hierarchy.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#1a2538]">
              <button
                onClick={handleClearFacilityData}
                disabled={loading || !isAdmin}
                className="w-full py-2.5 px-4 bg-rose-600/20 hover:bg-rose-600/30 border border-rose-500/40 text-rose-300 rounded-xl text-xs font-bold transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                <Trash2 className="w-4 h-4" />
                <span>Wipe All Facilities (Clean Slate)</span>
              </button>
              {!isAdmin && (
                <div className="text-[10px] text-slate-500 text-center mt-1.5 font-mono">
                  Requires SYSTEM_ADMIN role
                </div>
              )}
            </div>
          </div>

          {/* Card 3: Populate Clean Baseline Facility */}
          <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl flex flex-col justify-between">
            <div>
              <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400 mb-4">
                <RotateCcw className="w-5 h-5" />
              </div>
              <h3 className="text-sm font-bold text-slate-100">Populate Baseline Facility</h3>
              <p className="text-xs text-slate-400 mt-2 leading-relaxed">
                Initializes the reference flagship building (NEXORA Innovation Tower) with 3 floors, 7 rooms, and calibrated IoT sensors. Safe to run on empty databases.
              </p>
            </div>
            <div className="mt-6 pt-4 border-t border-[#1a2538]">
              <button
                onClick={handleSeedBaseline}
                disabled={loading || !isAdmin}
                className="w-full py-2.5 px-4 bg-emerald-600/20 hover:bg-emerald-600/30 border border-emerald-500/40 text-emerald-300 rounded-xl text-xs font-bold transition-all disabled:opacity-50 flex items-center justify-center gap-2"
              >
                <RotateCcw className="w-4 h-4" />
                <span>Populate Reference Facility</span>
              </button>
              {!isAdmin && (
                <div className="text-[10px] text-slate-500 text-center mt-1.5 font-mono">
                  Requires SYSTEM_ADMIN role
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: SAFETY & ALARM THRESHOLDS */}
      {activeTab === 'thresholds' && (
        <form onSubmit={handleSaveThresholds} className="bg-[#080d16] border border-[#1a2538] rounded-2xl p-6 shadow-xl space-y-6">
          <div className="flex items-center justify-between border-b border-[#1a2538] pb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-100">Real-Time Telemetry & Safety Alarm Limits</h2>
              <p className="text-xs text-slate-400 mt-0.5">Define multi-criteria edge detection thresholds for anomaly scoring and safety incidents.</p>
            </div>
            <button
              type="submit"
              className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold transition-all shadow-lg shadow-emerald-950/40"
            >
              Save Alarm Limits
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Temp Limit */}
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-200">
                  <Thermometer className="w-4 h-4 text-rose-400" />
                  <span>Critical Temperature Alarm (°C)</span>
                </div>
                <span className="font-mono text-rose-400 font-bold">{tempThreshold}°C</span>
              </div>
              <input
                type="range"
                min="30"
                max="80"
                step="0.5"
                value={tempThreshold}
                onChange={(e) => setTempThreshold(parseFloat(e.target.value))}
                className="w-full accent-rose-500"
              />
              <p className="text-[11px] text-slate-400">Readings exceeding this value immediately elevate the room safety status to CRITICAL.</p>
            </div>

            {/* Smoke Limit */}
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-200">
                  <Flame className="w-4 h-4 text-amber-400" />
                  <span>Optical Smoke Alarm Threshold (ppm)</span>
                </div>
                <span className="font-mono text-amber-400 font-bold">{smokeThreshold} ppm</span>
              </div>
              <input
                type="range"
                min="0.1"
                max="3.0"
                step="0.05"
                value={smokeThreshold}
                onChange={(e) => setSmokeThreshold(parseFloat(e.target.value))}
                className="w-full accent-amber-500"
              />
              <p className="text-[11px] text-slate-400">Trigger value for fire evacuation dispatch and automated corridor seal alarms.</p>
            </div>

            {/* AQI Limit */}
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-200">
                  <Wind className="w-4 h-4 text-cyan-400" />
                  <span>Hazardous Air Quality Threshold (AQI)</span>
                </div>
                <span className="font-mono text-cyan-400 font-bold">{aqiThreshold} AQI</span>
              </div>
              <input
                type="range"
                min="50"
                max="300"
                step="5"
                value={aqiThreshold}
                onChange={(e) => setAqiThreshold(parseInt(e.target.value))}
                className="w-full accent-cyan-500"
              />
              <p className="text-[11px] text-slate-400">Indicates toxic gas buildup or HVAC filter failures requiring ventilation override.</p>
            </div>

            {/* Occupancy Warning */}
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-200">
                  <Users className="w-4 h-4 text-purple-400" />
                  <span>Max Occupancy Warning (% Capacity)</span>
                </div>
                <span className="font-mono text-purple-400 font-bold">{occupancyThreshold}%</span>
              </div>
              <input
                type="range"
                min="50"
                max="100"
                step="5"
                value={occupancyThreshold}
                onChange={(e) => setOccupancyThreshold(parseInt(e.target.value))}
                className="w-full accent-purple-500"
              />
              <p className="text-[11px] text-slate-400">Flag spaces nearing over-capacity thresholds to trigger crowd dispersal protocols.</p>
            </div>

            {/* Polling Interval */}
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3 md:col-span-2">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-xs font-medium text-slate-200">
                  <Activity className="w-4 h-4 text-emerald-400" />
                  <span>Telemetry Sampling Frequency (Seconds)</span>
                </div>
                <span className="font-mono text-emerald-400 font-bold">{pollInterval}s</span>
              </div>
              <input
                type="range"
                min="1"
                max="30"
                step="1"
                value={pollInterval}
                onChange={(e) => setPollInterval(parseInt(e.target.value))}
                className="w-full accent-emerald-500"
              />
              <p className="text-[11px] text-slate-400">Controls how often edge IoT sensors emit telemetry updates across the WebSocket bus.</p>
            </div>
          </div>
        </form>
      )}

      {/* TAB 3: GATEWAYS & SERVICE HEALTH */}
      {activeTab === 'gateways' && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-medium">Java Spring Boot Gateway</span>
                <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                  backendHealth === 'UP' ? 'bg-emerald-950 text-emerald-400 border-emerald-800' : 'bg-rose-950 text-rose-400 border-rose-800'
                }`}>
                  {backendHealth}
                </span>
              </div>
              <div className="text-lg font-bold text-slate-100 font-mono">PORT 8080</div>
              <div className="text-[11px] text-slate-500 font-mono">Base: /api/v1 (REST & STOMP WS)</div>
            </div>

            <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-medium">Python Edge AI Engine</span>
                <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                  aiHealth === 'UP' ? 'bg-purple-950 text-purple-400 border-purple-800' : 'bg-rose-950 text-rose-400 border-rose-800'
                }`}>
                  {aiHealth}
                </span>
              </div>
              <div className="text-lg font-bold text-slate-100 font-mono">PORT 8000</div>
              <div className="text-[11px] text-slate-500 font-mono">Scikit-Learn Isolation & Random Forest</div>
            </div>

            <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-2xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs text-slate-400 font-medium">Database Persistence Engine</span>
                <span className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                  dbHealth === 'UP' ? 'bg-cyan-950 text-cyan-400 border-cyan-800' : 'bg-rose-950 text-rose-400 border-rose-800'
                }`}>
                  {dbHealth}
                </span>
              </div>
              <div className="text-lg font-bold text-slate-100 font-mono">PostgreSQL / H2</div>
              <div className="text-[11px] text-slate-500 font-mono">Schema V1 through V6 Active</div>
            </div>
          </div>

          {systemInfo && (
            <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-2xl">
              <div className="text-xs font-semibold uppercase tracking-wider text-slate-400 mb-3">
                System Build & Host Diagnostics
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs">
                <div>
                  <span className="text-slate-500 block">Application:</span>
                  <span className="text-slate-200 font-mono">{systemInfo.application || 'NEXORA'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Version:</span>
                  <span className="text-slate-200 font-mono">{systemInfo.version || '2.0.0'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Java Runtime:</span>
                  <span className="text-slate-200 font-mono">{systemInfo.javaVersion || '21 LTS'}</span>
                </div>
                <div>
                  <span className="text-slate-500 block">Status:</span>
                  <span className="text-emerald-400 font-mono">ACTIVE & VERIFIED</span>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: DIGITAL TWIN DISPLAY */}
      {activeTab === 'display' && (
        <form onSubmit={handleSaveDisplay} className="bg-[#080d16] border border-[#1a2538] rounded-2xl p-6 shadow-xl space-y-6">
          <div className="flex items-center justify-between border-b border-[#1a2538] pb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-100">3D Digital Twin Visual Preferences</h2>
              <p className="text-xs text-slate-400 mt-0.5">Calibrate spatial rendering, floor layer spacing, and WebGL parameters.</p>
            </div>
            <button
              type="submit"
              className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold transition-all shadow-lg shadow-emerald-950/40"
            >
              Save Visual Settings
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-200">Exploded Floor Gap (Meters)</span>
                <span className="font-mono text-cyan-400 font-bold">{explodedGap}m</span>
              </div>
              <input
                type="range"
                min="0.5"
                max="8.0"
                step="0.5"
                value={explodedGap}
                onChange={(e) => setExplodedGap(parseFloat(e.target.value))}
                className="w-full accent-cyan-500"
              />
              <p className="text-[11px] text-slate-400">Vertical separation distance between floor levels when exploded view is enabled.</p>
            </div>

            <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl flex items-center justify-between">
              <div>
                <div className="text-xs font-medium text-slate-200">Structural Wireframe Mode</div>
                <p className="text-[11px] text-slate-400 mt-1">Render room geometry in high-tech structural wireframe.</p>
              </div>
              <input
                type="checkbox"
                checked={showWireframe}
                onChange={(e) => setShowWireframe(e.target.checked)}
                className="w-5 h-5 rounded accent-emerald-500"
              />
            </div>
          </div>
        </form>
      )}
    </div>
  );
};
