import React, { useState, useEffect } from 'react';
import { userApi } from '../services/api';
import { User, Role } from '../types/auth';
import { useAuth } from '../context/AuthContext';
import {
  Users,
  Search,
  Filter,
  ShieldCheck,
  ShieldAlert,
  Shield,
  UserCheck,
  UserX,
  AlertCircle,
  RefreshCw,
  Check,
  X,
} from 'lucide-react';

export const UserManagementPage: React.FC = () => {
  const { user: currentUser } = useAuth();
  const [users, setUsers] = useState<User[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedRole, setSelectedRole] = useState<Role | 'ALL'>('ALL');
  const [selectedStatus, setSelectedStatus] = useState<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  const [isLoading, setIsLoading] = useState(true);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);

  // Modal / Dropdown editing state
  const [editingUserId, setEditingUserId] = useState<string | null>(null);
  const [targetRole, setTargetRole] = useState<Role>('BUILDING_MANAGER');

  const fetchUsers = async () => {
    setIsLoading(true);
    setActionError(null);
    try {
      const activeFilter =
        selectedStatus === 'ACTIVE' ? true : selectedStatus === 'INACTIVE' ? false : undefined;
      const roleFilter = selectedRole === 'ALL' ? undefined : selectedRole;
      const data = await userApi.listUsers(searchQuery || undefined, roleFilter, activeFilter);
      setUsers(data);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to load user directory');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [selectedRole, selectedStatus]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    fetchUsers();
  };

  const handleRoleChange = async (userId: string, newRole: Role) => {
    setActionError(null);
    setActionSuccess(null);
    try {
      const updated = await userApi.updateRole(userId, newRole);
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)));
      setActionSuccess(`Role updated to ${newRole} for ${updated.fullName}`);
      setEditingUserId(null);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to update user role');
    }
  };

  const handleStatusToggle = async (userId: string, currentActive: boolean) => {
    setActionError(null);
    setActionSuccess(null);
    try {
      const updated = await userApi.updateStatus(userId, !currentActive);
      setUsers((prev) => prev.map((u) => (u.id === userId ? updated : u)));
      setActionSuccess(`Account ${updated.active ? 'activated' : 'deactivated'} for ${updated.fullName}`);
    } catch (err: any) {
      setActionError(err.response?.data?.message || 'Failed to update user status');
    }
  };

  const getRoleBadge = (role: Role) => {
    switch (role) {
      case 'SYSTEM_ADMIN':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-mono font-medium bg-purple-500/10 text-purple-400 border border-purple-500/30">
            <ShieldCheck className="w-3 h-3" />
            ADMIN
          </span>
        );
      case 'BUILDING_MANAGER':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-mono font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            <Shield className="w-3 h-3" />
            MANAGER
          </span>
        );
      case 'SAFETY_OFFICER':
        return (
          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-mono font-medium bg-amber-500/10 text-amber-400 border border-amber-500/30">
            <ShieldAlert className="w-3 h-3" />
            SAFETY OFF.
          </span>
        );
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold tracking-tight text-slate-100">User & RBAC Administration</h1>
            <span className="px-2 py-0.5 text-xs font-mono font-medium bg-purple-500/10 text-purple-400 border border-purple-500/30 rounded">
              ADMIN ONLY
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Manage security clearance, operator roles, and account access statuses across NEXORA.
          </p>
        </div>

        <button
          onClick={fetchUsers}
          className="inline-flex items-center gap-2 px-3 py-1.5 text-xs font-medium text-slate-300 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />
          <span>Refresh Directory</span>
        </button>
      </div>

      {/* Notifications */}
      {actionError && (
        <div className="p-3 rounded-lg bg-red-500/10 border border-red-500/30 flex items-start gap-2.5 text-xs text-red-300">
          <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
          <span>{actionError}</span>
        </div>
      )}

      {actionSuccess && (
        <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-start gap-2.5 text-xs text-emerald-300">
          <Check className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <span>{actionSuccess}</span>
        </div>
      )}

      {/* Filter / Search Bar */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl p-4 flex flex-col md:flex-row gap-3 items-stretch md:items-center justify-between">
        <form onSubmit={handleSearch} className="flex-1 relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-2.5 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search operator by name or email address..."
            className="w-full pl-9 pr-4 py-2 bg-slate-950/60 border border-slate-800 focus:border-emerald-500 rounded-lg text-xs text-slate-100 placeholder-slate-500 outline-none"
          />
        </form>

        <div className="flex items-center gap-2">
          <div className="flex items-center gap-1.5 text-xs text-slate-400">
            <Filter className="w-3.5 h-3.5 text-slate-500" />
            <span>Role:</span>
          </div>
          <select
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value as any)}
            className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 outline-none focus:border-emerald-500"
          >
            <option value="ALL">All Roles</option>
            <option value="SYSTEM_ADMIN">System Admin</option>
            <option value="BUILDING_MANAGER">Building Manager</option>
            <option value="SAFETY_OFFICER">Safety Officer</option>
          </select>

          <div className="flex items-center gap-1.5 text-xs text-slate-400 ml-2">
            <span>Status:</span>
          </div>
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value as any)}
            className="bg-slate-950 border border-slate-800 rounded-lg px-2.5 py-1.5 text-xs text-slate-200 outline-none focus:border-emerald-500"
          >
            <option value="ALL">All Statuses</option>
            <option value="ACTIVE">Active Only</option>
            <option value="INACTIVE">Deactivated Only</option>
          </select>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-xl overflow-hidden shadow-xl">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 uppercase font-mono tracking-wider border-b border-slate-800">
              <tr>
                <th className="py-3 px-4">Operator</th>
                <th className="py-3 px-4">Contact</th>
                <th className="py-3 px-4">Clearance Role</th>
                <th className="py-3 px-4">Account Status</th>
                <th className="py-3 px-4">Registered</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin mx-auto mb-2" />
                    <span>Loading authorized users...</span>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-500">
                    <Users className="w-8 h-8 mx-auto mb-2 opacity-30" />
                    <span>No operators found matching the criteria.</span>
                  </td>
                </tr>
              ) : (
                users.map((u) => {
                  const isCurrent = currentUser?.id === u.id;
                  const isEditing = editingUserId === u.id;

                  return (
                    <tr key={u.id} className="hover:bg-slate-800/30 transition-colors">
                      <td className="py-3.5 px-4">
                        <div className="flex items-center gap-2.5">
                          <div className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700 flex items-center justify-center font-bold text-slate-200">
                            {u.fullName.charAt(0)}
                          </div>
                          <div>
                            <div className="font-medium text-slate-100 flex items-center gap-1.5">
                              <span>{u.fullName}</span>
                              {isCurrent && (
                                <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-emerald-500/20 text-emerald-400 border border-emerald-500/30">
                                  YOU
                                </span>
                              )}
                            </div>
                            <div className="text-[11px] text-slate-400 font-mono">{u.email}</div>
                          </div>
                        </div>
                      </td>

                      <td className="py-3.5 px-4 font-mono text-slate-400">
                        {u.phoneNumber || '—'}
                      </td>

                      <td className="py-3.5 px-4">
                        {isEditing ? (
                          <div className="flex items-center gap-1.5">
                            <select
                              value={targetRole}
                              onChange={(e) => setTargetRole(e.target.value as Role)}
                              className="bg-slate-950 border border-emerald-500 rounded px-2 py-1 text-xs text-slate-100"
                            >
                              <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
                              <option value="BUILDING_MANAGER">BUILDING_MANAGER</option>
                              <option value="SAFETY_OFFICER">SAFETY_OFFICER</option>
                            </select>
                            <button
                              onClick={() => handleRoleChange(u.id, targetRole)}
                              title="Save"
                              className="p-1 rounded bg-emerald-600 hover:bg-emerald-500 text-white"
                            >
                              <Check className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={() => setEditingUserId(null)}
                              title="Cancel"
                              className="p-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300"
                            >
                              <X className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        ) : (
                          getRoleBadge(u.role)
                        )}
                      </td>

                      <td className="py-3.5 px-4">
                        {u.active ? (
                          <span className="inline-flex items-center gap-1 text-[11px] text-emerald-400 font-mono">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                            Active
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-[11px] text-slate-500 font-mono">
                            <span className="w-1.5 h-1.5 rounded-full bg-slate-600" />
                            Deactivated
                          </span>
                        )}
                      </td>

                      <td className="py-3.5 px-4 font-mono text-slate-400 text-[11px]">
                        {new Date(u.createdAt).toLocaleDateString()}
                      </td>

                      <td className="py-3.5 px-4 text-right">
                        <div className="inline-flex items-center gap-1.5">
                          {!isEditing && (
                            <button
                              onClick={() => {
                                setEditingUserId(u.id);
                                setTargetRole(u.role);
                              }}
                              className="px-2 py-1 text-[11px] font-medium text-slate-300 hover:text-white bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded transition-colors"
                            >
                              Change Role
                            </button>
                          )}

                          <button
                            onClick={() => handleStatusToggle(u.id, u.active)}
                            disabled={isCurrent && u.role === 'SYSTEM_ADMIN'}
                            className={`p-1 rounded border transition-colors ${
                              u.active
                                ? 'bg-red-500/10 hover:bg-red-500/20 text-red-400 border-red-500/30'
                                : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400 border-emerald-500/30'
                            } disabled:opacity-30 disabled:cursor-not-allowed`}
                            title={u.active ? 'Deactivate Account' : 'Activate Account'}
                          >
                            {u.active ? <UserX className="w-3.5 h-3.5" /> : <UserCheck className="w-3.5 h-3.5" />}
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

