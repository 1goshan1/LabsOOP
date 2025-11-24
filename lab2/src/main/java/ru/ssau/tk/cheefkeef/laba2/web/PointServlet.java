package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.auth.AuthorizationService;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.functions.*;
import ru.ssau.tk.cheefkeef.laba2.jdbc.FunctionDAO;
import ru.ssau.tk.cheefkeef.laba2.jdbc.PointDAO;
import ru.ssau.tk.cheefkeef.laba2.mapper.PointMapper;
import ru.ssau.tk.cheefkeef.laba2.models.Function;
import ru.ssau.tk.cheefkeef.laba2.models.Point;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import ru.ssau.tk.cheefkeef.laba2.operations.LeftSteppingDifferentialOperator;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/api/v1/points/*")
public class PointServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(PointServlet.class);
    private final PointDAO pointDAO = new PointDAO();
    private final FunctionDAO functionDAO = new FunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();
        try {
            // Проверка аутентификации для всех GET запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllPoints(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetPointById(pathInfo.substring(1), req, resp);
            } else if (pathInfo.startsWith("/function/")) {
                handleGetPointsByFunctionId(pathInfo.substring("/function/".length()), req, resp);
            } else if (pathInfo.equals("/batch/search-by-ids")) {
                handleBatchSearchByIds(req, resp);
            } else if (pathInfo.startsWith("/generate/")) {
                handleGenerateTabulatedFunction(pathInfo.substring("/generate/".length()), req, resp);
            } else if (pathInfo.startsWith("/differential/")) {
                handleDifferentiateFunction(pathInfo.substring("/differential/".length()), req, resp);
            } else if (pathInfo.startsWith("/linear/")) {
                handleInterpolateAtX(pathInfo.substring("/linear/".length()), req, resp);
            } else {
                handleError(resp, 400, "Неверный путь", req.getRequestURI());
            }
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("GET запрос обработан за {} мс", executionTime);
        } catch (Exception e) {
            logger.error("Ошибка при обработке GET запроса: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", req.getRequestURI());
        }
    }
    private void handleInterpolateAtX(String pathSuffix, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Запрос на линейную интерполяцию: {}", pathSuffix);

        // Разбираем путь: {functionId}/interpolate/{x}
        // /linear/123/interpolate/2.5
        String[] parts = pathSuffix.split("/");
        if (parts.length != 3 || !"interpolate".equals(parts[1])) {
            handleError(resp, 400, "Неверный формат пути для интерполяции",
                    "/api/v1/points/linear/" + pathSuffix);
            return;
        }

        try {
            int functionId = Integer.parseInt(parts[0]);
            double x = Double.parseDouble(parts[2]);

            User currentUser = AuthorizationService.getCurrentUser(req);
            logger.info("Запрос на интерполяцию функции {} в точке x={} пользователем: {}",
                    functionId, x, currentUser.getLogin());

            // Проверка доступа к функции
            Optional<Function> functionOpt = functionDAO.findById(functionId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                handleError(resp, 404, "Функция не найдена",
                        "/api/v1/points/linear/" + functionId + "/interpolate");
                return;
            }

            Function function = functionOpt.get();
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается выполнить интерполяцию для чужой функции {}",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступ запрещен к этой функции");
                return;
            }

            // Получаем точки функции
            List<Point> points = pointDAO.findByFunctionId(functionId);
            if (points.isEmpty()) {
                logger.warn("Функция {} не имеет точек", functionId);
                handleError(resp, 400, "Функция не содержит точек для интерполяции",
                        "/api/v1/points/linear/" + functionId + "/interpolate");
                return;
            }

            // Сортируем по X
            points.sort(Comparator.comparingDouble(Point::getXValue));

            // Преобразуем в массивы
            double[] xValues = points.stream().mapToDouble(Point::getXValue).toArray();
            double[] yValues = points.stream().mapToDouble(Point::getYValue).toArray();

            // Создаём табулированную функцию
            ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Выполняем интерполяцию (или экстраполяцию) через apply()
            double y = tabulatedFunction.apply(x);

            // Формируем ответ в требуемом формате
            Map<String, Double> response = new HashMap<>();
            response.put("xvalue", x);
            response.put("yvalue", y);

            logger.info("Успешно выполнена интерполяция для функции {} в точке x={}: y={}",
                    functionId, x, y);
            writeJson(resp, 200, response);

        } catch (NumberFormatException e) {
            logger.error("Неверный формат параметров: {}", pathSuffix, e);
            handleError(resp, 400, "Неверный формат параметров",
                    "/api/v1/points/linear/" + pathSuffix);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при интерполяции: {}", e.getMessage());
            handleError(resp, 400, "Ошибка интерполяции: " + e.getMessage(),
                    "/api/v1/points/linear/" + pathSuffix);
        } catch (Exception e) {
            logger.error("Внутренняя ошибка при интерполяции: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера",
                    "/api/v1/points/linear/" + pathSuffix);
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();
        try {
            // Проверка аутентификации для всех POST запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("")) {
                handleCreatePoint(req, resp);
            } else if (pathInfo.equals("/batch")) {
                handleCreatePointsBatch(req, resp);
            } else if (pathInfo.equals("/batch/search-by-ids")) {
                handleBatchSearchByIds(req, resp);
            } else if (pathInfo.startsWith("/composite/")) {
                handleCreateCompositeFunction(pathInfo.substring("/composite/".length()), req, resp);
            } else {
                handleError(resp, 400, "Неверный путь", req.getRequestURI());
            }
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("POST запрос обработан за {} мс", executionTime);
        } catch (Exception e) {
            logger.error("Ошибка при обработке POST запроса: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", req.getRequestURI());
        }
    }

    // Новый метод для создания композитной функции
    private void handleCreateCompositeFunction(String pathSuffix, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Запрос на создание композитной функции: {}", pathSuffix);

        // Разбираем путь: functionId/function/name
        String[] parts = pathSuffix.split("/");
        if (parts.length != 3) {
            handleError(resp, 400, "Неверный формат пути для создания композитной функции",
                    "/api/v1/points/composite/" + pathSuffix);
            return;
        }

        try {
            int functionId = Integer.parseInt(parts[0]);
            String functionType = parts[1].toLowerCase();
            String name = parts[2];

            User currentUser = AuthorizationService.getCurrentUser(req);
            logger.info("Запрос на создание композитной функции для функции {} с типом {} и именем {} пользователем: {}",
                    functionId, functionType, name, currentUser.getLogin());

            // Проверка доступа к исходной функции
            Optional<Function> functionOpt = functionDAO.findById(functionId); // Изменено имя переменной
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                handleError(resp, 404, "Исходная функция не найдена",
                        "/api/v1/points/composite/" + functionId + "/" + functionType + "/" + name);
                return;
            }

            Function originalFunction = functionOpt.get(); // Используем правильное имя переменной
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(originalFunction.getUserId())) {
                logger.warn("Пользователь {} пытается создать композитную функцию для чужой функции {}",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступ запрещен к исходной функции");
                return;
            }

            // Получаем точки исходной функции и сортируем по X
            List<Point> originalPoints = pointDAO.findByFunctionId(functionId);
            if (originalPoints.isEmpty()) {
                logger.warn("Для функции с ID {} не найдены точки", functionId);
                handleError(resp, 400, "Для исходной функции не найдены точки",
                        "/api/v1/points/composite/" + functionId + "/" + functionType + "/" + name);
                return;
            }
            originalPoints.sort(Comparator.comparingDouble(Point::getXValue));

            // Создаем соответствующую функцию в зависимости от параметра
            MathFunction mathFunction;
            switch (functionType) {
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
                    logger.warn("Неподдерживаемый тип функции: {}", functionType);
                    handleError(resp, 400, "Неподдерживаемый тип функции. Доступные: identity, sqr, constant",
                            "/api/v1/points/composite/" + functionId + "/" + functionType + "/" + name);
                    return;
            }

            // Создаем новую функцию
            Function newFunction = new Function();
            newFunction.setName(name);
            newFunction.setSignature(functionType + "(" + functionId + ")");
            newFunction.setUserId(originalFunction.getUserId()); // Новая функция принадлежит тому же пользователю
            Function savedFunction = functionDAO.insert(newFunction);
            if (savedFunction == null) {
                logger.error("Не удалось создать новую функцию");
                handleError(resp, 500, "Не удалось создать новую функцию",
                        "/api/v1/points/composite/" + functionId + "/" + functionType + "/" + name);
                return;
            }

            // Вычисляем значения новой функции для каждой точки
            List<Point> newPoints = new ArrayList<>();
            for (Point originalPoint : originalPoints) {
                double x = originalPoint.getXValue();
                double y_old = originalPoint.getYValue();
                double y = mathFunction.apply(y_old); // Применяем функцию к значению Y

                Point point = new Point();
                point.setFunctionId(savedFunction.getId());
                point.setXValue(x);
                point.setYValue(y);
                newPoints.add(point);
            }

            // Сохраняем точки
            pointDAO.insertBatch(newPoints);

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("initFunctionId", functionId);
            response.put("functionId", savedFunction.getId());

            List<Map<String, Double>> pointsList = newPoints.stream()
                    .map(point -> {
                        Map<String, Double> pointMap = new HashMap<>();
                        pointMap.put("xvalue", point.getXValue());
                        pointMap.put("yvalue", point.getYValue());
                        return pointMap;
                    })
                    .collect(Collectors.toList());

            response.put("points", pointsList);

            logger.info("Успешно создана композитная функция с ID: {} на основе функции {}",
                    savedFunction.getId(), functionId);
            writeJson(resp, 201, response); // HttpStatus.CREATED = 201

        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID функции: {}", parts[0], e);
            handleError(resp, 400, "Неверный формат ID функции",
                    "/api/v1/points/composite/" + pathSuffix);
        } catch (Exception e) {
            logger.error("Ошибка при создании композитной функции для функции {}: {}",
                    parts[0], e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера",
                    "/api/v1/points/composite/" + pathSuffix);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();
        try {
            // Проверка аутентификации для всех PUT запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleUpdatePoint(pathInfo.substring(1), req, resp);
            } else if (pathInfo != null && pathInfo.startsWith("/update/batch/")) {
                handleUpdatePointsBatch(pathInfo.substring("/update/batch/".length()), req, resp);
            } else {
                handleError(resp, 400, "Неверный путь", req.getRequestURI());
            }
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("PUT запрос обработан за {} мс", executionTime);
        } catch (Exception e) {
            logger.error("Ошибка при обработке PUT запроса: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", req.getRequestURI());
        }
    }
    // Новый метод для генерации точек табулированной функции
    private void handleGenerateTabulatedFunction(String pathSuffix, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Запрос на генерацию табулированной функции: {}", pathSuffix);

        // Разбираем путь: function/from/to/count
        String[] parts = pathSuffix.split("/");
        if (parts.length != 4) {
            handleError(resp, 400, "Неверный формат пути для генерации функции", "/api/v1/points/generate/" + pathSuffix);
            return;
        }

        try {
            String functionType = parts[0].toLowerCase();
            double from = Double.parseDouble(parts[1]);
            double to = Double.parseDouble(parts[2]);
            int count = Integer.parseInt(parts[3]);

            User currentUser = AuthorizationService.getCurrentUser(req);
            logger.info("Запрос на генерацию функции {} в диапазоне [{}, {}] с {} точками пользователем: {}",
                    functionType, from, to, count, currentUser.getLogin());

            // Проверяем минимальное количество точек
            if (count < 2) {
                logger.warn("Некорректное количество точек: {}", count);
                handleError(resp, 400, "Количество точек должно быть не менее 2",
                        "/api/v1/points/generate/" + functionType + "/" + from + "/" + to + "/" + count);
                return;
            }

            // Создаем Map для выбора функций
            Map<String, MathFunction> functionMap = new HashMap<>();
            functionMap.put("identity", new IdentityFunction());
            functionMap.put("constant", new ConstantFunction(1.0));
            functionMap.put("sqr", new SqrFunction());

            // Получаем функцию из Map по ключу
            MathFunction mathFunction = functionMap.get(functionType);

            if (mathFunction == null) {
                logger.warn("Неподдерживаемый тип функции: {}. Доступные: {}", functionType, functionMap.keySet());
                handleError(resp, 400, "Неподдерживаемый тип функции. Доступные: " + functionMap.keySet(),
                        "/api/v1/points/generate/" + functionType + "/" + from + "/" + to + "/" + count);
                return;
            }

            // Создаем табулированную функцию
            ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(mathFunction, from, to, count);

            // Формируем ответ в требуемом формате
            List<Map<String, Double>> pointsList = new ArrayList<>();
            for (int i = 0; i < tabulatedFunction.getCount(); i++) {
                Map<String, Double> pointMap = new HashMap<>();
                pointMap.put("xvalue", tabulatedFunction.getX(i));
                pointMap.put("yvalue", tabulatedFunction.getY(i));
                pointsList.add(pointMap);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("points", pointsList);

            logger.info("Успешно сгенерировано {} точек для функции {}", pointsList.size(), functionType);
            writeJson(resp, 200, response);

        } catch (NumberFormatException e) {
            logger.error("Неверный формат параметров: {}", pathSuffix, e);
            handleError(resp, 400, "Неверный формат параметров", "/api/v1/points/generate/" + pathSuffix);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при генерации функции: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/points/generate/" + pathSuffix);
        } catch (Exception e) {
            logger.error("Ошибка при генерации точек функции: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", "/api/v1/points/generate/" + pathSuffix);
        }
    }

    // Новый метод для дифференцирования функции
    private void handleDifferentiateFunction(String functionIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Запрос на дифференцирование функции с ID: {}", functionIdStr);

        try {
            int functionId = Integer.parseInt(functionIdStr);
            ValidationUtils.validateId(functionId, "функция");

            User currentUser = AuthorizationService.getCurrentUser(req);
            logger.info("Запрос на дифференцирование функции с ID: {} пользователем: {}", functionId, currentUser.getLogin());

            // Проверка доступа к функции
            Optional<Function> functionOpt = functionDAO.findById(functionId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                handleError(resp, 404, "Функция не найдена", "/api/v1/points/differential/" + functionId);
                return;
            }

            Function function = functionOpt.get();
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается дифференцировать чужую функцию {}",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступ запрещен к этой функции");
                return;
            }

            // Получаем точки исходной функции
            List<Point> originalPoints = pointDAO.findByFunctionId(functionId);
            if (originalPoints.isEmpty()) {
                logger.warn("Не найдены точки для функции с ID: {}", functionId);
                handleError(resp, 400, "Для функции не найдены точки",
                        "/api/v1/points/differential/" + functionId);
                return;
            }

            // Сортируем точки по X для корректного создания ArrayTabulatedFunction
            originalPoints.sort(Comparator.comparingDouble(Point::getXValue));

            // Создаем массивы X и Y значений
            double[] xValues = originalPoints.stream().mapToDouble(Point::getXValue).toArray();
            double[] yValues = originalPoints.stream().mapToDouble(Point::getYValue).toArray();

            // Создаем ArrayTabulatedFunction
            ArrayTabulatedFunction originalFunction = new ArrayTabulatedFunction(xValues, yValues);

            // Определяем шаг для дифференцирования
            double step = xValues.length > 1 ? xValues[1] - xValues[0] : 1.0;
            LeftSteppingDifferentialOperator diffOperator = new LeftSteppingDifferentialOperator(step);

            // Применяем оператор дифференцирования
            MathFunction differentiatedFunction = diffOperator.derive(originalFunction);

            // Создаем новую функцию
            Function newFunction = new Function();
            newFunction.setName("Дифференцирование " + functionId);
            newFunction.setSignature("d" + functionId + "/dx");
            newFunction.setUserId(function.getUserId());
            Function savedFunction = functionDAO.insert(newFunction);
            if (savedFunction == null) {
                logger.error("Не удалось создать функцию для результата дифференцирования");
                handleError(resp, 500, "Не удалось создать функцию для результата дифференцирования",
                        "/api/v1/points/differential/" + functionId);
                return;
            }

            // Создаем точки для дифференцированной функции (пропускаем первую точку)
            List<Point> diffPoints = new ArrayList<>();
            for (int i = 1; i < originalFunction.getCount(); i++) {
                double x = originalFunction.getX(i);
                double y = differentiatedFunction.apply(x);

                Point point = new Point();
                point.setFunctionId(savedFunction.getId());
                point.setXValue(x);
                point.setYValue(y);
                diffPoints.add(point);
            }

            // Сохраняем точки пакетно
            pointDAO.insertBatch(diffPoints);

            // Формируем ответ
            Map<String, Object> response = new HashMap<>();
            response.put("dfunctionId", functionId);
            response.put("functionId", savedFunction.getId());

            List<Map<String, Double>> pointsList = diffPoints.stream()
                    .map(point -> {
                        Map<String, Double> pointMap = new HashMap<>();
                        pointMap.put("xvalue", point.getXValue());
                        pointMap.put("yvalue", point.getYValue());
                        return pointMap;
                    })
                    .collect(Collectors.toList());

            response.put("points", pointsList);

            logger.info("Успешно выполнено дифференцирование функции {}. Создана новая функция с ID: {}",
                    functionId, savedFunction.getId());

            writeJson(resp, 200, response);

        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID функции: {}", functionIdStr, e);
            handleError(resp, 400, "Неверный формат ID функции", "/api/v1/points/differential/" + functionIdStr);
        } catch (Exception e) {
            logger.error("Ошибка при дифференцировании функции с ID {}: {}", functionIdStr, e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", "/api/v1/points/differential/" + functionIdStr);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();
        try {
            // Проверка аутентификации для всех DELETE запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleDeletePointById(pathInfo.substring(1), req, resp);
            } else if (pathInfo != null && pathInfo.startsWith("/function/")) {
                handleDeletePointsByFunctionId(pathInfo.substring("/function/".length()), req, resp);
            } else {
                handleError(resp, 400, "Неверный путь", req.getRequestURI());
            }
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("DELETE запрос обработан за {} мс", executionTime);
        } catch (Exception e) {
            logger.error("Ошибка при обработке DELETE запроса: {}", e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", req.getRequestURI());
        }
    }

    private void handleGetAllPoints(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение списка всех точек");
        String sortField = req.getParameter("sortField");
        boolean ascending = getBooleanParam(req, "ascending", true);

        User currentUser = AuthorizationService.getCurrentUser(req);

        List<Point> points;
        if (AuthorizationService.isAdmin(req)) {
            points = pointDAO.findAll();
            logger.debug("Администратор {} запросил все точки", currentUser.getLogin());
        } else {
            // Обычные пользователи видят только точки своих функций
            List<Function> userFunctions = functionDAO.findByUserId(currentUser.getId());
            List<Integer> functionIds = userFunctions.stream()
                    .map(Function::getId)
                    .collect(Collectors.toList());

            points = new ArrayList<>();
            for (Integer functionId : functionIds) {
                points.addAll(pointDAO.findByFunctionId(functionId));
            }
            logger.debug("Пользователь {} запросил точки своих {} функций",
                    currentUser.getLogin(), userFunctions.size());
        }

        logger.debug("Получено {} точек из базы данных", points.size());
        List<PointDTO> pointDTOs = points.stream()
                .map(PointMapper::toDTO)
                .collect(Collectors.toList());
        SortingUtils.sortPoints(pointDTOs, sortField, ascending);
        writeJson(resp, 200, pointDTOs);
        logger.info("Отправлен список точек. Количество: {}", pointDTOs.size());
    }

    private void handleGetPointById(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение точки по ID: {}", idStr);
        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "точка");

            Optional<Point> pointOpt = pointDAO.findById(id);
            if (!pointOpt.isPresent()) {
                logger.warn("Точка не найдена по ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
                return;
            }

            Point point = pointOpt.get();
            Optional<Function> functionOpt = functionDAO.findById(point.getFunctionId());

            if (!functionOpt.isPresent()) {
                logger.error("Функция для точки ID {} не найдена", id);
                handleError(resp, 404, "Функция для точки не найдена", "/api/v1/points/" + id);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается получить доступ к точке функции другого пользователя (ID функции: {})",
                        currentUser.getLogin(), function.getId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для доступа к этой точке");
                return;
            }

            PointDTO pointDTO = PointMapper.toDTO(point);
            writeJson(resp, 200, pointDTO);
            logger.info("Найдена точка по ID: {}", id);
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/points/" + idStr);
        }
    }

    private void handleGetPointsByFunctionId(String functionIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение точек для функции с ID: {}", functionIdStr);
        try {
            int functionId = Integer.parseInt(functionIdStr);
            ValidationUtils.validateId(functionId, "функция");

            Optional<Function> functionOpt = functionDAO.findById(functionId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена по ID: {}", functionId);
                handleError(resp, 404, "Функция не найдена", "/api/v1/points/function/" + functionId);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается получить точки функции другого пользователя (ID: {})",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для доступа к точкам этой функции");
                return;
            }

            String sortField = req.getParameter("sortField");
            boolean ascending = getBooleanParam(req, "ascending", true);
            List<Point> points = pointDAO.findByFunctionId(functionId);
            logger.debug("Найдено {} точек для функции ID {}", points.size(), functionId);
            List<PointDTO> pointDTOs = points.stream()
                    .map(PointMapper::toDTO)
                    .collect(Collectors.toList());
            SortingUtils.sortPoints(pointDTOs, sortField, ascending);
            writeJson(resp, 200, pointDTOs);
            logger.info("Отправлен список точек для функции ID {}. Количество: {}", functionId, pointDTOs.size());
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID функции: {}", functionIdStr, e);
            handleError(resp, 400, "Неверный формат ID функции", "/api/v1/points/function/" + functionIdStr);
        }
    }

    private void handleCreatePoint(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Создание новой точки");
        try {
            PointDTO pointDTO = objectMapper.readValue(req.getInputStream(), PointDTO.class);
            logger.debug("Получены данные для создания точки: {}", pointDTO);
            ValidationUtils.validatePointDTO(pointDTO);

            Optional<Function> functionOpt = functionDAO.findById(pointDTO.getFunctionId());
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для создания точки с ID: {}", pointDTO.getFunctionId());
                handleError(resp, 404, "Функция не найдена", "/api/v1/points");
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается создать точку для функции другого пользователя (ID: {})",
                        currentUser.getLogin(), pointDTO.getFunctionId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для создания точки в этой функции");
                return;
            }

            // Проверка существования точки с такой же координатой X
            if (pointDAO.existsByFunctionIdAndX(function.getId(), pointDTO.getXValue())) {
                logger.warn("Точка с X={} уже существует для функции ID {}", pointDTO.getXValue(), function.getId());
                handleError(resp, 409, "Точка с такой координатой X уже существует для этой функции", "/api/v1/points");
                return;
            }

            Point point = PointMapper.toEntity(pointDTO);
            Point savedPoint = pointDAO.insert(point);
            if (savedPoint != null && savedPoint.getId() != null) {
                PointDTO savedPointDTO = PointMapper.toDTO(savedPoint);
                writeJson(resp, 201, savedPointDTO);
                logger.info("Создана новая точка с ID: {} для функции ID {} пользователем {}",
                        savedPoint.getId(), function.getId(), currentUser.getLogin());
            } else {
                logger.error("Не удалось создать точку");
                handleError(resp, 400, "Не удалось создать точку", "/api/v1/points");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при создании точки: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/points");
        } catch (Exception e) {
            logger.error("Ошибка при создании точки: {}", e.getMessage(), e);
            handleError(resp, 500, "Ошибка при создании точки", "/api/v1/points");
        }
    }

    private void handleCreatePointsBatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Массовое создание точек");
        try {
            BatchPointsRequest request = objectMapper.readValue(req.getInputStream(), BatchPointsRequest.class);
            logger.debug("Получены данные для массового создания точек: {}", request);

            Optional<Function> functionOpt = functionDAO.findById(request.getFunctionId());
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для массового создания точек с ID: {}", request.getFunctionId());
                handleError(resp, 404, "Функция не найдена", "/api/v1/points/batch");
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается создать точки для функции другого пользователя (ID: {})",
                        currentUser.getLogin(), request.getFunctionId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для создания точек в этой функции");
                return;
            }

            if (request.getPoints() == null || request.getPoints().isEmpty()) {
                logger.error("Список точек для создания пустой");
                handleError(resp, 400, "Список точек для создания пустой", "/api/v1/points/batch");
                return;
            }

            List<Point> points = new ArrayList<>();
            for (PointCoordinates coords : request.getPoints()) {
                // Проверка существования точки с такой же координатой X
                if (pointDAO.existsByFunctionIdAndX(function.getId(), coords.getXValue())) {
                    logger.warn("Точка с X={} уже существует для функции ID {}", coords.getXValue(), function.getId());
                    handleError(resp, 409, "Точка с координатой X=" + coords.getXValue() + " уже существует для этой функции", "/api/v1/points/batch");
                    return;
                }

                Point point = new Point();
                point.setFunctionId(function.getId());
                point.setXValue(coords.getXValue());
                point.setYValue(coords.getYValue());
                points.add(point);
            }

            int insertedCount = pointDAO.insertBatch(points);
            if (insertedCount > 0) {
                // Получаем все точки для этой функции после вставки
                List<Point> allPoints = pointDAO.findByFunctionId(function.getId());
                List<PointDTO> result = allPoints.stream()
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());
                writeJson(resp, 201, result);
                logger.info("Успешно создано {} точек для функции ID {} пользователем {}",
                        insertedCount, function.getId(), currentUser.getLogin());
            } else {
                logger.error("Не удалось создать точки");
                handleError(resp, 400, "Не удалось создать точки", "/api/v1/points/batch");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при массовом создании точек: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/points/batch");
        } catch (Exception e) {
            logger.error("Ошибка при массовом создании точек: {}", e.getMessage(), e);
            handleError(resp, 500, "Ошибка при массовом создании точек", "/api/v1/points/batch");
        }
    }

    private void handleUpdatePoint(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Обновление точки с ID: {}", idStr);
        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "точка");

            Optional<Point> pointOpt = pointDAO.findById(id);
            if (!pointOpt.isPresent()) {
                logger.warn("Точка не найдена для обновления с ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
                return;
            }

            Point existingPoint = pointOpt.get();
            Optional<Function> functionOpt = functionDAO.findById(existingPoint.getFunctionId());

            if (!functionOpt.isPresent()) {
                logger.error("Функция для точки ID {} не найдена", id);
                handleError(resp, 404, "Функция для точки не найдена", "/api/v1/points/" + id);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на обновление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается обновить точку функции другого пользователя (ID функции: {})",
                        currentUser.getLogin(), function.getId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для обновления этой точки");
                return;
            }

            PointDTO pointDTO = objectMapper.readValue(req.getInputStream(), PointDTO.class);
            logger.debug("Получены данные для обновления точки ID {}: {}", id, pointDTO);
            pointDTO.setId(id); // Устанавливаем ID из пути
            pointDTO.setFunctionId(existingPoint.getFunctionId()); // Сохраняем оригинальный ID функции

            ValidationUtils.validatePointDTO(pointDTO);

            // Проверка существования точки с такой же координатой X для другой точки
            if (!pointDTO.getXValue().equals(existingPoint.getXValue()) &&
                    pointDAO.existsByFunctionIdAndX(function.getId(), pointDTO.getXValue())) {
                logger.warn("Точка с X={} уже существует для функции ID {}", pointDTO.getXValue(), function.getId());
                handleError(resp, 409, "Точка с такой координатой X уже существует для этой функции", "/api/v1/points/" + id);
                return;
            }

            Point point = PointMapper.toEntity(pointDTO);
            if (pointDAO.update(point)) {
                writeJson(resp, 200, pointDTO);
                logger.info("Точка с ID {} успешно обновлена пользователем {}",
                        id, currentUser.getLogin());
            } else {
                logger.warn("Точка не найдена для обновления с ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/points/" + idStr);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при обновлении точки: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/points/" + idStr);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении точки с ID {}: {}", idStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при обновлении точки", "/api/v1/points/" + idStr);
        }
    }

    private void handleDeletePointById(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление точки по ID: {}", idStr);
        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "точка");

            Optional<Point> pointOpt = pointDAO.findById(id);
            if (!pointOpt.isPresent()) {
                logger.warn("Точка не найдена для удаления с ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
                return;
            }

            Point point = pointOpt.get();
            Optional<Function> functionOpt = functionDAO.findById(point.getFunctionId());

            if (!functionOpt.isPresent()) {
                logger.error("Функция для точки ID {} не найдена", id);
                handleError(resp, 404, "Функция для точки не найдена", "/api/v1/points/" + id);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на удаление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается удалить точку функции другого пользователя (ID функции: {})",
                        currentUser.getLogin(), function.getId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для удаления этой точки");
                return;
            }

            if (pointDAO.delete(id)) {
                resp.setStatus(204); // No Content
                logger.info("Точка с ID {} успешно удалена пользователем {}",
                        id, currentUser.getLogin());
            } else {
                logger.warn("Точка не найдена для удаления с ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/points/" + idStr);
        } catch (Exception e) {
            logger.error("Ошибка при удалении точки с ID {}: {}", idStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при удалении точки", "/api/v1/points/" + idStr);
        }
    }

    private void handleDeletePointsByFunctionId(String functionIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление всех точек для функции с ID: {}", functionIdStr);
        try {
            int functionId = Integer.parseInt(functionIdStr);
            ValidationUtils.validateId(functionId, "функция");

            Optional<Function> functionOpt = functionDAO.findById(functionId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для удаления точек с ID: {}", functionId);
                handleError(resp, 404, "Функция не найдена", "/api/v1/points/function/" + functionId);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на удаление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается удалить точки функции другого пользователя (ID: {})",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для удаления точек этой функции");
                return;
            }

            if (pointDAO.deleteByFunctionId(functionId)) {
                resp.setStatus(204); // No Content
                logger.info("Все точки для функции ID {} успешно удалены пользователем {}",
                        functionId, currentUser.getLogin());
            } else {
                logger.error("Ошибка при удалении точек для функции ID {}", functionId);
                handleError(resp, 500, "Ошибка при удалении точек для функции", "/api/v1/points/function/" + functionId);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID функции: {}", functionIdStr, e);
            handleError(resp, 400, "Неверный формат ID функции", "/api/v1/points/function/" + functionIdStr);
        } catch (Exception e) {
            logger.error("Ошибка при удалении точек для функции ID {}: {}", functionIdStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при удалении точек для функции", "/api/v1/points/function/" + functionIdStr);
        }
    }

    private void handleBatchSearchByIds(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Множественный поиск точек по IDs");
        try {
            IdsRequest request = objectMapper.readValue(req.getInputStream(), IdsRequest.class);
            logger.debug("Получены ID для поиска: {}", request.getIds());
            ValidationUtils.validateIds(request.getIds(), "точка");

            User currentUser = AuthorizationService.getCurrentUser(req);

            // Получаем все точки по ID
            List<Point> allPoints = new ArrayList<>();
            for (Integer id : request.getIds()) {
                pointDAO.findById(id).ifPresent(allPoints::add);
            }

            // Фильтруем точки, к которым у пользователя есть доступ
            List<Point> accessiblePoints = new ArrayList<>();
            for (Point point : allPoints) {
                Optional<Function> functionOpt = functionDAO.findById(point.getFunctionId());
                if (functionOpt.isPresent()) {
                    Function function = functionOpt.get();
                    if (AuthorizationService.isAdmin(req) ||
                            currentUser.getId().equals(function.getUserId())) {
                        accessiblePoints.add(point);
                    }
                }
            }

            // Создаем DTO только для доступных точек
            List<PointDTO> result = accessiblePoints.stream()
                    .map(PointMapper::toDTO)
                    .collect(Collectors.toList());

            // Логируем результат
            if (allPoints.size() != accessiblePoints.size()) {
                logger.warn("Доступны только {} из {} запрошенных точек для пользователя {}",
                        accessiblePoints.size(), allPoints.size(), currentUser.getLogin());
            }

            writeJson(resp, 200, result);
            logger.info("Отправлено {} точек из {} запрошенных для пользователя {}",
                    result.size(), request.getIds().size(), currentUser.getLogin());
        } catch (Exception e) {
            logger.error("Ошибка при множественном поиске точек: {}", e.getMessage(), e);
            handleError(resp, 400, "Ошибка при поиске точек", "/api/v1/points/batch/search-by-ids");
        }
    }

    // Вспомогательные классы для массовых операций
    private static class IdsRequest {
        private List<Integer> ids;
        public List<Integer> getIds() {
            return ids;
        }
        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

    private static class BatchPointsRequest {
        private Integer functionId;
        private List<PointCoordinates> points;
        public Integer getFunctionId() {
            return functionId;
        }
        public void setFunctionId(Integer functionId) {
            this.functionId = functionId;
        }
        public List<PointCoordinates> getPoints() {
            return points;
        }
        public void setPoints(List<PointCoordinates> points) {
            this.points = points;
        }
    }

    private static class PointCoordinates {
        private Double xValue;
        private Double yValue;
        public Double getXValue() {
            return xValue;
        }
        public void setXValue(Double xValue) {
            this.xValue = xValue;
        }
        public Double getYValue() {
            return yValue;
        }
        public void setYValue(Double yValue) {
            this.yValue = yValue;
        }
    }
    // Новый метод для массового обновления точек
    private void handleUpdatePointsBatch(String functionIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.info("Запрос на массовое обновление точек для функции: {}", functionIdStr);

        try {
            int functionId = Integer.parseInt(functionIdStr);
            ValidationUtils.validateId(functionId, "функция");

            User currentUser = AuthorizationService.getCurrentUser(req);
            logger.info("Запрос на массовое обновление точек для функции {} пользователем: {}",
                    functionId, currentUser.getLogin());

            // Проверка доступа к функции
            Optional<Function> functionOpt = functionDAO.findById(functionId);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция с ID {} не найдена", functionId);
                handleError(resp, 404, "Функция не найдена", "/api/v1/points/update/batch/" + functionId);
                return;
            }

            Function function = functionOpt.get();
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается обновлять точки чужой функции {}",
                        currentUser.getLogin(), functionId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступ запрещен к этой функции");
                return;
            }

            // Чтение тела запроса
            UpdatePointsBatchRequest request = objectMapper.readValue(req.getInputStream(), UpdatePointsBatchRequest.class);

            // Проверяем, что functionId в пути совпадает с functionId в теле запроса
            if (functionId != request.getFunctionId()) {
                logger.warn("Несоответствие functionId в пути ({}) и в теле ({})",
                        functionId, request.getFunctionId());
                handleError(resp, 400, "ID функции в пути и в теле запроса не совпадают",
                        "/api/v1/points/update/batch/" + functionId);
                return;
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
                handleError(resp, 400, "Обнаружены дублирующиеся X значения: " + duplicateXValues,
                        "/api/v1/points/update/batch/" + functionId);
                return;
            }

            // Проверяем, не заняты ли новые X значения другими точками этой функции
            List<Double> newXValues = request.getPoints().stream()
                    .map(UpdatePointCoordinate::getXValue)
                    .collect(Collectors.toList());

            // Получаем существующие точки с такими X значениями (исключая обновляемые точки)
            List<Integer> updatingPointIds = request.getPoints().stream()
                    .map(UpdatePointCoordinate::getId)
                    .collect(Collectors.toList());

            List<Point> conflictingPoints = pointDAO.findByFunctionIdAndXInAndIdNotIn(
                    functionId, newXValues, updatingPointIds);

            if (!conflictingPoints.isEmpty()) {
                List<Double> conflictingXValues = conflictingPoints.stream()
                        .map(Point::getXValue)
                        .collect(Collectors.toList());
                logger.warn("Конфликтующие X значения: {}", conflictingXValues);
                handleError(resp, 400, "Точки с такими X значениями уже существуют: " + conflictingXValues,
                        "/api/v1/points/update/batch/" + functionId);
                return;
            }

            // Выполняем массовое обновление
            List<Point> updatedPoints = pointDAO.updatePointsBatch(functionId, request.getPoints());
            List<PointDTO> pointDTOs = updatedPoints.stream()
                    .map(PointMapper::toDTO)
                    .collect(Collectors.toList());

            logger.info("Успешно обновлено {} точек для функции {}", pointDTOs.size(), functionId);

            writeJson(resp, 200, pointDTOs);

        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID функции: {}", functionIdStr, e);
            handleError(resp, 400, "Неверный формат ID функции", "/api/v1/points/update/batch/" + functionIdStr);
        } catch (IOException e) {
            logger.error("Ошибка при чтении тела запроса: {}", e.getMessage(), e);
            handleError(resp, 400, "Ошибка при чтении тела запроса", "/api/v1/points/update/batch/" + functionIdStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка при массовом обновлении точек: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/points/update/batch/" + functionIdStr);
        } catch (Exception e) {
            logger.error("Ошибка при массовом обновлении точек для функции {}: {}",
                    functionIdStr, e.getMessage(), e);
            handleError(resp, 500, "Внутренняя ошибка сервера", "/api/v1/points/update/batch/" + functionIdStr);
        }
    }

    // Вспомогательные классы для массового обновления
    private static class UpdatePointsBatchRequest {
        private Integer functionId;
        private List<UpdatePointCoordinate> points;

        // Getters и setters
        public Integer getFunctionId() { return functionId; }
        public void setFunctionId(Integer functionId) { this.functionId = functionId; }
        public List<UpdatePointCoordinate> getPoints() { return points; }
        public void setPoints(List<UpdatePointCoordinate> points) { this.points = points; }
    }

    public static class UpdatePointCoordinate {
        private Integer id;
        private Double xValue;
        private Double yValue;

        // Getters и setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public Double getXValue() { return xValue; }
        public void setXValue(Double xValue) { this.xValue = xValue; }
        public Double getYValue() { return yValue; }
        public void setYValue(Double yValue) { this.yValue = yValue; }
    }

    private static class TabulatedPointDTO {
        private double x;
        private double y;

        public TabulatedPointDTO(double x, double y) {
            this.x = x;
            this.y = y;
        }

        // Getters
        public double getX() { return x; }
        public double getY() { return y; }
    }

    private static class TabulatedFunctionResponse {
        private List<TabulatedPointDTO> points;

        public TabulatedFunctionResponse(List<TabulatedPointDTO> points) {
            this.points = points;
        }

        // Getters
        public List<TabulatedPointDTO> getPoints() { return points; }
    }
}