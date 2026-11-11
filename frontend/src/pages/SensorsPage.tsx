import React, { useState, useEffect, useCallback, useMemo } from 'react';
import {
  sensorApi,
  simulationApi,
  readingApi,
  buildingApi,
  floorApi,
  roomApi,
} from '../services/api';
import {
  Sensor,
  SensorReading,
  SensorStats,
  SensorType,
  CreateSensorPayload,
} from '../types/sensor';
import {
  SimulationSession,
  ScenarioType,
  ScenarioDefinition,
} from '../types/simulation';
import { Building, Floor, Room } from '../types/building';
import { useAuth } from '../context/AuthContext';
import {
  Radio,
  Activity,
  Flame,
  Droplets,
  Wind,
  Users,
  Thermometer,
  Play,
  Pause,
  Square,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  AlertOctagon,
  Building2,
  Layers,
  DoorOpen,
  Clock,
  Sparkles,
  RefreshCw,
  X,
  TrendingUp,
  Sliders,
} from 'lucide-react';

export const SensorsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);

  // Tabs: 'overview' | 'explorer' | 'simulation'
  const [activeTab, setActiveTab] = useState<'overview' | 'explorer' | 'simulation'>('overview');

  // Core Data State
  const [stats, setStats] = useState<SensorStats | null>(null);
  const [activeSession, setActiveSession] = useState<SimulationSession | null>(null);
  const [scenarios, setScenarios] = useState<ScenarioDefinition[]>([]);
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [floors, setFloors] = useState<Floor[]>([]);
  const [selectedFloorId, setSelectedFloorId] = useState<string>('');
  const [rooms, setRooms] = useState<Room[]>([]);
  const [selectedRoomId, setSelectedRoomId] = useState<string>('');
  const [sensors, setSensors] = useState<Sensor[]>([]);

  // Filter & Search
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedTypeFilter, setSelectedTypeFilter] = useState<string>('ALL');
  const [selectedStatusFilter, setSelectedStatusFilter] = useState<string>('ALL');

  // Loading & Notification
  const [isLoading, setIsLoading] = useState(false);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  // Modals
  const [selectedSensorForDetails, setSelectedSensorForDetails] = useState<Sensor | null>(null);
  const [sensorHistory, setSensorHistory] = useState<SensorReading[]>([]);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);

  const [selectedSensorForManual, setSelectedSensorForManual] = useState<Sensor | null>(null);
  const [manualValue, setManualValue] = useState<number>(0);
  const [isManualSubmitting, setIsManualSubmitting] = useState(false);

  const [isCreateSensorModalOpen, setIsCreateSensorModalOpen] = useState(false);
  const [newSensorPayload, setNewSensorPayload] = useState<CreateSensorPayload>({
    sensorName: '',
    sensorType: 'TEMPERATURE',
    simulationIntervalSeconds: 5,
  });

  const [isStartSimModalOpen, setIsStartSimModalOpen] = useState(false);
  const [simInterval, setSimInterval] = useState<number>(5);
  const [simScenarioType, setSimScenarioType] = useState<ScenarioType>('NORMAL');

  // Auto-dismiss toasts
  useEffect(() => {
    if (actionSuccess || actionError) {
      const t = setTimeout(() => {
        setActionSuccess(null);
        setActionError(null);
      }, 5000);
      return () => clearTimeout(t);
    }
  }, [actionSuccess, actionError]);

  // Initial Load: Stats, Buildings, Scenarios, Active Simulation
  const loadInitialData = useCallback(async () => {
    setIsLoading(true);
    try {
      const [sStats, bList, scList, actSession] = await Promise.all([
        sensorApi.getSensorStats(),
        buildingApi.getBuildings(),
        simulationApi.getAvailableScenarios(),
        simulationApi.getActiveSimulation(),
      ]);
      setStats(sStats);
      setBuildings(bList);
      setScenarios(scList);
      setActiveSession(actSession);

      if (bList.length > 0 && !selectedBuildingId) {
        setSelectedBuildingId(bList[0].id);
      }
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to initialize sensor telemetry.');
    } finally {
      setIsLoading(false);
    }
  }, [selectedBuildingId]);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  // Periodic Polling to Refresh Telemetry & Active Simulation
  useEffect(() => {
    const pollTimer = setInterval(async () => {
      try {
        const [actSession, sStats] = await Promise.all([
          simulationApi.getActiveSimulation(selectedBuildingId || undefined),
          sensorApi.getSensorStats(),
        ]);
        setActiveSession(actSession);
        setStats(sStats);

        // If on explorer or overview, refresh sensor readings
        if (activeTab === 'explorer' && selectedBuildingId) {
          const fresh = await sensorApi.getSensors(
            selectedBuildingId || undefined,
            selectedFloorId || undefined,
            selectedRoomId || undefined
          );
          setSensors(fresh);
        }
      } catch {
        // Silently skip transient background poll error
      }
    }, 5000);
    return () => clearInterval(pollTimer);
  }, [selectedBuildingId, selectedFloorId, selectedRoomId, activeTab]);

  // When selectedBuilding changes, fetch its floors
  useEffect(() => {
    if (!selectedBuildingId) return;
    floorApi.getFloors(selectedBuildingId)
      .then((fList) => {
        setFloors(fList);
        setSelectedFloorId('');
        setRooms([]);
        setSelectedRoomId('');
      })
      .catch(() => {});
  }, [selectedBuildingId]);

  // When selectedFloor changes, fetch its rooms
  useEffect(() => {
    if (!selectedFloorId) return;
    roomApi.getRooms(selectedFloorId)
      .then((rList) => {
        setRooms(rList);
        setSelectedRoomId('');
      })
      .catch(() => {});
  }, [selectedFloorId]);

  // Fetch sensors when scope changes
  const fetchScopedSensors = useCallback(async () => {
    try {
      const data = await sensorApi.getSensors(
        selectedBuildingId || undefined,
        selectedFloorId || undefined,
        selectedRoomId || undefined
      );
      setSensors(data);
    } catch (err: any) {
      setActionError('Failed to fetch sensors for selected scope.');
    }
  }, [selectedBuildingId, selectedFloorId, selectedRoomId]);

  useEffect(() => {
    fetchScopedSensors();
  }, [fetchScopedSensors]);

  // Open details modal and fetch readings history
  const handleOpenDetails = async (sensor: Sensor) => {
    setSelectedSensorForDetails(sensor);
    setIsLoadingHistory(true);
    try {
      const history = await readingApi.getSensorHistory(sensor.id);
      setSensorHistory(history);
    } catch {
      setSensorHistory([]);
    } finally {
      setIsLoadingHistory(false);
    }
  };

  // Open manual injection modal
  const handleOpenManual = (sensor: Sensor) => {
    setSelectedSensorForManual(sensor);
    setManualValue(sensor.lastReadingValue ?? 20);
  };

  // Submit manual reading injection
  const handleManualSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSensorForManual) return;
    setIsManualSubmitting(true);
    setActionError(null);
    try {
      const reading = await sensorApi.recordManualReading(selectedSensorForManual.id, manualValue);
      setActionSuccess(`Injected ${reading.readingValue} ${reading.unitSymbol} into ${selectedSensorForManual.sensorCode}`);
      setSelectedSensorForManual(null);
      fetchScopedSensors();
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Manual reading injection failed.');
    } finally {
      setIsManualSubmitting(false);
    }
  };

  // Auto-provision 5-sensor suite for a room
  const handleProvisionRoom = async (roomId: string) => {
    setActionError(null);
    try {
      const provisioned = await sensorApi.provisionRoomSensors(roomId);
      setActionSuccess(`Successfully verified and provisioned ${provisioned.length} sensors for room.`);
      fetchScopedSensors();
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to auto-provision sensors.');
    }
  };

  // Auto-provision 5-sensor suite for the entire building
  const handleProvisionBuilding = async (buildingId: string) => {
    if (!window.confirm('Provision standard 5-sensor suites across all rooms in this building?')) return;
    setActionError(null);
    try {
      const provisioned = await sensorApi.provisionBuildingSensors(buildingId);
      setActionSuccess(`Provisioned ${provisioned.length} sensors across building topology.`);
      fetchScopedSensors();
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to provision building sensors.');
    }
  };

  // Create single custom sensor
  const handleCreateSensor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedRoomId) {
      setActionError('Please select a specific target room to create this sensor.');
      return;
    }
    setActionError(null);
    try {
      const created = await sensorApi.createSensor(selectedRoomId, newSensorPayload);
      setActionSuccess(`Registered sensor ${created.sensorCode} successfully.`);
      setIsCreateSensorModalOpen(false);
      setNewSensorPayload({
        sensorName: '',
        sensorType: 'TEMPERATURE',
        simulationIntervalSeconds: 5,
      });
      fetchScopedSensors();
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Sensor registration failed.');
    }
  };

  // Simulation Controls: Start
  const handleStartSimulation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBuildingId) {
      setActionError('Select a target building for simulation session.');
      return;
    }
    setActionError(null);
    try {
      const session = await simulationApi.startSimulation({
        buildingId: selectedBuildingId,
        sessionName: `LiveSim-${buildings.find((b) => b.id === selectedBuildingId)?.buildingCode || 'BLD'}`,
        simulationIntervalSeconds: simInterval,
        scenarioType: simScenarioType,
      });
      setActiveSession(session);
      setIsStartSimModalOpen(false);
      setActionSuccess(`Simulation session started [Interval: ${session.simulationIntervalSeconds}s, Scenario: ${session.scenarioType}].`);
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to start simulation session.');
    }
  };

  // Simulation Controls: Pause
  const handlePauseSimulation = async () => {
    if (!activeSession) return;
    setActionError(null);
    try {
      const updated = await simulationApi.pauseSimulation(activeSession.id);
      setActiveSession(updated);
      setActionSuccess('Simulation session paused.');
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to pause simulation.');
    }
  };

  // Simulation Controls: Resume
  const handleResumeSimulation = async () => {
    if (!activeSession) return;
    setActionError(null);
    try {
      const updated = await simulationApi.resumeSimulation(activeSession.id);
      setActiveSession(updated);
      setActionSuccess('Simulation session resumed.');
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to resume simulation.');
    }
  };

  // Simulation Controls: Stop
  const handleStopSimulation = async () => {
    if (!activeSession) return;
    if (!window.confirm('Are you sure you want to stop the active simulation session? Readings generation will halt.')) {
      return;
    }
    setActionError(null);
    try {
      await simulationApi.stopSimulation(activeSession.id);
      setActiveSession(null);
      setActionSuccess('Simulation session stopped successfully.');
      const sStats = await sensorApi.getSensorStats();
      setStats(sStats);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to stop simulation.');
    }
  };

  // Activate Scenario
  const handleActivateScenario = async (type: ScenarioType) => {
    if (!activeSession) {
      setActionError('No running simulation session found. Please start a session first.');
      return;
    }
    setActionError(null);
    try {
      await simulationApi.activateScenario(activeSession.id, {
        scenarioType: type,
        durationSeconds: 300,
      });
      const refreshed = await simulationApi.getActiveSimulation(selectedBuildingId || undefined);
      setActiveSession(refreshed);
      setActionSuccess(`Activated scenario: ${type}. Generating simulated anomalous telemetry.`);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to activate scenario.');
    }
  };

  // Filtered Sensors list
  const filteredSensors = useMemo(() => {
    return sensors.filter((s) => {
      const matchSearch =
        s.sensorName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        s.sensorCode.toLowerCase().includes(searchQuery.toLowerCase()) ||
        s.roomName.toLowerCase().includes(searchQuery.toLowerCase());
      const matchType = selectedTypeFilter === 'ALL' || s.sensorType === selectedTypeFilter;
      const matchStatus = selectedStatusFilter === 'ALL' || s.status === selectedStatusFilter;
      return matchSearch && matchType && matchStatus;
    });
  }, [sensors, searchQuery, selectedTypeFilter, selectedStatusFilter]);

  // Type Icon and Styling Helper
  const getSensorTypeBadge = (type: SensorType) => {
    switch (type) {
      case 'TEMPERATURE':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-amber-500/10 text-amber-400 border border-amber-500/30">
            <Thermometer className="w-3 h-3" />
            Temperature
          </span>
        );
      case 'HUMIDITY':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-cyan-500/10 text-cyan-400 border border-cyan-500/30">
            <Droplets className="w-3 h-3" />
            Humidity
          </span>
        );
      case 'SMOKE':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-rose-500/10 text-rose-400 border border-rose-500/30">
            <Flame className="w-3 h-3" />
            Smoke (Simulated)
          </span>
        );
      case 'AIR_QUALITY':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <Wind className="w-3 h-3" />
            Air Quality (AQI)
          </span>
        );
      case 'OCCUPANCY':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-purple-500/10 text-purple-400 border border-purple-500/30">
            <Users className="w-3 h-3" />
            Occupancy
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-emerald-500/20 to-cyan-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
              <Radio className="w-5 h-5 animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-2xl font-bold tracking-tight text-slate-100">Virtual IoT Sensor Registry</h1>
                <span className="px-2 py-0.5 text-xs font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 rounded">
                  Phase 3 Active
                </span>
              </div>
              <p className="text-xs text-slate-400 mt-0.5">
                Multi-zone simulated sensor fabric, brownian noise generators, and scenario-based anomaly injection.
              </p>
            </div>
          </div>
        </div>

        {/* Global Action Toolbar */}
        <div className="flex items-center gap-2.5 self-start">
          <button
            onClick={() => {
              loadInitialData();
              fetchScopedSensors();
            }}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors"
            title="Refresh Telemetry"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>

          {canManage && (
            <>
              {activeSession && activeSession.status === 'RUNNING' ? (
                <div className="flex items-center gap-2">
                  <button
                    onClick={handlePauseSimulation}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-lg transition-colors"
                  >
                    <Pause className="w-3.5 h-3.5" />
                    <span>Pause Sim</span>
                  </button>
                  <button
                    onClick={handleStopSimulation}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-red-300 bg-red-500/10 hover:bg-red-500/20 border border-red-500/30 rounded-lg transition-colors"
                  >
                    <Square className="w-3.5 h-3.5" />
                    <span>Stop Sim</span>
                  </button>
                </div>
              ) : activeSession && activeSession.status === 'PAUSED' ? (
                <button
                  onClick={handleResumeSimulation}
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
                >
                  <Play className="w-3.5 h-3.5" />
                  <span>Resume Sim</span>
                </button>
              ) : (
                <button
                  onClick={() => setIsStartSimModalOpen(true)}
                  className="inline-flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
                >
                  <Play className="w-3.5 h-3.5" />
                  <span>Start Simulation</span>
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* Notifications */}
      {actionSuccess && (
        <div className="p-3.5 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-between gap-2.5 text-xs text-emerald-300 animate-in fade-in">
          <div className="flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span>{actionSuccess}</span>
          </div>
          <button onClick={() => setActionSuccess(null)} className="text-emerald-400 hover:text-emerald-200">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {actionError && (
        <div className="p-3.5 rounded-lg bg-red-500/10 border border-red-500/30 flex items-center justify-between gap-2.5 text-xs text-red-300 animate-in fade-in">
          <div className="flex items-center gap-2">
            <AlertOctagon className="w-4 h-4 text-red-400 shrink-0" />
            <span>{actionError}</span>
          </div>
          <button onClick={() => setActionError(null)} className="text-red-400 hover:text-red-200">
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Simulation Running Banner */}
      {activeSession && (
        <div className={`p-4 rounded-xl border backdrop-blur-md flex flex-col md:flex-row items-start md:items-center justify-between gap-4 ${
          activeSession.status === 'RUNNING'
            ? 'bg-emerald-950/30 border-emerald-500/30'
            : 'bg-amber-950/30 border-amber-500/30'
        }`}>
          <div className="flex items-center gap-3">
            <div className={`w-3 h-3 rounded-full ${
              activeSession.status === 'RUNNING' ? 'bg-emerald-400 animate-ping' : 'bg-amber-400'
            }`} />
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold text-slate-100 uppercase tracking-wide">
                  Simulation Session Active:
                </span>
                <span className="text-xs font-mono text-emerald-400 font-semibold">{activeSession.sessionName}</span>
                <span className={`px-2 py-0.5 rounded text-[10px] font-mono ${
                  activeSession.status === 'RUNNING' ? 'bg-emerald-950 text-emerald-400 border border-emerald-800' : 'bg-amber-950 text-amber-400 border border-amber-800'
                }`}>
                  {activeSession.status}
                </span>
              </div>
              <p className="text-[11px] text-slate-400 mt-0.5">
                Facility: <span className="text-slate-200 font-medium">{activeSession.buildingName}</span> ({activeSession.buildingCode}) • Scenario: <span className="text-amber-400 font-mono font-medium">{activeSession.scenarioType}</span> • Interval: <span className="text-slate-200 font-mono">{activeSession.simulationIntervalSeconds}s</span>
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 self-end md:self-auto">
            <button
              onClick={() => setActiveTab('simulation')}
              className="inline-flex items-center gap-1.5 px-3 py-1 text-xs font-medium text-cyan-300 bg-cyan-950/60 hover:bg-cyan-900/60 border border-cyan-800 rounded-lg transition-colors"
            >
              <Sliders className="w-3.5 h-3.5" />
              <span>Simulation Controls</span>
            </button>
          </div>
        </div>
      )}

      {/* Navigation Tab Bar */}
      <div className="border-b border-slate-800 flex gap-6 text-sm font-medium">
        <button
          onClick={() => setActiveTab('overview')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'overview'
              ? 'text-emerald-400 border-b-2 border-emerald-400'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Activity className="w-4 h-4" />
          <span>Fleet Overview</span>
        </button>

        <button
          onClick={() => setActiveTab('explorer')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'explorer'
              ? 'text-emerald-400 border-b-2 border-emerald-400'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Sensor Topology Explorer</span>
        </button>

        <button
          onClick={() => setActiveTab('simulation')}
          className={`pb-3 transition-colors flex items-center gap-2 ${
            activeTab === 'simulation'
              ? 'text-emerald-400 border-b-2 border-emerald-400'
              : 'text-slate-400 hover:text-slate-200'
          }`}
        >
          <Sliders className="w-4 h-4" />
          <span>Simulation & Scenarios</span>
        </button>
      </div>

      {/* TAB 1: FLEET OVERVIEW */}
      {activeTab === 'overview' && (
        <div className="space-y-6">
          {/* Top KPI Metrics Cards */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 shadow-lg backdrop-blur-md">
              <div className="text-[11px] font-mono uppercase tracking-wider text-slate-400">Total Virtual Sensors</div>
              <div className="text-2xl font-bold text-slate-100 mt-1 font-mono">{stats?.totalSensors ?? 0}</div>
              <div className="text-[11px] text-slate-500 mt-1">Simulated IoT endpoints</div>
            </div>

            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 shadow-lg backdrop-blur-md">
              <div className="text-[11px] font-mono uppercase tracking-wider text-slate-400">Active Sensors</div>
              <div className="text-2xl font-bold text-emerald-400 mt-1 font-mono">{stats?.activeSensors ?? 0}</div>
              <div className="text-[11px] text-emerald-500/80 mt-1">Generating telemetry stream</div>
            </div>

            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 shadow-lg backdrop-blur-md">
              <div className="text-[11px] font-mono uppercase tracking-wider text-slate-400">Inactive / Standby</div>
              <div className="text-2xl font-bold text-slate-400 mt-1 font-mono">{stats?.inactiveSensors ?? 0}</div>
              <div className="text-[11px] text-slate-500 mt-1">Off-line or paused</div>
            </div>

            <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 shadow-lg backdrop-blur-md">
              <div className="text-[11px] font-mono uppercase tracking-wider text-slate-400">Sensors In Error</div>
              <div className="text-2xl font-bold text-rose-400 mt-1 font-mono">{stats?.errorSensors ?? 0}</div>
              <div className="text-[11px] text-rose-400/80 mt-1">Anomalous or degraded</div>
            </div>
          </div>

          {/* Sensor Distribution by Type */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-xl">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Radio className="w-4 h-4 text-emerald-400" />
                <h2 className="text-sm font-semibold tracking-wide uppercase text-slate-200">
                  Sensor Type Distribution Across Monitored Building Topology
                </h2>
              </div>
              <span className="text-xs font-mono text-slate-500">5 Telemetry Modalities</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 font-mono">
              {[
                { type: 'TEMPERATURE', label: 'Temperature', icon: Thermometer, color: 'text-amber-400', count: stats?.distributionByType?.TEMPERATURE ?? 0 },
                { type: 'HUMIDITY', label: 'Humidity', icon: Droplets, color: 'text-cyan-400', count: stats?.distributionByType?.HUMIDITY ?? 0 },
                { type: 'SMOKE', label: 'Smoke Detector', icon: Flame, color: 'text-rose-400', count: stats?.distributionByType?.SMOKE ?? 0 },
                { type: 'AIR_QUALITY', label: 'Air Quality (AQI)', icon: Wind, color: 'text-emerald-400', count: stats?.distributionByType?.AIR_QUALITY ?? 0 },
                { type: 'OCCUPANCY', label: 'Occupancy Flow', icon: Users, color: 'text-purple-400', count: stats?.distributionByType?.OCCUPANCY ?? 0 },
              ].map((item) => {
                const Icon = item.icon;
                return (
                  <div key={item.type} className="p-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl flex items-center justify-between">
                    <div>
                      <div className="text-[10px] text-slate-400 uppercase tracking-wider">{item.label}</div>
                      <div className={`text-xl font-bold mt-1 ${item.color}`}>{item.count}</div>
                    </div>
                    <div className={`p-2 rounded-lg bg-slate-900 border border-slate-800 ${item.color}`}>
                      <Icon className="w-5 h-5" />
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Quick Recent Telemetry Table */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-xl">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Activity className="w-4 h-4 text-cyan-400" />
                <h2 className="text-sm font-semibold tracking-wide uppercase text-slate-200">
                  Live Stream Sample (Most Recent Sensor Heartbeats)
                </h2>
              </div>
              <span className="text-xs font-mono text-slate-500">{sensors.length} Endpoints Registered</span>
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs font-mono">
                <thead>
                  <tr className="border-b border-slate-800 text-slate-400 uppercase text-[10px] tracking-wider">
                    <th className="pb-2.5">Sensor Code</th>
                    <th className="pb-2.5">Type</th>
                    <th className="pb-2.5">Facility / Location</th>
                    <th className="pb-2.5">Current Value</th>
                    <th className="pb-2.5">Status</th>
                    <th className="pb-2.5">Last Sampled</th>
                    <th className="pb-2.5 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60">
                  {sensors.slice(0, 10).map((s) => (
                    <tr key={s.id} className="hover:bg-slate-800/40 transition-colors">
                      <td className="py-2.5 font-bold text-slate-200">{s.sensorCode}</td>
                      <td className="py-2.5">{getSensorTypeBadge(s.sensorType)}</td>
                      <td className="py-2.5 text-slate-400 font-sans">
                        <span className="text-slate-200 font-medium">{s.roomName}</span> (Floor {s.floorNumber})
                      </td>
                      <td className="py-2.5 text-emerald-400 font-bold text-sm">
                        {s.lastReadingValue != null ? `${s.lastReadingValue} ${s.unitSymbol}` : '—'}
                      </td>
                      <td className="py-2.5">
                        <span className={`px-2 py-0.5 rounded text-[10px] ${
                          s.status === 'ACTIVE'
                            ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30'
                            : 'bg-slate-800 text-slate-400'
                        }`}>
                          {s.status}
                        </span>
                      </td>
                      <td className="py-2.5 text-slate-500 text-[11px]">
                        {s.lastReadingAt ? new Date(s.lastReadingAt).toLocaleTimeString() : 'Awaiting tick'}
                      </td>
                      <td className="py-2.5 text-right">
                        <button
                          onClick={() => handleOpenDetails(s)}
                          className="px-2 py-1 text-[11px] font-sans font-medium text-cyan-400 hover:text-cyan-300 bg-cyan-500/10 hover:bg-cyan-500/20 rounded border border-cyan-500/20"
                        >
                          Telemetry
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}

      {/* TAB 2: SENSOR EXPLORER */}
      {activeTab === 'explorer' && (
        <div className="space-y-6">
          {/* Cascading Topology Filters */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl backdrop-blur-xl">
            <div className="flex items-center justify-between mb-4 pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Filter className="w-4 h-4 text-emerald-400" />
                <h3 className="text-xs font-semibold uppercase tracking-wider text-slate-200">
                  Spatial Scope Selection (Building → Floor → Room)
                </h3>
              </div>

              {canManage && selectedBuildingId && (
                <button
                  onClick={() => handleProvisionBuilding(selectedBuildingId)}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-emerald-300 bg-emerald-950/60 hover:bg-emerald-900/60 border border-emerald-800 rounded-lg transition-colors"
                >
                  <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
                  <span>Provision Entire Building</span>
                </button>
              )}
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
              {/* Building Selector */}
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Target Facility</label>
                <div className="relative">
                  <Building2 className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
                  <select
                    value={selectedBuildingId}
                    onChange={(e) => setSelectedBuildingId(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-200 outline-none"
                  >
                    {buildings.map((b) => (
                      <option key={b.id} value={b.id}>
                        {b.name} ({b.buildingCode})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Floor Selector */}
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Vertical Floor</label>
                <div className="relative">
                  <Layers className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
                  <select
                    value={selectedFloorId}
                    onChange={(e) => setSelectedFloorId(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-200 outline-none"
                  >
                    <option value="">All Floor Levels</option>
                    {floors.map((f) => (
                      <option key={f.id} value={f.id}>
                        Level {f.floorNumber} — {f.floorName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Room Selector */}
              <div>
                <label className="block text-[11px] font-mono text-slate-400 mb-1">Specific Room</label>
                <div className="relative">
                  <DoorOpen className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
                  <select
                    value={selectedRoomId}
                    onChange={(e) => setSelectedRoomId(e.target.value)}
                    disabled={!selectedFloorId}
                    className="w-full pl-9 pr-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-200 outline-none disabled:opacity-50"
                  >
                    <option value="">All Floor Rooms</option>
                    {rooms.map((r) => (
                      <option key={r.id} value={r.id}>
                        {r.roomName} ({r.roomCode})
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>

            {/* Room Action Strip if Room Selected */}
            {selectedRoomId && (
              <div className="mt-4 pt-3 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3">
                <div className="text-xs text-slate-300">
                  Targeted Room: <span className="font-semibold text-emerald-400">{rooms.find((r) => r.id === selectedRoomId)?.roomName}</span> (Max Capacity: {rooms.find((r) => r.id === selectedRoomId)?.maximumCapacity} occupants)
                </div>

                {canManage && (
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleProvisionRoom(selectedRoomId)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-emerald-300 bg-emerald-600/20 hover:bg-emerald-600/30 border border-emerald-500/30 rounded-lg transition-colors"
                    >
                      <Sparkles className="w-3.5 h-3.5 text-emerald-400" />
                      <span>Auto-Provision 5 Sensors</span>
                    </button>
                    <button
                      onClick={() => setIsCreateSensorModalOpen(true)}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors"
                    >
                      <Plus className="w-3.5 h-3.5" />
                      <span>Custom Sensor</span>
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Search & Filter Toolbar */}
          <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
            <div className="relative flex-1">
              <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
              <input
                type="text"
                placeholder="Search sensors by code, name, or room..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-9 pr-3 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none focus:border-emerald-500"
              />
            </div>

            <div className="flex items-center gap-2">
              <select
                value={selectedTypeFilter}
                onChange={(e) => setSelectedTypeFilter(e.target.value)}
                className="px-3 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none"
              >
                <option value="ALL">All Types</option>
                <option value="TEMPERATURE">Temperature</option>
                <option value="HUMIDITY">Humidity</option>
                <option value="SMOKE">Smoke</option>
                <option value="AIR_QUALITY">Air Quality</option>
                <option value="OCCUPANCY">Occupancy</option>
              </select>

              <select
                value={selectedStatusFilter}
                onChange={(e) => setSelectedStatusFilter(e.target.value)}
                className="px-3 py-2 bg-slate-900 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none"
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="ERROR">Error</option>
              </select>
            </div>
          </div>

          {/* Sensors Grid / Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredSensors.map((sensor) => (
              <div
                key={sensor.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700 rounded-xl p-4 shadow-lg backdrop-blur-md flex flex-col justify-between transition-all group"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-mono text-xs font-bold text-slate-200">{sensor.sensorCode}</span>
                    {getSensorTypeBadge(sensor.sensorType)}
                  </div>

                  <h4 className="text-sm font-semibold text-slate-100 group-hover:text-emerald-300 transition-colors">
                    {sensor.sensorName}
                  </h4>

                  <p className="text-xs text-slate-400 mt-1 flex items-center gap-1 font-sans">
                    <DoorOpen className="w-3.5 h-3.5 text-slate-500" />
                    <span>{sensor.roomName}</span>
                    <span className="text-slate-600">•</span>
                    <span className="font-mono text-[11px]">{sensor.roomCode}</span>
                  </p>

                  {/* Reading Display */}
                  <div className="my-4 p-3 bg-slate-950/70 border border-slate-800/80 rounded-xl flex items-center justify-between">
                    <div>
                      <div className="text-[10px] text-slate-500 uppercase tracking-wider font-mono">Live Telemetry</div>
                      <div className="text-2xl font-extrabold text-emerald-400 font-mono mt-0.5">
                        {sensor.lastReadingValue != null ? `${sensor.lastReadingValue} ` : '— '}
                        <span className="text-xs font-normal text-slate-400">{sensor.unitSymbol}</span>
                      </div>
                    </div>

                    <div className="text-right">
                      <div className="text-[10px] text-slate-500 font-mono">Interval: {sensor.simulationIntervalSeconds}s</div>
                      <div className="text-[10px] text-slate-400 font-mono mt-1 flex items-center gap-1 justify-end">
                        <Clock className="w-3 h-3 text-slate-500" />
                        <span>{sensor.lastReadingAt ? new Date(sensor.lastReadingAt).toLocaleTimeString() : 'Waiting'}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {/* Card Actions */}
                <div className="pt-3 border-t border-slate-800 flex items-center justify-between gap-2">
                  <button
                    onClick={() => handleOpenDetails(sensor)}
                    className="inline-flex items-center gap-1 text-xs font-medium text-slate-300 hover:text-white px-2.5 py-1.5 rounded bg-slate-800 hover:bg-slate-700 transition-colors"
                  >
                    <TrendingUp className="w-3.5 h-3.5 text-cyan-400" />
                    <span>History & Details</span>
                  </button>

                  {canManage && (
                    <button
                      onClick={() => handleOpenManual(sensor)}
                      className="inline-flex items-center gap-1 text-xs font-medium text-amber-300 hover:text-amber-200 px-2.5 py-1.5 rounded bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/20 transition-colors"
                      title="Inject manual test reading value"
                    >
                      <Sliders className="w-3.5 h-3.5" />
                      <span>Inject Value</span>
                    </button>
                  )}
                </div>
              </div>
            ))}
          </div>

          {filteredSensors.length === 0 && (
            <div className="p-12 text-center bg-slate-900/40 border border-slate-800/80 rounded-2xl">
              <Radio className="w-10 h-10 text-slate-600 mx-auto mb-3 opacity-40" />
              <h3 className="text-sm font-semibold text-slate-300">No sensors match your query</h3>
              <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
                Select a room and click "Auto-Provision 5 Sensors" to generate virtual telemetry endpoints.
              </p>
            </div>
          )}
        </div>
      )}

      {/* TAB 3: SIMULATION & SCENARIOS */}
      {activeTab === 'simulation' && (
        <div className="space-y-6">
          {/* Active Simulation Session Overview Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-xl">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between pb-4 mb-5 border-b border-slate-800 gap-4">
              <div>
                <h2 className="text-base font-bold text-slate-100 flex items-center gap-2">
                  <Sliders className="w-5 h-5 text-emerald-400" />
                  Simulation Control Center
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Real-time session orchestration, Brownian noise parameters, and anomaly trigger engine.
                </p>
              </div>

              {activeSession ? (
                <div className="flex items-center gap-2">
                  {activeSession.status === 'RUNNING' ? (
                    <button
                      onClick={handlePauseSimulation}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-amber-300 bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 rounded-lg transition-colors"
                    >
                      <Pause className="w-3.5 h-3.5" />
                      <span>Pause Session</span>
                    </button>
                  ) : (
                    <button
                      onClick={handleResumeSimulation}
                      className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-emerald-300 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 rounded-lg transition-colors"
                    >
                      <Play className="w-3.5 h-3.5" />
                      <span>Resume Session</span>
                    </button>
                  )}

                  <button
                    onClick={handleStopSimulation}
                    className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-red-300 bg-red-500/10 hover:bg-red-500/20 border border-red-500/30 rounded-lg transition-colors"
                  >
                    <Square className="w-3.5 h-3.5" />
                    <span>Stop Session</span>
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setIsStartSimModalOpen(true)}
                  className="inline-flex items-center gap-1.5 px-4 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
                >
                  <Play className="w-4 h-4" />
                  <span>Launch New Simulation Session</span>
                </button>
              )}
            </div>

            {/* Session Status Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 font-mono">
              <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                <div className="text-[10px] text-slate-500 uppercase">Session Status</div>
                <div className="text-lg font-bold text-slate-100 mt-1 flex items-center gap-1.5">
                  <span className={`w-2.5 h-2.5 rounded-full ${activeSession?.status === 'RUNNING' ? 'bg-emerald-400 animate-pulse' : 'bg-slate-500'}`} />
                  {activeSession?.status || 'IDLE'}
                </div>
              </div>

              <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                <div className="text-[10px] text-slate-500 uppercase">Active Scenario</div>
                <div className="text-lg font-bold text-amber-400 mt-1">
                  {activeSession?.scenarioType || 'NONE'}
                </div>
              </div>

              <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                <div className="text-[10px] text-slate-500 uppercase">Simulation Cadence</div>
                <div className="text-lg font-bold text-cyan-400 mt-1">
                  {activeSession ? `${activeSession.simulationIntervalSeconds}s` : '5s Default'}
                </div>
              </div>

              <div className="bg-slate-950/60 border border-slate-800/80 rounded-xl p-3.5">
                <div className="text-[10px] text-slate-500 uppercase">Active Sensors</div>
                <div className="text-lg font-bold text-emerald-400 mt-1">
                  {activeSession ? activeSession.activeSensorCount : 0} Nodes
                </div>
              </div>
            </div>
          </div>

          {/* Scenario Catalog */}
          <div className="space-y-4">
            <div>
              <h3 className="text-base font-bold text-slate-100 flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-emerald-400" />
                Preconfigured Simulation Scenarios
              </h3>
              <p className="text-xs text-slate-400 mt-0.5">
                Activate predefined environmental events to test telemetry pipelines, sensor threshold compliance, and room occupancy limits.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {scenarios.map((sc) => {
                const isActive = activeSession && activeSession.scenarioType === sc.type;
                const isCritical = sc.severity === 'CRITICAL';
                const isHigh = sc.severity === 'HIGH';

                return (
                  <div
                    key={sc.type}
                    className={`rounded-2xl p-5 border transition-all flex flex-col justify-between ${
                      isActive
                        ? 'bg-emerald-950/30 border-emerald-500 shadow-lg shadow-emerald-500/10'
                        : isCritical
                        ? 'bg-slate-900/80 border-rose-500/30 hover:border-rose-500/60'
                        : isHigh
                        ? 'bg-slate-900/80 border-purple-500/30 hover:border-purple-500/60'
                        : 'bg-slate-900/80 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div>
                      <div className="flex items-center justify-between mb-3">
                        <span className={`px-2 py-0.5 rounded text-[10px] font-mono font-medium ${
                          isCritical
                            ? 'bg-rose-500/10 text-rose-400 border border-rose-500/30'
                            : isHigh
                            ? 'bg-purple-500/10 text-purple-400 border border-purple-500/30'
                            : 'bg-slate-800 text-slate-300 border border-slate-700'
                        }`}>
                          SEVERITY: {sc.severity}
                        </span>

                        {isActive && (
                          <span className="flex items-center gap-1 text-[11px] font-mono text-emerald-400 font-bold">
                            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
                            ACTIVE NOW
                          </span>
                        )}
                      </div>

                      <h4 className="text-sm font-bold text-slate-100 mb-2">{sc.name}</h4>
                      <p className="text-xs text-slate-400 leading-relaxed">{sc.description}</p>
                    </div>

                    <div className="mt-5 pt-4 border-t border-slate-800/80 flex items-center justify-between">
                      <span className="text-[11px] font-mono text-slate-500">{sc.type}</span>
                      {canManage && (
                        <button
                          onClick={() => handleActivateScenario(sc.type)}
                          disabled={isActive || !activeSession || activeSession.status !== 'RUNNING'}
                          className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-all ${
                            isActive
                              ? 'bg-emerald-600/30 text-emerald-300 border border-emerald-500/30 cursor-default'
                              : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 disabled:opacity-40'
                          }`}
                        >
                          {isActive ? 'Scenario Active' : 'Activate Scenario'}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}

      {/* MODAL 1: SENSOR DETAILS & HISTORICAL CHART */}
      {selectedSensorForDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-2xl w-full p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2.5">
                <div className="p-2 rounded-xl bg-slate-800 border border-slate-700 text-emerald-400">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-100">{selectedSensorForDetails.sensorName}</h3>
                  <p className="text-xs font-mono text-slate-400">{selectedSensorForDetails.sensorCode}</p>
                </div>
              </div>
              <button
                onClick={() => setSelectedSensorForDetails(null)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Current Value Pill */}
            <div className="p-4 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between font-mono">
              <div>
                <span className="text-[10px] text-slate-500 uppercase">Current Reading</span>
                <div className="text-3xl font-extrabold text-emerald-400 mt-0.5">
                  {selectedSensorForDetails.lastReadingValue ?? '—'}
                  <span className="text-sm font-normal text-slate-400 ml-1.5">{selectedSensorForDetails.unitSymbol}</span>
                </div>
              </div>
              <div className="text-right text-xs text-slate-400 space-y-1">
                <div>Type: <span className="text-slate-200">{selectedSensorForDetails.sensorType}</span></div>
                <div>Cadence: <span className="text-slate-200">{selectedSensorForDetails.simulationIntervalSeconds}s</span></div>
                <div>Status: <span className="text-emerald-400">{selectedSensorForDetails.status}</span></div>
              </div>
            </div>

            {/* Historical Readings SVG Chart */}
            <div className="space-y-2">
              <div className="flex items-center justify-between text-xs text-slate-400 font-mono">
                <span className="flex items-center gap-1.5">
                  <TrendingUp className="w-3.5 h-3.5 text-cyan-400" />
                  Recent Telemetry Trend (Past Readings)
                </span>
                <span>{sensorHistory.length} Samples</span>
              </div>

              <div className="bg-slate-950 border border-slate-800/80 rounded-xl p-4 h-48 flex items-center justify-center">
                {isLoadingHistory ? (
                  <div className="text-xs text-slate-500 font-mono flex items-center gap-2">
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    <span>Loading telemetry datapoints...</span>
                  </div>
                ) : sensorHistory.length < 2 ? (
                  <div className="text-xs text-slate-500 font-mono">Insufficient datapoints to plot trend curve</div>
                ) : (
                  <div className="w-full h-full flex flex-col justify-between">
                    {/* SVG Curve */}
                    {(() => {
                      const values = sensorHistory.map((r) => r.readingValue);
                      const min = Math.min(...values);
                      const max = Math.max(...values);
                      const range = max - min || 1;
                      const width = 500;
                      const height = 110;

                      const points = values
                        .map((v, i) => {
                          const x = (i / (values.length - 1)) * width;
                          const y = height - ((v - min) / range) * (height - 20) - 10;
                          return `${x},${y}`;
                        })
                        .join(' ');

                      return (
                        <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-32 overflow-visible">
                          <polyline
                            fill="none"
                            stroke="#10b981"
                            strokeWidth="2.5"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            points={points}
                          />
                        </svg>
                      );
                    })()}
                    <div className="flex items-center justify-between text-[10px] font-mono text-slate-500 border-t border-slate-800/60 pt-2">
                      <span>Oldest: {sensorHistory[sensorHistory.length - 1]?.readingValue} {selectedSensorForDetails.unitSymbol}</span>
                      <span>Latest: {sensorHistory[0]?.readingValue} {selectedSensorForDetails.unitSymbol}</span>
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedSensorForDetails(null)}
                className="px-4 py-2 text-xs font-medium text-slate-200 bg-slate-800 hover:bg-slate-700 rounded-lg transition-colors"
              >
                Close Details
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: MANUAL READING INJECTION */}
      {selectedSensorForManual && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Sliders className="w-5 h-5 text-amber-400" />
                <h3 className="text-base font-bold text-slate-100">Manual Telemetry Injection</h3>
              </div>
              <button
                onClick={() => setSelectedSensorForManual(null)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <p className="text-xs text-slate-400">
              Manually override the simulated reading for <span className="text-slate-200 font-semibold">{selectedSensorForManual.sensorName}</span> ({selectedSensorForManual.sensorCode}).
            </p>

            <form onSubmit={handleManualSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-mono text-slate-300 mb-1">
                  Inject Value ({selectedSensorForManual.unitSymbol})
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={manualValue}
                  onChange={(e) => setManualValue(parseFloat(e.target.value))}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-amber-500 rounded-lg text-sm text-slate-100 font-mono outline-none"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setSelectedSensorForManual(null)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200 rounded-lg"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isManualSubmitting}
                  className="px-4 py-2 text-xs font-medium text-white bg-amber-600 hover:bg-amber-500 rounded-lg shadow-lg shadow-amber-600/20 disabled:opacity-50 transition-all"
                >
                  {isManualSubmitting ? 'Injecting...' : 'Transmit Override'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 3: START SIMULATION SESSION */}
      {isStartSimModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Play className="w-5 h-5 text-emerald-400" />
                <h3 className="text-base font-bold text-slate-100">Launch Simulation Session</h3>
              </div>
              <button
                onClick={() => setIsStartSimModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleStartSimulation} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Target Facility</label>
                <select
                  value={selectedBuildingId}
                  onChange={(e) => setSelectedBuildingId(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none focus:border-emerald-500"
                >
                  {buildings.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.name} ({b.buildingCode})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">
                  Simulation Cadence (Seconds between ticks)
                </label>
                <input
                  type="number"
                  min="1"
                  max="60"
                  required
                  value={simInterval}
                  onChange={(e) => setSimInterval(parseInt(e.target.value) || 5)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 font-mono outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Initial Scenario</label>
                <select
                  value={simScenarioType}
                  onChange={(e) => setSimScenarioType(e.target.value as ScenarioType)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none focus:border-emerald-500"
                >
                  <option value="NORMAL">Normal Building Operation</option>
                  <option value="TEMPERATURE_ANOMALY">Temperature Anomaly</option>
                  <option value="SMOKE_EVENT">Smoke Detection Event</option>
                  <option value="POOR_AIR_QUALITY">Poor Air Quality (AQI)</option>
                  <option value="HIGH_OCCUPANCY">High Occupancy Surge</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsStartSimModalOpen(false)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
                >
                  Launch Engine
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 4: CREATE CUSTOM SENSOR */}
      {isCreateSensorModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <div className="flex items-center gap-2">
                <Radio className="w-5 h-5 text-emerald-400" />
                <h3 className="text-base font-bold text-slate-100">Register Custom Sensor</h3>
              </div>
              <button
                onClick={() => setIsCreateSensorModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateSensor} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Sensor Display Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. East Wing Ambient Thermometer"
                  value={newSensorPayload.sensorName}
                  onChange={(e) => setNewSensorPayload({ ...newSensorPayload, sensorName: e.target.value })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none focus:border-emerald-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Sensor Modality / Type</label>
                <select
                  value={newSensorPayload.sensorType}
                  onChange={(e) => setNewSensorPayload({ ...newSensorPayload, sensorType: e.target.value as SensorType })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 outline-none focus:border-emerald-500"
                >
                  <option value="TEMPERATURE">Temperature (°C)</option>
                  <option value="HUMIDITY">Humidity (%)</option>
                  <option value="SMOKE">Smoke Concentration (ppm)</option>
                  <option value="AIR_QUALITY">Air Quality Index (AQI)</option>
                  <option value="OCCUPANCY">Occupancy Flow (People)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Simulation Interval (Seconds)</label>
                <input
                  type="number"
                  min="1"
                  max="60"
                  value={newSensorPayload.simulationIntervalSeconds}
                  onChange={(e) => setNewSensorPayload({ ...newSensorPayload, simulationIntervalSeconds: parseInt(e.target.value) || 5 })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 rounded-lg text-xs text-slate-200 font-mono outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsCreateSensorModalOpen(false)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
                >
                  Create Endpoint
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
