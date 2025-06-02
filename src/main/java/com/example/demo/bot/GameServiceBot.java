package com.example.demo.bot;

import com.example.demo.bot.bots.BotStrategy;
import com.example.demo.bot.bots.IntelligentBot;
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
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final BotStrategy  simpleBot;
    private final BotStrategy  intelligentBot;
    private final ShotRepository shotRepository;
    private final GameSessionRepository gameSessionRepository;

    // Mapas para almacenar el estado del juego por sesión
    private final Map<String, GameState> gameStates = new HashMap<>();

    @Autowired
    public GameServiceBot(
            @Qualifier("simpleBot") BotStrategy  simpleBot,
            @Qualifier("intelligentBot") BotStrategy  intelligentBot, //mmm como funciona para tener uno solo?
            ShotRepository shotRepository,
            GameSessionRepository gameSessionRepository
    ) {
        this.simpleBot = simpleBot;
        this.intelligentBot = intelligentBot;
        this.shotRepository = shotRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    public GameState getGameState(String sessionId) {
        return gameStates.get(sessionId);
    }

    public String startGame(List<List<Integer>> playerBoard, String playerId, String botType) {
        // Generar ID único para la sesión
        String sessionId = UUID.randomUUID().toString();

        BotStrategy bot = botType.equals("intelligent") ? intelligentBot : simpleBot;

        List<List<Integer>> botBoard = bot.generateRandomBoard();

        GameState gameState = new GameState(playerBoard, botBoard, playerId, "BOT", botType);
        gameStates.put(sessionId, gameState);

        //guardo la partida en db
        try {
            ObjectMapper mapper = new ObjectMapper();
            String playerBoardJson = mapper.writeValueAsString(playerBoard);
            String botBoardJson = mapper.writeValueAsString(botBoard);

            GameSession session = new GameSession(sessionId, playerId, "BOT", playerBoardJson, botBoardJson);
            System.out.println("GUARDANDO JSON:");
            System.out.println("Player board JSON: " + playerBoardJson); //esto es para ver el tablero de ambos en la consola del back
            System.out.println("Bot board JSON: " + botBoardJson);
            gameSessionRepository.saveAndFlush(session);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando los tableros", e);
        }

        return sessionId;
    }

    public Map<String, Object> processPlayerShot(String sessionId, ShotDTO shotDTO, String playerId) {
        GameState gameState = gameStates.get(sessionId);
        if (gameState == null) throw new IllegalStateException("La sesión no existe");
        if (!gameState.getPlayerId().equals(playerId)) throw new IllegalArgumentException("Jugador inválido para esta sesión");

        int row = shotDTO.getRow();
        int col = shotDTO.getCol();

        if (gameState.getPlayerShots()[row][col]) {
            throw new IllegalStateException("Ya has disparado en esta posición");
        }

        // Marcar el disparo del jugador
        gameState.getPlayerShots()[row][col] = true;

        // Determinar resultado
        String result = "miss";
        boolean shipSunk = false;
        Integer shipId = gameState.getEnemyBoard().get(row).get(col);
        if (shipId != null) {
            result = "hit";
            shipSunk = isShipSunk(gameState.getEnemyBoard(), gameState.getPlayerShots(), shipId);
        }

        boolean gameOver = checkForVictory(gameState.getEnemyBoard(), gameState.getPlayerShots());

        Map<String, Object> response = new HashMap<>();
        response.put("playerShotResult", result);
        response.put("gameOver", gameOver);
        response.put("shipSunk", shipSunk);


        // Guardar el disparo del jugador
        Shot playerShot = new Shot(sessionId, playerId, row, col, result.equals("hit"), shipSunk, false);
        shotRepository.save(playerShot);

        GameSession session = gameSessionRepository.findBySessionId(sessionId);

        // Si el jugador ganó primero
        // Aca habia prints de sout "checking win condition y uno mas"
        if (gameOver && session.getWinner() == null) {
            session.setWinner(playerId);
            session.setEndedAt(LocalDateTime.now());
            gameSessionRepository.saveAndFlush(session);
            return response;
        }

        // Turno del bot, procesar su disparo
        BotStrategy bot = gameState.getBotType().equalsIgnoreCase("intelligent") ? intelligentBot : simpleBot;
        ShotResultDTO botShotDTO = bot.processBotShot(gameState);
        response.put("botShotResult", botShotDTO.getResult());
        response.put("botShotRow", botShotDTO.getRow());
        response.put("botShotCol", botShotDTO.getCol());
        response.put("shipSunkBot", botShotDTO.isShipSunk());

        //verificar si el bot ha ganado
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

            if (!session.getPlayerOneId().equals(playerId)) throw new IllegalArgumentException("Jugador no autorizado para esta partida");

            // Parsear tableros
            List<List<Integer>> playerBoard;
            List<List<Integer>> botBoard;
            try {
                ObjectMapper mapper = new ObjectMapper();
                playerBoard = mapper.readValue(session.getPlayerBoardJson(), List.class);
                botBoard = mapper.readValue(session.getplayerTwoBoardJson(), List.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error deserializando tableros", e);
            }

            // El botType no está en DB, asumimos "simple" por ahora
            state = new GameState(playerBoard, botBoard, playerId, "BOT", "simple"); //fuuuuuuck que hago y como. Osea el state que es donde yo guardaba, se esta fijando si en null en este caso (entonces ????)
            // todo: preguntale a @juan si ese comment sigue vigente o si resolvió su duda
            // Disparos
            List<Shot> shots = shotRepository.findAll().stream()
                    .filter(s -> s.getSessionId().equals(sessionId)).toList();

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
        List<Shot> allShots = shotRepository.findAll().stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .sorted((s1, s2) -> s1.getId().compareTo(s2.getId())) // Ordenar por ID para asegurar orden cronológico
                .toList();

        return GameViewService.toView(session, state, allShots);
    }

    private boolean checkForVictory(List<List<Integer>> board, boolean[][] shots) {
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (board.get(i).get(j) != null && !shots[i][j]) return false;
        return true;
    }

    private boolean isShipSunk(List<List<Integer>> board, boolean[][] shots, int shipId) {
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                if (board.get(i).get(j) != null && board.get(i).get(j).equals(shipId) && !shots[i][j]) return false;
        return true;
    }
}
