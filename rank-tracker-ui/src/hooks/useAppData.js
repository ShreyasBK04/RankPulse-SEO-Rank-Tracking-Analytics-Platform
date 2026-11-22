import { useState, useEffect, useCallback } from 'react';
import websiteService from '../services/websiteService';
import alertService from '../services/alertService';

export function useWebsites() {
  const [websites, setWebsites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchWebsites = useCallback(async () => {
    try {
      setLoading(true);
      const res = await websiteService.getAll();
      setWebsites(res.data);
      setError(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load websites');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchWebsites(); }, [fetchWebsites]);

  return { websites, loading, error, refetch: fetchWebsites };
}

export function useSelectedWebsite() {
  const [selectedWebsiteId, setSelectedWebsiteId] = useState(() => {
    const saved = localStorage.getItem('selectedWebsiteId');
    return saved ? Number(saved) : null;
  });

  const selectWebsite = useCallback((id) => {
    setSelectedWebsiteId(id);
    if (id) {
      localStorage.setItem('selectedWebsiteId', id.toString());
    } else {
      localStorage.removeItem('selectedWebsiteId');
    }
  }, []);

  return { selectedWebsiteId, selectWebsite };
}

export function useUnreadAlerts(websiteId) {
  const [count, setCount] = useState(0);

  const fetchCount = useCallback(async () => {
    if (!websiteId) return;
    try {
      const res = await alertService.getUnreadCount(websiteId);
      setCount(res.data.count);
    } catch {
      // silent fail for notification badge
    }
  }, [websiteId]);

  useEffect(() => {
    fetchCount();
    const interval = setInterval(fetchCount, 60000);
    return () => clearInterval(interval);
  }, [fetchCount]);

  return { count, refetch: fetchCount };
}
