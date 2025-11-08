package ru.ssau.tk.cheefkeef.laba2.util;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DtoTransformerTest {

    // UserDTO tests
    @Test
    void transformUserFromUi_Success() {
        Map<String, Object> data = new HashMap<>();
        data.put("login", "user123");
        data.put("role", "ADMIN");
        data.put("password", "pass123");

        UserDTO result = DtoTransformer.transformUserFromUi(data);

        assertEquals("user123", result.getLogin());
        assertEquals("ADMIN", result.getRole());
        assertEquals("pass123", result.getPassword());
    }

    @Test
    void transformUserFromUi_MissingField() {
        Map<String, Object> data = new HashMap<>();
        data.put("login", "user123");
        // Пропущены role и password

        UserDTO result = DtoTransformer.transformUserFromUi(data);
        assertEquals("user123", result.getLogin());
        assertNull(result.getRole());
        assertNull(result.getPassword());
    }

    @Test
    void transformUserFromUi_WrongType() {
        Map<String, Object> data = new HashMap<>();
        data.put("login", "user123");
        data.put("role", 123); // Неправильный тип
        data.put("password", "pass123");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> DtoTransformer.transformUserFromUi(data));
        assertTrue(exception.getMessage().contains("Неверный формат данных пользователя"));
    }

    @Test
    void transformUserFromUi_NullInput() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> DtoTransformer.transformUserFromUi(null));
        assertTrue(exception.getMessage().contains("Неверный формат данных пользователя"));
    }

    // FunctionDTO tests
    @Test
    void transformFunctionFromUi_Success() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 100);
        data.put("name", "quadratic");
        data.put("signature", "f(x) = x^2");

        FunctionDTO result = DtoTransformer.transformFunctionFromUi(data);

        assertEquals(100, result.getUserId());
        assertEquals("quadratic", result.getName());
        assertEquals("f(x) = x^2", result.getSignature());
    }

    @Test
    void transformFunctionFromUi_InvalidType() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", "not_an_integer"); // Неправильный тип
        data.put("name", "linear");
        data.put("signature", "f(x) = x");

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> DtoTransformer.transformFunctionFromUi(data));
        assertTrue(exception.getMessage().contains("Неверный формат данных функции"));
    }

    @Test
    void transformFunctionFromUi_NullValues() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", null);
        data.put("name", null);
        data.put("signature", null);

        FunctionDTO result = DtoTransformer.transformFunctionFromUi(data);
        assertNull(result.getUserId());
        assertNull(result.getName());
        assertNull(result.getSignature());
    }

    // PointDTO tests
    @Test
    void transformPointFromUi_Success() {
        Map<String, Object> data = new HashMap<>();
        data.put("functionId", 42);
        data.put("x", 3.14);
        data.put("y", 2.71);

        PointDTO result = DtoTransformer.transformPointFromUi(data);

        assertEquals(42, result.getFunctionId());
        assertEquals(3.14, result.getXValue());
        assertEquals(2.71, result.getYValue());
    }

    @Test
    void transformPointFromUi_MissingCoordinate() {
        Map<String, Object> data = new HashMap<>();
        data.put("functionId", 42);
        data.put("x", 3.14);
        // Пропущен y

        PointDTO result = DtoTransformer.transformPointFromUi(data);
        assertEquals(42, result.getFunctionId());
        assertEquals(3.14, result.getXValue());
        assertNull(result.getYValue());
    }

    // Batch transformation tests
    @Test
    void transformPointsBatchFromUi_Success() {
        List<Map<String, Object>> dataList = new ArrayList<>();

        Map<String, Object> point1 = new HashMap<>();
        point1.put("functionId", 1);
        point1.put("x", 1.0);
        point1.put("y", 1.0);

        Map<String, Object> point2 = new HashMap<>();
        point2.put("functionId", 1);
        point2.put("x", 2.0);
        point2.put("y", 4.0);

        dataList.add(point1);
        dataList.add(point2);

        List<PointDTO> results = DtoTransformer.transformPointsBatchFromUi(dataList);

        assertEquals(2, results.size());
        assertEquals(1.0, results.get(0).getXValue());
        assertEquals(1.0, results.get(0).getYValue());
        assertEquals(2.0, results.get(1).getXValue());
        assertEquals(4.0, results.get(1).getYValue());
    }

    @Test
    void transformPointsBatchFromUi_EmptyList() {
        List<PointDTO> results = DtoTransformer.transformPointsBatchFromUi(Collections.emptyList());
        assertTrue(results.isEmpty());
    }

    @Test
    void transformPointsBatchFromUi_InvalidPointInBatch() {
        List<Map<String, Object>> dataList = new ArrayList<>();
        Map<String, Object> invalidPoint = new HashMap<>();
        invalidPoint.put("functionId", "not_an_integer"); // Неправильный тип
        invalidPoint.put("x", 1.0);
        invalidPoint.put("y", 2.0);
        dataList.add(invalidPoint);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> DtoTransformer.transformPointsBatchFromUi(dataList));
        assertTrue(exception.getMessage().contains("Неверный формат данных точки"));
    }

    @Test
    void transformPointsBatchFromUi_NullList() {
        Exception exception = assertThrows(NullPointerException.class,
                () -> DtoTransformer.transformPointsBatchFromUi(null));
        assertTrue(exception.getMessage().contains("Cannot invoke"));
    }
}