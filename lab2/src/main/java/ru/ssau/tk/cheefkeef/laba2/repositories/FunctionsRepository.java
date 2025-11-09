// FunctionsRepository.java - добавьте эти методы
package ru.ssau.tk.cheefkeef.laba2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;

import java.util.List;
import java.util.Optional;

@Repository
public interface FunctionsRepository extends JpaRepository<Functions, Long> {

    // Существующие методы...
    List<Functions> findByUserId(Long userId);
    List<Functions> findByName(String name);
    List<Functions> findByNameAndUserId(String name, Long userId);
    List<Functions> findByNameContainingIgnoreCase(String name);
    List<Functions> findBySignature(String signature);
    List<Functions> findBySignatureContaining(String signature);
    boolean existsByNameAndUserId(String name, Long userId);
    void deleteByUserId(Long userId);
    void deleteByNameAndUserId(String name, Long userId);

    // Поиск функций по нескольким ID пользователей
    List<Functions> findByUserIdIn(List<Long> userIds);

    // Поиск функций по userId с сортировкой по имени
    @Query("SELECT f FROM Functions f WHERE f.userId = :userId ORDER BY f.name")
    List<Functions> findByUserIdOrderByName(@Param("userId") Long userId);

    // Поиск функций по userId с сортировкой по ID (последние добавленные)
    @Query("SELECT f FROM Functions f WHERE f.userId = :userId ORDER BY f.id DESC")
    List<Functions> findRecentByUserId(@Param("userId") Long userId);

    // Подсчет количества функций у пользователя
    @Query("SELECT COUNT(f) FROM Functions f WHERE f.userId = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Поиск функций по части имени и userId
    @Query("SELECT f FROM Functions f WHERE f.userId = :userId AND LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Functions> findByUserIdAndNameContaining(@Param("userId") Long userId, @Param("name") String name);

    // Поиск дубликатов функций (одинаковое имя и сигнатура у одного пользователя)
    @Query("SELECT f FROM Functions f WHERE f.userId = :userId AND f.name = :name AND f.signature = :signature")
    List<Functions> findDuplicates(@Param("userId") Long userId, @Param("name") String name, @Param("signature") String signature);
}