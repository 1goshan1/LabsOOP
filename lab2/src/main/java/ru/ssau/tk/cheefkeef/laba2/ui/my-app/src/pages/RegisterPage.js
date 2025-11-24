import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Link, Paper, FormHelperText } from '@mui/material';
import { toast } from 'react-toastify';
import { register } from '../api/auth';
import { useNavigate } from 'react-router-dom';

const RegisterPage = () => {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('user');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const navigate = useNavigate();

  // Регулярные выражения для валидации (соответствуют Java-паттернам)
  const LOGIN_PATTERN = /^[a-zA-Z0-9_]{3,20}$/;
  const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,32}$/;

  const validateForm = () => {
    const newErrors = {};

    // Валидация логина
    if (!login) {
      newErrors.login = 'Логин обязателен';
    } else if (!LOGIN_PATTERN.test(login)) {
      newErrors.login = 'Логин должен содержать 3-20 символов (только латинские буквы, цифры и _)';
    }

    // Валидация пароля
    if (!password) {
      newErrors.password = 'Пароль обязателен';
    } else if (!PASSWORD_PATTERN.test(password)) {
      newErrors.password = 'Пароль должен содержать 8-32 символа, включая минимум одну заглавную букву, одну строчную букву и одну цифру';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validateForm()) {
      toast.error('Пожалуйста, исправьте ошибки в форме');
      return;
    }

    setLoading(true);
    try {
      await register({
        login,
        password,
        role
      });
      toast.success('Регистрация прошла успешно! Теперь вы можете войти в систему.');
      navigate('/login');
    } catch (error) {
      toast.error('Ошибка регистрации: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleLoginChange = (value) => {
    setLogin(value);
    // Очищаем ошибку при вводе
    if (errors.login) {
      setErrors(prev => ({ ...prev, login: '' }));
    }
  };

  const handlePasswordChange = (value) => {
    setPassword(value);
    // Очищаем ошибку при вводе
    if (errors.password) {
      setErrors(prev => ({ ...prev, password: '' }));
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2 }}>
      <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Typography variant="h5" align="center" gutterBottom>
          Регистрация
        </Typography>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Логин"
            value={login}
            onChange={(e) => handleLoginChange(e.target.value)}
            margin="normal"
            required
            error={!!errors.login}
            helperText={errors.login || "Только латинские буквы, цифры и _ (3-20 символов)"}
            inputProps={{
              pattern: "^[a-zA-Z0-9_]{3,20}$",
              title: "Логин должен содержать 3-20 символов: только латинские буквы, цифры и _"
            }}
          />

          <TextField
            fullWidth
            label="Пароль"
            type="password"
            value={password}
            onChange={(e) => handlePasswordChange(e.target.value)}
            margin="normal"
            required
            error={!!errors.password}
            helperText={errors.password || "Минимум 8 символов, включая заглавную, строчную буквы и цифру"}
            inputProps={{
              pattern: "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$",
              title: "Пароль должен содержать 8-32 символа, включая минимум одну заглавную букву, одну строчную букву и одну цифру"
            }}
          />

          <Box sx={{ mt: 2, mb: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
            <Typography variant="body2" gutterBottom>
              <strong>Требования к паролю:</strong>
            </Typography>
            <Typography variant="body2" color={password.length >= 8 && password.length <= 32 ? 'success.main' : 'text.secondary'}>
              • Длина: 8-32 символа {password.length >= 8 && password.length <= 32 && '✓'}
            </Typography>
            <Typography variant="body2" color={/(?=.*[a-z])/.test(password) ? 'success.main' : 'text.secondary'}>
              • Минимум одна строчная буква (a-z) {/(?=.*[a-z])/.test(password) && '✓'}
            </Typography>
            <Typography variant="body2" color={/(?=.*[A-Z])/.test(password) ? 'success.main' : 'text.secondary'}>
              • Минимум одна заглавная буква (A-Z) {/(?=.*[A-Z])/.test(password) && '✓'}
            </Typography>
            <Typography variant="body2" color={/(?=.*\d)/.test(password) ? 'success.main' : 'text.secondary'}>
              • Минимум одна цифра (0-9) {/(?=.*\d)/.test(password) && '✓'}
            </Typography>
          </Box>

          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            disabled={loading}
            sx={{ mt: 3, mb: 2 }}
          >
            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
          </Button>
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="body2">
              Уже есть аккаунт?{' '}
              <Link href="/login" underline="hover">
                Войти
              </Link>
            </Typography>
          </Box>
        </form>
      </Paper>
    </Box>
  );
};

export default RegisterPage;