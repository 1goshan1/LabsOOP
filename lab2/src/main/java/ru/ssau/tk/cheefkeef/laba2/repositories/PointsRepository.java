package ru.ssau.tk.cheefkeef.laba2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.tk.cheefkeef.laba2.entities.Points;

import java.util.List;
import java.util.Optional;

@Repository
public interface PointsRepository extends JpaRepository<Points, Long> {

    // Поиск всех точек по functionId
    List<Points> findByFunctionId(Long functionId);

    // Поиск точек по functionId с сортировкой по X
    List<Points> findByFunctionIdOrderByX(Long functionId);

    // Поиск точек по functionId с сортировкой по X (по убыванию)
    List<Points> findByFunctionIdOrderByXDesc(Long functionId);

    // Поиск точки по конкретным functionId и X значению
    Optional<Points> findByFunctionIdAndX(Long functionId, Double x);

    // Поиск точек по диапазону X значений для определенной функции
    List<Points> findByFunctionIdAndXBetween(Long functionId, Double xStart, Double xEnd);

    // Поиск точек по диапазону Y значений для определенной функции
    List<Points> findByFunctionIdAndYBetween(Long functionId, Double yStart, Double yEnd);

    // Проверка существования точки с определенным X для функции
    boolean existsByFunctionIdAndX(Long functionId, Double x);

    // Удаление всех точек по functionId
    void deleteByFunctionId(Long functionId);

    // Удаление конкретной точки по functionId и X
    void deleteByFunctionIdAndX(Long functionId, Double x);

    // Кастомные запросы

    // Подсчет количества точек у функции
    @Query("SELECT COUNT(p) FROM Points p WHERE p.functionId = :functionId")
    long countByFunctionId(@Param("functionId") Long functionId);

    // Поиск минимального X для функции
    @Query("SELECT MIN(p.x) FROM Points p WHERE p.functionId = :functionId")
    Double findMinXByFunctionId(@Param("functionId") Long functionId);

    // Поиск максимального X для функции
    @Query("SELECT MAX(p.x) FROM Points p WHERE p.functionId = :functionId")
    Double findMaxXByFunctionId(@Param("functionId") Long functionId);

    // Поиск минимального Y для функции
    @Query("SELECT MIN(p.y) FROM Points p WHERE p.functionId = :functionId")
    Double findMinYByFunctionId(@Param("functionId") Long functionId);

    // Поиск максимального Y для функции
    @Query("SELECT MAX(p.y) FROM Points p WHERE p.functionId = :functionId")
    Double findMaxYByFunctionId(@Param("functionId") Long functionId);

    // Поиск точек с Y больше определенного значения
    @Query("SELECT p FROM Points p WHERE p.functionId = :functionId AND p.y > :yValue ORDER BY p.y")
    List<Points> findByFunctionIdAndYGreaterThan(@Param("functionId") Long functionId, @Param("yValue") Double yValue);

    // Поиск точек с Y меньше определенного значения
    @Query("SELECT p FROM Points p WHERE p.functionId = :functionId AND p.y < :yValue ORDER BY p.y DESC")
    List<Points> findByFunctionIdAndYLessThan(@Param("functionId") Long functionId, @Param("yValue") Double yValue);

    // Поиск точек по нескольким functionId
    @Query("SELECT p FROM Points p WHERE p.functionId IN :functionIds ORDER BY p.functionId, p.x")
    List<Points> findByFunctionIdIn(@Param("functionIds") List<Long> functionIds);

    // Поиск точек с пагинацией
    @Query("SELECT p FROM Points p WHERE p.functionId = :functionId ORDER BY p.x")
    List<Points> findPointsPage(@Param("functionId") Long functionId, org.springframework.data.domain.Pageable pageable);

}