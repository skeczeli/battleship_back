package com.example.demo.game;

import java.util.List;

public class GameState {
    private List<List<Integer>> playerBoard;
    private List<List<Integer>> botBoard;
    private boolean[][] playerShots;
    private boolean[][] botShots;
    private String playerId;
    
    public  GameState(List<List<Integer>> playerBoard, List<List<Integer>> botBoard, String playerId) {
        playerShots = new boolean[10][10];
        botShots = new boolean[10][10];
        this.playerBoard = playerBoard;
        this.botBoard = botBoard;
        this.playerId = playerId;
    }
    
    public List<List<Integer>> getPlayerBoard() {
        return playerBoard;
    }
    
    public List<List<Integer>> getBotBoard() {
        return botBoard;
    }
    
    public boolean[][] getPlayerShots() {
        return playerShots;
    }
    
    public boolean[][] getBotShots() {
        return botShots;
    }

    public String getPlayerId() {
        return playerId;
    }

}