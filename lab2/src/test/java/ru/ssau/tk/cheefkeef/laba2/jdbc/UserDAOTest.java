package ru.ssau.tk.cheefkeef.laba2.jdbc;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import ru.ssau.tk.cheefkeef.laba2.models.UserRole;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOTest.class);
    private static UserDAO userDAO;
    private static Faker faker;
    private static User testUser;
    private static String originalLogin;

    @BeforeAll
    static void setUp() {
        logger.info("Инициализация тестового окружения UserDAO");
        userDAO = new UserDAO();
        faker = new Faker(new Locale("ru-RU"));

        // Создаем тестового пользователя для всех тестов
        originalLogin = generateUniqueLogin();
        testUser = new User(
                originalLogin,
                UserRole.USER,
                faker.internet().password(8, 16, true, true, true)
        );

        logger.debug("Создан тестовый пользователь: {}", testUser);
    }

    @AfterAll
    static void tearDown() {
        logger.info("Завершение тестов UserDAO");
    }

    private static String generateUniqueLogin() {
        return "testuser_" +
                faker.name().username().replace(".", "_") +
                "_" +
                System.currentTimeMillis();
    }

    @Test
    @Order(1)
    void testInsertUser() {
        logger.info("Запуск теста: создание пользователя");

        User insertedUser = userDAO.insert(testUser);

        assertNotNull(insertedUser, "Созданный пользователь не должен быть null");
        assertNotNull(insertedUser.getId(), "ID созданного пользователя не должен быть null");
        assertEquals(testUser.getLogin(), insertedUser.getLogin(), "Логины должны совпадать");
        assertEquals(testUser.getRole(), insertedUser.getRole(), "Роли должны совпадать");
        assertEquals(testUser.getPassword(), insertedUser.getPassword(), "Пароли должны совпадать");

        testUser.setId(insertedUser.getId());
        logger.info("Успешно создан пользователь с ID: {}", insertedUser.getId());
    }

    @Test
    @Order(2)
    void testFindById() {
        logger.info("Запуск теста: поиск пользователя по ID");

        User foundUser = userDAO.findById(testUser.getId());

        assertNotNull(foundUser, "Пользователь должен быть найден по ID");
        assertEquals(testUser.getId(), foundUser.getId(), "ID должны совпадать");
        assertEquals(testUser.getLogin(), foundUser.getLogin(), "Логины должны совпадать");
        assertEquals(testUser.getRole(), foundUser.getRole(), "Роли должны совпадать");
        assertEquals(testUser.getPassword(), foundUser.getPassword(), "Пароли должны совпадать");

        logger.debug("Найден пользователь: {}", foundUser);
    }

    @Test
    @Order(3)
    void testFindByLogin() {
        logger.info("Запуск теста: поиск пользователя по логину");

        User foundUser = userDAO.findByLogin(testUser.getLogin());

        assertNotNull(foundUser, "Пользователь должен быть найден по логину");
        assertEquals(testUser.getLogin(), foundUser.getLogin(), "Логины должны совпадать");
        assertEquals(testUser.getId(), foundUser.getId(), "ID должны совпадать");

        logger.debug("Найден пользователь по логину {}: {}", testUser.getLogin(), foundUser);
    }

    @Test
    @Order(4)
    void testFindAll() {
        logger.info("Запуск теста: получение всех пользователей");

        List<User> users = userDAO.findAll();

        assertNotNull(users, "Список пользователей не должен быть null");
        assertFalse(users.isEmpty(), "Список пользователей не должен быть пустым");

        // Проверяем, что наш тестовый пользователь есть в списке
        boolean found = users.stream()
                .anyMatch(user -> user.getId().equals(testUser.getId()));
        assertTrue(found, "Тестовый пользователь должен присутствовать в списке");

        logger.info("Найдено {} пользователей", users.size());
    }

    @Test
    @Order(5)
    void testFindByRole() {
        logger.info("Запуск теста: поиск пользователей по роли");

        List<User> users = userDAO.findByRole(UserRole.USER);

        assertNotNull(users, "Список пользователей по роли не должен быть null");

        // Проверяем, что наш тестовый пользователь есть в списке
        boolean found = users.stream()
                .anyMatch(user -> user.getId().equals(testUser.getId()));
        assertTrue(found, "Тестовый пользователь должен присутствовать в списке пользователей с ролью USER");

        logger.info("Найдено {} пользователей с ролью USER", users.size());
    }

    @Test
    @Order(6)
    void testUpdateUser() {
        logger.info("Запуск теста: обновление пользователя");

        // Генерируем новые данные
        String newLogin = generateUniqueLogin();
        String newPassword = faker.internet().password(12, 20, true, true, true);

        User userToUpdate = new User(
                newLogin,
                UserRole.ADMIN,
                newPassword
        );
        userToUpdate.setId(testUser.getId());

        boolean updateResult = userDAO.update(userToUpdate);

        assertTrue(updateResult, "Обновление пользователя должно быть успешным");

        // Проверяем, что данные обновились
        User updatedUser = userDAO.findById(testUser.getId());
        assertNotNull(updatedUser, "Обновленный пользователь должен существовать");
        assertEquals(newLogin, updatedUser.getLogin(), "Логин должен быть обновлен");
        assertEquals(UserRole.ADMIN, updatedUser.getRole(), "Роль должна быть обновлена");
        assertEquals(newPassword, updatedUser.getPassword(), "Пароль должен быть обновлен");

        testUser = updatedUser; // Обновляем ссылку на тестового пользователя
        logger.info("Пользователь успешно обновлен: {}", updatedUser);
    }

    @Test
    @Order(7)
    void testInsertMultipleUsersWithFaker() {
        logger.info("Запуск теста: создание нескольких пользователей с Faker");

        int numberOfUsers = 5;
        for (int i = 0; i < numberOfUsers; i++) {
            User randomUser = createRandomUser();
            User insertedUser = userDAO.insert(randomUser);

            assertNotNull(insertedUser, "Созданный пользователь не должен быть null");
            assertNotNull(insertedUser.getId(), "ID созданного пользователя не должен быть null");

            logger.debug("Создан случайный пользователь #{}/{}: {}", i + 1, numberOfUsers, insertedUser);
        }

        logger.info("Успешно создано {} случайных пользователей", numberOfUsers);
    }

    @Test
    @Order(8)
    void testFindByRoleAfterMultipleInserts() {
        logger.info("Запуск теста: поиск по ролям после создания нескольких пользователей");

        List<User> adminUsers = userDAO.findByRole(UserRole.ADMIN);
        List<User> userUsers = userDAO.findByRole(UserRole.USER);

        assertNotNull(adminUsers, "Список администраторов не должен быть null");
        assertNotNull(userUsers, "Список пользователей не должен быть null");

        logger.info("Найдено {} администраторов и {} обычных пользователей",
                adminUsers.size(), userUsers.size());

        // Логируем первых нескольких пользователей каждой роли для отладки
        if (!adminUsers.isEmpty()) {
            adminUsers.stream().limit(3).forEach(user ->
                    logger.debug("Администратор: {}", user));
        }
        if (!userUsers.isEmpty()) {
            userUsers.stream().limit(3).forEach(user ->
                    logger.debug("Обычный пользователь: {}", user));
        }
    }

    @Test
    @Order(9)
    void testDeleteByLogin() {
        logger.info("Запуск теста: удаление пользователя по логину");

        // Создаем временного пользователя для удаления
        User tempUser = createRandomUser();
        User insertedTempUser = userDAO.insert(tempUser);
        assertNotNull(insertedTempUser, "Временный пользователь должен быть создан");

        boolean deleteResult = userDAO.deleteByLogin(insertedTempUser.getLogin());
        assertTrue(deleteResult, "Удаление по логину должно быть успешным");

        // Проверяем, что пользователь действительно удален
        User deletedUser = userDAO.findByLogin(insertedTempUser.getLogin());
        assertNull(deletedUser, "Удаленный пользователь не должен быть найден");

        logger.info("Пользователь с логином {} успешно удален", insertedTempUser.getLogin());
    }

    @Test
    @Order(10)
    void testDeleteById() {
        logger.info("Запуск теста: удаление основного тестового пользователя по ID");

        boolean deleteResult = userDAO.delete(testUser.getId());
        assertTrue(deleteResult, "Удаление по ID должно быть успешным");

        // Проверяем, что пользователь действительно удален
        User deletedUser = userDAO.findById(testUser.getId());
        assertNull(deletedUser, "Удаленный пользователь не должен быть найден");

        logger.info("Основной тестовый пользователь с ID {} успешно удален", testUser.getId());
    }

    @Test
    @Order(11)
    void testEdgeCases() {
        logger.info("Запуск теста: проверка граничных случаев");

        // Поиск несуществующего пользователя
        User nonExistentUser = userDAO.findById(-1);
        assertNull(nonExistentUser, "Несуществующий пользователь должен возвращать null");

        // Поиск по несуществующему логину
        User nonExistentLogin = userDAO.findByLogin("non_existent_login_12345");
        assertNull(nonExistentLogin, "Несуществующий логин должен возвращать null");

        // Удаление несуществующего пользователя
        boolean deleteNonExistent = userDAO.delete(-1);
        assertFalse(deleteNonExistent, "Удаление несуществующего пользователя должно возвращать false");

        logger.info("Все граничные случаи обработаны корректно");
    }

    @Test
    @Order(12)
    void testUserWithSpecialCharacters() {
        logger.info("Запуск теста: создание пользователя со специальными символами");

        User specialUser = new User(
                "user_with_special_chars_测试_тест_" + System.currentTimeMillis(),
                UserRole.USER,
                "password123!@#$%"
        );

        User insertedUser = userDAO.insert(specialUser);
        assertNotNull(insertedUser, "Пользователь со специальными символами должен быть создан");

        // Очистка
        if (insertedUser != null && insertedUser.getId() != null) {
            userDAO.delete(insertedUser.getId());
        }

        logger.info("Тест со специальными символами завершен успешно");
    }

    private User createRandomUser() {
        String login = generateUniqueLogin();
        UserRole role = faker.bool().bool() ? UserRole.ADMIN : UserRole.USER;
        String password = faker.internet().password(8, 16, true, true, true);

        // Иногда добавляем специальные символы в пароль для разнообразия
        if (faker.bool().bool()) {
            password += "!@#$%";
        }

        return new User(login, role, password);
    }

    @Test
    @Order(13)
    void testPerformanceFindAll() {
        logger.info("Запуск теста производительности: поиск всех пользователей");

        long startTime = System.currentTimeMillis();
        List<User> users = userDAO.findAll();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        logger.info("Поиск всех пользователей выполнен за {} мс. Найдено {} записей", duration, users.size());

        assertTrue(duration < 5000, "Поиск всех пользователей должен выполняться менее чем за 5 секунд");
    }
}