package com.example.demo.game;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String sessionId; // UUID que ya genero en memoria

    private String playerOneId;
    private String playerTwoId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String winner; // playerOneId, playerTwoId, "bot", null

    @Column(columnDefinition = "TEXT")
    private String playerBoardJson;


    @Column(columnDefinition = "TEXT")
    private String playerTwoBoardJson;

    public GameSession() {}

    public GameSession(String sessionId, String playerOneId, String playerTwoId, String playerBoardJson, String playerTwoBoardJson) {
        this.sessionId = sessionId;
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
        this.playerBoardJson = playerBoardJson;
        this.playerTwoBoardJson = playerTwoBoardJson;
        this.startedAt = LocalDateTime.now();
        this.winner = null;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPlayerOneId() {
        return playerOneId;
    }

    public void setPlayerOneId(String playerOneId) {
        this.playerOneId = playerOneId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(String playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getPlayerBoardJson() {
        return playerBoardJson;
    }

    public void setPlayerBoardJson(String playerBoardJson) {
        this.playerBoardJson = playerBoardJson;
    }

    public String getPlayerTwoBoardJson() {
        return playerTwoBoardJson;
    }

    public void setPlayerTwoBoardJson(String playerTwoBoardJson) {
        this.playerTwoBoardJson = playerTwoBoardJson;
    }

    @Override
    public String toString() {
        return "gs: id: " + id + ", winner: " + winner + ", ended at: " + endedAt + ", player1id: " + playerOneId + ", player2id: " + playerTwoId;
    }


}

