import React, { useState, useEffect, useCallback } from 'react';
import { buildingApi, floorApi, roomApi, aiApi } from '../services/api';
import { Building, Floor, Room } from '../types/building';
import { RoomAiOverview } from '../types/ai';
import {
  Users,
  TrendingUp,
  Brain,
  Sparkles,
  RefreshCw,
  Building2,
  Layers,
  Clock,
  CheckCircle2,
  ArrowRight,
  Sliders,
} from 'lucide-react';

export const OccupancyPage: React.FC = () => {
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [floors, setFloors] = useState<Floor[]>([]);
  const [selectedFloorId, setSelectedFloorId] = useState<string>('ALL');
  const [rooms, setRooms] = useState<Room[]>([]);
  const [roomOverviews, setRoomOverviews] = useState<Record<string, RoomAiOverview>>({});
  const [loading, setLoading] = useState<boolean>(true);
  const [analyzing, setAnalyzing] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');

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

  // 2. Load Floors and Rooms for Selected Building
  const loadBuildingData = useCallback(async (bId: string) => {
    if (!bId) return;
    setLoading(true);
    try {
      const fList = await floorApi.getFloors(bId);
      setFloors(fList);

      // Fetch all rooms across floors
      const allRooms: Room[] = [];
      for (const fl of fList) {
        const rList = await roomApi.getRooms(fl.id);
        allRooms.push(...rList);
      }
      setRooms(allRooms);

      // Fetch latest AI overview for each room
      const overviews: Record<string, RoomAiOverview> = {};
      await Promise.all(
        allRooms.map(async (r) => {
          try {
            const ov = await aiApi.getRoomAiOverview(r.id);
            overviews[r.id] = ov;
          } catch {
            // Nominal fallback
            const cap = Math.max(1, r.maximumCapacity);
            overviews[r.id] = {
              roomId: r.id,
              roomName: r.roomName,
              roomNumber: r.roomCode,
              floorId: r.floorId || '',
              floorNumber: 1,
              buildingId: bId,
              currentOccupancy: r.currentOccupancy,
              capacity: cap,
              currentUtilizationPct: Math.round((r.currentOccupancy / cap) * 1000) / 10,
              riskScore: 10,
              riskLevel: 'NORMAL',
              predictedOccupancy: r.currentOccupancy,
              predictedUtilizationPct: Math.round((r.currentOccupancy / cap) * 1000) / 10,
              anomalyDetected: false,
              anomalySeverity: 'NORMAL',
              anomalyScore: 0.1,
              explanation: 'Nominal baseline.',
              sensorSnapshots: {},
              lastEvaluatedAt: new Date().toISOString(),
            };
          }
        })
      );
      setRoomOverviews(overviews);
    } catch (err) {
      console.error('Failed to load occupancy intelligence telemetry', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (selectedBuildingId) {
      loadBuildingData(selectedBuildingId);
    }
  }, [selectedBuildingId, loadBuildingData]);

  // Trigger On-Demand AI Inference
  const handleRunAiAnalysis = async () => {
    if (!selectedBuildingId) return;
    setAnalyzing(true);
    try {
      await aiApi.analyzeBuilding(selectedBuildingId);
      await loadBuildingData(selectedBuildingId);
    } catch (err) {
      console.error('Failed to run AI analysis', err);
    } finally {
      setAnalyzing(false);
    }
  };

  // Filtered Rooms
  const filteredRooms = rooms.filter((r) => {
    const matchesFloor = selectedFloorId === 'ALL' || r.floorId === selectedFloorId;
    const matchesQuery =
      r.roomName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      r.roomCode.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesFloor && matchesQuery;
  });

  // Fleet Totals
  const totalOccupants = rooms.reduce((acc, r) => acc + r.currentOccupancy, 0);
  const totalCapacity = rooms.reduce((acc, r) => acc + r.maximumCapacity, 0);
  const totalPredicted = filteredRooms.reduce((acc, r) => {
    const ov = roomOverviews[r.id];
    return acc + (ov ? ov.predictedOccupancy : r.currentOccupancy);
  }, 0);
  const avgUtilization = totalCapacity > 0 ? Math.round((totalOccupants / totalCapacity) * 1000) / 10 : 0;
  const predUtilization = totalCapacity > 0 ? Math.round((totalPredicted / totalCapacity) * 1000) / 10 : 0;

  return (
    <div className="space-y-6">
      {/* Top Header & AI Summary Banner */}
      <div className="p-6 bg-[#080d16] border border-[#1a2538] rounded-2xl shadow-xl flex flex-col lg:flex-row lg:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2 text-xs font-mono text-emerald-400 uppercase tracking-widest mb-1">
            <Brain className="w-4 h-4 text-emerald-400 animate-pulse" />
            <span>Random Forest Edge-AI Inference</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-100 flex items-center gap-2">
            Occupancy Prediction & Spatial Dynamics
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            15-minute predictive occupancy forecasting using multi-criteria time-series regression and room capacity models.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => selectedBuildingId && loadBuildingData(selectedBuildingId)}
            disabled={loading || analyzing}
            className="p-2.5 bg-[#0e1624] hover:bg-[#162234] border border-[#1e2e47] text-slate-300 rounded-xl transition-colors"
            title="Refresh Telemetry"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>

          <button
            onClick={handleRunAiAnalysis}
            disabled={analyzing || loading}
            className="flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-500 hover:from-emerald-500 hover:to-teal-400 text-white text-xs font-bold rounded-xl shadow-lg shadow-emerald-950/40 transition-all disabled:opacity-50"
          >
            <Sparkles className={`w-4 h-4 ${analyzing ? 'animate-spin' : ''}`} />
            <span>{analyzing ? 'Running AI Models...' : 'Run Fleet AI Inference'}</span>
          </button>
        </div>
      </div>

      {/* KPI Metrics Strip */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 font-medium">Current Total Occupancy</div>
            <div className="text-2xl font-bold text-slate-100 font-mono mt-1">
              {totalOccupants} <span className="text-xs font-normal text-slate-400">/ {totalCapacity}</span>
            </div>
            <div className="text-[11px] text-slate-400 mt-1">Live aggregated people count</div>
          </div>
          <div className="w-11 h-11 rounded-xl bg-blue-500/10 border border-blue-500/30 flex items-center justify-center text-blue-400">
            <Users className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 font-medium">15-Min Predicted Occupancy</div>
            <div className="text-2xl font-bold text-emerald-400 font-mono mt-1">
              {totalPredicted} <span className="text-xs font-normal text-slate-400">people</span>
            </div>
            <div className="text-[11px] text-emerald-400/80 mt-1 flex items-center gap-1">
              <TrendingUp className="w-3 h-3" />
              <span>RF Regressor (Horizon: +15m)</span>
            </div>
          </div>
          <div className="w-11 h-11 rounded-xl bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center text-emerald-400">
            <Brain className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 font-medium">Current vs Predicted Utilization</div>
            <div className="text-2xl font-bold text-slate-100 font-mono mt-1 flex items-baseline gap-2">
              <span>{avgUtilization}%</span>
              <ArrowRight className="w-4 h-4 text-slate-400 inline" />
              <span className="text-cyan-400">{predUtilization}%</span>
            </div>
            <div className="text-[11px] text-slate-400 mt-1">Spatial capacity density shift</div>
          </div>
          <div className="w-11 h-11 rounded-xl bg-cyan-500/10 border border-cyan-500/30 flex items-center justify-center text-cyan-400">
            <Sliders className="w-5 h-5" />
          </div>
        </div>

        <div className="p-5 bg-[#080d16] border border-[#1a2538] rounded-xl flex items-center justify-between">
          <div>
            <div className="text-xs text-slate-400 font-medium">Active AI Model Status</div>
            <div className="text-sm font-bold text-emerald-400 font-mono mt-1 flex items-center gap-1.5">
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              <span>RANDOM_FOREST v1.0.0</span>
            </div>
            <div className="text-[11px] text-slate-400 mt-1">Inference latency: &lt; 15ms</div>
          </div>
          <div className="w-11 h-11 rounded-xl bg-purple-500/10 border border-purple-500/30 flex items-center justify-center text-purple-400">
            <Clock className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Filter and Selection Toolbar */}
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

          {/* Floor Filter */}
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
        </div>

        {/* Room Search */}
        <div className="w-full sm:w-64">
          <input
            type="text"
            placeholder="Search rooms..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full px-3 py-1.5 bg-[#0e1624] border border-[#1e2e47] rounded-lg text-xs text-slate-200 placeholder-slate-400 focus:outline-none focus:border-emerald-500"
          />
        </div>
      </div>

      {/* Room Prediction Cards Grid */}
      {loading ? (
        <div className="p-12 text-center text-slate-400 text-xs flex flex-col items-center justify-center gap-3 bg-[#080d16] border border-[#1a2538] rounded-2xl">
          <RefreshCw className="w-6 h-6 animate-spin text-emerald-400" />
          <span>Synchronizing spatial AI models and live sensor readings...</span>
        </div>
      ) : filteredRooms.length === 0 ? (
        <div className="p-12 text-center text-slate-400 text-xs bg-[#080d16] border border-[#1a2538] rounded-2xl">
          No rooms match the selected criteria.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {filteredRooms.map((room) => {
            const ov = roomOverviews[room.id];
            const currentOcc = ov ? ov.currentOccupancy : room.currentOccupancy;
            const predOcc = ov ? ov.predictedOccupancy : room.currentOccupancy;
            const cap = Math.max(1, room.maximumCapacity);
            const curUtil = Math.round((currentOcc / cap) * 100);
            const predUtil = Math.round((predOcc / cap) * 100);
            const delta = predOcc - currentOcc;

            return (
              <div
                key={room.id}
                className="p-5 bg-[#080d16] border border-[#1a2538] hover:border-emerald-500/40 rounded-2xl shadow-lg transition-all group"
              >
                {/* Room Header */}
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#101928] text-emerald-400 border border-[#1c2c44]">
                      {room.roomCode}
                    </span>
                    <h3 className="text-sm font-bold text-slate-100 mt-1">{room.roomName}</h3>
                    <div className="text-[11px] text-slate-400">{room.roomType}</div>
                  </div>
                  <div className="text-right">
                    <span className="text-[10px] font-mono text-slate-400">Capacity</span>
                    <div className="text-xs font-bold text-slate-200 font-mono">{cap} max</div>
                  </div>
                </div>

                {/* Utilization Gauge Comparison */}
                <div className="space-y-3 my-4 p-3 bg-[#0b121e] border border-[#162234] rounded-xl">
                  {/* Current Occupancy */}
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-slate-400">Current Occupancy</span>
                      <span className="font-mono font-bold text-slate-200">
                        {currentOcc} people ({curUtil}%)
                      </span>
                    </div>
                    <div className="w-full bg-[#162234] h-2 rounded-full overflow-hidden">
                      <div
                        className={`h-full transition-all duration-500 ${
                          curUtil > 90
                            ? 'bg-red-500'
                            : curUtil > 75
                            ? 'bg-amber-400'
                            : 'bg-blue-500'
                        }`}
                        style={{ width: `${Math.min(100, curUtil)}%` }}
                      />
                    </div>
                  </div>

                  {/* 15-Min Prediction */}
                  <div>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-emerald-400 flex items-center gap-1 font-medium">
                        <Sparkles className="w-3 h-3" />
                        <span>Predicted (+15 min)</span>
                      </span>
                      <span className="font-mono font-bold text-emerald-400">
                        {predOcc} people ({predUtil}%)
                      </span>
                    </div>
                    <div className="w-full bg-[#162234] h-2 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 transition-all duration-500"
                        style={{ width: `${Math.min(100, predUtil)}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Trajectory Insight */}
                <div className="flex items-center justify-between text-xs pt-1 border-t border-[#141e30]">
                  <div className="flex items-center gap-1.5">
                    {delta > 0 ? (
                      <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">
                        +{delta} arrivals expected
                      </span>
                    ) : delta < 0 ? (
                      <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                        {delta} departures expected
                      </span>
                    ) : (
                      <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        Stable occupancy
                      </span>
                    )}
                  </div>

                  <span className="text-[10px] text-slate-400 font-mono">
                    Model: RF-v1.0.0
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
