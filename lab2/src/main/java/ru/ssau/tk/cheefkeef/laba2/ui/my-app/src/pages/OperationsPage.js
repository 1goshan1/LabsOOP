import React, { useState, useEffect, useRef } from 'react';
import { Box, Typography, Grid, Card, CardContent, CardActions, Button, Select, MenuItem, FormControl, InputLabel, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TextField, LinearProgress, Alert, IconButton, Tooltip } from '@mui/material';
import { Edit as EditIcon, Save as SaveIcon, Cancel as CancelIcon } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { getFunctionsByUser, performOperation, deserializeFunction, serializeFunction } from '../api/functions';
import { getPointsByFunctionId, updatePointsBatch } from '../api/points';
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
  const [resultFunctionId, setResultFunctionId] = useState(null);
  const [operationLoading, setOperationLoading] = useState(false);
  const [deserializationLoading, setDeserializationLoading] = useState(false);
  const [deserializedPoints, setDeserializedPoints] = useState([]);
  const [deserializedFunctionId, setDeserializedFunctionId] = useState(null);
  const [hasPerformedOperation, setHasPerformedOperation] = useState(false);

  // States for editable points
  const [editablePoints, setEditablePoints] = useState({
    first: [],
    second: [],
    deserialized: []
  });
  const [editingMode, setEditingMode] = useState({
    first: false,
    second: false,
    deserialized: false
  });
  const [savingPoints, setSavingPoints] = useState({
    first: false,
    second: false,
    deserialized: false
  });

  const fileInputRef = useRef(null);

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

  // Load points for selected functions and initialize editable points
  useEffect(() => {
    const loadPoints = async () => {
      if (selectedFunctions.first) {
        try {
          const points1 = await getPointsByFunctionId(selectedFunctions.first);
          setFunction1Points(points1);
          setEditablePoints(prev => ({ ...prev, first: points1.map(p => ({ ...p })) }));
        } catch (err) {
          toast.error('Ошибка при загрузке точек первой функции');
          console.error(err);
        }
      } else {
        setFunction1Points([]);
        setEditablePoints(prev => ({ ...prev, first: [] }));
      }

      if (selectedFunctions.second) {
        try {
          const points2 = await getPointsByFunctionId(selectedFunctions.second);
          setFunction2Points(points2);
          setEditablePoints(prev => ({ ...prev, second: points2.map(p => ({ ...p })) }));
        } catch (err) {
          toast.error('Ошибка при загрузке точек второй функции');
          console.error(err);
        }
      } else {
        setFunction2Points([]);
        setEditablePoints(prev => ({ ...prev, second: [] }));
      }
    };

    loadPoints();
  }, [selectedFunctions.first, selectedFunctions.second]);

  // Initialize editable deserialized points when they are loaded
  useEffect(() => {
    if (deserializedPoints.length > 0) {
      setEditablePoints(prev => ({
        ...prev,
        deserialized: deserializedPoints.map(p => ({ ...p }))
      }));
    } else {
      setEditablePoints(prev => ({ ...prev, deserialized: [] }));
    }
  }, [deserializedPoints]);

  // Handle point value change in editable mode
  const handlePointChange = (functionType, index, field, value) => {
    setEditablePoints(prev => {
      const updatedPoints = [...prev[functionType]];
      if (updatedPoints[index]) {
        updatedPoints[index] = {
          ...updatedPoints[index],
          [field]: field === 'yvalue' ? parseFloat(value) : value
        };
      }
      return { ...prev, [functionType]: updatedPoints };
    });
  };

  // Toggle editing mode for a function
  const toggleEditingMode = (functionType) => {
    setEditingMode(prev => ({
      ...prev,
      [functionType]: !prev[functionType]
    }));
  };

  // Save edited points to server
  const saveEditedPoints = async (functionType, functionId) => {
    if (!functionId || editingMode[functionType] === false) return;

    setSavingPoints(prev => ({ ...prev, [functionType]: true }));

    try {
      // Prepare points data for batch update
      const pointsToUpdate = editablePoints[functionType].map(point => ({
        id: point.id,
        xvalue: point.xvalue,
        yvalue: point.yvalue
      }));

      await updatePointsBatch(functionId, pointsToUpdate);

      // Update original points state
      if (functionType === 'first') {
        setFunction1Points([...editablePoints[functionType]]);
      } else if (functionType === 'second') {
        setFunction2Points([...editablePoints[functionType]]);
      } else if (functionType === 'deserialized') {
        setDeserializedPoints([...editablePoints[functionType]]);
      }

      // Exit editing mode
      setEditingMode(prev => ({ ...prev, [functionType]: false }));

      toast.success(`Точки ${functionType === 'first' ? 'первой' : functionType === 'second' ? 'второй' : 'десериализованной'} функции успешно обновлены!`);
    } catch (err) {
      toast.error(`Ошибка при сохранении точек: ${err.response?.data?.message || err.message}`);
      console.error(err);
    } finally {
      setSavingPoints(prev => ({ ...prev, [functionType]: false }));
    }
  };

  // Cancel editing mode and revert changes
  const cancelEditing = (functionType) => {
    if (functionType === 'first' && function1Points.length > 0) {
      setEditablePoints(prev => ({ ...prev, first: function1Points.map(p => ({ ...p })) }));
    } else if (functionType === 'second' && function2Points.length > 0) {
      setEditablePoints(prev => ({ ...prev, second: function2Points.map(p => ({ ...p })) }));
    } else if (functionType === 'deserialized' && deserializedPoints.length > 0) {
      setEditablePoints(prev => ({ ...prev, deserialized: deserializedPoints.map(p => ({ ...p })) }));
    }

    setEditingMode(prev => ({ ...prev, [functionType]: false }));
  };

  const executeOperation = async () => {
    if (!selectedFunctions.first || !selectedFunctions.second) {
      toast.warn('Пожалуйста, выберите обе функции');
      return;
    }

    // Save any pending edits before performing operation
    if (editingMode.first) {
      await saveEditedPoints('first', selectedFunctions.first);
    }
    if (editingMode.second) {
      await saveEditedPoints('second', selectedFunctions.second);
    }

    setOperationLoading(true);
    setHasPerformedOperation(true);
    setResultPoints([]);
    setResultFunctionId(null);

    try {
      // Map client operation to server operation names
      const operationMap = {
        'add': 'plus',
        'subtract': 'minus',
        'multiply': 'multiply',
        'divide': 'divide'
      };

      const serverOperation = operationMap[operation] || 'plus';

      const result = await performOperation(
        selectedFunctions.first,
        selectedFunctions.second,
        serverOperation
      );

      setResultPoints(result);

      // Extract function ID from the first point if available
      if (result.length > 0 && result[0].functionId) {
        setResultFunctionId(result[0].functionId);
      }

      toast.success('Операция успешно выполнена!');
    } catch (err) {
      toast.error('Ошибка при выполнении операции: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setOperationLoading(false);
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    setDeserializationLoading(true);
    setDeserializedPoints([]);
    setDeserializedFunctionId(null);

    try {
      // Read file content
      const fileContent = await readFileContent(file);

      // Parse JSON and extract serialized function string
      let serializedFunction;
      try {
        const jsonData = JSON.parse(fileContent);
        serializedFunction = jsonData.serializedFunction;

        if (!serializedFunction) {
          throw new Error('Invalid file format: missing serializedFunction field');
        }
      } catch (parseError) {
        // If not valid JSON, treat entire content as serialized string
        serializedFunction = fileContent.trim();
      }

      // Deserialize function on server
      const deserializeResult = await deserializeFunction(serializedFunction);

      if (!deserializeResult.functionId) {
        throw new Error('Invalid response from server: missing functionId');
      }

      setDeserializedFunctionId(deserializeResult.functionId);

      // Get points for the deserialized function
      const points = await getPointsByFunctionId(deserializeResult.functionId);
      setDeserializedPoints(points);

      // Refresh function list to include new function
      const updatedFunctions = await getFunctionsByUser(user.id);
      setFunctions(updatedFunctions);

      toast.success('Функция успешно десериализована!');
    } catch (err) {
      toast.error('Ошибка при десериализации: ' + (err.response?.data?.message || err.message));
      console.error(err);
    } finally {
      setDeserializationLoading(false);
      // Reset file input
      event.target.value = null;
    }
  };

  const readFileContent = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onload = (event) => {
        resolve(event.target.result);
      };

      reader.onerror = (error) => {
        reject(error);
      };

      reader.readAsText(file);
    });
  };

  const handleDownloadSerialized = async () => {
    if (!resultFunctionId) {
      toast.warn('Нет результата для сериализации');
      return;
    }

    try {
      const serializedData = await serializeFunction(resultFunctionId);

      // Create blob and download link
      const blob = new Blob([serializedData], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `function_${resultFunctionId}_${new Date().toISOString().replace(/[:.]/g, '-')}.json`;
      document.body.appendChild(a);
      a.click();

      // Cleanup
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      toast.success('Функция успешно сериализована и скачана!');
    } catch (err) {
      toast.error('Ошибка при сериализации: ' + (err.response?.data?.message || err.message));
      console.error(err);
    }
  };

  const handleDownloadDeserialized = async () => {
    if (!deserializedFunctionId) {
      toast.warn('Нет десериализованной функции для скачивания');
      return;
    }

    try {
      const serializedData = await serializeFunction(deserializedFunctionId);

      // Create blob and download link
      const blob = new Blob([serializedData], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `deserialized_function_${deserializedFunctionId}_${new Date().toISOString().replace(/[:.]/g, '-')}.json`;
      document.body.appendChild(a);
      a.click();

      // Cleanup
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      toast.success('Десериализованная функция успешно скачана!');
    } catch (err) {
      toast.error('Ошибка при скачивании десериализованной функции: ' + (err.response?.data?.message || err.message));
      console.error(err);
    }
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

  // Helper function to render function table with editable capabilities
  const renderFunctionTable = (functionType, points, editablePointsArray, functionId) => {
    const isEditing = editingMode[functionType];
    const isSaving = savingPoints[functionType];

    return (
      <>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
          <Typography variant="subtitle1">
            {isEditing ? 'Режим редактирования' : 'Просмотр точек'}
          </Typography>
          <Box>
            {isEditing ? (
              <>
                <Tooltip title="Сохранить изменения">
                  <IconButton
                    color="primary"
                    onClick={() => saveEditedPoints(functionType, functionId)}
                    disabled={isSaving}
                  >
                    <SaveIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Отменить изменения">
                  <IconButton
                    color="error"
                    onClick={() => cancelEditing(functionType)}
                    disabled={isSaving}
                  >
                    <CancelIcon />
                  </IconButton>
                </Tooltip>
              </>
            ) : (
              <Tooltip title="Редактировать значения Y">
                <IconButton
                  color="primary"
                  onClick={() => toggleEditingMode(functionType)}
                  disabled={points.length === 0}
                >
                  <EditIcon />
                </IconButton>
              </Tooltip>
            )}
          </Box>
        </Box>

        {isSaving && (
          <LinearProgress sx={{ mb: 1 }} />
        )}

        <TableContainer component={Paper} sx={{ maxHeight: 250 }}>
          <Table stickyHeader size="small">
            <TableHead>
              <TableRow>
                <TableCell width="40%">X</TableCell>
                <TableCell width="60%">
                  Y {isEditing && <Typography variant="caption" color="textSecondary">(редактируется)</Typography>}
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {points.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={2} align="center">
                    Нет данных
                  </TableCell>
                </TableRow>
              ) : (
                editablePointsArray.map((point, index) => (
                  <TableRow key={index}>
                    <TableCell>
                      {typeof point.xvalue !== 'undefined' ? point.xvalue.toFixed(2) : 'N/A'}
                    </TableCell>
                    <TableCell>
                      {isEditing ? (
                        <TextField
                          type="number"
                          value={point.yvalue}
                          onChange={(e) => handlePointChange(functionType, index, 'yvalue', e.target.value)}
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
      </>
    );
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Элементарные операции над функциями
      </Typography>

      {/* Deserialization Section */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Загрузить сериализованную функцию
          </Typography>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={8}>
              <TextField
                fullWidth
                label="Выберите JSON файл с сериализованной функцией"
                variant="outlined"
                InputProps={{
                  readOnly: true,
                  endAdornment: (
                    <Button
                      variant="contained"
                      component="label"
                      disabled={deserializationLoading}
                    >
                      Выбрать файл
                      <input
                        type="file"
                        hidden
                        accept=".json"
                        onChange={handleFileUpload}
                        ref={fileInputRef}
                      />
                    </Button>
                  )
                }}
              />
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="outlined"
                color="primary"
                onClick={handleDownloadDeserialized}
                disabled={!deserializedFunctionId || deserializationLoading}
              >
                Скачать сериализованную
              </Button>
            </Grid>
          </Grid>

          {deserializationLoading && (
            <Box sx={{ mt: 2 }}>
              <LinearProgress />
            </Box>
          )}

          {deserializedPoints.length > 0 && (
            <Box sx={{ mt: 3 }}>
              <Typography variant="h6" gutterBottom>
                Точки десериализованной функции (ID: {deserializedFunctionId})
              </Typography>
              {renderFunctionTable('deserialized', deserializedPoints, editablePoints.deserialized, deserializedFunctionId)}
            </Box>
          )}
        </CardContent>
      </Card>

      {/* Operation Selection */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2} alignItems="flex-end">
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Первая функция</InputLabel>
                <Select
                  value={selectedFunctions.first || ''}
                  onChange={(e) => {
                    setSelectedFunctions(prev => ({ ...prev, first: e.target.value }));
                    setHasPerformedOperation(false);
                    setEditingMode(prev => ({ ...prev, first: false }));
                  }}
                  label="Первая функция"
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
                <InputLabel>Вторая функция</InputLabel>
                <Select
                  value={selectedFunctions.second || ''}
                  onChange={(e) => {
                    setSelectedFunctions(prev => ({ ...prev, second: e.target.value }));
                    setHasPerformedOperation(false);
                    setEditingMode(prev => ({ ...prev, second: false }));
                  }}
                  label="Вторая функция"
                >
                  {functions.map(func => (
                    <MenuItem key={func.id} value={func.id}>
                      {func.name} (ID: {func.id})
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <FormControl fullWidth>
                <InputLabel>Операция</InputLabel>
                <Select
                  value={operation}
                  onChange={(e) => {
                    setOperation(e.target.value);
                    setHasPerformedOperation(false);
                  }}
                  label="Операция"
                >
                  <MenuItem value="add">Сложение (+)</MenuItem>
                  <MenuItem value="subtract">Вычитание (-)</MenuItem>
                  <MenuItem value="multiply">Умножение (*)</MenuItem>
                  <MenuItem value="divide">Деление (/)</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={4}>
              <Button
                fullWidth
                variant="contained"
                color="primary"
                size="large"
                onClick={executeOperation}
                disabled={!selectedFunctions.first || !selectedFunctions.second || operationLoading ||
                         savingPoints.first || savingPoints.second || savingPoints.deserialized}
                sx={{ height: '56px' }}
              >
                {operationLoading ? 'Выполнение...' : 'Получить результат'}
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Results Display */}
      <Grid container spacing={3}>
        {/* First Function */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Первая функция {selectedFunctions.first ? `(ID: ${selectedFunctions.first})` : ''}
              </Typography>
              {selectedFunctions.first ? (
                renderFunctionTable('first', function1Points, editablePoints.first, selectedFunctions.first)
              ) : (
                <Typography>Выберите первую функцию</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Second Function */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Вторая функция {selectedFunctions.second ? `(ID: ${selectedFunctions.second})` : ''}
              </Typography>
              {selectedFunctions.second ? (
                renderFunctionTable('second', function2Points, editablePoints.second, selectedFunctions.second)
              ) : (
                <Typography>Выберите вторую функцию</Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Operation Result */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Результат операции {resultFunctionId ? `(ID: ${resultFunctionId})` : ''}
              </Typography>

              {operationLoading && hasPerformedOperation ? (
                <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
                  <LinearProgress sx={{ width: '100%' }} />
                </Box>
              ) : hasPerformedOperation ? (
                resultPoints.length > 0 ? (
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
                  <Alert severity="info" sx={{ mt: 1 }}>
                    Нет результатов для отображения
                  </Alert>
                )
              ) : (
                <Alert severity="info" sx={{ mt: 1 }}>
                  Выберите функции и операцию, затем нажмите "Получить результат"
                </Alert>
              )}
            </CardContent>
            <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
              <Button
                variant="contained"
                color="primary"
                disabled={resultPoints.length === 0 || operationLoading || !hasPerformedOperation}
                onClick={() => toast.info('Функция будет сохранена в вашем профиле автоматически')}
              >
                Сохранить результат
              </Button>
              <Button
                variant="outlined"
                color="primary"
                disabled={!resultFunctionId || operationLoading || !hasPerformedOperation}
                onClick={handleDownloadSerialized}
              >
                Скачать сериализованную
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default OperationsPage;