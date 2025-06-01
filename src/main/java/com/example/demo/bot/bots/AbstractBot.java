package com.example.demo.bot.bots;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;


public abstract class AbstractBot implements BotStrategy {

    protected static final int BOARD_SIZE = 10;
    protected static final Random random = new Random();

    //Barcos {ID, Tamaño}
    protected static final int[][] SHIPS = {
        {1, 5}, {2, 4}, {3, 3}, {4, 3}, {5, 2}
    };


    /**
     * Genera un tablero con barcos colocados aleatoriamente.
     * 
     * @return Matriz 10x10 con los IDs de los barcos o null en celdas vacías
     */
    @Override
    public List<List<Integer>> generateRandomBoard() {
        List<List<Integer>> board = new ArrayList<>();

        for (int i = 0; i < BOARD_SIZE; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < BOARD_SIZE; j++) {
                row.add(null);
            }
            board.add(row);
        }

        // Colocar cada barco
        for (int[] ship : SHIPS) {
            int shipId = ship[0];
            int shipSize = ship[1];
            boolean placed = false;

            while (!placed) {
                //orientacion aleatoria
                boolean isHorizontal = random.nextBoolean();

                // Calcular límites para coordenadas iniciales basado en tamaño y orientación
                int maxRow = isHorizontal ? BOARD_SIZE - 1 : BOARD_SIZE - shipSize;
                int maxCol = isHorizontal ? BOARD_SIZE - shipSize : BOARD_SIZE - 1;

                // Generar coordenadas aleatorias
                int row = random.nextInt(maxRow + 1);
                int col = random.nextInt(maxCol + 1);

                if (canPlaceShip(board, row, col, shipSize, isHorizontal)) {
                    placeShip(board, row, col, shipId, shipSize, isHorizontal);
                    placed = true;
                }
            }
        }

        return board;
    }

    protected boolean canPlaceShip(List<List<Integer>> board, int startRow, int startCol, int size, boolean isHorizontal) {
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            if (board.get(row).get(col) != null) return false;

            for (int r = Math.max(0, row - 1); r <= Math.min(BOARD_SIZE - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(BOARD_SIZE - 1, col + 1); c++) {
                    if ((r != row || c != col) && board.get(r).get(c) != null) return false;
                }
            }
        }
        return true;
    }

    protected void placeShip(List<List<Integer>> board, int startRow, int startCol, int shipId, int size, boolean isHorizontal) {
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            board.get(row).set(col, shipId);
        }
    }

    protected boolean isShipSunk(GameState gameState, int shipId) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (gameState.getPlayerBoard().get(i).get(j) != null &&
                    gameState.getPlayerBoard().get(i).get(j).equals(shipId) &&
                    !gameState.getplayerTwoShots()[i][j]) return false;
            }
        }
        return true;
    }

    protected int[] getRandomUnshotCell(GameState gameState) {
        int row, col;
        do {
            row = random.nextInt(BOARD_SIZE);
            col = random.nextInt(BOARD_SIZE);
        } while (gameState.getplayerTwoShots()[row][col]);
        return new int[]{row, col};
    }

    // Este método es abstracto, cada bot lo implementa distinto
    public abstract ShotResultDTO processBotShot(GameState gameState);
    
}
