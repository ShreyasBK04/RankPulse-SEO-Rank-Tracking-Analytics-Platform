import React, { useState, useEffect, useCallback } from 'react';
import alertService from '../services/alertService';
import Pagination from '../components/common/Pagination';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { formatDateTime } from '../utils/formatters';
import './Notifications.css';

export default function Notifications({ selectedWebsiteId, websites, onAddToast, onRefreshUnread }) {
  const [alertsPage, setAlertsPage] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);

  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  const loadAlerts = useCallback(async () => {
    if (!selectedWebsiteId) return;
    try {
      setLoading(true);
      const res = await alertService.getByWebsite(selectedWebsiteId, { page, size: 15 });
      setAlertsPage(res.data);
    } catch (err) {
      onAddToast('Failed to load notifications: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoading(false);
    }
  }, [selectedWebsiteId, page, onAddToast]);

  useEffect(() => {
    loadAlerts();
  }, [loadAlerts]);

  const handleMarkAsRead = async (id) => {
    try {
      await alertService.markAsRead(id);
      loadAlerts();
      if (onRefreshUnread) onRefreshUnread();
    } catch (err) {
      onAddToast('Failed to mark notification as read', 'error');
    }
  };

  const handleMarkAllRead = async () => {
    if (!selectedWebsiteId) return;
    try {
      await alertService.markAllAsRead(selectedWebsiteId);
      onAddToast('All alerts marked as read', 'success');
      loadAlerts();
      if (onRefreshUnread) onRefreshUnread();
    } catch (err) {
      onAddToast('Failed to mark all as read', 'error');
    }
  };

  const getAlertIcon = (type) => {
    switch (type) {
      case 'ENTERED_TOP_3':
      case 'ENTERED_TOP_10':
      case 'SIGNIFICANT_IMPROVEMENT':
        return (
          <div className="alert-badge alert-badge--success">
            ▲
          </div>
        );
      case 'SIGNIFICANT_DECLINE':
      case 'DROPPED_FROM_TOP_100':
        return (
          <div className="alert-badge alert-badge--danger">
            ▼
          </div>
        );
      default:
        return (
          <div className="alert-badge alert-badge--neutral">
            •
          </div>
        );
    }
  };

  if (!selectedWebsiteId) {
    return (
      <div className="notifications-page">
        <EmptyState
          title="Select a Website"
          description="Select a website from the top navigation to view ranking movement alerts."
        />
      </div>
    );
  }

  return (
    <div className="notifications-page">
      <div className="notifications-header">
        <div>
          <h1 className="notifications-title">Ranking Alerts & Notifications</h1>
          <p className="notifications-subtitle">
            Critical SERP shifts recorded for <strong style={{ color: '#4F46E5' }}>{selectedWebsite?.domain}</strong>
          </p>
        </div>

        {alertsPage.totalElements > 0 && (
          <button className="btn btn--secondary btn--sm" onClick={handleMarkAllRead}>
            Mark All as Read
          </button>
        )}
      </div>

      <div className="notifications-card">
        {loading ? (
          <LoadingSpinner text="Loading notifications..." />
        ) : alertsPage.content.length === 0 ? (
          <div style={{ padding: '40px' }}>
            <EmptyState
              icon={
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
              }
              title="No Alerts Generated"
              description="Alerts are triggered automatically when a keyword enters the top 10/top 3, moves 5+ places, or drops from the top 100."
            />
          </div>
        ) : (
          <div className="alerts-list">
            {alertsPage.content.map((alert) => (
              <div
                key={alert.id}
                className={`alert-item ${!alert.read ? 'alert-item--unread' : ''}`}
              >
                <div className="alert-item__icon">
                  {getAlertIcon(alert.alertType)}
                </div>

                <div className="alert-item__content">
                  <div className="alert-item__msg">
                    <strong className="alert-item__keyword">{alert.keyword}</strong>: {alert.message}
                  </div>
                  <span className="alert-item__time">{formatDateTime(alert.createdAt)}</span>
                </div>

                <div className="alert-item__action">
                  {!alert.read ? (
                    <button
                      className="btn btn--ghost btn--sm"
                      onClick={() => handleMarkAsRead(alert.id)}
                    >
                      Mark Read
                    </button>
                  ) : (
                    <span className="alert-item__read-label">Read</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}

        <Pagination
          page={page}
          totalPages={alertsPage.totalPages}
          onPageChange={(p) => setPage(p)}
        />
      </div>
    </div>
  );
}
