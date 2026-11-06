import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import App from '../App';
import * as apiModule from '../services/api';

vi.mock('../services/api', () => ({
  systemService: {
    getSystemInfo: vi.fn(),
    getBackendHealth: vi.fn(),
    getAiHealth: vi.fn(),
    getDatabaseHealth: vi.fn(),
  },
  apiClient: {},
}));

describe('Application Bootstrap and Layout', () => {
  beforeEach(() => {
    vi.mocked(apiModule.systemService.getSystemInfo).mockResolvedValue({
      applicationName: 'smart-building-backend',
      applicationVersion: '1.0.0-SNAPSHOT',
      backendStatus: 'UP',
      databaseConnectivityStatus: 'UP',
      aiServiceConnectivityStatus: 'UP',
      environment: 'dev',
      subsystemDetails: {},
      timestamp: new Date().toISOString(),
    });
  });

  it('renders application branding and navigation sidebar', () => {
    render(<App />);
    expect(screen.getByText(/AEGIS/i)).toBeInTheDocument();
    expect(screen.getAllByText(/TWIN/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Phase 1 of 7')).toBeInTheDocument();
    expect(screen.getByText('Navigation Console')).toBeInTheDocument();
  });

  it('renders dashboard mission control page by default', () => {
    render(<App />);
    expect(
      screen.getByText('Smart Building Safety & Occupancy Digital Twin')
    ).toBeInTheDocument();
    expect(screen.getByText('Enterprise Subsystem Heartbeat')).toBeInTheDocument();
  });
});
