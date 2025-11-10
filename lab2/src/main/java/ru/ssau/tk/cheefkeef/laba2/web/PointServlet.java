package ru.ssau.tk.cheefkeef.laba2.web;

import ru.ssau.tk.cheefkeef.laba2.jdbc.PointDAO;
import ru.ssau.tk.cheefkeef.laba2.models.Point;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/points/*")
public class PointServlet extends HttpServlet {

    private final PointDAO pointDAO = new PointDAO();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/points — все точки
            List<Point> points = pointDAO.findAll();
            resp.getWriter().write(objectMapper.writeValueAsString(points));
        } else {
            // GET /api/points/{id}
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                var pointOpt = pointDAO.findById(id);
                if (pointOpt.isPresent()) {
                    resp.getWriter().write(objectMapper.writeValueAsString(pointOpt.get()));
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
        Point point = objectMapper.readValue(req.getInputStream(), Point.class);
        Point saved = pointDAO.insert(point);
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
            Point point = objectMapper.readValue(req.getInputStream(), Point.class);
            point.setId(id);
            if (pointDAO.update(point)) {
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
            if (pointDAO.delete(id)) {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}