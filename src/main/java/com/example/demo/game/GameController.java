package com.example.demo.game;

import com.example.demo.bot.GameServiceBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:3000")
public class GameController {
    
    private final GameServiceBot gameService;
    
    @Autowired
    public GameController(GameServiceBot gameService) {
        this.gameService = gameService;
    }
    
    @PostMapping("/setup")
    public ResponseEntity<Map<String, String>> startGame(@RequestBody Map<String, Object> setupData) {
        // Extraer el tablero del jugador y el ID del jugador del cuerpo de la solicitud
        Object boardObj = setupData.get("board");
        Integer[][] playerBoard = convertToBoard(boardObj);
        String playerId = (String) setupData.get("playerId");
        
        // Iniciar el juego y obtener el ID de sesión
        String sessionId = gameService.startGame(playerBoard);
        
        // Preparar la respuesta
        Map<String, String> response = new HashMap<>();
        response.put("gameId", sessionId);
        
        return ResponseEntity.ok(response);
    }
    
    // Método auxiliar para convertir el objeto JSON a una matriz Integer[][]
    private Integer[][] convertToBoard(Object boardObj) {
        if (boardObj instanceof java.util.List<?>) {
            java.util.List<?> rows = (java.util.List<?>) boardObj;
            Integer[][] board = new Integer[rows.size()][];
            for (int i = 0; i < rows.size(); i++) {
                if (rows.get(i) instanceof java.util.List<?>) {
                    java.util.List<?> cols = (java.util.List<?>) rows.get(i);
                    board[i] = new Integer[cols.size()];
                    for (int j = 0; j < cols.size(); j++) {
                        // Intentar convertir el valor a Integer
                        Object val = cols.get(j);
                        if (val == null) {
                            board[i][j] = null;
                        } else if (val instanceof Integer) {
                            board[i][j] = (Integer) val;
                        } else if (val instanceof Number) {
                            board[i][j] = ((Number) val).intValue();
                        } else if (val instanceof String) {
                            try {
                                board[i][j] = Integer.parseInt((String) val);
                            } catch (NumberFormatException e) {
                                board[i][j] = null;
                            }
                        } else {
                            board[i][j] = null;
                        }
                    }
                }
            }
            return board;
        }
        // Si no se puede convertir, devolver un tablero vacío
        return new Integer[10][10];
    }
}