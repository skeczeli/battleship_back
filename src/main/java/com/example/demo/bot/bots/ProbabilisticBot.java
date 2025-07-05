package com.example.demo.bot.bots;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

import java.util.*;

public class ProbabilisticBot extends AbstractBot {

    private final List<int[]> hitStack = new ArrayList<>();
    private int[] currentDirection = null;
    private int[] origin = null;

    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    public ProbabilisticBot(int BOARD_SIZE) {
        super(BOARD_SIZE);
    }

    @Override
    public List<List<Integer>> generateRandomBoard() {
        // Igual que el base, pero podrías personalizar para evitar centros o usar simetría
        return super.generateRandomBoard();
    }

    @Override
    public ShotResultDTO processBotShot(GameState gameState) {
        int row, col;

        if (!hitStack.isEmpty()) {
            int[] target = getNextTarget(gameState);
            row = target[0];
            col = target[1];
        } else {
            int[] target = getMostProbableShot(gameState);
            row = target[0];
            col = target[1];
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
            if (shipSunk) resetHuntingState();
        } else {
            if (currentDirection != null && origin != null) {
                currentDirection = new int[]{-currentDirection[0], -currentDirection[1]};
                hitStack.clear();
                hitStack.add(origin.clone());
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

        resetHuntingState();
        return getMostProbableShot(gameState);
    }

    private int[] getMostProbableShot(GameState gameState) {
        int[][] heatmap = new int[BOARD_SIZE][BOARD_SIZE];

        for (int[] ship : SHIPS) {
            int shipSize = ship[1];

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col <= BOARD_SIZE - shipSize; col++) {
                    boolean fits = true;
                    for (int k = 0; k < shipSize; k++) {
                        if (gameState.getplayerTwoShots()[row][col + k]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        for (int k = 0; k < shipSize; k++) {
                            heatmap[row][col + k]++;
                        }
                    }
                }
            }

            for (int col = 0; col < BOARD_SIZE; col++) {
                for (int row = 0; row <= BOARD_SIZE - shipSize; row++) {
                    boolean fits = true;
                    for (int k = 0; k < shipSize; k++) {
                        if (gameState.getplayerTwoShots()[row + k][col]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        for (int k = 0; k < shipSize; k++) {
                            heatmap[row + k][col]++;
                        }
                    }
                }
            }
        }

        int bestRow = -1, bestCol = -1, max = -1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!gameState.getplayerTwoShots()[row][col] && heatmap[row][col] > max) {
                    max = heatmap[row][col];
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        return new int[]{bestRow, bestCol};
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
