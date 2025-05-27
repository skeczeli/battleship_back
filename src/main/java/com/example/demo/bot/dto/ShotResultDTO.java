package com.example.demo.bot.dto;

/**
 * DTO para transferir información sobre un disparo.
 */
public class ShotResultDTO {
    private int row;
    private int col;
    private String result; // "hit", "miss"
    private boolean shipSunk;
    private Integer shipId;
    
    public ShotResultDTO(int row, int col, String result, boolean shipSunk, Integer shipId) {
        this.row = row;
        this.col = col;
        this.result = result;
        this.shipSunk = shipSunk;
        this.shipId = shipId;
    }
    
    public int getRow() {
        return row;
    }
    
    public int getCol() {
        return col;
    }
    
    public String getResult() {
        return result;
    }

    public boolean isShipSunk() {
        return shipSunk;
    }

    public Integer getShipId() { return shipId; }
}