// src/pages/CompositeFunctionsPage.js
import React, { useState, useEffect } from 'react';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TextField, LinearProgress, Alert, IconButton, Tooltip } from '@mui/material';
import { Edit as EditIcon, Save as SaveIcon, Cancel as CancelIcon } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { getFunctionsByUser } from '../api/functions';
import { createCompositeFunction, getPointsByFunctionId, updatePointsBatch } from '../api/points';
import { toast } from 'react-toastify';

const CompositeFunctionsPage = () => {
  const { user } = useAuth();
  const [functions, setFunctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [compositeFunctionBase, setCompositeFunctionBase] = useState(null);
  const [compositeFunctionType, setCompositeFunctionType] = useState('identity');
  const [compositeFunctionName, setCompositeFunctionName] = useState('');
  const [compositeResult, setCompositeResult] = useState(null);
  const [compositeLoading, setCompositeLoading] = useState(false);
  const [compositeError, setCompositeError] = useState(null);
  const [compositePoints, setCompositePoints] = useState([]);
  const [compositeEditingMode, setCompositeEditingMode] = useState(false);
  const [compositeEditablePoints, setCompositeEditablePoints] = useState([]);
  const [savingCompositePoints, setSavingCompositePoints] = useState(false);

  // Загрузка функций пользователя при монтировании компонента
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

  // Инициализация редактируемых точек при загрузке результата
  useEffect(() => {
    if (compositeResult) {
      setCompositePoints(compositeResult.points);
      setCompositeEditablePoints(compositeResult.points.map(p => ({ ...p })));
    }
  }, [compositeResult]);

  // Создание сложной функции
  const handleCreateCompositeFunction = async () => {
    if (!compositeFunctionBase || !compositeFunctionName.trim()) {
      toast.warn('Пожалуйста, выберите базовую функцию и введите имя для новой функции');
      return;
    }

    setCompositeLoading(true);
    setCompositeError(null);
    setCompositeResult(null);

    try {
      const result = await createCompositeFunction(
        compositeFunctionBase,
        compositeFunctionType,
        compositeFunctionName.trim()
      );

      setCompositeResult(result);

      // Обновление списка функций для отображения новой функции
      const updatedFunctions = await getFunctionsByUser(user.id);
      setFunctions(updatedFunctions);

      toast.success(`Сложная функция "${compositeFunctionName}" успешно создана! ID: ${result.functionId}`);
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'Ошибка при создании сложной функции';
      setCompositeError(errorMessage);
      toast.error(errorMessage);
      console.error(err);
    } finally {
      setCompositeLoading(false);
    }
  };

  // Изменение значения точки в режиме редактирования
  const handleCompositePointChange = (index, field, value) => {
    setCompositeEditablePoints(prev => {
      const updatedPoints = [...prev];
      if (updatedPoints[index]) {
        updatedPoints[index] = {
          ...updatedPoints[index],
          [field]: field === 'yvalue' ? parseFloat(value) : value
        };
      }
      return updatedPoints;
    });
  };

  // Сохранение отредактированных точек
  const saveCompositePoints = async () => {
    if (!compositeResult?.functionId) return;

    setSavingCompositePoints(true);
    try {
      const pointsToUpdate = compositeEditablePoints.map(point => ({
        id: point.id,
        xvalue: point.xvalue,
        yvalue: point.yvalue
      }));

      await updatePointsBatch(compositeResult.functionId, pointsToUpdate);
      setCompositePoints([...compositeEditablePoints]);
      setCompositeEditingMode(false);
      toast.success('Точки сложной функции успешно обновлены!');
    } catch (err) {
      toast.error('Ошибка при сохранении точек: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setSavingCompositePoints(false);
    }
  };

  // Отмена редактирования
  const cancelCompositeEditing = () => {
    setCompositeEditablePoints(compositePoints.map(p => ({ ...p })));
    setCompositeEditingMode(false);
  };

  // Скачивание сериализованной функции
  const handleDownloadComposite = async () => {
    if (!compositeResult?.functionId) {
      toast.warn('Нет сложной функции для скачивания');
      return;
    }

    try {
      // В реальном приложении здесь должен быть вызов API для сериализации
      // Пока имитируем скачивание
      const mockSerializedData = JSON.stringify({
        functionId: compositeResult.functionId,
        initFunctionId: compositeResult.initFunctionId,
        functionName: compositeFunctionName,
        functionType: compositeFunctionType,
        points: compositePoints
      }, null, 2);

      const blob = new Blob([mockSerializedData], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `composite_function_${compositeResult.functionId}_${new Date().toISOString().replace(/[:.]/g, '-')}.json`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      toast.success('Сложная функция успешно сериализована и скачана!');
    } catch (err) {
      toast.error('Ошибка при скачивании сложной функции: ' + (err.response?.data?.message || err.message));
      console.error(err);
    }
  };

  // Рендеринг таблицы с точками
  const renderCompositeFunctionTable = () => {
    if (!compositeResult) return null;

    return (
      <Card sx={{ mt: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">
              Точки сложной функции (ID: {compositeResult.functionId})
            </Typography>
            <Box>
              {compositeEditingMode ? (
                <>
                  <Tooltip title="Сохранить изменения">
                    <IconButton
                      color="primary"
                      onClick={saveCompositePoints}
                      disabled={savingCompositePoints}
                    >
                      <SaveIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Отменить изменения">
                    <IconButton
                      color="error"
                      onClick={cancelCompositeEditing}
                      disabled={savingCompositePoints}
                    >
                      <CancelIcon />
                    </IconButton>
                  </Tooltip>
                </>
              ) : (
                <Tooltip title="Редактировать значения Y">
                  <IconButton
                    color="primary"
                    onClick={() => setCompositeEditingMode(true)}
                    disabled={compositePoints.length === 0}
                  >
                    <EditIcon />
                  </IconButton>
                </Tooltip>
              )}
            </Box>
          </Box>

          {savingCompositePoints && <LinearProgress sx={{ mb: 1 }} />}

          <TableContainer component={Paper} sx={{ maxHeight: 300 }}>
            <Table stickyHeader size="small">
              <TableHead>
                <TableRow>
                  <TableCell width="40%">X</TableCell>
                  <TableCell width="60%">
                    Y {compositeEditingMode && <Typography variant="caption" color="textSecondary">(редактируется)</Typography>}
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {compositePoints.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={2} align="center">
                      Нет данных
                    </TableCell>
                  </TableRow>
                ) : (
                  compositeEditablePoints.map((point, index) => (
                    <TableRow key={index}>
                      <TableCell>
                        {typeof point.xvalue !== 'undefined' ? point.xvalue.toFixed(2) : 'N/A'}
                      </TableCell>
                      <TableCell>
                        {compositeEditingMode ? (
                          <TextField
                            type="number"
                            value={point.yvalue}
                            onChange={(e) => handleCompositePointChange(index, 'yvalue', e.target.value)}
                            size="small"
                            fullWidth
                            InputProps={{
                              style: { fontSize: '0.875rem' }
                            }}
                          />
                        ) : (
                          typeof point.yvalue !== 'undefined' ? point.yvalue.toFixed(2) : 'N/A'
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
        <CardActions sx={{ justifyContent: 'flex-end', px: 2, pb: 2 }}>
          <Button
            variant="outlined"
            color="primary"
            onClick={handleDownloadComposite}
            disabled={!compositeResult?.functionId}
          >
            Скачать сериализованную
          </Button>
        </CardActions>
      </Card>
    );
  };

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography>Загрузка функций...</Typography>
        <LinearProgress sx={{ mt: 2 }} />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>
        <Button
          onClick={() => window.location.reload()}
          variant="contained"
          color="error"
        >
          Попробовать снова
        </Button>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Создание сложных функций
      </Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="flex-end">
            <Grid item xs={12} md={4}>
              <FormControl fullWidth>
                <InputLabel>Базовая функция</InputLabel>
                <Select
                  value={compositeFunctionBase || ''}
                  onChange={(e) => setCompositeFunctionBase(e.target.value)}
                  label="Базовая функция"
                >
                  {functions.map(func => (
                    <MenuItem key={func.id} value={func.id}>
                      {func.name} (ID: {func.id})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Тип функции</InputLabel>
                <Select
                  value={compositeFunctionType}
                  onChange={(e) => setCompositeFunctionType(e.target.value)}
                  label="Тип функции"
                >
                  <MenuItem value="identity">identity (f(x) = x)</MenuItem>
                  <MenuItem value="constant">constant (f(x) = 1)</MenuItem>
                  <MenuItem value="sqr">sqr (f(x) = x^2)</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                label="Имя новой функции"
                value={compositeFunctionName}
                onChange={(e) => setCompositeFunctionName(e.target.value)}
                variant="outlined"
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="contained"
                color="success"
                onClick={handleCreateCompositeFunction}
                disabled={compositeLoading}
                sx={{ height: '56px' }}
              >
                {compositeLoading ? 'Создание...' : 'Создать'}
              </Button>
            </Grid>
          </Grid>
          {compositeError && (
            <Alert severity="error" sx={{ mt: 2 }}>
              {compositeError}
            </Alert>
          )}
          {compositeLoading && !compositeError && (
            <Box sx={{ mt: 2 }}>
              <LinearProgress />
            </Box>
          )}
        </CardContent>
      </Card>

      {compositeResult && renderCompositeFunctionTable()}
    </Box>
  );
};

export default CompositeFunctionsPage;