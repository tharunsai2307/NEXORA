import React, { useState, useEffect } from 'react';
import { useOutletContext, Link } from 'react-router-dom';
import { SubsystemStatus } from '../types/system';
import { DashboardStats } from '../types/building';
import { systemService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { SystemStatusBar } from '../components/common/SystemStatusBar';
import { MetricCard } from '../components/common/MetricCard';
import {
  Building2,
  ShieldCheck,
  Network,
  ArrowRight,
  DoorOpen,
  Users,
} from 'lucide-react';

interface DashboardContext {
  status: SubsystemStatus;
  checkStatus: () => void;
  isRefreshing: boolean;
}

export const DashboardPage: React.FC = () => {
  const { status, checkStatus, isRefreshing } = useOutletContext<DashboardContext>();
  const { user } = useAuth();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [statsLoading, setStatsLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      setStatsLoading(true);
      try {
        const data = await systemService.getDashboardStats();
        setStats(data);
      } catch (e) {
        console.error('Failed to fetch dashboard telemetry metrics', e);
      } finally {
        setStatsLoading(false);
      }
    };

    fetchStats();
  }, []);

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 p-6 bg-[#0b111a] border border-[#1e293b] rounded-2xl shadow-xl">
        <div>
          <div className="flex items-center gap-2 mb-1.5">
            <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-emerald-950/60 text-emerald-400 border border-emerald-800">
              MISSION CONTROL ACTIVE
            </span>
            <span className="text-xs text-slate-400 font-mono">
              OPERATOR: {user?.fullName?.toUpperCase() || 'SYSTEM'} ({user?.role})
            </span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100 flex items-center gap-2">
            NEXORA Digital Twin Infrastructure
          </h1>
          <p className="text-xs text-slate-400 mt-1 max-w-2xl leading-relaxed">
            Phase 2 Authenticated Foundation · Multi-tenant building topology, role-based spatial management, and real-time backend telemetry bridge.
          </p>
        </div>

        <div className="flex items-center gap-2 self-start lg:self-center">
          <Link
            to="/buildings"
            className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-medium text-white bg-emerald-600 hover:bg-emerald-500 rounded-lg shadow-lg shadow-emerald-600/20 transition-all"
          >
            <span>Manage Facilities</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </div>

      {/* Service Status Bar */}
      <SystemStatusBar
        status={status}
        onRefresh={() => {
          checkStatus();
          systemService.getDashboardStats().then(setStats).catch(() => {});
        }}
        isRefreshing={isRefreshing}
      />

      {/* Live Operational Telemetry Metric Cards Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Facility Fleet"
          value={stats ? `${stats.totalBuildings} Buildings` : statsLoading ? 'Loading...' : '0 Buildings'}
          subtitle={stats ? `${stats.totalFloors} configured floor levels` : 'Hierarchical levels'}
          icon={Building2}
          badge={{ text: 'Phase 2 Live', variant: 'cyan' }}
        />
        <MetricCard
          title="Partitioned Spaces"
          value={stats ? `${stats.totalRooms} Rooms` : statsLoading ? 'Loading...' : '0 Rooms'}
          subtitle="3D coordinate calibrated"
          icon={DoorOpen}
          badge={{ text: 'Spatial Ready', variant: 'emerald' }}
        />
        <MetricCard
          title="Occupancy Load"
          value={stats ? `${stats.currentOccupancy} / ${stats.totalCapacity}` : statsLoading ? 'Loading...' : '0 / 0'}
          subtitle={stats ? `${stats.occupancyPercentage}% capacity utilized` : 'Occupancy tracking'}
          icon={Users}
          badge={{ text: stats && stats.occupancyPercentage > 85 ? 'High Load' : 'Nominal', variant: stats && stats.occupancyPercentage > 85 ? 'amber' : 'slate' }}
        />
        <MetricCard
          title="Safety Compliance"
          value={stats ? `${stats.safetySummary?.NORMAL || 0} Normal` : statsLoading ? 'Loading...' : 'Nominal'}
          subtitle={stats && stats.safetySummary?.WARNING > 0 ? `${stats.safetySummary.WARNING} warning zones` : 'Zero active safety alerts'}
          icon={ShieldCheck}
          badge={{ text: stats && stats.safetySummary?.CRITICAL > 0 ? 'CRITICAL' : 'Secure', variant: stats && stats.safetySummary?.CRITICAL > 0 ? 'rose' : 'emerald' }}
        />
      </div>

      {/* Cross-Service Communication Monorepo Architecture Box */}
      <div className="bg-[#0b111a] border border-[#1e293b] rounded-2xl p-5 shadow-lg">
        <div className="flex items-center justify-between pb-3 mb-4 border-b border-[#1e293b]">
          <div className="flex items-center gap-2">
            <Network className="w-4 h-4 text-cyan-400" />
            <h2 className="text-xs font-semibold tracking-wider uppercase text-slate-300">
              Cross-Service Communication Pipeline (Monorepo Architecture)
            </h2>
          </div>
          <span className="text-[10px] font-mono text-slate-500">Decoupled Microservice Topology</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 items-center">
          {/* Frontend Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl flex flex-col justify-between h-32">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200">React Frontend</span>
                <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-cyan-950 text-cyan-400 border border-cyan-800">PORT 5173</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">Industrial Dashboard UI, Auth & Digital Twin Shell</p>
            </div>
            <div className="text-[10px] font-mono text-cyan-400 flex items-center gap-1">
              <span>TypeScript · Tailwind</span>
            </div>
          </div>

          {/* Arrow 1 */}
          <div className="hidden md:flex flex-col items-center justify-center text-slate-600">
            <span className="text-[9px] font-mono text-slate-400 mb-0.5">JWT / REST</span>
            <div className="w-full flex items-center">
              <div className="h-0.5 w-full bg-slate-700 relative">
                <div className="absolute right-0 top-1/2 -translate-y-1/2 w-1.5 h-1.5 bg-emerald-400 rotate-45" />
              </div>
            </div>
            <span className="text-[8px] font-mono text-slate-500 mt-0.5">HTTP:8080</span>
          </div>

          {/* Backend Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl flex flex-col justify-between h-32">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200">Java Backend</span>
                <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-emerald-950 text-emerald-400 border border-emerald-800">PORT 8080</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">Spring Boot 3 · Spring Security · Spatial DB</p>
            </div>
            <div className="text-[10px] font-mono text-emerald-400 flex items-center gap-1">
              <span>Java 21 · Flyway · H2/PG</span>
            </div>
          </div>

          {/* AI Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-xl flex flex-col justify-between h-32">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-slate-200">Python AI Service</span>
                <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-purple-950 text-purple-400 border border-purple-800">PORT 8000</span>
              </div>
              <p className="text-[11px] text-slate-400 mt-1">FastAPI Inference Service</p>
            </div>
            <div className="text-[10px] font-mono text-purple-400 flex items-center gap-1">
              <span>FastAPI · Scikit-learn</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
