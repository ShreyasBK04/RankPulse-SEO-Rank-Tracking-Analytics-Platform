export function formatPosition(position) {
  if (position === null || position === undefined) return '100+';
  return `#${position}`;
}

export function formatChange(change) {
  if (change === null || change === undefined) return '—';
  if (change > 0) return `+${change}`;
  if (change < 0) return `${change}`;
  return '0';
}

export function formatDate(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '—';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export function timeAgo(dateStr) {
  if (!dateStr) return 'Never';
  const now = new Date();
  const date = new Date(dateStr);
  const seconds = Math.floor((now - date) / 1000);

  if (seconds < 60) return 'Just now';
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ago`;
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h ago`;
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d ago`;
  return formatDate(dateStr);
}

export function getChangeClass(change) {
  if (change === null || change === undefined || change === 0) return '';
  return change > 0 ? 'positive' : 'negative';
}

export function getTrendLabel(trend) {
  switch (trend) {
    case 'up': return 'Improving';
    case 'down': return 'Declining';
    case 'stable': return 'Stable';
    case 'new': return 'New';
    default: return '—';
  }
}
