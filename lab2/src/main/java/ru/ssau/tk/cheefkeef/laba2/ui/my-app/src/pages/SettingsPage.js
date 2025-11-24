import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  CardActions,
  Switch,
  FormControlLabel,
  Grid,
  TextField,
  Select,
  MenuItem,
  InputLabel,
  FormControl,
  Divider,
  Alert
} from '@mui/material';
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

  // Настройки инклюзивности
  const [largeText, setLargeText] = useState(false);
  const [highContrast, setHighContrast] = useState(false);
  const [colorBlindMode, setColorBlindMode] = useState(false);
  const [reducedMotion, setReducedMotion] = useState(false);
  const [screenReader, setScreenReader] = useState(false);

  useEffect(() => {
    // Загружаем настройки из localStorage
    const savedSettings = JSON.parse(localStorage.getItem('userSettings')) || {};
    setFactoryType(savedSettings.factoryType || 'array');
    setAutoSave(savedSettings.autoSave !== undefined ? savedSettings.autoSave : true);
    setNotifications(savedSettings.notifications !== undefined ? savedSettings.notifications : true);
    setLanguage(savedSettings.language || 'ru');

    // Загружаем настройки инклюзивности
    const accessibilitySettings = JSON.parse(localStorage.getItem('accessibilitySettings')) || {};
    setLargeText(accessibilitySettings.largeText || false);
    setHighContrast(accessibilitySettings.highContrast || false);
    setColorBlindMode(accessibilitySettings.colorBlindMode || false);
    setReducedMotion(accessibilitySettings.reducedMotion || false);
    setScreenReader(accessibilitySettings.screenReader || false);

    // Применяем настройки инклюзивности при загрузке
    applyAccessibilitySettings(accessibilitySettings);
  }, []);

  // Функция для применения настроек инклюзивности
  const applyAccessibilitySettings = (settings) => {
    const root = document.documentElement;

    // Большой текст
    if (settings.largeText) {
      root.style.fontSize = '18px';
      document.body.classList.add('large-text');
    } else {
      root.style.fontSize = '';
      document.body.classList.remove('large-text');
    }

    // Высокая контрастность
    if (settings.highContrast) {
      document.body.classList.add('high-contrast');
    } else {
      document.body.classList.remove('high-contrast');
    }

    // Режим для дальтоников
    if (settings.colorBlindMode) {
      document.body.classList.add('color-blind-friendly');
    } else {
      document.body.classList.remove('color-blind-friendly');
    }

    // Уменьшение движения
    if (settings.reducedMotion) {
      document.body.classList.add('reduced-motion');
    } else {
      document.body.classList.remove('reduced-motion');
    }

    // Поддержка скринридеров
    if (settings.screenReader) {
      document.body.setAttribute('aria-live', 'polite');
      document.body.setAttribute('aria-atomic', 'true');
    } else {
      document.body.removeAttribute('aria-live');
      document.body.removeAttribute('aria-atomic');
    }
  };

  const handleSaveSettings = () => {
    const settings = {
      factoryType,
      autoSave,
      notifications,
      language
    };

    const accessibilitySettings = {
      largeText,
      highContrast,
      colorBlindMode,
      reducedMotion,
      screenReader
    };

    localStorage.setItem('userSettings', JSON.stringify(settings));
    localStorage.setItem('accessibilitySettings', JSON.stringify(accessibilitySettings));

    // Применяем настройки инклюзивности
    applyAccessibilitySettings(accessibilitySettings);

    toast.success('Настройки успешно сохранены');
  };

  const handleResetAccessibility = () => {
    setLargeText(false);
    setHighContrast(false);
    setColorBlindMode(false);
    setReducedMotion(false);
    setScreenReader(false);

    // Сбрасываем стили
    const root = document.documentElement;
    root.style.fontSize = '';
    document.body.className = '';
    document.body.removeAttribute('aria-live');
    document.body.removeAttribute('aria-atomic');

    toast.info('Настройки доступности сброшены');
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
        {/* Настройки инклюзивности */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h5" gutterBottom color="primary">
                Настройки доступности
              </Typography>
              <Alert severity="info" sx={{ mb: 2 }}>
                Эти настройки помогут сделать приложение более удобным для людей с ограниченными возможностями
              </Alert>

              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={largeText}
                        onChange={(e) => setLargeText(e.target.checked)}
                        color="primary"
                      />
                    }
                    label="Увеличенный текст"
                    sx={{ mb: 2, display: 'block' }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Увеличивает размер текста по всему приложению
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={highContrast}
                        onChange={(e) => setHighContrast(e.target.checked)}
                        color="primary"
                      />
                    }
                    label="Высокая контрастность"
                    sx={{ mb: 2, display: 'block' }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Увеличивает контрастность интерфейса
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={colorBlindMode}
                        onChange={(e) => setColorBlindMode(e.target.checked)}
                        color="primary"
                      />
                    }
                    label="Режим для дальтоников"
                    sx={{ mb: 2, display: 'block' }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Оптимизирует цвета для людей с дальтонизмом
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={reducedMotion}
                        onChange={(e) => setReducedMotion(e.target.checked)}
                        color="primary"
                      />
                    }
                    label="Уменьшение анимации"
                    sx={{ mb: 2, display: 'block' }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Сокращает или убирает анимации и переходы
                  </Typography>
                </Grid>

                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={screenReader}
                        onChange={(e) => setScreenReader(e.target.checked)}
                        color="primary"
                      />
                    }
                    label="Режим для скринридеров"
                    sx={{ mb: 2, display: 'block' }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Улучшает совместимость со скринридерами
                  </Typography>
                </Grid>
              </Grid>

              <CardActions sx={{ justifyContent: 'flex-end', mt: 2 }}>
                <Button
                  variant="outlined"
                  color="secondary"
                  onClick={handleResetAccessibility}
                >
                  Сбросить настройки доступности
                </Button>
              </CardActions>
            </CardContent>
          </Card>
        </Grid>

        {/* Общие настройки */}
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
                sx={{ mb: 2, display: 'block' }}
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
                sx={{ mb: 2, display: 'block' }}
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

        {/* Настройки профиля */}
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
                Сохранить все настройки
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SettingsPage;