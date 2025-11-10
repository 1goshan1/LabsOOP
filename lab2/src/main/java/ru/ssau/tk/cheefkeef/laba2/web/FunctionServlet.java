package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.UserIdsRequest;
import ru.ssau.tk.cheefkeef.laba2.jdbc.FunctionDAO;
import ru.ssau.tk.cheefkeef.laba2.mapper.FunctionMapper;
import ru.ssau.tk.cheefkeef.laba2.models.Function;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionServlet.class);
    private final FunctionDAO functionDAO = new FunctionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logRequest(req);
        long startTime = System.currentTimeMillis();

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAllFunctions(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetFunctionById(pathInfo.substring(1), resp);
            } else if (pathInfo.equals("/search")) {
                handleSearchFunctions(req, resp);
            } else if (pathInfo.startsWith("/search/by-name/")) {
                handleSearchByName(pathInfo.substring("/search/by-name/".length()), req, resp);
            } else if (pathInfo.startsWith("/search/by-user/")) {
                handleSearchByUserId(pathInfo.substring("/search/by-user/".length()), req, resp);
            } else if (pathInfo.equals("/search/by-user-and-name")) {
                handleSearchByUserAndName(req, resp);
            } else if (pathInfo.startsWith("/users/") && pathInfo.endsWith("/count")) {
                handleGetFunctionCountForUser(pathInfo, resp);
            } else if (pathInfo.equals("/exists")) {
                handleCheckFunctionExists(req, resp);
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
                handleCreateFunction(req, resp);
            } else if (pathInfo.equals("/batch/search-by-ids")) {
                handleBatchSearchByIds(req, resp);
            } else if (pathInfo.equals("/batch/search-by-user-ids")) {
                handleBatchSearchByUserIds(req, resp);
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
                handleUpdateFunction(pathInfo.substring(1), req, resp);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/signature")) {
                handleUpdateSignature(pathInfo.substring(1, pathInfo.lastIndexOf('/')), req, resp);
            } else {
                handleError(resp, 400, "Неверный путь", req.getRequestURI());
            }

            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("PUT/PATCH запрос обработан за {} мс", executionTime);
        } catch (Exception e) {
            logger.error("Ошибка при обработке PUT/PATCH запроса: {}", e.getMessage(), e);
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
                handleDeleteFunctionById(pathInfo.substring(1), resp);
            } else if (pathInfo != null && pathInfo.startsWith("/search/by-user/")) {
                handleDeleteFunctionsByUserId(pathInfo.substring("/search/by-user/".length()), resp);
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

    private void handleGetAllFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение списка всех функций");

        String sortField = req.getParameter("sortField");
        boolean ascending = getBooleanParam(req, "ascending", true);

        List<Function> functions = functionDAO.findAll();
        logger.debug("Получено {} функций из базы данных", functions.size());

        List<FunctionDTO> functionDTOs = functions.stream()
                .map(FunctionMapper::toDTO)
                .collect(Collectors.toList());

        SortingUtils.sortFunctions(functionDTOs, sortField, ascending);

        writeJson(resp, 200, functionDTOs);
        logger.info("Отправлен список всех функций. Количество: {}", functionDTOs.size());
    }

    private void handleGetFunctionById(String idStr, HttpServletResponse resp) throws IOException {
        logger.debug("Получение функции по ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            Optional<Function> functionOpt = functionDAO.findById(id);
            if (functionOpt.isPresent()) {
                FunctionDTO functionDTO = FunctionMapper.toDTO(functionOpt.get());
                writeJson(resp, 200, functionDTO);
                logger.info("Найдена функция по ID: {}", id);
            } else {
                logger.warn("Функция не найдена по ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/functions/" + idStr);
        }
    }

    private void handleSearchFunctions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Расширенный поиск функций");

        Integer userId = getIntegerParam(req, "userId");
        String namePattern = req.getParameter("namePattern");
        String sortField = req.getParameter("sortField");
        boolean ascending = getBooleanParam(req, "ascending", true);

        List<Function> functions;
        if (userId != null && namePattern != null) {
            functions = functionDAO.findByNameAndUserId(namePattern, userId);
            logger.debug("Найдено {} функций по имени '{}' и пользователю ID {}", functions.size(), namePattern, userId);
        } else if (userId != null) {
            functions = functionDAO.findByUserId(userId);
            logger.debug("Найдено {} функций для пользователя ID {}", functions.size(), userId);
        } else if (namePattern != null) {
            functions = functionDAO.findByName(namePattern);
            logger.debug("Найдено {} функций по имени '{}'", functions.size(), namePattern);
        } else {
            functions = functionDAO.findAll();
            logger.debug("Найдено {} функций при общем поиске", functions.size());
        }

        List<FunctionDTO> functionDTOs = functions.stream()
                .map(FunctionMapper::toDTO)
                .collect(Collectors.toList());

        SortingUtils.sortFunctions(functionDTOs, sortField, ascending);

        writeJson(resp, 200, functionDTOs);
        logger.info("Отправлен результат расширенного поиска. Количество: {}", functionDTOs.size());
    }

    private void handleSearchByName(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Поиск функций по имени: {}", name);

        if (name == null || name.trim().isEmpty()) {
            logger.error("Имя функции не может быть пустым");
            handleError(resp, 400, "Имя функции не может быть пустым", "/api/v1/functions/search/by-name/" + name);
            return;
        }

        String sortField = req.getParameter("sortField");
        boolean ascending = getBooleanParam(req, "ascending", true);

        List<Function> functions = functionDAO.findByName(name);
        logger.debug("Найдено {} функций по имени '{}'", functions.size(), name);

        List<FunctionDTO> functionDTOs = functions.stream()
                .map(FunctionMapper::toDTO)
                .collect(Collectors.toList());

        SortingUtils.sortFunctions(functionDTOs, sortField, ascending);

        writeJson(resp, 200, functionDTOs);
        logger.info("Отправлен список функций по имени '{}'. Количество: {}", name, functionDTOs.size());
    }

    private void handleSearchByUserId(String userIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Поиск функций по ID пользователя: {}", userIdStr);

        try {
            int userId = Integer.parseInt(userIdStr);
            ValidationUtils.validateId(userId, "пользователь");

            String sortField = req.getParameter("sortField");
            boolean ascending = getBooleanParam(req, "ascending", true);

            List<Function> functions = functionDAO.findByUserId(userId);
            logger.debug("Найдено {} функций для пользователя ID {}", functions.size(), userId);

            List<FunctionDTO> functionDTOs = functions.stream()
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());

            SortingUtils.sortFunctions(functionDTOs, sortField, ascending);

            writeJson(resp, 200, functionDTOs);
            logger.info("Отправлен список функций для пользователя ID {}. Количество: {}", userId, functionDTOs.size());
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID пользователя: {}", userIdStr, e);
            handleError(resp, 400, "Неверный формат ID пользователя", "/api/v1/functions/search/by-user/" + userIdStr);
        }
    }

    private void handleSearchByUserAndName(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Поиск функций по имени и ID пользователя");

        Integer userId = getIntegerParam(req, "userId");
        String name = req.getParameter("name");

        if (userId == null || userId <= 0) {
            logger.error("Неверный ID пользователя для поиска");
            handleError(resp, 400, "ID пользователя обязателен и должен быть положительным", "/api/v1/functions/search/by-user-and-name");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            logger.error("Имя функции не может быть пустым");
            handleError(resp, 400, "Имя функции не может быть пустым", "/api/v1/functions/search/by-user-and-name");
            return;
        }

        List<Function> functions = functionDAO.findByNameAndUserId(name, userId);
        logger.debug("Найдено {} функций по имени '{}' и пользователю ID {}", functions.size(), name, userId);

        List<FunctionDTO> functionDTOs = functions.stream()
                .map(FunctionMapper::toDTO)
                .collect(Collectors.toList());

        writeJson(resp, 200, functionDTOs);
        logger.info("Отправлен результат поиска функций по имени '{}' и пользователю ID {}. Количество: {}", name, userId, functionDTOs.size());
    }

    private void handleGetFunctionCountForUser(String pathInfo, HttpServletResponse resp) throws IOException {
        logger.debug("Получение количества функций для пользователя");

        try {
            String userIdStr = pathInfo.substring("/users/".length(), pathInfo.length() - "/count".length());
            int userId = Integer.parseInt(userIdStr);
            ValidationUtils.validateId(userId, "пользователь");

            int count = functionDAO.countByUserId(userId);
            logger.info("Найдено {} функций для пользователя ID {}", count, userId);

            writeJson(resp, 200, Map.of("count", count));
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID пользователя в пути: {}", pathInfo, e);
            handleError(resp, 400, "Неверный формат ID пользователя", "/api/v1/functions" + pathInfo);
        }
    }

    private void handleCheckFunctionExists(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Проверка существования функции");

        Integer userId = getIntegerParam(req, "userId");
        String name = req.getParameter("name");

        if (userId == null || userId <= 0) {
            logger.error("Неверный ID пользователя для проверки существования функции");
            handleError(resp, 400, "ID пользователя обязателен и должен быть положительным", "/api/v1/functions/exists");
            return;
        }

        if (name == null || name.trim().isEmpty()) {
            logger.error("Имя функции не может быть пустым");
            handleError(resp, 400, "Имя функции не может быть пустым", "/api/v1/functions/exists");
            return;
        }

        boolean exists = functionDAO.existsByNameAndUserId(name, userId);
        logger.info("Функция с именем '{}' для пользователя ID {} {}: {}", name, userId, exists ? "существует" : "не существует", exists);

        writeJson(resp, 200, Map.of("exists", exists));
    }

    private void handleCreateFunction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Создание новой функции");

        try {
            FunctionDTO functionDTO = objectMapper.readValue(req.getInputStream(), FunctionDTO.class);
            logger.debug("Получены данные для создания функции: {}", functionDTO);

            ValidationUtils.validateFunctionDTO(functionDTO);

            Function function = FunctionMapper.toEntity(functionDTO);
            Function savedFunction = functionDAO.insert(function);

            if (savedFunction != null && savedFunction.getId() != null) {
                FunctionDTO savedFunctionDTO = FunctionMapper.toDTO(savedFunction);
                writeJson(resp, 201, savedFunctionDTO);
                logger.info("Создана новая функция с ID: {}", savedFunction.getId());
            } else {
                logger.error("Не удалось создать функцию");
                handleError(resp, 400, "Не удалось создать функцию", "/api/v1/functions");
            }
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при создании функции: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/functions");
        } catch (Exception e) {
            logger.error("Ошибка при создании функции: {}", e.getMessage(), e);
            handleError(resp, 500, "Ошибка при создании функции", "/api/v1/functions");
        }
    }

    private void handleBatchSearchByIds(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Множественный поиск функций по IDs");

        try {
            IdsRequest request = objectMapper.readValue(req.getInputStream(), IdsRequest.class);
            logger.debug("Получены ID для поиска: {}", request.getIds());

            ValidationUtils.validateIds(request.getIds(), "функция");

            List<FunctionDTO> result = new ArrayList<>();
            for (Integer id : request.getIds()) {
                functionDAO.findById(id).ifPresent(function ->
                        result.add(FunctionMapper.toDTO(function))
                );
            }

            writeJson(resp, 200, result);
            logger.info("Найдено {} функций из {} запрошенных", result.size(), request.getIds().size());
        } catch (Exception e) {
            logger.error("Ошибка при множественном поиске функций: {}", e.getMessage(), e);
            handleError(resp, 400, "Ошибка при поиске функций", "/api/v1/functions/batch/search-by-ids");
        }
    }

    private void handleBatchSearchByUserIds(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Множественный поиск функций по ID пользователей");

        try {
            UserIdsRequest request = objectMapper.readValue(req.getInputStream(), UserIdsRequest.class);
            logger.debug("Получены ID пользователей для поиска: {}", request.getIds());

            ValidationUtils.validateIds(request.getIds(), "пользователь");

            List<Function> functions = new ArrayList<>();
            for (Integer userId : request.getIds()) {
                functions.addAll(functionDAO.findByUserId(userId));
            }

            List<FunctionDTO> result = functions.stream()
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());

            writeJson(resp, 200, result);
            logger.info("Найдено {} функций для {} пользователей", result.size(), request.getIds().size());
        } catch (Exception e) {
            logger.error("Ошибка при множественном поиске функций по ID пользователей: {}", e.getMessage(), e);
            handleError(resp, 400, "Ошибка при поиске функций", "/api/v1/functions/batch/search-by-user-ids");
        }
    }

    private void handleUpdateFunction(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Обновление функции с ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            FunctionDTO functionDTO = objectMapper.readValue(req.getInputStream(), FunctionDTO.class);
            logger.debug("Получены данные для обновления функции ID {}: {}", id, functionDTO);

            functionDTO.setId(id); // Устанавливаем ID из пути
            ValidationUtils.validateFunctionDTO(functionDTO);

            Function function = FunctionMapper.toEntity(functionDTO);
            if (functionDAO.update(function)) {
                writeJson(resp, 200, functionDTO);
                logger.info("Функция с ID {} успешно обновлена", id);
            } else {
                logger.warn("Функция не найдена для обновления с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/functions/" + idStr);
        } catch (IllegalArgumentException e) {
            logger.error("Ошибка валидации при обновлении функции: {}", e.getMessage());
            handleError(resp, 400, e.getMessage(), "/api/v1/functions/" + idStr);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении функции с ID {}: {}", idStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при обновлении функции", "/api/v1/functions/" + idStr);
        }
    }

    private void handleUpdateSignature(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Обновление сигнатуры функции с ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            SignatureUpdateRequest request = objectMapper.readValue(req.getInputStream(), SignatureUpdateRequest.class);

            if (request.getSignature() == null || request.getSignature().trim().isEmpty()) {
                logger.error("Сигнатура не может быть пустой");
                handleError(resp, 400, "Сигнатура не может быть пустой", "/api/v1/functions/" + id + "/signature");
                return;
            }

            boolean updated = functionDAO.updateSignature(id, request.getSignature());
            if (updated) {
                Optional<Function> updatedFunction = functionDAO.findById(id);
                if (updatedFunction.isPresent()) {
                    FunctionDTO functionDTO = FunctionMapper.toDTO(updatedFunction.get());
                    writeJson(resp, 200, functionDTO);
                    logger.info("Сигнатура функции с ID {} успешно обновлена", id);
                } else {
                    logger.error("Функция не найдена после обновления сигнатуры");
                    handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id + "/signature");
                }
            } else {
                logger.warn("Функция не найдена для обновления сигнатуры с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id + "/signature");
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/functions/" + idStr + "/signature");
        } catch (Exception e) {
            logger.error("Ошибка при обновлении сигнатуры функции с ID {}: {}", idStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при обновлении сигнатуры функции", "/api/v1/functions/" + idStr + "/signature");
        }
    }

    private void handleDeleteFunctionById(String idStr, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление функции по ID: {}", idStr);

        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            if (functionDAO.delete(id)) {
                resp.setStatus(204); // No Content
                logger.info("Функция с ID {} успешно удалена", id);
            } else {
                logger.warn("Функция не найдена для удаления с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID: {}", idStr, e);
            handleError(resp, 400, "Неверный формат ID", "/api/v1/functions/" + idStr);
        } catch (Exception e) {
            logger.error("Ошибка при удалении функции с ID {}: {}", idStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при удалении функции", "/api/v1/functions/" + idStr);
        }
    }

    private void handleDeleteFunctionsByUserId(String userIdStr, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление всех функций пользователя с ID: {}", userIdStr);

        try {
            int userId = Integer.parseInt(userIdStr);
            ValidationUtils.validateId(userId, "пользователь");

            if (functionDAO.deleteByUserId(userId)) {
                resp.setStatus(204); // No Content
                logger.info("Все функции пользователя с ID {} успешно удалены", userId);
            } else {
                logger.error("Не удалось удалить функции пользователя с ID {}", userId);
                handleError(resp, 500, "Ошибка при удалении функций пользователя", "/api/v1/functions/search/by-user/" + userId);
            }
        } catch (NumberFormatException e) {
            logger.error("Неверный формат ID пользователя: {}", userIdStr, e);
            handleError(resp, 400, "Неверный формат ID пользователя", "/api/v1/functions/search/by-user/" + userIdStr);
        } catch (Exception e) {
            logger.error("Ошибка при удалении функций пользователя с ID {}: {}", userIdStr, e.getMessage(), e);
            handleError(resp, 500, "Ошибка при удалении функций пользователя", "/api/v1/functions/search/by-user/" + userIdStr);
        }
    }

    // Вспомогательные классы для обработки запросов
    private static class IdsRequest {
        private List<Integer> ids;

        public List<Integer> getIds() {
            return ids;
        }

        public void setIds(List<Integer> ids) {
            this.ids = ids;
        }
    }

    private static class SignatureUpdateRequest {
        private String signature;

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }
}