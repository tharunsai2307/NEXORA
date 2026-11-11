import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  buildingApi,
  incidentApi,
  emergencyApi,
  evacuationApi,
  userApi,
} from '../services/api';
import { Building } from '../types/building';
import { User } from '../types/auth';
import {
  Incident,
  IncidentCategory,
  IncidentSeverity,
  BuildingEmergencyStatus,
  EvacuationSession,
  EvacuationRoute,
  EvacuationGraph,
  CreateIncidentPayload,
  StartEvacuationPayload,
} from '../types/emergency';
import { useAuth } from '../context/AuthContext';
import {
  AlertTriangle,
  Flame,
  ShieldAlert,
  Users,
  LogOut,
  CheckCircle2,
  ArrowRight,
  Pause,
  Play,
  RotateCcw,
  Compass,
  Network,
  Plus,
  Search,
  Filter,
  Activity,
  Boxes,
  Lock,
  Unlock,
  Building2,
  X,
  UserCheck,
  AlertOctagon,
  RefreshCw,
  Zap,
} from 'lucide-react';

export const EmergencyPage: React.FC = () => {
  const { hasRole } = useAuth();
  const navigate = useNavigate();
  const canOperate = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER', 'SAFETY_OFFICER']);

  // Building & Status State
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [emergencyStatus, setEmergencyStatus] = useState<BuildingEmergencyStatus | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [officers, setOfficers] = useState<User[]>([]);

  // Active View Tab
  const [activeTab, setActiveTab] = useState<'overview' | 'incidents' | 'evacuation' | 'routes' | 'graph'>('overview');

  // Incidents Queue State
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [filterSeverity, setFilterSeverity] = useState<string>('ALL');
  const [filterCategory, setFilterCategory] = useState<string>('ALL');
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedIncident, setSelectedIncident] = useState<Incident | null>(null);

  // Evacuation Session State
  const [activeSession, setActiveSession] = useState<EvacuationSession | null>(null);
  const [routes, setRoutes] = useState<EvacuationRoute[]>([]);
  const [graph, setGraph] = useState<EvacuationGraph | null>(null);
  const [selectedRoute, setSelectedRoute] = useState<EvacuationRoute | null>(null);

  // Modals
  const [showReportModal, setShowReportModal] = useState<boolean>(false);
  const [showStartEvacModal, setShowStartEvacModal] = useState<boolean>(false);
  const [showAssignModal, setShowAssignModal] = useState<boolean>(false);
  const [assignIncidentId, setAssignIncidentId] = useState<string | null>(null);
  const [selectedOfficerId, setSelectedOfficerId] = useState<string>('');
  const [assignNotes, setAssignNotes] = useState<string>('');

  // Report Incident Form
  const [reportTitle, setReportTitle] = useState<string>('');
  const [reportCategory, setReportCategory] = useState<IncidentCategory>('MANUAL_EMERGENCY');
  const [reportSeverity, setReportSeverity] = useState<IncidentSeverity>('HIGH');
  const [reportDescription, setReportDescription] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);

  // WebSocket
  const wsRef = useRef<WebSocket | null>(null);

  // 1. Initial Load of Buildings & Officers
  useEffect(() => {
    const init = async () => {
      try {
        const bList = await buildingApi.getBuildings();
        setBuildings(bList);
        if (bList.length > 0) {
          setSelectedBuildingId(bList[0].id);
        }

        try {
          const uList: User[] = await userApi.listUsers();
          setOfficers(uList.filter((u: User) => u.role === 'SAFETY_OFFICER' || u.role === 'BUILDING_MANAGER'));
        } catch {
          // Non-admin may not view full directory, continue gracefully
        }
      } catch (err) {
        console.error('Failed to load initial emergency data', err);
      }
    };
    init();
  }, []);

  // 2. Fetch Building Emergency Data
  const loadBuildingData = useCallback(async (bId: string) => {
    if (!bId) return;
    setLoading(true);
    try {
      const [status, incList, session, rList, gData] = await Promise.all([
        emergencyApi.getBuildingEmergencyStatus(bId),
        incidentApi.getIncidents({ buildingId: bId, size: 50 }),
        evacuationApi.getActiveSession(bId),
        evacuationApi.getBuildingRoutes(bId),
        evacuationApi.getBuildingGraph(bId),
      ]);

      setEmergencyStatus(status);
      setIncidents(incList.content || []);
      setActiveSession(session);
      setRoutes(rList || []);
      setGraph(gData);
      if (rList && rList.length > 0) {
        setSelectedRoute(rList[0]);
      }
    } catch (err) {
      console.error('Failed to load emergency status', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (selectedBuildingId) {
      loadBuildingData(selectedBuildingId);
    }
  }, [selectedBuildingId, loadBuildingData]);

  // 3. WebSocket Real-time STOMP Subscription
  useEffect(() => {
    if (!selectedBuildingId) return;

    const wsUrl = `ws://${window.location.hostname}:8080/ws-raw`;
    let ws: WebSocket;

    try {
      ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        ws.send('CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:10000,10000\n\n\0');

        const topics = [
          `/topic/buildings/${selectedBuildingId}/incidents`,
          `/topic/buildings/${selectedBuildingId}/emergency`,
          `/topic/buildings/${selectedBuildingId}/evacuation`,
          `/topic/buildings/${selectedBuildingId}/routes`,
        ];

        topics.forEach((t, i) => {
          ws.send(`SUBSCRIBE\nid:em-sub-${i}\ndestination:${t}\n\n\0`);
        });
      };

      ws.onmessage = (event) => {
        const raw = event.data;
        if (!raw || typeof raw !== 'string') return;
        const nullIdx = raw.indexOf('\0');
        const bodyStr = nullIdx !== -1 ? raw.substring(0, nullIdx) : raw;
        const bodyLines = bodyStr.split('\n\n');
        if (bodyLines.length < 2) return;

        try {
          const payload = JSON.parse(bodyLines[1]);

          // Handle Incident update
          if (payload.referenceNumber) {
            setIncidents((prev) => {
              const idx = prev.findIndex((i) => i.id === payload.id);
              if (idx !== -1) {
                const next = [...prev];
                next[idx] = payload;
                return next;
              }
              return [payload, ...prev];
            });
            // Refresh overall status
            emergencyApi.getBuildingEmergencyStatus(selectedBuildingId).then(setEmergencyStatus).catch(() => {});
          }

          // Handle Evacuation session update
          if (payload.sessionStatus && payload.roomStatuses) {
            setActiveSession(payload);
          }

          // Handle Routes update
          if (Array.isArray(payload) && payload.length > 0 && payload[0].orderedWaypoints) {
            setRoutes(payload);
          }
        } catch {}
      };
    } catch {}

    return () => {
      if (wsRef.current) {
        try {
          wsRef.current.close();
        } catch {}
      }
    };
  }, [selectedBuildingId]);

  // Actions
  const handleDeclareEmergency = async () => {
    if (!selectedBuildingId || !window.confirm('Declare general building emergency and activate egress protocol?')) return;
    try {
      const res = await emergencyApi.activateEmergency(selectedBuildingId, 'Critical Command Center Manual Declaration');
      setEmergencyStatus(res);
      await loadBuildingData(selectedBuildingId);
    } catch (e) {
      console.error('Failed to declare emergency', e);
    }
  };

  const handleStartEvacuation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBuildingId) return;
    setSubmitting(true);
    try {
      const payload: StartEvacuationPayload = {
        sessionName: `Evacuation Order - ${new Date().toLocaleTimeString()}`,
        notes: 'Initiated from Emergency Operations Workspace',
      };
      const session = await evacuationApi.startEvacuation(selectedBuildingId, payload);
      setActiveSession(session);
      setShowStartEvacModal(false);
      setActiveTab('evacuation');
      await loadBuildingData(selectedBuildingId);
    } catch (err) {
      console.error('Failed to start evacuation', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleStepSimulation = async () => {
    if (!activeSession) return;
    try {
      const updated = await evacuationApi.stepEvacuation(activeSession.id);
      setActiveSession(updated);
    } catch (err) {
      console.error('Failed to advance evacuation simulation', err);
    }
  };

  const handlePauseEvac = async () => {
    if (!activeSession) return;
    try {
      const updated = await evacuationApi.pauseEvacuation(activeSession.id);
      setActiveSession(updated);
    } catch (err) {
      console.error('Failed to pause evacuation', err);
    }
  };

  const handleResumeEvac = async () => {
    if (!activeSession) return;
    try {
      const updated = await evacuationApi.resumeEvacuation(activeSession.id);
      setActiveSession(updated);
    } catch (err) {
      console.error('Failed to resume evacuation', err);
    }
  };

  const handleCancelEvac = async () => {
    if (!activeSession || !window.confirm('Cancel active evacuation session?')) return;
    try {
      const updated = await evacuationApi.cancelEvacuation(activeSession.id);
      setActiveSession(updated);
    } catch (err) {
      console.error('Failed to cancel evacuation', err);
    }
  };

  const handleAcknowledge = async (id: string) => {
    try {
      const updated = await incidentApi.acknowledgeIncident(id);
      setIncidents((prev) => prev.map((i) => (i.id === id ? updated : i)));
      if (selectedIncident?.id === id) setSelectedIncident(updated);
    } catch (err) {
      console.error('Failed to acknowledge incident', err);
    }
  };

  const handleResolve = async (id: string) => {
    try {
      const updated = await incidentApi.resolveIncident(id, 'Resolved via Emergency Operations Center');
      setIncidents((prev) => prev.map((i) => (i.id === id ? updated : i)));
      if (selectedIncident?.id === id) setSelectedIncident(updated);
    } catch (err) {
      console.error('Failed to resolve incident', err);
    }
  };

  const handleOpenAssign = (id: string) => {
    setAssignIncidentId(id);
    setShowAssignModal(true);
  };

  const handleConfirmAssign = async () => {
    if (!assignIncidentId || !selectedOfficerId) return;
    try {
      const updated = await incidentApi.assignIncident(assignIncidentId, selectedOfficerId, assignNotes);
      setIncidents((prev) => prev.map((i) => (i.id === assignIncidentId ? updated : i)));
      if (selectedIncident?.id === assignIncidentId) setSelectedIncident(updated);
      setShowAssignModal(false);
      setAssignNotes('');
    } catch (err) {
      console.error('Failed to assign incident', err);
    }
  };

  const handleCreateIncident = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBuildingId || !reportTitle.trim()) return;
    setSubmitting(true);
    try {
      const payload: CreateIncidentPayload = {
        buildingId: selectedBuildingId,
        category: reportCategory,
        severity: reportSeverity,
        detectionSource: 'MANUAL',
        title: reportTitle.trim(),
        description: reportDescription.trim() || undefined,
      };
      const created = await incidentApi.createIncident(payload);
      setIncidents((prev) => [created, ...prev]);
      setShowReportModal(false);
      setReportTitle('');
      setReportDescription('');
    } catch (err) {
      console.error('Failed to report incident', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleConnectionBlock = async (connId: string) => {
    try {
      await evacuationApi.toggleConnectionBlock(connId);
      // Reload graph and routes
      const [gData, rList] = await Promise.all([
        evacuationApi.getBuildingGraph(selectedBuildingId),
        evacuationApi.getBuildingRoutes(selectedBuildingId),
      ]);
      setGraph(gData);
      setRoutes(rList);
    } catch (err) {
      console.error('Failed to toggle connection block', err);
    }
  };

  // Filtered Incidents
  const filteredIncidents = incidents.filter((inc) => {
    if (filterSeverity !== 'ALL' && inc.severity !== filterSeverity) return false;
    if (filterCategory !== 'ALL' && inc.category !== filterCategory) return false;
    if (filterStatus !== 'ALL' && inc.status !== filterStatus) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      const matchTitle = inc.title.toLowerCase().includes(q);
      const matchRef = inc.referenceNumber.toLowerCase().includes(q);
      const matchRoom = inc.roomName?.toLowerCase().includes(q);
      if (!matchTitle && !matchRef && !matchRoom) return false;
    }
    return true;
  });

  return (
    <div className="flex flex-col h-full bg-[#080d16] text-slate-200 overflow-hidden">
      {/* Top Threat Ribbon & Building Selector */}
      <div className="px-6 py-4 bg-[#0c1320] border-b border-[#1a2538] flex flex-wrap items-center justify-between gap-4 shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-red-950/60 border border-red-500/50 flex items-center justify-center text-red-400 shadow-lg shadow-red-950/40">
            <ShieldAlert className="w-5 h-5 animate-pulse" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-lg font-bold text-slate-100 tracking-wide">Emergency Operations Center</h1>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-red-950 text-red-400 border border-red-800">
                Phase 6 Core
              </span>
            </div>
            <p className="text-xs text-slate-400">Incident command, multi-exit Dijkstra egress routing, and simulated evacuation</p>
          </div>
        </div>

        {/* Global Action Hub */}
        <div className="flex items-center gap-3">
          {/* Building Selector */}
          <div className="flex items-center gap-2 bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-1.5 text-xs">
            <Building2 className="w-4 h-4 text-emerald-400" />
            <select
              value={selectedBuildingId}
              onChange={(e) => setSelectedBuildingId(e.target.value)}
              className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
            >
              {buildings.map((b) => (
                <option key={b.id} value={b.id} className="bg-[#121c2d]">
                  {b.name} ({b.buildingCode})
                </option>
              ))}
            </select>
          </div>

          {/* Quick Action: Digital Twin 3D View */}
          <button
            onClick={() => navigate('/digital-twin')}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-sky-950/60 border border-sky-600/50 hover:bg-sky-900/60 text-sky-300 text-xs font-medium transition"
          >
            <Boxes className="w-3.5 h-3.5 text-sky-400" />
            <span>3D Digital Twin</span>
          </button>

          {/* Quick Action: Report Incident */}
          <button
            onClick={() => setShowReportModal(true)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-[#182337] border border-[#2a3c5a] hover:bg-[#1e2d47] text-slate-200 text-xs font-medium transition"
          >
            <Plus className="w-3.5 h-3.5 text-emerald-400" />
            <span>Log Incident</span>
          </button>

          {/* Critical Emergency Trigger */}
          {canOperate && (
            <button
              onClick={handleDeclareEmergency}
              className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg bg-red-600 hover:bg-red-500 text-white text-xs font-bold transition shadow-lg shadow-red-950/50 animate-pulse"
            >
              <AlertTriangle className="w-3.5 h-3.5" />
              <span>DECLARE EMERGENCY</span>
            </button>
          )}

          <button
            onClick={() => loadBuildingData(selectedBuildingId)}
            className="p-1.5 rounded-lg bg-[#121c2d] border border-[#1e2e47] hover:bg-[#1a273e] text-slate-400 hover:text-slate-200 transition"
            title="Refresh Operations"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </div>

      {/* Real-time KPI Status Ribbon */}
      <div className="grid grid-cols-2 md:grid-cols-6 gap-3 px-6 py-3 bg-[#0a101b] border-b border-[#1a2538] text-xs">
        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Building Threat</div>
            <div className="text-sm font-bold flex items-center gap-1.5 mt-0.5">
              <span className={`w-2 h-2 rounded-full ${emergencyStatus?.emergencyActive ? 'bg-red-500 animate-ping' : 'bg-emerald-500'}`} />
              <span className={emergencyStatus?.emergencyActive ? 'text-red-400' : 'text-emerald-400'}>
                {emergencyStatus?.emergencyActive ? 'ACTIVE ALARM' : 'NORMAL SECURE'}
              </span>
            </div>
          </div>
          <AlertOctagon className={`w-5 h-5 ${emergencyStatus?.emergencyActive ? 'text-red-400' : 'text-slate-600'}`} />
        </div>

        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Active Incidents</div>
            <div className="text-sm font-bold text-slate-100 mt-0.5">
              {emergencyStatus?.activeIncidentCount ?? 0}
              {emergencyStatus && emergencyStatus.criticalIncidentCount > 0 && (
                <span className="text-xs text-red-400 ml-1.5 font-normal">({emergencyStatus.criticalIncidentCount} Critical)</span>
              )}
            </div>
          </div>
          <Flame className="w-5 h-5 text-amber-400" />
        </div>

        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Evacuation Session</div>
            <div className="text-sm font-bold text-slate-100 mt-0.5">
              {activeSession ? (
                <span className="text-amber-400 flex items-center gap-1">
                  <Activity className="w-3.5 h-3.5 animate-pulse" />
                  {activeSession.sessionStatus}
                </span>
              ) : (
                <span className="text-slate-400">STANDBY</span>
              )}
            </div>
          </div>
          <Zap className="w-5 h-5 text-sky-400" />
        </div>

        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Occupants At Risk</div>
            <div className="text-sm font-bold text-slate-100 mt-0.5">
              {emergencyStatus?.totalOccupantsAtRisk ?? 0} <span className="text-[10px] text-slate-400 font-normal">people</span>
            </div>
          </div>
          <Users className="w-5 h-5 text-orange-400" />
        </div>

        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Rooms Cleared</div>
            <div className="text-sm font-bold text-emerald-400 mt-0.5">
              {emergencyStatus?.totalRoomsCleared ?? 0} / {(emergencyStatus?.totalRoomsCleared ?? 0) + (emergencyStatus?.totalRoomsEvacuating ?? 0)}
            </div>
          </div>
          <CheckCircle2 className="w-5 h-5 text-emerald-400" />
        </div>

        <div className="p-2.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
          <div>
            <div className="text-[10px] text-slate-400 uppercase font-mono">Egress Safe Exits</div>
            <div className="text-sm font-bold text-cyan-400 mt-0.5">
              {emergencyStatus?.safeExitsCount ?? 0} Available
            </div>
          </div>
          <LogOut className="w-5 h-5 text-cyan-400" />
        </div>
      </div>

      {/* Navigation Sub-Tabs */}
      <div className="px-6 border-b border-[#1a2538] bg-[#0c1320] flex items-center gap-6 text-xs">
        <button
          onClick={() => setActiveTab('overview')}
          className={`py-3 font-medium border-b-2 transition flex items-center gap-1.5 ${
            activeTab === 'overview'
              ? 'border-emerald-400 text-emerald-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Activity className="w-3.5 h-3.5" />
          <span>Incident Command Hub</span>
        </button>

        <button
          onClick={() => setActiveTab('incidents')}
          className={`py-3 font-medium border-b-2 transition flex items-center gap-1.5 ${
            activeTab === 'incidents'
              ? 'border-emerald-400 text-emerald-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Flame className="w-3.5 h-3.5" />
          <span>Incident Queue ({incidents.filter((i) => i.status !== 'RESOLVED' && i.status !== 'CLOSED').length})</span>
        </button>

        <button
          onClick={() => setActiveTab('evacuation')}
          className={`py-3 font-medium border-b-2 transition flex items-center gap-1.5 ${
            activeTab === 'evacuation'
              ? 'border-emerald-400 text-emerald-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Users className="w-3.5 h-3.5" />
          <span>Evacuation Management {activeSession && <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse ml-1" />}</span>
        </button>

        <button
          onClick={() => setActiveTab('routes')}
          className={`py-3 font-medium border-b-2 transition flex items-center gap-1.5 ${
            activeTab === 'routes'
              ? 'border-emerald-400 text-emerald-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Compass className="w-3.5 h-3.5" />
          <span>Dynamic Egress Routes ({routes.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('graph')}
          className={`py-3 font-medium border-b-2 transition flex items-center gap-1.5 ${
            activeTab === 'graph'
              ? 'border-emerald-400 text-emerald-400 font-semibold'
              : 'border-transparent text-slate-400 hover:text-slate-200'
          }`}
        >
          <Network className="w-3.5 h-3.5" />
          <span>Egress Graph & Hazards</span>
        </button>
      </div>

      {/* Main Tab Content */}
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {/* TAB 1: OVERVIEW & COMMAND HUB */}
        {activeTab === 'overview' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left 2 Cols: Active Emergency Situation & Active Evacuation Banner */}
            <div className="lg:col-span-2 space-y-6">
              {/* Active Evacuation Banner if running */}
              {activeSession ? (
                <div className="p-5 rounded-2xl bg-gradient-to-r from-amber-950/60 to-[#0e1726] border border-amber-500/40 shadow-xl space-y-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/50 flex items-center justify-center text-amber-400">
                        <Activity className="w-5 h-5 animate-pulse" />
                      </div>
                      <div>
                        <h2 className="text-base font-bold text-amber-300">{activeSession.sessionName}</h2>
                        <p className="text-xs text-slate-400">
                          Initiated by {activeSession.initiatedBy} • Status: <span className="font-mono text-amber-400 font-bold">{activeSession.sessionStatus}</span>
                        </p>
                      </div>
                    </div>

                    {/* Simulation Controls */}
                    <div className="flex items-center gap-2">
                      <button
                        onClick={handleStepSimulation}
                        className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium flex items-center gap-1.5 transition shadow"
                      >
                        <Play className="w-3.5 h-3.5" />
                        <span>Simulate Step</span>
                      </button>

                      {activeSession.sessionStatus === 'ACTIVE' ? (
                        <button
                          onClick={handlePauseEvac}
                          className="px-3 py-1.5 rounded-lg bg-amber-700 hover:bg-amber-600 text-white text-xs font-medium flex items-center gap-1.5 transition"
                        >
                          <Pause className="w-3.5 h-3.5" />
                          <span>Pause</span>
                        </button>
                      ) : (
                        <button
                          onClick={handleResumeEvac}
                          className="px-3 py-1.5 rounded-lg bg-sky-700 hover:bg-sky-600 text-white text-xs font-medium flex items-center gap-1.5 transition"
                        >
                          <Play className="w-3.5 h-3.5" />
                          <span>Resume</span>
                        </button>
                      )}

                      <button
                        onClick={handleCancelEvac}
                        className="px-2.5 py-1.5 rounded-lg bg-slate-800 hover:bg-red-950 text-slate-300 hover:text-red-400 border border-slate-700 text-xs transition"
                        title="Cancel Evacuation"
                      >
                        <RotateCcw className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div>
                    <div className="flex justify-between text-xs mb-1.5">
                      <span className="text-slate-400">Evacuation Clearance Progress</span>
                      <span className="font-mono text-amber-400 font-bold">{activeSession.progressPercentage}% Complete</span>
                    </div>
                    <div className="w-full bg-[#141e30] h-3 rounded-full overflow-hidden border border-[#233550]">
                      <div
                        className="bg-gradient-to-r from-amber-500 to-emerald-400 h-full transition-all duration-500 rounded-full"
                        style={{ width: `${Math.min(100, activeSession.progressPercentage)}%` }}
                      />
                    </div>
                  </div>

                  {/* Evacuation Quick Stats */}
                  <div className="grid grid-cols-4 gap-3 text-center text-xs pt-1 font-mono">
                    <div className="p-2 rounded-lg bg-[#0d1522] border border-[#1e2e47]">
                      <div className="text-[10px] text-slate-400">Total Rooms</div>
                      <div className="text-sm font-bold text-slate-200 mt-0.5">{activeSession.totalRooms}</div>
                    </div>
                    <div className="p-2 rounded-lg bg-[#0d1522] border border-[#1e2e47]">
                      <div className="text-[10px] text-slate-400">Cleared</div>
                      <div className="text-sm font-bold text-emerald-400 mt-0.5">{activeSession.clearedRooms}</div>
                    </div>
                    <div className="p-2 rounded-lg bg-[#0d1522] border border-[#1e2e47]">
                      <div className="text-[10px] text-slate-400">Evacuating</div>
                      <div className="text-sm font-bold text-amber-400 mt-0.5">{activeSession.evacuatingRooms}</div>
                    </div>
                    <div className="p-2 rounded-lg bg-[#0d1522] border border-[#1e2e47]">
                      <div className="text-[10px] text-slate-400">Remaining Occupants</div>
                      <div className="text-sm font-bold text-rose-400 mt-0.5">{activeSession.remainingOccupants}</div>
                    </div>
                  </div>
                </div>
              ) : (
                /* No Active Evacuation Banner */
                <div className="p-6 rounded-2xl bg-[#0c1320] border border-[#1a2538] flex items-center justify-between">
                  <div className="space-y-1">
                    <h2 className="text-base font-bold text-slate-200">No Active Evacuation Session</h2>
                    <p className="text-xs text-slate-400">
                      Building egress protocol is on standby. You can initiate a controlled evacuation or simulation anytime.
                    </p>
                  </div>
                  {canOperate && (
                    <button
                      onClick={() => setShowStartEvacModal(true)}
                      className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition flex items-center gap-1.5 shadow"
                    >
                      <Zap className="w-4 h-4" />
                      <span>Start Building Evacuation</span>
                    </button>
                  )}
                </div>
              )}

              {/* Active Threat Incidents List */}
              <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Flame className="w-4 h-4 text-red-400" />
                    <h3 className="text-sm font-bold text-slate-200">Current Active Hazards & Incidents</h3>
                  </div>
                  <button
                    onClick={() => setActiveTab('incidents')}
                    className="text-xs text-emerald-400 hover:underline flex items-center gap-1"
                  >
                    <span>View all ({incidents.length})</span>
                    <ArrowRight className="w-3 h-3" />
                  </button>
                </div>

                {incidents.filter((i) => i.status !== 'RESOLVED' && i.status !== 'CLOSED').length === 0 ? (
                  <div className="p-8 text-center text-slate-400 text-xs bg-[#080d16] rounded-xl border border-dashed border-[#1a2538]">
                    <CheckCircle2 className="w-8 h-8 text-emerald-400 mx-auto mb-2 opacity-80" />
                    <span>No unaddressed hazards detected in this building. All zones reporting nominal safety metrics.</span>
                  </div>
                ) : (
                  <div className="space-y-2.5">
                    {incidents
                      .filter((i) => i.status !== 'RESOLVED' && i.status !== 'CLOSED')
                      .slice(0, 4)
                      .map((inc) => (
                        <div
                          key={inc.id}
                          className="p-3.5 rounded-xl bg-[#0e1624] border border-[#1e2e47] hover:border-[#2a3f63] flex items-center justify-between gap-4 transition cursor-pointer"
                          onClick={() => {
                            setSelectedIncident(inc);
                            setActiveTab('incidents');
                          }}
                        >
                          <div className="flex items-center gap-3">
                            <span
                              className={`w-2.5 h-2.5 rounded-full ${
                                inc.severity === 'CRITICAL'
                                  ? 'bg-red-500 animate-ping'
                                  : inc.severity === 'HIGH'
                                  ? 'bg-orange-500'
                                  : 'bg-yellow-500'
                              }`}
                            />
                            <div>
                              <div className="flex items-center gap-2">
                                <span className="font-mono text-[10px] text-slate-400">{inc.referenceNumber}</span>
                                <span className="text-xs font-bold text-slate-200">{inc.title}</span>
                              </div>
                              <div className="text-[11px] text-slate-400 flex items-center gap-2 mt-0.5">
                                <span>{inc.roomName || 'General Building'}</span>
                                <span>•</span>
                                <span>Level {inc.floorNumber ?? 0}</span>
                                <span>•</span>
                                <span className="font-mono">{inc.category}</span>
                              </div>
                            </div>
                          </div>

                          <div className="flex items-center gap-2">
                            <span
                              className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                                inc.status === 'DETECTED'
                                  ? 'bg-red-950/80 text-red-400 border-red-700'
                                  : inc.status === 'ACKNOWLEDGED'
                                  ? 'bg-amber-950/80 text-amber-400 border-amber-700'
                                  : 'bg-sky-950/80 text-sky-400 border-sky-700'
                              }`}
                            >
                              {inc.status}
                            </span>
                          </div>
                        </div>
                      ))}
                  </div>
                )}
              </div>
            </div>

            {/* Right Col: First Responder & Egress Summary Panel */}
            <div className="space-y-6">
              {/* Egress Safety Summary */}
              <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                <div className="flex items-center gap-2">
                  <Compass className="w-4 h-4 text-cyan-400" />
                  <h3 className="text-sm font-bold text-slate-200">Egress Safety Readiness</h3>
                </div>

                <div className="space-y-3 text-xs">
                  <div className="p-3 rounded-xl bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
                    <span className="text-slate-400">Safe Exits Active</span>
                    <span className="font-mono text-emerald-400 font-bold">{emergencyStatus?.safeExitsCount ?? 0} Exits</span>
                  </div>
                  <div className="p-3 rounded-xl bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
                    <span className="text-slate-400">Blocked / Hazardous Paths</span>
                    <span className="font-mono text-rose-400 font-bold">{emergencyStatus?.blockedRoutesCount ?? 0} Blocked</span>
                  </div>
                  <div className="p-3 rounded-xl bg-[#0e1624] border border-[#1e2e47] flex items-center justify-between">
                    <span className="text-slate-400">Algorithm Engine</span>
                    <span className="font-mono text-sky-400 font-bold">Dynamic Dijkstra (A*)</span>
                  </div>
                </div>

                <button
                  onClick={() => setActiveTab('routes')}
                  className="w-full py-2 rounded-lg bg-[#141f33] hover:bg-[#1a2942] border border-[#233550] text-cyan-300 text-xs font-medium transition flex items-center justify-center gap-1.5"
                >
                  <Compass className="w-3.5 h-3.5" />
                  <span>Inspect Room Egress Paths</span>
                </button>
              </div>

              {/* Digital Twin Shortcut Card */}
              <div className="p-5 rounded-2xl bg-gradient-to-b from-[#0c1320] to-[#09101d] border border-[#1a2538] space-y-3">
                <div className="flex items-center gap-2">
                  <Boxes className="w-4 h-4 text-emerald-400" />
                  <h3 className="text-sm font-bold text-slate-200">Interactive 3D Digital Twin</h3>
                </div>
                <p className="text-xs text-slate-400 leading-relaxed">
                  Switch directly into the 3D building twin to visualize active incident beacons, cleared rooms, and animated egress tubes in real-time.
                </p>
                <button
                  onClick={() => navigate('/digital-twin')}
                  className="w-full py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition flex items-center justify-center gap-1.5 shadow"
                >
                  <Boxes className="w-4 h-4" />
                  <span>Launch 3D Emergency Twin</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: INCIDENT MANAGEMENT QUEUE */}
        {activeTab === 'incidents' && (
          <div className="space-y-4">
            {/* Filter & Search Bar */}
            <div className="p-4 rounded-xl bg-[#0c1320] border border-[#1a2538] flex flex-wrap items-center justify-between gap-3 text-xs">
              <div className="flex flex-wrap items-center gap-3">
                {/* Search */}
                <div className="relative w-64">
                  <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-2.5" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Search reference, room, title..."
                    className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg pl-8 pr-3 py-1.5 text-slate-200 focus:outline-none focus:border-emerald-500"
                  />
                </div>

                {/* Severity Filter */}
                <div className="flex items-center gap-1 bg-[#121c2d] border border-[#1e2e47] rounded-lg px-2.5 py-1.5">
                  <Filter className="w-3.5 h-3.5 text-slate-400" />
                  <select
                    value={filterSeverity}
                    onChange={(e) => setFilterSeverity(e.target.value)}
                    className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
                  >
                    <option value="ALL" className="bg-[#121c2d]">All Severities</option>
                    <option value="CRITICAL" className="bg-[#121c2d]">Critical</option>
                    <option value="HIGH" className="bg-[#121c2d]">High</option>
                    <option value="MEDIUM" className="bg-[#121c2d]">Medium</option>
                    <option value="LOW" className="bg-[#121c2d]">Low</option>
                  </select>
                </div>

                {/* Category Filter */}
                <div className="flex items-center gap-1 bg-[#121c2d] border border-[#1e2e47] rounded-lg px-2.5 py-1.5">
                  <select
                    value={filterCategory}
                    onChange={(e) => setFilterCategory(e.target.value)}
                    className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
                  >
                    <option value="ALL" className="bg-[#121c2d]">All Categories</option>
                    <option value="FIRE" className="bg-[#121c2d]">Fire</option>
                    <option value="SMOKE" className="bg-[#121c2d]">Smoke</option>
                    <option value="HIGH_TEMPERATURE" className="bg-[#121c2d]">High Temp</option>
                    <option value="POOR_AIR_QUALITY" className="bg-[#121c2d]">Air Quality</option>
                    <option value="OVERCROWDING" className="bg-[#121c2d]">Overcrowding</option>
                    <option value="MANUAL_EMERGENCY" className="bg-[#121c2d]">Manual Emergency</option>
                  </select>
                </div>

                {/* Status Filter */}
                <div className="flex items-center gap-1 bg-[#121c2d] border border-[#1e2e47] rounded-lg px-2.5 py-1.5">
                  <select
                    value={filterStatus}
                    onChange={(e) => setFilterStatus(e.target.value)}
                    className="bg-transparent text-slate-200 focus:outline-none cursor-pointer"
                  >
                    <option value="ALL" className="bg-[#121c2d]">All Statuses</option>
                    <option value="DETECTED" className="bg-[#121c2d]">Detected</option>
                    <option value="ACKNOWLEDGED" className="bg-[#121c2d]">Acknowledged</option>
                    <option value="RESPONDING" className="bg-[#121c2d]">Responding</option>
                    <option value="RESOLVED" className="bg-[#121c2d]">Resolved</option>
                    <option value="CLOSED" className="bg-[#121c2d]">Closed</option>
                  </select>
                </div>
              </div>

              <div className="text-slate-400 font-mono">
                Showing {filteredIncidents.length} of {incidents.length} incidents
              </div>
            </div>

            {/* Incidents Table */}
            <div className="rounded-xl border border-[#1a2538] overflow-hidden bg-[#0c1320]">
              <table className="w-full text-left text-xs">
                <thead className="bg-[#0e1624] text-slate-400 uppercase font-mono text-[10px] border-b border-[#1a2538]">
                  <tr>
                    <th className="py-3 px-4">Ref & Title</th>
                    <th className="py-3 px-4">Category</th>
                    <th className="py-3 px-4">Severity</th>
                    <th className="py-3 px-4">Location</th>
                    <th className="py-3 px-4">Source</th>
                    <th className="py-3 px-4">Status</th>
                    <th className="py-3 px-4">Assigned Officer</th>
                    <th className="py-3 px-4 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#1a2538]">
                  {filteredIncidents.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="py-8 text-center text-slate-400">
                        No incidents match the selected filters.
                      </td>
                    </tr>
                  ) : (
                    filteredIncidents.map((inc) => (
                      <tr
                        key={inc.id}
                        className="hover:bg-[#121c2d]/60 transition cursor-pointer"
                        onClick={() => setSelectedIncident(inc)}
                      >
                        <td className="py-3 px-4">
                          <div className="font-mono text-[10px] text-slate-400">{inc.referenceNumber}</div>
                          <div className="font-semibold text-slate-200 mt-0.5">{inc.title}</div>
                        </td>
                        <td className="py-3 px-4">
                          <span className="font-mono text-[11px] text-slate-300">{inc.category}</span>
                        </td>
                        <td className="py-3 px-4">
                          <span
                            className={`px-2 py-0.5 rounded-full font-mono text-[10px] border ${
                              inc.severity === 'CRITICAL'
                                ? 'bg-red-950 text-red-400 border-red-700'
                                : inc.severity === 'HIGH'
                                ? 'bg-orange-950 text-orange-400 border-orange-700'
                                : inc.severity === 'MEDIUM'
                                ? 'bg-amber-950 text-amber-400 border-amber-700'
                                : 'bg-slate-900 text-slate-400 border-slate-700'
                            }`}
                          >
                            {inc.severity}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-slate-300">
                          {inc.roomName ? `${inc.roomName} (L${inc.floorNumber})` : 'Building-Wide'}
                        </td>
                        <td className="py-3 px-4">
                          <span className="text-[10px] font-mono text-slate-400">{inc.detectionSource}</span>
                        </td>
                        <td className="py-3 px-4">
                          <span
                            className={`px-2 py-0.5 rounded-full font-mono text-[10px] border ${
                              inc.status === 'DETECTED'
                                ? 'bg-red-950/70 text-red-300 border-red-800'
                                : inc.status === 'ACKNOWLEDGED'
                                ? 'bg-amber-950/70 text-amber-300 border-amber-800'
                                : inc.status === 'RESPONDING'
                                ? 'bg-sky-950/70 text-sky-300 border-sky-800'
                                : inc.status === 'RESOLVED'
                                ? 'bg-emerald-950/70 text-emerald-300 border-emerald-800'
                                : 'bg-slate-900 text-slate-400 border-slate-800'
                            }`}
                          >
                            {inc.status}
                          </span>
                        </td>
                        <td className="py-3 px-4 text-slate-400">
                          {inc.assignedOfficerName || <span className="italic text-slate-500">Unassigned</span>}
                        </td>
                        <td className="py-3 px-4 text-right" onClick={(e) => e.stopPropagation()}>
                          <div className="flex items-center justify-end gap-1.5">
                            {inc.status === 'DETECTED' && canOperate && (
                              <button
                                onClick={() => handleAcknowledge(inc.id)}
                                className="px-2 py-1 rounded bg-amber-950/80 hover:bg-amber-900 text-amber-300 border border-amber-700 text-[10px] transition"
                              >
                                Acknowledge
                              </button>
                            )}

                            {!inc.assignedOfficerId && canOperate && inc.status !== 'RESOLVED' && inc.status !== 'CLOSED' && (
                              <button
                                onClick={() => handleOpenAssign(inc.id)}
                                className="px-2 py-1 rounded bg-[#18253a] hover:bg-[#223552] text-slate-200 border border-[#2d4263] text-[10px] transition"
                              >
                                Assign
                              </button>
                            )}

                            {inc.status !== 'RESOLVED' && inc.status !== 'CLOSED' && canOperate && (
                              <button
                                onClick={() => handleResolve(inc.id)}
                                className="px-2 py-1 rounded bg-emerald-950/80 hover:bg-emerald-900 text-emerald-300 border border-emerald-700 text-[10px] transition"
                              >
                                Resolve
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Selected Incident Drawer */}
            {selectedIncident && (
              <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                <div className="flex items-center justify-between border-b border-[#1a2538] pb-3">
                  <div>
                    <div className="font-mono text-xs text-slate-400">{selectedIncident.referenceNumber}</div>
                    <h3 className="text-base font-bold text-slate-100">{selectedIncident.title}</h3>
                  </div>
                  <button
                    onClick={() => setSelectedIncident(null)}
                    className="p-1.5 rounded-lg bg-[#141e2e] text-slate-400 hover:text-slate-200"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>

                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs font-mono">
                  <div className="p-3 rounded-lg bg-[#0e1624] border border-[#1e2e47]">
                    <div className="text-[10px] text-slate-400 uppercase">Category</div>
                    <div className="text-slate-200 font-bold mt-0.5">{selectedIncident.category}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-[#0e1624] border border-[#1e2e47]">
                    <div className="text-[10px] text-slate-400 uppercase">Severity</div>
                    <div className="text-slate-200 font-bold mt-0.5">{selectedIncident.severity}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-[#0e1624] border border-[#1e2e47]">
                    <div className="text-[10px] text-slate-400 uppercase">Status</div>
                    <div className="text-slate-200 font-bold mt-0.5">{selectedIncident.status}</div>
                  </div>
                  <div className="p-3 rounded-lg bg-[#0e1624] border border-[#1e2e47]">
                    <div className="text-[10px] text-slate-400 uppercase">Detected At</div>
                    <div className="text-slate-200 mt-0.5">{new Date(selectedIncident.detectedAt).toLocaleString()}</div>
                  </div>
                </div>

                {selectedIncident.description && (
                  <div className="p-3.5 rounded-lg bg-[#0e1624] border border-[#1e2e47] text-xs text-slate-300">
                    <div className="font-mono text-[10px] text-slate-400 uppercase mb-1">Incident Report Details</div>
                    <p>{selectedIncident.description}</p>
                  </div>
                )}

                {/* Activity History */}
                <div>
                  <h4 className="text-xs font-bold text-slate-300 mb-2 font-mono uppercase">Activity Log</h4>
                  <div className="space-y-2 max-h-48 overflow-y-auto">
                    {selectedIncident.activities && selectedIncident.activities.length > 0 ? (
                      selectedIncident.activities.map((act) => (
                        <div key={act.id} className="p-2.5 rounded-lg bg-[#09101b] border border-[#162235] text-xs flex items-center justify-between">
                          <div>
                            <span className="font-mono text-emerald-400 font-semibold">{act.action}</span>
                            <span className="text-slate-300 ml-2">{act.details}</span>
                          </div>
                          <div className="text-[10px] text-slate-500 font-mono">
                            {act.performedByName || act.performedBy} • {new Date(act.createdAt).toLocaleTimeString()}
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-slate-500 text-xs italic">No activity recorded yet.</div>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* TAB 3: EVACUATION MANAGEMENT */}
        {activeTab === 'evacuation' && (
          <div className="space-y-6">
            {activeSession ? (
              <>
                {/* Active Session Control Panel */}
                <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                  <div className="flex flex-wrap items-center justify-between gap-4">
                    <div>
                      <div className="text-xs text-slate-400 uppercase font-mono">Session ID: {activeSession.id}</div>
                      <h2 className="text-lg font-bold text-slate-100 mt-0.5">{activeSession.sessionName}</h2>
                      <p className="text-xs text-slate-400">
                        Started at {new Date(activeSession.startedAt || '').toLocaleTimeString()} by {activeSession.initiatedBy}
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      <button
                        onClick={handleStepSimulation}
                        className="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold flex items-center gap-1.5 transition shadow"
                      >
                        <Play className="w-4 h-4" />
                        <span>Simulate Evacuation Step</span>
                      </button>

                      {activeSession.sessionStatus === 'ACTIVE' ? (
                        <button
                          onClick={handlePauseEvac}
                          className="px-3.5 py-2 rounded-lg bg-amber-700 hover:bg-amber-600 text-white text-xs font-medium flex items-center gap-1.5 transition"
                        >
                          <Pause className="w-4 h-4" />
                          <span>Pause Session</span>
                        </button>
                      ) : (
                        <button
                          onClick={handleResumeEvac}
                          className="px-3.5 py-2 rounded-lg bg-sky-700 hover:bg-sky-600 text-white text-xs font-medium flex items-center gap-1.5 transition"
                        >
                          <Play className="w-4 h-4" />
                          <span>Resume Session</span>
                        </button>
                      )}

                      <button
                        onClick={handleCancelEvac}
                        className="px-3.5 py-2 rounded-lg bg-red-950 hover:bg-red-900 border border-red-700 text-red-300 text-xs font-medium transition"
                      >
                        Cancel Session
                      </button>
                    </div>
                  </div>

                  {/* Progress Indicator */}
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-400">Clearance Status</span>
                      <span className="font-mono text-emerald-400 font-bold">{activeSession.progressPercentage}%</span>
                    </div>
                    <div className="w-full bg-[#141e30] h-3.5 rounded-full overflow-hidden border border-[#233550]">
                      <div
                        className="bg-gradient-to-r from-amber-500 via-sky-400 to-emerald-400 h-full transition-all duration-500"
                        style={{ width: `${Math.min(100, activeSession.progressPercentage)}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Room-by-Room Evacuation Status Grid */}
                <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                  <h3 className="text-sm font-bold text-slate-200">Room-by-Room Evacuation Status Grid</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {activeSession.roomStatuses && activeSession.roomStatuses.length > 0 ? (
                      activeSession.roomStatuses.map((rs) => (
                        <div
                          key={rs.id}
                          className={`p-4 rounded-xl border transition ${
                            rs.status === 'CLEARED'
                              ? 'bg-[#091512] border-emerald-500/40'
                              : rs.status === 'NEEDS_ASSISTANCE'
                              ? 'bg-[#1a0f24] border-fuchsia-500/50'
                              : rs.status === 'BLOCKED'
                              ? 'bg-[#1f0a0e] border-red-500/50'
                              : 'bg-[#0e1624] border-[#1e2e47]'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <span className="font-mono text-[10px] text-slate-400">{rs.roomCode}</span>
                            <span
                              className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                                rs.status === 'CLEARED'
                                  ? 'bg-emerald-950 text-emerald-400 border-emerald-700'
                                  : rs.status === 'NEEDS_ASSISTANCE'
                                  ? 'bg-fuchsia-950 text-fuchsia-400 border-fuchsia-700 animate-pulse'
                                  : rs.status === 'BLOCKED'
                                  ? 'bg-red-950 text-red-400 border-red-700'
                                  : 'bg-amber-950 text-amber-400 border-amber-700'
                              }`}
                            >
                              {rs.status}
                            </span>
                          </div>

                          <div className="text-sm font-bold text-slate-100 mt-1">{rs.roomName}</div>
                          <div className="text-xs text-slate-400 mt-0.5">Floor Level {rs.floorNumber}</div>

                          <div className="mt-3 pt-3 border-t border-[#1a2538] grid grid-cols-2 gap-2 text-xs font-mono">
                            <div>
                              <span className="text-[10px] text-slate-400">Remaining Occupants</span>
                              <div className="text-sm font-bold text-slate-200">{rs.remainingOccupants} / {rs.initialOccupancy}</div>
                            </div>
                            <div>
                              <span className="text-[10px] text-slate-400">Assistance Flag</span>
                              <div className={`text-sm font-bold ${rs.occupantsNeedingAssistance > 0 ? 'text-fuchsia-400' : 'text-slate-500'}`}>
                                {rs.occupantsNeedingAssistance > 0 ? `${rs.occupantsNeedingAssistance} Persons` : 'None'}
                              </div>
                            </div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="col-span-3 text-center py-6 text-slate-400 text-xs">
                        No room clearance tracking data available.
                      </div>
                    )}
                  </div>
                </div>
              </>
            ) : (
              <div className="p-12 text-center rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
                <Users className="w-12 h-12 text-slate-500 mx-auto" />
                <h3 className="text-base font-bold text-slate-200">No Evacuation Session Active</h3>
                <p className="text-xs text-slate-400 max-w-md mx-auto">
                  To monitor room clearance, simulated occupant movement, and special assistance flags, initiate an evacuation session.
                </p>
                {canOperate && (
                  <button
                    onClick={() => setShowStartEvacModal(true)}
                    className="px-4 py-2.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition inline-flex items-center gap-2 shadow"
                  >
                    <Play className="w-4 h-4" />
                    <span>Launch Evacuation Session</span>
                  </button>
                )}
              </div>
            )}
          </div>
        )}

        {/* TAB 4: DYNAMIC EGRESS ROUTES */}
        {activeTab === 'routes' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Route List */}
            <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Compass className="w-4 h-4 text-cyan-400" />
                  <h3 className="text-sm font-bold text-slate-200">Calculated Egress Routes</h3>
                </div>
                {canOperate && (
                  <button
                    onClick={async () => {
                      if (!selectedBuildingId) return;
                      const rList = await evacuationApi.recalculateRoutes(selectedBuildingId);
                      setRoutes(rList);
                    }}
                    className="text-[10px] text-cyan-400 hover:underline flex items-center gap-1 font-mono"
                  >
                    <RotateCcw className="w-3 h-3" />
                    <span>Recalculate All</span>
                  </button>
                )}
              </div>

              <div className="space-y-2 max-h-[600px] overflow-y-auto pr-1">
                {routes.map((rt) => (
                  <div
                    key={rt.id}
                    onClick={() => setSelectedRoute(rt)}
                    className={`p-3.5 rounded-xl border transition cursor-pointer ${
                      selectedRoute?.id === rt.id
                        ? 'bg-[#122238] border-cyan-500/60'
                        : 'bg-[#0e1624] border-[#1e2e47] hover:border-[#2a3f63]'
                    }`}
                  >
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-bold text-slate-200">{rt.originRoomName}</span>
                      <span
                        className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                          rt.routeStatus === 'SAFE'
                            ? 'bg-emerald-950 text-emerald-400 border-emerald-700'
                            : rt.routeStatus === 'CAUTION'
                            ? 'bg-amber-950 text-amber-400 border-amber-700'
                            : 'bg-red-950 text-red-400 border-red-700'
                        }`}
                      >
                        {rt.routeStatus}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-[11px] text-slate-400 mt-2">
                      <span className="flex items-center gap-1">
                        <LogOut className="w-3 h-3 text-emerald-400" />
                        {rt.destinationExitName || 'NO EXIT AVAILABLE'}
                      </span>
                      <span className="font-mono text-cyan-400">{rt.totalDistanceMeters} m</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Route Step-by-Step Waypoint Breadcrumbs */}
            <div className="lg:col-span-2 p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-5">
              {selectedRoute ? (
                <>
                  <div className="flex items-center justify-between border-b border-[#1a2538] pb-4">
                    <div>
                      <div className="text-[10px] text-slate-400 uppercase font-mono">Origin Room</div>
                      <h3 className="text-base font-bold text-slate-100">{selectedRoute.originRoomName}</h3>
                      <p className="text-xs text-slate-400 mt-0.5">
                        Target Exit: <span className="text-emerald-400 font-semibold">{selectedRoute.destinationExitName}</span> • Egress Distance: <span className="font-mono text-cyan-400">{selectedRoute.totalDistanceMeters} meters</span>
                      </p>
                    </div>

                    <span
                      className={`px-3 py-1 rounded-full text-xs font-mono border font-bold ${
                        selectedRoute.routeStatus === 'SAFE'
                          ? 'bg-emerald-950 text-emerald-300 border-emerald-600'
                          : selectedRoute.routeStatus === 'CAUTION'
                          ? 'bg-amber-950 text-amber-300 border-amber-600'
                          : 'bg-red-950 text-red-300 border-red-600'
                      }`}
                    >
                      {selectedRoute.routeStatus}
                    </span>
                  </div>

                  {selectedRoute.calculationReason && (
                    <div className="p-3.5 rounded-xl bg-[#0e1624] border border-[#1e2e47] text-xs text-slate-300 flex items-start gap-2.5">
                      <Compass className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                      <div>
                        <span className="font-mono text-[10px] text-slate-400 uppercase font-bold">Routing Algorithm Explanation:</span>
                        <p className="mt-0.5">{selectedRoute.calculationReason}</p>
                      </div>
                    </div>
                  )}

                  {/* Waypoint Sequence */}
                  <div>
                    <h4 className="text-xs font-bold text-slate-300 uppercase font-mono mb-3">Waypoint Sequence ({selectedRoute.orderedWaypoints?.length ?? 0} Steps)</h4>
                    <div className="space-y-2">
                      {selectedRoute.orderedWaypoints && selectedRoute.orderedWaypoints.length > 0 ? (
                        selectedRoute.orderedWaypoints.map((wp, idx) => (
                          <div
                            key={wp.nodeId}
                            className="p-3 rounded-xl bg-[#09101b] border border-[#162235] flex items-center justify-between text-xs font-mono"
                          >
                            <div className="flex items-center gap-3">
                              <span className="w-6 h-6 rounded-full bg-[#141f33] border border-[#243552] text-cyan-400 flex items-center justify-center font-bold text-[10px]">
                                {idx + 1}
                              </span>
                              <div>
                                <span className="text-slate-200 font-semibold">{wp.nodeName}</span>
                                <span className="text-slate-500 ml-2">[{wp.nodeType}]</span>
                              </div>
                            </div>

                            <div className="text-slate-400 text-[11px]">
                              Floor Level {wp.floorNumber} • ({wp.posX.toFixed(1)}, {wp.posY.toFixed(1)}, {wp.posZ.toFixed(1)})
                            </div>
                          </div>
                        ))
                      ) : (
                        <div className="text-slate-500 text-xs italic">No safe waypoint path computed.</div>
                      )}
                    </div>
                  </div>
                </>
              ) : (
                <div className="text-center py-16 text-slate-400 text-xs">
                  Select a room from the left to inspect its dynamic egress path and waypoints.
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 5: EGRESS GRAPH & HAZARD CONFIGURATION */}
        {activeTab === 'graph' && (
          <div className="space-y-6">
            <div className="p-5 rounded-2xl bg-[#0c1320] border border-[#1a2538] space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-slate-200">Building Egress Connectivity & Hazard Simulation</h3>
                  <p className="text-xs text-slate-400">
                    Toggle connection blocks (e.g. fire door locked, collapsed corridor, heavy smoke obstruction) to trigger real-time route re-computation.
                  </p>
                </div>
                {canOperate && (
                  <button
                    onClick={async () => {
                      if (!selectedBuildingId) return;
                      await evacuationApi.recalculateRoutes(selectedBuildingId);
                      const gData = await evacuationApi.getBuildingGraph(selectedBuildingId);
                      setGraph(gData);
                    }}
                    className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium flex items-center gap-1.5 transition"
                  >
                    <RotateCcw className="w-3.5 h-3.5" />
                    <span>Recalculate Routes</span>
                  </button>
                )}
              </div>
            </div>

            {/* Connections Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {graph?.connections && graph.connections.length > 0 ? (
                graph.connections.map((conn) => (
                  <div
                    key={conn.id}
                    className={`p-4 rounded-xl border transition ${
                      conn.isBlocked
                        ? 'bg-red-950/30 border-red-500/50'
                        : 'bg-[#0e1624] border-[#1e2e47]'
                    }`}
                  >
                    <div className="flex items-center justify-between text-xs">
                      <span className="font-mono text-[10px] text-slate-400">CONN: {conn.id.substring(0, 8)}</span>
                      <span
                        className={`text-[10px] font-mono px-2 py-0.5 rounded-full border ${
                          conn.isBlocked
                            ? 'bg-red-950 text-red-400 border-red-700'
                            : 'bg-emerald-950 text-emerald-400 border-emerald-700'
                        }`}
                      >
                        {conn.isBlocked ? 'BLOCKED' : 'CLEAR'}
                      </span>
                    </div>

                    <div className="text-xs font-semibold text-slate-200 mt-2 flex items-center gap-2">
                      <span>{conn.fromNodeName}</span>
                      <ArrowRight className="w-3.5 h-3.5 text-slate-500 shrink-0" />
                      <span>{conn.toNodeName}</span>
                    </div>

                    <div className="flex items-center justify-between text-[11px] text-slate-400 mt-3 pt-2 border-t border-[#1a2538]">
                      <span>Distance: {conn.distanceMeters} m</span>
                      {canOperate && (
                        <button
                          onClick={() => handleToggleConnectionBlock(conn.id)}
                          className={`px-2.5 py-1 rounded text-[10px] font-medium flex items-center gap-1 transition ${
                            conn.isBlocked
                              ? 'bg-emerald-950 text-emerald-300 border border-emerald-700 hover:bg-emerald-900'
                              : 'bg-red-950 text-red-300 border border-red-700 hover:bg-red-900'
                          }`}
                        >
                          {conn.isBlocked ? (
                            <>
                              <Unlock className="w-3 h-3" />
                              <span>Unblock Path</span>
                            </>
                          ) : (
                            <>
                              <Lock className="w-3 h-3" />
                              <span>Simulate Block</span>
                            </>
                          )}
                        </button>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <div className="col-span-3 text-center py-8 text-slate-500 text-xs">
                  No egress graph connections loaded.
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* MODAL 1: REPORT INCIDENT */}
      {showReportModal && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#0c1320] border border-[#1e2e47] rounded-2xl w-full max-w-lg p-6 space-y-4 shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#1a2538] pb-3">
              <div className="flex items-center gap-2">
                <Flame className="w-5 h-5 text-red-400" />
                <h3 className="text-base font-bold text-slate-100">Log Emergency Incident</h3>
              </div>
              <button
                onClick={() => setShowReportModal(false)}
                className="p-1 rounded-lg hover:bg-[#162235] text-slate-400"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreateIncident} className="space-y-4 text-xs">
              <div>
                <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Title</label>
                <input
                  type="text"
                  required
                  value={reportTitle}
                  onChange={(e) => setReportTitle(e.target.value)}
                  placeholder="e.g. Heavy Smoke Detected in AI Lab"
                  className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Category</label>
                  <select
                    value={reportCategory}
                    onChange={(e) => setReportCategory(e.target.value as IncidentCategory)}
                    className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none"
                  >
                    <option value="FIRE">FIRE</option>
                    <option value="SMOKE">SMOKE</option>
                    <option value="HIGH_TEMPERATURE">HIGH_TEMPERATURE</option>
                    <option value="POOR_AIR_QUALITY">POOR_AIR_QUALITY</option>
                    <option value="OVERCROWDING">OVERCROWDING</option>
                    <option value="MANUAL_EMERGENCY">MANUAL_EMERGENCY</option>
                    <option value="OTHER">OTHER</option>
                  </select>
                </div>

                <div>
                  <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Severity</label>
                  <select
                    value={reportSeverity}
                    onChange={(e) => setReportSeverity(e.target.value as IncidentSeverity)}
                    className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none"
                  >
                    <option value="LOW">LOW</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="HIGH">HIGH</option>
                    <option value="CRITICAL">CRITICAL</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Description & Observations</label>
                <textarea
                  rows={3}
                  value={reportDescription}
                  onChange={(e) => setReportDescription(e.target.value)}
                  placeholder="Provide context, affected room, observed hazards..."
                  className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>

              <div className="flex items-center justify-end gap-2 pt-2 border-t border-[#1a2538]">
                <button
                  type="button"
                  onClick={() => setShowReportModal(false)}
                  className="px-3 py-1.5 rounded-lg bg-[#141e2e] text-slate-300 hover:bg-[#1a283e]"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-1.5 rounded-lg bg-red-600 hover:bg-red-500 text-white font-bold transition shadow"
                >
                  {submitting ? 'Logging...' : 'Submit Incident'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* MODAL 2: START EVACUATION */}
      {showStartEvacModal && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#0c1320] border border-[#1e2e47] rounded-2xl w-full max-w-md p-6 space-y-4 shadow-2xl text-xs">
            <div className="flex items-center justify-between border-b border-[#1a2538] pb-3">
              <div className="flex items-center gap-2">
                <Zap className="w-5 h-5 text-amber-400" />
                <h3 className="text-base font-bold text-slate-100">Initiate Evacuation Protocol</h3>
              </div>
              <button
                onClick={() => setShowStartEvacModal(false)}
                className="p-1 rounded-lg hover:bg-[#162235] text-slate-400"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <p className="text-slate-400 leading-relaxed">
              This will launch a building-wide evacuation session. Safe multi-exit routes will be calculated, and room clearance progress will be tracked.
            </p>

            <div className="flex items-center justify-end gap-2 pt-2 border-t border-[#1a2538]">
              <button
                type="button"
                onClick={() => setShowStartEvacModal(false)}
                className="px-3 py-1.5 rounded-lg bg-[#141e2e] text-slate-300 hover:bg-[#1a283e]"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleStartEvacuation}
                disabled={submitting}
                className="px-4 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold transition shadow"
              >
                {submitting ? 'Initiating...' : 'Confirm Evacuation Order'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 3: ASSIGN INCIDENT */}
      {showAssignModal && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#0c1320] border border-[#1e2e47] rounded-2xl w-full max-w-md p-6 space-y-4 shadow-2xl text-xs">
            <div className="flex items-center justify-between border-b border-[#1a2538] pb-3">
              <div className="flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-emerald-400" />
                <h3 className="text-base font-bold text-slate-100">Dispatch Safety Officer</h3>
              </div>
              <button
                onClick={() => setShowAssignModal(false)}
                className="p-1 rounded-lg hover:bg-[#162235] text-slate-400"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <div className="space-y-3">
              <div>
                <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Select Officer</label>
                <select
                  value={selectedOfficerId}
                  onChange={(e) => setSelectedOfficerId(e.target.value)}
                  className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none"
                >
                  <option value="">-- Choose Officer --</option>
                  {officers.map((off) => (
                    <option key={off.id} value={off.id}>
                      {off.fullName} ({off.role})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-slate-400 mb-1 font-mono uppercase text-[10px]">Dispatch Directives / Notes</label>
                <textarea
                  rows={2}
                  value={assignNotes}
                  onChange={(e) => setAssignNotes(e.target.value)}
                  placeholder="Instructions for responding officer..."
                  className="w-full bg-[#121c2d] border border-[#1e2e47] rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:border-emerald-500"
                />
              </div>
            </div>

            <div className="flex items-center justify-end gap-2 pt-2 border-t border-[#1a2538]">
              <button
                type="button"
                onClick={() => setShowAssignModal(false)}
                className="px-3 py-1.5 rounded-lg bg-[#141e2e] text-slate-300 hover:bg-[#1a283e]"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirmAssign}
                disabled={!selectedOfficerId}
                className="px-4 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold transition shadow disabled:opacity-50"
              >
                Dispatch & Assign
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
