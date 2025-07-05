package com.example.demo.multiplayer;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.example.demo.ranking.RankingService;
import jakarta.annotation.PostConstruct;
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
    private RankingService rankingService;

    @Autowired
    public GameServiceMultiplayer(ShotRepository shotRepository, GameSessionRepository gameSessionRepository) {
        this.shotRepository = shotRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    @PostConstruct
    public void clearOrphanedWaitingGames() {
        List<GameSession> sessions = gameSessionRepository.findAllByEndedAtIsNull();
        for (GameSession s : sessions) {
            if (!gameRooms.containsKey(s.getSessionId())) {
                gameSessionRepository.delete(s);
                System.out.println("🧹 Deleted orphaned session (no endedAt): " + s.getSessionId());
            }
        }
    }

    public GameState getGameState(String sessionId) {
        return gameStates.get(sessionId);
    }

    private int getLevelFromScore(int score) {
        if (score >= 1000) return 5;
        if (score >= 700)  return 4;
        if (score >= 400)  return 3;
        if (score >= 100)  return 2;
        return 1;
    }

    // Crear una sala de espera
    public String createGameRoom(List<List<Integer>> playerBoard, String playerId, int boardSize, boolean matchByLevel) throws JsonProcessingException {
        System.out.println(">> gameRooms size: " + gameRooms.size());

        String sessionId = UUID.randomUUID().toString();
        
        GameRoom room = new GameRoom();
        room.setSessionId(sessionId);
        room.setPlayer1Id(playerId);
        room.setPlayer1Board(playerBoard);
        room.setBoardSize(boardSize);
        room.setMatchByLevel(matchByLevel);
        int playerLevel = rankingService.getScoreIfExists(playerId)
                .map(this::getLevelFromScore)
                .orElse(1);
        room.setLevel(playerLevel);

        room.setStatus("WAITING_FOR_PLAYER");

        System.out.println("Creating game room.. matchByLevel: " + matchByLevel);

        gameRooms.put(sessionId, room);

        ObjectMapper mapper = new ObjectMapper();
        String playerBoardJson = mapper.writeValueAsString(playerBoard);

        GameSession gameSession = new GameSession();
        gameSession.setSessionId(sessionId);
        gameSession.setPlayerOneId(playerId);
        gameSession.setPlayerBoardJson(playerBoardJson);
        gameSession.setBoardSize(boardSize);

        gameSessionRepository.save(gameSession);
        
        return sessionId;
    }

    public String createGameRoom(List<List<Integer>> playerBoard, String playerId, int boardSize, String passkey) throws JsonProcessingException {
        System.out.println(">> gameRooms size: " + gameRooms.size());

        String sessionId = UUID.randomUUID().toString();

        GameRoom room = new GameRoom();
        room.setSessionId(sessionId);
        room.setPlayer1Id(playerId);
        room.setPlayer1Board(playerBoard);
        room.setBoardSize(boardSize);
        room.setMatchByLevel(false);
        room.setPasskey(passkey);
        int playerLevel = rankingService.getScoreIfExists(playerId)
                .map(this::getLevelFromScore)
                .orElse(1);
        room.setLevel(playerLevel);

        room.setStatus("WAITING_FOR_PLAYER");

        gameRooms.put(sessionId, room);

        ObjectMapper mapper = new ObjectMapper();
        String playerBoardJson = mapper.writeValueAsString(playerBoard);

        GameSession gameSession = new GameSession();
        gameSession.setSessionId(sessionId);
        gameSession.setPlayerOneId(playerId);
        gameSession.setPlayerBoardJson(playerBoardJson);
        gameSession.setBoardSize(boardSize);

        gameSessionRepository.save(gameSession);

        return sessionId;
    }

    // Un jugador se une a la sala
    public boolean joinGameRoom(String sessionId, List<List<Integer>> playerBoard, String playerId) {
        GameRoom room = gameRooms.get(sessionId);
        System.out.println("room id: "+ room);
        if (room == null || !room.getStatus().equals("WAITING_FOR_PLAYER") || room.getBoardSize() != playerBoard.size()) {
            return false;
        }

        System.out.println("p1: "+ room.getPlayer1Id());
        System.out.println("p2: "+ playerId);

        room.setPlayer2Id(playerId);
        room.setPlayer2Board(playerBoard);
        room.setStatus("IN_PROGRESS");

        System.out.println("Room matchByLevel: " + room.getMatchByLevel());
        System.out.println("Room level: " + room.getLevel());
        // Ahora podemos crear el GameState
        startGame(room);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String playerBoardJson = mapper.writeValueAsString(playerBoard);

            GameSession session = gameSessionRepository.findBySessionId(sessionId);

            session.setPlayerTwoId(playerId);
            session.setPlayerTwoBoardJson(playerBoardJson);
            session.setStartedAt(LocalDateTime.now());
            gameSessionRepository.saveAndFlush(session);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }


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

        System.out.println("gameState: "+gameState);

        gameStates.put(room.getSessionId(), gameState);

        // Guardar partida en db
        try {
            ObjectMapper mapper = new ObjectMapper();
            String playerBoardJson = mapper.writeValueAsString(room.getPlayer1Board());
            String EnemyPlayerBoardJson = mapper.writeValueAsString(room.getPlayer2Board());

            GameSession session = new GameSession(room.getSessionId(), room.getPlayer1Id(), room.getPlayer2Id(),
                    playerBoardJson, EnemyPlayerBoardJson, room.getBoardSize());
            System.out.println("GUARDANDO JSON:");
            System.out.println("Player board JSON: " + playerBoardJson);
            System.out.println("Enemy board JSON: " + EnemyPlayerBoardJson);
            if (gameSessionRepository.findBySessionId(room.getSessionId()) == null) {
                gameSessionRepository.save(session); // solo guardá si no existe
            }
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
            for (int i = 0; i < gameState.getPlayerBoard().size(); i++) {
                for (int j = 0; j < gameState.getPlayerBoard().size(); j++) {
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

        gameState.switchTurn();

        return response;
    }


    public GameViewDTO resumeGame(String sessionId, String playerId) {
        // Verificar si ya está cargado en memoria
        GameState state = gameStates.get(sessionId);
        GameSession session = gameSessionRepository.findBySessionId(sessionId);

        String playerOneId = session.getPlayerOneId();
        String playerTwoId = session.getPlayerTwoId();

        if (!playerOneId.equals(playerId) && !Objects.equals(playerTwoId, playerId)) {
            throw new IllegalArgumentException("Jugador no autorizado para esta partida");
        }

        if (state == null) {
            // Buscar la sesión en la base
            if (session == null) throw new IllegalArgumentException("Partida no encontrada");

            // Validar que ambos jugadores hayan colocado tablero
            if (session.getPlayerBoardJson() == null || session.getPlayerTwoBoardJson() == null) {
                throw new IllegalStateException("Esperando que ambos jugadores coloquen sus tableros.");
            }

            // Parsear tableros
            List<List<Integer>> board1;
            List<List<Integer>> board2;
            try {
                ObjectMapper mapper = new ObjectMapper();
                board1 = mapper.readValue(session.getPlayerBoardJson(), List.class);
                board2 = mapper.readValue(session.getPlayerTwoBoardJson(), List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializando los tableros", e);
            }

            List<Shot> shots = shotRepository.findAll().stream()
                    .filter(s -> s.getSessionId().equals(sessionId))
                    .toList();

            long shotsP1 = shots.stream().filter(s -> s.getPlayerId().equals(playerOneId)).count();
            long shotsP2 = shots.stream().filter(s -> s.getPlayerId().equals(playerTwoId)).count();
            String nextTurn = (shotsP1 <= shotsP2) ? playerOneId : playerTwoId;


            // Crear estado
            state = new GameState(board1, board2, playerOneId, playerTwoId, nextTurn, null);

            // Disparos
            for (Shot shot : shots) {
                if (shot.isBot()) {
                    state.getplayerTwoShots()[shot.getRow()][shot.getCol()] = true;
                } else {
                    state.getPlayerShots()[shot.getRow()][shot.getCol()] = true;
                }
            }

            gameStates.put(sessionId, state);
        }



        List<Shot> allShots = shotRepository.findAll();

        return GameViewService.toView(session, state, allShots, playerId);
    }



    public String findWaitingGame(int boardSize, boolean matchByLevel, String playerId) {
        int playerLevel = rankingService.getScoreIfExists(playerId)
                .map(this::getLevelFromScore)
                .orElse(1);
        gameRooms.values().forEach(room ->
                System.out.println("RoomId: " + room.getSessionId() + ", status: " + room.getStatus() +
                        ", boardSize: " + room.getBoardSize() + ", passkey: " + room.getPasskey())
        );
        var stream = gameRooms.values().stream();

        var filteredByStatus = stream.filter(room -> room.getStatus().equals("WAITING_FOR_PLAYER")).toList();
        System.out.println("After status: " + filteredByStatus);

        var filteredByBoardSize = filteredByStatus.stream()
                .filter(room -> room.getBoardSize() == boardSize).toList();
        System.out.println("After boardSize: " + filteredByBoardSize);

        var filteredByPasskey = filteredByBoardSize.stream()
                .filter(room -> passkeyMatches(room, null))
                .toList();

        System.out.println("After passkeyMatches: ");
        filteredByPasskey.forEach(room -> System.out.println("RoomId: " + room.getSessionId()));

        var filteredByMatchAllowed = filteredByPasskey.stream()
                .filter(room -> isMatchAllowed(room, matchByLevel, playerLevel))
                .toList();

        System.out.println("After isMatchAllowed: ");
        filteredByMatchAllowed.forEach(room -> System.out.println("RoomId: " + room.getSessionId()));

        String sessionId = filteredByMatchAllowed.stream()
                .map(GameRoom::getSessionId)
                .findFirst()
                .orElse(null);

        System.out.println("GameRooms: " + gameRooms.values());
        System.out.println("Result: " + sessionId);
        return sessionId;
    }

    public String findWaitingGame(int boardSize, String passkey) {
        String result = gameRooms.values().stream()
                .filter(room -> room.getStatus().equals("WAITING_FOR_PLAYER")
                        && room.getBoardSize() == boardSize
                        && passkeyMatches(room, passkey))
                .map(GameRoom::getSessionId)
                .findFirst()
                .orElse(null);
        System.out.println("GameRooms: " + gameRooms.values());
        System.out.println("Result: " + result);
        return result;
    }

    private boolean isMatchAllowed(GameRoom room, boolean matchByLevel, int playerLevel) {
        boolean roomWantsLevel = room.getMatchByLevel();
        boolean playerWantsLevel = matchByLevel;
        boolean levelsMatch = room.getLevel() == playerLevel;

        System.out.println("room wants level: " + roomWantsLevel);
        System.out.println("player wants level: " + matchByLevel);
        System.out.println("room level: " + room.getLevel());
        System.out.println("player level: " + playerLevel);
        System.out.println("levels match: " + levelsMatch);

        if (!roomWantsLevel && !playerWantsLevel) return true;
        return levelsMatch;
    }

    private boolean passkeyMatches(GameRoom room, String passkey){
        return Objects.equals(passkey, room.getPasskey());
    }


    /**
     * Verifica si hay victoria (todos los barcos hundidos).
     */
    private boolean checkForVictory(List<List<Integer>> board, boolean[][] shots) {
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                if (board.get(i).get(j) != null && !shots[i][j]) {
                    // Hay al menos una celda de barco que no ha sido disparada
                    return false;
                }
            }
        }
        return true;
    }

}
