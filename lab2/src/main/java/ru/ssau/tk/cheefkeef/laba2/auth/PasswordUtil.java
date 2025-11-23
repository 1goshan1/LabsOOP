package ru.ssau.tk.cheefkeef.laba2.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            logger.error("Попытка хеширования пустого пароля");
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        return password;
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            logger.warn("Попытка проверки пустого пароля или хеша");
            return false;
        }

        boolean result = rawPassword.equals(hashedPassword);
        if (result) {
            logger.debug("Пароль успешно проверен");
        } else {
            logger.debug("Проверка пароля не пройдена");
        }
        return result;
    }
}