// PointsController.java
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
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.cheefkeef.laba2.dto.functions.*;
import ru.ssau.tk.cheefkeef.laba2.dto.user.*;
import ru.ssau.tk.cheefkeef.laba2.dto.points.*;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import ru.ssau.tk.cheefkeef.laba2.entities.Points;
import ru.ssau.tk.cheefkeef.laba2.services.FunctionsService;
import ru.ssau.tk.cheefkeef.laba2.services.PointsService;
import ru.ssau.tk.cheefkeef.laba2.services.SecurityService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/points")
public class PointsController {
    private static final Logger logger = LoggerFactory.getLogger(PointsController.class);

    @Autowired
    private PointsService pointsService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FunctionsService functionsService;

    // GET /points - Получить список всех точек (только для своих функций)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllPoints(
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long currentUserId = securityService.getCurrentUserId();

        logger.info("Запрос на получение всех точек пользователем: {}. Сортировка по: {}",
                username, sortField);

        try {
            List<Points> points;

            // ADMIN видит все точки, USER - только точки своих функций
            if (securityService.isAdmin()) {
                logger.debug("Пользователь {} (ADMIN) получает все точки", username);
                points = pointsService.findAll();
            } else {
                logger.debug("Пользователь {} получает только точки своих функций", username);
                // Получаем ID всех функций пользователя
                List<Functions> userFunctions = functionsService.findByUserId(currentUserId);
                List<Long> userFunctionIds = userFunctions.stream()
                        .map(Functions::getId)
                        .collect(Collectors.toList());

                if (userFunctionIds.isEmpty()) {
                    points = List.of();
                } else {
                    // Получаем точки только для функций пользователя
                    points = pointsService.findByFunctionIdIn(userFunctionIds);
                }
            }

            // Применяем сортировку
            points = sortPoints(points, sortField, ascending);

            // Конвертируем в DTO
            List<PointDTO> pointDTOs = convertToDTO(points);

            logger.info("Успешно возвращено {} точек для пользователя {}", pointDTOs.size(), username);
            return ResponseEntity.ok(pointDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при получении списка точек: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points"));
        }
    }

    // POST /points - Создать новую точку для своей функции
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPoint(@Valid @RequestBody CreatePointRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Long currentUserId = securityService.getCurrentUserId();

        logger.info("Запрос на создание точки для функции {} пользователем: {}. Координаты: ({}, {})",
                request.getFunctionId(), username, request.getXValue(), request.getYValue());

        try {
            // Проверяем, принадлежит ли функция текущему пользователю
            if (!canAccessFunction(request.getFunctionId(), authentication)) {
                logger.warn("Пользователь {} пытается создать точку для чужой функции {}",
                        username, request.getFunctionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к этой функции", "/api/v1/points"));
            }

            // Проверяем, существует ли точка с таким X для этой функции
            if (pointsService.existsByFunctionIdAndX(request.getFunctionId(), request.getXValue())) {
                logger.warn("Попытка создания точки с существующим X: {} для функции {}",
                        request.getXValue(), request.getFunctionId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Точка с таким X уже существует для этой функции",
                                "/api/v1/points"));
            }

            Points point = new Points();
            point.setFunctionId(request.getFunctionId());
            point.setX(request.getXValue());
            point.setY(request.getYValue());

            Points savedPoint = pointsService.save(point);
            PointDTO pointDTO = convertToDTO(savedPoint);

            logger.info("Точка успешно создана с ID: {} для функции {}", savedPoint.getId(), request.getFunctionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(pointDTO);

        } catch (Exception e) {
            logger.error("Ошибка при создании точки: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points"));
        }
    }

    // POST /points/batch - Создать несколько точек для своей функции
    @PostMapping("/batch")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPointsBatch(@Valid @RequestBody CreatePointsBatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на создание {} точек для функции {} пользователем: {}",
                request.getPoints().size(), request.getFunctionId(), username);

