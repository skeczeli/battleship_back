package com.example.demo.bot;

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
public class GameControllerBot {
    SimpMessagingTemplate simpMessagingTemplate;
    private final GameServiceBot gameServiceBot;
    
    @Autowired
    public GameControllerBot(GameServiceBot gameService, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameServiceBot = gameService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public GameServiceBot getGameServiceBot() {
        return gameServiceBot;
    }
    
    @PostMapping("/setup/bot")
    public Map<String, String> startGame(@RequestBody Map<String, Object> setupData) {
        // Extraer el tablero del jugador y el ID del jugador del cuerpo de la solicitud
        List<List<Integer>> playerBoard = (List<List<Integer>>) setupData.get("board");
        String playerId = (String) setupData.get("playerId");
        
        // Iniciar el juego y obtener el ID de sesión
        String sessionId = gameServiceBot.startGame(playerBoard, playerId);
        
        return Map.of("gameId",sessionId);
    }

    @GetMapping("/resume/{sessionId}/{playerId}")
    public ResponseEntity<Map<String, Object>> resumeGame(
            @PathVariable String sessionId,
            @PathVariable String playerId
    ) {
        GameViewDTO gameView = gameServiceBot.resumeGame(sessionId, playerId);

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