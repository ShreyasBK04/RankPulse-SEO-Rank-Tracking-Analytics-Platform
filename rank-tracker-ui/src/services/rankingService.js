import api from './api';

const rankingService = {
  checkKeyword: (keywordId) => api.post(`/rankings/check/${keywordId}`),
  checkWebsite: (websiteId) => api.post(`/rankings/check/website/${websiteId}`),
  getHistory: (keywordId, params = {}) =>
    api.get(`/keywords/${keywordId}/rankings`, { params }),
  getAllRankings: (keywordId) => api.get(`/keywords/${keywordId}/rankings/all`),
};

export default rankingService;
