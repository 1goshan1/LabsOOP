package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.models.Function;

public class FunctionMapper {
    private static final Logger logger = LoggerFactory.getLogger(FunctionMapper.class);

    public static FunctionDTO toDTO(Function function) {
        if (function == null) {
            logger.warn("Попытка преобразования null-объекта Function в DTO");
            return null;
        }

        logger.debug("Преобразование Function(id={}) в FunctionDTO", function.getId());
        return new FunctionDTO(
                function.getId(),
                function.getUserId(),
                function.getName(),
                function.getSignature()
        );
    }

    public static Function toEntity(FunctionDTO dto) {
        if (dto == null) {
            logger.warn("Попытка преобразования null-объекта FunctionDTO в сущность");
            return null;
        }

        logger.debug("Преобразование FunctionDTO в Function");
        Function function = new Function();
        function.setId(dto.getId());
        function.setUserId(dto.getUserId());
        function.setName(dto.getName());
        function.setSignature(dto.getSignature());
        return function;
    }
}