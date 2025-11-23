import React, { useState } from 'react';
import { TextField, Button, Typography, Box, Paper, Link } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';

const LoginSchema = Yup.object().shape({
  username: Yup.string()
    .min(3, 'Логин должен содержать не менее 3 символов')
    .max(20, 'Логин должен содержать не более 20 символов')
    .required('Логин обязателен'),
  password: Yup.string()
    .min(8, 'Пароль должен содержать не менее 8 символов')
    .required('Пароль обязателен'),
});

const LoginPage = () => {
  const [loading, setLoading] = useState(false);
  const { signIn } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (values, { setSubmitting }) => {
    setLoading(true);
    try {
      await signIn(values.username, values.password);
      toast.success('Успешная авторизация!');
      navigate('/');
    } catch (error) {
      toast.error('Ошибка авторизации: ' + error.message);
    } finally {
      setLoading(false);
      setSubmitting(false);
    }
  };

  return (
    <Box sx={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      p: 2
    }}>
      <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Typography variant="h5" align="center" gutterBottom>
          Вход в систему
        </Typography>
        <Formik
          initialValues={{ username: '', password: '' }}
          validationSchema={LoginSchema}
          onSubmit={handleSubmit}
        >
          {({ isSubmitting }) => (
            <Form>
              <Field
                as={TextField}
                name="username"
                label="Логин"
                fullWidth
                margin="normal"
                disabled={loading}
                error={false}
              />
              <ErrorMessage name="username" component="div" style={{ color: 'red', fontSize: '0.75rem', marginTop: '-8px', marginBottom: '8px' }} />

              <Field
                as={TextField}
                name="password"
                label="Пароль"
                type="password"
                fullWidth
                margin="normal"
                disabled={loading}
                error={false}
              />
              <ErrorMessage name="password" component="div" style={{ color: 'red', fontSize: '0.75rem', marginTop: '-8px', marginBottom: '16px' }} />

              <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                disabled={loading || isSubmitting}
                sx={{ mt: 2, mb: 2 }}
              >
                {loading ? 'Авторизация...' : 'Войти'}
              </Button>

              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="body2">
                  Нет аккаунта?{' '}
                  <Link href="/register" underline="hover">
                    Зарегистрироваться
                  </Link>
                </Typography>
              </Box>
            </Form>
          )}
        </Formik>
      </Paper>
    </Box>
  );
};

export default LoginPage;