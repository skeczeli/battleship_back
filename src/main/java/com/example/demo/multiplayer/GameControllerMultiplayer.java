package com.example.demo.multiplayer;

import com.example.demo.bot.dto.GameViewDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
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
    public GameControllerMultiplayer(GameServiceMultiplayer gameServiceMultiplayer, SimpMessagingTemplate simpMessagingTemplate, ChatRepository chatRepository) {
        this.gameServiceMultiplayer = gameServiceMultiplayer;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public GameServiceMultiplayer getGameServiceMultiplayer() {
        return gameServiceMultiplayer;
    }

    @Autowired
    ChatRepository chatRepository;

    @PostMapping("/setup/multiplayer")
    public Map<String, String> CreateGameRoom(@RequestBody Map<String, Object> setupData) throws JsonProcessingException {
        System.out.println("Payload recibido: " + setupData);
        System.out.println("matchByLevel recibido: " + setupData.get("matchByLevel"));
        List<List<Integer>> playerBoard = (List<List<Integer>>) setupData.get("board");
        String playerId = (String) setupData.get("playerId");
        boolean matchByLevel = ((String) setupData.get("matchByLevel")).equals("true");
        String sessionId;

        int boardSize = playerBoard.size();

        sessionId = gameServiceMultiplayer.createGameRoom(playerBoard, playerId, boardSize, matchByLevel);
        return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
    }

    @PostMapping("/setup/multiplayer/private")
    public Map<String, String> CreatePrivateGameRoom(@RequestBody Map<String, Object> setupData) throws JsonProcessingException {
        List<List<Integer>> playerBoard = (List<List<Integer>>) setupData.get("board");
        String playerId = (String) setupData.get("playerId");
        String passkey = ((String) setupData.get("passkey"));
        String sessionId;

        int boardSize = playerBoard.size();

        sessionId = gameServiceMultiplayer.createGameRoom(playerBoard, playerId, boardSize, passkey);
        return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
    }
        

    @GetMapping("/waiting")
    public Map<String, String> findWaitingGame(
            @RequestParam int boardSize, @RequestParam boolean matchByLevel, @RequestParam String playerId) {
        String sessionId = gameServiceMultiplayer.findWaitingGame(boardSize, matchByLevel, playerId);
        if (sessionId != null) {
            return Map.of("gameId", sessionId, "status", "WAITING_FOR_PLAYER");
        } else {
            return Map.of("gameId", "", "status", "NO_AVAILABLE_GAMES");
        }
    }

    @GetMapping("/waiting/private")
    public Map<String, String> findWaitingGame(
            @RequestParam int boardSize, @RequestParam String passkey) {
        String sessionId = gameServiceMultiplayer.findWaitingGame(boardSize, passkey);
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
        System.out.println("HOLA: " + sessionId + ":" + playerId);
        try {
            GameViewDTO gameView = gameServiceMultiplayer.resumeGame(sessionId, playerId);
            List<List<Integer>> playerBoard = gameView.playerBoard();
            int boardSize = playerBoard.size();
            List<ChatMessage> chatMessageList = chatRepository.findBySessionId(sessionId);

            List<Map<String, Object>> chatMessages = chatMessageList.stream().map(msg -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", msg.getId());
                m.put("text", msg.getMessage());
                m.put("sender", msg.getSenderId().equals(playerId) ? "me" : "opponent");
                m.put("senderName", msg.getSenderId().equals(playerId) ? "Tú" : "Oponente");
                m.put("timestamp", msg.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm")));
                return m;
            }).toList();

            Map<String, Object> gameConfig = new HashMap<>();
            gameConfig.put("boardSize", boardSize);
            gameConfig.put("totalShips", getTotalShips(boardSize));

            Map<String, Object> response = new HashMap<>();
            response.put("playerBoard", playerBoard);
            response.put("opponentBoard", gameView.opponentBoard());
            response.put("sunkShips", gameView.sunkShips());
            response.put("lastShot", gameView.lastShot());
            response.put("gameOver", gameView.gameOver());
            response.put("winner", gameView.winner());
            response.put("turn", gameView.turn());
            response.put("shotHistory", gameView.history());
            response.put("chatMessages", chatMessages);
            response.put("gameConfig", gameConfig);

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Jugador 2 aún no colocó su tablero
            Map<String, Object> waiting = new HashMap<>();
            waiting.put("status", "WAITING_FOR_OPPONENT");
            return ResponseEntity.ok(waiting);
        } catch (IllegalArgumentException e){
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "bad_request");
            return ResponseEntity.badRequest().body(error);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private int getTotalShips(int boardSize) {
        return switch(boardSize){
            case 6 -> 3;
            case 10 -> 5;
            case 14 -> 7;
            default -> 0;
        };
    }

}