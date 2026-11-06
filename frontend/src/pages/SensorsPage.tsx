import React from 'react';
import { Radio } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const SensorsPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Virtual IoT Sensor Registry"
      phase="Phase 3 Target"
      description="Management and configuration console for virtual IoT environmental and occupancy sensors."
      icon={Radio}
      plannedFeatures={[
        'Virtual sensor provisioning (Temperature, Humidity, CO2, Smoke, Motion)',
        'Simulation engine parameter tuning (Normal, Peak, Emergency scenarios)',
        'Sensor telemetry health check and stream telemetry inspection',
        'Heartbeat intervals and reading frequency calibration',
      ]}
    />
  );
};
