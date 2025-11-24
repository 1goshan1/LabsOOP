import axios from 'axios';

const API_URL = 'http://localhost:8080/lab2-1.0-SNAPSHOT/api/v1';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // Добавляем таймаут
});

// Только для авторизованных запросов
api.interceptors.request.use(config => {
  const user = JSON.parse(localStorage.getItem('user'));
  if (user && user.token) {
    config.headers.Authorization = `Basic ${user.token}`;
  }
  return config;
});

api.interceptors.response.use(
  response => response,
  error => {
    if (error.response) {
      // Сервер ответил с ошибкой
      const message = error.response.data?.message || error.response.data || 'Ошибка сервера';
      return Promise.reject(new Error(message));
    } else if (error.request) {
      // Запрос был сделан, но ответ не получен
      return Promise.reject(new Error('Нет ответа от сервера. Проверьте подключение.'));
    } else {
      // Ошибка настройки запроса
      return Promise.reject(new Error('Ошибка настройки запроса: ' + error.message));
    }
  }
);

export default api;