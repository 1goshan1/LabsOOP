package ru.ssau.tk.cheefkeef.laba2.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.FunctionDTO;
import ru.ssau.tk.cheefkeef.laba2.dto.PointDTO;

import java.util.Comparator;
import java.util.List;

public class SortingUtils {
    private static final Logger logger = LoggerFactory.getLogger(SortingUtils.class);

    public static void sortUsers(List<UserDTO> users, String sortField, boolean ascending) {
        logger.debug("Сортировка пользователей по полю: {}, порядок: {}", sortField, ascending ? "возрастание" : "убывание");

        if (sortField == null || sortField.isEmpty()) {
            sortField = "id";
        }

        Comparator<UserDTO> comparator = switch (sortField.toLowerCase()) {
            case "id" -> Comparator.comparing(UserDTO::getId);
            case "login" -> Comparator.comparing(UserDTO::getLogin);
            case "role" -> Comparator.comparing(UserDTO::getRole);
            case "password" -> Comparator.comparing(u -> u.getPassword() != null ? u.getPassword() : "");
            default -> {
                logger.warn("Неверное поле сортировки для пользователей: {}", sortField);
                yield Comparator.comparing(UserDTO::getId);
            }
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        users.sort(comparator);
        logger.debug("Пользователи отсортированы. Количество: {}", users.size());
    }

    public static void sortFunctions(List<FunctionDTO> functions, String sortField, boolean ascending) {
        logger.debug("Сортировка функций по полю: {}, порядок: {}", sortField, ascending ? "возрастание" : "убывание");

        if (sortField == null || sortField.isEmpty()) {
            sortField = "id";
        }

        Comparator<FunctionDTO> comparator = switch (sortField.toLowerCase()) {
            case "id" -> Comparator.comparing(FunctionDTO::getId);
            case "u_id", "userid" -> Comparator.comparing(FunctionDTO::getUserId);
            case "name" -> Comparator.comparing(FunctionDTO::getName);
            case "signature" -> Comparator.comparing(FunctionDTO::getSignature);
            default -> {
                logger.warn("Неверное поле сортировки для функций: {}", sortField);
                yield Comparator.comparing(FunctionDTO::getId);
            }
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        functions.sort(comparator);
        logger.debug("Функции отсортированы. Количество: {}", functions.size());
    }

    public static void sortPoints(List<PointDTO> points, String sortField, boolean ascending) {
        logger.debug("Сортировка точек по полю: {}, порядок: {}", sortField, ascending ? "возрастание" : "убывание");

        if (sortField == null || sortField.isEmpty()) {
            sortField = "id";
        }

        Comparator<PointDTO> comparator = switch (sortField.toLowerCase()) {
            case "id" -> Comparator.comparing(PointDTO::getId);
            case "functionid" -> Comparator.comparing(PointDTO::getFunctionId);
            case "xvalue" -> Comparator.comparing(PointDTO::getXValue);
            case "yvalue" -> Comparator.comparing(PointDTO::getYValue);
            default -> {
                logger.warn("Неверное поле сортировки для точек: {}", sortField);
                yield Comparator.comparing(PointDTO::getId);
            }
        };

        if (!ascending) {
            comparator = comparator.reversed();
        }

        points.sort(comparator);
        logger.debug("Точки отсортированы. Количество: {}", points.size());
    }
}