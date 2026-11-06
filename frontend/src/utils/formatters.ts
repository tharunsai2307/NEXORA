/**
 * Utility formatters for timestamps and numeric metrics.
 */

export function formatTimestamp(date: Date | string): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export function formatMetric(val: number, decimals: number = 1): string {
  return val.toFixed(decimals);
}
