package ru.ssau.tk.cheefkeef.laba2.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.jdbc.UserDAO;
import ru.ssau.tk.cheefkeef.laba2.models.User;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@WebFilter(urlPatterns = {"/api/v1/*"})
public class BasicAuthFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);
    private UserDAO userDAO;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        userDAO = new UserDAO();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        logger.info("Попытка аутентификации для запроса: {} {}",
                httpRequest.getMethod(), httpRequest.getRequestURI().replace("//", "/"));

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            try {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                String credentials = new String(Base64.getDecoder().decode(base64Credentials), "UTF-8");
                final String[] values = credentials.split(":", 2);

                if (values.length == 2) {
                    String login = values[0];
                    String password = values[1];

                    User user = userDAO.findByLogin(login);

                    if (user != null && user.getEnabled() && PasswordUtil.verifyPassword(password, user.getPassword())) {
                        httpRequest.setAttribute("currentUser", user);
                        logger.info("Успешная аутентификация пользователя: {}", login);
                        chain.doFilter(request, response);
                        return;
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при обработке аутентификации", e);
            }
        }

        logger.warn("Неудачная аутентификация для запроса: {} {}",
                httpRequest.getMethod(), httpRequest.getRequestURI().replace("//", "/"));
        httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"laba2 API\"");
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется аутентификация");
    }

    @Override
    public void destroy() {
        // nothing to do
    }
}