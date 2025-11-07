package ru.ssau.tk.cheefkeef.laba2.repositories;

import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class FunctionsRepositoryTest {

    @Autowired
    private FunctionsRepository functionsRepository;

    private Functions testFunction1;
    private Functions testFunction2;
    private Functions testFunction3;
    private final Long userId1 = 1L;
    private final Long userId2 = 2L;

    @BeforeEach
    void setUp() {
        functionsRepository.deleteAll();

        // Создаем тестовые функции для первого пользователя
        testFunction1 = new Functions();
        testFunction1.setName("calculateArea");
        testFunction1.setSignature("double calculateArea(double radius)");
        testFunction1.setUserId(userId1);
        testFunction1 = functionsRepository.save(testFunction1);

        testFunction2 = new Functions();
        testFunction2.setName("calculatePerimeter");
        testFunction2.setSignature("double calculatePerimeter(double radius)");
        testFunction2.setUserId(userId1);
        testFunction2 = functionsRepository.save(testFunction2);

        // Создаем функцию для второго пользователя
        testFunction3 = new Functions();
        testFunction3.setName("calculateArea");
        testFunction3.setSignature("double calculateArea(double side)");
        testFunction3.setUserId(userId2);
        testFunction3 = functionsRepository.save(testFunction3);
    }

    @Test
    void testPerformanceSelect() {
        Long userId = 1L;

        // Подготовка: создаем 10к функций
        for (int i = 0; i < 10000; i++) {
            Functions function = new Functions();
            function.setName("function_" + i);
            function.setSignature("void function_" + i + "()");
            function.setUserId(userId);
            functionsRepository.save(function);
        }

        // Замер скорости SELECT
        long startTime = System.currentTimeMillis();

        List<Functions> allFunctions = functionsRepository.findAll();
        List<Functions> userFunctions = functionsRepository.findByUserId(userId);
        List<Functions> namedFunctions = functionsRepository.findByNameContainingIgnoreCase("function_5000");
        long count = functionsRepository.countByUserId(userId);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("FunctionsRepository SELECT operations (10k records): " + duration + " ms");
        assertTrue(duration < 1000, "SELECT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceInsert() {
        Long userId = 1L;

        // Замер скорости INSERT
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 10000; i++) {
            Functions function = new Functions();
            function.setName("perf_function_" + i);
            function.setSignature("void perf_function_" + i + "()");
            function.setUserId(userId);
            functionsRepository.save(function);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("FunctionsRepository INSERT 10k records: " + duration + " ms");
        assertTrue(duration < 5000, "INSERT operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceUpdate() {
        Long userId = 1L;

        // Подготовка: создаем 10к функций
        for (int i = 0; i < 10000; i++) {
            Functions function = new Functions();
            function.setName("update_function_" + i);
            function.setSignature("void update_function_" + i + "()");
            function.setUserId(userId);
            functionsRepository.save(function);
        }

        // Замер скорости UPDATE
        long startTime = System.currentTimeMillis();

        List<Functions> functions = functionsRepository.findByUserId(userId);
        for (Functions function : functions) {
            function.setSignature("updated_signature");
            functionsRepository.save(function);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("FunctionsRepository UPDATE 10k records: " + duration + " ms");
        assertTrue(duration < 3000, "UPDATE operations took too long: " + duration + " ms");
    }

    @Test
    void testPerformanceDelete() {
        Long userId = 1L;

        // Подготовка: создаем 10к функций
        for (int i = 0; i < 10000; i++) {
            Functions function = new Functions();
            function.setName("delete_function_" + i);
            function.setSignature("void delete_function_" + i + "()");
            function.setUserId(userId);
            functionsRepository.save(function);
        }

        // Замер скорости DELETE
        long startTime = System.currentTimeMillis();

        functionsRepository.deleteByUserId(userId);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("FunctionsRepository DELETE 10k records: " + duration + " ms");
        assertTrue(duration < 2000, "DELETE operations took too long: " + duration + " ms");
    }

    @Test
    void testFindByUserId() {
        List<Functions> user1Functions = functionsRepository.findByUserId(userId1);
        assertNotNull(user1Functions);
        assertEquals(2, user1Functions.size());

        List<Functions> user2Functions = functionsRepository.findByUserId(userId2);
        assertEquals(1, user2Functions.size());
        assertEquals("calculateArea", user2Functions.get(0).getName());

        List<Functions> nonExistentUserFunctions = functionsRepository.findByUserId(999L);
        assertTrue(nonExistentUserFunctions.isEmpty());
    }

    @Test
    void testFindByName() {
        List<Functions> areaFunctions = functionsRepository.findByName("calculateArea");
        assertNotNull(areaFunctions);
        assertEquals(2, areaFunctions.size());

        List<Functions> perimeterFunctions = functionsRepository.findByName("calculatePerimeter");
        assertEquals(1, perimeterFunctions.size());
        assertEquals("calculatePerimeter", perimeterFunctions.get(0).getName());

        List<Functions> nonExistentFunctions = functionsRepository.findByName("nonexistent");
        assertTrue(nonExistentFunctions.isEmpty());
    }

    @Test
    void testFindByNameAndUserId() {
        List<Functions> user1AreaFunctions = functionsRepository.findByNameAndUserId("calculateArea", userId1);
        assertEquals(1, user1AreaFunctions.size());
        assertEquals("double calculateArea(double radius)", user1AreaFunctions.get(0).getSignature());

        List<Functions> user2AreaFunctions = functionsRepository.findByNameAndUserId("calculateArea", userId2);
        assertEquals(1, user2AreaFunctions.size());
        assertEquals("double calculateArea(double side)", user2AreaFunctions.get(0).getSignature());

        List<Functions> nonExistentCombination = functionsRepository.findByNameAndUserId("calculatePerimeter", userId2);
        assertTrue(nonExistentCombination.isEmpty());
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<Functions> calculateFunctions = functionsRepository.findByNameContainingIgnoreCase("calculate");
        assertEquals(3, calculateFunctions.size());

        List<Functions> areaFunctions = functionsRepository.findByNameContainingIgnoreCase("AREA");
        assertEquals(2, areaFunctions.size());

        List<Functions> perimeterFunctions = functionsRepository.findByNameContainingIgnoreCase("perimeter");
        assertEquals(1, perimeterFunctions.size());

        List<Functions> nonExistentFunctions = functionsRepository.findByNameContainingIgnoreCase("nonexistent");
        assertTrue(nonExistentFunctions.isEmpty());
    }

    @Test
    void testFindBySignature() {
        List<Functions> radiusSignature = functionsRepository.findBySignature("double calculateArea(double radius)");
        assertEquals(1, radiusSignature.size());
        assertEquals("calculateArea", radiusSignature.get(0).getName());

        List<Functions> nonExistentSignature = functionsRepository.findBySignature("nonexistent signature");
        assertTrue(nonExistentSignature.isEmpty());
    }

    @Test
    void testFindBySignatureContaining() {
        List<Functions> radiusFunctions = functionsRepository.findBySignatureContaining("radius");
        assertEquals(2, radiusFunctions.size());

        List<Functions> sideFunctions = functionsRepository.findBySignatureContaining("side");
        assertEquals(1, sideFunctions.size());

        List<Functions> calculateFunctions = functionsRepository.findBySignatureContaining("calculate");
        assertEquals(3, calculateFunctions.size());

        List<Functions> nonExistent = functionsRepository.findBySignatureContaining("nonexistent");
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testExistsByNameAndUserId() {
        assertTrue(functionsRepository.existsByNameAndUserId("calculateArea", userId1));
        assertTrue(functionsRepository.existsByNameAndUserId("calculatePerimeter", userId1));
        assertTrue(functionsRepository.existsByNameAndUserId("calculateArea", userId2));

        assertFalse(functionsRepository.existsByNameAndUserId("calculatePerimeter", userId2));
        assertFalse(functionsRepository.existsByNameAndUserId("nonexistent", userId1));
        assertFalse(functionsRepository.existsByNameAndUserId("calculateArea", 999L));
    }

    @Test
    void testDeleteByUserId() {
        assertEquals(3, functionsRepository.count());

        functionsRepository.deleteByUserId(userId1);

        assertEquals(1, functionsRepository.count());
        assertTrue(functionsRepository.findByUserId(userId1).isEmpty());
        assertEquals(1, functionsRepository.findByUserId(userId2).size());

        // Удаление несуществующего пользователя
        assertDoesNotThrow(() -> functionsRepository.deleteByUserId(999L));
        assertEquals(1, functionsRepository.count());
    }

    @Test
    void testDeleteByNameAndUserId() {
        assertEquals(3, functionsRepository.count());

        functionsRepository.deleteByNameAndUserId("calculateArea", userId1);

        assertEquals(2, functionsRepository.count());
        assertFalse(functionsRepository.existsByNameAndUserId("calculateArea", userId1));
        assertTrue(functionsRepository.existsByNameAndUserId("calculateArea", userId2));

        // Удаление несуществующей комбинации
        assertDoesNotThrow(() -> functionsRepository.deleteByNameAndUserId("nonexistent", userId1));
        assertEquals(2, functionsRepository.count());
    }

    @Test
    void testFindByUserIdOrderByName() {
        // Добавляем еще одну функцию для проверки сортировки
        Functions aFunction = new Functions();
        aFunction.setName("aFunction");
        aFunction.setSignature("void aFunction()");
        aFunction.setUserId(userId1);
        functionsRepository.save(aFunction);

        List<Functions> sortedFunctions = functionsRepository.findByUserIdOrderByName(userId1);
        assertEquals(3, sortedFunctions.size());

        // Проверяем сортировку по алфавиту
        assertEquals("aFunction", sortedFunctions.get(0).getName());
        assertEquals("calculateArea", sortedFunctions.get(1).getName());
        assertEquals("calculatePerimeter", sortedFunctions.get(2).getName());
    }

    @Test
    void testFindRecentByUserId() {
        // Создаем новую функцию (будет с наибольшим ID)
        Functions newFunction = new Functions();
        newFunction.setName("newFunction");
        newFunction.setSignature("void newFunction()");
        newFunction.setUserId(userId1);
        functionsRepository.save(newFunction);

        List<Functions> recentFunctions = functionsRepository.findRecentByUserId(userId1);
        assertEquals(3, recentFunctions.size());

        // Первой должна быть самая новая функция
        assertEquals("newFunction", recentFunctions.get(0).getName());
    }

    @Test
    void testCountByUserId() {
        assertEquals(2, functionsRepository.countByUserId(userId1));
        assertEquals(1, functionsRepository.countByUserId(userId2));
        assertEquals(0, functionsRepository.countByUserId(999L));
    }

    @Test
    void testFindByUserIdAndNameContaining() {
        List<Functions> user1AreaFunctions = functionsRepository.findByUserIdAndNameContaining(userId1, "area");
        assertEquals(1, user1AreaFunctions.size());
        assertEquals("calculateArea", user1AreaFunctions.get(0).getName());

        List<Functions> user1CalculateFunctions = functionsRepository.findByUserIdAndNameContaining(userId1, "calc");
        assertEquals(2, user1CalculateFunctions.size());

        List<Functions> user2AreaFunctions = functionsRepository.findByUserIdAndNameContaining(userId2, "AREA");
        assertEquals(1, user2AreaFunctions.size());

        List<Functions> nonExistent = functionsRepository.findByUserIdAndNameContaining(userId1, "nonexistent");
        assertTrue(nonExistent.isEmpty());
    }

    @Test
    void testFindDuplicates() {
        // Создаем дубликат функции
        Functions duplicate = new Functions();
        duplicate.setName("calculateArea");
        duplicate.setSignature("double calculateArea(double radius)");
        duplicate.setUserId(userId1);
        functionsRepository.save(duplicate);

        List<Functions> duplicates = functionsRepository.findDuplicates(userId1, "calculateArea", "double calculateArea(double radius)");
        assertEquals(2, duplicates.size());

        // Проверяем, что у обеих функций одинаковые имя и сигнатура
        for (Functions func : duplicates) {
            assertEquals("calculateArea", func.getName());
            assertEquals("double calculateArea(double radius)", func.getSignature());
            assertEquals(userId1, func.getUserId());
        }

        // Проверяем несуществующие дубликаты
        List<Functions> noDuplicates = functionsRepository.findDuplicates(userId1, "nonexistent", "signature");
        assertTrue(noDuplicates.isEmpty());
    }

    @Test
    void testSaveAndUpdate() {
        Functions newFunction = new Functions();
        newFunction.setName("testFunction");
        newFunction.setSignature("void test()");
        newFunction.setUserId(userId1);

        Functions savedFunction = functionsRepository.save(newFunction);
        assertNotNull(savedFunction.getId());

        // Обновляем функцию
        savedFunction.setName("updatedFunction");
        savedFunction.setSignature("void updated()");
        Functions updatedFunction = functionsRepository.save(savedFunction);

        assertEquals("updatedFunction", updatedFunction.getName());
        assertEquals("void updated()", updatedFunction.getSignature());

        // Проверяем, что обновление сохранилось
        List<Functions> found = functionsRepository.findByNameAndUserId("updatedFunction", userId1);
        assertEquals(1, found.size());
    }

    @Test
    void testFindAll() {
        List<Functions> allFunctions = functionsRepository.findAll();
        assertEquals(3, allFunctions.size());
    }

    @Test
    void testComplexScenario() {
        // Создаем дополнительные функции
        Functions func1 = new Functions();
        func1.setName("func1");
        func1.setSignature("void func1()");
        func1.setUserId(3L);
        functionsRepository.save(func1);

        Functions func2 = new Functions();
        func2.setName("func2");
        func2.setSignature("void func2()");
        func2.setUserId(3L);
        functionsRepository.save(func2);

        // Проверяем количество функций у пользователя 3
        assertEquals(2, functionsRepository.countByUserId(3L));

        // Проверяем поиск с сортировкой
        List<Functions> sorted = functionsRepository.findByUserIdOrderByName(3L);
        assertEquals(2, sorted.size());
        assertEquals("func1", sorted.get(0).getName());
        assertEquals("func2", sorted.get(1).getName());

        // Удаляем одну функцию
        functionsRepository.deleteByNameAndUserId("func1", 3L);

        // Проверяем итоговое состояние
        assertEquals(1, functionsRepository.countByUserId(3L));
        assertFalse(functionsRepository.existsByNameAndUserId("func1", 3L));
        assertTrue(functionsRepository.existsByNameAndUserId("func2", 3L));
    }

    @Test
    void testEdgeCases() {
        // Поиск с пустыми строками
        List<Functions> emptyNameSearch = functionsRepository.findByNameContainingIgnoreCase("");
        assertEquals(3, emptyNameSearch.size());

        List<Functions> emptySignatureSearch = functionsRepository.findBySignatureContaining("");
        assertEquals(3, emptySignatureSearch.size());

        // Проверка существования с пустыми параметрами
        assertFalse(functionsRepository.existsByNameAndUserId("", userId1));

        // Поиск с null значениями (если поддерживается)
        // List<Functions> nullNameSearch = functionsRepository.findByNameContainingIgnoreCase(null);
        // assertTrue(nullNameSearch.isEmpty());
    }
}