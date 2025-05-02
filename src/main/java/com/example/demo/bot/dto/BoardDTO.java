package com.example.demo.bot.dto;

/**
 * DTO para transferir el estado del tablero entre el frontend y el backend.
 */
public class BoardDTO {
    private Integer[][] board;
    
    public BoardDTO() {
        // Constructor vacío para serialización
    }
    
    public BoardDTO(Integer[][] board) {
        this.board = board;
    }
    
    public Integer[][] getBoard() {
        return board;
    }
    
    public void setBoard(Integer[][] board) {
        this.board = board;
    }
}