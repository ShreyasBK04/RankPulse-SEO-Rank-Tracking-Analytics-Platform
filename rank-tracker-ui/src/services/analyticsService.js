import api from './api';

const analyticsService = {
  getDashboardStats: (websiteId) =>
    api.get(`/analytics/dashboard/${websiteId}`),
  getPerformance: (websiteId) =>
    api.get(`/analytics/performance/${websiteId}`),
  getTrends: (websiteId, params = {}) =>
    api.get(`/analytics/trends/${websiteId}`, { params }),
  getDistribution: (websiteId) =>
    api.get(`/analytics/distribution/${websiteId}`),
};

export default analyticsService;
