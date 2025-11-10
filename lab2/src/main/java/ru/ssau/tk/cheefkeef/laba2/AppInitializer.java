package ru.ssau.tk.cheefkeef.laba2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.auth.PasswordUtil;
import ru.ssau.tk.cheefkeef.laba2.jdbc.UserDAO;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(AppInitializer.class);
    private UserDAO userDAO = new UserDAO();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Инициализация приложения...");

        // Создание администратора по умолчанию, если еще не существует
        createDefaultAdmin();

        logger.info("Приложение успешно инициализировано");
    }

    private void createDefaultAdmin() {
        String adminLogin = "admin";
        User admin = userDAO.findByLogin(adminLogin);

        if (admin == null) {
            logger.info("Создание администратора по умолчанию");
            User newAdmin = new User();
            newAdmin.setLogin(adminLogin);
            newAdmin.setPassword(PasswordUtil.hashPassword("admin123"));
            newAdmin.setRole("admin");
            newAdmin.setEnabled(true);

            User createdAdmin = userDAO.insert(newAdmin);
            if (createdAdmin != null) {
                logger.info("Администратор успешно создан с логином 'admin' и паролем 'admin123'");
            } else {
                logger.error("Не удалось создать администратора по умолчанию");
            }
        } else {
            logger.info("Администратор уже существует в базе данных");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Приложение останавливается...");
    }
}