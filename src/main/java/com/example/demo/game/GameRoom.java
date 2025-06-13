package com.example.demo.game;

import java.util.List;

public class GameRoom {
    private String sessionId;
    private String player1Id;
    private String player2Id;
    private List<List<Integer>> player1Board;
    private List<List<Integer>> player2Board;
    private String status; // "WAITING_FOR_PLAYER", "IN_PROGRESS"
    private int boardSize;
    private boolean matchByLevel;
    private int level;

    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    public String getPlayer1Id() {
        return player1Id;
    }
    public void setPlayer1Id(String player1Id) {
        this.player1Id = player1Id;
    }
    public String getPlayer2Id() {
        return player2Id;
    }
    public void setPlayer2Id(String player2Id) {
        this.player2Id = player2Id;
    }
    public List<List<Integer>> getPlayer1Board() {
        return player1Board;
    }
    public void setPlayer1Board(List<List<Integer>> player1Board) {
        this.player1Board = player1Board;
    }
    public List<List<Integer>> getPlayer2Board() {
        return player2Board;
    }
    public void setPlayer2Board(List<List<Integer>> player2Board) {
        this.player2Board = player2Board;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public boolean getMatchByLevel() {
        return matchByLevel;
    }
    public void setMatchByLevel(boolean match) {
        this.matchByLevel = match;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return sessionId;
    }
    
    public int getBoardSize() {
        return boardSize;
    }

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    }
}
