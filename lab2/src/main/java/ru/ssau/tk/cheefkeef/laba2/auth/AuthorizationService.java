package ru.ssau.tk.cheefkeef.laba2.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import javax.servlet.http.HttpServletRequest;

public class AuthorizationService {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationService.class);

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    public static boolean hasAccess(HttpServletRequest request, String requiredRole, Integer resourceId) {
        User currentUser = (User) request.getAttribute("currentUser");

        if (currentUser == null) {
            logger.warn("Попытка доступа без аутентификации");
            return false;
        }

        logger.debug("Проверка прав доступа для пользователя: {}, роль: {}, требуемая роль: {}",
                currentUser.getLogin(), currentUser.getRole(), requiredRole);

        // Админы имеют доступ ко всему
        if (ROLE_ADMIN.equals(currentUser.getRole())) {
            return true;
        }

        // Если указана роль, проверяем соответствие
        if (requiredRole != null && !currentUser.getRole().equals(requiredRole)) {
            logger.warn("Пользователь {} не имеет прав доступа. Требуется роль: {}",
                    currentUser.getLogin(), requiredRole);
            return false;
        }

        // Если указан resourceId и это не админ, нужно проверить принадлежность
        if (resourceId != null) {
            logger.debug("Проверка принадлежности ресурса ID: {} для пользователя ID: {}",
                    resourceId, currentUser.getId());
        }

        logger.debug("Доступ разрешен для пользователя: {}", currentUser.getLogin());
        return true;
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return currentUser != null && ROLE_ADMIN.equals(currentUser.getRole());
    }

    public static boolean isUser(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        return currentUser != null && ROLE_USER.equals(currentUser.getRole());
    }

    public static User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }
}