import { useNavigate } from 'react-router-dom';
import './TopNav.css';

export default function TopNav({
  websites,
  selectedWebsiteId,
  onSelectWebsite,
  unreadCount,
  sidebarCollapsed,
  onToggleSidebar,
}) {
  const navigate = useNavigate();
  const selectedWebsite = websites.find((w) => w.id === selectedWebsiteId);

  return (
    <header className="topnav" style={{ paddingLeft: sidebarCollapsed ? 80 : 256 }}>
      <button className="topnav__menu-btn" onClick={onToggleSidebar}>
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
          <line x1="3" y1="12" x2="21" y2="12" /><line x1="3" y1="6" x2="21" y2="6" /><line x1="3" y1="18" x2="21" y2="18" />
        </svg>
      </button>

      <div className="topnav__left">
        <select
          className="topnav__select"
          value={selectedWebsiteId || ''}
          onChange={(e) => onSelectWebsite(e.target.value ? Number(e.target.value) : null)}
        >
          <option value="">Select a website...</option>
          {websites.map((w) => (
            <option key={w.id} value={w.id}>
              {w.name || w.domain}
            </option>
          ))}
        </select>
        {selectedWebsite && (
          <span className="topnav__domain">{selectedWebsite.domain}</span>
        )}
      </div>

      <div className="topnav__right">
        <button
          className="topnav__icon-btn"
          onClick={() => navigate('/notifications')}
          title="Notifications"
        >
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          {unreadCount > 0 && (
            <span className="topnav__badge">{unreadCount > 99 ? '99+' : unreadCount}</span>
          )}
        </button>
      </div>
    </header>
  );
}
