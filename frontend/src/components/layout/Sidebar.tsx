import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Building2,
  Radio,
  Boxes,
  Users,
  ShieldCheck,
  AlertTriangle,
  BarChart3,
  Settings,
  UserCog,
  LogOut,
} from 'lucide-react';

interface NavItem {
  name: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  tag?: string;
  adminOnly?: boolean;
}

const navItems: NavItem[] = [
  { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  { name: 'Buildings', path: '/buildings', icon: Building2 },
  { name: 'User Directory', path: '/users', icon: UserCog, adminOnly: true },
  { name: 'Sensors', path: '/sensors', icon: Radio },
  { name: 'Digital Twin', path: '/digital-twin', icon: Boxes, tag: '3D' },
  { name: 'Occupancy', path: '/occupancy', icon: Users, tag: 'AI' },
  { name: 'Safety', path: '/safety', icon: ShieldCheck },
  { name: 'Emergency', path: '/emergency', icon: AlertTriangle, tag: 'Alert' },
  { name: 'Analytics', path: '/analytics', icon: BarChart3 },
  { name: 'Settings', path: '/settings', icon: Settings },
];

export const Sidebar: React.FC = () => {
  const { user, logout, hasRole } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getRoleBadge = () => {
    if (!user) return null;
    switch (user.role) {
      case 'SYSTEM_ADMIN':
        return (
          <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-purple-500/10 text-purple-400 border border-purple-500/30">
            ADMIN
          </span>
        );
      case 'BUILDING_MANAGER':
        return (
          <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/30">
            MANAGER
          </span>
        );
      case 'SAFETY_OFFICER':
        return (
          <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30">
            SAFETY
          </span>
        );
    }
  };

  return (
    <aside className="w-64 bg-[#080d16] border-r border-[#1a2538] flex flex-col justify-between shrink-0 h-screen sticky top-0">
      <div className="overflow-y-auto">
        {/* Brand Header */}
        <div className="h-16 flex items-center gap-3 px-5 border-b border-[#1a2538]">
          <div className="w-9 h-9 rounded-lg bg-emerald-950/60 border border-emerald-500/40 flex items-center justify-center text-emerald-400">
            <Building2 className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold tracking-wider text-slate-100 flex items-center gap-1.5">
              <span>NEX<span className="text-emerald-400">ORA</span></span>
              <span className="text-[10px] font-mono px-1 py-0.2 bg-emerald-950 text-emerald-400 border border-emerald-800 rounded">v2.0</span>
            </div>
            <p className="text-[10px] text-slate-400 font-mono tracking-tight">Intelligence Beyond Infrastructure</p>
          </div>
        </div>

        {/* Phase Indicator */}
        <div className="mx-4 my-3 p-2.5 bg-[#0e1624] border border-[#1e2e47] rounded-lg">
          <div className="flex items-center justify-between text-[11px] mb-1">
            <span className="text-slate-400 font-medium">Milestone</span>
            <span className="font-mono text-emerald-400 font-semibold">Phase 6 of 7</span>
          </div>
          <div className="text-[10px] text-slate-400">Emergency & Evacuation</div>
          <div className="w-full bg-[#182337] h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-gradient-to-r from-emerald-500 to-cyan-400 h-full w-[86%]"></div>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="px-3 py-2 space-y-1">
          <div className="px-3 py-1.5 text-[10px] font-bold tracking-wider uppercase text-slate-400">
            Navigation Console
          </div>
          {navItems.map((item) => {
            if (item.adminOnly && !hasRole('SYSTEM_ADMIN')) {
              return null;
            }

            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-emerald-950/40 text-emerald-300 border border-emerald-500/30'
                      : 'text-slate-300 hover:bg-[#111928] hover:text-slate-100 border border-transparent'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4 h-4 text-slate-400 group-hover:text-emerald-400" />
                  <span>{item.name}</span>
                </div>
                {item.tag && (
                  <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#162234] text-slate-400 border border-[#233550]">
                    {item.tag}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* User Session Footer Panel */}
      <div className="p-3 border-t border-[#1a2538] bg-[#070b12]">
        <div className="flex items-center justify-between p-2 rounded-xl bg-slate-900/80 border border-slate-800">
          <div className="flex items-center gap-2.5 overflow-hidden">
            <div className="w-8 h-8 rounded-lg bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center font-bold text-xs text-emerald-400 shrink-0">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="overflow-hidden">
              <div className="text-xs font-semibold text-slate-100 truncate">
                {user?.fullName || 'Operator'}
              </div>
              <div className="mt-0.5">{getRoleBadge()}</div>
            </div>
          </div>

          <button
            onClick={handleLogout}
            className="p-1.5 text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors shrink-0 ml-1"
            title="Log out of session"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
};
