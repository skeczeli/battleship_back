package com.example.demo.multiplayer;

import com.example.demo.bot.dto.GameViewDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:3000")
public class GameControllerMultiplayer {
    
    SimpMessagingTemplate simpMessagingTemplate;
    private final GameServiceMultiplayer gameServiceMultiplayer;
    
    @Autowired
    public GameControllerMultiplayer(GameServiceMultiplayer gameServiceMultiplayer, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameServiceMultiplayer = gameServiceMultiplayer;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public GameServiceMultiplayer getGameServiceMultiplayer() {
        return gameServiceMultiplayer;
    }


    @PostMapping("/setup/multiplayer") // Cambiar ruta
    public Map<String, String> createOrJoinGame(@RequestBody Map<String, Object> setupData) {
        List<List<Integer>> playerBoard = (List<List<Integer>>) setupData.get("board");
        String playerId = (String) setupData.get("playerId");
        String sessionId = (String) setupData.get("sessionId");
        
        if (sessionId == null) {
            // Crear nueva sala
            sessionId = gameServiceMultiplayer.createGameRoom(playerBoard, playerId);
            return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
        } else {
            // Unirse a sala existente
            boolean joined = gameServiceMultiplayer.joinGameRoom(sessionId, playerBoard, playerId);
            if (joined) {
                // Notificar a ambos jugadores que el juego puede empezar
                notifyGameStart(sessionId);
                return Map.of("gameId", sessionId, "status", "GAME_STARTED");
            } else {
                throw new IllegalStateException("No se pudo unir a la sala");
            }
        }
    }


    private void notifyGameStart(String sessionId) { //mmmm detalle(?
        Map<String, Object> gameStartMessage = new HashMap<>();
        gameStartMessage.put("type", "GAME_START");
        gameStartMessage.put("message", "Ambos jugadores listos. ¡El juego comienza!");
        
        simpMessagingTemplate.convertAndSend("/topic/game/" + sessionId, gameStartMessage);
    }
        

    @GetMapping("/resume/multiplayer/{sessionId}/{playerId}")
    public ResponseEntity<Map<String, Object>> resumeGame(
            @PathVariable String sessionId,
            @PathVariable String playerId,
            @PathVariable String playerTwoId
    ) {
        GameViewDTO gameView = gameServiceMultiplayer.resumeGame(sessionId, playerId, playerTwoId);

        Map<String, Object> response = new HashMap<>();
        response.put("playerBoard", gameView.playerBoard());
        response.put("botBoard", gameView.opponentBoard());
        response.put("sunkShips", gameView.sunkShips());
        response.put("lastShot", gameView.lastShot());
        response.put("gameOver", gameView.gameOver());
        response.put("winner", gameView.winner());
        response.put("turn", gameView.turn());

        return ResponseEntity.ok(response);
    }


}