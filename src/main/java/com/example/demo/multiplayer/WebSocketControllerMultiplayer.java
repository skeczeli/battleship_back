// GameWebSocketController.java
package com.example.demo.multiplayer;

import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.game.GameSession;
import com.example.demo.game.GameSessionRepository;
import com.example.demo.game.GameState;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebSocketControllerMultiplayer {

    private final GameServiceMultiplayer gameServiceMultiplayer;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GameSessionRepository gameSessionRepository;

    @Autowired
    public WebSocketControllerMultiplayer(SimpMessagingTemplate messagingTemplate, GameServiceMultiplayer gameServiceMultiplayer) {
        this.gameServiceMultiplayer = gameServiceMultiplayer;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/multiplayer/{sessionId}/join")
    public void joinGame(@DestinationVariable String sessionId, Map<String, Object> joinData) {
        String playerId = (String) joinData.get("playerId");
        Object rawBoard = joinData.get("board");
        List<List<Integer>> playerBoard = (List<List<Integer>>) (List<?>) rawBoard;

        System.out.println("playerId: " + playerId + ", sessionID: " + sessionId);


        boolean joined = gameServiceMultiplayer.joinGameRoom(sessionId, playerBoard, playerId);

        if (joined) {
            GameState gameState = gameServiceMultiplayer.getGameState(sessionId);

            Map<String, Object> startMessage = new HashMap<>();
            startMessage.put("type", "GAME_START");
            startMessage.put("turn", gameState.getCurrentTurn());

            messagingTemplate.convertAndSend("/topic/game/" + sessionId, startMessage);
        } else {
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("type", "ERROR");
            errorMessage.put("message", "No se pudo unir a la sala.");
            
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorMessage);
        }
    }

    @MessageMapping("/game/multiplayer/{sessionId}/shot")
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

            // Obtener el otro jugador para determinar el próximo turno
            GameState gameState = gameServiceMultiplayer.getGameState(sessionId);
            String otherPlayerId = gameState.getPlayerId().equals(playerId) ? 
                                gameState.getPlayerTwoId() : gameState.getPlayerId();

                                
            // Procesar el disparo del jugador
            Map<String, Object> shotResult = gameServiceMultiplayer.processPlayerShot(sessionId, shot, playerId);

            // Añadir información que necesita el frontend
            result.put("playerId", playerId);
            result.put("row", row);
            result.put("col", col);
            result.put("hit", shotResult.get("playerShotResult"));
            result.put("shipSunk", shotResult.get("shipSunk"));
            result.put("type", "SHOT_RESULT");
            
            
            // Si el jugador ganó, enviar mensaje adicional de GAME_OVER
            Boolean gameOver = (Boolean) shotResult.get("gameOver");
            if (gameOver) {
                result.put("gameOver", true);
                result.put("winner", playerId);
                saveEndedGame(sessionId, playerId, false);
            } else {
                // Enviar el resultado al cliente
                result.put("gameOver", false);
                result.put("winner", null);
                result.put("nextTurn", otherPlayerId);
                
            }
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

    @MessageMapping("/game/multiplayer/{sessionId}/abandon")
    public void abandonGame(@DestinationVariable String sessionId, Map<String, Object> abandonData) {
        try {
            String playerId = (String) abandonData.get("playerId");

            Map<String, Object> gameAbandonedMessage = new HashMap<>();
            gameAbandonedMessage.put("type", "GAME_ABANDONED");
            gameAbandonedMessage.put("playerId", playerId);
            gameAbandonedMessage.put("message", "El jugador ha abandonado la partida");

            saveEndedGame(sessionId, playerId, true);

            // Enviar mensaje de que el juego ha sido abandonado
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, gameAbandonedMessage);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("error", e.getMessage());
            messagingTemplate.convertAndSend("/topic/game/" + sessionId, errorResponse);
        }
    }

    private void saveEndedGame(String sessionId, String playerId, boolean isLoser) {
        GameSession gs = gameSessionRepository.findBySessionId(sessionId);

        String playerOneId = gs.getPlayerOneId();
        String playerTwoId = gs.getPlayerTwoId();

        String loserId = isLoser ? playerId :
                (playerId.equals(playerOneId) ? playerTwoId : playerOneId);

        String winnerId = loserId.equals(playerOneId) ? playerTwoId : playerOneId;

        updateUserStats(loserId, playerOneId, playerTwoId);
        gs.setEndedAt(LocalDateTime.now());
        gs.setWinner(winnerId);
        gameSessionRepository.saveAndFlush(gs);
    }


    private void updateUserStats(String loserId, String playerOneId, String playerTwoId) {
        boolean playerOneIsBailer = loserId.equals(playerOneId);
        updatePlayerStats(playerOneId, playerOneIsBailer);
        updatePlayerStats(playerTwoId, !playerOneIsBailer);
    }

    private void updatePlayerStats(String playerId, boolean playerIsBailer) {
        if (isNotGuest(playerId)) {
            Optional<User> userOpt = userRepository.findByUsername(playerId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (playerIsBailer) {
                    updateLoss(user);
                } else {
                    updateWin(user);
                }
                userRepository.save(user);
                System.out.println("Estadísticas actualizadas para " + playerId +
                        " - Wins: " + user.getWins() + ", Losses: " + user.getLosses());
            }
        }
    }

    private static void updateLoss(User user) {
        user.setLosses(user.getLosses() + 1);
    }

    private static void updateWin(User user) {
        user.setWins(user.getWins() + 1);
    }

    private static boolean isNotGuest(String playerId) {
        return playerId != null && !playerId.startsWith("guest");
    }

}