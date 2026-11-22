import api from './api';

const alertService = {
  getByWebsite: (websiteId, params = {}) =>
    api.get(`/alerts/website/${websiteId}`, { params }),
  getUnreadCount: (websiteId) =>
    api.get(`/alerts/website/${websiteId}/unread-count`),
  markAsRead: (id) => api.put(`/alerts/${id}/read`),
  markAllAsRead: (websiteId) =>
    api.put(`/alerts/website/${websiteId}/read-all`),
};

export default alertService;
