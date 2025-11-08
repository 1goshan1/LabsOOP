package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.models.Point;

import static org.junit.jupiter.api.Assertions.*;

class PointMapperTest {

    @Test
    void toDTO_NullPoint() {
        PointDTO result = PointMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void toDTO_ValidPoint() {
        Point point = new Point();
        point.setId(10);
        point.setFunctionId(1);
        point.setXValue(2.5);
        point.setYValue(6.25);

        PointDTO result = PointMapper.toDTO(point);

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(1, result.getFunctionId());
        assertEquals(2.5, result.getXValue());
        assertEquals(6.25, result.getYValue());
    }

    @Test
    void toEntity_NullDTO() {
        Point result = PointMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_ValidDTO() {
        PointDTO dto = new PointDTO(20, 2, 3.0, 9.0);

        Point result = PointMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(20, result.getId());
        assertEquals(2, result.getFunctionId());
        assertEquals(3.0, result.getXValue());
        assertEquals(9.0, result.getYValue());
    }
}
