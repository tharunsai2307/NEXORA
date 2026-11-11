import React, { useState, useEffect, useCallback, useRef } from 'react';
import { buildingApi, digitalTwinApi, evacuationApi } from '../services/api';
import { Building } from '../types/building';
import {
  DigitalTwinBuilding,
  DigitalTwinRoom,
  DisplayMode,
  CameraViewPreset,
  RoomLayoutUpdatePayload,
} from '../types/digitaltwin';
import { EvacuationRoute } from '../types/emergency';
import { BuildingScene } from '../components/digitaltwin/BuildingScene';
import { SceneToolbar } from '../components/digitaltwin/SceneToolbar';
import { DigitalTwinLegend } from '../components/digitaltwin/DigitalTwinLegend';
import { RoomInformationPanel } from '../components/digitaltwin/RoomInformationPanel';
import { useAuth } from '../context/AuthContext';
import {
  Boxes,
  Building2,
  AlertOctagon,
  RefreshCw,
  Radio,
} from 'lucide-react';

export const DigitalTwinPage: React.FC = () => {
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);

  // Buildings & Selected State
  const [buildings, setBuildings] = useState<Building[]>([]);
  const [selectedBuildingId, setSelectedBuildingId] = useState<string>('');
  const [twinData, setTwinData] = useState<DigitalTwinBuilding | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // Scene Controls State
  const [selectedFloorId, setSelectedFloorId] = useState<string>('ALL');
  const [displayMode, setDisplayMode] = useState<DisplayMode>('RISK');
  const [selectedRoom, setSelectedRoom] = useState<DigitalTwinRoom | null>(null);
  const [cameraPreset, setCameraPreset] = useState<CameraViewPreset>('PERSPECTIVE');
  const [explodedGap, setExplodedGap] = useState<number>(3);
  const [wireframe, setWireframe] = useState<boolean>(false);
  const [wsConnected, setWsConnected] = useState<boolean>(false);
  const [routes, setRoutes] = useState<EvacuationRoute[]>([]);

  // WebSocket reference
  const wsRef = useRef<WebSocket | null>(null);

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

  // 2. Load Digital Twin Spatial Telemetry for Selected Building
  const loadTwinData = useCallback(async (bId: string) => {
    if (!bId) return;
    setLoading(true);
    try {
      const data = await digitalTwinApi.getBuildingTwin(bId);
      setTwinData(data);
      try {
        const rList = await evacuationApi.getBuildingRoutes(bId);
        setRoutes(rList || []);
      } catch (e) {
        console.warn('Evacuation routes not initialized yet for building', e);
      }
      // If selected room was open, refresh reference
      setSelectedRoom((prev) => {
        if (!prev) return null;
        for (const fl of data.floors) {
          const found = fl.rooms.find((r) => r.id === prev.id);
          if (found) return found;
        }
        return null;
      });
    } catch (err) {
      console.error('Failed to load digital twin telemetry', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (selectedBuildingId) {
      loadTwinData(selectedBuildingId);
      setSelectedFloorId('ALL');
    }
  }, [selectedBuildingId, loadTwinData]);

  // 3. Real-Time WebSocket Synchronization
  useEffect(() => {
    if (!selectedBuildingId) return;

    // Connect to Spring native WebSocket endpoint
    const wsUrl = `ws://${window.location.hostname}:8080/ws-raw`;
    let ws: WebSocket;

    try {
      ws = new WebSocket(wsUrl);
      wsRef.current = ws;

      ws.onopen = () => {
        setWsConnected(true);
        // Send STOMP CONNECT frame
        ws.send('CONNECT\naccept-version:1.2,1.1,1.0\nheart-beat:10000,10000\n\n\0');

        // Subscribe to building topics
        const topics = [
          `/topic/buildings/${selectedBuildingId}/sensors`,
          `/topic/buildings/${selectedBuildingId}/occupancy`,
          `/topic/buildings/${selectedBuildingId}/ai/anomalies`,
          `/topic/buildings/${selectedBuildingId}/ai/predictions`,
          `/topic/buildings/${selectedBuildingId}/ai/risk`,
          `/topic/buildings/${selectedBuildingId}/incidents`,
          `/topic/buildings/${selectedBuildingId}/emergency`,
          `/topic/buildings/${selectedBuildingId}/evacuation`,
          `/topic/buildings/${selectedBuildingId}/routes`,
        ];

        topics.forEach((t, i) => {
          ws.send(`SUBSCRIBE\nid:sub-${i}\ndestination:${t}\n\n\0`);
        });
      };

      ws.onmessage = (event) => {
        const raw = event.data;
        if (!raw || typeof raw !== 'string') return;

        // Parse STOMP MESSAGE frame payload
        const nullIdx = raw.indexOf('\0');
        const bodyStr = nullIdx !== -1 ? raw.substring(0, nullIdx) : raw;
        const bodyLines = bodyStr.split('\n\n');
        if (bodyLines.length < 2) return;

        try {
          const payload = JSON.parse(bodyLines[1]);

          // Real-time incremental state updater
          setTwinData((prev) => {
            if (!prev) return prev;
            const updatedFloors = prev.floors.map((fl) => ({
              ...fl,
              rooms: fl.rooms.map((r) => {
                if (payload.roomId && r.id === payload.roomId) {
                  const updatedRoom = { ...r };

                  if (payload.currentOccupancy !== undefined) {
                    updatedRoom.currentOccupancy = payload.currentOccupancy;
                    updatedRoom.utilizationPercentage =
                      Math.round((payload.currentOccupancy / Math.max(1, r.maximumCapacity)) * 1000) / 10;
                  }
                  if (payload.predictedOccupancy !== undefined) {
                    updatedRoom.predictedOccupancy = payload.predictedOccupancy;
                  }
                  if (payload.riskScore !== undefined) {
                    updatedRoom.riskScore = payload.riskScore;
                  }
                  if (payload.riskLevel !== undefined) {
                    updatedRoom.riskLevel = payload.riskLevel;
                  }
                  if (payload.severity !== undefined) {
                    updatedRoom.anomalySeverity = payload.severity;
                    updatedRoom.anomalyDetected = payload.anomalyDetected ?? false;
                  }
                  if (payload.sensorType && payload.value !== undefined) {
                    updatedRoom.sensorReadings = {
                      ...updatedRoom.sensorReadings,
                      [payload.sensorType]: payload.value,
                    };
                  }
                  return updatedRoom;
                }
                return r;
              }),
            }));

            return { ...prev, floors: updatedFloors };
          });
        } catch {
          // Heartbeat or non-json frame
        }
      };

      ws.onerror = () => setWsConnected(false);
      ws.onclose = () => setWsConnected(false);
    } catch {
      setWsConnected(false);
    }

    return () => {
      if (wsRef.current) {
        try {
          wsRef.current.close();
        } catch {}
      }
    };
  }, [selectedBuildingId]);

  // Handle Room Layout Updates
  const handleSaveRoomLayout = async (roomId: string, layout: RoomLayoutUpdatePayload) => {
    await digitalTwinApi.updateRoomLayout(roomId, layout);
    // Update local twin state
    setTwinData((prev) => {
      if (!prev) return prev;
      return {
        ...prev,
        floors: prev.floors.map((fl) => ({
          ...fl,
          rooms: fl.rooms.map((r) => (r.id === roomId ? { ...r, ...layout } : r)),
        })),
      };
    });
    // Update selected room object
    setSelectedRoom((prev) => (prev && prev.id === roomId ? { ...prev, ...layout } : prev));
  };

  return (
    <div className="flex flex-col h-[calc(100vh-5rem)] space-y-4">
      {/* Top Telemetry Header Bar */}
      <div className="p-4 bg-[#080d16] border border-[#1a2538] rounded-2xl shadow-xl flex flex-wrap items-center justify-between gap-4 shrink-0">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="w-9 h-9 rounded-xl bg-cyan-950/60 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
              <Boxes className="w-5 h-5" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-sm font-bold text-slate-100">
                  {twinData?.name || '3D Building Digital Twin'}
                </h1>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
                  {twinData?.buildingCode || 'NEX-TWIN'}
                </span>
              </div>
              <div className="text-[10px] text-slate-400 flex items-center gap-2 mt-0.5">
                <span>{twinData?.totalFloors ?? 0} Floors</span>
                <span>•</span>
                <span>{twinData?.roomCount ?? 0} Rooms</span>
                <span>•</span>
                <span className="text-slate-300">
                  Capacity: {twinData?.currentOccupancy ?? 0} / {twinData?.totalCapacity ?? 0}
                </span>
              </div>
            </div>
          </div>

          {/* Building Selector */}
          <div className="flex items-center gap-1.5 px-3 py-1.5 bg-[#0e1624] border border-[#1e2e47] rounded-xl text-xs">
            <Building2 className="w-3.5 h-3.5 text-slate-400" />
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
        </div>

        {/* Right Status Badges */}
        <div className="flex items-center gap-3">
          {/* Critical Room Counter Alert */}
          {(twinData?.criticalRoomsCount ?? 0) > 0 && (
            <div className="flex items-center gap-1.5 px-3 py-1 bg-red-950/60 border border-red-500/50 text-red-400 rounded-xl text-xs font-bold animate-pulse">
              <AlertOctagon className="w-4 h-4" />
              <span>{twinData?.criticalRoomsCount} Critical Zones</span>
            </div>
          )}

          {/* WebSocket Status Indicator */}
          <div
            className={`flex items-center gap-1.5 px-2.5 py-1 rounded-xl text-[11px] font-mono border ${
              wsConnected
                ? 'bg-emerald-950/50 text-emerald-400 border-emerald-500/30'
                : 'bg-slate-900 text-slate-400 border-slate-700'
            }`}
          >
            <Radio className={`w-3 h-3 ${wsConnected ? 'animate-pulse text-emerald-400' : ''}`} />
            <span>{wsConnected ? 'WS LIVE' : 'WS POLLING'}</span>
          </div>

          {/* Refresh Button */}
          <button
            onClick={() => selectedBuildingId && loadTwinData(selectedBuildingId)}
            disabled={loading}
            className="p-2 bg-[#0e1624] hover:bg-[#162234] border border-[#1e2e47] text-slate-300 rounded-xl transition-colors"
            title="Reload Digital Twin"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </div>

      {/* Main 3D Viewport and Slide-Out Inspection Panel */}
      <div className="relative flex-1 rounded-2xl overflow-hidden border border-[#1a2538] flex">
        {loading || !twinData ? (
          <div className="w-full h-full flex flex-col items-center justify-center gap-3 bg-[#060a12] text-slate-400 text-xs">
            <RefreshCw className="w-8 h-8 animate-spin text-emerald-400" />
            <span>Constructing 3D Euclidean Spatial Digital Twin...</span>
          </div>
        ) : (
          <>
            {/* 3D Scene Viewport */}
            <div className="relative flex-1 h-full">
              {/* Floating Toolbar Controls */}
              <SceneToolbar
                displayMode={displayMode}
                onChangeDisplayMode={setDisplayMode}
                selectedFloorId={selectedFloorId}
                onChangeFloor={setSelectedFloorId}
                floors={twinData.floors}
                cameraPreset={cameraPreset}
                onChangeCameraPreset={setCameraPreset}
                explodedGap={explodedGap}
                onChangeExplodedGap={setExplodedGap}
                wireframe={wireframe}
                onToggleWireframe={() => setWireframe(!wireframe)}
              />

              {/* Three.js / React Three Fiber Scene */}
              <BuildingScene
                building={twinData}
                selectedFloorId={selectedFloorId}
                displayMode={displayMode}
                selectedRoomId={selectedRoom?.id ?? null}
                onSelectRoom={(r) => setSelectedRoom(r)}
                cameraPreset={cameraPreset}
                explodedGap={explodedGap}
                wireframe={wireframe}
                routes={routes}
                showRoutes={displayMode === 'EMERGENCY'}
              />

              {/* Color Scale Legend */}
              <DigitalTwinLegend displayMode={displayMode} />
            </div>

            {/* Selected Room Details Side Panel */}
            {selectedRoom && (
              <RoomInformationPanel
                room={selectedRoom}
                onClose={() => setSelectedRoom(null)}
                onSaveLayout={handleSaveRoomLayout}
                canManage={canManage}
              />
            )}
          </>
        )}
      </div>
    </div>
  );
};
