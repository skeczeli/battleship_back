package com.example.demo;

public class PlayerRankingDTO {
    private String username;
    private int score;
    private int rank;

    public PlayerRankingDTO(String username, int score, int rank) {
        this.username = username;
        this.score = score;
        this.rank = rank;
    }

    // Getters
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public int getRank() { return rank; }

    // Setters
    public void setRank(int rank) { this.rank = rank; }
}
