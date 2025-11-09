// FunctionsService.java
package ru.ssau.tk.cheefkeef.laba2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import ru.ssau.tk.cheefkeef.laba2.repositories.FunctionsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FunctionsService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionsService.class);

    @Autowired
    private FunctionsRepository functionsRepository;

    public List<Functions> findAll() {
        logger.debug("Получение списка всех функций");
        return functionsRepository.findAll();
    }

    public Optional<Functions> findById(Long id) {
        logger.debug("Поиск функции по ID: {}", id);
        return functionsRepository.findById(id);
    }

    public List<Functions> findByUserId(Long userId) {
        logger.debug("Поиск функций по ID пользователя: {}", userId);
        return functionsRepository.findByUserId(userId);
    }

    public List<Functions> findByName(String name) {
        logger.debug("Поиск функций по имени: {}", name);
        return functionsRepository.findByName(name);
    }

    public List<Functions> findByNameContaining(String name) {
        logger.debug("Поиск функций по части имени: {}", name);
        return functionsRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Functions> findByNameAndUserId(String name, Long userId) {
        logger.debug("Поиск функций по имени {} и ID пользователя {}", name, userId);
        return functionsRepository.findByNameAndUserId(name, userId);
    }

    public List<Functions> findByIds(List<Long> ids) {
        logger.debug("Поиск функций по IDs: {}", ids);
        return functionsRepository.findAllById(ids);
    }

    public List<Functions> findByUserIds(List<Long> userIds) {
        logger.debug("Поиск функций по IDs пользователей: {}", userIds);
        return functionsRepository.findByUserIdIn(userIds);
    }

    public Functions save(Functions function) {
        logger.debug("Сохранение функции: {} для пользователя {}", function.getName(), function.getUserId());
        return functionsRepository.save(function);
    }

    public void deleteById(Long id) {
        logger.debug("Удаление функции по ID: {}", id);
        functionsRepository.deleteById(id);
    }

    public void deleteByUserId(Long userId) {
        logger.debug("Удаление всех функций пользователя с ID: {}", userId);
        functionsRepository.deleteByUserId(userId);
    }

    public boolean existsByNameAndUserId(String name, Long userId) {
        logger.debug("Проверка существования функции с именем {} для пользователя {}", name, userId);
        return functionsRepository.existsByNameAndUserId(name, userId);
    }

    public long countByUserId(Long userId) {
        logger.debug("Подсчет количества функций для пользователя с ID: {}", userId);
        return functionsRepository.countByUserId(userId);
    }

    public List<Functions> findByUserIdAndNamePattern(Long userId, String namePattern) {
        logger.debug("Поиск функций по ID пользователя {} и шаблону имени: {}", userId, namePattern);
        if (namePattern != null && !namePattern.trim().isEmpty()) {
            return functionsRepository.findByUserIdAndNameContaining(userId, namePattern);
        } else {
            return functionsRepository.findByUserId(userId);
        }
    }
}