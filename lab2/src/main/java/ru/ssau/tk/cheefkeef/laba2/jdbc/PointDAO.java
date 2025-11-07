package ru.ssau.tk.cheefkeef.laba2.jdbc;

import ru.ssau.tk.cheefkeef.laba2.models.Point;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointDAO {
    private final String URL = "jdbc:postgresql://localhost:5432/laboop";
    private final String USER = "postgres";
    private final String PASSWORD = "1234";

    // SELECT - получение всех точек
    public List<Point> findAll() {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Point point = mapResultSetToPoint(resultSet);
                points.add(point);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении точек: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точки по ID
    public Optional<Point> findById(Integer id) {
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToPoint(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске точки по ID: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    // SELECT - поиск точек по ID функции
    public List<Point> findByFunctionId(Integer functionId) {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE f_id = ? ORDER BY x_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске точек по ID функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точек по диапазону X значений
    public List<Point> findByXRange(Double minX, Double maxX) {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE x_value BETWEEN ? AND ? ORDER BY x_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minX);
            statement.setDouble(2, maxX);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске точек по диапазону X: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точек по диапазону Y значений
    public List<Point> findByYRange(Double minY, Double maxY) {
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE y_value BETWEEN ? AND ? ORDER BY y_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minY);
            statement.setDouble(2, maxY);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске точек по диапазону Y: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точки по конкретным X и functionId
    public Optional<Point> findByFunctionIdAndX(Integer functionId, Double xValue) {
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE f_id = ? AND x_value = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            statement.setDouble(2, xValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToPoint(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске точки по functionId и X: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    // INSERT - создание новой точки
    public Point insert(Point point) {
        String sql = "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, point.getFunctionId());
            statement.setDouble(2, point.getXValue());
            statement.setDouble(3, point.getYValue());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        point.setId(generatedKeys.getInt(1));
                        return point;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при создании точки: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    // INSERT - массовое добавление точек
    public int insertBatch(List<Point> points) {
        String sql = "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?)";
        int totalInserted = 0;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);

            for (Point point : points) {
                statement.setInt(1, point.getFunctionId());
                statement.setDouble(2, point.getXValue());
                statement.setDouble(3, point.getYValue());
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            connection.commit();

            for (int result : batchResults) {
                if (result > 0) {
                    totalInserted++;
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при массовом добавлении точек: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return totalInserted;
    }

    // UPDATE - обновление точки
    public boolean update(Point point) {
        String sql = "UPDATE points SET f_id = ?, x_value = ?, y_value = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, point.getFunctionId());
            statement.setDouble(2, point.getXValue());
            statement.setDouble(3, point.getYValue());
            statement.setInt(4, point.getId());

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении точки: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE - обновление Y значения точки
    public boolean updateYValue(Integer pointId, Double newYValue) {
        String sql = "UPDATE points SET y_value = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, newYValue);
            statement.setInt(2, pointId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении Y значения: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление точки по ID
    public boolean delete(Integer id) {
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении точки: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление всех точек функции
    public boolean deleteByFunctionId(Integer functionId) {
        String sql = "DELETE FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении точек функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // COUNT - подсчет количества точек функции
    public int countByFunctionId(Integer functionId) {
        String sql = "SELECT COUNT(*) FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете точек функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return 0;
    }

    // EXISTS - проверка существования точки с таким X для функции
    public boolean existsByFunctionIdAndX(Integer functionId, Double xValue) {
        String sql = "SELECT 1 FROM points WHERE f_id = ? AND x_value = ? LIMIT 1";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            statement.setDouble(2, xValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования точки: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // SELECT - получение минимального и максимального X для функции
    public double[] getXRangeForFunction(Integer functionId) {
        String sql = "SELECT MIN(x_value), MAX(x_value) FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double minX = resultSet.getDouble(1);
                    double maxX = resultSet.getDouble(2);
                    return new double[]{minX, maxX};
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении диапазона X: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return new double[]{0.0, 0.0};
    }

    // Вспомогательный метод для маппинга ResultSet в Point
    private Point mapResultSetToPoint(ResultSet resultSet) throws SQLException {
        Point point = new Point();
        point.setId(resultSet.getInt("id"));
        point.setFunctionId(resultSet.getInt("f_id"));
        point.setXValue(resultSet.getDouble("x_value"));
        point.setYValue(resultSet.getDouble("y_value"));
        return point;
    }
}