import React, { useState, useEffect, useCallback } from 'react';
import analyticsService from '../services/analyticsService';
import RankingTrendChart from '../components/charts/RankingTrendChart';
import DistributionBarChart from '../components/charts/DistributionBarChart';
import LoadingSpinner from '../components/common/LoadingSpinner';
import EmptyState from '../components/common/EmptyState';
import { formatPosition, formatChange } from '../utils/formatters';
import './Analytics.css';

export default function Analytics({ selectedWebsiteId, websites, onAddToast }) {
  const [trends, setTrends] = useState(null);
  const [distribution, setDistribution] = useState(null);
  const [performance, setPerformance] = useState([]);
  const [loading, setLoading] = useState(false);
  const [daysRange, setDaysRange] = useState(30);

  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  const loadAnalytics = useCallback(async () => {
    if (!selectedWebsiteId) return;
    try {
      setLoading(true);
      const endDate = new Date().toISOString().split('T')[0];
      const startDate = new Date(Date.now() - daysRange * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      const [trendsRes, distRes, perfRes] = await Promise.all([
        analyticsService.getTrends(selectedWebsiteId, { startDate, endDate }),
        analyticsService.getDistribution(selectedWebsiteId),
        analyticsService.getPerformance(selectedWebsiteId)
      ]);

      setTrends(trendsRes.data);
      setDistribution(distRes.data);
      setPerformance(perfRes.data);
    } catch (err) {
      onAddToast('Failed to load analytics: ' + (err.response?.data?.message || err.message), 'error');
    } finally {
      setLoading(false);
    }
  }, [selectedWebsiteId, daysRange, onAddToast]);

  useEffect(() => {
    loadAnalytics();
  }, [loadAnalytics]);

  // Top gainers and losers
  const topGainers = performance
    .filter((k) => k.change !== null && k.change > 0)
    .sort((a, b) => b.change - a.change)
    .slice(0, 5);

  const topLosers = performance
    .filter((k) => k.change !== null && k.change < 0)
    .sort((a, b) => a.change - b.change)
    .slice(0, 5);

  if (!selectedWebsiteId) {
    return (
      <div className="analytics-page">
        <EmptyState
          title="Select a Website"
          description="Select a website from the header dropdown to view detailed SEO rank analytics."
        />
      </div>
    );
  }

  return (
    <div className="analytics-page">
      <div className="analytics-header">
        <div>
          <h1 className="analytics-title">Ranking Analytics</h1>
          <p className="analytics-subtitle">
            Deep dive into Google SERP trends for <strong style={{ color: '#4F46E5' }}>{selectedWebsite?.domain}</strong>
          </p>
        </div>

        <div className="analytics-range">
          <button
            className={`btn btn--sm ${daysRange === 7 ? 'btn--primary' : 'btn--secondary'}`}
            onClick={() => setDaysRange(7)}
          >
            7 Days
          </button>
          <button
            className={`btn btn--sm ${daysRange === 30 ? 'btn--primary' : 'btn--secondary'}`}
            onClick={() => setDaysRange(30)}
          >
            30 Days
          </button>
          <button
            className={`btn btn--sm ${daysRange === 90 ? 'btn--primary' : 'btn--secondary'}`}
            onClick={() => setDaysRange(90)}
          >
            90 Days
          </button>
        </div>
      </div>

      {loading && !trends ? (
        <LoadingSpinner text="Analyzing SERP ranking data..." />
      ) : (
        <>
          {/* CHARTS ROW */}
          <div className="analytics-grid">
            <div className="analytics-card analytics-card--wide">
              <div className="analytics-card__header">
                <h3 className="analytics-card__title">Domain Average Ranking Progression</h3>
                <span className="analytics-card__desc">Position #1 is Google's top organic result</span>
              </div>
              <RankingTrendChart data={trends?.dataPoints} height={300} />
            </div>

            <div className="analytics-card">
              <div className="analytics-card__header">
                <h3 className="analytics-card__title">Position Bracket Distribution</h3>
                <span className="analytics-card__desc">Keyword distribution by page/tier</span>
              </div>
              <DistributionBarChart distribution={distribution} height={300} />
            </div>
          </div>

          {/* MOVERS ROW */}
          <div className="movers-grid">
            {/* TOP GAINERS */}
            <div className="movers-card">
              <div className="movers-card__header">
                <div className="movers-badge movers-badge--up">▲ Top Gainers</div>
                <span className="text-muted text-sm">Largest upward movements</span>
              </div>
              {topGainers.length === 0 ? (
                <p className="movers-empty">No improving keywords in this period</p>
              ) : (
                <div className="movers-list">
                  {topGainers.map((k) => (
                    <div key={k.keywordId} className="mover-item">
                      <div className="mover-item__info">
                        <strong className="mover-item__keyword">{k.keyword}</strong>
                        <span className="mover-item__positions">
                          {formatPosition(k.previousPosition)} → {formatPosition(k.currentPosition)}
                        </span>
                      </div>
                      <span className="mover-item__change mover-item__change--up">
                        ▲ {formatChange(k.change)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* TOP LOSERS */}
            <div className="movers-card">
              <div className="movers-card__header">
                <div className="movers-badge movers-badge--down">▼ Top Decliners</div>
                <span className="text-muted text-sm">Largest downward drops</span>
              </div>
              {topLosers.length === 0 ? (
                <p className="movers-empty">No declining keywords in this period</p>
              ) : (
                <div className="movers-list">
                  {topLosers.map((k) => (
                    <div key={k.keywordId} className="mover-item">
                      <div className="mover-item__info">
                        <strong className="mover-item__keyword">{k.keyword}</strong>
                        <span className="mover-item__positions">
                          {formatPosition(k.previousPosition)} → {formatPosition(k.currentPosition)}
                        </span>
                      </div>
                      <span className="mover-item__change mover-item__change--down">
                        ▼ {formatChange(k.change)}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
}
