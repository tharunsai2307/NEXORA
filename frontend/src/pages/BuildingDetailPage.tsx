import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { buildingApi, floorApi, userApi } from '../services/api';
import { Building, Floor, FloorPayload, FloorStatus } from '../types/building';
import { User } from '../types/auth';
import { useAuth } from '../context/AuthContext';
import {
  Layers,
  DoorOpen,
  Users,
  Plus,
  ChevronRight,
  ArrowLeft,
  Trash2,
  RefreshCw,
  AlertCircle,
  X,
  Check,
  UserCheck,
  UserPlus,
} from 'lucide-react';

export const BuildingDetailPage: React.FC = () => {
  const { buildingId } = useParams<{ buildingId: string }>();
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);
  const isAdmin = hasRole('SYSTEM_ADMIN');

  const [building, setBuilding] = useState<Building | null>(null);
  const [floors, setFloors] = useState<Floor[]>([]);
  const [assignedUsers, setAssignedUsers] = useState<User[]>([]);
  const [allUsers, setAllUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Floor modal
  const [isFloorModalOpen, setIsFloorModalOpen] = useState(false);
  const [isFloorSubmitting, setIsFloorSubmitting] = useState(false);
  const [floorForm, setFloorForm] = useState<FloorPayload>({
    floorName: '',
    floorNumber: 0,
    description: '',
    layoutWidth: 60.0,
    layoutDepth: 40.0,
    status: 'ACTIVE',
  });

  // Assign user modal
  const [isAssignModalOpen, setIsAssignModalOpen] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState('');

  const loadData = async () => {
    if (!buildingId) return;
    setIsLoading(true);
    setError(null);
    try {
      const [bData, fData, uData] = await Promise.all([
        buildingApi.getBuilding(buildingId),
        floorApi.getFloors(buildingId),
        buildingApi.getAssignedUsers(buildingId),
      ]);
      setBuilding(bData);
      setFloors(fData);
      setAssignedUsers(uData);

      if (isAdmin) {
        const directory = await userApi.listUsers(undefined, undefined, true);
        setAllUsers(directory);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load building topology');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [buildingId]);

  const handleCreateFloor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!buildingId) return;
    setIsFloorSubmitting(true);
    setError(null);
    try {
      await floorApi.createFloor(buildingId, floorForm);
      setIsFloorModalOpen(false);
      setFloorForm({
        floorName: '',
        floorNumber: (floors.length > 0 ? Math.max(...floors.map((f) => f.floorNumber)) + 1 : 0),
        description: '',
        layoutWidth: 60.0,
        layoutDepth: 40.0,
        status: 'ACTIVE',
      });
      await loadData();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register floor');
    } finally {
      setIsFloorSubmitting(false);
    }
  };

  const handleDeleteFloor = async (floorId: string, floorName: string) => {
    if (!window.confirm(`Delete floor "${floorName}"? This action requires that all rooms on this floor be deleted first.`)) {
      return;
    }
    setError(null);
    try {
      await floorApi.deleteFloor(floorId);
      setFloors((prev) => prev.filter((f) => f.id !== floorId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete floor');
    }
  };

  const handleAssignUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!buildingId || !selectedUserId) return;
    try {
      await buildingApi.assignUser(buildingId, selectedUserId);
      setIsAssignModalOpen(false);
      setSelectedUserId('');
      const updatedAssigned = await buildingApi.getAssignedUsers(buildingId);
      setAssignedUsers(updatedAssigned);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to assign operator');
    }
  };

  const handleUnassignUser = async (userId: string) => {
    if (!buildingId) return;
    try {
      await buildingApi.unassignUser(buildingId, userId);
      setAssignedUsers((prev) => prev.filter((u) => u.id !== userId));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to unassign operator');
    }
  };

  if (isLoading && !building) {
    return (
      <div className="py-24 text-center text-slate-400">
        <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mx-auto mb-3" />
        <p className="text-xs font-mono">Loading building spatial structure...</p>
      </div>
    );
  }

  if (!building) {
    return (
      <div className="py-20 text-center space-y-4">
        <AlertCircle className="w-10 h-10 text-red-400 mx-auto" />
        <h2 className="text-lg font-bold text-slate-100">Facility Not Found</h2>
        <Link to="/buildings" className="inline-flex items-center gap-1.5 text-xs text-emerald-400">
          <ArrowLeft className="w-4 h-4" /> Return to Building Directory
        </Link>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Breadcrumb & Navigation */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-xs font-mono text-slate-400">
          <Link to="/buildings" className="hover:text-emerald-400 transition-colors flex items-center gap-1">
            <ArrowLeft className="w-3.5 h-3.5" />
            Buildings
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-slate-600" />
          <span className="text-slate-200 font-medium">{building.buildingCode}</span>
        </div>

        <button
          onClick={loadData}
          className="p-1.5 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors"
          title="Refresh"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Error alert */}
      {error && (
        <div className="p-3.5 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-2.5 text-xs text-red-300">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Building Overview Header */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-xl">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-3">
              <span className="text-xs font-mono font-medium px-2.5 py-0.5 rounded-full bg-emerald-500/10 border border-emerald-500/30 text-emerald-400">
                {building.buildingCode}
              </span>
              <span className="text-xs font-mono text-slate-400 uppercase">
                {building.buildingType}
              </span>
            </div>
            <h1 className="text-2xl font-extrabold text-slate-100 tracking-tight">
              {building.name}
            </h1>
            <p className="text-xs text-slate-400 max-w-xl">
              {building.description || building.address}
            </p>
          </div>

          {/* Quick Metrics */}
          <div className="grid grid-cols-3 gap-3 font-mono">
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase tracking-wider">Floors</div>
              <div className="text-xl font-bold text-cyan-400 mt-1">{floors.length}</div>
            </div>
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase tracking-wider">Rooms</div>
              <div className="text-xl font-bold text-emerald-400 mt-1">{building.roomCount}</div>
            </div>
            <div className="bg-slate-950/70 border border-slate-800/80 rounded-xl p-3.5 text-center">
              <div className="text-[10px] text-slate-500 uppercase tracking-wider">Capacity</div>
              <div className="text-xl font-bold text-purple-400 mt-1">{building.totalCapacity}</div>
            </div>
          </div>
        </div>

        {/* Assigned Operators Sub-row */}
        <div className="mt-6 pt-5 border-t border-slate-800/80 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono text-slate-400">Assigned Operators:</span>
            <div className="flex items-center gap-1.5 flex-wrap">
              {assignedUsers.length === 0 ? (
                <span className="text-xs text-slate-500 italic">Global access (unrestricted)</span>
              ) : (
                assignedUsers.map((u) => (
                  <span
                    key={u.id}
                    className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-mono bg-slate-800 border border-slate-700 text-slate-300"
                  >
                    <UserCheck className="w-3 h-3 text-emerald-400" />
                    <span>{u.fullName}</span>
                    {isAdmin && (
                      <button
                        onClick={() => handleUnassignUser(u.id)}
                        className="hover:text-red-400 ml-0.5"
                        title="Remove Assignment"
                      >
                        ×
                      </button>
                    )}
                  </span>
                ))
              )}
            </div>
          </div>

          {isAdmin && (
            <button
              onClick={() => setIsAssignModalOpen(true)}
              className="inline-flex items-center gap-1.5 text-xs text-slate-300 hover:text-white px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 border border-slate-700 transition-colors self-start"
            >
              <UserPlus className="w-3.5 h-3.5" />
              <span>Assign Manager</span>
            </button>
          )}
        </div>
      </div>

      {/* Floors Section */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold text-slate-100 flex items-center gap-2">
              <Layers className="w-5 h-5 text-cyan-400" />
              Vertical Floor Hierarchy
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Select a floor level to view and configure room 3D coordinates and capacities.
            </p>
          </div>

          {canManage && (
            <button
              onClick={() => {
                setFloorForm({
                  floorName: `Level ${floors.length}`,
                  floorNumber: floors.length > 0 ? Math.max(...floors.map((f) => f.floorNumber)) + 1 : 0,
                  description: '',
                  layoutWidth: 60.0,
                  layoutDepth: 40.0,
                  status: 'ACTIVE',
                });
                setIsFloorModalOpen(true);
              }}
              className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Add Floor Level</span>
            </button>
          )}
        </div>

        {floors.length === 0 ? (
          <div className="py-16 text-center bg-slate-900/40 border border-slate-800/60 rounded-2xl">
            <Layers className="w-8 h-8 text-slate-600 mx-auto mb-2 opacity-40" />
            <h3 className="text-xs font-semibold text-slate-300">No floors defined</h3>
            <p className="text-[11px] text-slate-500 mt-1 max-w-sm mx-auto">
              Add vertical levels to this structure to organize spaces and rooms.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {floors.map((f) => (
              <div
                key={f.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700 rounded-xl p-4 shadow-lg transition-all flex flex-col justify-between group"
              >
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-cyan-500/10 text-cyan-400 border border-cyan-500/20">
                      LEVEL {f.floorNumber}
                    </span>
                    <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-slate-800 border border-slate-700 text-slate-300">
                      {f.status}
                    </span>
                  </div>

                  <h3 className="text-sm font-bold text-slate-100 group-hover:text-cyan-300 transition-colors">
                    {f.floorName}
                  </h3>
                  <p className="text-xs text-slate-400 mt-1 line-clamp-2">
                    {f.description || `Spatial footprint: ${f.layoutWidth}m × ${f.layoutDepth}m`}
                  </p>

                  {/* Footprint metrics */}
                  <div className="grid grid-cols-2 gap-2 mt-3 p-2 bg-slate-950/60 border border-slate-800/60 rounded-lg text-xs font-mono">
                    <div className="flex items-center gap-1.5 text-slate-300">
                      <DoorOpen className="w-3.5 h-3.5 text-emerald-400" />
                      <span>{f.roomCount} Rooms</span>
                    </div>
                    <div className="flex items-center gap-1.5 text-slate-300">
                      <Users className="w-3.5 h-3.5 text-purple-400" />
                      <span>Cap: {f.totalCapacity}</span>
                    </div>
                  </div>
                </div>

                <div className="mt-4 pt-3 border-t border-slate-800/80 flex items-center justify-between">
                  {canManage && (
                    <button
                      onClick={() => handleDeleteFloor(f.id, f.floorName)}
                      className="p-1 text-slate-500 hover:text-red-400 hover:bg-red-500/10 rounded transition-colors"
                      title="Delete Floor"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  )}

                  <Link
                    to={`/buildings/${building.id}/floors/${f.id}`}
                    className="inline-flex items-center gap-1 text-xs font-medium text-cyan-400 hover:text-cyan-300 py-1 px-2.5 rounded bg-cyan-500/10 hover:bg-cyan-500/20 border border-cyan-500/20 transition-all ml-auto"
                  >
                    <span>Manage Rooms</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Floor Modal */}
      {isFloorModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-md p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-base font-bold text-slate-100 flex items-center gap-2">
                <Layers className="w-5 h-5 text-cyan-400" />
                Add Floor to {building.name}
              </h3>
              <button
                onClick={() => setIsFloorModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreateFloor} className="space-y-3.5">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Floor Name</label>
                  <input
                    type="text"
                    required
                    value={floorForm.floorName}
                    onChange={(e) => setFloorForm({ ...floorForm, floorName: e.target.value })}
                    placeholder="Level 2 - Engineering"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Floor Number (Index)</label>
                  <input
                    type="number"
                    required
                    value={floorForm.floorNumber}
                    onChange={(e) => setFloorForm({ ...floorForm, floorNumber: parseInt(e.target.value) || 0 })}
                    placeholder="2"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 font-mono outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Width (meters)</label>
                  <input
                    type="number"
                    step="0.5"
                    required
                    value={floorForm.layoutWidth}
                    onChange={(e) => setFloorForm({ ...floorForm, layoutWidth: parseFloat(e.target.value) || 60 })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 font-mono outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Depth (meters)</label>
                  <input
                    type="number"
                    step="0.5"
                    required
                    value={floorForm.layoutDepth}
                    onChange={(e) => setFloorForm({ ...floorForm, layoutDepth: parseFloat(e.target.value) || 40 })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 font-mono outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Status</label>
                <select
                  value={floorForm.status}
                  onChange={(e) => setFloorForm({ ...floorForm, status: e.target.value as FloorStatus })}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 outline-none"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="MAINTENANCE">Maintenance</option>
                  <option value="RESTRICTED">Restricted</option>
                  <option value="INACTIVE">Inactive</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={floorForm.description}
                  onChange={(e) => setFloorForm({ ...floorForm, description: e.target.value })}
                  placeholder="Software engineering pods and collaboration hub..."
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-cyan-500 rounded-lg text-xs text-slate-100 outline-none"
                />
              </div>

              <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => setIsFloorModalOpen(false)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200 bg-slate-800 rounded-lg transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isFloorSubmitting}
                  className="px-4 py-2 text-xs font-medium text-white bg-cyan-600 hover:bg-cyan-500 rounded-lg shadow-lg shadow-cyan-600/20 transition-all flex items-center gap-1.5"
                >
                  {isFloorSubmitting ? (
                    <>
                      <div className="w-3.5 h-3.5 border-2 border-white/20 border-t-white rounded-full animate-spin" />
                      <span>Adding...</span>
                    </>
                  ) : (
                    <>
                      <Check className="w-3.5 h-3.5" />
                      <span>Add Floor</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Assign User Modal */}
      {isAssignModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-sm p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-sm font-bold text-slate-100 flex items-center gap-2">
                <UserPlus className="w-4 h-4 text-emerald-400" />
                Assign Facility Operator
              </h3>
              <button
                onClick={() => setIsAssignModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleAssignUser} className="space-y-3.5">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Select Operator</label>
                <select
                  required
                  value={selectedUserId}
                  onChange={(e) => setSelectedUserId(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                >
                  <option value="">-- Choose Operator --</option>
                  {allUsers
                    .filter((u) => !assignedUsers.some((assigned) => assigned.id === u.id))
                    .map((u) => (
                      <option key={u.id} value={u.id}>
                        {u.fullName} ({u.role}) — {u.email}
                      </option>
                    ))}
                </select>
              </div>

              <div className="pt-3 border-t border-slate-800 flex items-center justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => setIsAssignModalOpen(false)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-slate-200 bg-slate-800 rounded-lg transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={!selectedUserId}
                  className="px-4 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 rounded-lg transition-all"
                >
                  Confirm Assignment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

