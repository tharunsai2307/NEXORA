import React from 'react';
import { useOutletContext } from 'react-router-dom';
import { SubsystemStatus } from '../types/system';
import { SystemStatusBar } from '../components/common/SystemStatusBar';
import { MetricCard } from '../components/common/MetricCard';
import {
  Building2,
  Radio,
  Cpu,
  ShieldCheck,
  Network,
  ArrowRight,
  Terminal,
} from 'lucide-react';

interface DashboardContext {
  status: SubsystemStatus;
  checkStatus: () => void;
  isRefreshing: boolean;
}

export const DashboardPage: React.FC = () => {
  const { status, checkStatus, isRefreshing } = useOutletContext<DashboardContext>();

  return (
    <div className="space-y-6">
      {/* Top Banner */}
      <div className="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-4 p-6 bg-[#0b111a] border border-[#1e293b] rounded-xl shadow-lg">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-cyan-950/60 text-cyan-400 border border-cyan-800">
              MISSION CONTROL
            </span>
            <span className="text-xs text-slate-400 font-mono">NODE ID: SB-EDGE-01</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-100">
            Smart Building Safety & Occupancy Digital Twin
          </h1>
          <p className="text-xs text-slate-400 mt-1 max-w-2xl">
            Phase 1 Foundation · High-reliability edge AI microservice architecture integrating virtual IoT sensors, Spring Boot orchestration, FastAPI inference, and PostgreSQL persistence.
          </p>
        </div>
      </div>

      {/* Service Status Bar */}
      <SystemStatusBar
        status={status}
        onRefresh={checkStatus}
        isRefreshing={isRefreshing}
      />

      {/* Metric Cards Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Campus Topology"
          value="Architecture"
          subtitle="Building · Floor · Zone schema"
          icon={Building2}
          badge={{ text: 'Phase 2 Ready', variant: 'cyan' }}
        />
        <MetricCard
          title="Virtual IoT Sensors"
          value="Standby"
          subtitle="IAQ, CO2, Smoke, Motion"
          icon={Radio}
          badge={{ text: 'Phase 3 Target', variant: 'slate' }}
        />
        <MetricCard
          title="Edge-AI Engines"
          value="Operational"
          subtitle="FastAPI + scikit-learn bridge"
          icon={Cpu}
          badge={{ text: 'Bridge Live', variant: 'emerald' }}
        />
        <MetricCard
          title="Safety Protocols"
          value="Configured"
          subtitle="Threshold & egress safety"
          icon={ShieldCheck}
          badge={{ text: 'Phase 4/6 Target', variant: 'slate' }}
        />
      </div>

      {/* System Architecture Flow Diagram */}
      <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-5 shadow-lg">
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
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-lg flex flex-col justify-between h-32">
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono text-cyan-400 font-semibold">FRONTEND</span>
                <span className="text-[10px] font-mono text-slate-500">Port 5173</span>
              </div>
              <h4 className="text-sm font-bold text-slate-200 mt-1">React + Vite</h4>
              <p className="text-[11px] text-slate-400 mt-1">TypeScript · Tailwind · Three.js</p>
            </div>
            <div className="text-[10px] font-mono text-emerald-400 flex items-center gap-1">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> Active View
            </div>
          </div>

          {/* Java Backend Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-lg flex flex-col justify-between h-32 relative">
            <div className="hidden md:block absolute -left-3 top-1/2 -translate-y-1/2 text-cyan-500">
              <ArrowRight className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono text-cyan-400 font-semibold">BACKEND CORE</span>
                <span className="text-[10px] font-mono text-slate-500">Port 8080</span>
              </div>
              <h4 className="text-sm font-bold text-slate-200 mt-1">Spring Boot 3</h4>
              <p className="text-[11px] text-slate-400 mt-1">Java 21 · REST · JPA · Security</p>
            </div>
            <div className="text-[10px] font-mono text-slate-400">
              State: <span className={status.backend === 'Connected' ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>{status.backend}</span>
            </div>
          </div>

          {/* Python AI Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-lg flex flex-col justify-between h-32 relative">
            <div className="hidden md:block absolute -left-3 top-1/2 -translate-y-1/2 text-cyan-500">
              <ArrowRight className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono text-indigo-400 font-semibold">AI ENGINE</span>
                <span className="text-[10px] font-mono text-slate-500">Port 8000</span>
              </div>
              <h4 className="text-sm font-bold text-slate-200 mt-1">FastAPI Microservice</h4>
              <p className="text-[11px] text-slate-400 mt-1">Python 3 · NumPy · scikit-learn</p>
            </div>
            <div className="text-[10px] font-mono text-slate-400">
              State: <span className={status.aiEngine === 'Connected' ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>{status.aiEngine}</span>
            </div>
          </div>

          {/* PostgreSQL Box */}
          <div className="p-4 bg-[#0e1624] border border-[#1e2e47] rounded-lg flex flex-col justify-between h-32 relative">
            <div className="hidden md:block absolute -left-3 top-1/2 -translate-y-1/2 text-cyan-500">
              <ArrowRight className="w-4 h-4" />
            </div>
            <div>
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono text-emerald-400 font-semibold">DATABASE</span>
                <span className="text-[10px] font-mono text-slate-500">Port 5432</span>
              </div>
              <h4 className="text-sm font-bold text-slate-200 mt-1">PostgreSQL 16</h4>
              <p className="text-[11px] text-slate-400 mt-1">Flyway Migrations · Persistent Volume</p>
            </div>
            <div className="text-[10px] font-mono text-slate-400">
              State: <span className={status.database === 'Connected' ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>{status.database}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Phase 1 Verification Checklist */}
      <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-5 shadow-lg">
        <div className="flex items-center gap-2 pb-3 mb-4 border-b border-[#1e293b]">
          <Terminal className="w-4 h-4 text-cyan-400" />
          <h2 className="text-xs font-semibold tracking-wider uppercase text-slate-300">
            Phase 1 Foundation Acceptance Verification
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
          <div className="p-3 bg-[#0e1624] border border-[#1e2e47] rounded-lg space-y-2">
            <div className="font-semibold text-slate-200 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              Architectural Separation Enforced
            </div>
            <p className="text-slate-400 text-[11px] leading-relaxed">
              Frontend talks strictly to Java Backend over REST. Java Backend communicates with PostgreSQL via JPA and with Python AI via internal HTTP API. Python AI never accesses database directly.
            </p>
          </div>

          <div className="p-3 bg-[#0e1624] border border-[#1e2e47] rounded-lg space-y-2">
            <div className="font-semibold text-slate-200 flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              Automated Testing & Multi-Container Docker
            </div>
            <p className="text-slate-400 text-[11px] leading-relaxed">
              Pytest suite for AI engine, JUnit 5 + Spring Boot Test suite for Java backend, and Vitest suite for React frontend with complete Docker Compose orchestration.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
