import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';
import Dashboard from './pages/Dashboard';
import Websites from './pages/Websites';
import Keywords from './pages/Keywords';
import Analytics from './pages/Analytics';
import RankingHistory from './pages/RankingHistory';
import Notifications from './pages/Notifications';
import Settings from './pages/Settings';
import { useWebsites, useSelectedWebsite, useUnreadAlerts } from './hooks/useAppData';
import { useToast } from './hooks/useToast';
import './styles/global.css';

export default function App() {
  const { websites, loading: loadingWebsites, refetch: refetchWebsites } = useWebsites();
  const { selectedWebsiteId, selectWebsite } = useSelectedWebsite();
  const { count: unreadCount, refetch: refetchUnread } = useUnreadAlerts(selectedWebsiteId);
  const { toasts, addToast, removeToast } = useToast();

  // If no website selected yet, select the first one once loaded
  useEffect(() => {
    if (!loadingWebsites && websites.length > 0 && !selectedWebsiteId) {
      selectWebsite(websites[0].id);
    }
  }, [websites, loadingWebsites, selectedWebsiteId, selectWebsite]);

  return (
    <BrowserRouter>
      <Routes>
        <Route
          path="/"
          element={
            <Layout
              websites={websites}
              selectedWebsiteId={selectedWebsiteId}
              onSelectWebsite={selectWebsite}
              unreadCount={unreadCount}
              toasts={toasts}
              onRemoveToast={removeToast}
            />
          }
        >
          <Route
            index
            element={
              <Dashboard
                websites={websites}
                selectedWebsiteId={selectedWebsiteId}
                onSelectWebsite={selectWebsite}
                onAddToast={addToast}
              />
            }
          />
          <Route
            path="websites"
            element={
              <Websites
                websites={websites}
                selectedWebsiteId={selectedWebsiteId}
                onSelectWebsite={selectWebsite}
                onRefreshWebsites={refetchWebsites}
                onAddToast={addToast}
              />
            }
          />
          <Route
            path="keywords"
            element={
              <Keywords
                selectedWebsiteId={selectedWebsiteId}
                websites={websites}
                onAddToast={addToast}
              />
            }
          />
          <Route
            path="analytics"
            element={
              <Analytics
                selectedWebsiteId={selectedWebsiteId}
                websites={websites}
                onAddToast={addToast}
              />
            }
          />
          <Route
            path="ranking-history"
            element={
              <RankingHistory
                selectedWebsiteId={selectedWebsiteId}
                websites={websites}
                onAddToast={addToast}
              />
            }
          />
          <Route
            path="notifications"
            element={
              <Notifications
                selectedWebsiteId={selectedWebsiteId}
                websites={websites}
                onAddToast={addToast}
                onRefreshUnread={refetchUnread}
              />
            }
          />
          <Route
            path="settings"
            element={<Settings onAddToast={addToast} />}
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
