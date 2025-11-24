import api from './api';

export const getPointsByFunctionId = async (functionId, sortField = 'xValue', ascending = true) => {
  const params = { sortField, ascending };
  const response = await api.get(`/points/function/${functionId}`, { params });
  console.log(response)
  return response.data;
};

export const getPointsByFunctionName = async (functionName, from, to, count, sortField = 'xValue', ascending = true) => {
  const params = { sortField, ascending };
  const response = await api.get(`/points/generate/${functionName}/${from}/${to}/${count}`, { params });
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
    points: points.map(p => ({
      xvalue: p.xvalue !== undefined ? p.xvalue : p.x, // Исправлено!
      yvalue: p.yvalue !== undefined ? p.yvalue : p.y  // Исправлено!
    }))
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

export const updatePointsBatch = async (functionId, points) => {
  const response = await api.put(`/points/update/batch/${functionId}`, {
    functionId,
    points
  });
  return response.data;
};

export const getDifferentiatedPoints = async (functionId) => {
  const response = await api.get(`/points/differential/${functionId}`);
  return response.data;
};

export const createCompositeFunction = async (functionId, functionType, functionName) => {
  const response = await api.post(`/points/composite/${functionId}/${functionType}/${functionName}`);
  return response.data;
};

export const interpolatePoint = async (functionId, x) => {
  const response = await api.get(`/points/linear/${functionId}/interpolate/${x}`);
  return response.data;
};