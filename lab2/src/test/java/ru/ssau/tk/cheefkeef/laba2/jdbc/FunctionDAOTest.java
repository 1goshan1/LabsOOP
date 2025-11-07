package ru.ssau.tk.cheefkeef.laba2.jdbc;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.models.Function;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FunctionDAOTest {
    private static final Logger logger = LoggerFactory.getLogger(FunctionDAOTest.class);
    private static FunctionDAO functionDAO;
    private static Faker faker;
    private static Function testFunction;
    private static Integer testUserId;

    @BeforeAll
    static void setUp() {
        logger.info("Инициализация тестового окружения FunctionDAO");


        functionDAO = new FunctionDAO();
        faker = new Faker(new Locale("en"));

        // Создаем тестового пользователя (предполагаем, что пользователь с ID 1 существует)
        testUserId = 1; // Можно изменить на существующий ID пользователя

        // Создаем тестовую функцию
        testFunction = createRandomFunction(testUserId);

        logger.debug("Создана тестовая функция: {}", testFunction);
    }

    @AfterAll
    static void tearDown() {
        logger.info("Завершение тестов functionDAO");
    }

    private static Function createRandomFunction(Integer userId) {
        String functionName = "test_func_" +
                faker.lorem().word().replace(" ", "_") +
                "_" + System.currentTimeMillis();

        String signature = generateRandomSignature();

        return new Function(userId, functionName, signature);
    }

    private static String generateRandomSignature() {
        String[] f = {"x", "1", "2", "4", "x^2", "x1"};
        return faker.options().option(f);
    }

    @Test
    @Order(1)
    void testInsertFunction() {
        logger.info("Запуск теста: создание функции");

        Function insertedFunction = functionDAO.insert(testFunction);

        assertNotNull(insertedFunction, "Созданная функция не должна быть null");
        assertNotNull(insertedFunction.getId(), "ID созданной функции не должен быть null");
        assertEquals(testFunction.getUserId(), insertedFunction.getUserId(), "User ID должны совпадать");
        assertEquals(testFunction.getName(), insertedFunction.getName(), "Имена функций должны совпадать");
        assertEquals(testFunction.getSignature(), insertedFunction.getSignature(), "Сигнатуры должны совпадать");

        testFunction.setId(insertedFunction.getId());
        logger.info("Успешно создана функция с ID: {}", insertedFunction.getId());
    }

    @Test
    @Order(2)
    void testFindById() {
        logger.info("Запуск теста: поиск функции по ID");

        Optional<Function> foundFunctionOpt = functionDAO.findById(testFunction.getId());

        assertTrue(foundFunctionOpt.isPresent(), "Функция должна быть найдена по ID");
        Function foundFunction = foundFunctionOpt.get();

        assertEquals(testFunction.getId(), foundFunction.getId(), "ID должны совпадать");
        assertEquals(testFunction.getUserId(), foundFunction.getUserId(), "User ID должны совпадать");
        assertEquals(testFunction.getName(), foundFunction.getName(), "Имена функций должны совпадать");
        assertEquals(testFunction.getSignature(), foundFunction.getSignature(), "Сигнатуры должны совпадать");

        logger.debug("Найдена функция: {}", foundFunction);
    }

    @Test
    @Order(3)
    void testFindByUserId() {
        logger.info("Запуск теста: поиск функций по ID пользователя");

        List<Function> functions = functionDAO.findByUserId(testUserId);

        assertNotNull(functions, "Список функций не должен быть null");

        // Проверяем, что наша тестовая функция есть в списке
        boolean found = functions.stream()
                .anyMatch(func -> func.getId().equals(testFunction.getId()));
        assertTrue(found, "Тестовая функция должна присутствовать в списке");

        logger.info("Найдено {} функций для пользователя с ID: {}", functions.size(), testUserId);
    }

    @Test
    @Order(4)
    void testFindByName() {
        logger.info("Запуск теста: поиск функций по имени (частичное совпадение)");

        // Ищем по части имени
        String searchName = testFunction.getName().substring(0, 8);
        List<Function> functions = functionDAO.findByName(searchName);

        assertNotNull(functions, "Список функций не должен быть null");

        boolean found = functions.stream()
                .anyMatch(func -> func.getId().equals(testFunction.getId()));
        assertTrue(found, "Тестовая функция должна быть найдена по частичному имени");

        logger.info("Найдено {} функций по частичному имени '{}'", functions.size(), searchName);
    }

    @Test
    @Order(5)
    void testFindByNameAndUserId() {
        logger.info("Запуск теста: поиск функций по имени и ID пользователя");

        List<Function> functions = functionDAO.findByNameAndUserId(
                testFunction.getName(), testUserId);

        assertNotNull(functions, "Список функций не должен быть null");
        assertFalse(functions.isEmpty(), "Список функций не должен быть пустым");

        Function foundFunction = functions.get(0);
        assertEquals(testFunction.getId(), foundFunction.getId(), "ID должны совпадать");
        assertEquals(testFunction.getName(), foundFunction.getName(), "Имена функций должны совпадать");
        assertEquals(testFunction.getUserId(), foundFunction.getUserId(), "User ID должны совпадать");

        logger.debug("Найдена функция по имени и пользователю: {}", foundFunction);
    }

    @Test
    @Order(6)
    void testExistsByNameAndUserId() {
        logger.info("Запуск теста: проверка существования функции по имени и пользователю");

        boolean exists = functionDAO.existsByNameAndUserId(
                testFunction.getName(), testUserId);

        assertTrue(exists, "Функция должна существовать с данным именем и пользователем");

        // Проверяем несуществующую комбинацию
        boolean notExists = functionDAO.existsByNameAndUserId(
                "non_existent_function_name", testUserId);
        assertFalse(notExists, "Несуществующая функция не должна быть найдена");

        logger.info("Проверка существования функции завершена успешно");
    }

    @Test
    @Order(7)
    void testUpdateFunction() {
        logger.info("Запуск теста: обновление функции");

        // Генерируем новые данные
        String newName = "updated_func_" + System.currentTimeMillis();
        String newSignature = generateRandomSignature();

        Function functionToUpdate = new Function(
                testUserId + 1, // Изменяем пользователя для теста
                newName,
                newSignature
        );
        functionToUpdate.setId(testFunction.getId());

        boolean updateResult = functionDAO.update(functionToUpdate);

        assertTrue(updateResult, "Обновление функции должно быть успешным");

        // Проверяем, что данные обновились
        Optional<Function> updatedFunctionOpt = functionDAO.findById(testFunction.getId());
        assertTrue(updatedFunctionOpt.isPresent(), "Обновленная функция должна существовать");
        Function updatedFunction = updatedFunctionOpt.get();

        assertEquals(newName, updatedFunction.getName(), "Имя функции должно быть обновлено");
        assertEquals(testUserId + 1, updatedFunction.getUserId(), "User ID должен быть обновлен");
        assertEquals(newSignature, updatedFunction.getSignature(), "Сигнатура должна быть обновлена");

        testFunction = updatedFunction; // Обновляем ссылку на тестовую функцию
        logger.info("Функция успешно обновлена: {}", updatedFunction);
    }

    @Test
    @Order(8)
    void testUpdateSignature() {
        logger.info("Запуск теста: обновление только сигнатуры функции");

        String newSignature = "String processData(int input, String config)";

        boolean updateResult = functionDAO.updateSignature(testFunction.getId(), newSignature);

        assertTrue(updateResult, "Обновление сигнатуры должно быть успешным");

        // Проверяем, что только сигнатура обновилась
        Optional<Function> updatedFunctionOpt = functionDAO.findById(testFunction.getId());
        assertTrue(updatedFunctionOpt.isPresent());
        Function updatedFunction = updatedFunctionOpt.get();

        assertEquals(newSignature, updatedFunction.getSignature(), "Сигнатура должна быть обновлена");
        assertEquals(testFunction.getName(), updatedFunction.getName(), "Имя функции должно остаться прежним");
        assertEquals(testFunction.getUserId(), updatedFunction.getUserId(), "User ID должен остаться прежним");

        testFunction.setSignature(newSignature);
        logger.info("Сигнатура функции успешно обновлена");
    }

    @Test
    @Order(9)
    void testCountByUserId() {
        logger.info("Запуск теста: подсчет количества функций пользователя");

        int countBefore = functionDAO.countByUserId(testFunction.getUserId());

        assertTrue(countBefore >= 1, "Должна быть хотя бы одна функция у пользователя");

        // Создаем еще одну функцию для этого пользователя
        Function additionalFunction = createRandomFunction(testFunction.getUserId());
        functionDAO.insert(additionalFunction);

        int countAfter = functionDAO.countByUserId(testFunction.getUserId());
        assertEquals(countBefore + 1, countAfter, "Количество функций должно увеличиться на 1");

        // Удаляем дополнительную функцию
        functionDAO.delete(additionalFunction.getId());

        logger.info("Подсчет функций: до={}, после={}", countBefore, countAfter);
    }

    @Test
    @Order(10)
    void testInsertMultipleFunctionsWithFaker() {
        logger.info("Запуск теста: создание нескольких функций с Faker");

        int numberOfFunctions = 5;
        for (int i = 0; i < numberOfFunctions; i++) {
            Function randomFunction = createRandomFunction(testUserId);
            Function insertedFunction = functionDAO.insert(randomFunction);

            assertNotNull(insertedFunction, "Созданная функция не должна быть null");
            assertNotNull(insertedFunction.getId(), "ID созданной функции не должен быть null");

            logger.debug("Создана случайная функция #{}/{}: {}", i + 1, numberOfFunctions, insertedFunction);

            // Сохраняем ID для последующей очистки
            if (i == 0) {
                // Первую созданную функцию будем использовать в других тестах
                testFunction = insertedFunction;
            }
        }

        logger.info("Успешно создано {} случайных функций", numberOfFunctions);
    }

    @Test
    @Order(11)
    void testFindAll() {
        logger.info("Запуск теста: получение всех функций");

        List<Function> functions = functionDAO.findAll();

        assertNotNull(functions, "Список функций не должен быть null");
        assertFalse(functions.isEmpty(), "Список функций не должен быть пустым");

        // Проверяем, что наша тестовая функция есть в списке
        boolean found = functions.stream()
                .anyMatch(func -> func.getId().equals(testFunction.getId()));
        assertTrue(found, "Тестовая функция должна присутствовать в списке");

        logger.info("Найдено {} функций в базе данных", functions.size());

        // Логируем первые несколько функций для отладки
        functions.stream().limit(3).forEach(func ->
                logger.debug("Функция из общего списка: {}", func));
    }

    @Test
    @Order(12)
    void testEdgeCases() {
        logger.info("Запуск теста: проверка граничных случаев");

        // Поиск несуществующей функции
        Optional<Function> nonExistentFunction = functionDAO.findById(-1);
        assertFalse(nonExistentFunction.isPresent(), "Несуществующая функция должна возвращать Optional.empty()");

        // Поиск функций несуществующего пользователя
        List<Function> nonExistentUserFunctions = functionDAO.findByUserId(-1);
        assertNotNull(nonExistentUserFunctions, "Список функций несуществующего пользователя не должен быть null");
        assertTrue(nonExistentUserFunctions.isEmpty(), "Список функций несуществующего пользователя должен быть пустым");

        // Поиск по несуществующему имени
        List<Function> nonExistentNameFunctions = functionDAO.findByName("non_existent_function_name_12345");
        assertNotNull(nonExistentNameFunctions, "Список функций по несуществующему имени не должен быть null");
        assertTrue(nonExistentNameFunctions.isEmpty(), "Список функций по несуществующему имени должен быть пустым");

        // Обновление несуществующей функции
        Function nonExistentFunc = new Function(1, "test", "void test()");
        nonExistentFunc.setId(-1);
        boolean updateResult = functionDAO.update(nonExistentFunc);
        assertFalse(updateResult, "Обновление несуществующей функции должно возвращать false");

        // Обновление сигнатуры несуществующей функции
        boolean updateSignatureResult = functionDAO.updateSignature(-1, "void test()");
        assertFalse(updateSignatureResult, "Обновление сигнатуры несуществующей функции должно возвращать false");

        logger.info("Все граничные случаи обработаны корректно");
    }

    @Test
    @Order(13)
    void testFunctionWithSpecialCharacters() {
        logger.info("Запуск теста: создание функции со специальными символами");

        Function specialFunction = new Function(
                testUserId,
                "function_with_special_测试_тест_" + System.currentTimeMillis(),
                "List<String> processData(Map<String, Object> input, int maxSize) throws IOException"
        );

        Function insertedFunction = functionDAO.insert(specialFunction);
        assertNotNull(insertedFunction, "Функция со специальными символами должна быть создана");

        // Проверяем, что можем найти созданную функцию
        Optional<Function> foundFunction = functionDAO.findById(insertedFunction.getId());
        assertTrue(foundFunction.isPresent(), "Функция со специальными символами должна быть найдена");

        // Очистка
        if (insertedFunction != null && insertedFunction.getId() != null) {
            functionDAO.delete(insertedFunction.getId());
        }

        logger.info("Тест со специальными символами завершен успешно");
    }

    @Test
    @Order(14)
    void testDeleteByUserId() {
        logger.info("Запуск теста: удаление всех функций пользователя");

        // Создаем временного пользователя (предполагаем, что пользователь с ID 999 существует или создаем его)
        Integer tempUserId = 999;

        // Создаем несколько функций для этого пользователя
        int functionsToCreate = 3;
        for (int i = 0; i < functionsToCreate; i++) {
            Function tempFunction = createRandomFunction(tempUserId);
            functionDAO.insert(tempFunction);
        }

        // Проверяем, что функции созданы
        int countBefore = functionDAO.countByUserId(tempUserId);
        assertTrue(countBefore >= functionsToCreate, "Должны быть созданы функции для тестового пользователя");

        // Удаляем все функции пользователя
        boolean deleteResult = functionDAO.deleteByUserId(tempUserId);
        assertTrue(deleteResult, "Удаление функций пользователя должно быть успешным");

        // Проверяем, что функции удалены
        int countAfter = functionDAO.countByUserId(tempUserId);
        assertEquals(0, countAfter, "Все функции пользователя должны быть удалены");

        logger.info("Успешно удалено {} функций пользователя с ID: {}", countBefore, tempUserId);
    }

    @Test
    @Order(15)
    void testDeleteFunction() {
        logger.info("Запуск теста: удаление основной тестовой функции по ID");

        boolean deleteResult = functionDAO.delete(testFunction.getId());
        assertTrue(deleteResult, "Удаление функции по ID должно быть успешным");

        // Проверяем, что функция действительно удалена
        Optional<Function> deletedFunction = functionDAO.findById(testFunction.getId());
        assertFalse(deletedFunction.isPresent(), "Удаленная функция не должна быть найдена");

        logger.info("Основная тестовая функция с ID {} успешно удалена", testFunction.getId());
    }

    @Test
    @Order(16)
    void testPerformanceFindAll() {
        logger.info("Запуск теста производительности: поиск всех функций");

        long startTime = System.currentTimeMillis();
        List<Function> functions = functionDAO.findAll();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        logger.info("Поиск всех функций выполнен за {} мс. Найдено {} записей", duration, functions.size());

        assertTrue(duration < 5000, "Поиск всех функций должен выполняться менее чем за 5 секунд");
    }
}