package com.example.demo.bot;

import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * Servicio para gestionar el estado del juego y coordinar la lógica.
 */
@Service
public class GameServiceBot {
    
    private final Bot bot;
    
    // Mapas para almacenar el estado del juego por sesión
    private final Map<String, GameState> gameStates = new HashMap<>();
    
    @Autowired
    public GameServiceBot(Bot bot) {
        this.bot = bot;
    }

    public GameState getGameState(String sessionId) {
        return gameStates.get(sessionId);
    }
    
    /**
     * Inicia un nuevo juego contra el bot.
     * 
     * @param playerBoard Tablero del jugador
     * @return Identificador de sesión y tablero del bot
     */
    public String startGame(List<List<Integer>> playerBoard, String playerId) {
        // Generar ID único para la sesión
        String sessionId = UUID.randomUUID().toString();
    
        // Generar tablero para el bot
        List<List<Integer>> botBoard = bot.generateRandomBoard();
        
        // Crear nuevo estado de juego
        GameState gameState = new GameState(playerBoard, botBoard, playerId);
        
        // Guardar estado
        gameStates.put(sessionId, gameState);
        
        // Devolver directamente el sessionId
        return sessionId;
    }
    
    /**
     * Procesa un disparo del jugador.
     * 
     * @param sessionId ID de la sesión
     * @param shotDTO Información del disparo
     * @return Resultado del disparo y posible disparo del bot
     */
    public Map<String, Object> processPlayerShot(String sessionId, ShotDTO shotDTO) {
        GameState gameState = gameStates.get(sessionId);
        if (gameState == null) {
            throw new IllegalStateException("La sesión no existe");
        }
        
        // Procesar disparo en el tablero del bot
        int row = shotDTO.getRow();
        int col = shotDTO.getCol();
        
        // Validar que la celda no haya sido disparada antes
        if (gameState.getPlayerShots()[row][col]) {
            throw new IllegalStateException("Ya has disparado en esta posición");
        }
        
        // Marcar la celda como disparada
        gameState.getPlayerShots()[row][col] = true;
        
        // Determinar resultado
        String result = "miss";
        boolean shipSunk = false;
        if (gameState.getBotBoard().get(row).get(col) != null) {
            Integer shipId = gameState.getBotBoard().get(row).get(col);
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (gameState.getBotBoard().get(i).get(j) != null && 
                        gameState.getBotBoard().get(i).get(j).equals(shipId) && 
                        !gameState.getPlayerShots()[i][j]) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                shipSunk = true;;
            }
        }
        
        // Verificar si el juego ha terminado
        boolean gameOver = checkForVictory(gameState.getBotBoard(), gameState.getPlayerShots());
        
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerShotResult", result);
        response.put("gameOver", gameOver);
        response.put("shipSunk", shipSunk);
        
        //Turno del bot, procesar su disparo
        ShotResultDTO botShot = bot.processBotShot(gameState);
        response.put("botShotResult", botShot.getResult());
        response.put("botShotRow", botShot.getRow());
        response.put("botShotCol", botShot.getCol());
        response.put("shipSunkBot", botShot.isShipSunk());
        
        // Verificar si el bot ha ganado
        boolean gameOverBot = checkForVictory(gameState.getPlayerBoard(), gameState.getBotShots());
        response.put("gameOverBot", gameOverBot);
        
        return response;
    }
    
    /**
     * Verifica si hay victoria (todos los barcos hundidos).
     */
    private boolean checkForVictory(List<List<Integer>> board, boolean[][] shots) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.get(i).get(j) != null && !shots[i][j]) {
                    // Hay al menos una celda de barco que no ha sido disparada
                    return false;
                }
            }
        }
        return true;
    }
    
}