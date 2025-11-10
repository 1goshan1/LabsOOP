package ru.ssau.tk.cheefkeef.laba2.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(BaseServlet.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected void writeJson(HttpServletResponse resp, int status, Object obj) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(obj));
        logger.debug("Отправлен JSON ответ со статусом {}: {}", status, obj);
    }

    protected void handleError(HttpServletResponse resp, int status, String message, String path) throws IOException {
        logger.warn("API ошибка [{}]: {} по пути {}", status, message, path);
        writeJson(resp, status, new ru.ssau.tk.cheefkeef.laba2.web.ErrorResponse(message, path));
    }

    protected Integer getIntegerParam(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Неверный формат параметра {}: {}", paramName, value);
            return null;
        }
    }

    protected Double getDoubleParam(HttpServletRequest req, String paramName) {
        String value = req.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.warn("Неверный формат параметра {}: {}", paramName, value);
            return null;
        }
    }

    protected boolean getBooleanParam(HttpServletRequest req, String paramName, boolean defaultValue) {
        String value = req.getParameter(paramName);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    protected void logRequest(HttpServletRequest req) {
        String queryString = req.getQueryString() != null ? "?" + req.getQueryString() : "";
        logger.info("{} {}{}", req.getMethod(), req.getRequestURI(), queryString);

        // Логирование заголовков для отладки
        if (logger.isDebugEnabled()) {
            logger.debug("Заголовки запроса:");
            req.getHeaderNames().asIterator().forEachRemaining(headerName ->
                    logger.debug("  {}: {}", headerName, req.getHeader(headerName))
            );
        }
    }
}