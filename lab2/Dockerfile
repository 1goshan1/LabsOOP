# Используем официальный образ OpenJDK
FROM eclipse-temurin:17-jdk-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл в контейнер
COPY target/laba2.jar app.jar

# Открываем порт (обычно 8080 для Spring Boot)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
