import React from 'react';
import { Html } from '@react-three/drei';
import { DigitalTwinFloor } from '../../types/digitaltwin';

interface FloorSlabProps {
  floor: DigitalTwinFloor;
  floorElevation: number;
  isIsolated: boolean;
}

export const FloorSlab: React.FC<FloorSlabProps> = ({
  floor,
  floorElevation,
  isIsolated,
}) => {
  const slabWidth = Math.max(20, floor.layoutWidth || 60);
  const slabDepth = Math.max(20, floor.layoutDepth || 40);
  const slabThickness = 0.4;

  return (
    <group position={[0, floorElevation, 0]}>
      {/* Floor Plate Slab */}
      <mesh position={[0, -slabThickness / 2, 0]} receiveShadow>
        <boxGeometry args={[slabWidth, slabThickness, slabDepth]} />
        <meshStandardMaterial
          color={isIsolated ? '#1e293b' : '#0f172a'}
          roughness={0.8}
          metalness={0.2}
          transparent
          opacity={0.65}
        />
      </mesh>

      {/* Grid Orientation Guide on Floor */}
      <gridHelper
        args={[Math.max(slabWidth, slabDepth), 12, '#334155', '#1e293b']}
        position={[0, 0.01, 0]}
      />

      {/* 3D Floating Floor Identifier Tag */}
      <Html
        position={[-slabWidth / 2 - 1.5, 0.5, -slabDepth / 2]}
        distanceFactor={45}
        zIndexRange={[50, 0]}
      >
        <div className="px-2 py-0.5 rounded bg-[#090e17]/90 border border-slate-700 text-slate-300 text-[10px] font-mono shadow-md backdrop-blur-sm flex items-center gap-1.5 whitespace-nowrap">
          <span className="w-2 h-2 rounded-full bg-emerald-400" />
          <span className="font-bold text-white">L{floor.floorNumber}</span>
          <span className="text-slate-400">{floor.floorName}</span>
        </div>
      </Html>
    </group>
  );
};

