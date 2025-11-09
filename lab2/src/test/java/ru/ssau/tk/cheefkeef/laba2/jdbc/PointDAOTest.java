package ru.ssau.tk.cheefkeef.laba2.jdbc;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.models.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PointDAOTest {

    private static final Logger logger = LoggerFactory.getLogger(PointDAOTest.class);
    private PointDAO pointDAO;
    private Faker faker;
    private List<Point> testPoints;
    private Integer testFunctionId;

    @BeforeAll
    void setUp() {
        pointDAO = new PointDAO();
        faker = new Faker(new Locale("ru"));
        testPoints = new ArrayList<>();
        testFunctionId = faker.number().numberBetween(1000, 9999);

        logger.info("Начало подготовки тестовых данных для функции ID: {}", testFunctionId);
    }

    @BeforeEach
    void clearTestData() {
        // Очищаем тестовые точки перед каждым тестом
        if (testFunctionId != null) {
            pointDAO.deleteByFunctionId(testFunctionId);
        }
        testPoints.clear();
    }

    @AfterAll
    void tearDown() {
        // Финальная очистка после всех тестов
        if (testFunctionId != null) {
            pointDAO.deleteByFunctionId(testFunctionId);
        }
        logger.info("Очистка тестовых данных завершена");
    }

 

    @Test
    @DisplayName("Тест поиска несуществующей точки")
    void testFindNonExistentPoint() {
        // Act
        Optional<Point> result = pointDAO.findById(-1);

        // Assert
        assertFalse(result.isPresent(), "Несуществующая точка не должна быть найдена");
    }

    @Test
    @DisplayName("Тест получения всех точек")
    void testFindAll() {
        // Arrange
        createTestPoints(3);

        // Act
        List<Point> allPoints = pointDAO.findAll();

        // Assert
        assertNotNull(allPoints, "Список точек не должен быть null");
        assertFalse(allPoints.isEmpty(), "Список точек не должен быть пустым");
    }

    @Test
    @DisplayName("Тест поиска точек по ID функции")
    void testFindByFunctionId() {
        // Arrange
        createTestPoints(5);

        // Act
        List<Point> points = pointDAO.findByFunctionId(testFunctionId);

        // Assert
        assertAll(
                () -> assertNotNull(points, "Список точек не должен быть null"),
                () -> assertEquals(5, points.size(), "Должно быть найдено 5 точек"),
                () -> assertTrue(points.stream().allMatch(p -> p.getFunctionId().equals(testFunctionId)),
                        "Все точки должны принадлежать тестовой функции")
        );
    }

    @Test
    @DisplayName("Тест поиска точек по диапазону X")
    void testFindByXRange() {
        // Arrange
        createTestPointsWithSpecificXValues();

        // Act
        List<Point> points = pointDAO.findByXRange(5.0, 15.0);

        // Assert
        assertNotNull(points, "Список точек не должен быть null");
        assertTrue(points.stream().allMatch(p -> p.getXValue() >= 5.0 && p.getXValue() <= 15.0),
                "Все точки должны быть в указанном диапазоне X");
    }

    @Test
    @DisplayName("Тест поиска точек по диапазону Y")
    void testFindByYRange() {
        // Arrange
        createTestPointsWithSpecificYValues();

        // Act
        List<Point> points = pointDAO.findByYRange(-5.0, 5.0);

        // Assert
        assertNotNull(points, "Список точек не должен быть null");
        assertTrue(points.stream().allMatch(p -> p.getYValue() >= -5.0 && p.getYValue() <= 5.0),
                "Все точки должны быть в указанном диапазоне Y");
    }

    @Test
    @DisplayName("Тест поиска точки по functionId и X")
    void testFindByFunctionIdAndX() {
        // Arrange
        double specificX = 42.5;
        Point point = new Point();
        point.setFunctionId(testFunctionId);
        point.setXValue(specificX);
        point.setYValue(faker.number().randomDouble(2, -50, 50));
        pointDAO.insert(point);

        // Act
        Optional<Point> foundPoint = pointDAO.findByFunctionIdAndX(testFunctionId, specificX);

        // Assert
        assertAll(
                () -> assertTrue(foundPoint.isPresent(), "Точка должна быть найдена"),
                () -> assertEquals(testFunctionId, foundPoint.get().getFunctionId(), "FunctionId должен совпадать"),
                () -> assertEquals(specificX, foundPoint.get().getXValue(), "XValue должен совпадать")
        );
    }

    @Test
    @DisplayName("Тест массового добавления точек")
    void testInsertBatch() {
        // Arrange
        List<Point> batchPoints = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Point point = new Point();
            point.setFunctionId(testFunctionId);
            point.setXValue(faker.number().randomDouble(2, 0, 100));
            point.setYValue(faker.number().randomDouble(2, 0, 100));
            batchPoints.add(point);
        }

        // Act
        int insertedCount = pointDAO.insertBatch(batchPoints);

        // Assert
        assertEquals(10, insertedCount, "Должно быть вставлено 10 точек");

        List<Point> foundPoints = pointDAO.findByFunctionId(testFunctionId);
        assertEquals(10, foundPoints.size(), "Должно быть найдено 10 точек после массовой вставки");
    }




    @Test
    @DisplayName("Тест удаления точек по functionId")
    void testDeleteByFunctionId() {
        // Arrange
        createTestPoints(5);

        // Act
        boolean deleteResult = pointDAO.deleteByFunctionId(testFunctionId);
        List<Point> foundPoints = pointDAO.findByFunctionId(testFunctionId);

        // Assert
        assertAll(
                () -> assertTrue(deleteResult, "Удаление должно быть успешным"),
                () -> assertTrue(foundPoints.isEmpty(), "Не должно остаться точек после удаления")
        );
    }

    @Test
    @DisplayName("Тест подсчета количества точек функции")
    void testCountByFunctionId() {
        // Arrange
        int expectedCount = 7;
        createTestPoints(expectedCount);

        // Act
        int actualCount = pointDAO.countByFunctionId(testFunctionId);

        // Assert
        assertEquals(expectedCount, actualCount, "Количество точек должно совпадать");
    }

    @Test
    @DisplayName("Тест проверки существования точки")
    void testExistsByFunctionIdAndX() {
        // Arrange
        double specificX = 33.3;
        Point point = new Point();
        point.setFunctionId(testFunctionId);
        point.setXValue(specificX);
        point.setYValue(faker.number().randomDouble(2, -10, 10));
        pointDAO.insert(point);

        // Act & Assert
        assertTrue(pointDAO.existsByFunctionIdAndX(testFunctionId, specificX),
                "Точка должна существовать");
        assertFalse(pointDAO.existsByFunctionIdAndX(testFunctionId, 999.9),
                "Точка с несуществующим X не должна существовать");
    }

    @Test
    @DisplayName("Тест получения диапазона X для функции")
    void testGetXRangeForFunction() {
        // Arrange
        createTestPointsWithSpecificXValues();

        // Act
        double[] xRange = pointDAO.getXRangeForFunction(testFunctionId);

        // Assert
        assertAll(
                () -> assertNotNull(xRange, "Диапазон не должен быть null"),
                () -> assertEquals(2, xRange.length, "Диапазон должен содержать 2 значения"),
                () -> assertTrue(xRange[0] <= xRange[1], "Min должен быть <= Max")
        );
    }

    @Test
    @DisplayName("Тест поиска с сортировкой по различным полям")
    void testFindWithSorting() {
        // Arrange
        createTestPoints(5);

        // Act & Assert - тестируем сортировку по разным полям
        testSorting("id", "ASC");
        testSorting("x_value", "DESC");
        testSorting("y_value", "ASC");
        testSorting("f_id", "DESC");
    }

    @Test
    @DisplayName("Тест поиска с сортировкой с невалидными параметрами")
    void testFindWithSortingInvalidParameters() {
        // Arrange
        createTestPoints(3);

        // Act - используем невалидные параметры
        List<Point> points = pointDAO.findWithSorting("invalid_field", "INVALID_DIRECTION");

        // Assert - метод должен обработать невалидные параметры и вернуть результат
        assertNotNull(points, "Список точек не должен быть null даже при невалидных параметрах сортировки");
    }

    // Вспомогательные методы

    private Point createAndInsertTestPoint() {
        Point point = new Point();
        point.setFunctionId(testFunctionId);
        point.setXValue(faker.number().randomDouble(2, 0, 100));
        point.setYValue(faker.number().randomDouble(2, 0, 100));
        Point insertedPoint = pointDAO.insert(point);
        testPoints.add(insertedPoint);
        return insertedPoint;
    }

    private void createTestPoints(int count) {
        for (int i = 0; i < count; i++) {
            createAndInsertTestPoint();
        }
    }

    private void createTestPointsWithSpecificXValues() {
        double[] xValues = {2.5, 7.8, 12.3, 18.9, 25.1};
        for (double x : xValues) {
            Point point = new Point();
            point.setFunctionId(testFunctionId);
            point.setXValue(x);
            point.setYValue(faker.number().randomDouble(2, -10, 10));
            pointDAO.insert(point);
            testPoints.add(point);
        }
    }

    private void createTestPointsWithSpecificYValues() {
        double[] yValues = {-7.2, -3.1, 0.0, 2.8, 8.5, 15.3};
        for (double y : yValues) {
            Point point = new Point();
            point.setFunctionId(testFunctionId);
            point.setXValue(faker.number().randomDouble(2, 0, 20));
            point.setYValue(y);
            pointDAO.insert(point);
            testPoints.add(point);
        }
    }

    private void testSorting(String sortField, String sortDirection) {
        List<Point> sortedPoints = pointDAO.findWithSorting(sortField, sortDirection);
        assertNotNull(sortedPoints, "Отсортированный список не должен быть null");
        assertFalse(sortedPoints.isEmpty(), "Отсортированный список не должен быть пустым");
    }
}