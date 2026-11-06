import React from 'react';
import { LucideIcon, Layers, ShieldCheck, CheckCircle2 } from 'lucide-react';

interface PlaceholderPageProps {
  title: string;
  phase: string;
  description: string;
  icon: LucideIcon;
  plannedFeatures: string[];
}

export const PlaceholderPage: React.FC<PlaceholderPageProps> = ({
  title,
  phase,
  description,
  icon: Icon,
  plannedFeatures,
}) => {
  return (
    <div className="space-y-6">
      {/* Title banner */}
      <div className="flex items-center justify-between p-6 bg-[#0b111a] border border-[#1e293b] rounded-xl">
        <div className="flex items-center gap-4">
          <div className="p-3 bg-cyan-950/40 border border-cyan-500/30 rounded-xl text-cyan-400">
            <Icon className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-slate-100">{title}</h1>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded bg-[#162234] text-cyan-400 border border-cyan-500/30">
                {phase}
              </span>
            </div>
            <p className="text-xs text-slate-400 mt-1">{description}</p>
          </div>
        </div>
        <div className="hidden sm:flex items-center gap-2 text-xs font-mono text-slate-400 bg-[#0e1624] px-3 py-1.5 rounded-lg border border-[#1e2e47]">
          <Layers className="w-3.5 h-3.5 text-cyan-400" />
          <span>Structural Placeholder</span>
        </div>
      </div>

      {/* Planned feature roadmap card */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-5">
          <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-slate-300 mb-4 pb-2 border-b border-[#1e293b]">
            <ShieldCheck className="w-4 h-4 text-emerald-400" />
            <span>Scheduled Milestone Features</span>
          </div>
          <ul className="space-y-3">
            {plannedFeatures.map((feat, idx) => (
              <li key={idx} className="flex items-start gap-2.5 text-xs text-slate-300">
                <CheckCircle2 className="w-4 h-4 text-cyan-400 shrink-0 mt-0.5" />
                <span>{feat}</span>
              </li>
            ))}
          </ul>
        </div>

        <div className="bg-[#0b111a] border border-[#1e293b] rounded-xl p-5 flex flex-col justify-between">
          <div>
            <div className="text-xs font-semibold uppercase tracking-wider text-slate-300 mb-3 pb-2 border-b border-[#1e293b]">
              Phase 1 Architectural Boundary
            </div>
            <p className="text-xs text-slate-400 leading-relaxed">
              In accordance with Phase 1 design constraints, this route is structurally bound and ready for route activation in subsequent development phases.
              No fake data, mocked sensors, or simulated business operations are active on this view.
            </p>
          </div>
          <div className="mt-4 p-3 bg-[#0e1624] border border-[#1e2e47] rounded-lg text-[11px] font-mono text-slate-400">
            Route Status: <span className="text-emerald-400 font-semibold">Active & Registered</span>
          </div>
        </div>
      </div>
    </div>
  );
};
