import React from 'react';
import { Settings } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const SettingsPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Platform & Gateway Configuration"
      phase="Phase 1/2 Setting"
      description="System settings, API endpoint routing, alerting targets, and security policies."
      icon={Settings}
      plannedFeatures={[
        'Edge gateway URL configuration (Backend & AI engine endpoints)',
        'Polling frequencies and WebSocket reconnection backoff parameters',
        'User roles and permissions (Phase 2 Spring Security JWT)',
        'Dark mode theme customization and display telemetry density',
      ]}
    />
  );
};
