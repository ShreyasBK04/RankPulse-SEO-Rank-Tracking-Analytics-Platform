import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar';
import TopNav from './TopNav';
import Toast from '../common/Toast';
import './Layout.css';

export default function Layout({
  websites,
  selectedWebsiteId,
  onSelectWebsite,
  unreadCount,
  toasts,
  onRemoveToast,
}) {
  const [collapsed, setCollapsed] = useState(false);

  return (
    <div className="layout">
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed(!collapsed)} />
      <TopNav
        websites={websites}
        selectedWebsiteId={selectedWebsiteId}
        onSelectWebsite={onSelectWebsite}
        unreadCount={unreadCount}
        sidebarCollapsed={collapsed}
        onToggleSidebar={() => setCollapsed(!collapsed)}
      />
      <main className="layout__main" style={{ marginLeft: collapsed ? 64 : 240 }}>
        <Outlet />
      </main>
      <Toast toasts={toasts} onRemove={onRemoveToast} />
    </div>
  );
}

import { useState } from 'react';
