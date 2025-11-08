package ru.ssau.tk.cheefkeef.laba2.mapper;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.cheefkeef.laba2.dto.UserDTO;
import ru.ssau.tk.cheefkeef.laba2.models.User;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void toDTO_NullUser() {
        UserDTO result = UserMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    void toDTO_ValidUser() {
        User user = new User();
        user.setId(5);
        user.setLogin("test_user");
        user.setRole("USER");

        UserDTO result = UserMapper.toDTO(user);

        assertNotNull(result);
        assertEquals(5, result.getId());
        assertEquals("test_user", result.getLogin());
        assertEquals("USER", result.getRole());
        assertNull(result.getPassword()); // В конструкторе UserDTO не устанавливается пароль
    }

    @Test
    void toEntity_NullDTO() {
        User result = UserMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    void toEntity_ValidDTO() {
        UserDTO dto = new UserDTO();
        dto.setId(6);
        dto.setLogin("admin");
        dto.setRole("ADMIN");
        dto.setPassword("secure123");

        User result = UserMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(6, result.getId());
        assertEquals("admin", result.getLogin());
        assertEquals("ADMIN", result.getRole());
        assertEquals("secure123", result.getPassword());
    }
}