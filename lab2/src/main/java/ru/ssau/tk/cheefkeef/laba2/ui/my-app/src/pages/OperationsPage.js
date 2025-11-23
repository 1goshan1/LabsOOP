import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TextField } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { getFunctionsByUser } from '../api/functions';
import { getPointsByFunctionId } from '../api/points';
import { toast } from 'react-toastify';

const OperationsPage = () => {
  const { user } = useAuth();
  const [functions, setFunctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedFunctions, setSelectedFunctions] = useState({ first: null, second: null });
  const [operation, setOperation] = useState('add');
  const [function1Points, setFunction1Points] = useState([]);
  const [function2Points, setFunction2Points] = useState([]);
  const [resultPoints, setResultPoints] = useState([]);

  useEffect(() => {
    const loadFunctions = async () => {
      try {
        const data = await getFunctionsByUser(user.id);
        setFunctions(data);
      } catch (err) {
        setError('Ошибка при загрузке функций');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadFunctions();
  }, [user]);

  useEffect(() => {
    const loadPoints = async () => {
      if (selectedFunctions.first && selectedFunctions.second) {
        try {
          const points1 = await getPointsByFunctionId(selectedFunctions.first);
          const points2 = await getPointsByFunctionId(selectedFunctions.second);
          console.log(points1, points2);
          setFunction1Points(points1);
          setFunction2Points(points2);

          // Вычисление результата операции
          calculateResult(points1, points2, operation);
        } catch (err) {
          toast.error('Ошибка при загрузке точек функций');
          console.error(err);
        }
      } else {
        setResultPoints([]);
      }
    };

    loadPoints();
  }, [selectedFunctions, operation]);

  const calculateResult = (points1, points2, operation) => {
    // Добавляем защиту от некорректных данных
    if (!points1 || !points2 || points1.length === 0 || points2.length === 0) {
      setResultPoints([]);
      return;
    }

    // Для простоты предполагаем, что обе функции имеют одинаковые точки x
    const result = [];

    // Убедимся, что мы обрабатываем одинаковое количество точек
    const minLength = Math.min(points1.length, points2.length);

    for (let i = 0; i < minLength; i++) {
      // Проверяем, что точки существуют и имеют необходимые поля
      if (!points1[i] || !points2[i]) {
        continue;
      }

      const x1 = points1[i]?.xvalue;
      const x2 = points2[i]?.xvalue;

      // Проверяем, что значения x существуют
      if (typeof x1 === 'undefined' || typeof x2 === 'undefined') {
        continue;
      }

      // Проверяем, что точки имеют одинаковые x
      if (Math.abs(x1 - x2) > 0.001) {
        toast.warn('Функции имеют разные значения x в точке ' + i + '. Результат может быть некорректным.');
        continue;
      }

      const y1 = points1[i]?.yvalue;
      const y2 = points2[i]?.yvalue;

      // Проверяем, что значения y существуют
      if (typeof y1 === 'undefined' || typeof y2 === 'undefined') {
        continue;
      }

      let yResult;

      switch (operation) {
        case 'add':
          yResult = y1 + y2;
          break;
        case 'subtract':
          yResult = y1 - y2;
          break;
        case 'multiply':
          yResult = y1 * y2;
          break;
        case 'divide':
          if (Math.abs(y2) < 0.001) {
            toast.error('Деление на ноль в точке x=' + x1);
            yResult = 0;
          } else {
            yResult = y1 / y2;
          }
          break;
        default:
          yResult = 0;
      }

      // Добавляем только корректные точки в результат
      if (typeof x1 !== 'undefined' && typeof yResult !== 'undefined') {
        result.push({ x: x1, y: yResult });
      }
    }

    setResultPoints(result);
  };

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Загрузка функций...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography color="error">{error}</Typography>
        <Button onClick={() => window.location.reload()} sx={{ mt: 2 }}>
          Попробовать снова
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Элементарные операции над функциями
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Первая функция</InputLabel>
                <Select
                  value={selectedFunctions.first || ''}
                  onChange={(e) => setSelectedFunctions(prev => ({ ...prev, first: e.target.value }))}
                  label="Первая функция"
                >
                  {functions.map(func => (
                    <MenuItem key={func.id} value={func.id}>
                      {func.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Вторая функция</InputLabel>
                <Select
                  value={selectedFunctions.second || ''}
                  onChange={(e) => setSelectedFunctions(prev => ({ ...prev, second: e.target.value }))}
                  label="Вторая функция"
                >
                  {functions.map(func => (
                    <MenuItem key={func.id} value={func.id}>
                      {func.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Операция</InputLabel>
                <Select
                  value={operation}
                  onChange={(e) => setOperation(e.target.value)}
                  label="Операция"
                >
                  <MenuItem value="add">Сложение (+)</MenuItem>
                  <MenuItem value="subtract">Вычитание (-)</MenuItem>
                  <MenuItem value="multiply">Умножение (*)</MenuItem>
                  <MenuItem value="divide">Деление (/)</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Первая функция
              </Typography>
              {selectedFunctions.first ? (
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader>
                    <TableHead>
                      <TableRow>
                        <TableCell>X</TableCell>
                        <TableCell>Y</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {function1Points.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {typeof point.xvalue !== 'undefined' ? point.xvalue.toFixed(2) : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {typeof point.yvalue !== 'undefined' ? point.yvalue.toFixed(2) : 'N/A'}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography>Выберите первую функцию</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Вторая функция
              </Typography>
              {selectedFunctions.second ? (
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader>
                    <TableHead>
                      <TableRow>
                        <TableCell>X</TableCell>
                        <TableCell>Y</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {function2Points.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {typeof point.xvalue !== 'undefined' ? point.xvalue.toFixed(2) : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {typeof point.yvalue !== 'undefined' ? point.yvalue.toFixed(2) : 'N/A'}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography>Выберите вторую функцию</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Результат операции
              </Typography>
              {resultPoints.length > 0 ? (
                <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
                  <Table stickyHeader>
                    <TableHead>
                      <TableRow>
                        <TableCell>X</TableCell>
                        <TableCell>Y</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {resultPoints.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>
                            {typeof point.x !== 'undefined' ? point.x.toFixed(2) : 'N/A'}
                          </TableCell>
                          <TableCell>
                            {typeof point.y !== 'undefined' ? point.y.toFixed(2) : 'N/A'}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              ) : (
                <Typography>Выберите обе функции для выполнения операции</Typography>
              )}
            </CardContent>
            <CardActions>
              <Button
                variant="contained"
                color="primary"
                fullWidth
                disabled={resultPoints.length === 0}
                onClick={() => toast.info('Добавьте кнопку сохранения результата в следующей версии')}
              >
                Сохранить результат
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default OperationsPage;