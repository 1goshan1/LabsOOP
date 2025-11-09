// SecurityService.java
package ru.ssau.tk.cheefkeef.laba2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.ssau.tk.cheefkeef.laba2.entities.Functions;
import ru.ssau.tk.cheefkeef.laba2.entities.User;

@Service
public class SecurityService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FunctionsService functionsService;

    public boolean canAccessUser(Long userId, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userService.findByLogin(currentUsername);

        if (currentUser == null) {
            return false;
        }

        // Админы и менеджеры имеют доступ ко всем пользователям
        if (currentUser.getRole().equals("ADMIN") || currentUser.getRole().equals("MANAGER")) {
            logger.debug("Доступ к пользователю {} разрешен для {}", userId, currentUsername);
            return true;
        }

        // Пользователи имеют доступ только к своим данным
        boolean canAccess = currentUser.getId().equals(userId);
        logger.debug("Доступ пользователя {} к данным пользователя {}: {}",
                currentUsername, userId, canAccess);
        return canAccess;
    }

    public boolean canAccessFunction(Long functionId, Authentication authentication) {
        String currentUsername = authentication.getName();
        User currentUser = userService.findByLogin(currentUsername);

        if (currentUser == null) {
            return false;
        }

        // Админы имеют доступ ко всем функциям
        if (currentUser.getRole().equals("ADMIN")) {
            return true;
        }

        // Проверяем, принадлежит ли функция текущему пользователю
        Functions function = functionsService.findById(functionId).orElse(null);
        if (function == null) {
            return false;
        }

        boolean canAccess = function.getUserId().equals(currentUser.getId());
        logger.debug("Доступ пользователя {} к функции {}: {}",
                currentUsername, functionId, canAccess);
        return canAccess;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userService.findByLogin(username);
    }

    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    public String getCurrentUserRole() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getRole() : null;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    public boolean isManager() {
        String role = getCurrentUserRole();
        return "MANAGER".equals(role) || "ADMIN".equals(role);
    }

    public boolean isUser() {
        String role = getCurrentUserRole();
        return "USER".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role);
    }
}