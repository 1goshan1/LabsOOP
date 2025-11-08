package ru.ssau.tk.cheefkeef.laba2.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DtoTransformer {
    private static final Logger logger = LoggerFactory.getLogger(DtoTransformer.class);

    /**
     * Преобразует данные из UI в UserDTO
     */
    public static UserDTO transformUserFromUi(Map<String, Object> uiData) {
        logger.info("Преобразование данных пользователя из UI");

        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin((String) uiData.get("login"));
            userDTO.setRole((String) uiData.get("role"));
            userDTO.setPassword((String) uiData.get("password"));

            logger.debug("Создан UserDTO: login={}, role={}",
                    userDTO.getLogin(), userDTO.getRole());
            return userDTO;
        } catch (Exception e) {
            logger.error("Ошибка при преобразовании данных пользователя: {}", e.getMessage());
            throw new IllegalArgumentException("Неверный формат данных пользователя", e);
        }
    }

    /**
     * Преобразует данные из UI в FunctionDTO
     */
    public static FunctionDTO transformFunctionFromUi(Map<String, Object> uiData) {
        logger.info("Преобразование данных функции из UI");

        try {
            FunctionDTO functionDTO = new FunctionDTO();
            functionDTO.setUserId((Integer) uiData.get("userId"));
            functionDTO.setName((String) uiData.get("name"));
            functionDTO.setSignature((String) uiData.get("signature"));

            logger.debug("Создан FunctionDTO: userId={}, name={}",
                    functionDTO.getUserId(), functionDTO.getName());
            return functionDTO;
        } catch (Exception e) {
            logger.error("Ошибка при преобразовании данных функции: {}", e.getMessage());
            throw new IllegalArgumentException("Неверный формат данных функции", e);
        }
    }

    /**
     * Преобразует данные из UI в PointDTO
     */
    public static PointDTO transformPointFromUi(Map<String, Object> uiData) {
        logger.info("Преобразование данных точки из UI");

        try {
            PointDTO pointDTO = new PointDTO();
            pointDTO.setFunctionId((Integer) uiData.get("functionId"));
            pointDTO.setXValue((Double) uiData.get("x"));
            pointDTO.setYValue((Double) uiData.get("y"));

            logger.debug("Создан PointDTO: functionId={}, x={}, y={}",
                    pointDTO.getFunctionId(), pointDTO.getXValue(), pointDTO.getYValue());
            return pointDTO;
        } catch (Exception e) {
            logger.error("Ошибка при преобразовании данных точки: {}", e.getMessage());
            throw new IllegalArgumentException("Неверный формат данных точки", e);
        }
    }

    /**
     * Массовое преобразование точек из UI
     */
    public static List<PointDTO> transformPointsBatchFromUi(List<Map<String, Object>> pointsData) {
        logger.info("Массовое преобразование {} точек из UI", pointsData.size());

        List<PointDTO> result = new ArrayList<>();
        for (Map<String, Object> pointData : pointsData) {
            result.add(transformPointFromUi(pointData));
        }

        logger.info("Успешно преобразовано {} точек", result.size());
        return result;
    }
}