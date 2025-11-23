import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Card, CardContent, Paper, IconButton, Dialog, DialogTitle, DialogContent, TextField, MenuItem, Select, InputLabel, FormControl } from '@mui/material';
import { Line } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler } from 'chart.js';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { getFunctionById } from '../api/functions';
import { getPointsByFunctionId } from '../api/points';
import { ArrowBack as ArrowBackIcon } from '@mui/icons-material';
import { Grid } from '@mui/material';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const GraphViewerPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [functionData, setFunctionData] = useState(null);
  const [points, setPoints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [xValue, setXValue] = useState('');
  const [yResult, setYResult] = useState('');
  const [interpolationMethod, setInterpolationMethod] = useState('linear');

  useEffect(() => {
    const loadData = async () => {
      try {
        if (id) {
          const func = await getFunctionById(id);
          setFunctionData(func);

          const pointsData = await getPointsByFunctionId(id);
          setPoints(pointsData.map(p => ({ x: p.xvalue, y: p.yvalue })));
        }
      } catch (err) {
        setError('Ошибка при загрузке данных функции');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [id]);

  const handleCalculate = () => {
    if (!xValue || isNaN(xValue)) {
      toast.error('Введите корректное значение X');
      return;
    }

    const x = parseFloat(xValue);
    let y = 0;

    if (interpolationMethod === 'linear') {
      // Простая линейная интерполяция
      const sortedPoints = [...points].sort((a, b) => a.x - b.x);
      let y1, y2, x1, x2;

      for (let i = 0; i < sortedPoints.length - 1; i++) {
        if (x >= sortedPoints[i].x && x <= sortedPoints[i + 1].x) {
          x1 = sortedPoints[i].x;
          y1 = sortedPoints[i].y;
          x2 = sortedPoints[i + 1].x;
          y2 = sortedPoints[i + 1].y;

          // Линейная интерполяция: y = y1 + (y2 - y1) * (x - x1) / (x2 - x1)
          y = y1 + (y2 - y1) * (x - x1) / (x2 - x1);
          break;
        }
      }
    } else if (interpolationMethod === 'polynomial') {
      // Полиномиальная интерполяция (Лагранж)
      if (points.length >= 2) {
        let result = 0;
        for (let i = 0; i < points.length; i++) {
          let term = points[i].y;
          for (let j = 0; j < points.length; j++) {
            if (i !== j) {
              term *= (x - points[j].x) / (points[i].x - points[j].x);
            }
          }
          result += term;
        }
        y = result;
      }
    }

    setYResult(y.toFixed(4));
    toast.success(`Значение функции в точке x = ${xValue}: y = ${y.toFixed(4)}`);
  };

  const getChartOptions = () => {
    return {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
        },
        title: {
          display: true,
          text: functionData?.name || 'График функции',
        },
        tooltip: {
          mode: 'index',
          intersect: false,
        },
      },
      scales: {
        x: {
          title: {
            display: true,
            text: 'X',
          },
        },
        y: {
          title: {
            display: true,
            text: 'Y',
          },
        },
      },
      interaction: {
        mode: 'nearest',
        axis: 'x',
        intersect: false,
      },
    };
  };

  const getChartData = () => {
    return {
      labels: points.map(p => p.x.toFixed(2)),
      datasets: [
        {
          label: functionData?.name || 'Функция',
          data: points.map(p => ({ x: p.x, y: p.y })),
          borderColor: 'rgb(75, 192, 192)',
          backgroundColor: 'rgba(75, 192, 192, 0.5)',
          pointBackgroundColor: 'rgb(75, 192, 192)',
          pointRadius: 5,
          pointHoverRadius: 7,
          fill: false,
          tension: 0.4,
        },
      ],
    };
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Typography>Загрузка данных...</Typography>
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Typography color="error">{error}</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={() => navigate(-1)}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" gutterBottom>
          {functionData?.name || 'График функции'}
        </Typography>
      </Box>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="body1" gutterBottom>
            {functionData?.signature || 'Сигнатура функции не указана'}
          </Typography>
        </CardContent>
      </Card>

      <Grid container spacing={3}>
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, height: 500 }}>
            {points.length > 0 ? (
              <Line options={getChartOptions()} data={getChartData()} />
            ) : (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                <Typography>Нет данных для отображения графика</Typography>
              </Box>
            )}
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Расчет значения функции
              </Typography>

              <Box sx={{ mb: 2 }}>
                <TextField
                  fullWidth
                  label="Значение X"
                  type="number"
                  value={xValue}
                  onChange={(e) => setXValue(e.target.value)}
                  margin="normal"
                />

                <FormControl fullWidth sx={{ mt: 2, mb: 2 }}>
                  <InputLabel>Метод интерполяции</InputLabel>
                  <Select
                    value={interpolationMethod}
                    onChange={(e) => setInterpolationMethod(e.target.value)}
                    label="Метод интерполяции"
                  >
                    <MenuItem value="linear">Линейная</MenuItem>
                    <MenuItem value="polynomial">Полиномиальная (Лагранж)</MenuItem>
                  </Select>
                </FormControl>

                <Button
                  variant="contained"
                  fullWidth
                  onClick={handleCalculate}
                  disabled={!xValue}
                >
                  Рассчитать
                </Button>
              </Box>

              {yResult && (
                <Box sx={{ mt: 2, p: 2, bgcolor: 'background.paper', borderRadius: 1 }}>
                  <Typography variant="body1">
                    f({xValue}) = {yResult}
                  </Typography>
                </Box>
              )}

              <Box sx={{ mt: 3 }}>
                <Typography variant="subtitle1" gutterBottom>
                  Информация о функции
                </Typography>
                <Typography variant="body2">
                  Количество точек: {points.length}
                </Typography>
                <Typography variant="body2">
                  Диапазон X: [{Math.min(...points.map(p => p.x)).toFixed(2)}, {Math.max(...points.map(p => p.x)).toFixed(2)}]
                </Typography>
                <Typography variant="body2">
                  Диапазон Y: [{Math.min(...points.map(p => p.y)).toFixed(2)}, {Math.max(...points.map(p => p.y)).toFixed(2)}]
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default GraphViewerPage;