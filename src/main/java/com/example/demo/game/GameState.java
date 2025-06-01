package com.example.demo.game;

import java.util.List;

public class GameState {
    private List<List<Integer>> playerBoard;
    private List<List<Integer>> EnemyBoard;
    private boolean[][] playerShots;
    private boolean[][] playerTwoShots;
    private String playerId;
    private String playerTwoId;
    private String botType;
    
    public  GameState(List<List<Integer>> playerBoard, List<List<Integer>> EnemyBoard, String playerId, String playertwoId, String botType) {
        playerShots = new boolean[10][10];
        playerTwoShots = new boolean[10][10];
        this.playerBoard = playerBoard;
        this.EnemyBoard = EnemyBoard;
        this.playerId = playerId;
        this.playerTwoId = playertwoId;
        this.botType = (botType == null) ? "simple" : botType;
    }
    
    public List<List<Integer>> getPlayerBoard() {
        return playerBoard;
    }
    
    public List<List<Integer>> getEnemyBoard() {
        return EnemyBoard;
    }
    
    public boolean[][] getPlayerShots() {
        return playerShots;
    }
    
    public boolean[][] getplayerTwoShots() {
        return playerTwoShots;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerTwoId() {
        return playerTwoId;
    }

    public void setPlayerTwoId(String playerTwoId) {
        this.playerTwoId = playerTwoId;
    }

    public String getBotType() {
        return botType;
    }

    public void setBotType(String botType) {
        this.botType = botType;
    }

}