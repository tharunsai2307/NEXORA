import React from 'react';
import { NavLink } from 'react-router-dom';
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
  ShieldAlert,
} from 'lucide-react';

interface NavItem {
  name: string;
  path: string;
  icon: React.ComponentType<{ className?: string }>;
  tag?: string;
}

const navItems: NavItem[] = [
  { name: 'Dashboard', path: '/dashboard', icon: LayoutDashboard },
  { name: 'Buildings', path: '/buildings', icon: Building2 },
  { name: 'Sensors', path: '/sensors', icon: Radio },
  { name: 'Digital Twin', path: '/digital-twin', icon: Boxes, tag: '3D' },
  { name: 'Occupancy', path: '/occupancy', icon: Users, tag: 'AI' },
  { name: 'Safety', path: '/safety', icon: ShieldCheck },
  { name: 'Emergency', path: '/emergency', icon: AlertTriangle, tag: 'Alert' },
  { name: 'Analytics', path: '/analytics', icon: BarChart3 },
  { name: 'Settings', path: '/settings', icon: Settings },
];

export const Sidebar: React.FC = () => {
  return (
    <aside className="w-64 bg-[#080d16] border-r border-[#1a2538] flex flex-col justify-between shrink-0 h-screen sticky top-0">
      <div>
        {/* Brand Header */}
        <div className="h-16 flex items-center gap-3 px-5 border-b border-[#1a2538]">
          <div className="w-9 h-9 rounded-lg bg-cyan-950/60 border border-cyan-500/40 flex items-center justify-center text-cyan-400">
            <ShieldAlert className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold tracking-wider text-slate-100 flex items-center gap-1.5">
              <span>AEGIS<span className="text-cyan-400">TWIN</span></span>
              <span className="text-[10px] font-mono px-1 py-0.2 bg-cyan-950 text-cyan-400 border border-cyan-800 rounded">v1.0</span>
            </div>
            <p className="text-[10px] text-slate-400 font-mono tracking-tight">Smart Building Edge-AI</p>
          </div>
        </div>

        {/* Phase Indicator */}
        <div className="mx-4 my-3 p-2.5 bg-[#0e1624] border border-[#1e2e47] rounded-lg">
          <div className="flex items-center justify-between text-[11px] mb-1">
            <span className="text-slate-400 font-medium">Milestone</span>
            <span className="font-mono text-cyan-400 font-semibold">Phase 1 of 7</span>
          </div>
          <div className="text-[10px] text-slate-400">Architectural Foundation</div>
          <div className="w-full bg-[#182337] h-1.5 rounded-full mt-2 overflow-hidden">
            <div className="bg-gradient-to-r from-cyan-500 to-emerald-400 h-full w-[14%]"></div>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="px-3 py-2 space-y-1">
          <div className="px-3 py-1.5 text-[10px] font-bold tracking-wider uppercase text-slate-400">
            Navigation Console
          </div>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3 py-2 rounded-lg text-xs font-medium transition-colors ${
                    isActive
                      ? 'bg-cyan-950/40 text-cyan-300 border border-cyan-500/30'
                      : 'text-slate-300 hover:bg-[#111928] hover:text-slate-100 border border-transparent'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4 h-4 text-slate-400 group-hover:text-cyan-400" />
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

      {/* Footer Info */}
      <div className="p-4 border-t border-[#1a2538] text-[11px] font-mono text-slate-400">
        <div className="flex items-center justify-between mb-1">
          <span>Target Architecture</span>
          <span className="text-emerald-400">Industry Spec</span>
        </div>
        <div className="text-[10px] text-slate-400">
          React · Spring Boot · FastAPI · Postgres
        </div>
      </div>
    </aside>
  );
};
