package com.example.demo.bot.dto;

/**
 * DTO para transferir el resultado de un disparo.
 */
public class ShotResultDTO {
    private int row;
    private int col;
    private String result; // "hit", "miss", "sunk"
    
    public ShotResultDTO() {
        // Constructor vacío para serialización
    }
    
    public ShotResultDTO(int row, int col, String result) {
        this.row = row;
        this.col = col;
        this.result = result;
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
}