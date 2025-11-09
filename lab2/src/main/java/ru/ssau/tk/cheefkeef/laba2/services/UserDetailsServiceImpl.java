// UserDetailsServiceImpl.java
package ru.ssau.tk.cheefkeef.laba2.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ssau.tk.cheefkeef.laba2.entities.User;
import ru.ssau.tk.cheefkeef.laba2.repositories.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Загрузка пользователя по логину: {}", username);

        User user = userRepository.findByLogin(username);
        if (user == null) {
            logger.warn("Пользователь с логином {} не найден", username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }

        logger.debug("Пользователь {} найден, роль: {}", username, user.getRole());
        return user;
    }
}