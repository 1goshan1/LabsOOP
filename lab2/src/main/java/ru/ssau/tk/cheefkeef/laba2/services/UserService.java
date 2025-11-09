// UserService.java
package ru.ssau.tk.cheefkeef.laba2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssau.tk.cheefkeef.laba2.entities.User;
import ru.ssau.tk.cheefkeef.laba2.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        logger.debug("Получение списка всех пользователей");
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        logger.debug("Поиск пользователя по ID: {}", id);
        return userRepository.findById(id);
    }

    public User findByLogin(String login) {
        logger.debug("Поиск пользователя по логину: {}", login);
        return userRepository.findByLogin(login);
    }

    public List<User> findByRole(String role) {
        logger.debug("Поиск пользователей по роли: {}", role);
        return userRepository.findByRole(role);
    }

    public List<User> findByIds(List<Long> ids) {
        logger.debug("Поиск пользователей по IDs: {}", ids);
        return userRepository.findAllById(ids);
    }

    public User save(User user) {
        logger.debug("Сохранение пользователя: {}", user.getLogin());
        return userRepository.save(user);
    }

    public void deleteById(Long id) {
        logger.debug("Удаление пользователя по ID: {}", id);
        userRepository.deleteById(id);
    }

    public void deleteByLogin(String login) {
        logger.debug("Удаление пользователя по логину: {}", login);
        userRepository.deleteByLogin(login);
    }

    public boolean existsByLogin(String login) {
        logger.debug("Проверка существования пользователя с логином: {}", login);
        return userRepository.existsByLogin(login);
    }

}