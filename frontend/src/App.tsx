import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './components/layout/AppLayout';
import { DashboardPage } from './pages/DashboardPage';
import { BuildingsPage } from './pages/BuildingsPage';
import { SensorsPage } from './pages/SensorsPage';
import { DigitalTwinPage } from './pages/DigitalTwinPage';
import { OccupancyPage } from './pages/OccupancyPage';
import { SafetyPage } from './pages/SafetyPage';
import { EmergencyPage } from './pages/EmergencyPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SettingsPage } from './pages/SettingsPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/buildings" element={<BuildingsPage />} />
          <Route path="/sensors" element={<SensorsPage />} />
          <Route path="/digital-twin" element={<DigitalTwinPage />} />
          <Route path="/occupancy" element={<OccupancyPage />} />
          <Route path="/safety" element={<SafetyPage />} />
          <Route path="/emergency" element={<EmergencyPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

export default App;
