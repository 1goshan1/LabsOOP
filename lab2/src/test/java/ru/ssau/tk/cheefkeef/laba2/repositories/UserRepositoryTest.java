package ru.ssau.tk.cheefkeef.laba2.repositories;

import ru.ssau.tk.cheefkeef.laba2.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        userRepository.deleteAll();

        testUser = new User();
        testUser.setLogin("testuser");
        testUser.setPassword("password123");
        testUser.setRole("user");
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setLogin("adminuser");
        adminUser.setPassword("adminpass");
        adminUser.setRole("admin");
        adminUser = userRepository.save(adminUser);
    }

    @Test
    void testPerformanceSelect() {
        // Подготовка: создаем 10к пользователей
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setLogin("user_" + i);
            user.setPassword("pass_" + i);
            user.setRole("role_" + (i % 5));
            userRepository.save(user);
        }

        // Замер скорости SELECT
        long startTime = System.currentTimeMillis();

        List<User> users = userRepository.findAll();
        User user = userRepository.findByLogin("user_5000");
        List<User> roleUsers = userRepository.findByRole("role_1");
        boolean exists = userRepository.existsByLogin("user_9999");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("UserRepository SELECT operations (10k records): " + duration + " ms");
        assertTrue(duration < 1000, "SELECT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceInsert() {
        // Замер скорости INSERT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setLogin("perf_user_" + i);
            user.setPassword("perf_pass_" + i);
            user.setRole("perf_role");
            userRepository.save(user);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("UserRepository INSERT 10k records: " + duration + " ms");
        assertTrue(duration < 5000, "INSERT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceUpdate() {
        // Подготовка: создаем 10к пользователей
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setLogin("update_user_" + i);
            user.setPassword("update_pass_" + i);
            user.setRole("update_role");
            userRepository.save(user);
        }

        // Замер скорости UPDATE
        long startTime = System.currentTimeMillis();

        List<User> users = userRepository.findByLoginContainingIgnoreCase("update_user");
        for (User user : users) {
            user.setPassword("updated_password");
            userRepository.save(user);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("UserRepository UPDATE 10k records: " + duration + " ms");
        assertTrue(duration < 3000, "UPDATE operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceDelete() {
        // Подготовка: создаем 10к пользователей
        for (int i = 0; i < 10000; i++) {
            User user = new User();
            user.setLogin("delete_user_" + i);
            user.setPassword("delete_pass_" + i);
            user.setRole("delete_role");
            userRepository.save(user);
        }

        // Замер скорости DELETE
        long startTime = System.currentTimeMillis();

        userRepository.deleteAll();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("UserRepository DELETE 10k records: " + duration + " ms");
        assertTrue(duration < 2000, "DELETE operations took too long: " + duration + " ms");
    }

    @Test
    void testSelectUser() {
        User user = userRepository.findByLogin("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.getLogin());
        assertEquals("password123", user.getPassword());
        assertEquals("user", user.getRole());
    }

    @Test
    void testSelectNonExistentUser() {
        User user = userRepository.findByLogin("nonexistent");
        assertNull(user);
    }

    @Test
    void testExistsByLogin() {
        assertTrue(userRepository.existsByLogin("testuser"));
        assertTrue(userRepository.existsByLogin("adminuser"));
        assertFalse(userRepository.existsByLogin("nonexistent"));
    }

    @Test
    void testFindByRole() {
        List<User> users = userRepository.findByRole("user");
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getLogin());

        List<User> admins = userRepository.findByRole("admin");
        assertEquals(1, admins.size());
        assertEquals("adminuser", admins.get(0).getLogin());

        List<User> emptyList = userRepository.findByRole("moderator");
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void testFindByLoginContainingIgnoreCase() {
        List<User> users = userRepository.findByLoginContainingIgnoreCase("test");
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("testuser", users.get(0).getLogin());

        List<User> userIgnoreCase = userRepository.findByLoginContainingIgnoreCase("TEST");
        assertEquals(1, userIgnoreCase.size());
        assertEquals("testuser", userIgnoreCase.get(0).getLogin());

        List<User> multipleUsers = userRepository.findByLoginContainingIgnoreCase("user");
        assertEquals(2, multipleUsers.size());
    }

    @Test
    void testFindByLoginAndRole() {
        Optional<User> user = userRepository.findByLoginAndRole("testuser", "user");
        assertTrue(user.isPresent());
        assertEquals("testuser", user.get().getLogin());
        assertEquals("user", user.get().getRole());

        Optional<User> wrongRole = userRepository.findByLoginAndRole("testuser", "admin");
        assertFalse(wrongRole.isPresent());

        Optional<User> nonExistent = userRepository.findByLoginAndRole("nonexistent", "user");
        assertFalse(nonExistent.isPresent());
    }

    @Test
    void testCountByRole() {
        long userCount = userRepository.countByRole("user");
        assertEquals(1, userCount);

        long adminCount = userRepository.countByRole("admin");
        assertEquals(1, adminCount);

        long moderatorCount = userRepository.countByRole("moderator");
        assertEquals(0, moderatorCount);
    }

    @Test
    void testDeleteByLogin() {
        assertEquals(2, userRepository.count());

        userRepository.deleteByLogin("testuser");

        assertEquals(1, userRepository.count());
        assertFalse(userRepository.existsByLogin("testuser"));
        assertTrue(userRepository.existsByLogin("adminuser"));
    }

    @Test
    void testDeleteNonExistentUser() {
        assertEquals(2, userRepository.count());

        // Не должно выбрасывать исключение при удалении несуществующего пользователя
        assertDoesNotThrow(() -> userRepository.deleteByLogin("nonexistent"));

        assertEquals(2, userRepository.count());
    }

    @Test
    void testSaveAndFindById() {
        User newUser = new User();
        newUser.setLogin("newuser");
        newUser.setPassword("newpass");
        newUser.setRole("moderator");

        User savedUser = userRepository.save(newUser);
        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("newuser", foundUser.get().getLogin());
        assertEquals("moderator", foundUser.get().getRole());
    }

    @Test
    void testFindAll() {
        List<User> allUsers = userRepository.findAll();
        assertNotNull(allUsers);
        assertEquals(2, allUsers.size());
    }

    @Test
    void testUpdateUser() {
        User user = userRepository.findByLogin("testuser");
        assertNotNull(user);

        user.setPassword("updatedPassword");
        user.setRole("moderator");
        User updatedUser = userRepository.save(user);

        assertEquals("updatedPassword", updatedUser.getPassword());
        assertEquals("moderator", updatedUser.getRole());

        // Проверяем, что изменения сохранились в базе
        User retrievedUser = userRepository.findByLogin("testuser");
        assertEquals("updatedPassword", retrievedUser.getPassword());
        assertEquals("moderator", retrievedUser.getRole());
    }

    @Test
    void testComplexScenario() {
        // Создаем дополнительного пользователя
        User extraUser = new User();
        extraUser.setLogin("extrauser");
        extraUser.setPassword("extrapass");
        extraUser.setRole("user");
        userRepository.save(extraUser);

        // Проверяем количество пользователей с ролью "user"
        long userCount = userRepository.countByRole("user");
        assertEquals(2, userCount);

        // Проверяем поиск по частичному совпадению
        List<User> usersWithUser = userRepository.findByLoginContainingIgnoreCase("user");
        assertEquals(3, usersWithUser.size());

        // Удаляем одного пользователя
        userRepository.deleteByLogin("extrauser");

        // Проверяем итоговое состояние
        assertFalse(userRepository.existsByLogin("extrauser"));
        assertEquals(2, userRepository.count());
    }
}