// AuthController.java
package ru.ssau.tk.cheefkeef.laba2.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.cheefkeef.laba2.dto.user.*;
import ru.ssau.tk.cheefkeef.laba2.entities.User;
import ru.ssau.tk.cheefkeef.laba2.services.UserService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Регистрация нового пользователя
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        logger.info("Запрос на регистрацию пользователя: {}", request.getLogin());

        try {
            // Проверяем, существует ли пользователь
            if (userService.existsByLogin(request.getLogin())) {
                logger.warn("Попытка регистрации с существующим логином: {}", request.getLogin());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким логином уже существует", "/api/v1/auth/register"));
            }

            // Проверяем роль
            String role = request.getRole();
            if (!isValidRole(role)) {
                logger.warn("Недопустимая роль при регистрации: {}", role);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Недопустимая роль", "/api/v1/auth/register"));
            }

            // Создаем пользователя (пароль без шифрования)
            User user = new User();
            user.setLogin(request.getLogin());
            user.setPassword(request.getPassword()); // Пароль в чистом виде
            user.setRole(role);
            user.setEnabled(true);

            User savedUser = userService.save(user);
            logger.info("Пользователь успешно зарегистрирован: {}, роль: {}", savedUser.getLogin(), savedUser.getRole());

            AuthResponse response = new AuthResponse(
                    savedUser.getLogin(),
                    savedUser.getRole(),
                    "Пользователь успешно зарегистрирован"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Ошибка при регистрации пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/auth/register"));
        }
    }

    // Получение информации о текущем пользователе
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос информации о текущем пользователе: {}", username);

        try {
            User user = userService.findByLogin(username);
            if (user == null) {
                logger.warn("Пользователь {} не найден", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/auth/me"));
            }

            // Создаем DTO без пароля
            UserInfoResponse userInfo = new UserInfoResponse(
                    user.getId(),
                    user.getLogin(),
                    user.getRole()
            );

            logger.debug("Информация о пользователе {} возвращена", username);
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            logger.error("Ошибка при получении информации о пользователе: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/auth/me"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на смену пароля для пользователя: {}", username);

        try {
            User user = userService.findByLogin(username);
            if (user == null) {
                logger.warn("Пользователь {} не найден при смене пароля", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Пользователь не найден", "/api/v1/auth/change-password"));
            }

            // Проверяем старый пароль (простое сравнение)
            if (!user.getPassword().equals(request.getOldPassword())) {
                logger.warn("Неверный старый пароль для пользователя: {}", username);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Неверный старый пароль", "/api/v1/auth/change-password"));
            }

            // Устанавливаем новый пароль (без шифрования)
            user.setPassword(request.getNewPassword());
            userService.save(user);

            logger.info("Пароль успешно изменен для пользователя: {}", username);

            AuthResponse response = new AuthResponse(
                    user.getLogin(),
                    user.getRole(),
                    "Пароль успешно изменен"
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при смене пароля: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/auth/change-password"));
        }
    }
    // Административный endpoint для создания пользователей с любыми ролями
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserByAdmin(@Valid @RequestBody RegisterRequest request) {
        logger.info("Административное создание пользователя: {}, роль: {}",
                request.getLogin(), request.getRole());

        try {
            if (userService.existsByLogin(request.getLogin())) {
                logger.warn("Попытка административного создания пользователя с существующим логином: {}",
                        request.getLogin());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Пользователь с таким логином уже существует",
                                "/api/v1/auth/admin/create-user"));
            }

            User user = new User();
            user.setLogin(request.getLogin());
            user.setPassword(request.getPassword()); // Пароль в чистом виде
            user.setRole(request.getRole());
            user.setEnabled(true);

            User savedUser = userService.save(user);
            logger.info("Пользователь создан администратором: {}, роль: {}",
                    savedUser.getLogin(), savedUser.getRole());

            AuthResponse response = new AuthResponse(
                    savedUser.getLogin(),
                    savedUser.getRole(),
                    "Пользователь успешно создан"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Ошибка при административном создании пользователя: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/auth/admin/create-user"));
        }
    }
    private boolean isValidRole(String role) {
        return role != null && (role.equals("USER") || role.equals("MANAGER") || role.equals("ADMIN"));
    }
}