// GameWebSocketController.java
package com.example.demo.bot;

import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.game.GameSession;
import com.example.demo.game.GameSessionRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebSocketController {

    private final GameServiceBot gameServiceBot;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    public WebSocketController(GameServiceBot gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameServiceBot = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/bot/{sessionId}/shot")
    public void playerShot(@DestinationVariable String sessionId, Map<String, Object> shotData) {
        try {
            Integer row = (Integer) shotData.get("row");
            Integer col = (Integer) shotData.get("col");
            String playerId = (String) shotData.get("playerId");
            
            if (row == null || col == null) {
                throw new IllegalArgumentException("Row and col are required"); //para mi innecesario
            }
    
            ShotDTO shot = new ShotDTO(row, col);
            Map<String, Object> result = new HashMap<>();

            // Procesar el disparo del jugador
            Map<String, Object> shotResult = gameServiceBot.processPlayerShot(sessionId, shot, playerId);
            
            // Añadir información que necesita el frontend
            result.put("playerId", playerId);
            result.put("row", row);
            result.put("col", col);
            result.put("hit", shotResult.get("playerShotResult"));
            result.put("rowBot", shotResult.get("botShotRow"));
            result.put("colBot", shotResult.get("botShotCol"));
            result.put("hitBot", shotResult.get("botShotResult"));
            result.put("shipSunk", shotResult.get("shipSunk"));
            result.put("shipSunkBot", shotResult.get("shipSunkBot"));
            
            
            // Si el jugador ganó, enviar mensaje adicional de GAME_OVER
            Boolean gameOver = (Boolean) shotResult.get("gameOver") || (Boolean) shotResult.get("gameOverBot");
            if (gameOver) {
                result.put("gameOver", true);
                if ((Boolean) shotResult.get("gameOver")) {
                    result.put("winner", playerId);
                } else if ((Boolean) shotResult.get("gameOverBot")) {
                    result.put("winner", "BOT");
                }
            } else {
                // Enviar el resultado al cliente
                result.put("gameOver", false);
                result.put("winner", null);
                
            }
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

    @MessageMapping("/game/bot/{sessionId}/abandon")
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

            updateUserStats(playerId);
            GameSession gs = gameSessionRepository.findBySessionId(sessionId);
            gs.setEndedAt(LocalDateTime.now());
            gs.setWinner("BOT");
            gameSessionRepository.saveAndFlush(gs);
            
            // Enviar mensaje de que el juego ha sido abandonado
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, gameAbandonedMessage);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

    private void updateUserStats(String playerId) {
        // Solo actualizar si no es un invitado
        if (playerId != null && !playerId.startsWith("guest")) {
            // Buscar el usuario por username (asumiendo que playerId es username para usuarios registrados)
            Optional<User> userOpt = userRepository.findByUsername(playerId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLosses(user.getLosses() + 1);
                userRepository.save(user);
                System.out.println("Estadísticas actualizadas para " + playerId +
                        " - Wins: " + user.getWins() + ", Losses: " + user.getLosses());
            }
        }
    }

}