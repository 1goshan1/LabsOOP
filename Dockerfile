# Используем официальный образ OpenJDK
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR файл в контейнер
COPY target/app.jar app.jar

# Открываем порт (обычно 8080 для Spring Boot)
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
