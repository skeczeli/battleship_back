package com.example.demo.bot;

import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    
    /**
     * Inicia un nuevo juego contra el bot.
     * 
     * @param playerBoard Tablero del jugador
     * @return Identificador de sesión y tablero del bot
     */
    public String startGame(Integer[][] playerBoard) {
        // Generar ID único para la sesión
        String sessionId = UUID.randomUUID().toString();
    
        // Generar tablero para el bot
        Integer[][] botBoard = bot.generateRandomBoard();
        
        // Crear nuevo estado de juego
        GameState gameState = new GameState();
        gameState.setPlayerBoard(playerBoard);
        gameState.setBotBoard(botBoard);
        
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
        if (gameState.getBotBoard()[row][col] != null) {
            Integer shipId = gameState.getBotBoard()[row][col];
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (gameState.getBotBoard()[i][j] != null && 
                        gameState.getBotBoard()[i][j].equals(shipId) && 
                        !gameState.getPlayerShots()[i][j]) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                result = "sunk";
            }
        }
        
        // Verificar si el juego ha terminado
        boolean gameOver = checkForVictory(gameState.getBotBoard(), gameState.getPlayerShots());
        
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerShotResult", result);
        response.put("gameOver", gameOver);
        
        // Si el juego no ha terminado y es turno del bot, procesar su disparo
        if (!gameOver) {
            ShotResultDTO botShot = bot.processBotShot(gameState);
            response.put("botShotResult", botShot.getResult());
            response.put("botShotRow", botShot.getRow());
            response.put("botShotCol", botShot.getCol());
            
            // Verificar si el bot ha ganado
            boolean botWon = checkForVictory(gameState.getPlayerBoard(), gameState.getBotShots());
            response.put("botWon", botWon);
        }
        
        return response;
    }
    
    /**
     * Verifica si hay victoria (todos los barcos hundidos).
     */
    private boolean checkForVictory(Integer[][] board, boolean[][] shots) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] != null && !shots[i][j]) {
                    // Hay al menos una celda de barco que no ha sido disparada
                    return false;
                }
            }
        }
        return true;
    }
    
}