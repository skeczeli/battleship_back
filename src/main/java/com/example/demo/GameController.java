package com.example.demo;

import com.example.demo.bot.dto.BoardDTO;
import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.bot.BotController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador principal para gestionar las partidas.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    
    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    /**
     * Inicia un nuevo juego contra el bot.
     * 
     * @param boardDTO Tablero configurado por el jugador
     * @return Información inicial del juego
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startGame(@RequestBody BoardDTO boardDTO) {
        Map<String, Object> gameInfo = gameService.startGame(boardDTO.getBoard());
        return ResponseEntity.ok(gameInfo);
    }
    
    /**
     * Procesa un disparo del jugador en modo contra bot.
     * 
     * @param sessionId ID de la sesión de juego
     * @param shotDTO Información del disparo
     * @return Resultado del disparo y posible respuesta del bot
     */
    @PostMapping("/shot")
    public ResponseEntity<Map<String, Object>> processPlayerShot(
            @RequestParam String sessionId,
            @RequestBody ShotDTO shotDTO) {
        try {
            Map<String, Object> result = gameService.processPlayerShot(sessionId, shotDTO);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            // Manejo adecuado de errores
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Reinicia la partida actual.
     * 
     * @param sessionId ID de la sesión de juego
     * @return Confirmación de reinicio
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Boolean>> resetGame(@RequestParam String sessionId) {
        // Delegamos al BotController
        BotController botController = new BotController(null); // Solo para usar su método
        return botController.resetGame(sessionId);
    }
}