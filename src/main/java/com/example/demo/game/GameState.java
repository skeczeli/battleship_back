package com.example.demo.game;

public class GameState {
    private Integer[][] playerBoard;
    private Integer[][] botBoard;
    private boolean[][] playerShots;
    private boolean[][] botShots;
    
    public  GameState() {
        playerShots = new boolean[10][10];
        botShots = new boolean[10][10];
    }
    
    public Integer[][] getPlayerBoard() {
        return playerBoard;
    }
    
    public void setPlayerBoard(Integer[][] playerBoard) {
        this.playerBoard = playerBoard;
    }
    
    public Integer[][] getBotBoard() {
        return botBoard;
    }
    
    public void setBotBoard(Integer[][] botBoard) {
        this.botBoard = botBoard;
    }
    
    public boolean[][] getPlayerShots() {
        return playerShots;
    }
    
    public boolean[][] getBotShots() {
        return botShots;
    }

}