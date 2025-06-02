package com.example.demo.game;

import com.example.demo.bot.dto.GameViewDTO;
import com.example.demo.bot.dto.LastShotDTO;
import com.example.demo.shot.Shot;

import java.util.*;
import java.util.stream.Collectors;

public class GameViewService {

    public static GameViewDTO toView(GameSession session, GameState state, List<Shot> allShots) {
        String sessionId = session.getSessionId();
        List<List<Integer>> rawPlayerBoard = state.getPlayerBoard();
        boolean[][] botShots = state.getplayerTwoShots();
        boolean[][] playerShots = state.getPlayerShots();

        List<List<Integer>> playerBoard = applyShotsToPlayerBoard(rawPlayerBoard, botShots);
        List<List<String>> opponentBoard = generateOpponentView(playerShots, allShots, sessionId, false);
        List<Integer> sunkOpponentShips = detectSunkShips(allShots, sessionId, false, state.getEnemyBoard());
        List<Integer> sunkPlayerShips = detectSunkShips(allShots, sessionId, true, state.getPlayerBoard());
        Map<String, List<Integer>> sunkShips = Map.of(
                "opponent", sunkOpponentShips,
                "player", sunkPlayerShips
        );
        LastShotDTO lastShot = findLastShot(allShots, sessionId, state.getEnemyBoard(), state.getPlayerBoard(), session.getWinner());
        List<Map<String, Object>> history = generateShotHistory(sessionId, allShots);

        System.out.println(session.getWinner());
        System.out.println(session.getEndedAt());
        System.out.println(session.getPlayerOneId());
        System.out.println(session.getPlayerTwoId());
        boolean gameOver = session.getWinner() != null && !session.getWinner().equals("none");
        String winner = gameOver ? session.getWinner() : null;
        String turn = calculateTurn(allShots, session, state.getPlayerId());

        return new GameViewDTO(playerBoard, opponentBoard, sunkShips, lastShot, gameOver, winner, turn, history);
    }

    private static List<List<Integer>> applyShotsToPlayerBoard(List<List<Integer>> board, boolean[][] botShots) {
        List<List<Integer>> result = new ArrayList<>();
        for (int row = 0; row < 10; row++) {
            List<Integer> line = new ArrayList<>();
            for (int col = 0; col < 10; col++) {
                Integer value = board.get(row).get(col);
                if (botShots[row][col]) {
                    line.add(value != null ? -Math.abs(value) : 0); // negativo = impactado, 0 = miss
                } else {
                    line.add(value);
                }
            }
            result.add(line);
        }
        return result;
    }

    private static List<List<String>> generateOpponentView(boolean[][] shots, List<Shot> allShots, String sessionId, boolean isBot) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) result.add(new ArrayList<>(Collections.nCopies(10, null)));

        for (Shot shot : allShots) {
            if (!shot.getSessionId().equals(sessionId)) continue;
            if (shot.isBot() != isBot) continue;

            String value = shot.isHit() ? "hit" : "miss";
            result.get(shot.getRow()).set(shot.getCol(), value);
        }

        return result;
    }

    private static List<Integer> detectSunkShips(
            List<Shot> shots,
            String sessionId,
            boolean attackerIsBot,  // quién disparó
            List<List<Integer>> targetBoard  // sobre qué board se impactó
    ) {
        return shots.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .filter(s -> s.isBot() == attackerIsBot && s.isShipSunk())
                .map(s -> getShipId(targetBoard, s.getRow(), s.getCol()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }


    private static LastShotDTO findLastShot(
            List<Shot> shots,
            String sessionId,
            List<List<Integer>> botBoard,
            List<List<Integer>> playerBoard,
            String winner
    ) {
        List<Shot> sessionShots = shots.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .sorted((s1, s2) -> Long.compare(s1.getId(), s2.getId())) // Asegurar orden por ID
                .toList();

        if (sessionShots.isEmpty()) return null;
        int lastIndex = sessionShots.size() - 1;

        Shot s = sessionShots.get(lastIndex);
        
        return new LastShotDTO(
                s.getRow(),
                s.getCol(),
                s.isHit() ? "hit" : "miss",
                s.isBot() ? "BOT" : s.getPlayerId(),
                s.isShipSunk() ? (s.isBot() ? "¡El oponente hundió tu barco!" : "¡Hundiste un barco!") : null
        );

    }

    private static List<Map<String, Object>> generateShotHistory(String sessionId, List<Shot> shots) {
        List<Map<String, Object>> history = new ArrayList<>();

        for (Shot shot : shots) {
            Map<String, Object> shotData = new HashMap<>();
            shotData.put("row", shot.getRow());
            shotData.put("col", shot.getCol());
            shotData.put("hit", shot.isHit() ? "hit" : "miss");
            shotData.put("player", shot.isBot() ? "opponent" : "player");

            if (shot.isShipSunk()) {
                shotData.put("message", shot.isBot() ?
                        "¡El oponente hundió tu barco!" :
                        "¡Hundiste un barco!");
            }

            history.add(shotData);
        }

        return history;
    }

    // not sure i love this
    private static String calculateTurn(List<Shot> shots, GameSession session, String playerId) {
        long playerShots = shots.stream()
                .filter(s -> s.getSessionId().equals(session.getSessionId()) && !s.isBot())
                .count();
        long botShots = shots.stream()
                .filter(s -> s.getSessionId().equals(session.getSessionId()) && s.isBot())
                .count();
        return playerShots <= botShots ? playerId : "BOT";
    }

    private static Integer getShipId(List<List<Integer>> botBoard, int row, int col) {
        Integer val = botBoard.get(row).get(col);
        return (val != null && val > 0) ? val : null;
    }
}

