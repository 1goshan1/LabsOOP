package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;

import java.util.List;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtils.class);
    private static final Pattern LOGIN_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,32}$");

    public static void validateUserDTO(UserDTO userDTO) {
        logger.debug("Валидация UserDTO: {}", userDTO);

        if (userDTO == null) {
            logger.error("Передан null объект UserDTO");
            throw new IllegalArgumentException("Данные пользователя не могут быть пустыми");
        }

        if (userDTO.getLogin() == null || !LOGIN_PATTERN.matcher(userDTO.getLogin()).matches()) {
            logger.error("Неверный формат логина: {}", userDTO.getLogin());
            throw new IllegalArgumentException("Неверный формат логина. Допустимы буквы, цифры и подчеркивание, длина от 3 до 20 символов");
        }

        if (userDTO.getPassword() == null || !PASSWORD_PATTERN.matcher(userDTO.getPassword()).matches()) {
            logger.error("Неверный формат пароля");
            throw new IllegalArgumentException("Пароль должен содержать заглавные и строчные буквы, цифры, длина от 8 до 32 символов");
        }

        if (userDTO.getRole() == null || (!"user".equals(userDTO.getRole()) && !"admin".equals(userDTO.getRole()))) {
            logger.error("Неверная роль пользователя: {}", userDTO.getRole());
            throw new IllegalArgumentException("Неверная роль пользователя. Допустимые значения: user, admin");
        }

        logger.debug("UserDTO успешно валидирован");
    }

    public static void validateFunctionDTO(FunctionDTO functionDTO) {
        logger.debug("Валидация FunctionDTO: {}", functionDTO);

        if (functionDTO == null) {
            logger.error("Передан null объект FunctionDTO");
            throw new IllegalArgumentException("Данные функции не могут быть пустыми");
        }

        if (functionDTO.getUserId() == null || functionDTO.getUserId() <= 0) {
            logger.error("Неверный ID пользователя: {}", functionDTO.getUserId());
            throw new IllegalArgumentException("ID пользователя должен быть положительным числом");
        }

        if (functionDTO.getName() == null || functionDTO.getName().trim().isEmpty() || functionDTO.getName().length() > 100) {
            logger.error("Неверное имя функции: {}", functionDTO.getName());
            throw new IllegalArgumentException("Имя функции должно быть заполнено и не превышать 100 символов");
        }

        if (functionDTO.getSignature() == null || functionDTO.getSignature().trim().isEmpty() || functionDTO.getSignature().length() > 255) {
            logger.error("Неверная сигнатура функции");
            throw new IllegalArgumentException("Сигнатура функции должна быть заполнена и не превышать 255 символов");
        }

        logger.debug("FunctionDTO успешно валидирован");
    }

    public static void validatePointDTO(PointDTO pointDTO) {
        logger.debug("Валидация PointDTO: {}", pointDTO);

        if (pointDTO == null) {
            logger.error("Передан null объект PointDTO");
            throw new IllegalArgumentException("Данные точки не могут быть пустыми");
        }

        if (pointDTO.getFunctionId() == null || pointDTO.getFunctionId() <= 0) {
            logger.error("Неверный ID функции: {}", pointDTO.getFunctionId());
            throw new IllegalArgumentException("ID функции должен быть положительным числом");
        }

        if (Double.isNaN(pointDTO.getXValue()) || Double.isInfinite(pointDTO.getXValue())) {
            logger.error("Неверное значение X: {}", pointDTO.getXValue());
            throw new IllegalArgumentException("Неверное значение координаты X");
        }

        if (Double.isNaN(pointDTO.getYValue()) || Double.isInfinite(pointDTO.getYValue())) {
            logger.error("Неверное значение Y: {}", pointDTO.getYValue());
            throw new IllegalArgumentException("Неверное значение координаты Y");
        }

        logger.debug("PointDTO успешно валидирован");
    }

    public static void validateId(Integer id, String entityName) {
        logger.debug("Валидация ID для {}: {}", entityName, id);

        if (id == null || id <= 0) {
            logger.error("Неверный ID для {}: {}", entityName, id);
            throw new IllegalArgumentException(String.format("Неверный ID для %s", entityName));
        }

        logger.debug("ID для {} успешно валидирован", entityName);
    }

    public static void validateIds(List<Integer> ids, String entityName) {
        logger.debug("Валидация списка ID для {}: {}", entityName, ids);

        if (ids == null || ids.isEmpty()) {
            logger.error("Список ID для {} пустой", entityName);
            throw new IllegalArgumentException(String.format("Список ID для %s не может быть пустым", entityName));
        }

        for (Integer id : ids) {
            validateId(id, entityName);
        }

        logger.debug("Список ID для {} успешно валидирован", entityName);
    }
}