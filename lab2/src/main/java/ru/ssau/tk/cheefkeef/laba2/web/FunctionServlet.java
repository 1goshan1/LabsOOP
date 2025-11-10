package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.auth.AuthorizationService;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.UserIdsRequest;
import ru.ssau.tk.cheefkeef.laba2.jdbc.FunctionDAO;
import ru.ssau.tk.cheefkeef.laba2.mapper.FunctionMapper;
import ru.ssau.tk.cheefkeef.laba2.models.Function;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebServlet("/api/v1/functions/*")
public class FunctionServlet extends BaseServlet {
    private static final Logger logger = LoggerFactory.getLogger(FunctionServlet.class);
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
                handleGetAllFunctions(req, resp);
            } else if (pathInfo.matches("/\\d+")) {
                handleGetFunctionById(pathInfo.substring(1), req, resp);
            } else if (pathInfo.equals("/search")) {
                handleSearchFunctions(req, resp);
            } else if (pathInfo.startsWith("/search/by-name/")) {
                handleSearchByName(pathInfo.substring("/search/by-name/".length()), req, resp);
            } else if (pathInfo.startsWith("/search/by-user/")) {
                handleSearchByUserId(pathInfo.substring("/search/by-user/".length()), req, resp);
            } else if (pathInfo.equals("/search/by-user-and-name")) {
                handleSearchByUserAndName(req, resp);
            } else if (pathInfo.startsWith("/users/") && pathInfo.endsWith("/count")) {
                handleGetFunctionCountForUser(pathInfo, req, resp);
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
            // Проверка аутентификации для всех POST запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }

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
            // Проверка аутентификации для всех PUT запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }

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
            // Проверка аутентификации для всех DELETE запросов
            if (!checkAccess(req, resp, null, null)) {
                return;
            }

            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.matches("/\\d+")) {
                handleDeleteFunctionById(pathInfo.substring(1), req, resp);
            } else if (pathInfo != null && pathInfo.startsWith("/search/by-user/")) {
                handleDeleteFunctionsByUserId(pathInfo.substring("/search/by-user/".length()), req, resp);
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

        List<Function> functions;
        User currentUser = AuthorizationService.getCurrentUser(req);

        if (AuthorizationService.isAdmin(req)) {
            functions = functionDAO.findAll();
            logger.info("Администратор {} запросил список всех функций", currentUser.getLogin());
        } else {
            // Обычные пользователи видят только свои функции
            functions = functionDAO.findByUserId(currentUser.getId());
            logger.info("Пользователь {} запросил список своих функций", currentUser.getLogin());
        }

        logger.debug("Получено {} функций из базы данных", functions.size());
        List<FunctionDTO> functionDTOs = functions.stream()
                .map(FunctionMapper::toDTO)
                .collect(Collectors.toList());
        SortingUtils.sortFunctions(functionDTOs, sortField, ascending);
        writeJson(resp, 200, functionDTOs);
        logger.info("Отправлен список функций. Количество: {}", functionDTOs.size());
    }

    private void handleGetFunctionById(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение функции по ID: {}", idStr);
        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            Optional<Function> functionOpt = functionDAO.findById(id);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена по ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается получить доступ к функции другого пользователя (ID: {})",
                        currentUser.getLogin(), function.getUserId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для доступа к этой функции");
                return;
            }

            FunctionDTO functionDTO = FunctionMapper.toDTO(function);
            writeJson(resp, 200, functionDTO);
            logger.info("Найдена функция по ID: {}", id);
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

        User currentUser = AuthorizationService.getCurrentUser(req);

        List<Function> functions;
        if (userId != null && namePattern != null) {
            if (AuthorizationService.isAdmin(req) || currentUser.getId().equals(userId)) {
                functions = functionDAO.findByNameAndUserId(namePattern, userId);
                logger.debug("Найдено {} функций по имени '{}' и пользователю ID {}", functions.size(), namePattern, userId);
            } else {
                logger.warn("Пользователь {} пытается найти функции другого пользователя (ID: {})",
                        currentUser.getLogin(), userId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для поиска функций другого пользователя");
                return;
            }
        } else if (userId != null) {
            if (AuthorizationService.isAdmin(req) || currentUser.getId().equals(userId)) {
                functions = functionDAO.findByUserId(userId);
                logger.debug("Найдено {} функций для пользователя ID {}", functions.size(), userId);
            } else {
                logger.warn("Пользователь {} пытается найти функции другого пользователя (ID: {})",
                        currentUser.getLogin(), userId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для поиска функций другого пользователя");
                return;
            }
        } else if (namePattern != null) {
            if (AuthorizationService.isAdmin(req)) {
                functions = functionDAO.findByName(namePattern);
                logger.debug("Найдено {} функций по имени '{}'", functions.size(), namePattern);
            } else {
                // Обычные пользователи ищут только среди своих функций
                functions = functionDAO.findByCriteria(currentUser.getId(), namePattern, "id", true);
                logger.debug("Найдено {} функций по имени '{}' для пользователя {}", functions.size(), namePattern, currentUser.getId());
            }
        } else {
            if (AuthorizationService.isAdmin(req)) {
                functions = functionDAO.findAll();
                logger.debug("Найдено {} функций при общем поиске администратором", functions.size());
            } else {
                // Обычные пользователи видят только свои функции
                functions = functionDAO.findByUserId(currentUser.getId());
                logger.debug("Найдено {} функций при общем поиске для пользователя {}", functions.size(), currentUser.getId());
            }
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

        List<Function> functions;
        User currentUser = AuthorizationService.getCurrentUser(req);

        if (AuthorizationService.isAdmin(req)) {
            functions = functionDAO.findByName(name);
            logger.debug("Найдено {} функций по имени '{}' администратором", functions.size(), name);
        } else {
            // Обычные пользователи ищут только среди своих функций
            functions = functionDAO.findByNameAndUserId(name, currentUser.getId());
            logger.debug("Найдено {} функций по имени '{}' для пользователя {}", functions.size(), name, currentUser.getId());
        }

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

            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(userId)) {
                logger.warn("Пользователь {} пытается найти функции другого пользователя с ID {}",
                        currentUser.getLogin(), userId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для поиска функций другого пользователя");
                return;
            }

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

        User currentUser = AuthorizationService.getCurrentUser(req);

        // Проверка прав доступа
        if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(userId)) {
            logger.warn("Пользователь {} пытается найти функции другого пользователя с ID {}",
                    currentUser.getLogin(), userId);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для поиска функций другого пользователя");
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

    private void handleGetFunctionCountForUser(String pathInfo, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Получение количества функций для пользователя");
        try {
            String userIdStr = pathInfo.substring("/users/".length(), pathInfo.length() - "/count".length());
            int userId = Integer.parseInt(userIdStr);
            ValidationUtils.validateId(userId, "пользователь");

            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав доступа
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(userId)) {
                logger.warn("Пользователь {} пытается получить количество функций другого пользователя с ID {}",
                        currentUser.getLogin(), userId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для получения количества функций другого пользователя");
                return;
            }

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

        User currentUser = AuthorizationService.getCurrentUser(req);

        // Проверка прав доступа
        if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(userId)) {
            logger.warn("Пользователь {} пытается проверить существование функции другого пользователя с ID {}",
                    currentUser.getLogin(), userId);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для проверки существования функции другого пользователя");
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

            User currentUser = AuthorizationService.getCurrentUser(req);
            functionDTO.setUserId(currentUser.getId()); // Принудительно устанавливаем ID текущего пользователя

            ValidationUtils.validateFunctionDTO(functionDTO);
            Function function = FunctionMapper.toEntity(functionDTO);

            Function savedFunction = functionDAO.insert(function);
            if (savedFunction != null && savedFunction.getId() != null) {
                FunctionDTO savedFunctionDTO = FunctionMapper.toDTO(savedFunction);
                writeJson(resp, 201, savedFunctionDTO);
                logger.info("Пользователь {} создал новую функцию (ID: {})",
                        currentUser.getLogin(), savedFunction.getId());
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

            User currentUser = AuthorizationService.getCurrentUser(req);

            // Получаем все функции по ID
            List<Function> allFunctions = functionDAO.findByIds(request.getIds());

            // Фильтруем функции, к которым у пользователя есть доступ
            List<Function> accessibleFunctions = allFunctions.stream()
                    .filter(function -> AuthorizationService.isAdmin(req) ||
                            currentUser.getId().equals(function.getUserId()))
                    .collect(Collectors.toList());

            // Создаем DTO только для доступных функций
            List<FunctionDTO> result = accessibleFunctions.stream()
                    .map(FunctionMapper::toDTO)
                    .collect(Collectors.toList());

            // Логируем результат
            if (allFunctions.size() != accessibleFunctions.size()) {
                logger.warn("Доступны только {} из {} запрошенных функций для пользователя {}",
                        accessibleFunctions.size(), allFunctions.size(), currentUser.getLogin());
            }

            writeJson(resp, 200, result);
            logger.info("Отправлено {} функций из {} запрошенных для пользователя {}",
                    result.size(), request.getIds().size(), currentUser.getLogin());
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

            User currentUser = AuthorizationService.getCurrentUser(req);

            if (!AuthorizationService.isAdmin(req)) {
                // Обычные пользователи могут искать функции только для себя
                if (!request.getIds().contains(currentUser.getId())) {
                    logger.warn("Пользователь {} пытается найти функции других пользователей", currentUser.getLogin());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для поиска функций других пользователей");
                    return;
                }
                // Оставляем только ID текущего пользователя
                request.getIds().retainAll(Collections.singletonList(currentUser.getId()));
            }

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

            Optional<Function> functionOpt = functionDAO.findById(id);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для обновления с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
                return;
            }

            Function existingFunction = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на обновление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(existingFunction.getUserId())) {
                logger.warn("Пользователь {} пытается обновить функцию другого пользователя (ID: {})",
                        currentUser.getLogin(), existingFunction.getUserId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для обновления этой функции");
                return;
            }

            FunctionDTO functionDTO = objectMapper.readValue(req.getInputStream(), FunctionDTO.class);
            logger.debug("Получены данные для обновления функции ID {}: {}", id, functionDTO);
            functionDTO.setId(id); // Устанавливаем ID из пути
            functionDTO.setUserId(existingFunction.getUserId()); // Сохраняем оригинальный ID пользователя

            ValidationUtils.validateFunctionDTO(functionDTO);
            Function function = FunctionMapper.toEntity(functionDTO);

            if (functionDAO.update(function)) {
                writeJson(resp, 200, functionDTO);
                logger.info("Функция с ID {} успешно обновлена пользователем {}",
                        id, currentUser.getLogin());
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

            Optional<Function> functionOpt = functionDAO.findById(id);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для обновления сигнатуры с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id + "/signature");
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на обновление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается обновить сигнатуру функции другого пользователя (ID: {})",
                        currentUser.getLogin(), function.getUserId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для обновления сигнатуры этой функции");
                return;
            }

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
                    logger.info("Сигнатура функции с ID {} успешно обновлена пользователем {}",
                            id, currentUser.getLogin());
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

    private void handleDeleteFunctionById(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление функции по ID: {}", idStr);
        try {
            int id = Integer.parseInt(idStr);
            ValidationUtils.validateId(id, "функция");

            Optional<Function> functionOpt = functionDAO.findById(id);
            if (!functionOpt.isPresent()) {
                logger.warn("Функция не найдена для удаления с ID: {}", id);
                handleError(resp, 404, "Функция не найдена", "/api/v1/functions/" + id);
                return;
            }

            Function function = functionOpt.get();
            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на удаление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(function.getUserId())) {
                logger.warn("Пользователь {} пытается удалить функцию другого пользователя (ID: {})",
                        currentUser.getLogin(), function.getUserId());
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для удаления этой функции");
                return;
            }

            if (functionDAO.delete(id)) {
                resp.setStatus(204); // No Content
                logger.info("Функция с ID {} успешно удалена пользователем {}",
                        id, currentUser.getLogin());
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

    private void handleDeleteFunctionsByUserId(String userIdStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        logger.debug("Удаление всех функций пользователя с ID: {}", userIdStr);
        try {
            int userId = Integer.parseInt(userIdStr);
            ValidationUtils.validateId(userId, "пользователь");

            User currentUser = AuthorizationService.getCurrentUser(req);

            // Проверка прав на удаление
            if (!AuthorizationService.isAdmin(req) && !currentUser.getId().equals(userId)) {
                logger.warn("Пользователь {} пытается удалить функции другого пользователя (ID: {})",
                        currentUser.getLogin(), userId);
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Недостаточно прав для удаления функций другого пользователя");
                return;
            }

            if (functionDAO.deleteByUserId(userId)) {
                resp.setStatus(204); // No Content
                logger.info("Все функции пользователя с ID {} успешно удалены пользователем {}",
                        userId, currentUser.getLogin());
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