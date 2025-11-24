import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, IconButton, Dialog, DialogTitle, DialogContent, DialogActions, TextField } from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { getFunctionsByUser, deleteFunction } from '../api/functions';
import { Delete as DeleteIcon, Add as AddIcon, Visibility as VisibilityIcon, Edit as EditIcon } from '@mui/icons-material';
import { toast } from 'react-toastify';
import { Link } from 'react-router-dom';

const FunctionsPage = () => {
  const { user } = useAuth();
  const [functions, setFunctions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [deleteDialog, setDeleteDialog] = useState({ open: false, functionId: null });

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

  const handleDeleteClick = (functionId) => {
    setDeleteDialog({ open: true, functionId });
  };

  const handleDeleteConfirm = async () => {
    try {
      await deleteFunction(deleteDialog.functionId);
      toast.success('Функция удалена');
      setFunctions(prev => prev.filter(func => func.id !== deleteDialog.functionId));
      setDeleteDialog({ open: false, functionId: null });
    } catch (error) {
      toast.error('Ошибка при удалении функции');
    }
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
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Мои функции</Typography>
        <Button
          component={Link}
          to="/functions/new"
          variant="contained"
          startIcon={<AddIcon />}
        >
          Создать функцию
        </Button>
      </Box>

      {functions.length === 0 ? (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1" gutterBottom>
            У вас пока нет созданных функций
          </Typography>
          <Button
            component={Link}
            to="/functions/new"
            variant="contained"
            color="primary"
          >
            Создать первую функцию
          </Button>
        </Paper>
      ) : (
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>ID</TableCell>
                <TableCell>Название</TableCell>
                <TableCell>Сигнатура</TableCell>
                <TableCell>Действия</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {functions.map(func => (
                <TableRow key={func.id}>
                  <TableCell>{func.id}</TableCell>
                  <TableCell>{func.name}</TableCell>
                  <TableCell>{func.signature}</TableCell>
                  <TableCell>
                    <IconButton
                      component={Link}
                      to={`/functions/${func.id}/graph`}
                      color="primary"
                      title="Просмотреть график"
                    >
                      <VisibilityIcon />
                    </IconButton>
                    <IconButton
                      component={Link}
                      to={`/functions/${func.id}/edit`}
                      color="info"
                      title="Редактировать функцию"
                    >
                      <EditIcon />
                    </IconButton>
                    <IconButton
                      color="error"
                      onClick={() => handleDeleteClick(func.id)}
                      title="Удалить функцию"
                    >
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, functionId: null })}>
        <DialogTitle>Подтверждение удаления</DialogTitle>
        <DialogContent>
          <Typography>Вы уверены, что хотите удалить эту функцию? Это действие нельзя отменить.</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialog({ open: false, functionId: null })}>Отмена</Button>
          <Button onClick={handleDeleteConfirm} color="error" variant="contained">
            Удалить
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default FunctionsPage;