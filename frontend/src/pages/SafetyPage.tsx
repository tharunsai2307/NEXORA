import React from 'react';
import { ShieldCheck } from 'lucide-react';
import { PlaceholderPage } from './PlaceholderPage';

export const SafetyPage: React.FC = () => {
  return (
    <PlaceholderPage
      title="Environmental Safety & Anomaly Detection"
      phase="Phase 4 Target"
      description="Real-time environmental hazard monitoring, air quality thresholds, and anomaly detection."
      icon={ShieldCheck}
      plannedFeatures={[
        'Unsupervised anomaly detection (Isolation Forests / One-Class SVM)',
        'Indoor Air Quality (IAQ) compliance grading',
        'Smoke, CO, and thermal spike instantaneous hazard triggers',
        'Configurable threshold rule engine with automated notifications',
      ]}
    />
  );
};
