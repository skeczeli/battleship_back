package com.example.demo;

import com.example.demo.bot.dto.ShotDTO;
//import com.example.demo.bot.dto.ShotResultDTO;
//import com.example.demo.bot.BotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para gestionar el estado del juego y coordinar la lógica.
 */
@Service
public class GameServiceMultiplayer {
    /*
    
    
    // Mapas para almacenar el estado del juego por sesión
    private final Map<String, GameState> gameStates = new HashMap<>();
    

    public String startGame(Integer[][] playerBoard) {
        // Generar ID único para la sesión
        String sessionId = UUID.randomUUID().toString();
    
        // Generar tablero para el bot
        Integer[][] botBoard = botService.generateRandomBoard();
        
        // Crear nuevo estado de juego
        GameState gameState = new GameState();
        gameState.setPlayerBoard(playerBoard);
        gameState.setBotBoard(botBoard);
        gameState.setCurrentTurn("player"); // El jugador empieza
        
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
     *
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
        
        // Cambiar turno si el juego no ha terminado
        if (!gameOver) {
            gameState.setCurrentTurn("bot"); //aca sofia en multiplayer retorna
        }
        
        // Preparar resultado
        ShotResultDTO playerShotResult = new ShotResultDTO(row, col, result);
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerShot", playerShotResult);
        response.put("gameOver", gameOver);
        
        // Si el juego no ha terminado y es turno del bot, procesar su disparo
        ShotResultDTO botShotResult = null;
        if (!gameOver && "bot".equals(gameState.getCurrentTurn())) {
            botShotResult = processBotShot(gameState);
            response.put("botShot", botShotResult);
            
            // Verificar si el bot ha ganado
            boolean botWon = checkForVictory(gameState.getPlayerBoard(), gameState.getBotShots());
            response.put("botWon", botWon);
            
            // Cambiar turno de vuelta al jugador si el juego continúa
            if (!botWon) {
                gameState.setCurrentTurn("player");
            }
        }
        
        return response;
    }
    

    private ShotResultDTO processBotShot(GameState gameState) {
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
        if (gameState.getPlayerBoard()[row][col] != null) {
            Integer shipId = gameState.getPlayerBoard()[row][col];
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (gameState.getPlayerBoard()[i][j] != null && 
                        gameState.getPlayerBoard()[i][j].equals(shipId) && 
                        !gameState.getBotShots()[i][j]) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                result = "sunk";
            }
        }
        
        return new ShotResultDTO(row, col, result);
    }
    

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
    */
}