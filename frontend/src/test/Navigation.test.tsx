import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
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

describe('Navigation Routes', () => {
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

  it('navigates to Buildings route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Buildings/i });
    fireEvent.click(link);
    expect(screen.getByText('Building Topology Management')).toBeInTheDocument();
  });

  it('navigates to Sensors route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Sensors/i });
    fireEvent.click(link);
    expect(screen.getByText('Virtual IoT Sensor Registry')).toBeInTheDocument();
  });

  it('navigates to Digital Twin route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Digital Twin/i });
    fireEvent.click(link);
    expect(screen.getByText('Interactive 3D Digital Twin Viewer')).toBeInTheDocument();
  });

  it('navigates to Occupancy route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Occupancy/i });
    fireEvent.click(link);
    expect(screen.getByText('Edge-AI Occupancy Prediction & Analytics')).toBeInTheDocument();
  });

  it('navigates to Safety route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Safety/i });
    fireEvent.click(link);
    expect(screen.getByText('Environmental Safety & Anomaly Detection')).toBeInTheDocument();
  });

  it('navigates to Emergency route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Emergency/i });
    fireEvent.click(link);
    expect(screen.getByText('Emergency Response & Evacuation Routing')).toBeInTheDocument();
  });

  it('navigates to Analytics route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Analytics/i });
    fireEvent.click(link);
    expect(screen.getByText('Historical Telemetry & Building Analytics')).toBeInTheDocument();
  });

  it('navigates to Settings route when clicked', () => {
    render(<App />);
    const link = screen.getByRole('link', { name: /Settings/i });
    fireEvent.click(link);
    expect(screen.getByText('Platform & Gateway Configuration')).toBeInTheDocument();
  });
});
