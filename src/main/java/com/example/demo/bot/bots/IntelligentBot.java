package com.example.demo.bot.bots;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

import java.util.ArrayList;
import java.util.List;


public class IntelligentBot extends AbstractBot {

    private final List<int[]> hitStack = new ArrayList<>(); // celdas que dieron hit
    private int[] currentDirection = null; // dirección actual (ej: [0, 1])
    private int[] origin = null; // celda inicial de hit

    private static final int[][] DIRECTIONS = {
            {-1, 0}, // arriba
            {1, 0},  // abajo
            {0, -1}, // izquierda
            {0, 1}   // derecha
    };

    @Override
    public ShotResultDTO processBotShot(GameState gameState) {
        int row, col;

        if (!hitStack.isEmpty()) {
            int[] target = getNextTarget(gameState);
            row = target[0];
            col = target[1];
        } else {
            int[] pos = getRandomUnshotCell(gameState);
            row = pos[0];
            col = pos[1];
        }

        gameState.getplayerTwoShots()[row][col] = true;

        String result = "miss";
        Integer shipId = null;
        boolean shipSunk = false;

        if (gameState.getPlayerBoard().get(row).get(col) != null) {
            shipId = gameState.getPlayerBoard().get(row).get(col);
            result = "hit";

            if (origin == null) origin = new int[]{row, col};
            hitStack.add(new int[]{row, col});

            shipSunk = isShipSunk(gameState, shipId);
            if (shipSunk) {
                resetHuntingState(); // Volver al modo búsqueda
            }
        } else {
            if (currentDirection != null && origin != null) {
                // Fallamos en la dirección actual → invertir
                currentDirection = new int[]{-currentDirection[0], -currentDirection[1]};
                hitStack.clear();
                hitStack.add(origin.clone()); // volver al punto de inicio
            }
        }

        return new ShotResultDTO(row, col, result, shipSunk, shipId);
    }

    private int[] getNextTarget(GameState gameState) {
        if (currentDirection != null && origin != null) {
            int[] lastHit = hitStack.get(hitStack.size() - 1);
            int nextRow = lastHit[0] + currentDirection[0];
            int nextCol = lastHit[1] + currentDirection[1];

            if (isValid(nextRow, nextCol) && !gameState.getplayerTwoShots()[nextRow][nextCol]) {
                return new int[]{nextRow, nextCol};
            } else {
                // dirección inválida → invertir
                currentDirection = new int[]{-currentDirection[0], -currentDirection[1]};
                hitStack.clear();
                hitStack.add(origin.clone());
            }
        }

        if (origin != null) {
            for (int[] dir : DIRECTIONS) {
                int nextRow = origin[0] + dir[0];
                int nextCol = origin[1] + dir[1];

                if (isValid(nextRow, nextCol) && !gameState.getplayerTwoShots()[nextRow][nextCol]) {
                    currentDirection = dir;
                    return new int[]{nextRow, nextCol};
                }
            }
        }

        // Nada útil → modo búsqueda
        resetHuntingState();
        return getRandomUnshotCell(gameState);
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void resetHuntingState() {
        hitStack.clear();
        currentDirection = null;
        origin = null;
    }
}