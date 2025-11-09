// UserController.java
package ru.ssau.tk.cheefkeef.laba2.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.cheefkeef.laba2.dto.user.*;
import ru.ssau.tk.cheefkeef.laba2.entities.User;
import ru.ssau.tk.cheefkeef.laba2.services.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // GET /users - Получить список всех пользователей
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending) {

        logger.info("Запрос на получение всех пользователей. Сортировка по: {}, порядок: {}",
                sortField, ascending ? "возрастание" : "убывание");

        try {
            List<User> users = userService.findAll();

            // Применяем сортировку
            users = sortUsers(users, sortField, ascending);

            logger.info("Успешно возвращено {} пользователей", users.size());
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users"));
        }
    }

    // POST /users - Создать нового пользователя
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        logger.info("Запрос на создание пользователя с логином: {}", request.getLogin());

        try {
            // Проверяем, существует ли пользователь с таким логином
            if (userService.existsByLogin(request.getLogin())) {
                logger.warn("Попытка создания пользователя с существующим логином: {}", request.getLogin());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким логином уже существует", "/api/v1/users"));
            }

            User user = new User();
            user.setLogin(request.getLogin());
            user.setRole(request.getRole());
            user.setPassword(request.getPassword()); // В реальном приложении пароль должен быть зашифрован

            User savedUser = userService.save(user);
            logger.info("Пользователь успешно создан с ID: {}", savedUser.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);

        } catch (Exception e) {
            logger.error("Ошибка при создании пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users"));
        }
    }

    // GET /users/{id} - Получить пользователя по ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or @securityService.canAccessUser(#id, authentication)")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        logger.info("Запрос на получение пользователя по ID: {}", id);

        try {
            Optional<User> user = userService.findById(id);

            if (user.isPresent()) {
                logger.info("Пользователь с ID {} найден", id);
                return ResponseEntity.ok(user.get());
            } else {
                logger.warn("Пользователь с ID {} не найден", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/users/" + id));
            }

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/" + id));
        }
    }

    // PUT /users/{id} - Обновить пользователя
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        logger.info("Запрос на обновление пользователя с ID: {}", id);

        try {
            Optional<User> existingUser = userService.findById(id);

            if (existingUser.isEmpty()) {
                logger.warn("Пользователь с ID {} не найден для обновления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/users/" + id));
            }

            User user = existingUser.get();

            // Проверяем, не занят ли новый логин другим пользователем
            if (!user.getLogin().equals(request.getLogin()) &&
                    userService.existsByLogin(request.getLogin())) {
                logger.warn("Попытка изменения логина на существующий: {}", request.getLogin());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким логином уже существует", "/api/v1/users/" + id));
            }

            user.setLogin(request.getLogin());
            user.setRole(request.getRole());
            user.setPassword(request.getPassword()); // В реальном приложении пароль должен быть зашифрован

            User updatedUser = userService.save(user);
            logger.info("Пользователь с ID {} успешно обновлен", id);

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            logger.error("Ошибка при обновлении пользователя с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/" + id));
        }
    }

    // DELETE /users/{id} - Удалить пользователя по ID
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserById(@PathVariable Long id) {
        logger.info("Запрос на удаление пользователя по ID: {}", id);

        try {
            if (!userService.findById(id).isPresent()) {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/users/" + id));
            }

            userService.deleteById(id);
            logger.info("Пользователь с ID {} успешно удален", id);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/" + id));
        }
    }

    // GET /users/search/by-login/{login} - Найти пользователя по логину
    @GetMapping("/search/by-login/{login}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserByLogin(@PathVariable String login) {
        logger.info("Запрос на поиск пользователя по логину: {}", login);

        try {
            User user = userService.findByLogin(login);

            if (user != null) {
                logger.info("Пользователь с логином {} найден", login);
                return ResponseEntity.ok(user);
            } else {
                logger.warn("Пользователь с логином {} не найден", login);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/users/search/by-login/" + login));
            }

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по логину {}: {}", login, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/search/by-login/" + login));
        }
    }

    // DELETE /users/search/by-login/{login} - Удалить пользователя по логину
    @DeleteMapping("/search/by-login/{login}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserByLogin(@PathVariable String login) {
        logger.info("Запрос на удаление пользователя по логину: {}", login);

        try {
            User user = userService.findByLogin(login);

            if (user == null) {
                logger.warn("Пользователь с логином {} не найден для удаления", login);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/users/search/by-login/" + login));
            }

            userService.deleteByLogin(login);
            logger.info("Пользователь с логином {} успешно удален", login);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении пользователя с логином {}: {}", login, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/search/by-login/" + login));
        }
    }

    // GET /users/search/by-role/{role} - Найти пользователей по роли
    @GetMapping("/search/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending) {

        logger.info("Запрос на поиск пользователей по роли: {}. Сортировка по: {}, порядок: {}",
                role, sortField, ascending ? "возрастание" : "убывание");

        try {
            List<User> users = userService.findByRole(role);

            // Применяем сортировку
            users = sortUsers(users, sortField, ascending);

            logger.info("Найдено {} пользователей с ролью {}", users.size(), role);
            return ResponseEntity.ok(users);

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователей по роли {}: {}", role, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/search/by-role/" + role));
        }
    }

    // POST /users/search/by-ids - Множественный поиск пользователей по IDs
    @PostMapping("/search/by-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByIds(@Valid @RequestBody UserIdsRequest request) {
        logger.info("Запрос на поиск пользователей по IDs: {}", request.getIds());

        try {
            List<User> users = userService.findByIds(request.getIds());
            logger.info("Найдено {} пользователей из запрошенных {}", users.size(), request.getIds().size());

            return ResponseEntity.ok(users);

        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователей по IDs {}: {}", request.getIds(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/users/search/by-ids"));
        }
    }

    // Вспомогательный метод для сортировки
    private List<User> sortUsers(List<User> users, String sortField, boolean ascending) {
        return users.stream()
                .sorted((u1, u2) -> {
                    int result = 0;
                    switch (sortField) {
                        case "id":
                            result = u1.getId().compareTo(u2.getId());
                            break;
                        case "login":
                            result = u1.getLogin().compareTo(u2.getLogin());
                            break;
                        case "role":
                            result = u1.getRole().compareTo(u2.getRole());
                            break;
                        case "password":
                            result = u1.getPassword().compareTo(u2.getPassword());
                            break;
                        default:
                            result = u1.getId().compareTo(u2.getId());
                    }
                    return ascending ? result : -result;
                })
                .collect(Collectors.toList());
    }

    // Глобальный обработчик исключений для валидации
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        logger.warn("Ошибка валидации: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, ex.getBindingResult().getObjectName()));
    }
}