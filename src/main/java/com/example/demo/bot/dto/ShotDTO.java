package com.example.demo.bot.dto;

/**
 * DTO para transferir información sobre un disparo.
 */
public class ShotDTO {
    private int row;
    private int col;
    
    public ShotDTO() {
        // Constructor vacío para serialización
    }
    
    public ShotDTO(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    public int getRow() {
        return row;
    }
    
    public void setRow(int row) {
        this.row = row;
    }
    
    public int getCol() {
        return col;
    }
    
    public void setCol(int col) {
        this.col = col;
    }
}