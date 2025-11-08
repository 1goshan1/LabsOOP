package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.models.Point;

public class PointMapper {
    private static final Logger logger = LoggerFactory.getLogger(PointMapper.class);

    public static PointDTO toDTO(Point point) {
        if (point == null) {
            logger.warn("Попытка преобразования null-объекта Point в DTO");
            return null;
        }

        logger.debug("Преобразование Point(id={}) в PointDTO", point.getId());
        return new PointDTO(
                point.getId(),
                point.getFunctionId(),
                point.getXValue(),
                point.getYValue()
        );
    }

    public static Point toEntity(PointDTO dto) {
        if (dto == null) {
            logger.warn("Попытка преобразования null-объекта PointDTO в сущность");
            return null;
        }

        logger.debug("Преобразование PointDTO в Point");
        Point point = new Point();
        point.setId(dto.getId());
        point.setFunctionId(dto.getFunctionId());
        point.setXValue(dto.getXValue());
        point.setYValue(dto.getYValue());
        return point;
    }
}