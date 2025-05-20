package com.example.demo.game;

import com.example.demo.bot.GameServiceBot;
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
public class GameController {
    SimpMessagingTemplate simpMessagingTemplate;
    private final GameServiceBot gameServiceBot;
    
    @Autowired
    public GameController(GameServiceBot gameService, SimpMessagingTemplate simpMessagingTemplate) {
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
        GameState gameState = gameServiceBot.resumeGame(sessionId, playerId);

        Map<String, Object> response = new HashMap<>();
        response.put("playerBoard", gameState.getPlayerBoard());
        response.put("botBoard", gameState.getBotBoard());
        response.put("playerShots", gameState.getPlayerShots());
        response.put("botShots", gameState.getBotShots());

        return ResponseEntity.ok(response);
    }


}