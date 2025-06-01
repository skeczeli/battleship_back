package com.example.demo.bot.bots;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;
import java.util.List;


public interface BotStrategy {
    List<List<Integer>> generateRandomBoard();
    ShotResultDTO processBotShot(GameState gameState);
}
