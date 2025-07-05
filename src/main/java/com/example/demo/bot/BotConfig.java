package com.example.demo.bot;

import org.springframework.stereotype.Component;

import com.example.demo.bot.bots.BotStrategy;
import com.example.demo.bot.bots.IntelligentBot;
import com.example.demo.bot.bots.ProbabilisticBot;
import com.example.demo.bot.bots.SimpleBot;

/**
 * Configuración para los servicios relacionados con el bot.
 */
@Component
public class BotConfig {

    public BotStrategy createBot(String type, int boardSize) {
        return switch (type.toLowerCase()) {
            case "intelligent" -> new IntelligentBot(boardSize);
            case "simple" -> new SimpleBot(boardSize);
            case "probabilistic" -> new ProbabilisticBot(boardSize);
            default -> throw new IllegalArgumentException("Tipo de bot inválido: " + type);
        };
    }
}