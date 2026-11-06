import React from 'react';
import { Building2 } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const BuildingsPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Building Topology Management"
      phase="Phase 2 Target"
      description="Hierarchical structural model managing campuses, buildings, floors, zones, and room spaces."
      icon={Building2}
      plannedFeatures={[
        'Campus, Building, Floor, and Room relational schemas',
        'Floor plan spatial coordinate mapping',
        'Room capacity and metadata configurations',
        'Spatial zoning for HVAC and evacuation sectors',
      ]}
    />
  );
};
