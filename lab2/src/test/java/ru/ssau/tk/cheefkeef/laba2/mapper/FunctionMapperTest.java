package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.models.Function;


import static org.junit.jupiter.api.Assertions.*;

class FunctionMapperTest {

    @Test
    void toDTO_NullFunction() {
        FunctionDTO result = FunctionMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void toDTO_ValidFunction() {
        Function function = new Function();
        function.setId(1);
        function.setUserId(100);
        function.setName("linear");
        function.setSignature("f(x) = x");

        FunctionDTO result = FunctionMapper.toDTO(function);

        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(100, result.getUserId());
        assertEquals("linear", result.getName());
        assertEquals("f(x) = x", result.getSignature());
    }

    @Test
    void toEntity_NullDTO() {
        Function result = FunctionMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_ValidDTO() {
        FunctionDTO dto = new FunctionDTO(2, 200, "quadratic", "f(x) = x^2");

        Function result = FunctionMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals(200, result.getUserId());
        assertEquals("quadratic", result.getName());
        assertEquals("f(x) = x^2", result.getSignature());
    }
}