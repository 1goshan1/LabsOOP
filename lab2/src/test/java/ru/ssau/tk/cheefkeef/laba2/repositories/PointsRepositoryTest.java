package ru.ssau.tk.cheefkeef.laba2.repositories;

import ru.ssau.tk.cheefkeef.laba2.entities.Points;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class PointsRepositoryTest {

    @Autowired
    private PointsRepository pointsRepository;

    private Points point1;
    private Points point2;
    private Points point3;
    private Points point4;
    private final Long functionId1 = 1L;
    private final Long functionId2 = 2L;

    @BeforeEach
    void setUp() {
        pointsRepository.deleteAll();

        // Создаем тестовые точки для первой функции
        point1 = new Points();
        point1.setX(1.0);
        point1.setY(2.0);
        point1.setFunctionId(functionId1);
        point1 = pointsRepository.save(point1);

        point2 = new Points();
        point2.setX(2.0);
        point2.setY(4.0);
        point2.setFunctionId(functionId1);
        point2 = pointsRepository.save(point2);

        point3 = new Points();
        point3.setX(3.0);
        point3.setY(6.0);
        point3.setFunctionId(functionId1);
        point3 = pointsRepository.save(point3);

        // Создаем точку для второй функции
        point4 = new Points();
        point4.setX(1.5);
        point4.setY(3.0);
        point4.setFunctionId(functionId2);
        point4 = pointsRepository.save(point4);
    }


    @Test
    void testPerformanceSelect() {
        Long functionId = 1L;

        // Подготовка: создаем 10к точек
        for (int i = 0; i < 10000; i++) {
            Points point = new Points();
            point.setX((double) i);
            point.setY((double) i * 2);
            point.setFunctionId(functionId);
            pointsRepository.save(point);
        }

        // Замер скорости SELECT
        long startTime = System.currentTimeMillis();

        List<Points> allPoints = pointsRepository.findAll();
        List<Points> functionPoints = pointsRepository.findByFunctionId(functionId);
        List<Points> rangePoints = pointsRepository.findByFunctionIdAndXBetween(functionId, 1000.0, 2000.0);
        Double minX = pointsRepository.findMinXByFunctionId(functionId);
        Double maxY = pointsRepository.findMaxYByFunctionId(functionId);
        long count = pointsRepository.countByFunctionId(functionId);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("PointsRepository SELECT operations (10k records): " + duration + " ms");
        assertTrue(duration < 1500, "SELECT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceInsert() {
        Long functionId = 1L;

        // Замер скорости INSERT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            Points point = new Points();
            point.setX((double) i);
            point.setY((double) i * 3);
            point.setFunctionId(functionId);
            pointsRepository.save(point);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("PointsRepository INSERT 10k records: " + duration + " ms");
        assertTrue(duration < 5000, "INSERT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceUpdate() {
        Long functionId = 1L;

        // Подготовка: создаем 10к точек
        for (int i = 0; i < 10000; i++) {
            Points point = new Points();
            point.setX((double) i);
            point.setY((double) i * 2);
            point.setFunctionId(functionId);
            pointsRepository.save(point);
        }

        // Замер скорости UPDATE
        long startTime = System.currentTimeMillis();

        List<Points> points = pointsRepository.findByFunctionId(functionId);
        for (Points point : points) {
            point.setY(point.getY() + 10.0);
            pointsRepository.save(point);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("PointsRepository UPDATE 10k records: " + duration + " ms");
        assertTrue(duration < 3000, "UPDATE operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceDelete() {
        Long functionId = 1L;

        // Подготовка: создаем 10к точек
        for (int i = 0; i < 10000; i++) {
            Points point = new Points();
            point.setX((double) i);
            point.setY((double) i * 2);
            point.setFunctionId(functionId);
            pointsRepository.save(point);
        }

        // Замер скорости DELETE
        long startTime = System.currentTimeMillis();

        pointsRepository.deleteByFunctionId(functionId);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("PointsRepository DELETE 10k records: " + duration + " ms");
        assertTrue(duration < 2000, "DELETE operations took too long: " + duration + " ms");
    }
    
    @Test
    void testFindByFunctionId() {
        List<Points> function1Points = pointsRepository.findByFunctionId(functionId1);
        assertNotNull(function1Points);
        assertEquals(3, function1Points.size());

        List<Points> function2Points = pointsRepository.findByFunctionId(functionId2);
        assertEquals(1, function2Points.size());
        assertEquals(1.5, function2Points.get(0).getX());

        List<Points> nonExistentFunctionPoints = pointsRepository.findByFunctionId(999L);
        assertTrue(nonExistentFunctionPoints.isEmpty());
    }

    @Test
    void testFindByFunctionIdOrderByX() {
        List<Points> sortedPoints = pointsRepository.findByFunctionIdOrderByX(functionId1);
        assertEquals(3, sortedPoints.size());

        // Проверяем сортировку по возрастанию X
        assertEquals(1.0, sortedPoints.get(0).getX());
        assertEquals(2.0, sortedPoints.get(1).getX());
        assertEquals(3.0, sortedPoints.get(2).getX());
    }

    @Test
    void testFindByFunctionIdOrderByXDesc() {
        List<Points> sortedPoints = pointsRepository.findByFunctionIdOrderByXDesc(functionId1);
        assertEquals(3, sortedPoints.size());

        // Проверяем сортировку по убыванию X
        assertEquals(3.0, sortedPoints.get(0).getX());
        assertEquals(2.0, sortedPoints.get(1).getX());
        assertEquals(1.0, sortedPoints.get(2).getX());
    }

    @Test
    void testFindByFunctionIdAndX() {
        Optional<Points> foundPoint = pointsRepository.findByFunctionIdAndX(functionId1, 2.0);
        assertTrue(foundPoint.isPresent());
        assertEquals(4.0, foundPoint.get().getY());

        Optional<Points> nonExistentPoint = pointsRepository.findByFunctionIdAndX(functionId1, 5.0);
        assertFalse(nonExistentPoint.isPresent());

        Optional<Points> wrongFunctionPoint = pointsRepository.findByFunctionIdAndX(functionId2, 2.0);
        assertFalse(wrongFunctionPoint.isPresent());
    }

    @Test
    void testFindByFunctionIdAndXBetween() {
        List<Points> pointsInRange = pointsRepository.findByFunctionIdAndXBetween(functionId1, 1.5, 2.5);
        assertEquals(1, pointsInRange.size());
        assertEquals(2.0, pointsInRange.get(0).getX());

        List<Points> allPointsInRange = pointsRepository.findByFunctionIdAndXBetween(functionId1, 0.0, 5.0);
        assertEquals(3, allPointsInRange.size());

        List<Points> noPointsInRange = pointsRepository.findByFunctionIdAndXBetween(functionId1, 10.0, 20.0);
        assertTrue(noPointsInRange.isEmpty());
    }

    @Test
    void testFindByFunctionIdAndYBetween() {
        List<Points> pointsInRange = pointsRepository.findByFunctionIdAndYBetween(functionId1, 3.0, 5.0);
        assertEquals(1, pointsInRange.size());
        assertEquals(4.0, pointsInRange.get(0).getY());

        List<Points> allPointsInRange = pointsRepository.findByFunctionIdAndYBetween(functionId1, 0.0, 10.0);
        assertEquals(3, allPointsInRange.size());

        List<Points> noPointsInRange = pointsRepository.findByFunctionIdAndYBetween(functionId1, 10.0, 20.0);
        assertTrue(noPointsInRange.isEmpty());
    }

    @Test
    void testExistsByFunctionIdAndX() {
        assertTrue(pointsRepository.existsByFunctionIdAndX(functionId1, 1.0));
        assertTrue(pointsRepository.existsByFunctionIdAndX(functionId1, 2.0));
        assertTrue(pointsRepository.existsByFunctionIdAndX(functionId2, 1.5));

        assertFalse(pointsRepository.existsByFunctionIdAndX(functionId1, 5.0));
        assertFalse(pointsRepository.existsByFunctionIdAndX(999L, 1.0));
    }

    @Test
    void testDeleteByFunctionId() {
        assertEquals(4, pointsRepository.count());

        pointsRepository.deleteByFunctionId(functionId1);

        assertEquals(1, pointsRepository.count());
        assertTrue(pointsRepository.findByFunctionId(functionId1).isEmpty());
        assertEquals(1, pointsRepository.findByFunctionId(functionId2).size());

        // Удаление несуществующей функции
        assertDoesNotThrow(() -> pointsRepository.deleteByFunctionId(999L));
        assertEquals(1, pointsRepository.count());
    }

    @Test
    void testDeleteByFunctionIdAndX() {
        assertEquals(4, pointsRepository.count());

        pointsRepository.deleteByFunctionIdAndX(functionId1, 2.0);

        assertEquals(3, pointsRepository.count());
        assertFalse(pointsRepository.existsByFunctionIdAndX(functionId1, 2.0));
        assertTrue(pointsRepository.existsByFunctionIdAndX(functionId1, 1.0));

        // Удаление несуществующей точки
        assertDoesNotThrow(() -> pointsRepository.deleteByFunctionIdAndX(functionId1, 5.0));
        assertEquals(3, pointsRepository.count());
    }

    @Test
    void testCountByFunctionId() {
        assertEquals(3, pointsRepository.countByFunctionId(functionId1));
        assertEquals(1, pointsRepository.countByFunctionId(functionId2));
        assertEquals(0, pointsRepository.countByFunctionId(999L));
    }

    @Test
    void testFindMinXByFunctionId() {
        Double minX = pointsRepository.findMinXByFunctionId(functionId1);
        assertNotNull(minX);
        assertEquals(1.0, minX);

        Double minXFunction2 = pointsRepository.findMinXByFunctionId(functionId2);
        assertEquals(1.5, minXFunction2);

        Double minXNonExistent = pointsRepository.findMinXByFunctionId(999L);
        assertNull(minXNonExistent);
    }

    @Test
    void testFindMaxXByFunctionId() {
        Double maxX = pointsRepository.findMaxXByFunctionId(functionId1);
        assertNotNull(maxX);
        assertEquals(3.0, maxX);

        Double maxXFunction2 = pointsRepository.findMaxXByFunctionId(functionId2);
        assertEquals(1.5, maxXFunction2);

        Double maxXNonExistent = pointsRepository.findMaxXByFunctionId(999L);
        assertNull(maxXNonExistent);
    }

    @Test
    void testFindMinYByFunctionId() {
        Double minY = pointsRepository.findMinYByFunctionId(functionId1);
        assertNotNull(minY);
        assertEquals(2.0, minY);

        Double minYFunction2 = pointsRepository.findMinYByFunctionId(functionId2);
        assertEquals(3.0, minYFunction2);

        Double minYNonExistent = pointsRepository.findMinYByFunctionId(999L);
        assertNull(minYNonExistent);
    }

    @Test
    void testFindMaxYByFunctionId() {
        Double maxY = pointsRepository.findMaxYByFunctionId(functionId1);
        assertNotNull(maxY);
        assertEquals(6.0, maxY);

        Double maxYFunction2 = pointsRepository.findMaxYByFunctionId(functionId2);
        assertEquals(3.0, maxYFunction2);

        Double maxYNonExistent = pointsRepository.findMaxYByFunctionId(999L);
        assertNull(maxYNonExistent);
    }

    @Test
    void testFindByFunctionIdAndYGreaterThan() {
        List<Points> pointsGreaterThan3 = pointsRepository.findByFunctionIdAndYGreaterThan(functionId1, 3.0);
        assertEquals(2, pointsGreaterThan3.size());

        // Проверяем сортировку по возрастанию Y
        assertEquals(4.0, pointsGreaterThan3.get(0).getY());
        assertEquals(6.0, pointsGreaterThan3.get(1).getY());

        List<Points> pointsGreaterThan10 = pointsRepository.findByFunctionIdAndYGreaterThan(functionId1, 10.0);
        assertTrue(pointsGreaterThan10.isEmpty());
    }

    @Test
    void testFindByFunctionIdAndYLessThan() {
        List<Points> pointsLessThan5 = pointsRepository.findByFunctionIdAndYLessThan(functionId1, 5.0);
        assertEquals(2, pointsLessThan5.size());

        // Проверяем сортировку по убыванию Y
        assertEquals(4.0, pointsLessThan5.get(0).getY());
        assertEquals(2.0, pointsLessThan5.get(1).getY());

        List<Points> pointsLessThan1 = pointsRepository.findByFunctionIdAndYLessThan(functionId1, 1.0);
        assertTrue(pointsLessThan1.isEmpty());
    }

    @Test
    void testFindByFunctionIdIn() {
        List<Long> functionIds = List.of(functionId1, functionId2);
        List<Points> points = pointsRepository.findByFunctionIdIn(functionIds);

        assertEquals(4, points.size());

        // Проверяем сортировку по functionId, затем по X
        assertEquals(functionId1, points.get(0).getFunctionId());
        assertEquals(1.0, points.get(0).getX());
        assertEquals(functionId1, points.get(1).getFunctionId());
        assertEquals(2.0, points.get(1).getX());
        assertEquals(functionId1, points.get(2).getFunctionId());
        assertEquals(3.0, points.get(2).getX());
        assertEquals(functionId2, points.get(3).getFunctionId());

        List<Points> emptyList = pointsRepository.findByFunctionIdIn(List.of(999L));
        assertTrue(emptyList.isEmpty());
    }

    @Test
    void testFindPointsPage() {
        // Создаем больше точек для тестирования пагинации
        for (int i = 4; i <= 10; i++) {
            Points point = new Points();
            point.setX((double) i);
            point.setY((double) i * 2);
            point.setFunctionId(functionId1);
            pointsRepository.save(point);
        }

        // Тестируем пагинацию
        Pageable firstPage = PageRequest.of(0, 3, Sort.by("x"));
        List<Points> firstPagePoints = pointsRepository.findPointsPage(functionId1, firstPage);

        assertEquals(3, firstPagePoints.size());
        assertEquals(1.0, firstPagePoints.get(0).getX());
        assertEquals(2.0, firstPagePoints.get(1).getX());
        assertEquals(3.0, firstPagePoints.get(2).getX());

        Pageable secondPage = PageRequest.of(1, 3, Sort.by("x"));
        List<Points> secondPagePoints = pointsRepository.findPointsPage(functionId1, secondPage);

        assertEquals(3, secondPagePoints.size());
        assertEquals(4.0, secondPagePoints.get(0).getX());
        assertEquals(5.0, secondPagePoints.get(1).getX());
        assertEquals(6.0, secondPagePoints.get(2).getX());
    }

    @Test
    void testSaveAndUpdate() {
        Points newPoint = new Points();
        newPoint.setX(5.0);
        newPoint.setY(10.0);
        newPoint.setFunctionId(functionId1);

        Points savedPoint = pointsRepository.save(newPoint);
        assertNotNull(savedPoint.getId());

        // Обновляем точку
        savedPoint.setY(15.0);
        Points updatedPoint = pointsRepository.save(savedPoint);

        assertEquals(15.0, updatedPoint.getY());

        // Проверяем, что обновление сохранилось
        Optional<Points> found = pointsRepository.findByFunctionIdAndX(functionId1, 5.0);
        assertTrue(found.isPresent());
        assertEquals(15.0, found.get().getY());
    }

    @Test
    void testFindAll() {
        List<Points> allPoints = pointsRepository.findAll();
        assertEquals(4, allPoints.size());
    }

    @Test
    void testComplexScenario() {
        // Создаем дополнительные точки для сложного сценария
        Points extraPoint1 = new Points();
        extraPoint1.setX(0.5);
        extraPoint1.setY(1.0);
        extraPoint1.setFunctionId(functionId1);
        pointsRepository.save(extraPoint1);

        Points extraPoint2 = new Points();
        extraPoint2.setX(4.0);
        extraPoint2.setY(8.0);
        extraPoint2.setFunctionId(functionId1);
        pointsRepository.save(extraPoint2);

        // Проверяем агрегатные функции
        assertEquals(0.5, pointsRepository.findMinXByFunctionId(functionId1));
        assertEquals(4.0, pointsRepository.findMaxXByFunctionId(functionId1));
        assertEquals(1.0, pointsRepository.findMinYByFunctionId(functionId1));
        assertEquals(8.0, pointsRepository.findMaxYByFunctionId(functionId1));

        // Проверяем поиск по диапазонам
        List<Points> middlePoints = pointsRepository.findByFunctionIdAndXBetween(functionId1, 1.0, 3.0);
        assertEquals(3, middlePoints.size());

        // Проверяем сортировку
        List<Points> sortedAsc = pointsRepository.findByFunctionIdOrderByX(functionId1);
        assertEquals(5, sortedAsc.size());
        assertEquals(0.5, sortedAsc.get(0).getX());

        // Удаляем точку и проверяем изменения
        pointsRepository.deleteByFunctionIdAndX(functionId1, 0.5);
        assertEquals(4, pointsRepository.countByFunctionId(functionId1));
        assertEquals(1.0, pointsRepository.findMinXByFunctionId(functionId1));
    }

    @Test
    void testEdgeCases() {
        // Поиск с граничными значениями
        List<Points> exactBoundary = pointsRepository.findByFunctionIdAndXBetween(functionId1, 1.0, 3.0);
        assertEquals(3, exactBoundary.size());

        // Поиск с одинаковыми границами
        List<Points> sameBoundaries = pointsRepository.findByFunctionIdAndXBetween(functionId1, 2.0, 2.0);
        assertEquals(1, sameBoundaries.size());

        // Проверка существования с отрицательными значениями
        assertFalse(pointsRepository.existsByFunctionIdAndX(functionId1, -1.0));

        // Поиск с null значениями (если поддерживается)
        // List<Points> nullXSearch = pointsRepository.findByFunctionIdAndXBetween(functionId1, null, null);
        // assertEquals(3, nullXSearch.size());
    }

    @Test
    void testNegativeAndZeroValues() {
        // Тестируем с отрицательными и нулевыми значениями
        Points negativePoint = new Points();
        negativePoint.setX(-2.0);
        negativePoint.setY(-4.0);
        negativePoint.setFunctionId(functionId1);
        pointsRepository.save(negativePoint);

        Points zeroPoint = new Points();
        zeroPoint.setX(0.0);
        zeroPoint.setY(0.0);
        zeroPoint.setFunctionId(functionId1);
        pointsRepository.save(zeroPoint);

        // Проверяем поиск с отрицательными значениями
        List<Points> negativeRange = pointsRepository.findByFunctionIdAndXBetween(functionId1, -3.0, -1.0);
        assertEquals(1, negativeRange.size());
        assertEquals(-2.0, negativeRange.get(0).getX());

        List<Points> includingZero = pointsRepository.findByFunctionIdAndXBetween(functionId1, -1.0, 1.0);
        assertEquals(2, includingZero.size()); // 0.0 и 1.0

        // Проверяем агрегатные функции с отрицательными значениями
        assertEquals(-2.0, pointsRepository.findMinXByFunctionId(functionId1));
    }
}