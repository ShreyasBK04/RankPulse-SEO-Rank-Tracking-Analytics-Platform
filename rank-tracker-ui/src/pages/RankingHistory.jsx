import React, { useState, useEffect, useCallback } from 'react';
import keywordService from '../services/keywordService';
import rankingService from '../services/rankingService';
import KeywordHistoryChart from '../components/charts/KeywordHistoryChart';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { formatPosition, formatDate } from '../utils/formatters';
import './RankingHistory.css';

export default function RankingHistory({ selectedWebsiteId, websites, onAddToast }) {
  const [keywords, setKeywords] = useState([]);
  const [selectedKeywordId, setSelectedKeywordId] = useState(null);
  const [historyData, setHistoryData] = useState(null);
  const [allRankings, setAllRankings] = useState([]);
  const [loadingKeywords, setLoadingKeywords] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);

  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  // Load keywords list for selector
  useEffect(() => {
    if (!selectedWebsiteId) return;
    const fetchKeywords = async () => {
      try {
        setLoadingKeywords(true);
        const res = await keywordService.getByWebsite(selectedWebsiteId, { size: 100 });
        const list = res.data.content || [];
        setKeywords(list);
        if (list.length > 0) {
          setSelectedKeywordId(list[0].id);
        } else {
          setSelectedKeywordId(null);
          setHistoryData(null);
          setAllRankings([]);
        }
      } catch (err) {
        onAddToast('Failed to load keywords: ' + (err.response?.data?.message || err.message), 'error');
      } finally {
        setLoadingKeywords(false);
      }
    };
    fetchKeywords();
  }, [selectedWebsiteId, onAddToast]);

  // Load history for selected keyword
  const loadHistory = useCallback(async () => {
    if (!selectedKeywordId) return;
    try {
      setLoadingHistory(true);
      const [historyRes, allRes] = await Promise.all([
        rankingService.getHistory(selectedKeywordId),
        rankingService.getAllRankings(selectedKeywordId)
      ]);
      setHistoryData(historyRes.data);
      setAllRankings(allRes.data);
    } catch (err) {
      onAddToast('Failed to load ranking history: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoadingHistory(false);
    }
  }, [selectedKeywordId, onAddToast]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  const selectedKeyword = keywords.find((k) => k.id === selectedKeywordId);

  if (!selectedWebsiteId) {
    return (
      <div className="history-page">
        <EmptyState
          title="Select a Website"
          description="Select a website from the top navigation to view keyword historical tracking data."
        />
      </div>
    );
  }

  return (
    <div className="history-page">
      <div className="history-header">
        <div>
          <h1 className="history-title">Ranking History</h1>
          <p className="history-subtitle">
            Historical Google position logs for <strong style={{ color: '#4F46E5' }}>{selectedWebsite?.domain}</strong>
          </p>
        </div>

        {keywords.length > 0 && (
          <div className="history-selector">
            <label className="history-selector__label">Select Keyword:</label>
            <select
              className="history-select"
              value={selectedKeywordId || ''}
              onChange={(e) => setSelectedKeywordId(Number(e.target.value))}
            >
              {keywords.map((k) => (
                <option key={k.id} value={k.id}>
                  {k.keyword}
                </option>
              ))}
            </select>
          </div>
        )}
      </div>

      {loadingKeywords ? (
        <LoadingSpinner text="Loading keyword list..." />
      ) : keywords.length === 0 ? (
        <EmptyState
          title="No Keywords Found"
          description="Add keywords to this website first to start logging ranking history."
        />
      ) : (
        <>
          {/* STATS HIGHLIGHT */}
          <div className="history-meta-bar">
            <div className="history-meta-item">
              <span className="history-meta-item__label">Keyword Query</span>
              <strong className="history-meta-item__value">{selectedKeyword?.keyword}</strong>
            </div>
            <div className="history-meta-item">
              <span className="history-meta-item__label">Current Position</span>
              <strong className="history-meta-item__value">
                {formatPosition(historyData?.currentPosition)}
              </strong>
            </div>
            <div className="history-meta-item">
              <span className="history-meta-item__label">All-Time Best</span>
              <strong className="history-meta-item__value" style={{ color: '#16A34A' }}>
                {historyData?.bestPosition ? `#${historyData.bestPosition}` : '—'}
              </strong>
            </div>
            <div className="history-meta-item">
              <span className="history-meta-item__label">Days Recorded</span>
              <strong className="history-meta-item__value">{allRankings.length}</strong>
            </div>
          </div>

          {/* CHART */}
          <div className="history-card">
            <div className="history-card__header">
              <h3 className="history-card__title">Position Timeline (Google Top 100)</h3>
              <span className="text-muted text-sm">#1 represents Google's first organic search result</span>
            </div>
            {loadingHistory ? (
              <LoadingSpinner text="Rendering chart..." />
            ) : (
              <KeywordHistoryChart history={historyData?.history} height={280} />
            )}
          </div>

          {/* DETAILED LOG TABLE */}
          <div className="history-card">
            <div className="history-card__header">
              <h3 className="history-card__title">Daily Ranking Log</h3>
              <span className="text-muted text-sm">{allRankings.length} total entries</span>
            </div>

            {allRankings.length === 0 ? (
              <p style={{ padding: '24px', color: '#6B7280', textAlign: 'center' }}>
                No rankings recorded yet for this keyword.
              </p>
            ) : (
              <div className="table-container">
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Date Checked</th>
                      <th className="num">Position</th>
                      <th>Ranked URL</th>
                      <th>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {allRankings.map((r) => (
                      <tr key={r.id}>
                        <td className="fw-500">{formatDate(r.checkedOn)}</td>
                        <td className="num">
                          <span className={`position-badge ${r.position && r.position <= 10 ? 'position-badge--top10' : ''}`}>
                            {formatPosition(r.position)}
                          </span>
                        </td>
                        <td className="url-cell">
                          {r.searchUrl ? (
                            <a href={r.searchUrl} target="_blank" rel="noopener noreferrer" className="url-link">
                              {r.searchUrl}
                            </a>
                          ) : (
                            <span className="text-muted">Not ranking in top 100</span>
                          )}
                        </td>
                        <td className="text-muted text-sm">
                          {new Date(r.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
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
    </div>
  );
}
