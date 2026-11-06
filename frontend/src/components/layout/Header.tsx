import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { SubsystemStatus } from '../../types/system';
import { Clock, Radio, Activity } from 'lucide-react';

interface HeaderProps {
  status: SubsystemStatus;
}

export const Header: React.FC<HeaderProps> = ({ status }) => {
  const location = useLocation();
  const [currentTime, setCurrentTime] = useState<string>('');

  useEffect(() => {
    const update = () => setCurrentTime(new Date().toUTCString().replace('GMT', 'UTC'));
    update();
    const timer = setInterval(update, 1000);
    return () => clearInterval(timer);
  }, []);

  const routeName = location.pathname.substring(1).toUpperCase() || 'DASHBOARD';

  const allConnected =
    status.backend === 'Connected' &&
    status.aiEngine === 'Connected' &&
    status.database === 'Connected';

  return (
    <header className="h-16 bg-[#080d16]/90 backdrop-blur-md border-b border-[#1a2538] flex items-center justify-between px-6 sticky top-0 z-20">
      {/* Breadcrumb / Section */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2">
          <Activity className="w-4 h-4 text-cyan-400" />
          <span className="text-xs font-mono uppercase tracking-widest text-slate-400">
            SYSTEM /
          </span>
          <span className="text-sm font-semibold tracking-wide text-slate-100">
            {routeName}
          </span>
        </div>
      </div>

      {/* Right status area */}
      <div className="flex items-center gap-4">
        {/* UTC Clock */}
        <div className="hidden sm:flex items-center gap-2 px-3 py-1 bg-[#0e1624] border border-[#1e2e47] rounded-md text-xs font-mono text-slate-300">
          <Clock className="w-3.5 h-3.5 text-slate-400" />
          <span>{currentTime || 'Synchronizing clock...'}</span>
        </div>

        {/* Global Pipeline Health Pill */}
        <div className="flex items-center gap-2 px-3 py-1 rounded-md text-xs font-mono border bg-[#0e1624] border-[#1e2e47]">
          <span className="relative flex h-2 w-2">
            <span
              className={`relative inline-flex rounded-full h-2 w-2 ${
                allConnected
                  ? 'bg-emerald-400'
                  : status.backend === 'Connected'
                  ? 'bg-amber-400'
                  : 'bg-rose-500'
              }`}
            ></span>
          </span>
          <span className="text-slate-300 font-medium">
            {allConnected ? '3/3 Services Online' : 'Partial / Isolated'}
          </span>
        </div>

        {/* Node Profile */}
        <div className="hidden md:flex items-center gap-1.5 px-2.5 py-1 text-xs font-mono text-slate-400 bg-[#0e1624] border border-[#1e2e47] rounded-md">
          <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
          <span>NODE: EDGE-01</span>
        </div>
      </div>
    </header>
  );
};
