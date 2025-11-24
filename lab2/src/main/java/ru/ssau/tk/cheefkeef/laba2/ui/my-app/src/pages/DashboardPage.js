import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Alert } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { getFunctionsByUser, getFunctionCountForUser } from '../api/functions';
import { Link } from 'react-router-dom';

const DashboardPage = () => {
  const { user } = useAuth();
  const [functionsCount, setFunctionsCount] = useState(0);
  const [recentFunctions, setRecentFunctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const count = await getFunctionCountForUser(user.id);
        setFunctionsCount(count);

        const functions = await getFunctionsByUser(user.id, 'id', false);
        setRecentFunctions(functions.slice(0, 5));
      } catch (err) {
        setError('Ошибка при загрузке данных');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [user]);

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Загрузка данных...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Добро пожаловать, {user.username}!
      </Typography>

      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Статистика
              </Typography>
              <Typography variant="body1">
                Количество функций: {functionsCount}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Быстрые ссылки
              </Typography>
              <CardActions sx={{ display: 'flex', flexDirection: 'column', alignItems: 'stretch' }}>
                <Button
                  component={Link}
                  to="/functions/new"
                  variant="contained"
                  color="primary"
                  sx={{ mb: 1 }}
                >
                  Создать новую функцию
                </Button>
                <Button
                  component={Link}
                  to="/functions"
                  variant="outlined"
                >
                  Мои функции
                </Button>
              </CardActions>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Последние функции
          </Typography>
          {recentFunctions.length === 0 ? (
            <Typography>У вас пока нет созданных функций</Typography>
          ) : (
            <Grid container spacing={2}>
              {recentFunctions.map(func => (
                <Grid item xs={12} key={func.id}>
                  <Card variant="outlined">
                    <CardContent>
                      <Typography variant="h6">{func.name}</Typography>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {func.signature}
                      </Typography>
                      <Button
                        component={Link}
                        to={`/functions/${func.id}/graph`}
                        variant="contained"
                        size="small"
                      >
                        Просмотреть график
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default DashboardPage;