import api from './api';

export const login = async (username, password) => {
  // Кодируем учетные данные для Basic Auth
  const token = btoa(`${username}:${password}`);

  // Проверяем подключение с учетными данными
  try {
    const response = await api.get('/users/search/by-login/'+username, {
      headers: {
        'Authorization': `Basic ${token}`
      }
    });
    console.log(response)
    // Сохраняем токен и информацию о пользователе
    const user = {
      username,
      token,
      role: response.data.role || 'user',
      id: response.data.id
    };

    localStorage.setItem('user', JSON.stringify(user));
    return user;
  } catch (error) {
    throw new Error('Неверные учетные данные');
  }
};

export const register = async (userData) => {
  try {
    const response = await api.post('/users', userData);
    return response.data;
  } catch (error) {
    throw new Error('Ошибка при регистрации');
  }
};

export const getCurrentUser = () => {
  return JSON.parse(localStorage.getItem('user'));
};

export const logout = () => {
  localStorage.removeItem('user');
};