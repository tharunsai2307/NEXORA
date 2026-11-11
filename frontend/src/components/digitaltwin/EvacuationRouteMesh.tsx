import React, { useMemo } from 'react';
import * as THREE from 'three';
import { Html } from '@react-three/drei';
import { EvacuationRoute, RouteWaypoint } from '../../types/emergency';
import { LogOut, AlertOctagon } from 'lucide-react';

interface EvacuationRouteMeshProps {
  route: EvacuationRoute;
  isAllFloors: boolean;
  explodedGap: number;
  highlighted?: boolean;
}

export const EvacuationRouteMesh: React.FC<EvacuationRouteMeshProps> = ({
  route,
  isAllFloors,
  explodedGap,
  highlighted = false,
}) => {
  const waypoints = route.orderedWaypoints || [];

  // Compute 3D points
  const points = useMemo(() => {
    if (waypoints.length < 2) return [];
    return waypoints.map((wp: RouteWaypoint) => {
      const elev = isAllFloors ? wp.floorNumber * (5.5 + explodedGap) : 0;
      return new THREE.Vector3(wp.posX, elev + 0.9, wp.posZ);
    });
  }, [waypoints, isAllFloors, explodedGap]);

  // Create smooth CatmullRom curve geometry
  const tubeGeometry = useMemo(() => {
    if (points.length < 2) return null;
    const curve = new THREE.CatmullRomCurve3(points);
    return new THREE.TubeGeometry(curve, points.length * 10, highlighted ? 0.35 : 0.2, 8, false);
  }, [points, highlighted]);

  if (waypoints.length < 2 || !tubeGeometry) {
    if (route.routeStatus === 'NO_SAFE_ROUTE' && waypoints.length > 0) {
      const first = waypoints[0];
      const elev = isAllFloors ? first.floorNumber * (5.5 + explodedGap) : 0;
      return (
        <group position={[first.posX, elev + 2.0, first.posZ]}>
          <Html center distanceFactor={25}>
            <div className="bg-red-950/90 border border-red-500 text-red-300 text-[10px] px-2 py-1 rounded shadow-lg flex items-center gap-1 font-mono">
              <AlertOctagon className="w-3.5 h-3.5 text-red-400" />
              NO SAFE ROUTE
            </div>
          </Html>
        </group>
      );
    }
    return null;
  }

  const isBlocked = route.routeStatus === 'NO_SAFE_ROUTE';
  const isCaution = route.routeStatus === 'CAUTION';
  const routeColor = isBlocked ? '#dc2626' : (isCaution ? '#f59e0b' : '#06b6d4');
  const exitPoint = points[points.length - 1];

  return (
    <group>
      {/* 3D Egress Path Tube */}
      <mesh geometry={tubeGeometry}>
        <meshStandardMaterial
          color={routeColor}
          emissive={routeColor}
          emissiveIntensity={highlighted ? 0.9 : 0.5}
          roughness={0.2}
          metalness={0.1}
          transparent
          opacity={highlighted ? 0.95 : 0.75}
        />
      </mesh>

      {/* Destination Exit Marker */}
      {exitPoint && (
        <group position={[exitPoint.x, exitPoint.y + 0.5, exitPoint.z]}>
          <mesh>
            <cylinderGeometry args={[0.6, 0.6, 0.4, 16]} />
            <meshStandardMaterial color="#10b981" emissive="#10b981" emissiveIntensity={0.8} />
          </mesh>
          <Html center distanceFactor={22}>
            <div className="bg-emerald-950/90 border border-emerald-500 text-emerald-300 text-[10px] px-2 py-1 rounded shadow-lg flex items-center gap-1 font-mono font-bold whitespace-nowrap">
              <LogOut className="w-3.5 h-3.5 text-emerald-400" />
              {route.destinationExitName || 'EMERGENCY EXIT'}
            </div>
          </Html>
        </group>
      )}
    </group>
  );
};
