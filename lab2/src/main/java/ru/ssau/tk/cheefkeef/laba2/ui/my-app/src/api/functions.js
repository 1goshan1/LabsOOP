import api from './api';

export const getAllFunctions = async (sortField = 'id', ascending = true) => {
  const params = { sortField, ascending };
  const response = await api.get('/functions', { params });
  return response.data;
};

export const getFunctionById = async (id) => {
  const response = await api.get(`/functions/${id}`);
  return response.data;
};

export const createFunction = async (functionData) => {
  const response = await api.post('/functions', functionData);
  return response.data;
};

export const updateFunction = async (id, functionData) => {
  const response = await api.put(`/functions/${id}`, functionData);
  return response.data;
};

export const deleteFunction = async (id) => {
  await api.delete(`/functions/${id}`);
};

export const getFunctionsByUser = async (userId, sortField = 'id', ascending = true) => {
  const params = { sortField, ascending };
  const response = await api.get(`/functions/search/by-user/${userId}`, { params });
  return response.data;
};

export const getFunctionCountForUser = async (userId) => {
  const response = await api.get(`/functions/users/${userId}/count`);
  return response.data.count;
};