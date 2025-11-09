// PointsService.java
package ru.ssau.tk.cheefkeef.laba2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ssau.tk.cheefkeef.laba2.dto.points.PointCoordinate;
import ru.ssau.tk.cheefkeef.laba2.entities.Points;
import ru.ssau.tk.cheefkeef.laba2.repositories.PointsRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PointsService {
    private static final Logger logger = LoggerFactory.getLogger(PointsService.class);

    @Autowired
    private PointsRepository pointsRepository;

    public List<Points> findAll() {
        logger.debug("Получение списка всех точек");
        return pointsRepository.findAll();
    }

    public Optional<Points> findById(Long id) {
        logger.debug("Поиск точки по ID: {}", id);
        return pointsRepository.findById(id);
    }

    public List<Points> findByFunctionId(Long functionId) {
        logger.debug("Поиск точек по ID функции: {}", functionId);
        return pointsRepository.findByFunctionId(functionId);
    }

    public List<Points> findByFunctionIdIn(List<Long> functionIds) {
        logger.debug("Поиск точек по IDs функций: {}", functionIds);
        return pointsRepository.findByFunctionIdIn(functionIds);
    }

    public List<Points> findByFunctionIdOrdered(Long functionId, boolean ascending) {
        logger.debug("Поиск точек по ID функции {} с сортировкой по X: {}", functionId, ascending ? "возрастание" : "убывание");
        if (ascending) {
            return pointsRepository.findByFunctionIdOrderByX(functionId);
        } else {
            return pointsRepository.findByFunctionIdOrderByXDesc(functionId);
        }
    }

    public Optional<Points> findByFunctionIdAndX(Long functionId, Double x) {
        logger.debug("Поиск точки по ID функции {} и X: {}", functionId, x);
        return pointsRepository.findByFunctionIdAndX(functionId, x);
    }

    public List<Points> findByIds(List<Long> ids) {
        logger.debug("Поиск точек по IDs: {}", ids);
        return pointsRepository.findAllById(ids);
    }

    public Points save(Points point) {
        logger.debug("Сохранение точки для функции {}: ({}, {})",
                point.getFunctionId(), point.getX(), point.getY());
        return pointsRepository.save(point);
    }

    public List<Points> saveAll(List<Points> points) {
        logger.debug("Сохранение {} точек", points.size());
        return pointsRepository.saveAll(points);
    }

    public void deleteById(Long id) {
        logger.debug("Удаление точки по ID: {}", id);
        pointsRepository.deleteById(id);
    }

    public void deleteByFunctionId(Long functionId) {
        logger.debug("Удаление всех точек функции с ID: {}", functionId);
        pointsRepository.deleteByFunctionId(functionId);
    }

    public void deleteByFunctionIdAndX(Long functionId, Double x) {
        logger.debug("Удаление точки функции {} с X: {}", functionId, x);
        pointsRepository.deleteByFunctionIdAndX(functionId, x);
    }

    public boolean existsByFunctionIdAndX(Long functionId, Double x) {
        logger.debug("Проверка существования точки с X {} для функции {}", x, functionId);
        return pointsRepository.existsByFunctionIdAndX(functionId, x);
    }

    public long countByFunctionId(Long functionId) {
        logger.debug("Подсчет количества точек для функции с ID: {}", functionId);
        return pointsRepository.countByFunctionId(functionId);
    }

    public Double findMinXByFunctionId(Long functionId) {
        logger.debug("Поиск минимального X для функции с ID: {}", functionId);
        return pointsRepository.findMinXByFunctionId(functionId);
    }

    public Double findMaxXByFunctionId(Long functionId) {
        logger.debug("Поиск максимального X для функции с ID: {}", functionId);
        return pointsRepository.findMaxXByFunctionId(functionId);
    }

    public Double findMinYByFunctionId(Long functionId) {
        logger.debug("Поиск минимального Y для функции с ID: {}", functionId);
        return pointsRepository.findMinYByFunctionId(functionId);
    }

    public Double findMaxYByFunctionId(Long functionId) {
        logger.debug("Поиск максимального Y для функции с ID: {}", functionId);
        return pointsRepository.findMaxYByFunctionId(functionId);
    }

    public List<Points> createPointsBatch(Long functionId, List<PointCoordinate> pointCoordinates) {
        logger.debug("Создание {} точек для функции {}", pointCoordinates.size(), functionId);

        List<Points> points = pointCoordinates.stream()
                .map(coordinate -> new Points(functionId, coordinate.getXValue(), coordinate.getYValue()))
                .collect(Collectors.toList());

        return pointsRepository.saveAll(points);
    }

    public List<Points> findByFunctionIdAndXBetween(Long functionId, Double xStart, Double xEnd) {
        logger.debug("Поиск точек функции {} в диапазоне X: {} - {}", functionId, xStart, xEnd);
        return pointsRepository.findByFunctionIdAndXBetween(functionId, xStart, xEnd);
    }

    public List<Points> findByFunctionIdAndYBetween(Long functionId, Double yStart, Double yEnd) {
        logger.debug("Поиск точек функции {} в диапазоне Y: {} - {}", functionId, yStart, yEnd);
        return pointsRepository.findByFunctionIdAndYBetween(functionId, yStart, yEnd);
    }
}