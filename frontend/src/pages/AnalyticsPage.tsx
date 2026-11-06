import React from 'react';
import { BarChart3 } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const AnalyticsPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Historical Telemetry & Building Analytics"
      phase="Phase 7 Target"
      description="Long-term energy optimization, environmental trends, and space utilization reports."
      icon={BarChart3}
      plannedFeatures={[
        'Hourly, daily, and monthly aggregated time-series telemetry',
        'HVAC efficiency and energy consumption correlation analysis',
        'Occupancy heatmaps across floors and departments over time',
        'Automated PDF/CSV executive compliance and sustainability reports',
      ]}
    />
  );
};
