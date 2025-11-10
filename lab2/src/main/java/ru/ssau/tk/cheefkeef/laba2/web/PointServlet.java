package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.auth.AuthorizationService;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.jdbc.FunctionDAO;
import ru.ssau.tk.cheefkeef.laba2.jdbc.PointDAO;
import ru.ssau.tk.cheefkeef.laba2.mapper.PointMapper;
import ru.ssau.tk.cheefkeef.laba2.models.Function;
import ru.ssau.tk.cheefkeef.laba2.models.Point;
import ru.ssau.tk.cheefkeef.laba2.models.User;
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
}