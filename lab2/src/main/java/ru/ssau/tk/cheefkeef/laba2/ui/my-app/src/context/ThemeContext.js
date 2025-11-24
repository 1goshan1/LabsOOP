import React, { createContext, useContext, useState, useEffect, useMemo } from 'react';
import { createTheme, ThemeProvider as MuiThemeProvider } from '@mui/material/styles';

const ThemeContext = createContext(null);

// Функция для получения настроек доступности из localStorage
const getStoredAccessibilitySettings = () => {
  try {
    const stored = localStorage.getItem('accessibilitySettings');
    return stored ? JSON.parse(stored) : {
      largeText: false,
      highContrast: false,
      colorBlindMode: false,
      reducedMotion: false,
      screenReader: false
    };
  } catch {
    return {
      largeText: false,
      highContrast: false,
      colorBlindMode: false,
      reducedMotion: false,
      screenReader: false
    };
  }
};

// Функция применения CSS-классов для доступности
const applyAccessibilityStyles = (accessibility) => {
  const root = document.documentElement;
  const body = document.body;

  // Большой текст
  if (accessibility.largeText) {
    root.style.fontSize = '18px';
    body.classList.add('large-text');
  } else {
    root.style.fontSize = '';
    body.classList.remove('large-text');
  }

  // Высокая контрастность
  if (accessibility.highContrast) {
    body.classList.add('high-contrast');
  } else {
    body.classList.remove('high-contrast');
  }

  // Режим для дальтоников
  if (accessibility.colorBlindMode) {
    body.classList.add('color-blind-friendly');
  } else {
    body.classList.remove('color-blind-friendly');
  }

  // Уменьшение движения
  if (accessibility.reducedMotion) {
    body.classList.add('reduced-motion');
  } else {
    body.classList.remove('reduced-motion');
  }

  // Поддержка скринридеров
  if (accessibility.screenReader) {
    body.setAttribute('aria-live', 'polite');
    body.setAttribute('aria-atomic', 'true');
  } else {
    body.removeAttribute('aria-live');
    body.removeAttribute('aria-atomic');
  }
};

