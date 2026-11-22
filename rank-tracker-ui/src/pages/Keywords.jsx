import React, { useState, useEffect, useCallback } from 'react';
import keywordService from '../services/keywordService';
import rankingService from '../services/rankingService';
import AddKeywordModal from '../components/keywords/AddKeywordModal';
import ConfirmDialog from '../components/common/ConfirmDialog';
import Pagination from '../components/common/Pagination';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { formatPosition, formatChange, formatDate, getChangeClass } from '../utils/formatters';
import './Keywords.css';

export default function Keywords({ selectedWebsiteId, websites, onAddToast }) {
  const [keywordsPage, setKeywordsPage] = useState({ content: [], totalPages: 0, totalElements: 0 });
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  // Modals & Actions
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [isAdding, setIsAdding] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [checkingId, setCheckingId] = useState(null);

  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  const loadKeywords = useCallback(async () => {
    if (!selectedWebsiteId) return;
    try {
      setLoading(true);
      const res = await keywordService.getByWebsite(selectedWebsiteId, {
        search: search.trim() || undefined,
        page,
        size: 15
      });
      setKeywordsPage(res.data);
    } catch (err) {
      onAddToast('Failed to load keywords: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoading(false);
    }
  }, [selectedWebsiteId, search, page, onAddToast]);

  useEffect(() => {
    loadKeywords();
  }, [loadKeywords]);

  const handleAdd = async (keywordText) => {
    if (!selectedWebsiteId) return;
    try {
      setIsAdding(true);
      const res = await keywordService.create(selectedWebsiteId, { keyword: keywordText });
      const pos = res.data.currentPosition;
      onAddToast(
        `Keyword "${keywordText}" added! Initial rank: ${pos ? '#' + pos : '100+ (not in top 100)'}`,
        'success'
      );
      setAddModalOpen(false);
      loadKeywords();
    } catch (err) {
      onAddToast(err.response?.data?.message || 'Failed to add keyword', 'error');
    } finally {
      setIsAdding(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await keywordService.delete(deleteTarget.id);
      onAddToast(`Keyword "${deleteTarget.keyword}" deleted`, 'success');
      setDeleteTarget(null);
      loadKeywords();
    } catch (err) {
      onAddToast(err.response?.data?.message || 'Failed to delete keyword', 'error');
    }
  };

  const handleCheckNow = async (kw) => {
    try {
      setCheckingId(kw.id);
      const res = await rankingService.checkKeyword(kw.id);
      const pos = res.data?.position !== undefined && res.data?.position !== null
        ? (typeof res.data.position === 'number' ? `#${res.data.position}` : res.data.position)
        : 'Checked';
      onAddToast(`Rank check for "${kw.keyword}": ${pos}`, 'success');
      await loadKeywords();
    } catch (err) {
      onAddToast('Failed to check keyword rank: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setCheckingId(null);
    }
  };

  if (!selectedWebsiteId) {
    return (
      <div className="keywords-page">
        <EmptyState
          title="Select a Website"
          description="Choose a website from the header dropdown to view and manage its keywords."
        />
      </div>
    );
  }

  return (
    <div className="keywords-page">
      <div className="keywords-header">
        <div>
          <h1 className="keywords-title">Keywords Management</h1>
          <p className="keywords-subtitle">
            Tracking keywords for <strong style={{ color: '#4F46E5' }}>{selectedWebsite?.domain}</strong>
          </p>
        </div>
        <button className="btn btn--primary" onClick={() => setAddModalOpen(true)}>
          + Add Keyword
        </button>
      </div>

      <div className="keywords-card">
        <div className="keywords-card__toolbar">
          <div className="keywords-search">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
            <input
              type="text"
              placeholder="Search keywords..."
              value={search}
              onChange={(e) => {
                setSearch(e.target.value);
                setPage(0);
              }}
            />
            {search && (
              <button className="keywords-search__clear" onClick={() => setSearch('')}>×</button>
            )}
          </div>
          <span className="keywords-count">{keywordsPage.totalElements} Total Keywords</span>
        </div>

        {loading ? (
          <LoadingSpinner text="Loading keywords..." />
        ) : keywordsPage.content.length === 0 ? (
          <div style={{ padding: '32px' }}>
            <EmptyState
              title={search ? 'No keywords found' : 'No keywords yet'}
              description={search ? `No keywords matching "${search}"` : 'Add your first search keyword to begin tracking rankings.'}
              action={
                !search && (
                  <button className="btn btn--primary" onClick={() => setAddModalOpen(true)}>
                    + Add Keyword
                  </button>
                )
              }
            />
          </div>
        ) : (
          <div className="table-container">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Keyword</th>
                  <th className="num">Current Position</th>
                  <th className="num">Change</th>
                  <th className="num">Best Rank</th>
                  <th>Ranked URL</th>
                  <th>Last Checked</th>
                  <th className="action-col">Actions</th>
                </tr>
              </thead>
              <tbody>
                {keywordsPage.content.map((kw) => (
                  <tr key={kw.id}>
                    <td className="fw-500">{kw.keyword}</td>
                    <td className="num">
                      <span className={`position-badge ${kw.currentPosition && kw.currentPosition <= 10 ? 'position-badge--top10' : ''}`}>
                        {formatPosition(kw.currentPosition)}
                      </span>
                    </td>
                    <td className="num">
                      <span className={`change-indicator ${getChangeClass(kw.change)}`}>
                        {kw.change > 0 && '▲ '}
                        {kw.change < 0 && '▼ '}
                        {formatChange(kw.change)}
                      </span>
                    </td>
                    <td className="num text-muted">
                      {kw.bestPosition ? `#${kw.bestPosition}` : '—'}
                    </td>
                    <td className="url-cell">
                      {kw.searchUrl ? (
                        <a href={kw.searchUrl} target="_blank" rel="noopener noreferrer" className="url-link" title={kw.searchUrl}>
                          {kw.searchUrl.replace(/^https?:\/\//, '').substring(0, 32)}...
                        </a>
                      ) : (
                        <span className="text-muted">—</span>
                      )}
                    </td>
                    <td className="text-muted text-sm">{formatDate(kw.lastChecked)}</td>
                    <td className="action-col">
                      <button
                        className="btn btn--ghost btn--sm"
                        onClick={() => handleCheckNow(kw)}
                        disabled={checkingId === kw.id}
                        title="Force Check Google SERP Now"
                      >
                        {checkingId === kw.id ? 'Checking...' : 'Check Now'}
                      </button>
                      <button
                        className="btn btn--ghost btn--sm btn--danger-text"
                        onClick={() => setDeleteTarget(kw)}
                        title="Delete Keyword"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        <Pagination
          page={page}
          totalPages={keywordsPage.totalPages}
          onPageChange={(newPage) => setPage(newPage)}
        />
      </div>

      {/* MODAL: ADD KEYWORD */}
      <AddKeywordModal
        open={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        onSubmit={handleAdd}
        isSubmitting={isAdding}
      />

      {/* CONFIRM DELETE */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Delete Keyword"
        message={`Are you sure you want to delete "${deleteTarget?.keyword}"? All historical ranking data for this keyword will be permanently deleted.`}
        confirmLabel="Delete Keyword"
        danger={true}
      />
    </div>
  );
}
