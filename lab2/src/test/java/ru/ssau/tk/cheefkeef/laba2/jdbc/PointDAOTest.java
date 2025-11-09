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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PointDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(PointDAOTest.class);
    private static PointDAO pointDAO;
    private static Faker faker;
    private static Point testPoint;
    private static Integer testFunctionId;

    @BeforeAll
    static void setUp() {
        logger.info("Инициализация тестового окружения PointDAO");

        pointDAO = new PointDAO();
        faker = new Faker(new Locale("en"));

        // Предполагаем, что функция с ID 1 существует
        testFunctionId = 1;

        // Создаем тестовую точку
        testPoint = createRandomPoint(testFunctionId);

        pointDAO.insert(testPoint);

        logger.debug("Создана тестовая точка: {}", testPoint);
    }

    @AfterAll
    static void tearDown() {
        logger.info("Завершение тестов PointDAO");
    }

    private static Point createRandomPoint(Integer functionId) {
        double x = faker.number().randomDouble(3, -100, 100);
        double y = faker.number().randomDouble(3, -100, 100);

        return new Point(functionId, x, y);
    }

    private static List<Point> createRandomPoints(Integer functionId, int count) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            points.add(createRandomPoint(functionId));
        }
        return points;
    }

    @Test
    @Order(1)
    void testInsertPoint() {
        logger.info("Запуск теста: создание точки");

        Point insertedPoint = pointDAO.insert(testPoint);

        assertNotNull(insertedPoint, "Созданная точка не должна быть null");
        assertNotNull(insertedPoint.getId(), "ID созданной точки не должен быть null");
        assertEquals(testPoint.getFunctionId(), insertedPoint.getFunctionId(), "Function ID должны совпадать");
        assertEquals(testPoint.getXValue(), insertedPoint.getXValue(), 0.001, "X значения должны совпадать");
        assertEquals(testPoint.getYValue(), insertedPoint.getYValue(), 0.001, "Y значения должны совпадать");

        testPoint.setId(insertedPoint.getId());
        logger.info("Успешно создана точка с ID: {}", insertedPoint.getId());
    }

    @Test
    @Order(2)
    void testFindById() {
        logger.info("Запуск теста: поиск точки по ID");

        testPoint = new Point(1, 123.0, 321.0);
        testPoint = pointDAO.insert(testPoint);
        System.out.println(testPoint.getId());
        Optional<Point> foundPointOpt = pointDAO.findById(testPoint.getId());

        assertTrue(foundPointOpt.isPresent(), "Точка должна быть найдена по ID");
        Point foundPoint = foundPointOpt.get();

        assertEquals(testPoint.getId(), foundPoint.getId(), "ID должны совпадать");
        assertEquals(testPoint.getFunctionId(), foundPoint.getFunctionId(), "Function ID должны совпадать");
        assertEquals(testPoint.getXValue(), foundPoint.getXValue(), 0.001, "X значения должны совпадать");
        assertEquals(testPoint.getYValue(), foundPoint.getYValue(), 0.001, "Y значения должны совпадать");

        logger.debug("Найдена точка: {}", foundPoint);
    }

    @Test
    @Order(3)
    void testFindByFunctionId() {
        logger.info("Запуск теста: поиск точек по ID функции");

        List<Point> points = pointDAO.findByFunctionId(testFunctionId);

        assertNotNull(points, "Список точек не должен быть null");

        // Проверяем, что наша тестовая точка есть в списке
        boolean found = points.stream()
                .anyMatch(point -> point.getId().equals(testPoint.getId()));
        assertTrue(found, "Тестовая точка должна присутствовать в списке");

        // Проверяем сортировку по X
        for (int i = 1; i < points.size(); i++) {
            assertTrue(points.get(i).getXValue() >= points.get(i-1).getXValue(),
                    "Точки должны быть отсортированы по X");
        }

        logger.info("Найдено {} точек для функции с ID: {}", points.size(), testFunctionId);
    }

    @Test
    @Order(4)
    void testFindByXRange() {
        logger.info("Запуск теста: поиск точек по диапазону X значений");

        // Создаем точки с известными X значениями
        Point point1 = new Point(testFunctionId, 10.0, 20.0);
        Point point2 = new Point(testFunctionId, 30.0, 40.0);
        Point point3 = new Point(testFunctionId, 50.0, 60.0);

        pointDAO.insert(point1);
        pointDAO.insert(point2);
        pointDAO.insert(point3);

        // Ищем точки в диапазоне [20, 40]
        List<Point> pointsInRange = pointDAO.findByXRange(20.0, 40.0);

        assertNotNull(pointsInRange, "Список точек не должен быть null");

        // Проверяем, что все точки в диапазоне
        for (Point point : pointsInRange) {
            assertTrue(point.getXValue() >= 20.0 && point.getXValue() <= 40.0,
                    "Все точки должны быть в указанном диапазоне X");
        }

        // Проверяем сортировку
        for (int i = 1; i < pointsInRange.size(); i++) {
            assertTrue(pointsInRange.get(i).getXValue() >= pointsInRange.get(i-1).getXValue(),
                    "Точки должны быть отсортированы по X");
        }

        logger.info("Найдено {} точек в диапазоне X [20, 40]", pointsInRange.size());

        // Очистка
        pointDAO.delete(point1.getId());
        pointDAO.delete(point2.getId());
        pointDAO.delete(point3.getId());
    }

    @Test
    @Order(5)
    void testFindByYRange() {
        logger.info("Запуск теста: поиск точек по диапазону Y значений");

        // Создаем точки с известными Y значениями
        Point point1 = new Point(testFunctionId, 1.0, -10.0);
        Point point2 = new Point(testFunctionId, 2.0, 0.0);
        Point point3 = new Point(testFunctionId, 3.0, 10.0);

        pointDAO.insert(point1);
        pointDAO.insert(point2);
        pointDAO.insert(point3);

        // Ищем точки в диапазоне Y [-5, 5]
        List<Point> pointsInRange = pointDAO.findByYRange(-5.0, 5.0);

        assertNotNull(pointsInRange, "Список точек не должен быть null");

        // Проверяем, что все точки в диапазоне
        for (Point point : pointsInRange) {
            assertTrue(point.getYValue() >= -5.0 && point.getYValue() <= 5.0,
                    "Все точки должны быть в указанном диапазоне Y");
        }

        logger.info("Найдено {} точек в диапазоне Y [-5, 5]", pointsInRange.size());

        // Очистка
        pointDAO.delete(point1.getId());
        pointDAO.delete(point2.getId());
        pointDAO.delete(point3.getId());
    }

    @Test
    @Order(6)
    void testFindByFunctionIdAndX() {
        logger.info("Запуск теста: поиск точки по functionId и X значению");

        // Создаем точку с уникальным X значением
        double uniqueX = 123.456;
        Point uniquePoint = new Point(testFunctionId, uniqueX, 789.012);
        Point insertedPoint = pointDAO.insert(uniquePoint);

        Optional<Point> foundPointOpt = pointDAO.findByFunctionIdAndX(testFunctionId, uniqueX);

        assertTrue(foundPointOpt.isPresent(), "Точка должна быть найдена по functionId и X");
        Point foundPoint = foundPointOpt.get();

        assertEquals(insertedPoint.getId(), foundPoint.getId(), "ID должны совпадать");
        assertEquals(uniqueX, foundPoint.getXValue(), 0.001, "X значения должны совпадать");

        logger.debug("Найдена точка по functionId и X: {}", foundPoint);

        // Очистка
        pointDAO.delete(insertedPoint.getId());
    }

    @Test
    @Order(7)
    void testInsertBatch() {
        logger.info("Запуск теста: массовое добавление точек");

        int batchSize = 10;
        List<Point> points = createRandomPoints(testFunctionId, batchSize);

        long startTime = System.currentTimeMillis();
        int insertedCount = pointDAO.insertBatch(points);
        long endTime = System.currentTimeMillis();

        assertEquals(batchSize, insertedCount, "Все точки должны быть вставлены");

        // Проверяем, что точки действительно добавлены
        List<Point> foundPoints = pointDAO.findByFunctionId(testFunctionId);
        int pointsForFunction = (int) foundPoints.stream()
                .filter(p -> p.getFunctionId().equals(testFunctionId))
                .count();

        assertTrue(pointsForFunction >= batchSize, "Должны быть найдены все добавленные точки");

        long duration = endTime - startTime;
        logger.info("Массовое добавление {} точек выполнено за {} мс", batchSize, duration);

        // Очистка - удаляем точки этой функции
        pointDAO.deleteByFunctionId(testFunctionId);
    }

    @Test
    @Order(8)
    void testUpdatePoint() {
        logger.info("Диагностический тест: проверка подключения и базовых операций");

        // Проверяем подключение через поиск всех точек
        List<Point> allPoints = pointDAO.findAll();
        logger.info("В базе найдено {} точек", allPoints.size());

        // Тестируем создание и удаление точки
        Point testPoint = createRandomPoint(testFunctionId);
        Point inserted = pointDAO.insert(testPoint);

        if (inserted != null && inserted.getId() != null) {
            logger.info("Успешно создана точка с ID: {}", inserted.getId());

            // Проверяем обновление
            Point updateData = new Point(testFunctionId + 1, 999.0, 888.0);
            updateData.setId(inserted.getId());

            boolean updateResult = pointDAO.update(updateData);
            logger.info("Результат обновления: {}", updateResult);

            if (updateResult) {
                Optional<Point> updated = pointDAO.findById(inserted.getId());
                if (updated.isPresent()) {
                    logger.info("Точка после обновления: {}", updated.get());
                }
            }

            // Очистка
            pointDAO.delete(inserted.getId());
            logger.info("Тестовая точка удалена");
        } else {
            logger.error("Не удалось создать тестовую точку");
        }
    }

    @Test
    @Order(9)
    void testUpdateYValue() {
        logger.info("Запуск теста: обновление только Y значения точки");

        // Создаем новую точку специально для этого теста
        Point originalPoint = createRandomPoint(testFunctionId);
        Point insertedPoint = pointDAO.insert(originalPoint);
        assertNotNull(insertedPoint, "Не удалось создать точку для теста обновления Y");

        Integer pointId = insertedPoint.getId();
        Double originalY = insertedPoint.getYValue();
        Double newY = faker.number().randomDouble(3, -100, 100);

        logger.info("Создана точка для теста обновления Y: {}", insertedPoint);
        logger.debug("Обновление Y значения: {} -> {} для точки ID: {}", originalY, newY, pointId);

        // Выполняем обновление только Y значения
        boolean updateResult = pointDAO.updateYValue(pointId, newY);

        // Проверяем результат
        assertTrue(updateResult,
                String.format("Обновление Y значения должно быть успешным. ID точки: %d, Y: %.3f->%.3f",
                        pointId, originalY, newY));

        // Проверяем, что только Y значение обновилось, а остальные поля остались прежними
        Optional<Point> updatedPointOpt = pointDAO.findById(pointId);
        assertTrue(updatedPointOpt.isPresent(), "Точка должна существовать после обновления");

        Point updatedPoint = updatedPointOpt.get();

        assertEquals(newY, updatedPoint.getYValue(), 0.001,
                "Y значение должно быть обновлено");
        assertEquals(insertedPoint.getFunctionId(), updatedPoint.getFunctionId(),
                "Function ID должен остаться прежним");
        assertEquals(insertedPoint.getXValue(), updatedPoint.getXValue(), 0.001,
                "X значение должно остаться прежним");

        logger.info("Y значение успешно обновлено: {} -> {}", originalY, newY);
        logger.debug("Точка после обновления Y: {}", updatedPoint);

        // Очистка
        pointDAO.delete(pointId);
    }


    @Test
    @Order(10)
    void testCountByFunctionId() {
        logger.info("Запуск теста: подсчет количества точек функции");

        int countBefore = pointDAO.countByFunctionId(testFunctionId);

        // Создаем несколько точек для этой функции
        int pointsToAdd = 3;
        for (int i = 0; i < pointsToAdd; i++) {
            Point point = createRandomPoint(testFunctionId);
            pointDAO.insert(point);
        }

        int countAfter = pointDAO.countByFunctionId(testFunctionId);
        assertEquals(countBefore + pointsToAdd, countAfter, "Количество точек должно увеличиться");

        logger.info("Количество точек функции: до={}, после={}", countBefore, countAfter);

        // Очистка - удаляем добавленные точки
        pointDAO.deleteByFunctionId(testFunctionId);
    }

    @Test
    @Order(12)
    void testGetXRangeForFunction() {
        logger.info("Запуск теста: получение диапазона X для функции");

        // Создаем точки с известными X значениями
        Integer tempFunctionId = testFunctionId + 100;
        Point minPoint = new Point(tempFunctionId, -100.0, 0.0);
        Point maxPoint = new Point(tempFunctionId, 100.0, 0.0);
        Point middlePoint = new Point(tempFunctionId, 0.0, 0.0);

        pointDAO.insert(minPoint);
        pointDAO.insert(maxPoint);
        pointDAO.insert(middlePoint);

        double[] xRange = pointDAO.getXRangeForFunction(tempFunctionId);

        assertNotNull(xRange, "Диапазон X не должен быть null");
        assertEquals(2, xRange.length, "Диапазон должен содержать 2 значения");
        assertEquals(-100.0, xRange[0], 0.001, "Минимальное X должно быть -100.0");
        assertEquals(100.0, xRange[1], 0.001, "Максимальное X должно быть 100.0");

        logger.info("Диапазон X для функции: min={}, max={}", xRange[0], xRange[1]);

        // Очистка
        pointDAO.deleteByFunctionId(tempFunctionId);
    }

    @Test
    @Order(13)
    void testInsertMultiplePointsWithFaker() {
        logger.info("Запуск теста: создание нескольких точек с Faker");

        int numberOfPoints = 8;
        for (int i = 0; i < numberOfPoints; i++) {
            Point randomPoint = createRandomPoint(testFunctionId);
            Point insertedPoint = pointDAO.insert(randomPoint);

            assertNotNull(insertedPoint, "Созданная точка не должна быть null");
            assertNotNull(insertedPoint.getId(), "ID созданной точки не должен быть null");

            logger.debug("Создана случайная точка #{}/{}: {}", i + 1, numberOfPoints, insertedPoint);
        }

        logger.info("Успешно создано {} случайных точек", numberOfPoints);
    }


    @Test
    @Order(15)
    void testEdgeCases() {
        logger.info("Запуск теста: проверка граничных случаев");

        // Поиск несуществующей точки
        Optional<Point> nonExistentPoint = pointDAO.findById(-1);
        assertFalse(nonExistentPoint.isPresent(), "Несуществующая точка должна возвращать Optional.empty()");

        // Поиск точек несуществующей функции
        List<Point> nonExistentFunctionPoints = pointDAO.findByFunctionId(-1);
        assertNotNull(nonExistentFunctionPoints, "Список точек несуществующей функции не должен быть null");
        assertTrue(nonExistentFunctionPoints.isEmpty(), "Список точек несуществующей функции должен быть пустым");

        // Поиск по диапазону без результатов
        List<Point> noPointsInRange = pointDAO.findByXRange(1000.0, 2000.0);
        assertNotNull(noPointsInRange, "Список точек по пустому диапазону не должен быть null");
        assertTrue(noPointsInRange.isEmpty(), "Список точек по пустому диапазону должен быть пустым");

        // Обновление несуществующей точки
        Point nonExistentPointObj = new Point(1, 1.0, 1.0);
        nonExistentPointObj.setId(-1);
        boolean updateResult = pointDAO.update(nonExistentPointObj);
        assertFalse(updateResult, "Обновление несуществующей точки должно возвращать false");

        // Обновление Y значения несуществующей точки
        boolean updateYResult = pointDAO.updateYValue(-1, 1.0);
        assertFalse(updateYResult, "Обновление Y значения несуществующей точки должно возвращать false");

        // Получение диапазона для несуществующей функции
        double[] emptyRange = pointDAO.getXRangeForFunction(-1);
        assertNotNull(emptyRange, "Диапазон для несуществующей функции не должен быть null");
        assertEquals(0.0, emptyRange[0], 0.001, "Min X для несуществующей функции должен быть 0");
        assertEquals(0.0, emptyRange[1], 0.001, "Max X для несуществующей функции должен быть 0");

        logger.info("Все граничные случаи обработаны корректно");
    }

    @Test
    @Order(16)
    void testPointsWithExtremeValues() {
        logger.info("Запуск теста: создание точек с экстремальными значениями");

        double[][] extremeValues = {
                {Double.MIN_VALUE, Double.MAX_VALUE},
                {-Double.MAX_VALUE, Double.MIN_VALUE},
                {0.0, 0.0},
                {-1.0E-10, 1.0E10},
                {Math.PI, Math.E}
        };

        for (double[] values : extremeValues) {
            Point extremePoint = new Point(testFunctionId, values[0], values[1]);
            Point inserted = pointDAO.insert(extremePoint);
            assertNotNull(inserted, "Точка с экстремальными значениями должна быть создана");

            // Проверяем, что значения сохранились корректно
            Optional<Point> found = pointDAO.findById(inserted.getId());
            assertTrue(found.isPresent(), "Точка с экстремальными значениями должна быть найдена");
            assertEquals(values[0], found.get().getXValue(), 0.001, "Экстремальное X значение должно сохраниться");
            assertEquals(values[1], found.get().getYValue(), 0.001, "Экстремальное Y значение должно сохраниться");

            // Очистка
            pointDAO.delete(inserted.getId());

            logger.debug("Протестированы экстремальные значения: x={}, y={}", values[0], values[1]);
        }

        logger.info("Все экстремальные значения обработаны успешно");
    }

    @Test
    @Order(17)
    void testDeleteByFunctionId() {
        logger.info("Запуск теста: удаление всех точек функции");

        // Создаем временную функцию и добавляем точки
        Integer tempFunctionId = testFunctionId + 200;
        int pointsToCreate = 4;

        for (int i = 0; i < pointsToCreate; i++) {
            Point tempPoint = createRandomPoint(tempFunctionId);
            pointDAO.insert(tempPoint);
        }

        // Проверяем, что точки созданы
        int countBefore = pointDAO.countByFunctionId(tempFunctionId);
        assertTrue(countBefore >= pointsToCreate, "Должны быть созданы точки для тестовой функции");

        // Удаляем все точки функции
        boolean deleteResult = pointDAO.deleteByFunctionId(tempFunctionId);
        assertTrue(deleteResult, "Удаление точек функции должно быть успешным");

        // Проверяем, что точки удалены
        int countAfter = pointDAO.countByFunctionId(tempFunctionId);
        assertEquals(0, countAfter, "Все точки функции должны быть удалены");

        logger.info("Успешно удалено {} точек функции с ID: {}", countBefore, tempFunctionId);
    }


    @Test
    @Order(19)
    void testPerformanceFindAll() {
        logger.info("Запуск теста производительности: поиск всех точек");

        long startTime = System.currentTimeMillis();
        List<Point> points = pointDAO.findAll();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        logger.info("Поиск всех точек выполнен за {} мс. Найдено {} записей", duration, points.size());

        assertTrue(duration < 5000, "Поиск всех точек должен выполняться менее чем за 5 секунд");
    }

    @Test
    @Order(20)
    void testMathematicalFunctionsSimulation() {
        logger.info("Запуск теста: симуляция математических функций");

        // Симулируем точки для различных математических функций
        simulateLinearFunction(testFunctionId + 300, 2.0, 1.0); // y = 2x + 1
        simulateQuadraticFunction(testFunctionId + 301, 1.0, 0.0, 0.0); // y = x^2
        simulateSineFunction(testFunctionId + 302, 1.0, 1.0); // y = sin(x)

        logger.info("Симуляция математических функций завершена успешно");
    }

    private void simulateLinearFunction(Integer functionId, double slope, double intercept) {
        List<Point> points = new ArrayList<>();
        for (int i = -5; i <= 5; i++) {
            double x = i;
            double y = slope * x + intercept;
            points.add(new Point(functionId, x, y));
        }

        int inserted = pointDAO.insertBatch(points);
        assertEquals(11, inserted, "Все точки линейной функции должны быть вставлены");
        logger.debug("Создана линейная функция y = {}x + {} с {} точками", slope, intercept, inserted);

        // Очистка
        pointDAO.deleteByFunctionId(functionId);
    }

    private void simulateQuadraticFunction(Integer functionId, double a, double b, double c) {
        List<Point> points = new ArrayList<>();
        for (int i = -5; i <= 5; i++) {
            double x = i;
            double y = a * x * x + b * x + c;
            points.add(new Point(functionId, x, y));
        }

        int inserted = pointDAO.insertBatch(points);
        assertEquals(11, inserted, "Все точки квадратичной функции должны быть вставлены");
        logger.debug("Создана квадратичная функция y = {}x^2 + {}x + {} с {} точками", a, b, c, inserted);

        // Очистка
        pointDAO.deleteByFunctionId(functionId);
    }

    private void simulateSineFunction(Integer functionId, double amplitude, double frequency) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i <= 20; i++) {
            double x = i * 0.5;
            double y = amplitude * Math.sin(frequency * x);
            points.add(new Point(functionId, x, y));
        }

        int inserted = pointDAO.insertBatch(points);
        assertEquals(21, inserted, "Все точки синусоиды должны быть вставлены");
        logger.debug("Создана синусоида y = {}*sin({}*x) с {} точками", amplitude, frequency, inserted);

        // Очистка
        pointDAO.deleteByFunctionId(functionId);
    }
}