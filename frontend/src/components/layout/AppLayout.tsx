import React from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Header } from './Header';
import { useSystemStatus } from '../../hooks/useSystemStatus';

export const AppLayout: React.FC = () => {
  const { status, checkStatus, isRefreshing } = useSystemStatus(10000);

  return (
    <div className="flex min-h-screen bg-[#070b12] text-slate-100 font-sans">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <Header status={status} />
        <main className="flex-1 p-6 overflow-y-auto">
          <Outlet context={{ status, checkStatus, isRefreshing }} />
        </main>
      </div>
    </div>
  );
};
