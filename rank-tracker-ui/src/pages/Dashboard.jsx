import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import analyticsService from '../services/analyticsService';
import rankingService from '../services/rankingService';
import keywordService from '../services/keywordService';
import RankingTrendChart from '../components/charts/RankingTrendChart';
import DistributionBarChart from '../components/charts/DistributionBarChart';
import KeywordHistoryChart from '../components/charts/KeywordHistoryChart';
import AddKeywordModal from '../components/keywords/AddKeywordModal';
import Modal from '../components/common/Modal';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { formatPosition, formatChange, formatDate, timeAgo, getChangeClass } from '../utils/formatters';
import './Dashboard.css';

export default function Dashboard({ websites, selectedWebsiteId, onSelectWebsite, onAddToast }) {
  const [stats, setStats] = useState(null);
  const [performance, setPerformance] = useState([]);
  const [trends, setTrends] = useState(null);
  const [distribution, setDistribution] = useState(null);
  const [loading, setLoading] = useState(false);
  const [checkingRankings, setCheckingRankings] = useState(false);

  // Table filters and search
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL'); // ALL, TOP10, IMPROVED, DECLINED, NOT_RANKING
  const [sortField, setSortField] = useState('keyword');
  const [sortOrder, setSortOrder] = useState('asc');

  // Modals
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addingKeyword, setAddingKeyword] = useState(false);
  const [selectedKeywordForHistory, setSelectedKeywordForHistory] = useState(null);
  const [keywordHistory, setKeywordHistory] = useState(null);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  const loadDashboardData = useCallback(async () => {
    if (!selectedWebsiteId) return;
    try {
      setLoading(true);
      const [statsRes, perfRes, trendsRes, distRes] = await Promise.all([
        analyticsService.getDashboardStats(selectedWebsiteId),
        analyticsService.getPerformance(selectedWebsiteId),
        analyticsService.getTrends(selectedWebsiteId),
        analyticsService.getDistribution(selectedWebsiteId)
      ]);
      setStats(statsRes.data);
      setPerformance(perfRes.data);
      setTrends(trendsRes.data);
      setDistribution(distRes.data);
    } catch (err) {
      onAddToast('Failed to load dashboard data: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoading(false);
    }
  }, [selectedWebsiteId, onAddToast]);

  useEffect(() => {
    loadDashboardData();
  }, [loadDashboardData]);

  const handleCheckAllRankings = async () => {
    if (!selectedWebsiteId) return;
    try {
      setCheckingRankings(true);
      const res = await rankingService.checkWebsite(selectedWebsiteId);
      onAddToast(`Rank check completed! ${res.data.keywordsChecked} keywords checked.`, 'success');
      loadDashboardData();
    } catch (err) {
      onAddToast('Rank check failed: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setCheckingRankings(false);
    }
  };

  const handleAddKeyword = async (keywordText) => {
    if (!selectedWebsiteId) return;
    try {
      setAddingKeyword(true);
      await keywordService.create(selectedWebsiteId, { keyword: keywordText });
      onAddToast(`Keyword "${keywordText}" added successfully!`, 'success');
      setAddModalOpen(false);
      loadDashboardData();
    } catch (err) {
      onAddToast(err.response?.data?.message || 'Failed to add keyword', 'error');
    } finally {
      setAddingKeyword(false);
    }
  };

  const handleViewHistory = async (kw) => {
    setSelectedKeywordForHistory(kw);
    try {
      setLoadingHistory(true);
      const res = await rankingService.getHistory(kw.keywordId);
      setKeywordHistory(res.data);
    } catch (err) {
      onAddToast('Failed to load history for keyword: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoadingHistory(false);
    }
  };

  // Filter and sort keywords
  const filteredPerformance = useMemo(() => {
    return performance
      .filter((row) => {
        if (search.trim()) {
          const s = search.toLowerCase();
          if (!row.keyword.toLowerCase().includes(s)) return false;
        }
        if (statusFilter === 'TOP10') {
          return row.currentPosition !== null && row.currentPosition <= 10;
        }
        if (statusFilter === 'IMPROVED') {
          return row.change !== null && row.change > 0;
        }
        if (statusFilter === 'DECLINED') {
          return row.change !== null && row.change < 0;
        }
        if (statusFilter === 'NOT_RANKING') {
          return row.currentPosition === null;
        }
        return true;
      })
      .sort((a, b) => {
        let valA = a[sortField];
        let valB = b[sortField];

        if (sortField === 'currentPosition') {
          valA = valA === null ? 999 : valA;
          valB = valB === null ? 999 : valB;
        } else if (sortField === 'change') {
          valA = valA === null ? -999 : valA;
          valB = valB === null ? -999 : valB;
        }

        if (valA < valB) return sortOrder === 'asc' ? -1 : 1;
        if (valA > valB) return sortOrder === 'asc' ? 1 : -1;
        return 0;
      });
  }, [performance, search, statusFilter, sortField, sortOrder]);

  const handleSort = (field) => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  if (websites.length === 0) {
    return (
      <div className="dashboard-page">
        <EmptyState
          icon={
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
              <circle cx="12" cy="12" r="10" />
              <path d="M2 12h20M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
            </svg>
          }
          title="No Websites Tracked Yet"
          description="Add your first website to start monitoring Google keyword rankings and SEO performance."
          action={
            <button className="btn btn--primary" onClick={() => window.location.href = '/websites'}>
              Add Website
            </button>
          }
        />
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      {/* SECTION 1: HEADER & ACTIONS */}
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-title">SEO Overview</h1>
          <div className="dashboard-subtitle">
            <span className="dashboard-domain">{selectedWebsite?.name || selectedWebsite?.domain}</span>
            {stats?.lastChecked && (
              <span className="dashboard-updated">
                • Updated {timeAgo(stats.lastChecked)}
              </span>
            )}
          </div>
        </div>
        <div className="dashboard-actions">
          <button
            className="btn btn--secondary"
            onClick={handleCheckAllRankings}
            disabled={checkingRankings || loading}
          >
            {checkingRankings ? 'Checking Rankings...' : 'Check Rankings Now'}
          </button>
          <button
            className="btn btn--primary"
            onClick={() => setAddModalOpen(true)}
            disabled={loading}
          >
            + Add Keyword
          </button>
        </div>
      </div>

      {loading && !stats ? (
        <LoadingSpinner text="Loading dashboard analytics..." />
      ) : (
        <>
          {/* SECTION 1: STATS CARDS */}
          <div className="stats-grid">
            <div className="stat-card">
              <span className="stat-card__label">Total Keywords</span>
              <div className="stat-card__value">{stats?.totalKeywords || 0}</div>
              <span className="stat-card__footer">Tracked for this domain</span>
            </div>

            <div className="stat-card">
              <span className="stat-card__label">Average Position</span>
              <div className="stat-card__value">
                {stats?.averagePosition ? `#${stats.averagePosition}` : '—'}
              </div>
              <span className="stat-card__footer">Across ranked keywords</span>
            </div>

            <div className="stat-card">
              <span className="stat-card__label">Top 10 Keywords</span>
              <div className="stat-card__value stat-card__value--highlight">
                {stats?.top10Keywords || 0}
              </div>
              <span className="stat-card__footer">First page visibility</span>
            </div>

            <div className="stat-card">
              <span className="stat-card__label">Keyword Movements</span>
              <div className="stat-card__split">
                <span className="movement movement--up">▲ {stats?.improvedKeywords || 0} Improved</span>
                <span className="movement movement--down">▼ {stats?.declinedKeywords || 0} Declined</span>
              </div>
              <span className="stat-card__footer">Compared to previous check</span>
            </div>
          </div>

          {/* SECTION 2: CHARTS */}
          <div className="charts-grid">
            <div className="chart-card">
              <div className="chart-card__header">
                <div>
                  <h3 className="chart-card__title">Average Position Trend</h3>
                  <p className="chart-card__desc">Historical daily ranking progression (#1 is highest)</p>
                </div>
              </div>
              <RankingTrendChart data={trends?.dataPoints} height={260} />
            </div>

            <div className="chart-card">
              <div className="chart-card__header">
                <div>
                  <h3 className="chart-card__title">Ranking Distribution</h3>
                  <p className="chart-card__desc">Spread across Google position brackets</p>
                </div>
              </div>
              <DistributionBarChart distribution={distribution} height={260} />
            </div>
          </div>

          {/* SECTION 3: KEYWORD PERFORMANCE TABLE */}
          <div className="table-card">
            <div className="table-card__header">
              <div className="table-card__title-group">
                <h3 className="table-card__title">Tracked Keywords</h3>
                <span className="table-card__badge">{filteredPerformance.length} results</span>
              </div>

              <div className="table-filters">
                <div className="table-search">
                  <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <circle cx="11" cy="11" r="8" />
                    <line x1="21" y1="21" x2="16.65" y2="16.65" />
                  </svg>
                  <input
                    type="text"
                    placeholder="Search keywords..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                  {search && (
                    <button className="table-search__clear" onClick={() => setSearch('')}>×</button>
                  )}
                </div>

                <select
                  className="table-select"
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <option value="ALL">All Statuses</option>
                  <option value="TOP10">Top 10 Only</option>
                  <option value="IMPROVED">Improved Only</option>
                  <option value="DECLINED">Declined Only</option>
                  <option value="NOT_RANKING">100+ (Unranked)</option>
                </select>
              </div>
            </div>

            {filteredPerformance.length === 0 ? (
              <div className="table-empty">
                {performance.length === 0 ? (
                  <EmptyState
                    title="No Keywords Tracked"
                    description="Add keywords your target audience searches for to monitor rankings."
                    action={
                      <button className="btn btn--primary" onClick={() => setAddModalOpen(true)}>
                        + Add First Keyword
                      </button>
                    }
                  />
                ) : (
                  <p style={{ color: '#6B7280', padding: '32px', textAlign: 'center' }}>
                    No keywords match your filter criteria.
                  </p>
                )}
              </div>
            ) : (
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th onClick={() => handleSort('keyword')} className="sortable">
                        Keyword {sortField === 'keyword' && (sortOrder === 'asc' ? '↑' : '↓')}
                      </th>
                      <th onClick={() => handleSort('currentPosition')} className="sortable num">
                        Current Position {sortField === 'currentPosition' && (sortOrder === 'asc' ? '↑' : '↓')}
                      </th>
                      <th onClick={() => handleSort('previousPosition')} className="sortable num">
                        Previous
                      </th>
                      <th onClick={() => handleSort('change')} className="sortable num">
                        Change {sortField === 'change' && (sortOrder === 'asc' ? '↑' : '↓')}
                      </th>
                      <th className="num">Best Rank</th>
                      <th>Ranking URL</th>
                      <th>Last Checked</th>
                      <th className="action-col">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPerformance.map((row) => (
                      <tr key={row.keywordId}>
                        <td className="fw-500">{row.keyword}</td>
                        <td className="num">
                          <span className={`position-badge ${row.currentPosition && row.currentPosition <= 10 ? 'position-badge--top10' : ''}`}>
                            {formatPosition(row.currentPosition)}
                          </span>
                        </td>
                        <td className="num text-muted">
                          {formatPosition(row.previousPosition)}
                        </td>
                        <td className="num">
                          <span className={`change-indicator ${getChangeClass(row.change)}`}>
                            {row.change > 0 && '▲ '}
                            {row.change < 0 && '▼ '}
                            {formatChange(row.change)}
                          </span>
                        </td>
                        <td className="num text-muted">
                          {row.bestPosition ? `#${row.bestPosition}` : '—'}
                        </td>
                        <td className="url-cell">
                          {row.searchUrl ? (
                            <a href={row.searchUrl} target="_blank" rel="noopener noreferrer" className="url-link" title={row.searchUrl}>
                              {row.searchUrl.replace(/^https?:\/\//, '').substring(0, 32)}...
                            </a>
                          ) : (
                            <span className="text-muted">—</span>
                          )}
                        </td>
                        <td className="text-muted text-sm">{formatDate(row.lastChecked)}</td>
                        <td className="action-col">
                          <button
                            className="btn btn--ghost btn--sm"
                            onClick={() => handleViewHistory(row)}
                            title="View Ranking History"
                          >
                            History
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}

      {/* MODAL: ADD KEYWORD */}
      <AddKeywordModal
        open={addModalOpen}
        onClose={() => setAddModalOpen(false)}
        onSubmit={handleAddKeyword}
        isSubmitting={addingKeyword}
      />

      {/* MODAL: KEYWORD HISTORY */}
      <Modal
        open={!!selectedKeywordForHistory}
        onClose={() => {
          setSelectedKeywordForHistory(null);
          setKeywordHistory(null);
        }}
        title={`Ranking History: "${selectedKeywordForHistory?.keyword}"`}
        maxWidth={580}
      >
        {loadingHistory ? (
          <LoadingSpinner text="Loading history..." />
        ) : (
          <div>
            <div style={{ display: 'flex', gap: 16, marginBottom: 16, padding: '12px', background: '#F9FAFB', borderRadius: 8 }}>
              <div>
                <span style={{ fontSize: 11, color: '#6B7280', textTransform: 'uppercase', display: 'block' }}>Current</span>
                <strong style={{ fontSize: 16 }}>{formatPosition(selectedKeywordForHistory?.currentPosition)}</strong>
              </div>
              <div style={{ borderLeft: '1px solid #E5E7EB', paddingLeft: 16 }}>
                <span style={{ fontSize: 11, color: '#6B7280', textTransform: 'uppercase', display: 'block' }}>Best Achieved</span>
                <strong style={{ fontSize: 16, color: '#16A34A' }}>
                  {keywordHistory?.bestPosition ? `#${keywordHistory.bestPosition}` : '—'}
                </strong>
              </div>
              <div style={{ borderLeft: '1px solid #E5E7EB', paddingLeft: 16 }}>
                <span style={{ fontSize: 11, color: '#6B7280', textTransform: 'uppercase', display: 'block' }}>Checks Logged</span>
                <strong style={{ fontSize: 16 }}>{keywordHistory?.history?.length || 0}</strong>
              </div>
            </div>

            <KeywordHistoryChart history={keywordHistory?.history} height={240} />
          </div>
        )}
      </Modal>
    </div>
  );
}
