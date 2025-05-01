package com.example.demo.user;

public class ScoreUpdateDTO {
    private int score;
    private boolean isWin;

    public ScoreUpdateDTO(int score, boolean isWin) {
        this.score = score;
        this.isWin = isWin;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }
}
