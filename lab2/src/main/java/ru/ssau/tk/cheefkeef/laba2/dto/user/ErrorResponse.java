// ErrorResponse.java
package ru.ssau.tk.cheefkeef.laba2.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class ErrorResponse {
    private String error;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String path;

    public ErrorResponse(String error, String path) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.path = path;
    }

    // Геттеры и сеттеры
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}