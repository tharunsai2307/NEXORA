import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { buildingApi, floorApi, roomApi } from '../services/api';
import { Building, Floor, Room, RoomPayload, RoomType, SafetyStatus } from '../types/building';
import { useAuth } from '../context/AuthContext';
import {
  DoorOpen,
  ArrowLeft,
  ChevronRight,
  Plus,
  Trash2,
  Edit2,
  RefreshCw,
  AlertCircle,
  X,
  Check,
  ShieldCheck,
  ShieldAlert,
  ShieldX,
  Maximize2,
  Compass,
} from 'lucide-react';

export const FloorDetailPage: React.FC = () => {
  const { buildingId, floorId } = useParams<{ buildingId: string; floorId: string }>();
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);

  const [building, setBuilding] = useState<Building | null>(null);
  const [floor, setFloor] = useState<Floor | null>(null);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Room Modal (Create & Edit)
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRoomId, setEditingRoomId] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [roomForm, setRoomForm] = useState<RoomPayload>({
    roomName: '',
    roomCode: '',
    roomType: 'OFFICE',
    description: '',
    maximumCapacity: 20,
    currentOccupancy: 0,
    safetyStatus: 'NORMAL',
    posX: 0.0,
    posY: 0.0,
    posZ: 0.0,
    width: 10.0,
    depth: 8.0,
    height: 3.5,
    active: true,
  });

  const loadData = async () => {
    if (!buildingId || !floorId) return;
    setIsLoading(true);
    setError(null);
    try {
      const [bData, fData, rData] = await Promise.all([
        buildingApi.getBuilding(buildingId),
        floorApi.getFloor(floorId),
        roomApi.getRooms(floorId),
      ]);
      setBuilding(bData);
      setFloor(fData);
      setRooms(rData);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load floor spatial telemetry');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [buildingId, floorId]);

  const handleOpenCreateModal = () => {
    setEditingRoomId(null);
    setRoomForm({
      roomName: '',
      roomCode: `RM-${floor ? floor.floorNumber : 0}${rooms.length + 1 < 10 ? '0' : ''}${rooms.length + 1}`,
      roomType: 'OFFICE',
      description: '',
      maximumCapacity: 20,
      currentOccupancy: 0,
      safetyStatus: 'NORMAL',
      posX: rooms.length * 12.0,
      posY: 0.0,
      posZ: 0.0,
      width: 10.0,
      depth: 8.0,
      height: 3.5,
      active: true,
    });
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (room: Room) => {
    setEditingRoomId(room.id);
    setRoomForm({
      roomName: room.roomName,
      roomCode: room.roomCode,
      roomType: room.roomType,
      description: room.description || '',
      maximumCapacity: room.maximumCapacity,
      currentOccupancy: room.currentOccupancy,
      safetyStatus: room.safetyStatus,
      posX: room.posX,
      posY: room.posY,
      posZ: room.posZ,
      width: room.width,
      depth: room.depth,
      height: room.height,
      active: room.active,
    });
    setIsModalOpen(true);
  };

  const handleSubmitRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!floorId) return;
    setIsSubmitting(true);
    setError(null);
    try {
      if (editingRoomId) {
        await roomApi.updateRoom(editingRoomId, roomForm);
      } else {
        await roomApi.createRoom(floorId, roomForm);
      }
      setIsModalOpen(false);
      await loadData();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to save room coordinates');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteRoom = async (roomId: string, roomName: string) => {
    if (!window.confirm(`Delete room "${roomName}"? This action cannot be undone.`)) {
      return;
    }
    setError(null);
    try {
      await roomApi.deleteRoom(roomId);
      setRooms((prev) => prev.filter((r) => r.id !== roomId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete room');
    }
  };

  const getSafetyBadge = (status: SafetyStatus) => {
    switch (status) {
      case 'NORMAL':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <ShieldCheck className="w-3 h-3" />
            NORMAL
          </span>
        );
      case 'WARNING':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono bg-amber-500/10 text-amber-400 border border-amber-500/30">
            <ShieldAlert className="w-3 h-3" />
            WARNING
          </span>
        );
      case 'CRITICAL':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono bg-red-500/10 text-red-400 border border-red-500/30 animate-pulse">
            <ShieldX className="w-3 h-3" />
            CRITICAL
          </span>
        );
    }
  };

  if (isLoading && !floor) {
    return (
      <div className="py-24 text-center text-slate-400">
        <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mx-auto mb-3" />
        <p className="text-xs font-mono">Loading floor level geometry...</p>
      </div>
    );
  }

  if (!building || !floor) {
    return (
      <div className="py-20 text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-red-400 mx-auto" />
        <h2 className="text-lg font-bold text-slate-100">Floor Not Found</h2>
        <Link to="/buildings" className="inline-flex items-center gap-1.5 text-xs text-emerald-400">
          <ArrowLeft className="w-4 h-4" /> Return to Building Directory
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Breadcrumb Navigation */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
          <Link to="/buildings" className="hover:text-emerald-400 transition-colors">
            Buildings
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
          <Link to={`/buildings/${building.id}`} className="hover:text-emerald-400 transition-colors">
            {building.buildingCode}
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
          <span className="text-slate-200 font-medium">Level {floor.floorNumber}</span>
        </div>

        <button
          onClick={loadData}
          className="p-1.5 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors"
          title="Refresh"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="p-3.5 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-2.5 text-xs text-red-300">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Floor Overview Card */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-xl">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="text-xs font-mono font-medium px-2.5 py-0.5 rounded-full bg-cyan-500/10 border border-cyan-500/30 text-cyan-400">
                LEVEL {floor.floorNumber}
              </span>
              <span className="text-xs font-mono text-slate-400">
                {building.name} ({building.buildingCode})
              </span>
            </div>
            <h1 className="text-2xl font-extrabold text-slate-100 tracking-tight">
              {floor.floorName}
            </h1>
            <p className="text-xs text-slate-400 max-w-xl">
              {floor.description || `Spatial Boundaries: ${floor.layoutWidth}m (width) × ${floor.layoutDepth}m (depth)`}
            </p>
          </div>

          <div className="grid grid-cols-3 gap-3 font-mono">
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase">Rooms</div>
              <div className="text-xl font-bold text-emerald-400 mt-1">{rooms.length}</div>
            </div>
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase">Capacity</div>
              <div className="text-xl font-bold text-purple-400 mt-1">
                {rooms.reduce((acc, r) => acc + r.maximumCapacity, 0)}
              </div>
            </div>
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase">Occupancy</div>
              <div className="text-xl font-bold text-amber-400 mt-1">
                {rooms.reduce((acc, r) => acc + r.currentOccupancy, 0)}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Rooms Grid Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-slate-100 flex items-center gap-2">
              <DoorOpen className="w-5 h-5 text-emerald-400" />
              Room Partitions & 3D Twin Coordinates
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Configured physical dimensions and spatial origins for 3D digital twin rendering.
            </p>
          </div>

          {canManage && (
            <button
              onClick={handleOpenCreateModal}
              className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Add Room</span>
            </button>
          )}
        </div>

        {rooms.length === 0 ? (
          <div className="py-16 text-center bg-slate-900/40 border border-slate-800/60 rounded-2xl">
            <DoorOpen className="w-8 h-8 text-slate-600 mx-auto mb-2 opacity-40" />
            <h3 className="text-xs font-semibold text-slate-300">No rooms partitioned yet</h3>
            <p className="text-[11px] text-slate-500 mt-1 max-w-sm mx-auto">
              Add rooms with width, depth, height, and coordinates to model this floor.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {rooms.map((r) => (
              <div
                key={r.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700 rounded-xl p-4 shadow-lg transition-all flex flex-col justify-between group"
              >
                <div>
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div>
                      <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        {r.roomCode}
                      </span>
                      <h3 className="text-sm font-bold text-slate-100 group-hover:text-emerald-300 transition-colors mt-1.5">
                        {r.roomName}
                      </h3>
                    </div>
                    {getSafetyBadge(r.safetyStatus)}
                  </div>

                  <p className="text-xs text-slate-400 line-clamp-1 mb-3">
                    {r.description || `Type: ${r.roomType}`}
                  </p>

                  {/* 3D Coordinates Pill */}
                  <div className="p-2.5 bg-slate-950/70 border border-slate-800/80 rounded-lg space-y-1.5 font-mono text-[11px] mb-3">
                    <div className="flex items-center justify-between text-slate-400">
                      <span className="flex items-center gap-1 text-cyan-400">
                        <Compass className="w-3 h-3" />
                        3D Origin (X, Y, Z):
                      </span>
                      <span className="text-slate-200">
                        [{r.posX.toFixed(1)}, {r.posY.toFixed(1)}, {r.posZ.toFixed(1)}]
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-slate-400">
                      <span className="flex items-center gap-1 text-purple-400">
                        <Maximize2 className="w-3 h-3" />
                        Dimensions (W×D×H):
                      </span>
                      <span className="text-slate-200">
                        {r.width.toFixed(1)}m × {r.depth.toFixed(1)}m × {r.height.toFixed(1)}m
                      </span>
                    </div>
                  </div>

                  {/* Occupancy Progress */}
                  <div className="space-y-1">
                    <div className="flex items-center justify-between text-[11px] font-mono text-slate-400">
                      <span>Occupancy</span>
                      <span className="text-slate-200">
                        {r.currentOccupancy} / {r.maximumCapacity}
                      </span>
                    </div>
                    <div className="w-full bg-slate-950 rounded-full h-1.5 overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          r.currentOccupancy > r.maximumCapacity
                            ? 'bg-red-500'
                            : r.currentOccupancy / r.maximumCapacity > 0.8
                            ? 'bg-amber-400'
                            : 'bg-emerald-400'
                        }`}
                        style={{
                          width: `${Math.min(100, (r.currentOccupancy / r.maximumCapacity) * 100)}%`,
                        }}
                      />
                    </div>
                  </div>
                </div>

                {/* Footer Controls */}
                <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between">
                  <span className="text-[10px] font-mono text-slate-500 uppercase">
                    {r.roomType}
                  </span>

                  {canManage && (
                    <div className="flex items-center gap-1.5">
                      <button
                        onClick={() => handleOpenEditModal(r)}
                        className="p-1.5 text-slate-400 hover:text-white hover:bg-slate-800 rounded transition-colors"
                        title="Edit Spatial Coordinates"
                      >
                        <Edit2 className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteRoom(r.id, r.roomName)}
                        className="p-1.5 text-slate-500 hover:text-red-400 hover:bg-red-500/10 rounded transition-colors"
                        title="Delete Room"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Room Modal (Create/Edit) */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-xl p-6 shadow-2xl space-y-4 my-8">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-base font-bold text-slate-100 flex items-center gap-2">
                <DoorOpen className="w-5 h-5 text-emerald-400" />
                {editingRoomId ? 'Edit Room Spatial Properties' : 'Create Room on Level ' + floor.floorNumber}
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSubmitRoom} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Room Name</label>
                  <input
                    type="text"
                    required
                    value={roomForm.roomName}
                    onChange={(e) => setRoomForm({ ...roomForm, roomName: e.target.value })}
                    placeholder="Engineering Pod Alpha"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Room Code</label>
                  <input
                    type="text"
                    required
                    value={roomForm.roomCode}
                    onChange={(e) => setRoomForm({ ...roomForm, roomCode: e.target.value })}
                    placeholder="ENG-101"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 uppercase font-mono outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Room Type</label>
                  <select
                    value={roomForm.roomType}
                    onChange={(e) => setRoomForm({ ...roomForm, roomType: e.target.value as RoomType })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                  >
                    <option value="OFFICE">Office</option>
                    <option value="CONFERENCE_ROOM">Conference Room</option>
                    <option value="LABORATORY">Laboratory</option>
                    <option value="CLASSROOM">Classroom</option>
                    <option value="SERVER_ROOM">Server Room</option>
                    <option value="LOBBY">Lobby</option>
                    <option value="CAFETERIA">Cafeteria</option>
                    <option value="CORRIDOR">Corridor</option>
                    <option value="STAIRCASE">Staircase</option>
                    <option value="STORAGE_ROOM">Storage</option>
                    <option value="EMERGENCY_EXIT_AREA">Emergency Exit</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Max Capacity</label>
                  <input
                    type="number"
                    min="1"
                    required
                    value={roomForm.maximumCapacity}
                    onChange={(e) => setRoomForm({ ...roomForm, maximumCapacity: parseInt(e.target.value) || 1 })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 font-mono outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Current Occupancy</label>
                  <input
                    type="number"
                    min="0"
                    value={roomForm.currentOccupancy}
                    onChange={(e) => setRoomForm({ ...roomForm, currentOccupancy: parseInt(e.target.value) || 0 })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 font-mono outline-none"
                  />
                </div>
              </div>

              {/* Physical Dimensions */}
              <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl space-y-2">
                <div className="text-[11px] font-mono text-purple-400 flex items-center gap-1.5">
                  <Maximize2 className="w-3.5 h-3.5" />
                  <span>Physical Dimensions (meters)</span>
                </div>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">Width (m)</label>
                    <input
                      type="number"
                      step="0.5"
                      min="0.5"
                      required
                      value={roomForm.width}
                      onChange={(e) => setRoomForm({ ...roomForm, width: parseFloat(e.target.value) || 10 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">Depth (m)</label>
                    <input
                      type="number"
                      step="0.5"
                      min="0.5"
                      required
                      value={roomForm.depth}
                      onChange={(e) => setRoomForm({ ...roomForm, depth: parseFloat(e.target.value) || 8 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">Height (m)</label>
                    <input
                      type="number"
                      step="0.5"
                      min="0.5"
                      required
                      value={roomForm.height}
                      onChange={(e) => setRoomForm({ ...roomForm, height: parseFloat(e.target.value) || 3.5 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                </div>
              </div>

              {/* 3D Spatial Origin Coordinates */}
              <div className="p-3 bg-slate-950/60 border border-slate-800 rounded-xl space-y-2">
                <div className="text-[11px] font-mono text-cyan-400 flex items-center gap-1.5">
                  <Compass className="w-3.5 h-3.5" />
                  <span>3D Digital Twin Spatial Origin Coordinates (X, Y, Z)</span>
                </div>
                <div className="grid grid-cols-3 gap-3">
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">X Coordinate</label>
                    <input
                      type="number"
                      step="0.5"
                      required
                      value={roomForm.posX}
                      onChange={(e) => setRoomForm({ ...roomForm, posX: parseFloat(e.target.value) || 0 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">Y Coordinate (Elevation)</label>
                    <input
                      type="number"
                      step="0.5"
                      required
                      value={roomForm.posY}
                      onChange={(e) => setRoomForm({ ...roomForm, posY: parseFloat(e.target.value) || 0 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                  <div>
                    <label className="block text-[10px] text-slate-400 mb-0.5">Z Coordinate</label>
                    <input
                      type="number"
                      step="0.5"
                      required
                      value={roomForm.posZ}
                      onChange={(e) => setRoomForm({ ...roomForm, posZ: parseFloat(e.target.value) || 0 })}
                      className="w-full px-2.5 py-1.5 bg-slate-900 border border-slate-800 rounded text-xs text-slate-100 font-mono outline-none"
                    />
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200 bg-slate-800 rounded-lg transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all flex items-center gap-1.5"
                >
                  {isSubmitting ? (
                    <>
                      <div className="w-3.5 h-3.5 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                      <span>Saving...</span>
                    </>
                  ) : (
                    <>
                      <Check className="w-3.5 h-3.5" />
                      <span>{editingRoomId ? 'Save Changes' : 'Create Room'}</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

