import React, { useRef, useEffect } from 'react';
import { Canvas } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import type { OrbitControls as OrbitControlsImpl } from 'three-stdlib';
import * as THREE from 'three';

import { RoomMesh } from './RoomMesh';
import { FloorSlab } from './FloorSlab';
import { EvacuationRouteMesh } from './EvacuationRouteMesh';
import {
  DigitalTwinBuilding,
  DigitalTwinRoom,
  DisplayMode,
  CameraViewPreset,
} from '../../types/digitaltwin';
import { EvacuationRoute } from '../../types/emergency';

// Inject THREE into window for Geometry helper access if needed
if (typeof window !== 'undefined' && !(window as any).THREE) {
  (window as any).THREE = THREE;
}

interface BuildingSceneProps {
  building: DigitalTwinBuilding;
  selectedFloorId: string;
  displayMode: DisplayMode;
  selectedRoomId: string | null;
  onSelectRoom: (room: DigitalTwinRoom) => void;
  cameraPreset: CameraViewPreset;
  explodedGap: number;
  wireframe: boolean;
  routes?: EvacuationRoute[];
  showRoutes?: boolean;
}

// Camera controller component inside R3F Canvas
const CameraController: React.FC<{
  cameraPreset: CameraViewPreset;
  controlsRef: React.RefObject<OrbitControlsImpl>;
}> = ({ cameraPreset, controlsRef }) => {
  useEffect(() => {
    if (!controlsRef.current) return;
    const controls = controlsRef.current;
    const camera = controls.object;

    switch (cameraPreset) {
      case 'TOP':
        camera.position.set(0, 95, 0.01);
        controls.target.set(0, 0, 0);
        break;
      case 'FRONT':
        camera.position.set(0, 15, 80);
        controls.target.set(0, 10, 0);
        break;
      case 'SIDE':
        camera.position.set(80, 15, 0);
        controls.target.set(0, 10, 0);
        break;
      case 'PERSPECTIVE':
      case 'RESET':
      default:
        camera.position.set(50, 40, 60);
        controls.target.set(0, 8, 0);
        break;
    }
    controls.update();
  }, [cameraPreset, controlsRef]);

  return null;
};

export const BuildingScene: React.FC<BuildingSceneProps> = ({
  building,
  selectedFloorId,
  displayMode,
  selectedRoomId,
  onSelectRoom,
  cameraPreset,
  explodedGap,
  wireframe,
  routes = [],
  showRoutes = true,
}) => {
  const controlsRef = useRef<OrbitControlsImpl>(null);

  // Determine active floors to display
  const isAllFloors = selectedFloorId === 'ALL';
  const visibleFloors = isAllFloors
    ? building.floors
    : building.floors.filter((fl) => fl.id === selectedFloorId);

  return (
    <div className="w-full h-full relative bg-[#060a12] select-none overflow-hidden rounded-2xl">
      <Canvas
        shadows
        camera={{ position: [50, 40, 60], fov: 42, near: 0.1, far: 500 }}
        className="w-full h-full"
      >
        {/* Environmental Lighting */}
        <ambientLight intensity={0.8} />
        <directionalLight
          position={[40, 70, 40]}
          intensity={1.4}
          castShadow
          shadow-mapSize-width={1024}
          shadow-mapSize-height={1024}
        />
        <directionalLight position={[-40, 30, -40]} intensity={0.4} color="#94a3b8" />
        <pointLight position={[0, 20, 0]} intensity={0.3} color="#38bdf8" />

        {/* Orbit Camera Controls */}
        <OrbitControls
          ref={controlsRef}
          enableDamping
          dampingFactor={0.06}
          minDistance={10}
          maxDistance={220}
          maxPolarAngle={Math.PI / 2 + 0.05}
        />

        {/* Preset Camera Transition Listener */}
        <CameraController cameraPreset={cameraPreset} controlsRef={controlsRef} />

        {/* Dynamic Structural Building Assembly */}
        <group position={[0, 0, 0]}>
          {visibleFloors.map((floor) => {
            // Compute vertical elevation including user-controlled explosion distance
            const floorElevation = isAllFloors
              ? floor.floorNumber * (5.5 + explodedGap)
              : 0;

            return (
              <group key={floor.id}>
                {/* Structural Floor Plate Slab */}
                <FloorSlab
                  floor={floor}
                  floorElevation={floorElevation}
                  isIsolated={!isAllFloors}
                />

                {/* Floor's Component Rooms */}
                {floor.rooms.map((room) => (
                  <RoomMesh
                    key={room.id}
                    room={room}
                    floorElevation={floorElevation}
                    displayMode={displayMode}
                    isSelected={room.id === selectedRoomId}
                    onSelect={onSelectRoom}
                    wireframe={wireframe}
                  />
                ))}
              </group>
            );
          })}

          {/* 3D Dynamic Evacuation Routes */}
          {(displayMode === 'EMERGENCY' || showRoutes) &&
            routes.map((rt) => (
              <EvacuationRouteMesh
                key={rt.id}
                route={rt}
                isAllFloors={isAllFloors}
                explodedGap={explodedGap}
                highlighted={selectedRoomId === rt.originRoomId}
              />
            ))}
        </group>
      </Canvas>
    </div>
  );
};

