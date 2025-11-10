package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.jdbc.PointDAO;
import ru.ssau.tk.cheefkeef.laba2.mapper.PointMapper;
import ru.ssau.tk.cheefkeef.laba2.models.Point;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllPoints(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetPointById(pathInfo.substring(1), resp);
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
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleDeletePointById(pathInfo.substring(1), resp);
            } else if (pathInfo != null && pathInfo.startsWith("/function/")) {
                handleDeletePointsByFunctionId(pathInfo.substring("/function/".length()), resp);
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

        List<Point> points = pointDAO.findAll();
        logger.debug("Получено {} точек из базы данных", points.size());

        List<PointDTO> pointDTOs = points.stream()
                .map(PointMapper::toDTO)
                .collect(Collectors.toList());

        SortingUtils.sortPoints(pointDTOs, sortField, ascending);

        writeJson(resp, 200, pointDTOs);
        logger.info("Отправлен список всех точек. Количество: {}", pointDTOs.size());
    }

    private void handleGetPointById(String idStr, HttpServletResponse resp) throws IOException {
        logger.debug("Получение точки по ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "точка");

            Optional<Point> pointOpt = pointDAO.findById(id);
            if (pointOpt.isPresent()) {
                PointDTO pointDTO = PointMapper.toDTO(pointOpt.get());
                writeJson(resp, 200, pointDTO);
                logger.info("Найдена точка по ID: {}", id);
            } else {
                logger.warn("Точка не найдена по ID: {}", id);
                handleError(resp, 404, "Точка не найдена", "/api/v1/points/" + id);
            }
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

            Point point = PointMapper.toEntity(pointDTO);
            Point savedPoint = pointDAO.insert(point);

            if (savedPoint != null && savedPoint.getId() != null) {
                PointDTO savedPointDTO = PointMapper.toDTO(savedPoint);
                writeJson(resp, 201, savedPointDTO);
                logger.info("Создана новая точка с ID: {}", savedPoint.getId());
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

            ValidationUtils.validateId(request.getFunctionId(), "функция");

            if (request.getPoints() == null || request.getPoints().isEmpty()) {
                logger.error("Список точек для создания пустой");
                handleError(resp, 400, "Список точек для создания пустой", "/api/v1/points/batch");
                return;
            }

            List<Point> points = new ArrayList<>();
            for (PointCoordinates coords : request.getPoints()) {
                Point point = new Point();
                point.setFunctionId(request.getFunctionId());
                point.setXValue(coords.getXValue());
                point.setYValue(coords.getYValue());
                points.add(point);
            }

            int insertedCount = pointDAO.insertBatch(points);
            if (insertedCount > 0) {
                // Получаем все точки для этой функции после вставки
                List<Point> allPoints = pointDAO.findByFunctionId(request.getFunctionId());
                List<PointDTO> result = allPoints.stream()
                        .map(PointMapper::toDTO)
                        .collect(Collectors.toList());

                writeJson(resp, 201, result);
                logger.info("Успешно создано {} точек для функции ID {}", insertedCount, request.getFunctionId());
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

            PointDTO pointDTO = objectMapper.readValue(req.getInputStream(), PointDTO.class);
            logger.debug("Получены данные для обновления точки ID {}: {}", id, pointDTO);

            pointDTO.setId(id); // Устанавливаем ID из пути
            ValidationUtils.validatePointDTO(pointDTO);

            Point point = PointMapper.toEntity(pointDTO);
            if (pointDAO.update(point)) {
                writeJson(resp, 200, pointDTO);
                logger.info("Точка с ID {} успешно обновлена", id);
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

    private void handleDeletePointById(String idStr, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление точки по ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "точка");

            if (pointDAO.delete(id)) {
                resp.setStatus(204); // No Content
                logger.info("Точка с ID {} успешно удалена", id);
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

    private void handleDeletePointsByFunctionId(String functionIdStr, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление всех точек для функции с ID: {}", functionIdStr);

        try {
            int functionId = Integer.parseInt(functionIdStr);
            ValidationUtils.validateId(functionId, "функция");

            if (pointDAO.deleteByFunctionId(functionId)) {
                resp.setStatus(204); // No Content
                logger.info("Все точки для функции ID {} успешно удалены", functionId);
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

            List<PointDTO> result = new ArrayList<>();
            for (Integer id : request.getIds()) {
                pointDAO.findById(id).ifPresent(point ->
                        result.add(PointMapper.toDTO(point))
                );
            }

            writeJson(resp, 200, result);
            logger.info("Найдено {} точек из {} запрошенных", result.size(), request.getIds().size());
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