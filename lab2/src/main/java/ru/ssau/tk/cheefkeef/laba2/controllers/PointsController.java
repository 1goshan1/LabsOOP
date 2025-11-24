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
import ru.ssau.tk.cheefkeef.laba2.functions.*;
import ru.ssau.tk.cheefkeef.laba2.services.FunctionsService;
import ru.ssau.tk.cheefkeef.laba2.services.PointsService;
import ru.ssau.tk.cheefkeef.laba2.services.SecurityService;
import ru.ssau.tk.cheefkeef.laba2.operations.LeftSteppingDifferentialOperator;

import java.util.*;

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

    @GetMapping("/linear/{functionId}/interpolate/{x}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> interpolateAtX(
            @PathVariable Long functionId,
            @PathVariable double x) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Запрос на интерполяцию функции {} в точке x={} пользователем: {}", functionId, x, username);

        try {
            // Проверка доступа к функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается выполнить интерполяцию для чужой функции {}", username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к этой функции",
                                "/api/v1/points/linear/" + functionId + "/interpolate"));
            }

            // Получаем точки функции
            List<Points> points = pointsService.findByFunctionId(functionId);
            if (points.isEmpty()) {
                logger.warn("Функция {} не имеет точек", functionId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Функция не содержит точек для интерполяции",
                                "/api/v1/points/linear/" + functionId + "/interpolate"));
            }

            // Сортируем по X
            points.sort(Comparator.comparingDouble(Points::getX));

            // Преобразуем в массивы
            double[] xValues = points.stream().mapToDouble(Points::getX).toArray();
            double[] yValues = points.stream().mapToDouble(Points::getY).toArray();

            // Создаём табулированную функцию
            ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

            // Выполняем интерполяцию (или экстраполяцию) через apply()
            double y = function.apply(x);

            // Формируем ответ в требуемом формате
            Map<String, Double> response = Map.of("xvalue", x, "yvalue", y);

            logger.info("Успешно выполнена интерполяция для функции {} в точке x={}: y={}", functionId, x, y);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при интерполяции функции {}: {}", functionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Ошибка интерполяции: " + e.getMessage(),
                            "/api/v1/points/linear/" + functionId + "/interpolate"));
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при интерполяции функции {}: {}", functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/points/linear/" + functionId + "/interpolate"));
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

            // Создаем объект ArrayTabulatedFunction из полученных точек
            if (!savedPoints.isEmpty()) {
                double[] xValues = new double[savedPoints.size()];
                double[] yValues = new double[savedPoints.size()];

                for (int i = 0; i < savedPoints.size(); i++) {
                    Points point = savedPoints.get(i);
                    xValues[i] = point.getX();
                    yValues[i] = point.getY();
                }

                ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);
                logger.debug("Создан объект ArrayTabulatedFunction с {} точками", savedPoints.size());
            }

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

    @PostMapping("/composite/{functionId}/{function}/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createCompositeFunction(
            @PathVariable Long functionId,
            @PathVariable String function,
            @PathVariable String name) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Запрос на создание композитной функции для функции {} с типом {} и именем {} пользователем: {}",
                functionId, function, name, username);

        try {
            // Проверка доступа к исходной функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается создать композитную функцию для чужой функции {}",
                        username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к исходной функции",
                                "/api/v1/points/composite/" + functionId + "/" + function + "/" + name));
            }

            // Получаем исходную функцию
            Optional<Functions> originalFunctionOpt = functionsService.findById(functionId);
            if (originalFunctionOpt.isEmpty()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Исходная функция не найдена",
                                "/api/v1/points/composite/" + functionId + "/" + function + "/" + name));
            }
            Functions originalFunction = originalFunctionOpt.get();

            // Получаем точки исходной функции и сортируем по X
            List<Points> originalPoints = pointsService.findByFunctionId(functionId);
            if (originalPoints.isEmpty()) {
                logger.warn("Для функции с ID {} не найдены точки", functionId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Для исходной функции не найдены точки",
                                "/api/v1/points/composite/" + functionId + "/" + function + "/" + name));
            }
            originalPoints.sort(Comparator.comparingDouble(Points::getX));

            // Создаем соответствующую функцию в зависимости от параметра
            MathFunction mathFunction;
            switch (function.toLowerCase()) {
                case "identity":
                    mathFunction = new IdentityFunction();
                    break;
                case "sqr":
                    mathFunction = new SqrFunction();
                    break;
                case "constant":
                    mathFunction = new ConstantFunction(1.0); // Используем 1.0 как константу
                    break;
                default:
                    logger.warn("Неподдерживаемый тип функции: {}", function);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("Неподдерживаемый тип функции. Доступные: identity, sqr, constant",
                                    "/api/v1/points/composite/" + functionId + "/" + function + "/" + name));
            }

            // Создаем новую функцию
            Functions newFunction = new Functions();
            newFunction.setName(name);
            newFunction.setSignature(function + "(" + functionId + ")");
            newFunction.setUserId(originalFunction.getUserId()); // Новая функция принадлежит тому же пользователю
            Functions savedFunction = functionsService.save(newFunction);

            // Вычисляем значения новой функции для каждой точки
            List<Points> newPoints = new ArrayList<>();
            for (Points originalPoint : originalPoints) {
                double x = originalPoint.getX();
                double y_old = originalPoint.getY();
                double y = mathFunction.apply(y_old); // Применяем функцию к значению X

                Points point = new Points();
                point.setFunctionId(savedFunction.getId());
                point.setX(x);
                point.setY(y);
                newPoints.add(point);
            }

            // Сохраняем точки
            List<Points> savedPoints = pointsService.saveAll(newPoints);

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("initFunctionId", functionId);
            response.put("functionId", savedFunction.getId());

            List<Map<String, Double>> pointsList = savedPoints.stream()
                    .map(point -> {
                        Map<String, Double> pointMap = new HashMap<>();
                        pointMap.put("xvalue", point.getX());
                        pointMap.put("yvalue", point.getY());
                        return pointMap;
                    })
                    .collect(Collectors.toList());

            response.put("points", pointsList);

            logger.info("Успешно создана композитная функция с ID: {} на основе функции {}",
                    savedFunction.getId(), functionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Ошибка при создании композитной функции для функции {}: {}",
                    functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/points/composite/" + functionId + "/" + function + "/" + name));
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

    // PointsController.java - добавляем новый метод с Map

    // GET /points/generate/{function}/{from}/{to}/{count} - Генерация точек табулированной функции
    @GetMapping("/generate/{function}/{from}/{to}/{count}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> generateTabulatedFunction(
            @PathVariable String function,
            @PathVariable double from,
            @PathVariable double to,
            @PathVariable int count) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на генерацию функции {} в диапазоне [{}, {}] с {} точками пользователем: {}",
                function, from, to, count, username);

        try {
            // Проверяем минимальное количество точек
            if (count < 2) {
                logger.warn("Некорректное количество точек: {}", count);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Количество точек должно быть не менее 2",
                                "/api/v1/points/generate/" + function + "/" + from + "/" + to + "/" + count));
            }

            // Создаем Map для выбора функций
            Map<String, MathFunction> functionMap = Map.of(
                    "identity", new IdentityFunction(),
                    "constant", new ConstantFunction(1.0),
                    "sqr", new SqrFunction()
            );

            // Получаем функцию из Map по ключу
            MathFunction mathFunction = functionMap.get(function.toLowerCase());

            if (mathFunction == null) {
                logger.warn("Неподдерживаемый тип функции: {}. Доступные: {}", function, functionMap.keySet());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Неподдерживаемый тип функции. Доступные: " + functionMap.keySet(),
                                "/api/v1/points/generate/" + function + "/" + from + "/" + to + "/" + count));
            }

            // Создаем табулированную функцию
            ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(mathFunction, from, to, count);

            // Преобразуем в DTO
            List<TabulatedPointDTO> points = new ArrayList<>();
            for (int i = 0; i < tabulatedFunction.getCount(); i++) {
                points.add(new TabulatedPointDTO(
                        tabulatedFunction.getX(i),
                        tabulatedFunction.getY(i)
                ));
            }

            TabulatedFunctionResponse response = new TabulatedFunctionResponse(points);

            logger.info("Успешно сгенерировано {} точек для функции {}", points.size(), function);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при генерации функции: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(),
                            "/api/v1/points/generate/" + function + "/" + from + "/" + to + "/" + count));
        } catch (Exception e) {
            logger.error("Ошибка при генерации точек функции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/points/generate/" + function + "/" + from + "/" + to + "/" + count));
        }
    }

    @GetMapping("/differential/{functionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> differentiateFunction(@PathVariable Long functionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Запрос на дифференцирование функции с ID: {} пользователем: {}", functionId, username);

        try {
            // Проверка доступа к функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается дифференцировать чужую функцию {}", username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к этой функции", "/api/v1/points/differential/" + functionId));
            }

            // Получаем точки исходной функции
            List<Points> originalPoints = pointsService.findByFunctionId(functionId);
            if (originalPoints.isEmpty()) {
                logger.warn("Не найдены точки для функции с ID: {}", functionId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Для функции не найдены точки", "/api/v1/points/differential/" + functionId));
            }

            // Сортируем точки по X для корректного создания ArrayTabulatedFunction
            originalPoints.sort(Comparator.comparingDouble(Points::getX));

            // Создаем массивы X и Y значений
            double[] xValues = originalPoints.stream().mapToDouble(Points::getX).toArray();
            double[] yValues = originalPoints.stream().mapToDouble(Points::getY).toArray();

            // Создаем ArrayTabulatedFunction
            ArrayTabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Определяем шаг для дифференцирования
            double step = xValues.length > 1 ? xValues[1] - xValues[0] : 1.0;
            LeftSteppingDifferentialOperator diffOperator = new LeftSteppingDifferentialOperator(step);

            // Применяем оператор дифференцирования
            MathFunction differentiatedFunction = diffOperator.derive(originalFunction);

            // Получаем исходную функцию
            Optional<Functions> originalFunctionOpt = functionsService.findById(functionId);
            if (originalFunctionOpt.isEmpty()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Функция не найдена", "/api/v1/points/differential/" + functionId));
            }
            Functions originalFunctionEntity = originalFunctionOpt.get();

            // Создаем новую функцию
            Functions newFunction = new Functions();
            newFunction.setName("Дифференцирование " + functionId);
            newFunction.setSignature("d" + functionId + "/dx");
            newFunction.setUserId(originalFunctionEntity.getUserId());
            Functions savedFunction = functionsService.save(newFunction);

            // Создаем точки для дифференцированной функции (пропускаем первую точку)
            List<Points> diffPoints = new ArrayList<>();
            for (int i = 1; i < originalFunction.getCount(); i++) {
                double x = originalFunction.getX(i);
                double y = differentiatedFunction.apply(x);

                Points point = new Points();
                point.setFunctionId(savedFunction.getId());
                point.setX(x);
                point.setY(y);
                diffPoints.add(point);
            }

            // Сохраняем точки
            List<Points> savedPoints = pointsService.saveAll(diffPoints);

            // Формируем ответ в требуемом формате
            Map<String, Object> response = new HashMap<>();
            response.put("dfunctionId", functionId);
            response.put("functionId", savedFunction.getId());

            List<Map<String, Double>> pointsList = savedPoints.stream()
                    .map(point -> {
                        Map<String, Double> pointMap = new HashMap<>();
                        pointMap.put("xvalue", point.getX());
                        pointMap.put("yvalue", point.getY());
                        return pointMap;
                    })
                    .collect(Collectors.toList());

            response.put("points", pointsList);

            logger.info("Успешно выполнено дифференцирование функции {}. Создана новая функция с ID: {}",
                    functionId, savedFunction.getId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при дифференцировании функции с ID {}: {}", functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера", "/api/v1/points/differential/" + functionId));
        }
    }

    // PUT /points/update/batch/{functionId} - Массовое обновление точек функции
    @PutMapping("/update/batch/{functionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePointsBatch(
            @PathVariable Long functionId,
            @Valid @RequestBody UpdatePointsBatchRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        logger.info("Запрос на массовое обновление {} точек для функции {} пользователем: {}",
                request.getPoints().size(), functionId, username);

        try {
            // Проверяем, что functionId в пути совпадает с functionId в теле запроса
            if (!functionId.equals(request.getFunctionId())) {
                logger.warn("Несоответствие functionId в пути ({}) и в теле ({})",
                        functionId, request.getFunctionId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("ID функции в пути и в теле запроса не совпадают",
                                "/api/v1/points/update/batch/" + functionId));
            }

            // Проверяем права доступа к функции
            if (!canAccessFunction(functionId, authentication)) {
                logger.warn("Пользователь {} пытается обновлять точки чужой функции {}",
                        username, functionId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к этой функции",
                                "/api/v1/points/update/batch/" + functionId));
            }

            // Проверяем уникальность X значений в рамках одного запроса
            Map<Double, Long> xValueCounts = request.getPoints().stream()
                    .collect(Collectors.groupingBy(UpdatePointCoordinate::getXValue,
                            Collectors.counting()));

            List<Double> duplicateXValues = xValueCounts.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicateXValues.isEmpty()) {
                logger.warn("Обнаружены дублирующиеся X значения в запросе: {}", duplicateXValues);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Обнаружены дублирующиеся X значения: " + duplicateXValues,
                                "/api/v1/points/update/batch/" + functionId));
            }

            // Проверяем, не заняты ли новые X значения другими точками этой функции
            List<Double> newXValues = request.getPoints().stream()
                    .map(UpdatePointCoordinate::getXValue)
                    .collect(Collectors.toList());

            // Получаем существующие точки с такими X значениями (исключая обновляемые точки)
            List<Long> updatingPointIds = request.getPoints().stream()
                    .map(UpdatePointCoordinate::getId)
                    .collect(Collectors.toList());

            List<Points> conflictingPoints = pointsService.findByFunctionIdAndXInAndIdNotIn(
                    functionId, newXValues, updatingPointIds);

            if (!conflictingPoints.isEmpty()) {
                List<Double> conflictingXValues = conflictingPoints.stream()
                        .map(Points::getX)
                        .collect(Collectors.toList());
                logger.warn("Конфликтующие X значения: {}", conflictingXValues);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Точки с такими X значениями уже существуют: " + conflictingXValues,
                                "/api/v1/points/update/batch/" + functionId));
            }

            // Выполняем массовое обновление
            List<Points> updatedPoints = pointsService.updatePointsBatch(functionId, request.getPoints());
            List<PointDTO> pointDTOs = convertToDTO(updatedPoints);

            logger.info("Успешно обновлено {} точек для функции {}", pointDTOs.size(), functionId);

            return ResponseEntity.ok(pointDTOs);

        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при массовом обновлении точек: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage(), "/api/v1/points/update/batch/" + functionId));
        } catch (Exception e) {
            logger.error("Ошибка при массовом обновлении точек для функции {}: {}",
                    functionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/points/update/batch/" + functionId));
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