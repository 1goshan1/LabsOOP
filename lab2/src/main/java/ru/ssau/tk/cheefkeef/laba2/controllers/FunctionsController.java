// FunctionsController.java
package ru.ssau.tk.cheefkeef.laba2.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.cheefkeef.laba2.dto.CreateFunctionRequest;
import ru.ssau.tk.cheefkeef.laba2.dto.UpdateFunctionRequest;
import ru.ssau.tk.cheefkeef.laba2.dto.functions.*;
import ru.ssau.tk.cheefkeef.laba2.dto.user.*;
import ru.ssau.tk.cheefkeef.laba2.dto.points.*;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import ru.ssau.tk.cheefkeef.laba2.services.FunctionsService;
import ru.ssau.tk.cheefkeef.laba2.services.SecurityService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionsController {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsController.class);

    @Autowired
    private FunctionsService functionsService;

    @Autowired
    private SecurityService securityService;

    // GET /functions - Получить список всех функций (только свои для USER, все для ADMIN)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFunctions(
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending,
            Authentication authentication) {

        logger.info("Запрос на получение функций пользователем: {}. Сортировка по: {}",
                authentication.getName(), sortField);

        try {
            List<Functions> functions;

            // ADMIN видит все функции, USER - только свои
            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                logger.debug("Пользователь {} (ADMIN) получает все функции", authentication.getName());
                functions = functionsService.findAll();
            } else {
                Long currentUserId = securityService.getCurrentUserId();
                logger.debug("Пользователь {} получает только свои функции", authentication.getName());
                functions = functionsService.findByUserId(currentUserId);
            }

            // Применяем сортировку
            functions = sortFunctions(functions, sortField, ascending);

            logger.info("Успешно возвращено {} функций для пользователя {}",
                    functions.size(), authentication.getName());
            return ResponseEntity.ok(functions);

        } catch (Exception e) {
            logger.error("Ошибка при получении списка функций: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions"));
        }
    }

    // POST /functions - Создать новую функцию для текущего пользователя
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createFunction(@Valid @RequestBody CreateFunctionRequest request,
                                            Authentication authentication) {
        String username = authentication.getName();
        Long currentUserId = securityService.getCurrentUserId();

        logger.info("Запрос на создание функции пользователем: {}. Имя: {}",
                username, request.getName());

        try {
            // Проверяем, существует ли функция с таким именем у пользователя
            if (functionsService.existsByNameAndUserId(request.getName(), currentUserId)) {
                logger.warn("Попытка создания функции с существующим именем: {} для пользователя {}",
                        request.getName(), username);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Функция с таким именем уже существует",
                                "/api/v1/functions"));
            }

            Functions function = new Functions();
            function.setUserId(currentUserId); // Устанавливаем ID текущего пользователя
            function.setName(request.getName());
            function.setSignature(request.getSignature());

            Functions savedFunction = functionsService.save(function);
            logger.info("Функция успешно создана с ID: {} для пользователя {}",
                    savedFunction.getId(), username);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedFunction);

        } catch (Exception e) {
            logger.error("Ошибка при создании функции пользователем {}: {}",
                    username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions"));
        }
    }

    // GET /functions/search - Расширенный поиск функций
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> searchFunctions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String namePattern,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending,
            Authentication authentication) {

        logger.info("Расширенный поиск функций пользователем: {}. UserId: {}, NamePattern: {}",
                authentication.getName(), userId, namePattern);

        try {
            List<Functions> functions;
            Long currentUserId = securityService.getCurrentUserId();

            // ADMIN может искать по любому userId, USER - только по своему
            if (userId != null && !authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (!userId.equals(currentUserId)) {
                    logger.warn("Пользователь {} пытается получить функции другого пользователя {}",
                            authentication.getName(), userId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/search"));
                }
            }

            if (userId != null) {
                functions = functionsService.findByUserIdAndNamePattern(userId, namePattern);
            } else if (namePattern != null && !namePattern.trim().isEmpty()) {
                // Если userId не указан, USER видит только свои функции
                if (authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    functions = functionsService.findByNameContaining(namePattern);
                } else {
                    functions = functionsService.findByUserIdAndNamePattern(currentUserId, namePattern);
                }
            } else {
                // Если параметры не указаны, USER видит только свои функции
                if (authentication.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    functions = functionsService.findAll();
                } else {
                    functions = functionsService.findByUserId(currentUserId);
                }
            }

            // Применяем сортировку
            functions = sortFunctions(functions, sortField, ascending);

            logger.info("Найдено {} функций по критериям поиска для пользователя {}",
                    functions.size(), authentication.getName());
            return ResponseEntity.ok(functions);

        } catch (Exception e) {
            logger.error("Ошибка при расширенном поиске функций: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions/search"));
        }
    }

    // GET /functions/{id} - Получить функцию по ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFunctionById(@PathVariable Long id, Authentication authentication) {
        logger.info("Запрос на получение функции по ID: {} пользователем: {}",
                id, authentication.getName());

        try {
            Optional<Functions> function = functionsService.findById(id);

            if (function.isPresent()) {
                // Проверяем права доступа
                if (!securityService.canAccessFunction(id, authentication)) {
                    logger.warn("Пользователь {} пытается получить доступ к чужой функции {}",
                            authentication.getName(), id);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/" + id));
                }

                logger.info("Функция с ID {} найдена для пользователя {}", id, authentication.getName());
                return ResponseEntity.ok(function.get());
            } else {
                logger.warn("Функция с ID {} не найдена", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Функция не найдена", "/api/v1/functions/" + id));
            }

        } catch (Exception e) {
            logger.error("Ошибка при поиске функции по ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions/" + id));
        }
    }

    // PUT /functions/{id} - Обновить функцию
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateFunction(@PathVariable Long id,
                                            @Valid @RequestBody UpdateFunctionRequest request,
                                            Authentication authentication) {
        logger.info("Запрос на обновление функции с ID: {} пользователем: {}",
                id, authentication.getName());

        try {
            Optional<Functions> existingFunction = functionsService.findById(id);

            if (existingFunction.isEmpty()) {
                logger.warn("Функция с ID {} не найдена для обновления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Функция не найдена", "/api/v1/functions/" + id));
            }

            // Проверяем права доступа
            if (!securityService.canAccessFunction(id, authentication)) {
                logger.warn("Пользователь {} пытается обновить чужую функцию {}",
                        authentication.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/" + id));
            }

            Functions function = existingFunction.get();
            Long currentUserId = securityService.getCurrentUserId();

            // Проверяем, не занято ли новое имя другой функцией у этого пользователя
            if (!function.getName().equals(request.getName()) &&
                    functionsService.existsByNameAndUserId(request.getName(), currentUserId)) {
                logger.warn("Попытка изменения имени на существующее: {} для пользователя {}",
                        request.getName(), authentication.getName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Функция с таким именем уже существует",
                                "/api/v1/functions/" + id));
            }

            function.setName(request.getName());
            function.setSignature(request.getSignature());

            Functions updatedFunction = functionsService.save(function);
            logger.info("Функция с ID {} успешно обновлена пользователем {}",
                    id, authentication.getName());

            return ResponseEntity.ok(updatedFunction);

        } catch (Exception e) {
            logger.error("Ошибка при обновлении функции с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions/" + id));
        }
    }

    // DELETE /functions/{id} - Удалить функцию по ID
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFunctionById(@PathVariable Long id, Authentication authentication) {
        logger.info("Запрос на удаление функции по ID: {} пользователем: {}",
                id, authentication.getName());

        try {
            Optional<Functions> function = functionsService.findById(id);

            if (function.isEmpty()) {
                logger.warn("Функция с ID {} не найдена для удаления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Функция не найдена", "/api/v1/functions/" + id));
            }

            // Проверяем права доступа
            if (!securityService.canAccessFunction(id, authentication)) {
                logger.warn("Пользователь {} пытается удалить чужую функцию {}",
                        authentication.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/" + id));
            }

            functionsService.deleteById(id);
            logger.info("Функция с ID {} успешно удалена пользователем {}", id, authentication.getName());

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении функции с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions/" + id));
        }
    }

    // GET /functions/search/by-user/{userId} - Найти функции по ID пользователя
    @GetMapping("/search/by-user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFunctionsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending,
            Authentication authentication) {

        logger.info("Запрос на поиск функций по ID пользователя: {} пользователем: {}",
                userId, authentication.getName());

        try {
            // Проверяем права доступа
            Long currentUserId = securityService.getCurrentUserId();
            if (!userId.equals(currentUserId) && !authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                logger.warn("Пользователь {} пытается получить функции другого пользователя {}",
                        authentication.getName(), userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/search/by-user/" + userId));
            }

            List<Functions> functions = functionsService.findByUserId(userId);

            // Применяем сортировку
            functions = sortFunctions(functions, sortField, ascending);

            logger.info("Найдено {} функций для пользователя с ID {}", functions.size(), userId);
            return ResponseEntity.ok(functions);

        } catch (Exception e) {
            logger.error("Ошибка при поиске функций по ID пользователя {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/functions/search/by-user/" + userId));
        }
    }

    // Остальные методы остаются аналогичными с добавлением проверок прав доступа...

    // Вспомогательный метод для сортировки
    private List<Functions> sortFunctions(List<Functions> functions, String sortField, boolean ascending) {
        return functions.stream()
                .sorted((f1, f2) -> {
                    int result = 0;
                    switch (sortField) {
                        case "id":
                            result = f1.getId().compareTo(f2.getId());
                            break;
                        case "u_id":
                            result = f1.getUserId().compareTo(f2.getUserId());
                            break;
                        case "name":
                            result = f1.getName().compareTo(f2.getName());
                            break;
                        case "signature":
                            result = f1.getSignature().compareTo(f2.getSignature());
                            break;
                        default:
                            result = f1.getId().compareTo(f2.getId());
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

        logger.warn("Ошибка валидации функций: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, ex.getBindingResult().getObjectName()));
    }
}