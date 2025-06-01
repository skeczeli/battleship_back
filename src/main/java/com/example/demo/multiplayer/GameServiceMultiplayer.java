package com.example.demo.multiplayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.bot.dto.ShotDTO;
import com.example.demo.game.GameSessionRepository;
import com.example.demo.game.GameState;
import com.example.demo.game.GameViewService;
import com.example.demo.shot.ShotRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.game.GameSession;
import com.example.demo.shot.Shot;
import com.example.demo.bot.dto.GameViewDTO;
import com.example.demo.game.GameRoom;

@Service
public class GameServiceMultiplayer {

    private final ShotRepository shotRepository;
    private final GameSessionRepository gameSessionRepository;

    // Mapas para almacenar el estado del juego por sesión
    private final Map<String, GameState> gameStates = new HashMap<>();
    private final Map<String, GameRoom> gameRooms = new HashMap<>(); //"sala de espera"

    @Autowired
    public GameServiceMultiplayer(ShotRepository shotRepository, GameSessionRepository gameSessionRepository) {
        this.shotRepository = shotRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    public GameState getGameState(String sessionId) {
        return gameStates.get(sessionId);
    }

    // Crear una sala de espera
    public String createGameRoom(List<List<Integer>> playerBoard, String playerId) {
        String sessionId = UUID.randomUUID().toString();
        
        GameRoom room = new GameRoom();
        room.setSessionId(sessionId);
        room.setPlayer1Id(playerId);
        room.setPlayer1Board(playerBoard);
        room.setStatus("WAITING_FOR_PLAYER");
        
        gameRooms.put(sessionId, room);
        
        return sessionId;
    }

    // Un jugador se une a la sala
    public boolean joinGameRoom(String sessionId, List<List<Integer>> playerBoard, String playerId) {
        GameRoom room = gameRooms.get(sessionId);
        if (room == null || !room.getStatus().equals("WAITING_FOR_PLAYER")) {
            return false;
        }
        
        room.setPlayer2Id(playerId);
        room.setPlayer2Board(playerBoard);
        room.setStatus("IN_PROGRESS");
        
        // Ahora podemos crear el GameState
        startGame(room);
        
        return true;
    }
    
    public void startGame(GameRoom room) {

        // Crear nuevo estado de juego
        GameState gameState = new GameState(
            room.getPlayer1Board(),
            room.getPlayer2Board(),
            room.getPlayer1Id(),
            room.getPlayer2Id(),
            null
        );

        gameStates.put(room.getSessionId(), gameState);

        // Guardar partida en db
        try {
            ObjectMapper mapper = new ObjectMapper();
            String playerBoardJson = mapper.writeValueAsString(room.getPlayer1Board());
            String EnemyPlayerBoardJson = mapper.writeValueAsString(room.getPlayer2Board());

            GameSession session = new GameSession(room.getSessionId(), room.getPlayer1Id(), room.getPlayer2Id(), playerBoardJson, EnemyPlayerBoardJson);
            System.out.println("GUARDANDO JSON:");
            System.out.println("Player board JSON: " + playerBoardJson);
            System.out.println("Enemy board JSON: " + EnemyPlayerBoardJson);
            gameSessionRepository.save(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando los tableros", e);
        }
    }


    public Map<String, Object> processPlayerShot(String sessionId, ShotDTO shotDTO, String playerId) {
        GameState gameState = gameStates.get(sessionId);
        if (gameState == null) {
            throw new IllegalStateException("La sesión no existe");
        }
        if (!gameState.getPlayerId().equals(playerId) && !gameState.getPlayerTwoId().equals(playerId)) {
            throw new IllegalArgumentException("Jugador inválido para esta sesión");
        }

        int row = shotDTO.getRow();
        int col = shotDTO.getCol();

        // Determinar qué tablero atacar y qué array de disparos usar
        List<List<Integer>> targetBoard;
        boolean[][] playerShots;
        
        if (gameState.getPlayerId().equals(playerId)) {
            // Player 1 dispara al tablero de Player 2
            targetBoard = gameState.getEnemyBoard(); // Necesitas este getter
            playerShots = gameState.getPlayerShots();
        } else {
            // Player 2 dispara al tablero de Player 1
            targetBoard = gameState.getPlayerBoard();
            playerShots = gameState.getplayerTwoShots(); // Necesitas este getter
        }
        
        // Validar que la celda no haya sido disparada antes
        if (playerShots[row][col]) {
            throw new IllegalStateException("Ya has disparado en esta posición");
        }
        
        // Marcar la celda como disparada
        playerShots[row][col] = true;
        
        // Determinar resultado
        String result = "miss";
        boolean shipSunk = false;
        if (targetBoard.get(row).get(col) != null) {
            Integer shipId = targetBoard.get(row).get(col);
            result = "hit";
            
            // Verificar si el barco está hundido
            boolean allHit = true;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (targetBoard.get(i).get(j) != null && 
                        targetBoard.get(i).get(j).equals(shipId) && 
                        !playerShots[i][j]) {
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
        boolean gameOver = checkForVictory(targetBoard, playerShots);
        
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerShotResult", result);
        response.put("gameOver", gameOver);
        response.put("shipSunk", shipSunk);

        // Guardar disparo del jugador
        Shot playerShot = new Shot(sessionId, playerId, row, col, result.equals("hit"), shipSunk, false);
        shotRepository.save(playerShot);

        if (gameOver) {
            GameSession session = gameSessionRepository.findBySessionId(sessionId);
            session.setWinner(playerId);
            session.setEndedAt(LocalDateTime.now());
            gameSessionRepository.save(session);
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

        String playerOneId = session.getPlayerOneId();
        String playerTwoId = session.getPlayerTwoId();

        if (!playerOneId.equals(playerId) && !playerTwoId.equals(playerId)) {
            throw new IllegalArgumentException("Jugador no autorizado para esta partida");
        }

        boolean isPlayerOne = playerId.equals(playerOneId);
        String enemyId = isPlayerOne ? playerTwoId : playerOneId;

        // Validar que ambos jugadores hayan colocado tablero
        if (session.getPlayerBoardJson() == null || session.getplayerTwoBoardJson() == null) {
            throw new IllegalStateException("Esperando que ambos jugadores coloquen sus tableros.");
        }

        // Parsear tableros
        List<List<Integer>> board1;
        List<List<Integer>> board2;
        try {
            ObjectMapper mapper = new ObjectMapper();
            board1 = mapper.readValue(session.getPlayerBoardJson(), List.class);
            board2 = mapper.readValue(session.getplayerTwoBoardJson(), List.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializando los tableros", e);
        }

        List<List<Integer>> playerBoard = isPlayerOne ? board1 : board2;
        List<List<Integer>> enemyBoard = isPlayerOne ? board2 : board1;

        // Crear estado
        state = new GameState(playerBoard, enemyBoard, playerId, enemyId, null);

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

        GameSession session = gameSessionRepository.findBySessionId(sessionId);
        List<Shot> allShots = shotRepository.findAll();

        return GameViewService.toView(session, state, allShots);
    }



    public String findWaitingGame() {
        return gameRooms.values().stream()
            .filter(room -> room.getStatus().equals("WAITING_FOR_PLAYER"))
            .map(GameRoom::getSessionId)
            .findFirst()
            .orElse(null);
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
