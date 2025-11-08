package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;
import ru.ssau.tk.cheefkeef.laba2.models.User;

public class UserMapper {
    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    public static UserDTO toDTO(User user) {
        if (user == null) {
            logger.warn("Попытка преобразования null-объекта User в DTO");
            return null;
        }

        logger.debug("Преобразование User(id={}) в UserDTO", user.getId());
        return new UserDTO(
                user.getId(),
                user.getLogin(),
                user.getRole()
        );
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            logger.warn("Попытка преобразования null-объекта UserDTO в сущность");
            return null;
        }

        logger.debug("Преобразование UserDTO в User");
        User user = new User();
        user.setId(dto.getId());
        user.setLogin(dto.getLogin());
        user.setRole(dto.getRole());
        user.setPassword(dto.getPassword());
        return user;
    }
}