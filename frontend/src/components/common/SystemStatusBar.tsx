import React from 'react';
import { SubsystemStatus } from '../../types/system';
import { ServiceStatusBadge } from './ServiceStatusBadge';
import { RefreshCw, Server, Cpu, Database } from 'lucide-react';

interface SystemStatusBarProps {
  status: SubsystemStatus;
  onRefresh: () => void;
  isRefreshing?: boolean;
}

export const SystemStatusBar: React.FC<SystemStatusBarProps> = ({
  status,
  onRefresh,
  isRefreshing = false,
}) => {
  return (
    <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-4 shadow-lg shadow-black/40">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between pb-3 mb-3 border-b border-[#1e293b] gap-2">
        <div className="flex items-center gap-2">
          <span className="h-2 w-2 rounded-full bg-cyan-400"></span>
          <h3 className="text-xs font-semibold tracking-wider uppercase text-slate-300">
            Enterprise Subsystem Heartbeat
          </h3>
          <span className="text-[11px] font-mono text-slate-500">
            {status.lastChecked ? `Checked ${status.lastChecked}` : 'Initializing...'}
          </span>
        </div>
        <div className="flex items-center gap-3">
          {status.responseTimeMs !== null && (
            <span className="text-[11px] font-mono text-slate-400">
              Latency: <span className="text-cyan-400 font-semibold">{status.responseTimeMs}ms</span>
            </span>
          )}
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            data-testid="refresh-status-button"
            className="flex items-center gap-1.5 px-2.5 py-1 text-xs font-medium text-slate-300 bg-[#162032] hover:bg-[#1e2c45] border border-[#263753] rounded-md transition disabled:opacity-50"
            title="Refresh Service Status"
          >
            <RefreshCw className={`w-3.5 h-3.5 text-cyan-400 ${isRefreshing ? 'animate-spin' : ''}`} />
            <span>Probe Services</span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-1.5 text-[11px] text-slate-400 font-medium mb-1">
            <Server className="w-3.5 h-3.5 text-cyan-400" />
            <span>Spring Boot Core</span>
          </div>
          <ServiceStatusBadge
            label="Backend"
            status={status.backend}
            subtext="Port 8080 · Java 21 REST"
          />
        </div>

        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-1.5 text-[11px] text-slate-400 font-medium mb-1">
            <Cpu className="w-3.5 h-3.5 text-indigo-400" />
            <span>FastAPI Edge-AI</span>
          </div>
          <ServiceStatusBadge
            label="AI Engine"
            status={status.aiEngine}
            subtext="Port 8000 · Python ML"
          />
        </div>

        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-1.5 text-[11px] text-slate-400 font-medium mb-1">
            <Database className="w-3.5 h-3.5 text-emerald-400" />
            <span>PostgreSQL Database</span>
          </div>
          <ServiceStatusBadge
            label="Database"
            status={status.database}
            subtext="Port 5432 · Flyway Migrations"
          />
        </div>
      </div>

      {status.error && (
        <div className="mt-3 p-2 bg-rose-950/30 border border-rose-500/30 rounded text-rose-300 text-xs font-mono">
          Bridge Warning: {status.error}
        </div>
      )}
    </div>
  );
};
