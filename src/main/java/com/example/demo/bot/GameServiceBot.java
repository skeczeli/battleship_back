package com.example.demo.bot;

import com.example.demo.bot.dto.GameViewDTO;
import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameSession;
import com.example.demo.game.GameSessionRepository;
import com.example.demo.game.GameState;

import com.example.demo.game.GameViewService;
import com.example.demo.shot.Shot;
import com.example.demo.shot.ShotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

/**
 * Servicio para gestionar el estado del juego y coordinar la lógica.
 */
@Service
public class GameServiceBot {
    
    private final Bot bot;
    private final ShotRepository shotRepository;
    private final GameSessionRepository gameSessionRepository;
    
    // Mapas para almacenar el estado del juego por sesión
    private final Map<String, GameState> gameStates = new HashMap<>();
    
    @Autowired
    public GameServiceBot(Bot bot, ShotRepository shotRepository, GameSessionRepository gameSessionRepository) {
        this.bot = bot;
        this.shotRepository = shotRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    public GameState getGameState(String sessionId) {
        return gameStates.get(sessionId);
    }
    
    /**
     * Inicia un nuevo juego contra el bot.
     * 
     * @param playerBoard Tablero del jugador
     * @return Identificador de sesión y tablero del bot
     */
    public String startGame(List<List<Integer>> playerBoard, String playerId) {
        // Generar ID único para la sesión
        String sessionId = UUID.randomUUID().toString();
    
        // Generar tablero para el bot
        List<List<Integer>> botBoard = bot.generateRandomBoard();
        
        // Crear nuevo estado de juego
        GameState gameState = new GameState(playerBoard, botBoard, playerId, "BOT");
        
        // Guardar estado
        gameStates.put(sessionId, gameState);

        // Guardar partida en db
        try {
            ObjectMapper mapper = new ObjectMapper();
            String playerBoardJson = mapper.writeValueAsString(playerBoard);
            String botBoardJson = mapper.writeValueAsString(botBoard);

            GameSession session = new GameSession(sessionId, playerId, "BOT", playerBoardJson, botBoardJson);
            System.out.println("GUARDANDO JSON:");
            System.out.println("Player board JSON: " + playerBoardJson);
            System.out.println("Bot board JSON: " + botBoardJson);
            gameSessionRepository.saveAndFlush(session);
            System.out.println("✅ INSERT OK: " + gameSessionRepository.findAll().size());
            System.out.println(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando los tableros", e);
        }

        // Devolver directamente el sessionId
        return sessionId;
    }
    
    /**
     * Procesa un disparo del jugador.
     *
     * @param sessionId Id de la sesión
     * @param shotDTO   Información del disparo
     * @param playerId Id del jugador
     * @return Resultado del disparo y posible disparo del bot
     */
    public Map<String, Object> processPlayerShot(String sessionId, ShotDTO shotDTO, String playerId) {
        GameState gameState = gameStates.get(sessionId);
        if (gameState == null) {
            throw new IllegalStateException("La sesión no existe");
        }
        if (!gameState.getPlayerId().equals(playerId)) {
            throw new IllegalArgumentException("Jugador inválido para esta sesión");
        }

        // Procesar disparo en el tablero del bot
        int row = shotDTO.getRow();
        int col = shotDTO.getCol();
        
        // Validar que la celda no haya sido disparada antes
        if (gameState.getPlayerShots()[row][col]) {
            throw new IllegalStateException("Ya has disparado en esta posición");
        }
        
        // Marcar la celda como disparada
        gameState.getPlayerShots()[row][col] = true;
        
        // Determinar resultado
        String result = "miss";
        boolean shipSunk = false;
        if (gameState.getEnemyBoard().get(row).get(col) != null) {
            Integer shipId = gameState.getEnemyBoard().get(row).get(col);
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (gameState.getEnemyBoard().get(i).get(j) != null && 
                        gameState.getEnemyBoard().get(i).get(j).equals(shipId) && 
                        !gameState.getPlayerShots()[i][j]) {
                        allHit = false;
                        break;
                    }
                }
            }
            
            if (allHit) {
                shipSunk = true;;
            }
        }
        
        // Verificar si el juego ha terminado
        boolean gameOver = checkForVictory(gameState.getEnemyBoard(), gameState.getPlayerShots());
        
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerShotResult", result);
        response.put("gameOver", gameOver);
        response.put("shipSunk", shipSunk);

        // Guardar disparo del jugador
        Shot playerShot = new Shot(sessionId, playerId, row, col, result.equals("hit"), shipSunk, false);
        shotRepository.save(playerShot);

        GameSession session = gameSessionRepository.findBySessionId(sessionId);

        // Si el jugador ganó primero
        System.out.println("Checking win condition...");
        System.out.println("gameOver: " + gameOver + ", sessionWinner: " + session.getWinner());
        if (gameOver && session.getWinner() == null) {
            session.setWinner(playerId);
            session.setEndedAt(LocalDateTime.now());
            System.out.println("Setting player winner: " + session.getWinner());
            gameSessionRepository.saveAndFlush(session);
        }


        //Turno del bot, procesar su disparo
        ShotResultDTO botShotDTO = bot.processBotShot(gameState);
        response.put("botShotResult", botShotDTO.getResult());
        response.put("botShotRow", botShotDTO.getRow());
        response.put("botShotCol", botShotDTO.getCol());
        response.put("shipSunkBot", botShotDTO.isShipSunk());
        
        // Verificar si el bot ha ganado
        boolean gameOverBot = checkForVictory(gameState.getPlayerBoard(), gameState.getplayerTwoShots());
        response.put("gameOverBot", gameOverBot);

        Shot botShot = new Shot(
                sessionId,
                "BOT",
                botShotDTO.getRow(),
                botShotDTO.getCol(),
                botShotDTO.getResult().equals("hit"),
                botShotDTO.isShipSunk(),
                true
        );
        shotRepository.save(botShot);

        System.out.println("Checking win condition...");
        System.out.println("gameOverBot: " + gameOverBot + ", sessionWinner: " + session.getWinner());
        if (gameOverBot && session.getWinner() == null) {
            session.setWinner("BOT");
            session.setEndedAt(LocalDateTime.now());
            System.out.println("Setting bot winner: " + session.getWinner());
            gameSessionRepository.saveAndFlush(session);
        }

        return response;
    }

