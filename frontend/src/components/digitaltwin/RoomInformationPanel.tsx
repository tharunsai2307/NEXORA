import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  X,
  Users,
  AlertTriangle,
  AlertOctagon,
  Sparkles,
  Thermometer,
  Flame,
  Wind,
  Droplets,
  Sliders,
  CheckCircle2,
  ArrowRight,
  Maximize2,
} from 'lucide-react';
import { DigitalTwinRoom, RoomLayoutUpdatePayload } from '../../types/digitaltwin';

interface RoomInformationPanelProps {
  room: DigitalTwinRoom | null;
  onClose: () => void;
  onSaveLayout: (roomId: string, layout: RoomLayoutUpdatePayload) => Promise<void>;
  canManage: boolean;
}

export const RoomInformationPanel: React.FC<RoomInformationPanelProps> = ({
  room,
  onClose,
  onSaveLayout,
  canManage,
}) => {
  const [isEditingLayout, setIsEditingLayout] = useState(false);
  const [saving, setSaving] = useState(false);
  const [layoutForm, setLayoutForm] = useState<RoomLayoutUpdatePayload>({
    posX: 0,
    posY: 0,
    posZ: 0,
    width: 10,
    depth: 8,
    height: 3.5,
  });

  useEffect(() => {
    if (room) {
      setLayoutForm({
        posX: room.posX,
        posY: room.posY,
        posZ: room.posZ,
        width: room.width,
        depth: room.depth,
        height: room.height,
      });
      setIsEditingLayout(false);
    }
  }, [room]);

  if (!room) return null;

  const handleLayoutSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await onSaveLayout(room.id, layoutForm);
      setIsEditingLayout(false);
    } catch (err) {
      console.error('Failed to update spatial layout', err);
    } finally {
      setSaving(false);
    }
  };

  const getRiskBadge = () => {
    switch (room.riskLevel) {
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
      default:
        return (
          <span className="flex items-center gap-1 text-[11px] font-mono px-2.5 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 border border-emerald-500/40">
            <CheckCircle2 className="w-3.5 h-3.5" /> NORMAL
          </span>
        );
    }
  };

  const curOcc = room.currentOccupancy;
  const predOcc = room.predictedOccupancy;
  const delta = predOcc - curOcc;
  const temp = room.sensorReadings?.TEMPERATURE;
  const hum = room.sensorReadings?.HUMIDITY;
  const smoke = room.sensorReadings?.SMOKE;
  const aqi = room.sensorReadings?.AIR_QUALITY;

  return (
    <div className="w-80 sm:w-96 bg-[#080d16] border-l border-[#1a2538] flex flex-col h-full shadow-2xl z-20 overflow-y-auto">
      {/* Panel Header */}
      <div className="p-4 border-b border-[#1a2538] flex items-start justify-between bg-[#0b121e]">
        <div>
          <div className="flex items-center gap-2">
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#101928] text-emerald-400 border border-[#1c2c44] font-bold">
              {room.roomCode}
            </span>
            <span className="text-[10px] text-slate-400 font-mono">Floor {room.floorNumber}</span>
          </div>
          <h2 className="text-base font-bold text-slate-100 mt-1">{room.roomName}</h2>
          <div className="text-[11px] text-slate-400">{room.roomType}</div>
        </div>
        <button
          onClick={onClose}
          className="p-1 text-slate-400 hover:text-slate-100 hover:bg-[#162234] rounded-lg transition-colors"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      <div className="p-4 space-y-4">
        {/* Safety Risk Status Banner */}
        <div className="p-3.5 bg-[#0b121e] border border-[#162234] rounded-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium">Safety Risk Level</span>
            {getRiskBadge()}
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold font-mono text-slate-100">
              {Math.round(room.riskScore)}
            </span>
            <span className="text-xs text-slate-400">/ 100 Risk Score</span>
          </div>
          <p className="text-[11px] text-slate-400">
            {room.riskExplanation || room.anomalyExplanation || 'Nominal operating conditions.'}
          </p>
        </div>

        {/* Live Occupancy vs 15-Min Prediction */}
        <div className="p-3.5 bg-[#0b121e] border border-[#162234] rounded-xl space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium flex items-center gap-1.5">
              <Users className="w-3.5 h-3.5 text-blue-400" />
              <span>Occupancy Telemetry</span>
            </span>
            <span className="text-[10px] font-mono text-slate-400">
              Cap: {room.maximumCapacity}
            </span>
          </div>

          {/* Current Occupancy Bar */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-slate-400">Current</span>
              <span className="font-mono font-bold text-slate-200">
                {curOcc} people ({Math.round(room.utilizationPercentage)}%)
              </span>
            </div>
            <div className="w-full bg-[#162234] h-1.5 rounded-full overflow-hidden">
              <div
                className="h-full bg-blue-500 transition-all duration-300"
                style={{ width: `${Math.min(100, room.utilizationPercentage)}%` }}
              />
            </div>
          </div>

          {/* 15-Minute Random Forest Prediction Bar */}
          <div>
            <div className="flex justify-between text-xs mb-1">
              <span className="text-emerald-400 flex items-center gap-1">
                <Sparkles className="w-3 h-3" />
                <span>Predicted (+15 min)</span>
              </span>
              <span className="font-mono font-bold text-emerald-400">
                {predOcc} people
              </span>
            </div>
            <div className="w-full bg-[#162234] h-1.5 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 transition-all duration-300"
                style={{
                  width: `${Math.min(100, (predOcc / Math.max(1, room.maximumCapacity)) * 100)}%`,
                }}
              />
            </div>
          </div>

          <div className="text-[10px] font-mono text-slate-400 pt-1 border-t border-[#141e30] flex items-center justify-between">
            <span>Forecast Trajectory</span>
            <span className={delta > 0 ? 'text-amber-400' : delta < 0 ? 'text-blue-400' : 'text-emerald-400'}>
              {delta > 0 ? `+${delta} expected` : delta < 0 ? `${delta} departures` : 'Steady state'}
            </span>
          </div>
        </div>

        {/* Sensor Readings Snapshot Grid */}
        <div className="p-3.5 bg-[#0b121e] border border-[#162234] rounded-xl space-y-2">
          <div className="text-xs text-slate-400 font-medium flex items-center gap-1.5">
            <Sliders className="w-3.5 h-3.5 text-emerald-400" />
            <span>Virtual IoT Sensors</span>
          </div>

          <div className="grid grid-cols-2 gap-2 text-xs pt-1">
            <div className="p-2 bg-[#080d16] border border-[#162234] rounded-lg">
              <div className="text-[10px] text-slate-400 flex items-center gap-1">
                <Thermometer className="w-3 h-3 text-red-400" /> Temperature
              </div>
              <div className="font-mono font-bold text-slate-100 mt-0.5">
                {temp !== undefined ? `${temp.toFixed(1)}°C` : 'N/A'}
              </div>
            </div>

            <div className="p-2 bg-[#080d16] border border-[#162234] rounded-lg">
              <div className="text-[10px] text-slate-400 flex items-center gap-1">
                <Droplets className="w-3 h-3 text-cyan-400" /> Humidity
              </div>
              <div className="font-mono font-bold text-slate-100 mt-0.5">
                {hum !== undefined ? `${hum.toFixed(1)}%` : 'N/A'}
              </div>
            </div>

            <div className="p-2 bg-[#080d16] border border-[#162234] rounded-lg">
              <div className="text-[10px] text-slate-400 flex items-center gap-1">
                <Flame className="w-3 h-3 text-orange-400" /> Smoke
              </div>
              <div className="font-mono font-bold text-slate-100 mt-0.5">
                {smoke !== undefined ? `${smoke.toFixed(1)} ppm` : 'N/A'}
              </div>
            </div>

            <div className="p-2 bg-[#080d16] border border-[#162234] rounded-lg">
              <div className="text-[10px] text-slate-400 flex items-center gap-1">
                <Wind className="w-3 h-3 text-teal-400" /> Air Quality
              </div>
              <div className="font-mono font-bold text-slate-100 mt-0.5">
                {aqi !== undefined ? `${Math.round(aqi)} AQI` : 'N/A'}
              </div>
            </div>
          </div>
        </div>

        {/* 3D Spatial Layout Editor */}
        <div className="p-3.5 bg-[#0b121e] border border-[#162234] rounded-xl space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-xs text-slate-400 font-medium flex items-center gap-1.5">
              <Maximize2 className="w-3.5 h-3.5 text-purple-400" />
              <span>3D Spatial Coordinates</span>
            </span>
            {canManage && (
              <button
                onClick={() => setIsEditingLayout(!isEditingLayout)}
                className="text-[10px] font-mono text-emerald-400 hover:text-emerald-300"
              >
                {isEditingLayout ? 'Cancel' : 'Edit Layout'}
              </button>
            )}
          </div>

          {isEditingLayout ? (
            <form onSubmit={handleLayoutSave} className="space-y-2.5 pt-2">
              <div className="grid grid-cols-3 gap-2 text-xs">
                <div>
                  <label className="text-[9px] text-slate-400">Pos X (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    value={layoutForm.posX}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, posX: parseFloat(e.target.value) || 0 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
                <div>
                  <label className="text-[9px] text-slate-400">Pos Y (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    value={layoutForm.posY}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, posY: parseFloat(e.target.value) || 0 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
                <div>
                  <label className="text-[9px] text-slate-400">Pos Z (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    value={layoutForm.posZ}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, posZ: parseFloat(e.target.value) || 0 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 text-xs">
                <div>
                  <label className="text-[9px] text-slate-400">Width (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    min="1"
                    value={layoutForm.width}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, width: parseFloat(e.target.value) || 1 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
                <div>
                  <label className="text-[9px] text-slate-400">Depth (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    min="1"
                    value={layoutForm.depth}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, depth: parseFloat(e.target.value) || 1 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
                <div>
                  <label className="text-[9px] text-slate-400">Height (m)</label>
                  <input
                    type="number"
                    step="0.5"
                    min="1"
                    value={layoutForm.height}
                    onChange={(e) =>
                      setLayoutForm({ ...layoutForm, height: parseFloat(e.target.value) || 1 })
                    }
                    className="w-full px-2 py-1 bg-[#080d16] border border-[#1e2e47] rounded text-slate-100 font-mono text-xs"
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={saving}
                className="w-full py-1.5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg transition-colors disabled:opacity-50"
              >
                {saving ? 'Saving Coordinates...' : 'Save 3D Layout to Database'}
              </button>
            </form>
          ) : (
            <div className="grid grid-cols-2 gap-2 text-[11px] font-mono text-slate-300 pt-1">
              <div>Position: ({room.posX}, {room.posY}, {room.posZ})</div>
              <div>Size: {room.width}m × {room.depth}m × {room.height}m</div>
            </div>
          )}
        </div>

        {/* Quick Cross-Module Navigation Links */}
        <div className="pt-2 flex flex-col gap-1.5 text-xs">
          <Link
            to="/sensors"
            className="flex items-center justify-between p-2 rounded-lg bg-[#0c1320] hover:bg-[#141e30] border border-[#1a2538] text-slate-300 hover:text-white transition-colors"
          >
            <span>Inspect Room IoT Sensors</span>
            <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
          </Link>
          <Link
            to="/occupancy"
            className="flex items-center justify-between p-2 rounded-lg bg-[#0c1320] hover:bg-[#141e30] border border-[#1a2538] text-slate-300 hover:text-white transition-colors"
          >
            <span>Occupancy Prediction Center</span>
            <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
          </Link>
          <Link
            to="/safety"
            className="flex items-center justify-between p-2 rounded-lg bg-[#0c1320] hover:bg-[#141e30] border border-[#1a2538] text-slate-300 hover:text-white transition-colors"
          >
            <span>Building Safety & Anomaly Center</span>
            <ArrowRight className="w-3.5 h-3.5 text-slate-400" />
          </Link>
        </div>
      </div>
    </div>
  );
};
