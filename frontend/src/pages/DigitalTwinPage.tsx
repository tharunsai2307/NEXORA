import React from 'react';
import { Boxes } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const DigitalTwinPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Interactive 3D Digital Twin Viewer"
      phase="Phase 5 Target"
      description="Three.js & React Three Fiber spatial twin rendering real-time building state and sensor heatmaps."
      icon={Boxes}
      plannedFeatures={[
        'Interactive 3D multi-floor structural building rendering (Three.js / R3F)',
        'Live floor slicing and transparent structural x-ray views',
        'Real-time sensor value overlays and spatial heatmaps',
        'Camera controls with orbit, pan, fly-through, and zone inspection',
      ]}
    />
  );
};
