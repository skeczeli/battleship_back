package com.example.demo.bot;

import java.util.List;
import java.util.Random;

/**
 * Servicio que maneja la lógica del bot para el juego de hundir la flota.
 */
public class BotService {
    
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
    public Integer[][] generateRandomBoard() {
        Integer[][] board = new Integer[BOARD_SIZE][BOARD_SIZE];
        
        // Inicializar tablero vacío
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = null;
            }
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
    private boolean canPlaceShip(Integer[][] board, int startRow, int startCol, int size, boolean isHorizontal) {
        // Verificar cada celda y su entorno
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            
            // Verificar que la celda esté vacía
            if (board[row][col] != null) {
                return false;
            }
            
            // Verificar celdas adyacentes (para evitar barcos pegados)
            for (int r = Math.max(0, row - 1); r <= Math.min(BOARD_SIZE - 1, row + 1); r++) {
                for (int c = Math.max(0, col - 1); c <= Math.min(BOARD_SIZE - 1, col + 1); c++) {
                    // Si hay un barco adyacente, no podemos colocar
                    if (r != row || c != col) { // No verificar la celda actual
                        if (board[r][c] != null) {
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
    private void placeShip(Integer[][] board, int startRow, int startCol, int shipId, int size, boolean isHorizontal) {
        for (int i = 0; i < size; i++) {
            int row = isHorizontal ? startRow : startRow + i;
            int col = isHorizontal ? startCol + i : startCol;
            board[row][col] = shipId;
        }
    }
    
    /**
     * Genera una jugada (disparo) aleatoria que no se haya realizado antes.
     * 
     * @param shotsMade Lista de disparos ya realizados
     * @return Coordenadas [fila, columna] del nuevo disparo
     */
    public int[] makeRandomShot(List<int[]> shotsMade) {
        // Generar disparos aleatorios hasta encontrar uno que no se haya realizado antes
        while (true) {
            int row = random.nextInt(BOARD_SIZE);
            int col = random.nextInt(BOARD_SIZE);
            
            boolean alreadyShot = false;
            for (int[] shot : shotsMade) {
                if (shot[0] == row && shot[1] == col) {
                    alreadyShot = true;
                    break;
                }
            }
            
            if (!alreadyShot) {
                return new int[]{row, col};
            }
        }
    }
    
    /**
     * Maneja un turno completo del bot, generando un disparo aleatorio.
     * 
     * @param playerBoard Tablero del jugador para determinar resultado
     * @param shotsMade Lista de disparos ya realizados
     * @return Mapa con la información del disparo (coordenadas y resultado)
     */
    public BotShot makeBotTurn(Integer[][] playerBoard, List<int[]> shotsMade) {
        // Generar disparo aleatorio
        int[] shotCoords = makeRandomShot(shotsMade);
        int row = shotCoords[0];
        int col = shotCoords[1];
        
        // Añadir a la lista de disparos realizados
        shotsMade.add(shotCoords);
        
        // Determinar resultado del disparo
        String result = "miss";
        if (playerBoard[row][col] != null) {
            result = "hit";
            
            // Comprobar si el barco ha sido hundido
            boolean shipSunk = isShipSunk(playerBoard, playerBoard[row][col], shotsMade);
            if (shipSunk) {
                result = "sunk";
            }
        }
        
        return new BotShot(row, col, result);
    }
    
    /**
     * Verifica si un barco ha sido completamente hundido.
     */
    private boolean isShipSunk(Integer[][] board, Integer shipId, List<int[]> shots) {
        // Contar celdas del barco
        int shipCells = 0;
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] != null && board[i][j].equals(shipId)) {
                    shipCells++;
                }
            }
        }
        
        // Contar disparos acertados en el barco
        int hitCells = 0;
        for (int[] shot : shots) {
            int r = shot[0];
            int c = shot[1];
            if (r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE &&
                    board[r][c] != null && board[r][c].equals(shipId)) {
                hitCells++;
            }
        }
        
        return hitCells >= shipCells;
    }
    
    /**
     * Clase interna para representar el resultado de un disparo.
     */
    public static class BotShot {
        private int row;
        private int col;
        private String result; // "hit", "miss", "sunk"
        
        public BotShot(int row, int col, String result) {
            this.row = row;
            this.col = col;
            this.result = result;
        }
        
        public int getRow() {
            return row;
        }
        
        public int getCol() {
            return col;
        }
        
        public String getResult() {
            return result;
        }
    }
}