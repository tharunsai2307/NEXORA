import { useState, useEffect, useCallback } from 'react';
import { SubsystemStatus } from '../types/system';
import { systemService } from '../services/api';

export function useSystemStatus(pollIntervalMs: number = 10000) {
  const [status, setStatus] = useState<SubsystemStatus>({
    backend: 'Checking',
    aiEngine: 'Checking',
    database: 'Checking',
    lastChecked: null,
    responseTimeMs: null,
    error: null,
  });
  const [isRefreshing, setIsRefreshing] = useState<boolean>(false);

  const checkStatus = useCallback(async () => {
    setIsRefreshing(true);
    const startTime = performance.now();
    try {
      const info = await systemService.getSystemInfo();
      const elapsedMs = Math.round(performance.now() - startTime);

      setStatus({
        backend: info.backendStatus === 'UP' ? 'Connected' : 'Disconnected',
        aiEngine: info.aiServiceConnectivityStatus === 'UP' ? 'Connected' : 'Disconnected',
        database: info.databaseConnectivityStatus === 'UP' ? 'Connected' : 'Disconnected',
        lastChecked: new Date().toLocaleTimeString(),
        responseTimeMs: elapsedMs,
        error: null,
      });
    } catch (err: unknown) {
      const elapsedMs = Math.round(performance.now() - startTime);
      const errorMessage = err instanceof Error ? err.message : 'Backend unreachable';
      setStatus({
        backend: 'Disconnected',
        aiEngine: 'Disconnected',
        database: 'Disconnected',
        lastChecked: new Date().toLocaleTimeString(),
        responseTimeMs: elapsedMs,
        error: errorMessage,
      });
    } finally {
      setIsRefreshing(false);
    }
  }, []);

  useEffect(() => {
    checkStatus();
    if (pollIntervalMs > 0) {
      const interval = setInterval(checkStatus, pollIntervalMs);
      return () => clearInterval(interval);
    }
  }, [checkStatus, pollIntervalMs]);

  return { status, checkStatus, isRefreshing };
}
