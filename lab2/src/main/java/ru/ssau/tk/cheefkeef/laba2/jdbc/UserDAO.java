package ru.ssau.tk.cheefkeef.laba2.jdbc;

import ru.ssau.tk.cheefkeef.laba2.models.User;
import ru.ssau.tk.cheefkeef.laba2.models.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final String URL = "jdbc:postgresql://localhost:5432/laboop";
    private final String USER = "postgres";
    private final String PASSWORD = "1234";

    // SELECT - получение всех пользователей
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, role, password FROM users";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                User user = mapResultSetToUser(resultSet);
                users.add(user);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при получении пользователей: " + e.getMessage());
        }
        return users;
    }

    // SELECT - поиск пользователя по ID
    public User findById(Integer id) {
        String sql = "SELECT id, login, role, password FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пользователя по ID: " + e.getMessage());
        }
        return null;
    }

    // SELECT - поиск пользователя по Login
    public User findByLogin(String login) {
        String sql = "SELECT id, login, role, password FROM users WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToUser(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пользователя по login: " + e.getMessage());
        }
        return null;
    }

    // SELECT - поиск пользователей по роли
    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, role, password FROM users WHERE role = ?::role_enum";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    users.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске пользователей по роли: " + e.getMessage());
        }
        return users;
    }

    // INSERT - создание нового пользователя
    public User insert(User user) {
        String sql = "INSERT INTO users (login, role, password) VALUES (?, ?::role_enum, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getRole()); // Используем enum value
            statement.setString(3, user.getPassword());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        return user;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при создании пользователя: " + e.getMessage());
        }
        return null;
    }

    // UPDATE - обновление пользователя
    public boolean update(User user) {
        String sql = "UPDATE users SET login = ?, role = ?::role_enum, password = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getRole()); // Используем enum value
            statement.setString(3, user.getPassword());
            statement.setInt(4, user.getId());

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении пользователя: " + e.getMessage());
        }
        return false;
    }

    // DELETE - удаление пользователя по ID
    public boolean delete(Integer id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
        }
        return false;
    }

    // DELETE - удаление пользователя по login
    public boolean deleteByLogin(String login) {
        String sql = "DELETE FROM users WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении пользователя: " + e.getMessage());
        }
        return false;
    }

    // Вспомогательный метод для маппинга ResultSet в User
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));

        // Конвертируем строку из БД в Java enum
        String roleString = resultSet.getString("role");
        user.setRole(roleString);

        user.setPassword(resultSet.getString("password"));
        return user;
    }
}