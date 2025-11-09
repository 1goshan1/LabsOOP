package ru.ssau.tk.cheefkeef.laba2.jdbc;

import ru.ssau.tk.cheefkeef.laba2.models.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PointDAO {
    private static final Logger logger = LoggerFactory.getLogger(PointDAO.class);

    private final String URL = "jdbc:postgresql://localhost:5432/laboop";
    private final String USER = "postgres";
    private final String PASSWORD = "1234";

    // SELECT - получение всех точек
    public List<Point> findAll() {
        logger.info("Начало получения всех точек");
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            logger.debug("Выполнение SQL: {}", sql);

            while (resultSet.next()) {
                Point point = mapResultSetToPoint(resultSet);
                points.add(point);
            }

            logger.info("Успешно получено {} точек", points.size());

        } catch (SQLException e) {
            logger.error("Ошибка при получении всех точек", e);
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // НОВЫЙ МЕТОД: Поиск с сортировкой по различным полям
    public List<Point> findWithSorting(String sortField, String sortDirection) {
        logger.info("Начало поиска точек с сортировкой: поле={}, направление={}", sortField, sortDirection);

        // Валидация полей для сортировки
        List<String> allowedFields = List.of("id", "f_id", "x_value", "y_value");
        if (!allowedFields.contains(sortField)) {
            logger.warn("Попытка сортировки по недопустимому полю: {}", sortField);
            sortField = "id"; // значение по умолчанию
        }

        // Валидация направления сортировки
        if (!"ASC".equalsIgnoreCase(sortDirection) && !"DESC".equalsIgnoreCase(sortDirection)) {
            logger.warn("Недопустимое направление сортировки: {}, установлено ASC", sortDirection);
            sortDirection = "ASC";
        }

        List<Point> points = new ArrayList<>();
        String sql = String.format("SELECT id, f_id, x_value, y_value FROM points ORDER BY %s %s",
                sortField, sortDirection);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            logger.debug("Выполнение SQL с сортировкой: {}", sql);

            while (resultSet.next()) {
                Point point = mapResultSetToPoint(resultSet);
                points.add(point);
            }

            logger.info("Успешно получено {} точек с сортировкой по {} {}",
                    points.size(), sortField, sortDirection);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек с сортировкой. Поле: {}, Направление: {}",
                    sortField, sortDirection, e);
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точки по ID
    public Optional<Point> findById(Integer id) {
        logger.info("Поиск точки по ID: {}", id);
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            logger.debug("Выполнение SQL: {} с параметром ID: {}", sql, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    logger.info("Точка с ID {} найдена", id);
                    return Optional.of(point);
                } else {
                    logger.info("Точка с ID {} не найдена", id);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точки по ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // SELECT - поиск точек по ID функции
    public List<Point> findByFunctionId(Integer functionId) {
        logger.info("Поиск точек по ID функции: {}", functionId);
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE f_id = ? ORDER BY x_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            logger.debug("Выполнение SQL: {} с параметром functionId: {}", sql, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

            logger.info("Найдено {} точек для функции ID: {}", points.size(), functionId);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по ID функции: {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точек по диапазону X значений
    public List<Point> findByXRange(Double minX, Double maxX) {
        logger.info("Поиск точек по диапазону X: от {} до {}", minX, maxX);
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE x_value BETWEEN ? AND ? ORDER BY x_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minX);
            statement.setDouble(2, maxX);
            logger.debug("Выполнение SQL: {} с параметрами minX: {}, maxX: {}", sql, minX, maxX);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

            logger.info("Найдено {} точек в диапазоне X от {} до {}", points.size(), minX, maxX);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по диапазону X: от {} до {}", minX, maxX, e);
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точек по диапазону Y значений
    public List<Point> findByYRange(Double minY, Double maxY) {
        logger.info("Поиск точек по диапазону Y: от {} до {}", minY, maxY);
        List<Point> points = new ArrayList<>();
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE y_value BETWEEN ? AND ? ORDER BY y_value";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, minY);
            statement.setDouble(2, maxY);
            logger.debug("Выполнение SQL: {} с параметрами minY: {}, maxY: {}", sql, minY, maxY);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    points.add(point);
                }
            }

            logger.info("Найдено {} точек в диапазоне Y от {} до {}", points.size(), minY, maxY);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точек по диапазону Y: от {} до {}", minY, maxY, e);
            throw new RuntimeException("Database error", e);
        }
        return points;
    }

    // SELECT - поиск точки по конкретным X и functionId
    public Optional<Point> findByFunctionIdAndX(Integer functionId, Double xValue) {
        logger.info("Поиск точки по functionId: {} и X: {}", functionId, xValue);
        String sql = "SELECT id, f_id, x_value, y_value FROM points WHERE f_id = ? AND x_value = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            statement.setDouble(2, xValue);
            logger.debug("Выполнение SQL: {} с параметрами functionId: {}, xValue: {}", sql, functionId, xValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Point point = mapResultSetToPoint(resultSet);
                    logger.info("Точка найдена для functionId: {} и X: {}", functionId, xValue);
                    return Optional.of(point);
                } else {
                    logger.info("Точка не найдена для functionId: {} и X: {}", functionId, xValue);
                    return Optional.empty();
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при поиске точки по functionId: {} и X: {}", functionId, xValue, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // INSERT - создание новой точки
    public Point insert(Point point) {
        logger.info("Создание новой точки: functionId={}, x={}, y={}",
                point.getFunctionId(), point.getXValue(), point.getYValue());
        String sql = "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, point.getFunctionId());
            statement.setDouble(2, point.getXValue());
            statement.setDouble(3, point.getYValue());
            logger.debug("Выполнение SQL: {} с параметрами: functionId={}, x={}, y={}",
                    sql, point.getFunctionId(), point.getXValue(), point.getYValue());

            int affectedRows = statement.executeUpdate();
            logger.debug("Количество затронутых строк: {}", affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        point.setId(generatedKeys.getInt(4));
                        //System.out.println(generatedKeys.getInt(4));
                        logger.info("Точка успешно создана с ID: {}", point.getId());
                        return point;
                    }
                }
            }

            logger.warn("Не удалось создать точку, затронуто 0 строк");

        } catch (SQLException e) {
            logger.error("Ошибка при создании точки: functionId={}, x={}, y={}",
                    point.getFunctionId(), point.getXValue(), point.getYValue(), e);
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    // INSERT - массовое добавление точек
    public int insertBatch(List<Point> points) {
        logger.info("Массовое добавление {} точек", points.size());
        String sql = "INSERT INTO points (f_id, x_value, y_value) VALUES (?, ?, ?)";
        int totalInserted = 0;

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false);
            logger.debug("Начало массовой вставки, авто-коммит отключен");

            for (Point point : points) {
                statement.setInt(1, point.getFunctionId());
                statement.setDouble(2, point.getXValue());
                statement.setDouble(3, point.getYValue());
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            connection.commit();
            logger.debug("Массовая вставка выполнена, коммит завершен");

            for (int result : batchResults) {
                if (result > 0) {
                    totalInserted++;
                }
            }

            logger.info("Успешно добавлено {} из {} точек", totalInserted, points.size());

        } catch (SQLException e) {
            logger.error("Ошибка при массовом добавлении {} точек", points.size(), e);
            throw new RuntimeException("Database error", e);
        }
        return totalInserted;
    }

    // UPDATE - обновление точки
    public boolean update(Point point) {
        logger.info("Обновление точки ID: {}", point.getId());
        String sql = "UPDATE points SET f_id = ?, x_value = ?, y_value = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, point.getFunctionId());
            statement.setDouble(2, point.getXValue());
            statement.setDouble(3, point.getYValue());
            statement.setInt(4, point.getId());
            logger.debug("Выполнение SQL: {} с параметрами: functionId={}, x={}, y={}, id={}",
                    sql, point.getFunctionId(), point.getXValue(), point.getYValue(), point.getId());

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Точка ID: {} успешно обновлена", point.getId());
            } else {
                logger.warn("Точка ID: {} не найдена для обновления", point.getId());
            }

            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при обновлении точки ID: {}", point.getId(), e);
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE - обновление Y значения точки
    public boolean updateYValue(Integer pointId, Double newYValue) {
        logger.info("Обновление Y значения точки ID: {} на {}", pointId, newYValue);
        String sql = "UPDATE points SET y_value = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, newYValue);
            statement.setInt(2, pointId);
            logger.debug("Выполнение SQL: {} с параметрами: yValue={}, id={}", sql, newYValue, pointId);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Y значение точки ID: {} успешно обновлено на {}", pointId, newYValue);
            } else {
                logger.warn("Точка ID: {} не найдена для обновления Y значения", pointId);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при обновлении Y значения точки ID: {}", pointId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление точки по ID
    public boolean delete(Integer id) {
        logger.info("Удаление точки ID: {}", id);
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            logger.debug("Выполнение SQL: {} с параметром ID: {}", sql, id);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Точка ID: {} успешно удалена", id);
            } else {
                logger.warn("Точка ID: {} не найдена для удаления", id);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точки ID: {}", id, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление всех точек функции
    public boolean deleteByFunctionId(Integer functionId) {
        logger.info("Удаление всех точек функции ID: {}", functionId);
        String sql = "DELETE FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            logger.debug("Выполнение SQL: {} с параметром functionId: {}", sql, functionId);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Удалено {} точек функции ID: {}", affectedRows, functionId);
            } else {
                logger.info("Не найдено точек для удаления функции ID: {}", functionId);
            }

            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении точек функции ID: {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // COUNT - подсчет количества точек функции
    public int countByFunctionId(Integer functionId) {
        logger.debug("Подсчет количества точек функции ID: {}", functionId);
        String sql = "SELECT COUNT(*) FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            logger.debug("Выполнение SQL: {} с параметром functionId: {}", sql, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    logger.debug("Найдено {} точек для функции ID: {}", count, functionId);
                    return count;
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при подсчете точек функции ID: {}", functionId, e);
            throw new RuntimeException("Database error", e);
        }
        return 0;
    }

    // EXISTS - проверка существования точки с таким X для функции
    public boolean existsByFunctionIdAndX(Integer functionId, Double xValue) {
        logger.debug("Проверка существования точки functionId: {}, x: {}", functionId, xValue);
        String sql = "SELECT 1 FROM points WHERE f_id = ? AND x_value = ? LIMIT 1";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            statement.setDouble(2, xValue);
            logger.debug("Выполнение SQL: {} с параметрами: functionId={}, xValue={}", sql, functionId, xValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                boolean exists = resultSet.next();
                logger.debug("Точка functionId: {}, x: {} существует: {}", functionId, xValue, exists);
                return exists;
            }

        } catch (SQLException e) {
            logger.error("Ошибка при проверке существования точки functionId: {}, x: {}", functionId, xValue, e);
            throw new RuntimeException("Database error", e);
        }
    }

    // SELECT - получение минимального и максимального X для функции
    public double[] getXRangeForFunction(Integer functionId) {
        logger.debug("Получение диапазона X для функции ID: {}", functionId);
        String sql = "SELECT MIN(x_value), MAX(x_value) FROM points WHERE f_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, functionId);
            logger.debug("Выполнение SQL: {} с параметром functionId: {}", sql, functionId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double minX = resultSet.getDouble(1);
                    double maxX = resultSet.getDouble(2);
                    logger.debug("Диапазон X для функции ID: {} - от {} до {}", functionId, minX, maxX);
                    return new double[]{minX, maxX};
                }
            }

        } catch (SQLException e) {
            logger.error("Ошибка при получении диапазона X для функции ID: {}", functionId, e);
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
        logger.trace("Маппинг ResultSet в Point: id={}, functionId={}, x={}, y={}",
                point.getId(), point.getFunctionId(), point.getXValue(), point.getYValue());
        return point;
    }
}