        try {
            // Проверяем, принадлежит ли функция текущему пользователю
            if (!canAccessFunction(request.getFunctionId(), authentication)) {
                logger.warn("Пользователь {} пытается создать точки для чужой функции {}",
                        username, request.getFunctionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к этой функции", "/api/v1/points/batch"));
            }

            List<Points> savedPoints = pointsService.createPointsBatch(
                    request.getFunctionId(), request.getPoints());

            List<PointDTO> pointDTOs = convertToDTO(savedPoints);

            logger.info("Успешно создано {} точек для функции {}",
                    pointDTOs.size(), request.getFunctionId());

            return ResponseEntity.status(HttpStatus.CREATED).body(pointDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при массовом создании точек: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/batch"));
        }
    }

    // GET /points/{id} - Получить точку по ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPointById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на получение точки по ID: {} пользователем: {}", id, username);

        try {
            Optional<Points> point = pointsService.findById(id);

            if (point.isPresent()) {
                // Проверяем права доступа к функции этой точки
                Long functionId = point.get().getFunctionId();
                if (!canAccessFunction(functionId, authentication)) {
                    logger.warn("Пользователь {} пытается получить доступ к точке {} чужой функции {}",
                            username, id, functionId);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ErrorResponse("Доступ запрещен", "/api/v1/points/" + id));
                }

                PointDTO pointDTO = convertToDTO(point.get());
                logger.info("Точка с ID {} найдена для пользователя {}", id, username);
                return ResponseEntity.ok(pointDTO);
            } else {
                logger.warn("Точка с ID {} не найдена", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Точка не найдена", "/api/v1/points/" + id));
            }

        } catch (Exception e) {
            logger.error("Ошибка при поиске точки по ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/" + id));
        }
    }

    // PUT /points/{id} - Обновить точку
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePoint(@PathVariable Long id, @Valid @RequestBody UpdatePointRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на обновление точки с ID: {} пользователем: {}", id, username);

        try {
            Optional<Points> existingPoint = pointsService.findById(id);

            if (existingPoint.isEmpty()) {
                logger.warn("Точка с ID {} не найдена для обновления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Точка не найдена", "/api/v1/points/" + id));
            }

            Points point = existingPoint.get();
            Long currentFunctionId = point.getFunctionId();

            // Проверяем права доступа к текущей функции точки
            if (!canAccessFunction(currentFunctionId, authentication)) {
                logger.warn("Пользователь {} пытается обновить точку {} чужой функции {}",
                        username, id, currentFunctionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/points/" + id));
            }

            // Если меняется functionId, проверяем доступ к новой функции
            if (!currentFunctionId.equals(request.getFunctionId()) &&
                    !canAccessFunction(request.getFunctionId(), authentication)) {
                logger.warn("Пользователь {} пытается переместить точку в чужую функцию {}",
                        username, request.getFunctionId());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к целевой функции", "/api/v1/points/" + id));
            }

            // Проверяем, не занят ли новый X другой точкой этой функции
            if (!point.getX().equals(request.getXValue()) &&
                    pointsService.existsByFunctionIdAndX(request.getFunctionId(), request.getXValue())) {
                logger.warn("Попытка изменения X на существующий: {} для функции {}",
                        request.getXValue(), request.getFunctionId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Точка с таким X уже существует для этой функции",
                                "/api/v1/points/" + id));
            }

            point.setFunctionId(request.getFunctionId());
            point.setX(request.getXValue());
            point.setY(request.getYValue());

            Points updatedPoint = pointsService.save(point);
            PointDTO pointDTO = convertToDTO(updatedPoint);

            logger.info("Точка с ID {} успешно обновлена пользователем {}", id, username);

            return ResponseEntity.ok(pointDTO);

        } catch (Exception e) {
            logger.error("Ошибка при обновлении точки с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/" + id));
        }
    }

    // DELETE /points/{id} - Удалить точку по ID
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePointById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на удаление точки по ID: {} пользователем: {}", id, username);

        try {
            Optional<Points> point = pointsService.findById(id);

            if (point.isEmpty()) {
                logger.warn("Точка с ID {} не найдена для удаления", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Точка не найдена", "/api/v1/points/" + id));
            }

            // Проверяем права доступа к функции этой точки
            Long functionId = point.get().getFunctionId();
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается удалить точку {} чужой функции {}",
                        username, id, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/points/" + id));
            }

            pointsService.deleteById(id);
            logger.info("Точка с ID {} успешно удалена пользователем {}", id, username);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении точки с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/" + id));
        }
    }

    // GET /points/function/{functionId} - Получить точки по ID функции
    @GetMapping("/function/{functionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPointsByFunctionId(
            @PathVariable Long functionId,
            @RequestParam(defaultValue = "xValue") String sortField,
            @RequestParam(defaultValue = "true") boolean ascending) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на получение точек по ID функции: {} пользователем: {}", functionId, username);

        try {
            // Проверяем права доступа к функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается получить точки чужой функции {}",
                        username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/points/function/" + functionId));
            }

            List<Points> points;

            if ("xValue".equals(sortField)) {
                points = pointsService.findByFunctionIdOrdered(functionId, ascending);
            } else {
                points = pointsService.findByFunctionId(functionId);
                points = sortPoints(points, sortField, ascending);
            }

            List<PointDTO> pointDTOs = convertToDTO(points);

            logger.info("Найдено {} точек для функции с ID {}", pointDTOs.size(), functionId);
            return ResponseEntity.ok(pointDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при поиске точек по ID функции {}: {}", functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/function/" + functionId));
        }
    }

    // DELETE /points/function/{functionId} - Удалить все точки функции
    @DeleteMapping("/function/{functionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deletePointsByFunctionId(@PathVariable Long functionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на удаление всех точек функции с ID: {} пользователем: {}", functionId, username);

        try {
            // Проверяем права доступа к функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается удалить точки чужой функции {}",
                        username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/points/function/" + functionId));
            }

            long countBefore = pointsService.countByFunctionId(functionId);
            pointsService.deleteByFunctionId(functionId);

            logger.info("Успешно удалено {} точек функции с ID {}", countBefore, functionId);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            logger.error("Ошибка при удалении точек функции с ID {}: {}", functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/function/" + functionId));
        }
    }

    // POST /points/batch/search-by-ids - Множественный поиск точек по IDs
    @PostMapping("/batch/search-by-ids")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPointsByIds(@Valid @RequestBody PointIdsRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на поиск точек по IDs: {} пользователем: {}", request.getIds(), username);

        try {
            List<Points> points = pointsService.findByIds(request.getIds());

            // Фильтруем точки, к которым есть доступ
            List<Points> accessiblePoints = points.stream()
                    .filter(point -> canAccessFunction(point.getFunctionId(), authentication))
                    .collect(Collectors.toList());

            List<PointDTO> pointDTOs = convertToDTO(accessiblePoints);

            logger.info("Найдено {} доступных точек из запрошенных {}", pointDTOs.size(), request.getIds().size());

            return ResponseEntity.ok(pointDTOs);

        } catch (Exception e) {
            logger.error("Ошибка при поиске точек по IDs {}: {}", request.getIds(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/batch/search-by-ids"));
        }
    }

    // Вспомогательные методы

    private boolean canAccessFunction(Long functionId, Authentication authentication) {
        return securityService.canAccessFunction(functionId, authentication);
    }

    private List<PointDTO> convertToDTO(List<Points> points) {
        return points.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private PointDTO convertToDTO(Points point) {
        return new PointDTO(
                point.getId(),
                point.getFunctionId(),
                point.getX(),
                point.getY()
        );
    }

    private List<Points> sortPoints(List<Points> points, String sortField, boolean ascending) {
        return points.stream()
                .sorted((p1, p2) -> {
                    int result = 0;
                    switch (sortField) {
                        case "id":
                            result = p1.getId().compareTo(p2.getId());
                            break;
                        case "functionId":
                            result = p1.getFunctionId().compareTo(p2.getFunctionId());
                            break;
                        case "xValue":
                            result = p1.getX().compareTo(p2.getX());
                            break;
                        case "yValue":
                            result = p1.getY().compareTo(p2.getY());
                            break;
                        default:
                            result = p1.getId().compareTo(p2.getId());
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

        logger.warn("Ошибка валидации точек: {}", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(errorMessage, ex.getBindingResult().getObjectName()));
    }
}