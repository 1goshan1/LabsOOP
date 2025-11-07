package ru.ssau.tk.cheefkeef.laba2.jdbc;

import ru.ssau.tk.cheefkeef.laba2.models.Function;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FunctionDAO {
    private final String URL = "jdbc:postgresql://localhost:5432/laboop";
    private final String USER = "postgres";
    private final String PASSWORD = "1234";


    // SELECT - получение всех функций
    public List<Function> findAll() {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT id, u_id, name, signature FROM functions";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Function function = mapResultSetToFunction(resultSet);
                functions.add(function);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении функций: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return functions;
    }

    // SELECT - поиск функции по ID
    public Optional<Function> findById(Integer id) {
        String sql = "SELECT id, u_id, name, signature FROM functions WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapResultSetToFunction(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске функции по ID: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return Optional.empty();
    }

    // SELECT - поиск функций по ID пользователя
    public List<Function> findByUserId(Integer userId) {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT id, u_id, name, signature FROM functions WHERE u_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Function function = mapResultSetToFunction(resultSet);
                    functions.add(function);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске функций по ID пользователя: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return functions;
    }

    // SELECT - поиск функций по имени (частичное совпадение)
    public List<Function> findByName(String name) {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT id, u_id, name, signature FROM functions WHERE name LIKE ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + name + "%");

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Function function = mapResultSetToFunction(resultSet);
                    functions.add(function);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске функций по имени: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return functions;
    }

    // SELECT - поиск функций по имени и ID пользователя
    public List<Function> findByNameAndUserId(String name, Integer userId) {
        List<Function> functions = new ArrayList<>();
        String sql = "SELECT id, u_id, name, signature FROM functions WHERE name = ? AND u_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Function function = mapResultSetToFunction(resultSet);
                    functions.add(function);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске функций по имени и ID пользователя: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return functions;
    }

    // INSERT - создание новой функции
    public Function insert(Function function) {
        String sql = "INSERT INTO functions (u_id, name, signature) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, function.getUserId());
            statement.setString(2, function.getName());
            statement.setString(3, function.getSignature());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        function.setId(generatedKeys.getInt(1));
                        return function;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при создании функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    // UPDATE - обновление функции
    public boolean update(Function function) {
        String sql = "UPDATE functions SET u_id = ?, name = ?, signature = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, function.getUserId());
            statement.setString(2, function.getName());
            statement.setString(3, function.getSignature());
            statement.setInt(4, function.getId());

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // UPDATE - обновление только сигнатуры функции
    public boolean updateSignature(Integer functionId, String newSignature) {
        String sql = "UPDATE functions SET signature = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newSignature);
            statement.setInt(2, functionId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении сигнатуры: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление функции по ID
    public boolean delete(Integer id) {
        String sql = "DELETE FROM functions WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // DELETE - удаление всех функций пользователя
    public boolean deleteByUserId(Integer userId) {
        String sql = "DELETE FROM functions WHERE u_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении функций пользователя: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // COUNT - подсчет количества функций пользователя
    public int countByUserId(Integer userId) {
        String sql = "SELECT COUNT(*) FROM functions WHERE u_id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при подсчете функций пользователя: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
        return 0;
    }

    // EXISTS - проверка существования функции по имени и пользователю
    public boolean existsByNameAndUserId(String name, Integer userId) {
        String sql = "SELECT 1 FROM functions WHERE name = ? AND u_id = ? LIMIT 1";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования функции: " + e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    // Вспомогательный метод для маппинга ResultSet в Function
    private Function mapResultSetToFunction(ResultSet resultSet) throws SQLException {
        Function function = new Function();
        function.setId(resultSet.getInt("id"));
        function.setUserId(resultSet.getInt("u_id"));
        function.setName(resultSet.getString("name"));
        function.setSignature(resultSet.getString("signature"));
        return function;
    }
}