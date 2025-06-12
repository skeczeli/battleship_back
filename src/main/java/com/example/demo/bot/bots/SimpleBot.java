package com.example.demo.bot.bots;


import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

/**
 * Servicio que maneja la lógica del bot para el juego de hundir la flota.
 */
public class SimpleBot extends AbstractBot {

    public SimpleBot(int BOARD_SIZE) {
        super(BOARD_SIZE);
    }

    @Override
    public ShotResultDTO processBotShot(GameState gameState) {
        int[] pos = getRandomUnshotCell(gameState);
        int row = pos[0], col = pos[1];

        // Marcar la celda como disparada
        gameState.getplayerTwoShots()[row][col] = true;

        // Determinar resultado del disparo
        String result = "miss";
        Integer shipId = null;
        boolean shipSunk = false;

        if (gameState.getPlayerBoard().get(row).get(col) != null) {
            shipId = gameState.getPlayerBoard().get(row).get(col);
            result = "hit";
            shipSunk = isShipSunk(gameState, shipId);
        }

        return new ShotResultDTO(row, col, result, shipSunk, shipId);
    }
}