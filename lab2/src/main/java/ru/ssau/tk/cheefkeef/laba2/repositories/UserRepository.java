package ru.ssau.tk.cheefkeef.laba2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.tk.cheefkeef.laba2.entities.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск пользователя по логину
    User findByLogin(String login);

    // Проверка существования пользователя по логину
    boolean existsByLogin(String login);

    // Поиск пользователей по роли
    List<User> findByRole(String role);

    // Поиск пользователей по логину (частичное совпадение)
    List<User> findByLoginContainingIgnoreCase(String login);

    // Кастомный запрос для поиска по логину и роли
    @Query("SELECT u FROM User u WHERE u.login = :login AND u.role = :role")
    Optional<User> findByLoginAndRole(@Param("login") String login, @Param("role") String role);

    // Кастомный запрос для подсчета пользователей по роли
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") String role);

    // Удаление пользователя по логину
    void deleteByLogin(String login);
}