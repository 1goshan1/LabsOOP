package ru.ssau.tk.cheefkeef.laba2.jdbc;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.models.User;

import java.util.List;
import java.util.Locale;

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
                "user",
                faker.internet().password(8, 16, true, true, true),
                true
        );

        logger.debug("Создан тестовый пользователь: {}", testUser);
    }

    @AfterAll
    static void tearDown() {
        logger.info("Завершение тестов UserDAO");

        // Очистка тестовых данных
        if (testUser != null && testUser.getId() != null) {
            try {
                userDAO.delete(testUser.getId());
            } catch (Exception e) {
                logger.warn("Не удалось удалить тестового пользователя: {}", e.getMessage());
            }
        }
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
        assertNotNull(insertedUser.getEnabled(), "Поле enabled не должно быть null");
        assertTrue(insertedUser.getEnabled(), "По умолчанию enabled должен быть true");

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
        assertEquals(testUser.getEnabled(), foundUser.getEnabled(), "Статусы enabled должны совпадать");

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
        assertEquals(testUser.getEnabled(), foundUser.getEnabled(), "Статусы enabled должны совпадать");

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

        List<User> userRoleUsers = userDAO.findByRole("user");

        assertNotNull(userRoleUsers, "Список пользователей с ролью 'user' не должен быть null");

        // Проверяем, что наш тестовый пользователь есть в списке
        boolean found = userRoleUsers.stream()
                .anyMatch(user -> user.getId().equals(testUser.getId()));
        assertTrue(found, "Тестовый пользователь должен присутствовать в списке пользователей с ролью 'user'");

        logger.info("Найдено {} пользователей с ролью 'user'", userRoleUsers.size());
    }

    @Test
    @Order(6)
    void testFindByEnabledStatus() {
        logger.info("Запуск теста: поиск пользователей по статусу enabled");

        List<User> enabledUsers = userDAO.findByEnabledStatus(true);
        List<User> disabledUsers = userDAO.findByEnabledStatus(false);

        assertNotNull(enabledUsers, "Список активных пользователей не должен быть null");
        assertNotNull(disabledUsers, "Список неактивных пользователей не должен быть null");

        // Проверяем, что наш тестовый пользователь есть в списке активных
        boolean foundInEnabled = enabledUsers.stream()
                .anyMatch(user -> user.getId().equals(testUser.getId()));
        assertTrue(foundInEnabled, "Тестовый пользователь должен присутствовать в списке активных пользователей");

        logger.info("Найдено {} активных и {} неактивных пользователей",
                enabledUsers.size(), disabledUsers.size());
    }

    @Test
    @Order(7)
    void testUpdateUser() {
        logger.info("Запуск теста: обновление пользователя");

        // Генерируем новые данные
        String newLogin = generateUniqueLogin();
        String newPassword = faker.internet().password(12, 20, true, true, true);

        User userToUpdate = new User(
                newLogin,
                "admin",
                newPassword,
                false  // Меняем статус на неактивный
        );
        userToUpdate.setId(testUser.getId());

        boolean updateResult = userDAO.update(userToUpdate);

        assertTrue(updateResult, "Обновление пользователя должно быть успешным");

        // Проверяем, что данные обновились
        User updatedUser = userDAO.findById(testUser.getId());
        assertNotNull(updatedUser, "Обновленный пользователь должен существовать");
        assertEquals(newLogin, updatedUser.getLogin(), "Логин должен быть обновлен");
        assertEquals("admin", updatedUser.getRole(), "Роль должна быть обновлена");
        assertEquals(newPassword, updatedUser.getPassword(), "Пароль должен быть обновлен");
        assertFalse(updatedUser.getEnabled(), "Статус enabled должен быть обновлен на false");

        testUser = updatedUser; // Обновляем ссылку на тестового пользователя
        logger.info("Пользователь успешно обновлен: {}", updatedUser);
    }

    @Test
    @Order(8)
    void testUpdateEnabledStatus() {
        logger.info("Запуск теста: обновление только статуса enabled");

        // Меняем статус обратно на активный
        boolean updateResult = userDAO.updateEnabledStatus(testUser.getId(), true);
        assertTrue(updateResult, "Обновление статуса enabled должно быть успешным");

        // Проверяем, что статус обновился
        User updatedUser = userDAO.findById(testUser.getId());
        assertNotNull(updatedUser, "Пользователь должен существовать после обновления");
        assertTrue(updatedUser.getEnabled(), "Статус enabled должен быть true");

        testUser = updatedUser; // Обновляем ссылку
        logger.info("Статус enabled успешно обновлен на true");
    }

    @Test
    @Order(9)
    void testInsertMultipleUsersWithFaker() {
        logger.info("Запуск теста: создание нескольких пользователей с Faker");

        int numberOfUsers = 5;
        for (int i = 0; i < numberOfUsers; i++) {
            User randomUser = createRandomUser();
            User insertedUser = userDAO.insert(randomUser);

            assertNotNull(insertedUser, "Созданный пользователь не должен быть null");
            assertNotNull(insertedUser.getId(), "ID созданного пользователя не должен быть null");
            assertNotNull(insertedUser.getEnabled(), "Поле enabled не должно быть null");

            logger.debug("Создан случайный пользователь #{}/{}: {}", i + 1, numberOfUsers, insertedUser);
        }

        logger.info("Успешно создано {} случайных пользователей", numberOfUsers);
    }

    @Test
    @Order(10)
    void testFindByRoleAfterMultipleInserts() {
        logger.info("Запуск теста: поиск по ролям после создания нескольких пользователей");

        List<User> adminUsers = userDAO.findByRole("admin");
        List<User> userUsers = userDAO.findByRole("user");

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
    @Order(11)
    void testFindByIds() {
        logger.info("Запуск теста: множественный поиск по IDs");

        // Создаем еще одного пользователя для теста
        User anotherUser = createRandomUser();
        User insertedAnotherUser = userDAO.insert(anotherUser);
        assertNotNull(insertedAnotherUser, "Дополнительный пользователь должен быть создан");

        List<Integer> ids = List.of(testUser.getId(), insertedAnotherUser.getId());
        List<User> foundUsers = userDAO.findByIds(ids);

        assertNotNull(foundUsers, "Список найденных пользователей не должен быть null");
        assertEquals(2, foundUsers.size(), "Должны быть найдены оба пользователя");

        // Проверяем, что оба пользователя присутствуют в результате
        boolean foundTestUser = foundUsers.stream()
                .anyMatch(user -> user.getId().equals(testUser.getId()));
        boolean foundAnotherUser = foundUsers.stream()
                .anyMatch(user -> user.getId().equals(insertedAnotherUser.getId()));

        assertTrue(foundTestUser, "Тестовый пользователь должен быть найден");
        assertTrue(foundAnotherUser, "Дополнительный пользователь должен быть найден");

        // Очистка
        userDAO.delete(insertedAnotherUser.getId());
        logger.info("Множественный поиск по IDs выполнен успешно");
    }

    @Test
    @Order(12)
    void testFindAllWithSorting() {
        logger.info("Запуск теста: получение пользователей с сортировкой");

        List<User> usersByIdAsc = userDAO.findAllWithSorting("id", true);
        List<User> usersByIdDesc = userDAO.findAllWithSorting("id", false);
        List<User> usersByLogin = userDAO.findAllWithSorting("login", true);

        assertNotNull(usersByIdAsc, "Список с сортировкой по ID ASC не должен быть null");
        assertNotNull(usersByIdDesc, "Список с сортировкой по ID DESC не должен быть null");
        assertNotNull(usersByLogin, "Список с сортировкой по login не должен быть null");

        // Проверяем, что списки не пустые
        assertFalse(usersByIdAsc.isEmpty(), "Список с сортировкой не должен быть пустым");

        logger.info("Тест сортировки завершен: ASC={}, DESC={}, login={}",
                usersByIdAsc.size(), usersByIdDesc.size(), usersByLogin.size());
    }

    @Test
    @Order(13)
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
    @Order(14)
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
    @Order(15)
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

        // Поиск по пустому списку IDs
        List<User> emptyListUsers = userDAO.findByIds(List.of());
        assertNotNull(emptyListUsers, "Поиск по пустому списку IDs должен возвращать не-null список");
        assertTrue(emptyListUsers.isEmpty(), "Поиск по пустому списку IDs должен возвращать пустой список");

        logger.info("Все граничные случаи обработаны корректно");
    }

    @Test
    @Order(16)
    void testUserWithSpecialCharacters() {
        logger.info("Запуск теста: создание пользователя со специальными символами");

        User specialUser = new User(
                "user_with_special_chars_测试_тест_" + System.currentTimeMillis(),
                "user",
                "password123!@#$%",
                true
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
        String role = faker.bool().bool() ? "admin" : "user";
        String password = faker.internet().password(8, 16, true, true, true);
        Boolean enabled = faker.bool().bool();

        // Иногда добавляем специальные символы в пароль для разнообразия
        if (faker.bool().bool()) {
            password += "!@#$%";
        }

        return new User(login, role, password, enabled);
    }

    @Test
    @Order(18)
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