import api from './api';

const keywordService = {
  getByWebsite: (websiteId, params = {}) =>
    api.get(`/websites/${websiteId}/keywords`, { params }),
  getById: (id) => api.get(`/keywords/${id}`),
  create: (websiteId, data) =>
    api.post(`/websites/${websiteId}/keywords`, data),
  delete: (id) => api.delete(`/keywords/${id}`),
};

export default keywordService;
