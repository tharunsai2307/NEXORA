import React from 'react';
import {
  Eye,
  ShieldCheck,
  Users,
  Thermometer,
  Flame,
  Wind,
  Layers,
  Camera,
  RotateCcw,
  Sliders,
  Box,
  AlertTriangle,
} from 'lucide-react';
import { DisplayMode, CameraViewPreset, DigitalTwinFloor } from '../../types/digitaltwin';

interface SceneToolbarProps {
  displayMode: DisplayMode;
  onChangeDisplayMode: (mode: DisplayMode) => void;
  selectedFloorId: string;
  onChangeFloor: (floorId: string) => void;
  floors: DigitalTwinFloor[];
  cameraPreset: CameraViewPreset;
  onChangeCameraPreset: (preset: CameraViewPreset) => void;
  explodedGap: number;
  onChangeExplodedGap: (gap: number) => void;
  wireframe: boolean;
  onToggleWireframe: () => void;
}

export const SceneToolbar: React.FC<SceneToolbarProps> = ({
  displayMode,
  onChangeDisplayMode,
  selectedFloorId,
  onChangeFloor,
  floors,
  cameraPreset,
  onChangeCameraPreset,
  explodedGap,
  onChangeExplodedGap,
  wireframe,
  onToggleWireframe,
}) => {
  const displayModes: { mode: DisplayMode; label: string; icon: React.ComponentType<{ className?: string }> }[] = [
    { mode: 'DEFAULT', label: 'Default', icon: Eye },
    { mode: 'RISK', label: 'AI Risk', icon: ShieldCheck },
    { mode: 'OCCUPANCY', label: 'Occupancy', icon: Users },
    { mode: 'TEMPERATURE', label: 'Thermal', icon: Thermometer },
    { mode: 'SMOKE', label: 'Smoke', icon: Flame },
    { mode: 'AIR_QUALITY', label: 'AQI', icon: Wind },
    { mode: 'EMERGENCY', label: 'Emergency', icon: AlertTriangle },
  ];

  const cameraPresets: { preset: CameraViewPreset; label: string }[] = [
    { preset: 'PERSPECTIVE', label: '3D' },
    { preset: 'TOP', label: 'Top' },
    { preset: 'FRONT', label: 'Front' },
    { preset: 'SIDE', label: 'Side' },
  ];

  return (
    <div className="absolute top-4 left-4 right-4 z-10 flex flex-wrap items-center justify-between gap-3 pointer-events-none">
      {/* Left Toolbar: Display Mode Selector */}
      <div className="flex flex-wrap items-center gap-1.5 p-1.5 bg-[#080d16]/90 border border-[#1a2538] rounded-xl shadow-xl backdrop-blur-md pointer-events-auto">
        {displayModes.map(({ mode, label, icon: Icon }) => (
          <button
            key={mode}
            onClick={() => onChangeDisplayMode(mode)}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
              displayMode === mode
                ? 'bg-emerald-600 text-white font-bold shadow-md shadow-emerald-950/40'
                : 'text-slate-300 hover:text-white hover:bg-[#121c2c]'
            }`}
          >
            <Icon className="w-3.5 h-3.5" />
            <span>{label}</span>
          </button>
        ))}
      </div>

      {/* Right Toolbar: Camera, Floor, and Exploded View Controls */}
      <div className="flex flex-wrap items-center gap-2 pointer-events-auto">
        {/* Floor Isolation Selector */}
        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-[#080d16]/90 border border-[#1a2538] rounded-xl shadow-xl backdrop-blur-md text-xs">
          <Layers className="w-3.5 h-3.5 text-slate-400" />
          <select
            value={selectedFloorId}
            onChange={(e) => onChangeFloor(e.target.value)}
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

        {/* Exploded View Gap Slider */}
        {selectedFloorId === 'ALL' && (
          <div className="flex items-center gap-2 px-3 py-1.5 bg-[#080d16]/90 border border-[#1a2538] rounded-xl shadow-xl backdrop-blur-md text-xs">
            <Sliders className="w-3.5 h-3.5 text-slate-400" />
            <span className="text-[10px] text-slate-400 font-mono">Explode:</span>
            <input
              type="range"
              min="0"
              max="15"
              step="1"
              value={explodedGap}
              onChange={(e) => onChangeExplodedGap(parseFloat(e.target.value))}
              className="w-20 accent-emerald-500 cursor-pointer"
              title="Vertically separate floor plates"
            />
            <span className="text-[10px] text-emerald-400 font-mono w-6">{explodedGap}m</span>
          </div>
        )}

        {/* Wireframe Toggle */}
        <button
          onClick={onToggleWireframe}
          className={`p-2 rounded-xl border text-xs transition-colors backdrop-blur-md ${
            wireframe
              ? 'bg-purple-950/80 border-purple-500 text-purple-300'
              : 'bg-[#080d16]/90 border-[#1a2538] text-slate-300 hover:bg-[#121c2c]'
          }`}
          title="Toggle Transparent X-Ray Wireframe"
        >
          <Box className="w-3.5 h-3.5" />
        </button>

        {/* Camera Views Preset Toolbar */}
        <div className="flex items-center gap-1 p-1 bg-[#080d16]/90 border border-[#1a2538] rounded-xl shadow-xl backdrop-blur-md text-xs">
          <Camera className="w-3.5 h-3.5 text-slate-400 ml-1.5 mr-0.5" />
          {cameraPresets.map(({ preset, label }) => (
            <button
              key={preset}
              onClick={() => onChangeCameraPreset(preset)}
              className={`px-2 py-1 rounded-md text-[11px] font-mono transition-colors ${
                cameraPreset === preset
                  ? 'bg-sky-600 text-white font-bold'
                  : 'text-slate-300 hover:text-white hover:bg-[#121c2c]'
              }`}
            >
              {label}
            </button>
          ))}
          <button
            onClick={() => onChangeCameraPreset('RESET')}
            className="p-1 text-slate-400 hover:text-white hover:bg-[#121c2c] rounded-md transition-colors ml-0.5"
            title="Reset Camera Angle"
          >
            <RotateCcw className="w-3 h-3" />
          </button>
        </div>
      </div>
    </div>
  );
};

