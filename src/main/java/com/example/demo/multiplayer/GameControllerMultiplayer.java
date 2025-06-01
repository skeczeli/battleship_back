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


    @PostMapping("/setup/multiplayer")
    public Map<String, String> CreateGameRoom(@RequestBody Map<String, Object> setupData) {
        List<List<Integer>> playerBoard = (List<List<Integer>>) setupData.get("board");
        String playerId = (String) setupData.get("playerId");
        String sessionId;

        sessionId = gameServiceMultiplayer.createGameRoom(playerBoard, playerId);
        return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
    }
        

    @GetMapping("/waiting")
    public Map<String, String> findWaitingGame() {
        String sessionId = gameServiceMultiplayer.findWaitingGame();
        if (sessionId != null) {
            return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
        } else {
            return Map.of("gameId", "", "status", "NO_AVAILABLE_GAMES");
        }
    }


    @GetMapping("/resume/multiplayer/{sessionId}/{playerId}")
    public ResponseEntity<?> resumeGame(
            @PathVariable String sessionId,
            @PathVariable String playerId
    ) {
        try {
            GameViewDTO gameView = gameServiceMultiplayer.resumeGame(sessionId, playerId);

            Map<String, Object> response = new HashMap<>();
            response.put("playerBoard", gameView.playerBoard());
            response.put("opponentBoard", gameView.opponentBoard());
            response.put("sunkShips", gameView.sunkShips());
            response.put("lastShot", gameView.lastShot());
            response.put("gameOver", gameView.gameOver());
            response.put("winner", gameView.winner());
            response.put("turn", gameView.turn());

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Jugador 2 aún no colocó su tablero
            Map<String, Object> waiting = new HashMap<>();
            waiting.put("status", "WAITING_FOR_OPPONENT");
            return ResponseEntity.ok(waiting);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }



}