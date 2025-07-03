package com.example.demo.user;

import java.time.LocalDateTime;

// DTO para el historial de juegos
public class GameSessionDTO {
    private String sessionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String opponent;
    private String result;
    private String status;

    // Constructores
    public GameSessionDTO() {}

    public GameSessionDTO(String sessionId, LocalDateTime startTime, LocalDateTime endTime,
                          String opponent, String result, String status) {
        this.sessionId = sessionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.opponent = opponent;
        this.result = result;
        this.status = status;
    }

    // Getters y Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getOpponent() { return opponent; }
    public void setOpponent(String opponent) { this.opponent = opponent; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
