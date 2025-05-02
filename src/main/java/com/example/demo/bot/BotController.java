package com.example.demo.bot;

import com.example.demo.bot.dto.BoardDTO;
import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.bot.BotService.BotShot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar las operaciones del bot.
 */
@RestController
@RequestMapping("/api/bot")
public class BotController {

    private final BotService botService;
    
    // Almacenamos el estado del juego para cada sesión
    // En una implementación real, esto debería ser un servicio con persistencia adecuada
    private final Map<String, List<int[]>> shotsBySessionId = new HashMap<>();
    private final Map<String, Integer[][]> boardsBySessionId = new HashMap<>();
    
    @Autowired
    public BotController(BotService botService) {
        this.botService = botService;
    }
    
    /**
     * Genera un nuevo tablero aleatorio para el bot.
     * 
     * @param sessionId Identificador de la sesión de juego
     * @return Tablero generado
     */
    @PostMapping("/generate-board")
    public ResponseEntity<BoardDTO> generateBoard(@RequestParam String sessionId) {
        Integer[][] board = botService.generateRandomBoard();
        boardsBySessionId.put(sessionId, board);
        
        // Inicializar la lista de disparos para esta sesión
        shotsBySessionId.put(sessionId, new ArrayList<>());
        
        return ResponseEntity.ok(new BoardDTO(board));
    }
    
    /**
     * Realiza un disparo por parte del bot.
     * 
     * @param sessionId Identificador de la sesión de juego
     * @param boardDTO Tablero del jugador
     * @return Resultado del disparo
     */
    @PostMapping("/make-shot")
    public ResponseEntity<ShotResultDTO> makeShot(
            @RequestParam String sessionId,
            @RequestBody BoardDTO boardDTO) {
        
        // Recuperar o inicializar la lista de disparos para esta sesión
        List<int[]> shotsMade = shotsBySessionId.getOrDefault(sessionId, new ArrayList<>());
        
        // Realizar el disparo
        BotShot botShot = botService.makeBotTurn(boardDTO.getBoard(), shotsMade);
        
        // Actualizar la lista de disparos
        shotsBySessionId.put(sessionId, shotsMade);
        
        // Convertir a DTO y devolver respuesta
        ShotResultDTO result = new ShotResultDTO(
                botShot.getRow(),
                botShot.getCol(),
                botShot.getResult()
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Procesa un disparo del jugador contra el tablero del bot.
     * 
     * @param sessionId Identificador de la sesión de juego
     * @param shotDTO Coordenadas del disparo
     * @return Resultado del disparo
     */
    @PostMapping("/process-player-shot")
    public ResponseEntity<ShotResultDTO> processPlayerShot(
            @RequestParam String sessionId,
            @RequestBody ShotDTO shotDTO) {
        
        // Recuperar el tablero del bot para esta sesión
        Integer[][] botBoard = boardsBySessionId.get(sessionId);
        if (botBoard == null) {
            return ResponseEntity.badRequest().build();
        }
        
        int row = shotDTO.getRow();
        int col = shotDTO.getCol();
        
        // Verificar límites del tablero
        if (row < 0 || row >= 10 || col < 0 || col >= 10) {
            return ResponseEntity.badRequest().build();
        }
        
        // Determinar resultado
        String result = "miss";
        if (botBoard[row][col] != null) {
            Integer shipId = botBoard[row][col];
            result = "hit";
            
            // Marcar como disparado (esto es temporal, debería implementarse mejor)
            botBoard[row][col] = -shipId; // Marcar como disparado
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (botBoard[i][j] != null && botBoard[i][j] == shipId) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                result = "sunk";
            }
        }
        
        return ResponseEntity.ok(new ShotResultDTO(row, col, result));
    }
    
    /**
     * Reinicia el juego para una sesión específica.
     * 
     * @param sessionId Identificador de la sesión de juego
     * @return Estado de la operación
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Boolean>> resetGame(@RequestParam String sessionId) {
        boardsBySessionId.remove(sessionId);
        shotsBySessionId.remove(sessionId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("reset", true);
        
        return ResponseEntity.ok(response);
    }
}