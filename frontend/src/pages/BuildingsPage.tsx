import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { buildingApi } from '../services/api';
import { Building, BuildingPayload, BuildingStatus, BuildingType } from '../types/building';
import { useAuth } from '../context/AuthContext';
import {
  Building2,
  Plus,
  Search,
  Filter,
  Layers,
  DoorOpen,
  Users,
  ChevronRight,
  AlertCircle,
  Trash2,
  RefreshCw,
  X,
  Check,
} from 'lucide-react';

export const BuildingsPage: React.FC = () => {
  const { hasRole } = useAuth();
  const canManage = hasRole(['SYSTEM_ADMIN', 'BUILDING_MANAGER']);

  const [buildings, setBuildings] = useState<Building[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<BuildingStatus | 'ALL'>('ALL');
  const [typeFilter, setTypeFilter] = useState<BuildingType | 'ALL'>('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Create Modal state
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formData, setFormData] = useState<BuildingPayload>({
    name: '',
    buildingCode: '',
    buildingType: 'COMMERCIAL',
    address: '',
    status: 'ACTIVE',
    description: '',
  });

  const fetchBuildings = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const status = statusFilter === 'ALL' ? undefined : statusFilter;
      const type = typeFilter === 'ALL' ? undefined : typeFilter;
      const data = await buildingApi.getBuildings(searchQuery || undefined, status, type);
      setBuildings(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load buildings');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBuildings();
  }, [statusFilter, typeFilter]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchBuildings();
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError(null);
    try {
      await buildingApi.createBuilding(formData);
      setIsModalOpen(false);
      setFormData({
        name: '',
        buildingCode: '',
        buildingType: 'COMMERCIAL',
        address: '',
        status: 'ACTIVE',
        description: '',
      });
      await fetchBuildings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create building');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string, name: string) => {
    if (!window.confirm(`Are you sure you want to delete building "${name}"? This action cannot be undone.`)) {
      return;
    }
    setError(null);
    try {
      await buildingApi.deleteBuilding(id);
      setBuildings((prev) => prev.filter((b) => b.id !== id));
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to delete building');
    }
  };

  const getStatusBadge = (status: BuildingStatus) => {
    switch (status) {
      case 'ACTIVE':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">ACTIVE</span>;
      case 'MAINTENANCE':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-mono bg-amber-500/10 text-amber-400 border border-amber-500/30">MAINTENANCE</span>;
      case 'EMERGENCY':
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-mono bg-red-500/10 text-red-400 border border-red-500/30 animate-pulse">EMERGENCY</span>;
      default:
        return <span className="px-2 py-0.5 rounded-full text-[10px] font-mono bg-slate-800 text-slate-400 border border-slate-700">INACTIVE</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100 flex items-center gap-2.5">
            <Building2 className="w-6 h-6 text-emerald-400" />
            Building Fleet Operations
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Real-time management and spatial hierarchy of monitored physical facilities.
          </p>
        </div>

        <div className="flex items-center gap-2.5 self-start">
          <button
            onClick={fetchBuildings}
            className="p-2 text-slate-400 hover:text-slate-200 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors"
            title="Refresh"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>

          {canManage && (
            <button
              onClick={() => setIsModalOpen(true)}
              className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
            >
              <Plus className="w-4 h-4" />
              <span>Register Building</span>
            </button>
          )}
        </div>
      </div>

      {/* Error alert */}
      {error && (
        <div className="p-3.5 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-2.5 text-xs text-red-300">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
          <span>{error}</span>
        </div>
      )}

      {/* Filter and Search controls */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearch} className="flex-1 relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search by building name or code (e.g., NEX-TWR-01)..."
            className="w-full pl-9 pr-4 py-2 bg-slate-950/60 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 placeholder-slate-500 outline-none"
          />
        </form>

        <div className="flex items-center gap-2 flex-wrap">
          <div className="flex items-center gap-1.5 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5 text-slate-500" />
            <span>Type:</span>
          </div>
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value as any)}
            className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 outline-none focus:border-emerald-500"
          >
            <option value="ALL">All Types</option>
            <option value="COMMERCIAL">Commercial</option>
            <option value="EDUCATIONAL">Educational</option>
            <option value="INDUSTRIAL">Industrial</option>
            <option value="RESIDENTIAL">Residential</option>
            <option value="HEALTHCARE">Healthcare</option>
            <option value="OTHER">Other</option>
          </select>

          <div className="flex items-center gap-1.5 text-xs text-slate-400 ml-2">
            <span>Status:</span>
          </div>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value as any)}
            className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 outline-none focus:border-emerald-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="MAINTENANCE">Maintenance</option>
            <option value="EMERGENCY">Emergency</option>
            <option value="INACTIVE">Inactive</option>
          </select>
        </div>
      </div>

      {/* Buildings Grid */}
      {isLoading ? (
        <div className="py-20 text-center text-slate-400">
          <div className="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mx-auto mb-3" />
          <p className="text-xs font-mono">Querying facility topology...</p>
        </div>
      ) : buildings.length === 0 ? (
        <div className="py-20 text-center bg-slate-900/40 border border-slate-800/60 rounded-2xl">
          <Building2 className="w-10 h-10 text-slate-600 mx-auto mb-3 opacity-40" />
          <h3 className="text-sm font-semibold text-slate-300">No buildings found</h3>
          <p className="text-xs text-slate-500 mt-1 max-w-sm mx-auto">
            No physical structures match your current query or filter selection.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {buildings.map((b) => (
            <div
              key={b.id}
              className="bg-slate-900/80 border border-slate-800 hover:border-slate-700/80 rounded-2xl p-5 shadow-xl transition-all flex flex-col justify-between group"
            >
              <div>
                <div className="flex items-start justify-between gap-3 mb-3">
                  <div>
                    <span className="font-mono text-[11px] text-emerald-400 font-medium px-2 py-0.5 rounded bg-emerald-500/10 border border-emerald-500/20">
                      {b.buildingCode}
                    </span>
                    <h3 className="text-base font-bold text-slate-100 mt-2 group-hover:text-emerald-300 transition-colors">
                      {b.name}
                    </h3>
                  </div>
                  {getStatusBadge(b.status)}
                </div>

                <p className="text-xs text-slate-400 mb-4 line-clamp-2">
                  {b.description || b.address}
                </p>

                {/* Metrics */}
                <div className="grid grid-cols-3 gap-2 py-3 px-3.5 bg-slate-950/60 border border-slate-800/80 rounded-xl mb-4 font-mono text-xs">
                  <div>
                    <div className="text-[10px] text-slate-500 flex items-center gap-1">
                      <Layers className="w-3 h-3 text-cyan-400" />
                      FLOORS
                    </div>
                    <div className="text-sm font-bold text-slate-200 mt-0.5">{b.totalFloors}</div>
                  </div>
                  <div>
                    <div className="text-[10px] text-slate-500 flex items-center gap-1">
                      <DoorOpen className="w-3 h-3 text-emerald-400" />
                      ROOMS
                    </div>
                    <div className="text-sm font-bold text-slate-200 mt-0.5">{b.roomCount}</div>
                  </div>
                  <div>
                    <div className="text-[10px] text-slate-500 flex items-center gap-1">
                      <Users className="w-3 h-3 text-purple-400" />
                      CAPACITY
                    </div>
                    <div className="text-sm font-bold text-slate-200 mt-0.5">{b.totalCapacity}</div>
                  </div>
                </div>
              </div>

              {/* Card Footer Actions */}
              <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between">
                <span className="text-[10px] font-mono text-slate-500 uppercase">
                  {b.buildingType}
                </span>

                <div className="flex items-center gap-2">
                  {canManage && (
                    <button
                      onClick={() => handleDelete(b.id, b.name)}
                      className="p-1.5 text-slate-500 hover:text-red-400 hover:bg-red-500/10 rounded transition-colors"
                      title="Delete Building"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}

                  <Link
                    to={`/buildings/${b.id}`}
                    className="inline-flex items-center gap-1 text-xs font-medium text-emerald-400 hover:text-emerald-300 py-1 px-2.5 rounded bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/20 transition-all"
                  >
                    <span>View Hierarchy</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Register Building Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg p-6 shadow-2xl space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-800">
              <h3 className="text-base font-bold text-slate-100 flex items-center gap-2">
                <Building2 className="w-5 h-5 text-emerald-400" />
                Register New Building Facility
              </h3>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-200 p-1 rounded"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreate} className="space-y-3.5">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Building Name</label>
                  <input
                    type="text"
                    required
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="Nexora Tower Gamma"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Building Code</label>
                  <input
                    type="text"
                    required
                    value={formData.buildingCode}
                    onChange={(e) => setFormData({ ...formData, buildingCode: e.target.value })}
                    placeholder="NEX-TWR-03"
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 uppercase font-mono outline-none"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Facility Type</label>
                  <select
                    value={formData.buildingType}
                    onChange={(e) => setFormData({ ...formData, buildingType: e.target.value as BuildingType })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                  >
                    <option value="COMMERCIAL">Commercial</option>
                    <option value="EDUCATIONAL">Educational</option>
                    <option value="INDUSTRIAL">Industrial</option>
                    <option value="RESIDENTIAL">Residential</option>
                    <option value="HEALTHCARE">Healthcare</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1">Initial Status</label>
                  <select
                    value={formData.status}
                    onChange={(e) => setFormData({ ...formData, status: e.target.value as BuildingStatus })}
                    className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                  >
                    <option value="ACTIVE">Active</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="INACTIVE">Inactive</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Street Address</label>
                <input
                  type="text"
                  required
                  value={formData.address}
                  onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                  placeholder="300 Innovation Boulevard, Tech District"
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1">Description (Optional)</label>
                <textarea
                  rows={2}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  placeholder="Primary hardware laboratory and testing facility..."
                  className="w-full px-3 py-2 bg-slate-950 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 outline-none"
                />
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
                      <span>Creating...</span>
                    </>
                  ) : (
                    <>
                      <Check className="w-3.5 h-3.5" />
                      <span>Create Facility</span>
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
