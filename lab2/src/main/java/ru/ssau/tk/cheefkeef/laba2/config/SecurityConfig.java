// SecurityConfig.java
package ru.ssau.tk.cheefkeef.laba2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.ssau.tk.cheefkeef.laba2.services.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Используется NoOpPasswordEncoder (пароли не шифруются)");
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Настройка SecurityFilterChain");

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Публичные endpoints
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()

                        // Users endpoints
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "MANAGER")
                        .requestMatchers("/api/v1/users/search/**").hasAnyRole("ADMIN", "MANAGER", "USER")

                        // Functions endpoints
                        .requestMatchers("/api/v1/functions/**").authenticated()
                        .requestMatchers("/api/v1/functions/search/**").authenticated()

                        // Points endpoints
                        .requestMatchers("/api/v1/points/**").authenticated()

                        // Административные endpoints
                        .requestMatchers("/api/v1/auth/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {})
                .headers(headers -> headers.frameOptions().disable()) // Для H2 console
                .userDetailsService(userDetailsService);

        logger.info("SecurityFilterChain настроен успешно");
        return http.build();
    }
}