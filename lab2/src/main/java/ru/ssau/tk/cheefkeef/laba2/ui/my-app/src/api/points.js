import api from './api';

export const getPointsByFunctionId = async (functionId, sortField = 'xValue', ascending = true) => {
  const params = { sortField, ascending };
  const response = await api.get(`/points/function/${functionId}`, { params });
  console.log(response)
  return response.data;
};

export const createPoint = async (pointData) => {
  const response = await api.post('/points', pointData);
  return response.data;
};

export const createPointsBatch = async (functionId, points) => {
  const data = {
    functionId,
    points: points.map(p => ({ xvalue: p.x, yvalue: p.y }))
  };
  const response = await api.post('/points/batch', data);
  return response.data;
};

export const updatePoint = async (id, pointData) => {
  const response = await api.put(`/points/${id}`, pointData);
  return response.data;
};

export const deletePoint = async (id) => {
  await api.delete(`/points/${id}`);
};

export const deletePointsByFunctionId = async (functionId) => {
  await api.delete(`/points/function/${functionId}`);
};