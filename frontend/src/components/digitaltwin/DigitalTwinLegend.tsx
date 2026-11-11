import React from 'react';
import { DisplayMode } from '../../types/digitaltwin';

interface DigitalTwinLegendProps {
  displayMode: DisplayMode;
}

export const DigitalTwinLegend: React.FC<DigitalTwinLegendProps> = ({ displayMode }) => {
  const getLegendItems = () => {
    switch (displayMode) {
      case 'RISK':
        return [
          { label: 'Normal (0-29)', color: 'bg-emerald-500' },
          { label: 'Warning (30-59)', color: 'bg-yellow-500' },
          { label: 'High Risk (60-79)', color: 'bg-orange-500' },
          { label: 'Critical (80-100)', color: 'bg-red-500' },
        ];
      case 'OCCUPANCY':
        return [
          { label: '< 50% Low Density', color: 'bg-blue-500' },
          { label: '50-74% Moderate', color: 'bg-cyan-500' },
          { label: '75-89% High Density', color: 'bg-amber-500' },
          { label: '≥ 90% Near Capacity', color: 'bg-red-500' },
        ];
      case 'TEMPERATURE':
        return [
          { label: '< 20°C Cold', color: 'bg-cyan-500' },
          { label: '20-24°C Optimal', color: 'bg-emerald-500' },
          { label: '24-28°C Warm', color: 'bg-yellow-500' },
          { label: '28-35°C Hot', color: 'bg-orange-500' },
          { label: '≥ 35°C Surge', color: 'bg-red-500' },
        ];
      case 'SMOKE':
        return [
          { label: '< 1.5 ppm Clean', color: 'bg-emerald-500' },
          { label: '1.5-4 ppm Trace', color: 'bg-yellow-500' },
          { label: '4-15 ppm Elevated', color: 'bg-orange-500' },
          { label: '≥ 15 ppm Fire Hazard', color: 'bg-red-500' },
        ];
      case 'AIR_QUALITY':
        return [
          { label: '< 50 Good', color: 'bg-emerald-500' },
          { label: '50-100 Moderate', color: 'bg-yellow-500' },
          { label: '100-200 Unhealthy', color: 'bg-orange-500' },
          { label: '≥ 200 Hazardous', color: 'bg-red-500' },
        ];
      case 'EMERGENCY':
        return [
          { label: 'Active Hazard / Alarm', color: 'bg-red-500' },
          { label: 'Evacuating Room', color: 'bg-amber-500' },
          { label: 'Needs Assistance', color: 'bg-fuchsia-500' },
          { label: 'Room Cleared', color: 'bg-emerald-500' },
          { label: 'Safe Egress Path', color: 'bg-cyan-500' },
          { label: 'Emergency Exit', color: 'bg-emerald-400' },
          { label: 'Blocked Route', color: 'bg-rose-900' },
        ];
      case 'DEFAULT':
      default:
        return [
          { label: 'Lobby', color: 'bg-sky-500' },
          { label: 'Office', color: 'bg-emerald-500' },
          { label: 'Conference', color: 'bg-purple-500' },
          { label: 'Lab', color: 'bg-cyan-500' },
          { label: 'Server Room', color: 'bg-pink-500' },
          { label: 'Cafeteria', color: 'bg-amber-500' },
        ];
    }
  };

  const items = getLegendItems();

  return (
    <div className="absolute bottom-4 left-4 z-10 p-3 bg-[#080d16]/90 border border-[#1a2538] rounded-xl shadow-xl backdrop-blur-md text-[11px] font-mono">
      <div className="text-[10px] text-slate-400 uppercase tracking-wider mb-2 font-bold flex items-center justify-between gap-4">
        <span>Legend: {displayMode} Mode</span>
      </div>
      <div className="flex flex-wrap gap-x-4 gap-y-1.5 items-center">
        {items.map((item, idx) => (
          <div key={idx} className="flex items-center gap-1.5">
            <span className={`w-2.5 h-2.5 rounded-full ${item.color} shadow-sm`} />
            <span className="text-slate-300">{item.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

