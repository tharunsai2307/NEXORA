import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { RoleGuard } from './components/auth/RoleGuard';
import { AppLayout } from './components/layout/AppLayout';

import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { DashboardPage } from './pages/DashboardPage';
import { BuildingsPage } from './pages/BuildingsPage';
import { BuildingDetailPage } from './pages/BuildingDetailPage';
import { FloorDetailPage } from './pages/FloorDetailPage';
import { UserManagementPage } from './pages/UserManagementPage';

// Placeholder modules for upcoming phases
import { SensorsPage } from './pages/SensorsPage';
import { DigitalTwinPage } from './pages/DigitalTwinPage';
import { OccupancyPage } from './pages/OccupancyPage';
import { SafetyPage } from './pages/SafetyPage';
import { EmergencyPage } from './pages/EmergencyPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { SettingsPage } from './pages/SettingsPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* Protected Application Shell */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            
            {/* Building Management Hierarchy */}
            <Route path="/buildings" element={<BuildingsPage />} />
            <Route path="/buildings/:buildingId" element={<BuildingDetailPage />} />
            <Route path="/buildings/:buildingId/floors/:floorId" element={<FloorDetailPage />} />

            {/* Administration Routes */}
            <Route
              path="/users"
              element={
                <RoleGuard allowedRoles={['SYSTEM_ADMIN']}>
                  <UserManagementPage />
                </RoleGuard>
              }
            />

            {/* Placeholder Module Routes */}
            <Route path="/sensors" element={<SensorsPage />} />
            <Route path="/digital-twin" element={<DigitalTwinPage />} />
            <Route path="/occupancy" element={<OccupancyPage />} />
            <Route path="/safety" element={<SafetyPage />} />
            <Route path="/emergency" element={<EmergencyPage />} />
            <Route path="/analytics" element={<AnalyticsPage />} />
            <Route path="/settings" element={<SettingsPage />} />

            {/* Catch-all Fallback */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;
