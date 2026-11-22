import React, { useState } from 'react';
import './Settings.css';

export default function Settings({ onAddToast }) {
  const [apiKeyMasked] = useState('sec_••••••••••••••••••••••••••••••••');
  const [cronExpression] = useState('0 0 6 * * * (Daily at 06:00 AM UTC)');
  const [dbStatus] = useState('Connected (MySQL 8.x / InnoDB)');

  return (
    <div className="settings-page">
      <div className="settings-header">
        <div>
          <h1 className="settings-title">System Settings & Configuration</h1>
          <p className="settings-subtitle">Review application environment, scheduler, and database health</p>
        </div>
      </div>

      <div className="settings-grid">
        {/* API INTEGRATION */}
        <div className="settings-card">
          <h3 className="settings-card__title">SearchAPI.io Integration</h3>
          <p className="settings-card__desc">
            Used to query Google's Rank Tracking Engine for organic top 100 positions.
          </p>
          <div className="settings-field">
            <label className="settings-field__label">Environment Variable</label>
            <div className="settings-field__value-box">
              <code>SEARCH_API_KEY</code>
              <span className="badge badge--success">Configured</span>
            </div>
          </div>
          <div className="settings-field">
            <label className="settings-field__label">Active Key</label>
            <div className="settings-field__value-box">
              <span className="text-muted">{apiKeyMasked}</span>
            </div>
          </div>
          <div className="settings-field">
            <label className="settings-field__label">Engine</label>
            <div className="settings-field__value-box">
              <code>google_rank_tracking</code>
            </div>
          </div>
        </div>

        {/* SCHEDULER */}
        <div className="settings-card">
          <h3 className="settings-card__title">Automated Rank Tracking Scheduler</h3>
          <p className="settings-card__desc">
            Spring Boot @Scheduled background job that executes daily SERP checks.
          </p>
          <div className="settings-field">
            <label className="settings-field__label">Cron Expression</label>
            <div className="settings-field__value-box">
              <code>{cronExpression}</code>
            </div>
          </div>
          <div className="settings-field">
            <label className="settings-field__label">Duplicate Protection</label>
            <p className="text-muted text-sm">
              Each keyword is uniquely constrained per day (<code>uk_keyword_date</code>). The scheduler checks if a ranking exists before querying the API to conserve search credits.
            </p>
          </div>
        </div>

        {/* DATABASE */}
        <div className="settings-card">
          <h3 className="settings-card__title">Database & Persistence</h3>
          <p className="settings-card__desc">
            MySQL 8.x storage engine with normalized relations and foreign key cascades.
          </p>
          <div className="settings-field">
            <label className="settings-field__label">Status</label>
            <div className="settings-field__value-box">
              <span>{dbStatus}</span>
              <span className="badge badge--success">Healthy</span>
            </div>
          </div>
          <div className="settings-field">
            <label className="settings-field__label">Tables</label>
            <div className="tables-pill-list">
              <span className="pill">websites</span>
              <span className="pill">keywords</span>
              <span className="pill">rankings</span>
              <span className="pill">alerts</span>
            </div>
          </div>
        </div>

        {/* ARCHITECTURE SUMMARY */}
        <div className="settings-card">
          <h3 className="settings-card__title">Architecture Specs</h3>
          <p className="settings-card__desc">
            Clean 3-tier enterprise architecture built for scale.
          </p>
          <ul className="specs-list">
            <li><strong>Backend:</strong> Java 21 LTS + Spring Boot 3.4.3</li>
            <li><strong>ORM:</strong> Spring Data JPA + Hibernate with DTO projections</li>
            <li><strong>Database:</strong> MySQL 8.x (InnoDB Engine)</li>
            <li><strong>Frontend:</strong> React 19 + Recharts + Vite</li>
            <li><strong>Routing:</strong> React Router v7</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
