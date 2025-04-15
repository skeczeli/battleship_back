package com.example.demo;

public class GameState {

    private Bot bot;

    public GameState() {
        this.bot = new Bot();  // Al crear el GameState, ya crea el bot y coloca sus barcos.
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
