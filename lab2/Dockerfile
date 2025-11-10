FROM tomcat:9.0.111-jre17

# Очищаем стандартные приложения Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*


COPY target/lab2-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/lab2-1.0-SNAPSHOT.war

# Порт 8080 открыт по умолчанию в образе Tomcat
EXPOSE 8080