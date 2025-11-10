package ru.ssau.tk.cheefkeef.laba2.auth;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    private static final int BCRYPT_LOG_ROUNDS = 12;

    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            logger.error("Попытка хеширования пустого пароля");
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
        logger.debug("Пароль успешно захеширован");
        return hashedPassword;
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            logger.warn("Попытка проверки пустого пароля или хеша");
            return false;
        }

        boolean result = BCrypt.checkpw(rawPassword, hashedPassword);
        if (result) {
            logger.debug("Пароль успешно проверен");
        } else {
            logger.debug("Проверка пароля не пройдена");
        }
        return result;
    }
}