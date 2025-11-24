import api from './api';

export const login = async (username, password) => {
  try {
    const token = btoa(`${username}:${password}`);
    const response = await api.get(`/users/search/by-login/${username}`, {
      headers: {
        'Authorization': `Basic ${token}`
      }
    });

    const user = {
      username,
      token,
      role: response.data.role || 'user',
      id: response.data.id
    };

    localStorage.setItem('user', JSON.stringify(user));
    return user;
  } catch (error) {
    throw new Error(error.message || 'Неверные учетные данные');
  }
};

export const register = async (userData) => {
  try {
    // Временное решение: используем дефолтные учетные данные администратора
    // ЗАМЕНИТЕ 'admin:admin' на реальные учетные данные администратора
    const adminCredentials = btoa('admin:admin123');

    const response = await api.post('/users', userData, {
      headers: {
        'Authorization': `Basic ${adminCredentials}`
      }
    });
    return response.data;
  } catch (error) {
    if (error.response && error.response.status === 401) {
      throw new Error('Ошибка авторизации. Регистрация требует прав администратора.');
    }
    throw new Error(error.message || 'Ошибка при регистрации');
  }
};

export const getCurrentUser = () => {
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;

  try {
    return JSON.parse(userStr);
  } catch (error) {
    localStorage.removeItem('user');
    return null;
  }
};

export const logout = () => {
  localStorage.removeItem('user');
};