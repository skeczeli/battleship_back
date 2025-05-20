package com.example.demo.shot;

import jakarta.persistence.*;

@Entity
public class Shot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String sessionId;
    private String playerId;
    private int row;
    private int col;
    private boolean hit;
    private boolean shipSunk;
    private boolean isBot;

    public Shot() {
    }

    public Shot(String sessionId, String playerId, int row, int col, boolean hit, boolean shipSunk, boolean isBot) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        this.hit = hit;
        this.shipSunk = shipSunk;
        this.isBot = isBot;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isHit() {
        return hit;
    }

    public boolean isShipSunk() {
        return shipSunk;
    }

    public boolean isBot() {
        return isBot;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public void setShipSunk(boolean shipSunk) {
        this.shipSunk = shipSunk;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }
}