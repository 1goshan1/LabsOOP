import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Tabs, Tab, Grid, TextField, Card, CardContent, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, IconButton, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Select, InputLabel, FormControl } from '@mui/material';
import { Add as AddIcon, Delete as DeleteIcon, Save as SaveIcon, Cancel as CancelIcon, Edit as EditIcon, Visibility as VisibilityIcon } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { getFunctionById, createFunction, updateFunction } from '../api/functions';
import { getPointsByFunctionId, createPointsBatch, updatePoint, deletePoint } from '../api/points';

const FunctionEditorPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  const [functionData, setFunctionData] = useState({
    name: '',
    signature: '',
    userId: user?.id || 1 // По умолчанию используем ID 1, если нет авторизованного пользователя
  });
  const [points, setPoints] = useState([]);
  const [pointCount, setPointCount] = useState(5);
  const [editingPointIndex, setEditingPointIndex] = useState(null);
  const [editPointValue, setEditPointValue] = useState({ x: '', y: '' });
  const [loading, setLoading] = useState(false);
  const [mathFunctions, setMathFunctions] = useState([
    { id: 'identity', name: 'Тождественная функция', formula: 'f(x) = x' },
    { id: 'square', name: 'Квадратичная функция', formula: 'f(x) = x²' },
    { id: 'cube', name: 'Кубическая функция', formula: 'f(x) = x³' },
    { id: 'sqrt', name: 'Квадратный корень', formula: 'f(x) = √x' },
    { id: 'linear', name: 'Линейная функция', formula: 'f(x) = ax + b' },
  ]);
  const [selectedFunction, setSelectedFunction] = useState('');
  const [intervalFrom, setIntervalFrom] = useState(0);
  const [intervalTo, setIntervalTo] = useState(10);
  const [isCreatingFromFunction, setIsCreatingFromFunction] = useState(false);

  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
  };

  // Загрузка данных функции при редактировании
  useEffect(() => {
    if (id) {
      const loadFunction = async () => {
        try {
          const func = await getFunctionById(id);
          setFunctionData({
            id: func.id,
            name: func.name,
            signature: func.signature,
            userId: func.userId
          });

          const pointsData = await getPointsByFunctionId(id);
          setPoints(pointsData.map(p => ({
            id: p.id,
            x: p.xvalue,
            y: p.yvalue,
            isInsertable: true, // Предполагаем, что все точки можно редактировать
            isRemovable: pointsData.length > 2 // Минимум 2 точки должно остаться
          })));
        } catch (error) {
          toast.error('Ошибка при загрузке данных функции');
          console.error(error);
        }
      };

      loadFunction();
    }
  }, [id]);

  const handleCreateFromTable = () => {
    if (pointCount < 2) {
      toast.error('Минимальное количество точек - 2');
      return;
    }

    setPoints(Array.from({ length: pointCount }, (_, index) => ({
      x: index * 1,
      y: 0,
      isInsertable: true,
      isRemovable: pointCount > 2
    })));
  };

  const handleSaveFunction = async () => {
    if (!functionData.name || !functionData.signature) {
      toast.error('Заполните название и сигнатуру функции');
      return;
    }

    setLoading(true);
    try {
      if (id) {
        // Обновление существующей функции
        await updateFunction(id, functionData);
        toast.success('Функция успешно обновлена');
      } else {
        // Создание новой функции
        const newFunction = await createFunction(functionData);
        toast.success('Функция успешно создана');

        // Создание точек для новой функции
        if (points.length > 0 && newFunction.id) {
          const pointsToSave = points.map(p => ({ x: p.x, y: p.y }));
          await createPointsBatch(newFunction.id, pointsToSave);
          toast.success('Точки функции успешно сохранены');
        }

        navigate('/functions');
      }
    } catch (error) {
      toast.error('Ошибка при сохранении функции');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handlePointUpdate = (index, field, value) => {
    const newPoints = [...points];
    newPoints[index] = {
      ...newPoints[index],
      [field]: parseFloat(value) || 0
    };
    setPoints(newPoints);
  };

  const handleInsertPoint = (index) => {
    const newPoints = [...points];
    const prevPoint = index > 0 ? newPoints[index - 1] : null;
    const nextPoint = index < newPoints.length - 1 ? newPoints[index + 1] : null;

    let newX = 0;
    if (prevPoint && nextPoint) {
      newX = (prevPoint.x + nextPoint.x) / 2;
    } else if (prevPoint) {
      newX = prevPoint.x + 1;
    } else if (nextPoint) {
      newX = nextPoint.x - 1;
    }

    const newPoint = {
      x: newX,
      y: 0,
      isInsertable: true,
      isRemovable: true
    };

    newPoints.splice(index + 1, 0, newPoint);
    setPoints(newPoints);
  };

  const handleRemovePoint = async (index) => {
    if (points.length <= 2) {
      toast.error('Нельзя удалить точку. Минимальное количество точек - 2');
      return;
    }

    if (id && points[index].id) {
      // Удаление точки из базы данных
      try {
        await deletePoint(points[index].id);
        toast.success('Точка успешно удалена');
      } catch (error) {
        toast.error('Ошибка при удалении точки');
        return;
      }
    }

    const newPoints = points.filter((_, i) => i !== index);
    setPoints(newPoints);
  };

  const handleCreateFromMathFunction = async () => {
      // Исправленная проверка полей
      if (!selectedFunction || pointCount < 2) {
        toast.error('Выберите функцию и укажите количество точек (минимум 2)');
        return;
      }

      // Дополнительная проверка корректности интервала
      if (intervalFrom >= intervalTo) {
        toast.error('Начало интервала должно быть меньше конца интервала');
        return;
      }

      // Генерация точек на основе выбранной математической функции
      const step = (intervalTo - intervalFrom) / (pointCount - 1);
      const newPoints = [];

      for (let i = 0; i < pointCount; i++) {
        const x = intervalFrom + i * step;
        let y = 0;

        switch (selectedFunction) {
          case 'identity':
            y = x;
            break;
          case 'square':
            y = x * x;
            break;
          case 'cube':
            y = x * x * x;
            break;
          case 'sqrt':
            y = x >= 0 ? Math.sqrt(x) : 0;
            break;
          case 'linear':
            y = 2 * x + 1; // Пример линейной функции
            break;
          default:
            y = x;
        }

        newPoints.push({
          x,
          y,
          isInsertable: true,
          isRemovable: pointCount > 2
        });
      }

      setPoints(newPoints);
      setIsCreatingFromFunction(false);
      toast.success(`Создано ${pointCount} точек для функции`);
    };

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        {id ? 'Редактирование функции' : 'Создание новой функции'}
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Название функции"
                value={functionData.name}
                onChange={(e) => setFunctionData({...functionData, name: e.target.value})}
                margin="normal"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Сигнатура функции"
                value={functionData.signature}
                onChange={(e) => setFunctionData({...functionData, signature: e.target.value})}
                margin="normal"
              />
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Tabs value={tabValue} onChange={handleTabChange} sx={{ mb: 2 }}>
            <Tab label="Создание из таблицы" />
            <Tab label="Создание из функции" />
            {id && <Tab label="Точки функции" />}
          </Tabs>

          {tabValue === 0 && (
            <Box>
              <Box sx={{ mb: 2, display: 'flex', alignItems: 'center' }}>
                <Typography sx={{ mr: 2 }}>Количество точек:</Typography>
                <TextField
                  type="number"
                  value={pointCount}
                  onChange={(e) => setPointCount(Math.max(2, parseInt(e.target.value) || 2))}
                  size="small"
                  sx={{ width: 100, mr: 2 }}
                />
                <Button
                  variant="contained"
                  onClick={handleCreateFromTable}
                  startIcon={<AddIcon />}
                >
                  Создать таблицу
                </Button>
              </Box>

              {points.length > 0 && (
                <TableContainer component={Paper} sx={{ mt: 2 }}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>№</TableCell>
                        <TableCell>X</TableCell>
                        <TableCell>Y</TableCell>
                        <TableCell>Действия</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {points.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>{index + 1}</TableCell>
                          <TableCell>
                            <TextField
                              type="number"
                              value={point.x}
                              onChange={(e) => handlePointUpdate(index, 'x', e.target.value)}
                              size="small"
                              fullWidth
                            />
                          </TableCell>
                          <TableCell>
                            <TextField
                              type="number"
                              value={point.y}
                              onChange={(e) => handlePointUpdate(index, 'y', e.target.value)}
                              size="small"
                              fullWidth
                            />
                          </TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 1 }}>
                              <IconButton
                                color="primary"
                                onClick={() => handleInsertPoint(index)}
                                title="Добавить точку после текущей"
                              >
                                <AddIcon />
                              </IconButton>
                              {point.isRemovable && (
                                <IconButton
                                  color="error"
                                  onClick={() => handleRemovePoint(index)}
                                  title="Удалить точку"
                                >
                                  <DeleteIcon />
                                </IconButton>
                              )}
                            </Box>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Box>
          )}

          {tabValue === 1 && (
            <Box>
              <Grid container spacing={2} sx={{ mb: 3 }}>
                <Grid item xs={12} md={6}>
                  <FormControl fullWidth>
                    <InputLabel>Математическая функция</InputLabel>
                    <Select
                      value={selectedFunction}
                      onChange={(e) => setSelectedFunction(e.target.value)}
                      label="Математическая функция"
                    >
                      {mathFunctions.map((func) => (
                        <MenuItem key={func.id} value={func.id}>
                          {func.name} ({func.formula})
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                </Grid>
                <Grid item xs={6} md={3}>
                  <TextField
                    fullWidth
                    label="Начало интервала"
                    type="number"
                    value={intervalFrom}
                    onChange={(e) => setIntervalFrom(parseFloat(e.target.value) || 0)}
                  />
                </Grid>
                <Grid item xs={6} md={3}>
                  <TextField
                    fullWidth
                    label="Конец интервала"
                    type="number"
                    value={intervalTo}
                    onChange={(e) => setIntervalTo(parseFloat(e.target.value) || 0)}
                  />
                </Grid>
                <Grid item xs={6} md={3}>
                  <TextField
                    fullWidth
                    label="Количество точек"
                    type="number"
                    value={pointCount}
                    onChange={(e) => setPointCount(Math.max(2, parseInt(e.target.value) || 2))}
                    inputProps={{ min: 2 }}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Button
                    variant="contained"
                    onClick={handleCreateFromMathFunction}
                    fullWidth
                    startIcon={<AddIcon />}
                  >
                    Создать точки
                  </Button>
                </Grid>
              </Grid>

              {points.length > 0 && (
                <TableContainer component={Paper} sx={{ mt: 2 }}>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>№</TableCell>
                        <TableCell>X</TableCell>
                        <TableCell>Y</TableCell>
                        <TableCell>Действия</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {points.map((point, index) => (
                        <TableRow key={index}>
                          <TableCell>{index + 1}</TableCell>
                          <TableCell>{point.x.toFixed(2)}</TableCell>
                          <TableCell>{point.y.toFixed(2)}</TableCell>
                          <TableCell>
                            <Box sx={{ display: 'flex', gap: 1 }}>
                              <IconButton
                                color="primary"
                                onClick={() => handleInsertPoint(index)}
                                title="Добавить точку после текущей"
                              >
                                <AddIcon />
                              </IconButton>
                              {point.isRemovable && (
                                <IconButton
                                  color="error"
                                  onClick={() => handleRemovePoint(index)}
                                  title="Удалить точку"
                                >
                                  <DeleteIcon />
                                </IconButton>
                              )}
                            </Box>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </Box>
          )}

          {tabValue === 2 && id && (
            <Box>
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>№</TableCell>
                      <TableCell>X</TableCell>
                      <TableCell>Y</TableCell>
                      <TableCell>Действия</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {points.map((point, index) => (
                      <TableRow key={index}>
                        <TableCell>{index + 1}</TableCell>
                        <TableCell>
                          {point.isInsertable ? (
                            <TextField
                              type="number"
                              value={point.x}
                              onChange={(e) => handlePointUpdate(index, 'x', e.target.value)}
                              size="small"
                              fullWidth
                            />
                          ) : (
                            point.x.toFixed(2)
                          )}
                        </TableCell>
                        <TableCell>
                          {point.isInsertable ? (
                            <TextField
                              type="number"
                              value={point.y}
                              onChange={(e) => handlePointUpdate(index, 'y', e.target.value)}
                              size="small"
                              fullWidth
                            />
                          ) : (
                            point.y.toFixed(2)
                          )}
                        </TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            {point.isInsertable && (
                              <IconButton
                                color="primary"
                                onClick={() => handleInsertPoint(index)}
                                title="Добавить точку после текущей"
                              >
                                <AddIcon />
                              </IconButton>
                            )}
                            {point.isRemovable && (
                              <IconButton
                                color="error"
                                onClick={() => handleRemovePoint(index)}
                                title="Удалить точку"
                              >
                                <DeleteIcon />
                              </IconButton>
                            )}
                            <IconButton
                              color="info"
                              component="a"
                              href={`/functions/${id}/graph`}
                              target="_blank"
                              title="Посмотреть график"
                            >
                              <VisibilityIcon />
                            </IconButton>
                          </Box>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}

          <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between' }}>
            <Button
              variant="outlined"
              onClick={() => navigate('/functions')}
              startIcon={<CancelIcon />}
            >
              Отмена
            </Button>
            <Button
              variant="contained"
              onClick={handleSaveFunction}
              disabled={loading}
              startIcon={<SaveIcon />}
            >
              {loading ? 'Сохранение...' : 'Сохранить функцию'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default FunctionEditorPage;