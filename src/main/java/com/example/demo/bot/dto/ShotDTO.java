package com.example.demo.bot.dto;

/**
 * DTO para transferir información sobre un disparo.
 */
public class ShotDTO {
    private int row;
    private int col;
    
    public ShotDTO(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
}