import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string | number;
  unit?: string;
  subtitle?: string;
  icon: LucideIcon;
  badge?: {
    text: string;
    variant: 'emerald' | 'cyan' | 'amber' | 'rose' | 'slate';
  };
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  unit,
  subtitle,
  icon: Icon,
  badge,
}) => {
  const badgeColors = {
    emerald: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
    cyan: 'bg-cyan-500/10 text-cyan-400 border-cyan-500/20',
    amber: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
    rose: 'bg-rose-500/10 text-rose-400 border-rose-500/20',
    slate: 'bg-slate-500/10 text-slate-400 border-slate-500/20',
  };

  return (
    <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-4 flex flex-col justify-between hover:border-[#2d3f5d] transition-colors">
      <div className="flex items-center justify-between mb-3">
        <span className="text-xs font-medium text-slate-400 uppercase tracking-wider">
          {title}
        </span>
        <div className="p-2 bg-[#121a27] rounded-lg border border-[#1e293b]">
          <Icon className="w-4 h-4 text-cyan-400" />
        </div>
      </div>

      <div className="flex items-baseline gap-2">
        <span className="text-2xl font-bold font-mono text-slate-100">{value}</span>
        {unit && <span className="text-xs font-mono text-slate-400">{unit}</span>}
      </div>

      <div className="flex items-center justify-between mt-3 pt-2 border-t border-[#162032] text-xs">
        <span className="text-slate-400 truncate">{subtitle || 'Phase 1 Structural Metric'}</span>
        {badge && (
          <span
            className={`px-2 py-0.5 rounded text-[10px] font-mono border ${
              badgeColors[badge.variant]
            }`}
          >
            {badge.text}
          </span>
        )}
      </div>
    </div>
  );
};
