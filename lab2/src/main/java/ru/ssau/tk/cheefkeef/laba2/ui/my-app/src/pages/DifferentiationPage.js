import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardHeader,
  CardActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  CircularProgress,
  Alert,
  Button,
  Stack
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import {
  getFunctionsByUser,
  getFunctionById
} from '../api/functions';
import {
  getPointsByFunctionId,
  getDifferentiatedPoints
} from '../api/points';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { Link } from 'react-router-dom';

const DifferentiationPage = () => {
  const { user } = useAuth();
  const [functions, setFunctions] = useState([]);
  const [selectedFunctionId, setSelectedFunctionId] = useState('');
  const [originalPoints, setOriginalPoints] = useState([]);
  const [differentiatedData, setDifferentiatedData] = useState(null);
  const [loadingFunctions, setLoadingFunctions] = useState(true);
  const [loadingData, setLoadingData] = useState(false);
  const [error, setError] = useState(null);
  const [originalFunction, setOriginalFunction] = useState(null);
  const [hasResult, setHasResult] = useState(false);

  // Загружаем список функций пользователя при монтировании
  useEffect(() => {
    const loadFunctions = async () => {
      try {
        setLoadingFunctions(true);
        const funcs = await getFunctionsByUser(user.id);
        setFunctions(funcs);

        // Устанавливаем первую функцию как выбранную по умолчанию, если есть
        if (funcs.length > 0) {
          setSelectedFunctionId(funcs[0].id);
        }
      } catch (err) {
        setError('Ошибка при загрузке списка функций');
        console.error(err);
      } finally {
        setLoadingFunctions(false);
      }
    };

    loadFunctions();
  }, [user.id]);

  const handleFunctionChange = (event) => {
    setSelectedFunctionId(event.target.value);
    // Сбрасываем результат при изменении выбора функции
    setHasResult(false);
    setDifferentiatedData(null);
    setOriginalPoints([]);
    setOriginalFunction(null);
  };

  const handleCalculate = async () => {
    if (!selectedFunctionId) return;

    try {
      setLoadingData(true);
      setError(null);
      setHasResult(false);

      // Получаем детали исходной функции
      const func = await getFunctionById(selectedFunctionId);
      setOriginalFunction(func);

      // Получаем точки исходной функции
      const origPoints = await getPointsByFunctionId(selectedFunctionId);
      setOriginalPoints(origPoints);

      // Получаем результат дифференцирования
      const diffData = await getDifferentiatedPoints(selectedFunctionId);
      console.log('Результат дифференцирования:', diffData); // <-- ДОБАВЬ ЭТО
      setDifferentiatedData(diffData);

      setHasResult(true);
    } catch (err) {
      setError('Ошибка при вычислении дифференциала');
      setHasResult(false);
      console.error(err);
    } finally {
      setLoadingData(false);
    }
  };

  if (loadingFunctions) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (functions.length === 0) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="info">
          У вас еще нет созданных функций. Сначала создайте функцию на странице "Мои функции".
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Дифференцирование функции
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack spacing={3}>
            <FormControl fullWidth>
              <InputLabel id="function-select-label">Выберите функцию</InputLabel>
              <Select
                labelId="function-select-label"
                value={selectedFunctionId || ''}
                onChange={handleFunctionChange}
                label="Выберите функцию"
                disabled={loadingData}
              >
                {functions.map(func => (
                  <MenuItem key={func.id} value={func.id}>
                    {func.name} ({func.signature})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>

            <Button
              variant="contained"
              color="primary"
              onClick={handleCalculate}
              disabled={loadingData || !selectedFunctionId}
              size="large"
            >
              {loadingData ? 'Вычисление...' : 'Получить результат'}
            </Button>
          </Stack>
        </CardContent>
      </Card>

      {hasResult && (
        <Grid container spacing={3}>
          {/* Исходная функция */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader
                title={`Исходная функция: ${originalFunction?.name}`}
                subheader={originalFunction?.signature}
              />
              <CardContent sx={{ height: 400 }}>
                {originalPoints.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={originalPoints.map(p => ({ x: p.xvalue, y: p.yvalue }))}
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="x" name="x" />
                      <YAxis dataKey="y" name="y" />
                      <Tooltip />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="y"
                        stroke="#3f51b5"
                        name={originalFunction?.name}
                        dot={false}
                        strokeWidth={2}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                    <Typography color="textSecondary">
                      Нет данных для отображения
                    </Typography>
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Результат дифференцирования */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader title="Результат дифференцирования" />
              <CardContent sx={{ height: 400 }}>
                {differentiatedData?.points?.length > 0 ? (
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart
                      data={differentiatedData.points.map(p => ({
                        x: p.xvalue,
                        y: p.yvalue
                      }))}
                      margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="x" name="x" />
                      <YAxis dataKey="y" name="y" />
                      <Tooltip />
                      <Legend />
                      <Line
                        type="monotone"
                        dataKey="y"
                        stroke="#f50057"
                        name="f'(x)"
                        dot={false}
                        strokeWidth={2}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                ) : (
                  <Box sx={{ display: 'flex', height: '100%', alignItems: 'center', justifyContent: 'center' }}>
                    <Typography color="textSecondary">
                      Нет данных для отображения
                    </Typography>
                  </Box>
                )}
              </CardContent>

              {differentiatedData?.dfunctionId && (
                <CardActions sx={{ justifyContent: 'center', pb: 2, flexDirection: 'column' }}>
                  <Button
                    component={Link}
                    to={`/functions/${differentiatedData.functionId}/graph`}
                    variant="contained"
                    size="large"
                    color="primary"
                    sx={{ maxWidth: 400, width: '100%' }}
                  >
                    Перейти к графику новой функции
                  </Button>
                  <Typography
                    variant="body2"
                    color="textSecondary"
                    sx={{ mt: 1, textAlign: 'center' }}
                  >
                    Создана функция: f'(x) (ID: {differentiatedData.functionId})
                  </Typography>
                </CardActions>
              )}
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default DifferentiationPage;