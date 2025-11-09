package ru.ssau.tk.cheefkeef.laba2.jdbc;

import ru.ssau.tk.cheefkeef.laba2.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final String URL = "jdbc:postgresql://localhost:5432/laboop";
    private final String USER = "postgres";
    private final String PASSWORD = "1234";

    // SELECT - получение всех пользователей
    public List<User> findAll() {
        logger.info("Начало получения всех пользователей");
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, role, password, enabled FROM users";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                User user = mapResultSetToUser(resultSet);
                users.add(user);
            }
            logger.info("Успешно получено {} пользователей", users.size());

        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователей", e);
        }
        return users;
    }

    // SELECT - поиск пользователя по ID
    public User findById(Integer id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        String sql = "SELECT id, login, role, password, enabled FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    logger.debug("Пользователь с ID {} найден: {}", id, user.getLogin());
                    return user;
                }
            }
            logger.debug("Пользователь с ID {} не найден", id);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, e);
        }
        return null;
    }

    // SELECT - поиск пользователя по Login
    public User findByLogin(String login) {
        logger.debug("Поиск пользователя по login: {}", login);
        String sql = "SELECT id, login, role, password, enabled FROM users WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    logger.debug("Пользователь с login {} найден", login);
                    return user;
                }
            }
            logger.debug("Пользователь с login {} не найден", login);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователя по login: {}", login, e);
        }
        return null;
    }

    // SELECT - поиск пользователей по роли
    public List<User> findByRole(String role) {
        logger.debug("Поиск пользователей по роли: {}", role);
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, role, password, enabled FROM users WHERE role = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    users.add(user);
                }
            }
            logger.debug("Найдено {} пользователей с ролью {}", users.size(), role);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по роли: {}", role, e);
        }
        return users;
    }

    // SELECT - поиск активных/неактивных пользователей
    public List<User> findByEnabledStatus(Boolean enabled) {
        logger.debug("Поиск пользователей по статусу enabled: {}", enabled);
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, login, role, password, enabled FROM users WHERE enabled = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, enabled);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    users.add(user);
                }
            }
            logger.debug("Найдено {} пользователей со статусом enabled = {}", users.size(), enabled);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по статусу enabled: {}", enabled, e);
        }
        return users;
    }

    // INSERT - создание нового пользователя
    public User insert(User user) {
        logger.info("Создание нового пользователя: {}", user.getLogin());
        String sql = "INSERT INTO users (login, role, password, enabled) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getRole());
            statement.setString(3, user.getPassword());
            statement.setBoolean(4, user.getEnabled() != null ? user.getEnabled() : true);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                        logger.info("Пользователь {} успешно создан с ID: {}", user.getLogin(), user.getId());
                        return user;
                    }
                }
            }
            logger.warn("Пользователь {} не был создан, affectedRows: {}", user.getLogin(), affectedRows);

        } catch (SQLException e) {
            logger.error("Ошибка при создании пользователя: {}", user.getLogin(), e);
        }
        return null;
    }

    // UPDATE - обновление пользователя
    public boolean update(User user) {
        logger.info("Обновление пользователя с ID: {}", user.getId());
        String sql = "UPDATE users SET login = ?, role = ?, password = ?, enabled = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getLogin());
            statement.setString(2, user.getRole());
            statement.setString(3, user.getPassword());
            statement.setBoolean(4, user.getEnabled() != null ? user.getEnabled() : true);
            statement.setInt(5, user.getId());

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Пользователь с ID {} успешно обновлен", user.getId());
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления", user.getId());
            }
            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пользователя с ID: {}", user.getId(), e);
        }
        return false;
    }

    // UPDATE - обновление статуса enabled
    public boolean updateEnabledStatus(Integer id, Boolean enabled) {
        logger.info("Обновление статуса enabled для пользователя с ID: {}, новое значение: {}", id, enabled);
        String sql = "UPDATE users SET enabled = ? WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, enabled != null ? enabled : true);
            statement.setInt(2, id);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Статус enabled для пользователя с ID {} успешно обновлен на {}", id, enabled);
            } else {
                logger.warn("Пользователь с ID {} не найден для обновления статуса", id);
            }
            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при обновлении статуса enabled для пользователя с ID: {}", id, e);
        }
        return false;
    }

    // DELETE - удаление пользователя по ID
    public boolean delete(Integer id) {
        logger.info("Удаление пользователя с ID: {}", id);
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Пользователь с ID {} успешно удален", id);
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
            }
            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
        }
        return false;
    }

    // DELETE - удаление пользователя по login
    public boolean deleteByLogin(String login) {
        logger.info("Удаление пользователя с login: {}", login);
        String sql = "DELETE FROM users WHERE login = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);

            int affectedRows = statement.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Пользователь с login {} успешно удален", login);
            } else {
                logger.warn("Пользователь с login {} не найден для удаления", login);
            }
            return success;

        } catch (SQLException e) {
            logger.error("Ошибка при удалении пользователя с login: {}", login, e);
        }
        return false;
    }

    // Вспомогательный метод для маппинга ResultSet в User
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setLogin(resultSet.getString("login"));

        String roleString = resultSet.getString("role");
        user.setRole(roleString);

        user.setPassword(resultSet.getString("password"));
        user.setEnabled(resultSet.getBoolean("enabled"));

        // Обработка случая, когда значение может быть NULL
        if (resultSet.wasNull()) {
            user.setEnabled(true); // значение по умолчанию
        }

        return user;
    }

    public List<User> findByIds(List<Integer> ids) {
        logger.debug("Множественный поиск пользователей по IDs: {}", ids);
        List<User> users = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            logger.debug("Передан пустой список IDs");
            return users;
        }

        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = String.format("SELECT id, login, role, password, enabled FROM users WHERE id IN (%s)", placeholders);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                statement.setInt(i + 1, ids.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    users.add(user);
                }
            }
            logger.debug("Найдено {} пользователей из запрошенных {}", users.size(), ids.size());

        } catch (SQLException e) {
            logger.error("Ошибка при множественном поиске пользователей по IDs: {}", ids, e);
        }
        return users;
    }

    public List<User> findAllWithSorting(String sortField, boolean ascending) {
        logger.debug("Получение всех пользователей с сортировкой по полю: {}, порядок: {}",
                sortField, ascending ? "ASC" : "DESC");
        List<User> users = new ArrayList<>();

        List<String> allowedFields = List.of("id", "login", "role", "password", "enabled");
        if (!allowedFields.contains(sortField.toLowerCase())) {
            logger.warn("Недопустимое поле для сортировки: {}, используется поле по умолчанию: id", sortField);
            sortField = "id";
        }

        String direction = ascending ? "ASC" : "DESC";
        String sql = String.format("SELECT id, login, role, password, enabled FROM users ORDER BY %s %s", sortField, direction);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                User user = mapResultSetToUser(resultSet);
                users.add(user);
            }
            logger.debug("Успешно получено {} пользователей с сортировкой", users.size());

        } catch (SQLException e) {
            logger.error("Ошибка при получении пользователей с сортировкой по полю: {}", sortField, e);
        }
        return users;
    }

    public List<User> findByRoleWithSorting(String role, String sortField, boolean ascending) {
        logger.debug("Поиск пользователей по роли {} с сортировкой по полю: {}, порядок: {}",
                role, sortField, ascending ? "ASC" : "DESC");
        List<User> users = new ArrayList<>();

        List<String> allowedFields = List.of("id", "login", "role", "password", "enabled");
        if (!allowedFields.contains(sortField.toLowerCase())) {
            logger.warn("Недопустимое поле для сортировки: {}, используется поле по умолчанию: id", sortField);
            sortField = "id";
        }

        String direction = ascending ? "ASC" : "DESC";
        String sql = String.format("SELECT id, login, role, password, enabled FROM users WHERE role = ? ORDER BY %s %s", sortField, direction);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    User user = mapResultSetToUser(resultSet);
                    users.add(user);
                }
            }
            logger.debug("Найдено {} пользователей с ролью {} с сортировкой", users.size(), role);

        } catch (SQLException e) {
            logger.error("Ошибка при поиске пользователей по роли {} с сортировкой", role, e);
        }
        return users;
    }
}