const getDesignTokens = (mode, accessibility = {}) => ({
  palette: {
    mode,
    ...(mode === 'dark'
      ? {
          // Темная тема с учетом доступности
          primary: {
            main: accessibility.colorBlindMode ? '#4dabf5' :
                  accessibility.highContrast ? '#ffffff' : '#90caf9',
            contrastText: accessibility.highContrast ? '#000000' : '#ffffff',
          },
          secondary: {
            main: accessibility.colorBlindMode ? '#ffa726' :
                  accessibility.highContrast ? '#ffcc00' : '#f48fb1',
            contrastText: accessibility.highContrast ? '#000000' : '#ffffff',
          },
          error: {
            main: accessibility.colorBlindMode ? '#ff6b6b' :
                  accessibility.highContrast ? '#ff4444' : '#f44336',
          },
          warning: {
            main: accessibility.colorBlindMode ? '#ffd93d' :
                  accessibility.highContrast ? '#ffaa00' : '#ff9800',
          },
          info: {
            main: accessibility.colorBlindMode ? '#51cf66' :
                  accessibility.highContrast ? '#00aaff' : '#29b6f6',
          },
          success: {
            main: accessibility.colorBlindMode ? '#69db7c' :
                  accessibility.highContrast ? '#00cc66' : '#66bb6a',
          },
          background: {
            default: accessibility.highContrast ? '#000000' : '#121212',
            paper: accessibility.highContrast ? '#1a1a1a' : '#1e1e1e',
          },
          text: {
            primary: accessibility.highContrast ? '#ffffff' : '#ffffff',
            secondary: accessibility.highContrast ? '#cccccc' : '#b0b0b0',
            disabled: accessibility.highContrast ? '#666666' : 'rgba(255, 255, 255, 0.5)',
          },
          divider: accessibility.highContrast ? '#ffffff' : 'rgba(255, 255, 255, 0.12)',
          action: {
            active: accessibility.highContrast ? '#ffffff' : '#ffffff',
            hover: accessibility.highContrast ? '#333333' : 'rgba(255, 255, 255, 0.08)',
            selected: accessibility.highContrast ? '#444444' : 'rgba(255, 255, 255, 0.16)',
            disabled: accessibility.highContrast ? '#666666' : 'rgba(255, 255, 255, 0.3)',
            disabledBackground: accessibility.highContrast ? '#333333' : 'rgba(255, 255, 255, 0.12)',
          },
        }
      : {
          // Светлая тема с учетом доступности
          primary: {
            main: accessibility.colorBlindMode ? '#1971c2' :
                  accessibility.highContrast ? '#000000' : '#1976d2',
            contrastText: accessibility.highContrast ? '#ffffff' : '#ffffff',
          },
          secondary: {
            main: accessibility.colorBlindMode ? '#e67700' :
                  accessibility.highContrast ? '#666666' : '#dc004e',
            contrastText: accessibility.highContrast ? '#ffffff' : '#ffffff',
          },
          error: {
            main: accessibility.colorBlindMode ? '#e03131' :
                  accessibility.highContrast ? '#cc0000' : '#f44336',
          },
          warning: {
            main: accessibility.colorBlindMode ? '#f08c00' :
                  accessibility.highContrast ? '#996600' : '#ff9800',
          },
          info: {
            main: accessibility.colorBlindMode ? '#099268' :
                  accessibility.highContrast ? '#0066cc' : '#29b6f6',
          },
          success: {
            main: accessibility.colorBlindMode ? '#2f9e44' :
                  accessibility.highContrast ? '#008844' : '#66bb6a',
          },
          background: {
            default: accessibility.highContrast ? '#ffffff' : '#f5f5f5',
            paper: accessibility.highContrast ? '#f8f8f8' : '#ffffff',
          },
          text: {
            primary: accessibility.highContrast ? '#000000' : '#000000',
            secondary: accessibility.highContrast ? '#333333' : '#555555',
            disabled: accessibility.highContrast ? '#999999' : 'rgba(0, 0, 0, 0.38)',
          },
          divider: accessibility.highContrast ? '#000000' : 'rgba(0, 0, 0, 0.12)',
          action: {
            active: accessibility.highContrast ? '#000000' : 'rgba(0, 0, 0, 0.54)',
            hover: accessibility.highContrast ? '#f0f0f0' : 'rgba(0, 0, 0, 0.04)',
            selected: accessibility.highContrast ? '#e0e0e0' : 'rgba(0, 0, 0, 0.08)',
            disabled: accessibility.highContrast ? '#cccccc' : 'rgba(0, 0, 0, 0.26)',
            disabledBackground: accessibility.highContrast ? '#eeeeee' : 'rgba(0, 0, 0, 0.12)',
          },
        }),
  },
  typography: {
    fontFamily: [
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
      '"Apple Color Emoji"',
      '"Segoe UI Emoji"',
      '"Segoe UI Symbol"',
    ].join(','),
    ...(accessibility.largeText && {
      h1: {
        fontWeight: 700,
        fontSize: '2.5rem',
        lineHeight: 1.2
      },
      h2: {
        fontWeight: 600,
        fontSize: '2rem',
        lineHeight: 1.3
      },
      h3: {
        fontWeight: 600,
        fontSize: '1.75rem',
        lineHeight: 1.3
      },
      h4: {
        fontWeight: 500,
        fontSize: '1.5rem',
        lineHeight: 1.4
      },
      h5: {
        fontWeight: 500,
        fontSize: '1.25rem',
        lineHeight: 1.4
      },
      h6: {
        fontWeight: 500,
        fontSize: '1.1rem',
        lineHeight: 1.4
      },
      body1: {
        fontSize: '1.1rem',
        lineHeight: 1.6
      },
      body2: {
        fontSize: '1rem',
        lineHeight: 1.5
      },
      button: {
        fontSize: '1rem',
        fontWeight: 500
      },
      caption: {
        fontSize: '0.9rem',
        lineHeight: 1.4
      },
      overline: {
        fontSize: '0.8rem',
        lineHeight: 1.3
      }
    }),
    ...(!accessibility.largeText && {
      h1: { fontWeight: 700 },
      h2: { fontWeight: 600 },
      h3: { fontWeight: 600 },
      h4: { fontWeight: 500 },
      h5: { fontWeight: 500 },
      h6: { fontWeight: 500 },
    }),
  },
  shape: {
    borderRadius: 8,
  },
  spacing: 8,
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          borderRadius: 8,
          fontWeight: 500,
          ...(accessibility.largeText && {
            padding: '10px 20px',
            fontSize: '1.1rem',
            minHeight: '48px'
          }),
          ...(accessibility.highContrast && {
            borderWidth: '2px',
            borderStyle: 'solid'
          }),
        },
        outlined: {
          ...(accessibility.highContrast && {
            borderWidth: '2px'
          }),
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: accessibility.highContrast
            ? `0 0 0 2px ${mode === 'dark' ? '#ffffff' : '#000000'}, 0 4px 12px rgba(0,0,0,0.1)`
            : '0 4px 12px rgba(0,0,0,0.1)',
          ...(accessibility.highContrast && {
            border: `2px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          ...(accessibility.highContrast && {
            border: `1px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
    MuiTextField: {
      styleOverrides: {
        root: {
          ...(accessibility.largeText && {
            '& .MuiInputBase-root': {
              fontSize: '1.1rem'
            }
          }),
          ...(accessibility.highContrast && {
            '& .MuiOutlinedInput-root': {
              '& fieldset': {
                borderWidth: '2px'
              }
            }
          }),
        },
      },
    },
    MuiSelect: {
      styleOverrides: {
        root: {
          ...(accessibility.largeText && {
            fontSize: '1.1rem'
          }),
          ...(accessibility.highContrast && {
            '& .MuiOutlinedInput-notchedOutline': {
              borderWidth: '2px'
            }
          }),
        },
      },
    },
    MuiSwitch: {
      styleOverrides: {
        root: {
          ...(accessibility.largeText && {
            transform: 'scale(1.1)'
          }),
        },
        track: {
          ...(accessibility.reducedMotion && {
            transition: 'none'
          }),
        },
        thumb: {
          ...(accessibility.reducedMotion && {
            transition: 'none'
          }),
        },
      },
    },
    MuiAlert: {
      styleOverrides: {
        root: {
          ...(accessibility.largeText && {
            fontSize: '1.1rem',
            '& .MuiAlert-icon': {
              fontSize: '1.3rem'
            }
          }),
          ...(accessibility.highContrast && {
            border: '2px solid',
            borderColor: 'inherit'
          }),
        },
      },
    },
    MuiTable: {
      styleOverrides: {
        root: {
          ...(accessibility.largeText && {
            '& .MuiTableCell-root': {
              fontSize: '1.1rem',
              padding: '12px 16px'
            }
          }),
          ...(accessibility.highContrast && {
            border: `2px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          ...(accessibility.highContrast && {
            borderBottom: `2px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
    MuiAppBar: {
      styleOverrides: {
        root: {
          ...(accessibility.highContrast && {
            borderBottom: `2px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          ...(accessibility.highContrast && {
            borderRight: `2px solid ${mode === 'dark' ? '#ffffff' : '#000000'}`,
          }),
        },
      },
    },
  },
  ...(accessibility.reducedMotion && {
    transitions: {
      create: () => 'none',
    },
  }),
});

export const ThemeProvider = ({ children }) => {
  const [mode, setMode] = useState(() => {
    const storedMode = localStorage.getItem('themeMode');
    return storedMode || 'dark';
  });

  const [accessibility, setAccessibility] = useState(getStoredAccessibilitySettings);

  // Применяем настройки доступности при загрузке и изменении
  useEffect(() => {
    applyAccessibilityStyles(accessibility);
  }, [accessibility]);

  // Сохраняем тему в localStorage
  useEffect(() => {
    localStorage.setItem('themeMode', mode);
  }, [mode]);

  // Сохраняем настройки доступности в localStorage
  useEffect(() => {
    localStorage.setItem('accessibilitySettings', JSON.stringify(accessibility));
  }, [accessibility]);

  const toggleTheme = () => {
    setMode((prevMode) => (prevMode === 'dark' ? 'light' : 'dark'));
  };

  const updateAccessibility = (newSettings) => {
    setAccessibility(newSettings);
  };

  const resetAccessibility = () => {
    const defaultSettings = {
      largeText: false,
      highContrast: false,
      colorBlindMode: false,
      reducedMotion: false,
      screenReader: false
    };
    setAccessibility(defaultSettings);
    applyAccessibilityStyles(defaultSettings);
  };

  // Используем useMemo для оптимизации пересоздания темы
  const theme = useMemo(() =>
    createTheme(getDesignTokens(mode, accessibility)),
    [mode, accessibility]
  );

  const value = {
    mode,
    toggleTheme,
    accessibility,
    updateAccessibility,
    resetAccessibility
  };

  return (
    <ThemeContext.Provider value={value}>
      <MuiThemeProvider theme={theme}>{children}</MuiThemeProvider>
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};

// Хук для быстрого доступа к настройкам доступности
export const useAccessibility = () => {
  const { accessibility, updateAccessibility, resetAccessibility } = useTheme();
  return { accessibility, updateAccessibility, resetAccessibility };
};