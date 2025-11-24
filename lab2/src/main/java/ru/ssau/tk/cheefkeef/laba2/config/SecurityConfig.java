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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.ssau.tk.cheefkeef.laba2.services.UserDetailsServiceImpl;

import java.util.Arrays;
import java.util.List;

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
    public CorsConfigurationSource corsConfigurationSource() {
        logger.info("Настройка CORS конфигурации");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Разрешить все origins
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Разрешить все headers
        configuration.setAllowCredentials(false); // Должно быть false при allowedOrigins("*")
        configuration.setMaxAge(3600L); // Кэшировать preflight запросы на 1 час

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Применить ко всем endpoints

        logger.info("CORS настроен: origins=*, methods=GET,POST,PUT,DELETE,OPTIONS,PATCH, headers=*");
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Настройка SecurityFilterChain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Добавляем CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Публичные endpoints
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll() // Добавьте если есть login endpoint

                        // Users endpoints
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/users/search/**").authenticated()

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

        logger.info("SecurityFilterChain настроен успешно с CORS поддержкой");
        return http.build();
    }
}