package com.example.demo.multiplayer;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sessionId;
    private String senderId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    public ChatMessage(String sessionId, String senderId, String message, LocalDateTime timestamp) {
        this.sessionId = sessionId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ChatMessage() {}

    public Long getId() {
        return id;
    }

    public void setId() {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

