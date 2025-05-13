package com.example.demo.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

/**
 * Servicio que maneja la lógica del bot para el juego de hundir la flota.
 */
public class Bot {
    
    private static final int BOARD_SIZE = 10;
    private static final Random random = new Random();
    
    // Definición de barcos (id, tamaño)
    private static final int[][] SHIPS = {
        {1, 5}, // Portaaviones
        {2, 4}, // Buque
        {3, 3}, // Submarino
        {4, 3}, // Crucero
        {5, 2}  // Lancha
    };
    
    /**
     * Genera un tablero con barcos colocados aleatoriamente.
     * 
     * @return Matriz 10x10 con los IDs de los barcos o null en celdas vacías
     */
    public List<List<Integer>> generateRandomBoard() {
        List<List<Integer>> board = new ArrayList<>();
        
        // Inicializar tablero vacío
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
                // Decidir orientación aleatoria
                boolean isHorizontal = random.nextBoolean();
                
                // Calcular límites para coordenadas iniciales basado en tamaño y orientación
                int maxRow = isHorizontal ? BOARD_SIZE - 1 : BOARD_SIZE - shipSize;
                int maxCol = isHorizontal ? BOARD_SIZE - shipSize : BOARD_SIZE - 1;
                
                // Generar coordenadas aleatorias
                int row = random.nextInt(maxRow + 1);
                int col = random.nextInt(maxCol + 1);
                
                // Verificar si se puede colocar
                if (canPlaceShip(board, row, col, shipSize, isHorizontal)) {
                    // Colocar barco
                    placeShip(board, row, col, shipId, shipSize, isHorizontal);
                    placed = true;
                }
            }
        }
        
        return board;
    }
    
    /**
     * Verifica si un barco puede ser colocado en la posición específica.
     */
    private boolean canPlaceShip(List<List<Integer>> board, int startRow, int startCol, int size, boolean isHorizontal) {
        // Verificar cada celda y su entorno
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            
            // Verificar que la celda esté vacía
            if (board.get(row).get(col) != null) {
                return false;
            }
            
            // Verificar celdas adyacentes (para evitar barcos pegados)
            for (int r = Math.max(0, row - 1); r <= Math.min(BOARD_SIZE - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(BOARD_SIZE - 1, col + 1); c++) {
                    // Si hay un barco adyacente, no podemos colocar
                    if (r != row || c != col) { // No verificar la celda actual
                        if (board.get(r).get(c) != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Coloca un barco en el tablero.
     */
    private void placeShip(List<List<Integer>> board, int startRow, int startCol, int shipId, int size, boolean isHorizontal) {
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            board.get(row).set(col, shipId);
        }
    }


    public ShotResultDTO processBotShot(GameState gameState) {
        // Encontrar una celda donde el bot no haya disparado
        int row, col;
        do {
            row = (int) (Math.random() * 10);
            col = (int) (Math.random() * 10);
        } while (gameState.getBotShots()[row][col]);
        
        // Marcar la celda como disparada
        gameState.getBotShots()[row][col] = true;
        
        // Determinar resultado
        String result = "miss";
        boolean shipSunk = false; 
        if (gameState.getPlayerBoard().get(row).get(col) != null) {
            Integer shipId = gameState.getPlayerBoard().get(row).get(col);
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (gameState.getPlayerBoard().get(i).get(j) != null && 
                        gameState.getPlayerBoard().get(i).get(j).equals(shipId) && 
                        !gameState.getBotShots()[i][j]) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                shipSunk = true;
            }
        }
        ShotResultDTO shotResult = new ShotResultDTO(row, col, result, shipSunk);
        return shotResult;
    }

}