import React from 'react';
import { Users } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const OccupancyPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Edge-AI Occupancy Prediction & Analytics"
      phase="Phase 3/4 Target"
      description="Machine-learning driven room occupancy estimation, density tracking, and space utilization."
      icon={Users}
      plannedFeatures={[
        'Scikit-learn random forest / gradient boosting occupancy regression',
        'Multi-modal inference fusing CO2, motion, and temperature gradients',
        'Live room capacity utilization gauges and overcrowding warnings',
        'Desk and meeting room dynamic booking availability indicators',
      ]}
    />
  );
};
