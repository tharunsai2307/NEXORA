import React from 'react';
import { ConnectionStatus } from '../../types/system';

interface ServiceStatusBadgeProps {
  label: string;
  status: ConnectionStatus;
  subtext?: string;
}

export const ServiceStatusBadge: React.FC<ServiceStatusBadgeProps> = ({
  label,
  status,
  subtext,
}) => {
  const isConnected = status === 'Connected';
  const isChecking = status === 'Checking';

  return (
    <div
      data-testid={`status-badge-${label.toLowerCase().replace(/\s+/g, '-')}`}
      className={`flex items-center justify-between px-3.5 py-2 rounded-lg border transition-all ${
        isConnected
          ? 'bg-emerald-950/20 border-emerald-500/30 text-emerald-300'
          : isChecking
          ? 'bg-amber-950/20 border-amber-500/30 text-amber-300'
          : 'bg-rose-950/20 border-rose-500/30 text-rose-300'
      }`}
    >
      <div className="flex items-center gap-2.5">
        <span className="relative flex h-2.5 w-2.5">
          {isConnected && (
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          )}
          <span
            className={`relative inline-flex rounded-full h-2.5 w-2.5 ${
              isConnected
                ? 'bg-emerald-400'
                : isChecking
                ? 'bg-amber-400 animate-pulse'
                : 'bg-rose-500'
            }`}
          ></span>
        </span>
        <div>
          <div className="text-xs font-medium tracking-wide uppercase text-slate-400">
            {label}
          </div>
          {subtext && <div className="text-[10px] text-slate-500 font-mono">{subtext}</div>}
        </div>
      </div>
      <span
        className={`text-xs font-mono font-semibold px-2 py-0.5 rounded ${
          isConnected
            ? 'bg-emerald-500/20 text-emerald-400'
            : isChecking
            ? 'bg-amber-500/20 text-amber-300'
            : 'bg-rose-500/20 text-rose-400'
        }`}
      >
        {status}
      </span>
    </div>
  );
};
