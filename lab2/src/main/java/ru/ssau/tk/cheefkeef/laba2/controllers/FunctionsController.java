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
import ru.ssau.tk.cheefkeef.laba2.dto.functions.*;
import ru.ssau.tk.cheefkeef.laba2.dto.user.*;
import ru.ssau.tk.cheefkeef.laba2.dto.points.*;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import ru.ssau.tk.cheefkeef.laba2.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.cheefkeef.laba2.functions.Point;
import ru.ssau.tk.cheefkeef.laba2.functions.TabulatedFunction;
import ru.ssau.tk.cheefkeef.laba2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.cheefkeef.laba2.io.FunctionsIO;
import ru.ssau.tk.cheefkeef.laba2.services.FunctionsService;
import ru.ssau.tk.cheefkeef.laba2.services.SecurityService;
import ru.ssau.tk.cheefkeef.laba2.operations.TabulatedFunctionOperationService;
import ru.ssau.tk.cheefkeef.laba2.entities.Points;
import ru.ssau.tk.cheefkeef.laba2.services.PointsService;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/functions")
public class FunctionsController {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsController.class);

    @Autowired
    private FunctionsService functionsService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PointsService pointsService;

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

    // GET /functions/operations/{id1}/{id2}/{operation} - Выполнить операции над функциями
    @GetMapping("/operations/{id1}/{id2}/{operation}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> doOperationPlus(
            @PathVariable Long id1,
            @PathVariable Long id2,
            @PathVariable String operation,
            Authentication authentication) {

        logger.info("Запрос на сложение функций по ID1: {}, ID2: {} пользователем: {}",
                id1, id2, authentication.getName());

        try {
            // Получаем функции из базы
            Optional<Functions> function1Opt = functionsService.findById(id1);
            Optional<Functions> function2Opt = functionsService.findById(id2);

            if (function1Opt.isEmpty() || function2Opt.isEmpty()) {
                logger.warn("Функция с ID {} или {} не найдена", id1, id2);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Одна из функций не найдена", "/api/v1/functions/operations/" + id1 + "/" + id2 + "/plus"));
            }

            Functions function1 = function1Opt.get();
            Functions function2 = function2Opt.get();

            // Проверяем права доступа
            if (!securityService.canAccessFunction(id1, authentication)) {
                logger.warn("Пользователь {} пытается получить доступ к чужой функции {}",
                        authentication.getName(), id1);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к функции " + id1, "/api/v1/functions/" + id1));
            }

            if (!securityService.canAccessFunction(id2, authentication)) {
                logger.warn("Пользователь {} пытается получить доступ к чужой функции {}",
                        authentication.getName(), id2);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен к функции " + id2, "/api/v1/functions/" + id2));
            }

            // Получаем точки функций, отсортированные по X
            List<Points> points1 = pointsService.findByFunctionIdOrdered(id1, true);
            List<Points> points2 = pointsService.findByFunctionIdOrdered(id2, true);

            // Проверяем соответствие точек
            if (points1.size() != points2.size()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Функции имеют разное количество точек",
                                "/api/v1/functions/operations/" + id1 + "/" + id2 + "/plus"));
            }

            // Проверяем совпадение значений X
            for (int i = 0; i < points1.size(); i++) {
                if (Math.abs(points1.get(i).getX() - points2.get(i).getX()) > 1e-9) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse(String.format("Несовпадение значений X на позиции %d: %f vs %f",
                                    i, points1.get(i).getX(), points2.get(i).getX()),
                                    "/api/v1/functions/operations/" + id1 + "/" + id2 + "/plus"));
                }
            }

            // Преобразуем точки в массивы для создания табулированных функций
            double[] xValues = points1.stream().mapToDouble(Points::getX).toArray();
            double[] yValues1 = points1.stream().mapToDouble(Points::getY).toArray();
            double[] yValues2 = points2.stream().mapToDouble(Points::getY).toArray();

            // Создаем сервис операций
            TabulatedFunctionOperationService operationService = new TabulatedFunctionOperationService(new ArrayTabulatedFunctionFactory());

            // Создаем табулированные функции
            TabulatedFunction func1 = operationService.getFactory().create(xValues, yValues1);
            TabulatedFunction func2 = operationService.getFactory().create(xValues, yValues2);

            TabulatedFunction resultFunction = operationService.add(func1, func2);
            // Выполняем сложение
            if (operation.equals("plus")) resultFunction = operationService.add(func1, func2);
            if (operation.equals("divive")) resultFunction = operationService.divide(func1, func2);
            if (operation.equals("multiply")) resultFunction = operationService.multiply(func1, func2);
            if (operation.equals("minus")) resultFunction = operationService.subtract(func1, func2);

            Point[] resultPoints = TabulatedFunctionOperationService.asPoints(resultFunction);

            // Создаем новую функцию для результата
            Long currentUserId = securityService.getCurrentUserId();

            Functions resultFunctionEntity = new Functions();
            resultFunctionEntity.setUserId(currentUserId); // Предполагается наличие этого метода

            resultFunctionEntity.setName(String.format("Сумма функций %d и %d", id1, id2));

            if (operation.equals("plus")) resultFunctionEntity.setName(String.format("Сумма функций %d и %d", id1, id2));
            if (operation.equals("divive")) resultFunctionEntity.setName(String.format("Деление функций %d и %d", id1, id2));
            if (operation.equals("multiply")) resultFunctionEntity.setName(String.format("Умножение функций %d и %d", id1, id2));
            if (operation.equals("minus")) resultFunctionEntity.setName(String.format("Вычитание функций %d и %d", id1, id2));

            resultFunctionEntity.setSignature(function1.getName() + " + " + function2.getName());

            Functions savedFunction = functionsService.save(resultFunctionEntity);

            // Сохраняем точки результата
            List<Points> pointsToSave = Arrays.stream(resultPoints)
                    .map(point -> {
                        Points p = new Points();
                        p.setFunctionId(savedFunction.getId());
                        p.setX(point.x);
                        p.setY(point.y);
                        return p;
                    })
                    .collect(Collectors.toList());

            List<Points> savedPoints = pointsService.saveAll(pointsToSave);

            // Формируем ответ в требуемом формате
            List<OperationPointResponse> response = savedPoints.stream()
                    .map(p -> new OperationPointResponse(
                            p.getId(),
                            p.getFunctionId(),
                            p.getX(),
                            p.getY()
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (InconsistentFunctionsException e) {
            logger.error("Ошибка при операции над функциями: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Несовместимые функции: " + e.getMessage(),
                            "/api/v1/functions/operations/" + id1 + "/" + id2 + "/plus"));
        } catch (Exception e) {
            logger.error("Ошибка при выполнении операции: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/functions/operations/" + id1 + "/" + id2 + "/plus"));
        }
    }

    // GET /functions/serialize/{id} - Получить сериализованную функцию как строку
    @GetMapping("/serialize/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> serializeFunction(@PathVariable Long id, Authentication authentication) {
        logger.info("Запрос на сериализацию функции с ID: {} пользователем: {}", id, authentication.getName());

        try {
            // Проверка существования функции
            Optional<Functions> functionOpt = functionsService.findById(id);
            if (functionOpt.isEmpty()) {
                logger.warn("Функция с ID {} не найдена", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("Функция не найдена", "/api/v1/functions/serialize/" + id));
            }

            Functions functionEntity = functionOpt.get();

            // Проверка прав доступа
            if (!securityService.canAccessFunction(id, authentication)) {
                logger.warn("Пользователь {} пытается получить доступ к чужой функции {}",
                        authentication.getName(), id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/" + id));
            }

            // Получение точек функции (отсортированных по X)
            List<Points> points = pointsService.findByFunctionIdOrdered(id, true);
            if (points.isEmpty()) {
                logger.warn("Функция с ID {} не имеет точек", id);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Функция не содержит точек", "/api/v1/functions/serialize/" + id));
            }

            // Преобразование точек в массивы для создания TabulatedFunction
            double[] xValues = points.stream().mapToDouble(Points::getX).toArray();
            double[] yValues = points.stream().mapToDouble(Points::getY).toArray();

            // Создание табулированной функции
            TabulatedFunction tabulatedFunction = new ArrayTabulatedFunctionFactory().create(xValues, yValues);
            logger.debug("Создана табулированная функция с {} точками для ID {}", points.size(), id);

            // Сериализация в байтовый массив
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            try (BufferedOutputStream bufferedStream = new BufferedOutputStream(byteStream)) {
                FunctionsIO.serialize(bufferedStream, tabulatedFunction);
            }

            // Преобразование в Base64 строку
            String serializedString = Base64.getEncoder().encodeToString(byteStream.toByteArray());
            logger.info("Функция с ID {} успешно сериализована в строку длиной {}", id, serializedString.length());

            return ResponseEntity.ok(serializedString);

        } catch (Exception e) {
            logger.error("Ошибка при сериализации функции с ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка сериализации", "/api/v1/functions/serialize/" + id));
        }
    }

    @PostMapping("/deserialize")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deserializeFunction(
            @Valid @RequestBody DeserializeFunctionRequest request,
            Authentication authentication) {

        logger.info("Запрос на десериализацию функции пользователем: {}", authentication.getName());

        try {
            // Декодируем Base64 строку
            byte[] serializedBytes;
            try {
                serializedBytes = Base64.getDecoder().decode(request.getSerializedFunction());
            } catch (IllegalArgumentException e) {
                logger.warn("Некорректная Base64 строка: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Неверный формат Base64", "/api/v1/functions/deserialize"));
            }

            // Десериализуем из байтов
            TabulatedFunction function;
            try (BufferedInputStream bufferedStream = new BufferedInputStream(new ByteArrayInputStream(serializedBytes))) {
                function = FunctionsIO.deserialize(bufferedStream);
            } catch (ClassNotFoundException e) {
                logger.error("Не удалось десериализовать: класс не найден", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Несовместимый формат данных", "/api/v1/functions/deserialize"));
            } catch (IOException e) {
                logger.error("Ошибка при десериализации: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Повреждённые данные", "/api/v1/functions/deserialize"));
            }

            // Сохраняем как новую функцию
            Functions newFunctionEntity = new Functions();
            Long currentUserId = securityService.getCurrentUserId();
            newFunctionEntity.setUserId(currentUserId);
            newFunctionEntity.setName("Десериализованная функция");
            newFunctionEntity.setSignature("Создана из сериализованной строки");
            Functions savedFunction = functionsService.save(newFunctionEntity);

            // Сохраняем точки
            List<Points> pointsToSave = new ArrayList<>();
            for (int i = 0; i < function.getCount(); i++) {
                Points point = new Points();
                point.setFunctionId(savedFunction.getId());
                point.setX(function.getX(i));
                point.setY(function.getY(i));
                pointsToSave.add(point);
            }
            pointsService.saveAll(pointsToSave);

            logger.info("Функция успешно десериализована и сохранена с ID {}", savedFunction.getId());

            // Возвращаем ID новой функции или список точек
            return ResponseEntity.ok(Map.of(
                    "functionId", savedFunction.getId(),
                    "pointCount", function.getCount()
            ));

        } catch (Exception e) {
            logger.error("Необработанная ошибка при десериализации: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ошибка сервера при десериализации", "/api/v1/functions/deserialize"));
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

    // GET /functions/users/{userId}/count - Получить количество функций пользователя
    @GetMapping("/users/{userId}/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getFunctionCountByUserId(@PathVariable Long userId,
                                                      Authentication authentication) {
        logger.info("Запрос на получение количества функций пользователя с ID: {} пользователем: {}",
                userId, authentication.getName());

        try {
            // Проверяем права доступа
            Long currentUserId = securityService.getCurrentUserId();
            if (!userId.equals(currentUserId) && !authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                logger.warn("Пользователь {} пытается получить количество функций другого пользователя {}",
                        authentication.getName(), userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен", "/api/v1/functions/users/" + userId + "/count"));
            }

            long count = functionsService.countByUserId(userId);
            logger.info("Найдено {} функций для пользователя с ID {}", count, userId);

            // Создаем ответ в соответствии со спецификацией
            FunctionCountResponse response = new FunctionCountResponse((int) count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Ошибка при получении количества функций для пользователя {}: {}",
                    userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Внутренняя ошибка сервера",
                            "/api/v1/functions/users/" + userId + "/count"));
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