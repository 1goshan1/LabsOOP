package ru.ssau.tk.cheefkeef.laba2.web;

import ru.ssau.tk.cheefkeef.laba2.jdbc.FunctionDAO;
import ru.ssau.tk.cheefkeef.laba2.models.Function;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/functions/*")
public class FunctionServlet extends HttpServlet {

    private final FunctionDAO functionDAO = new FunctionDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/functions — все функции
            List<Function> functions = functionDAO.findAll();
            resp.getWriter().write(objectMapper.writeValueAsString(functions));
        } else {
            // GET /api/functions/{id}
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                var functionOpt = functionDAO.findById(id);
                if (functionOpt.isPresent()) {
                    resp.getWriter().write(objectMapper.writeValueAsString(functionOpt.get()));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Function function = objectMapper.readValue(req.getInputStream(), Function.class);
        Function saved = functionDAO.insert(function);
        if (saved != null && saved.getId() != null) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(objectMapper.writeValueAsString(saved));
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Function function = objectMapper.readValue(req.getInputStream(), Function.class);
            function.setId(id);
            if (functionDAO.update(function)) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.length() <= 1) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            if (functionDAO.delete(id)) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}