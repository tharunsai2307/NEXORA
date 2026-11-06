import React from 'react';
import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { SystemStatusBar } from '../components/common/SystemStatusBar';
import { SubsystemStatus } from '../types/system';

describe('SystemStatusBar Component', () => {
  it('renders all services as Connected when status is healthy', () => {
    const mockStatus: SubsystemStatus = {
      backend: 'Connected',
      aiEngine: 'Connected',
      database: 'Connected',
      lastChecked: '10:00:00 AM',
      responseTimeMs: 24,
      error: null,
    };

    render(<SystemStatusBar status={mockStatus} onRefresh={vi.fn()} isRefreshing={false} />);

    expect(screen.getByText('Enterprise Subsystem Heartbeat')).toBeInTheDocument();
    expect(screen.getByTestId('status-badge-backend')).toHaveTextContent('Connected');
    expect(screen.getByTestId('status-badge-ai-engine')).toHaveTextContent('Connected');
    expect(screen.getByTestId('status-badge-database')).toHaveTextContent('Connected');
    expect(screen.getByText('24ms')).toBeInTheDocument();
  });

  it('renders services as Disconnected when downstream systems are unavailable', () => {
    const mockStatus: SubsystemStatus = {
      backend: 'Disconnected',
      aiEngine: 'Disconnected',
      database: 'Disconnected',
      lastChecked: '10:05:00 AM',
      responseTimeMs: 5000,
      error: 'Connection timed out',
    };

    render(<SystemStatusBar status={mockStatus} onRefresh={vi.fn()} isRefreshing={false} />);

    expect(screen.getByTestId('status-badge-backend')).toHaveTextContent('Disconnected');
    expect(screen.getByTestId('status-badge-ai-engine')).toHaveTextContent('Disconnected');
    expect(screen.getByTestId('status-badge-database')).toHaveTextContent('Disconnected');
    expect(screen.getByText(/Connection timed out/i)).toBeInTheDocument();
  });

  it('renders services as Checking during initialization or refresh probe', () => {
    const mockStatus: SubsystemStatus = {
      backend: 'Checking',
      aiEngine: 'Checking',
      database: 'Checking',
      lastChecked: null,
      responseTimeMs: null,
      error: null,
    };

    render(<SystemStatusBar status={mockStatus} onRefresh={vi.fn()} isRefreshing={true} />);

    expect(screen.getByTestId('status-badge-backend')).toHaveTextContent('Checking');
    expect(screen.getByTestId('status-badge-ai-engine')).toHaveTextContent('Checking');
    expect(screen.getByTestId('status-badge-database')).toHaveTextContent('Checking');
  });
});
