// GameWebSocketController.java
package com.example.demo.game;

import com.example.demo.bot.GameServiceBot;
import com.example.demo.bot.dto.ShotDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    private final GameServiceBot gameService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(GameServiceBot gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/{sessionId}/shot")
    public void playerShot(@DestinationVariable String sessionId, Map<String, Object> shotData) {
        try {
            Integer row = (Integer) shotData.get("row");
            Integer col = (Integer) shotData.get("col");
            String playerId = (String) shotData.get("playerId");
            
            if (row == null || col == null) {
                throw new IllegalArgumentException("Row and col are required");
            }
    
            ShotDTO shot = new ShotDTO(row, col);
            
            // Procesar el disparo del jugador
            Map<String, Object> result = gameService.processPlayerShot(sessionId, shot);
            
            // Añadir información adicional que necesita el frontend
            result.put("type", "SHOT_RESULT");
            result.put("playerId", playerId);
            result.put("row", row);
            result.put("col", col);
            
            // Enviar el resultado al cliente
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, result);
            
            // Si el jugador ganó, enviar mensaje adicional de GAME_OVER
            Boolean gameOver = (Boolean) result.get("gameOver");
            if (gameOver) {
                Map<String, Object> gameOverMessage = new HashMap<>();
                gameOverMessage.put("type", "GAME_OVER");
                gameOverMessage.put("winner", playerId);
                messagingTemplate.convertAndSend("/topic/game/" + sessionId, gameOverMessage);
            }
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

    @MessageMapping("/game/{sessionId}/join")
    public void joinGame(@DestinationVariable String sessionId, Map<String, Object> joinData) {
        try {
            String playerId = (String) joinData.get("playerId");
            
            // Crear mensaje de inicio de juego
            Map<String, Object> gameStartMessage = new HashMap<>();
            gameStartMessage.put("type", "GAME_START");
            gameStartMessage.put("turn", playerId); // Asumiendo que el jugador siempre empieza
            
            // Enviar mensaje de que el juego ha comenzado
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, gameStartMessage);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }
    @MessageMapping("/game/{sessionId}/abandon")
    public void abandonGame(@DestinationVariable String sessionId, Map<String, Object> abandonData) {
        try {
            String playerId = (String) abandonData.get("playerId");
            
            // Aquí podrías realizar alguna limpieza, como:
            // - Marcar el juego como abandonado
            // - Liberar recursos
            // - Registrar estadísticas, etc.
            
            // Crear mensaje de juego abandonado
            Map<String, Object> gameAbandonedMessage = new HashMap<>();
            gameAbandonedMessage.put("type", "GAME_ABANDONED");
            gameAbandonedMessage.put("playerId", playerId);
            gameAbandonedMessage.put("message", "El jugador ha abandonado la partida");
            
            // Enviar mensaje de que el juego ha sido abandonado
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, gameAbandonedMessage);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

}