    public GameViewDTO resumeGame(String sessionId, String playerId) {
        // Verificar si ya está cargado en memoria
        GameState state = gameStates.get(sessionId);
        if (state == null) {
            // Buscar la sesión en la base
            GameSession session = gameSessionRepository.findBySessionId(sessionId);
            if (session == null) throw new IllegalArgumentException("Partida no encontrada");

            if (!session.getPlayerOneId().equals(playerId)) {
                throw new IllegalArgumentException("Jugador no autorizado para esta partida");
            }

            // Parsear tableros
            List<List<Integer>> playerBoard;
            List<List<Integer>> botBoard;
            try {
                ObjectMapper mapper = new ObjectMapper();
                playerBoard = mapper.readValue(session.getPlayerBoardJson(), List.class);
                botBoard = mapper.readValue(session.getplayerTwoBoardJson(), List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializando los tableros", e);
            }

            // Crear estado
            state = new GameState(playerBoard, botBoard, playerId, "BOT");

            // Disparos
            List<Shot> shots = shotRepository.findAll().stream()
                    .filter(s -> s.getSessionId().equals(sessionId))
                    .toList();

            for (Shot shot : shots) {
                if (shot.isBot()) {
                    state.getplayerTwoShots()[shot.getRow()][shot.getCol()] = true;
                } else {
                    state.getPlayerShots()[shot.getRow()][shot.getCol()] = true;
                }
            }

            gameStates.put(sessionId, state);
        }

        // Obtener sesión de base por si no la teníamos
        GameSession session = gameSessionRepository.findBySessionId(sessionId);
        List<Shot> allShots = shotRepository.findAll();

        return GameViewService.toView(session, state, allShots);
    }

    /**
     * Verifica si hay victoria (todos los barcos hundidos).
     */
    private boolean checkForVictory(List<List<Integer>> board, boolean[][] shots) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.get(i).get(j) != null && !shots[i][j]) {
                    // Hay al menos una celda de barco que no ha sido disparada
                    return false;
                }
            }
        }
        return true;
    }
    
}