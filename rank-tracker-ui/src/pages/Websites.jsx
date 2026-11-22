import React, { useState } from 'react';
import websiteService from '../services/websiteService';
import AddWebsiteModal from '../components/websites/AddWebsiteModal';
import ConfirmDialog from '../components/common/ConfirmDialog';
import EmptyState from '../components/common/EmptyState';
import { formatDate } from '../utils/formatters';
import './Websites.css';

export default function Websites({
  websites,
  selectedWebsiteId,
  onSelectWebsite,
  onRefreshWebsites,
  onAddToast,
}) {
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);

  const handleCreate = async (data) => {
    try {
      setIsSubmitting(true);
      const res = await websiteService.create(data);
      onAddToast(`Website "${res.data.domain}" added successfully!`, 'success');
      setAddModalOpen(false);
      await onRefreshWebsites();
      if (!selectedWebsiteId) {
        onSelectWebsite(res.data.id);
      }
    } catch (err) {
      onAddToast(err.response?.data?.message || 'Failed to create website', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await websiteService.delete(deleteTarget.id);
      onAddToast(`Website "${deleteTarget.domain}" deleted`, 'success');
      setDeleteTarget(null);
      await onRefreshWebsites();
      if (selectedWebsiteId === deleteTarget.id) {
        onSelectWebsite(null);
      }
    } catch (err) {
      onAddToast(err.response?.data?.message || 'Failed to delete website', 'error');
    }
  };

  return (
    <div className="websites-page">
      <div className="websites-header">
        <div>
          <h1 className="websites-title">Tracked Websites</h1>
          <p className="websites-subtitle">Manage domains and properties you monitor in Google search</p>
        </div>
        <button className="btn btn--primary" onClick={() => setAddModalOpen(true)}>
          + Add Website
        </button>
      </div>

      {websites.length === 0 ? (
        <EmptyState
          icon={
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="12" cy="12" r="10" />
              <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
            </svg>
          }
          title="No Websites Tracked"
          description="Add domains you want to track keywords and Google rankings for."
          action={
            <button className="btn btn--primary" onClick={() => setAddModalOpen(true)}>
              + Add First Website
            </button>
          }
        />
      ) : (
        <div className="websites-grid">
          {websites.map((w) => {
            const isSelected = w.id === selectedWebsiteId;
            return (
              <div key={w.id} className={`website-card ${isSelected ? 'website-card--selected' : ''}`}>
                <div className="website-card__header">
                  <div className="website-card__icon">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <circle cx="12" cy="12" r="10" />
                      <line x1="2" y1="12" x2="22" y2="12" />
                    </svg>
                  </div>
                  <div className="website-card__meta">
                    <h3 className="website-card__name">{w.name || w.domain}</h3>
                    <span className="website-card__domain">{w.domain}</span>
                  </div>
                  {isSelected && (
                    <span className="website-card__badge">Active</span>
                  )}
                </div>

                <div className="website-card__body">
                  <div className="website-card__stat">
                    <span className="website-card__stat-label">Tracked Keywords</span>
                    <strong className="website-card__stat-value">{w.keywordCount || 0}</strong>
                  </div>
                  <div className="website-card__stat">
                    <span className="website-card__stat-label">Added Date</span>
                    <span className="website-card__stat-date">{formatDate(w.createdAt)}</span>
                  </div>
                </div>

                <div className="website-card__footer">
                  <button
                    className={`btn btn--sm ${isSelected ? 'btn--secondary' : 'btn--primary'}`}
                    onClick={() => onSelectWebsite(w.id)}
                  >
                    {isSelected ? 'Viewing' : 'Select'}
                  </button>
                  <button
                    className="btn btn--ghost btn--sm btn--danger-text"
                    onClick={() => setDeleteTarget(w)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* MODAL: ADD WEBSITE */}
      <AddWebsiteModal
        open={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        onSubmit={handleCreate}
        isSubmitting={isSubmitting}
      />

      {/* CONFIRM DELETE DIALOG */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Delete Website"
        message={`Are you sure you want to delete "${deleteTarget?.domain}"? All associated keywords, ranking history, and alerts will be permanently removed.`}
        confirmLabel="Delete Website"
        danger={true}
      />
    </div>
  );
}
