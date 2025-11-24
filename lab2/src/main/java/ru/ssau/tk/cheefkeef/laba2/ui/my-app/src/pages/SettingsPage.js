import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Card, CardContent, CardActions, Switch, FormControlLabel, Grid, TextField, Select, MenuItem, InputLabel, FormControl } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { toast } from 'react-toastify';

const SettingsPage = () => {
  const { user, signOut } = useAuth();
  const { mode, toggleTheme } = useTheme();
  const [factoryType, setFactoryType] = useState('array');
  const [autoSave, setAutoSave] = useState(true);
  const [notifications, setNotifications] = useState(true);
  const [language, setLanguage] = useState('ru');

  useEffect(() => {
    // Загружаем настройки из localStorage или других источников
    const savedSettings = JSON.parse(localStorage.getItem('userSettings')) || {};
    setFactoryType(savedSettings.factoryType || 'array');
    setAutoSave(savedSettings.autoSave !== undefined ? savedSettings.autoSave : true);
    setNotifications(savedSettings.notifications !== undefined ? savedSettings.notifications : true);
    setLanguage(savedSettings.language || 'ru');
  }, []);

  const handleSaveSettings = () => {
    const settings = {
      factoryType,
      autoSave,
      notifications,
      language
    };

    localStorage.setItem('userSettings', JSON.stringify(settings));
    toast.success('Настройки успешно сохранены');
  };

  const handleLogout = () => {
    signOut();
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Настройки
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Общие настройки
              </Typography>

              <FormControlLabel
                control={
                  <Switch
                    checked={mode === 'dark'}
                    onChange={toggleTheme}
                    color="primary"
                  />
                }
                label={mode === 'dark' ? 'Темная тема включена' : 'Светлая тема включена'}
                sx={{ mb: 2 }}
              />

              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Язык интерфейса</InputLabel>
                <Select
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                  label="Язык интерфейса"
                >
                  <MenuItem value="ru">Русский</MenuItem>
                  <MenuItem value="en">English</MenuItem>
                </Select>
              </FormControl>

              <FormControlLabel
                control={
                  <Switch
                    checked={notifications}
                    onChange={(e) => setNotifications(e.target.checked)}
                    color="primary"
                  />
                }
                label="Показывать уведомления"
                sx={{ mb: 2 }}
              />

              <FormControlLabel
                control={
                  <Switch
                    checked={autoSave}
                    onChange={(e) => setAutoSave(e.target.checked)}
                    color="primary"
                  />
                }
                label="Автосохранение данных"
              />
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Профиль пользователя
              </Typography>

              <TextField
                fullWidth
                label="Логин"
                value={user?.username || ''}
                disabled
                margin="normal"
              />

              <TextField
                fullWidth
                label="Роль"
                value={user?.role || 'user'}
                disabled
                margin="normal"
              />

              <FormControl fullWidth sx={{ mt: 2 }}>
                <InputLabel>Тип фабрики для функций</InputLabel>
                <Select
                  value={factoryType}
                  onChange={(e) => setFactoryType(e.target.value)}
                  label="Тип фабрики для функций"
                >
                  <MenuItem value="array">Массив</MenuItem>
                  <MenuItem value="linkedlist">Связный список</MenuItem>
                </Select>
              </FormControl>
            </CardContent>
            <CardActions sx={{ justifyContent: 'space-between', p: 2 }}>
              <Button
                variant="outlined"
                color="error"
                onClick={handleLogout}
              >
                Выйти из системы
              </Button>
              <Button
                variant="contained"
                color="primary"
                onClick={handleSaveSettings}
              >
                Сохранить настройки
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SettingsPage;