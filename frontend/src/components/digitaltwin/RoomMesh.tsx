import React, { useState } from 'react';
import { Html } from '@react-three/drei';
import { DigitalTwinRoom, DisplayMode } from '../../types/digitaltwin';

interface RoomMeshProps {
  room: DigitalTwinRoom;
  floorElevation: number;
  displayMode: DisplayMode;
  isSelected: boolean;
  onSelect: (room: DigitalTwinRoom) => void;
  wireframe?: boolean;
}

export const RoomMesh: React.FC<RoomMeshProps> = ({
  room,
  floorElevation,
  displayMode,
  isSelected,
  onSelect,
  wireframe = false,
}) => {
  const [hovered, setHovered] = useState(false);

  // Compute room color based on active display mode
  const getRoomColor = (): string => {
    switch (displayMode) {
      case 'RISK':
        switch (room.riskLevel) {
          case 'CRITICAL':
            return '#ef4444'; // Red
          case 'HIGH_RISK':
            return '#f97316'; // Orange
          case 'WARNING':
            return '#eab308'; // Yellow
          case 'NORMAL':
          default:
            return '#10b981'; // Green
        }

      case 'OCCUPANCY': {
        const util = room.utilizationPercentage;
        if (util >= 90) return '#ef4444';
        if (util >= 75) return '#f59e0b';
        if (util >= 50) return '#06b6d4';
        return '#3b82f6';
      }

      case 'TEMPERATURE': {
        const temp = room.sensorReadings?.TEMPERATURE ?? 22.0;
        if (temp >= 35) return '#ef4444';
        if (temp >= 28) return '#f97316';
        if (temp >= 24) return '#eab308';
        if (temp >= 20) return '#10b981';
        return '#06b6d4';
      }

      case 'SMOKE': {
        const smoke = room.sensorReadings?.SMOKE ?? 0.2;
        if (smoke >= 15) return '#ef4444';
        if (smoke >= 4) return '#f97316';
        if (smoke >= 1.5) return '#eab308';
        return '#10b981';
      }

      case 'AIR_QUALITY': {
        const aqi = room.sensorReadings?.AIR_QUALITY ?? 35;
        if (aqi >= 200) return '#ef4444';
        if (aqi >= 100) return '#f97316';
        if (aqi >= 50) return '#eab308';
        return '#10b981';
      }

      case 'DEFAULT':
      default:
        switch (room.roomType) {
          case 'LOBBY':
            return '#0284c7';
          case 'CONFERENCE_ROOM':
            return '#8b5cf6';
          case 'CAFETERIA':
            return '#f59e0b';
          case 'LABORATORY':
            return '#06b6d4';
          case 'OFFICE':
            return '#10b981';
          case 'SERVER_ROOM':
            return '#ec4899';
          case 'EMERGENCY_EXIT_AREA':
            return '#ef4444';
          default:
            return '#475569';
        }
    }
  };

  // Get active badge value for the 3D floating tag
  const getDisplayBadge = (): string => {
    switch (displayMode) {
      case 'RISK':
        return `Risk ${Math.round(room.riskScore)}`;
      case 'OCCUPANCY':
        return `${room.currentOccupancy}/${room.maximumCapacity} (${Math.round(room.utilizationPercentage)}%)`;
      case 'TEMPERATURE':
        return `${(room.sensorReadings?.TEMPERATURE ?? 22.0).toFixed(1)}°C`;
      case 'SMOKE':
        return `${(room.sensorReadings?.SMOKE ?? 0.2).toFixed(1)} ppm`;
      case 'AIR_QUALITY':
        return `${Math.round(room.sensorReadings?.AIR_QUALITY ?? 35)} AQI`;
      default:
        return `${room.currentOccupancy} occ`;
    }
  };

  const isCritical = room.riskLevel === 'CRITICAL' && displayMode === 'RISK';
  const baseColor = getRoomColor();
  const roomY = floorElevation + room.height / 2;

  return (
    <group position={[room.posX, roomY, room.posZ]}>
      {/* Primary Room Mesh */}
      <mesh
        onClick={(e) => {
          e.stopPropagation();
          onSelect(room);
        }}
        onPointerOver={(e) => {
          e.stopPropagation();
          setHovered(true);
        }}
        onPointerOut={() => setHovered(false)}
        castShadow
        receiveShadow
      >
        <boxGeometry args={[room.width, room.height, room.depth]} />
        <meshStandardMaterial
          color={baseColor}
          transparent
          opacity={wireframe ? 0.2 : hovered ? 0.88 : 0.72}
          wireframe={wireframe}
          roughness={0.3}
          metalness={0.2}
          emissive={isSelected ? '#38bdf8' : isCritical ? '#ef4444' : hovered ? '#ffffff' : '#000000'}
          emissiveIntensity={isSelected ? 0.6 : isCritical ? 0.5 : hovered ? 0.25 : 0.0}
        />
      </mesh>

      {/* Selected / Critical Outline Edges */}
      {(isSelected || isCritical || hovered) && (
        <lineSegments>
          <edgesGeometry args={[new (window as any).THREE.BoxGeometry(room.width, room.height, room.depth)]} />
          <lineBasicMaterial
            color={isSelected ? '#38bdf8' : isCritical ? '#ef4444' : '#94a3b8'}
            linewidth={isSelected ? 3 : 1}
          />
        </lineSegments>
      )}

      {/* 3D Floating Room Label */}
      <Html
        position={[0, room.height / 2 + 0.6, 0]}
        center
        distanceFactor={35}
        zIndexRange={[100, 0]}
      >
        <div
          onClick={(e) => {
            e.stopPropagation();
            onSelect(room);
          }}
          className={`px-2 py-1 rounded-md text-[10px] font-mono whitespace-nowrap cursor-pointer transition-all shadow-lg select-none backdrop-blur-md border ${
            isSelected
              ? 'bg-sky-950/90 text-sky-300 border-sky-400 scale-110 ring-2 ring-sky-400/50'
              : isCritical
              ? 'bg-red-950/90 text-red-300 border-red-500 animate-pulse font-bold'
              : hovered
              ? 'bg-slate-900/90 text-white border-slate-400 scale-105'
              : 'bg-[#090e17]/80 text-slate-300 border-[#1a283e]'
          }`}
        >
          <div className="font-bold flex items-center gap-1">
            <span>{room.roomCode}</span>
            {isCritical && <span className="w-2 h-2 rounded-full bg-red-500 animate-ping" />}
          </div>
          <div className="text-[9px] text-slate-400 flex items-center justify-between gap-2 mt-0.5">
            <span>{room.roomName}</span>
            <span className="text-emerald-400 font-semibold">{getDisplayBadge()}</span>
          </div>
        </div>
      </Html>
    </group>
  );
};
