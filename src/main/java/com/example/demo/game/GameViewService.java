package com.example.demo.game;

import com.example.demo.bot.dto.GameViewDTO;
import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.shot.Shot;

import java.util.*;
import java.util.stream.Collectors;

public class GameViewService {

    public static GameViewDTO toView(GameSession session, GameState state, List<Shot> allShots) {
        List<List<Integer>> rawPlayerBoard = state.getPlayerBoard();
        boolean[][] botShots = state.getBotShots();
        boolean[][] playerShots = state.getPlayerShots();

        List<List<Integer>> playerBoard = applyShotsToPlayerBoard(rawPlayerBoard, botShots);
        List<List<String>> opponentBoard = generateOpponentView(playerShots, allShots, session.getSessionId(), false);
        List<Integer> sunkOpponentShips = detectSunkShips(allShots, session.getSessionId(), false, state.getBotBoard());
        List<Integer> sunkPlayerShips = detectSunkShips(allShots, session.getSessionId(), true, state.getPlayerBoard());
        Map<String, List<Integer>> sunkShips = Map.of(
                "opponent", sunkOpponentShips,
                "player", sunkPlayerShips
        );
        ShotResultDTO lastShot = findLastShot(allShots, session.getSessionId(), state.getBotBoard(), state.getPlayerBoard());

        boolean gameOver = session.getWinner() != null && !session.getWinner().equals("none");
        String winner = gameOver ? (session.getWinner().equals(state.getPlayerId()) ? "YOU" : "BOT") : null;
        String turn = calculateTurn(allShots, session, state.getPlayerId());

        return new GameViewDTO(playerBoard, opponentBoard, sunkShips, lastShot, gameOver, winner, turn);
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


    private static ShotResultDTO findLastShot(
            List<Shot> shots,
            String sessionId,
            List<List<Integer>> botBoard,
            List<List<Integer>> playerBoard
    ) {
        return shots.stream()
                .filter(s -> s.getSessionId().equals(sessionId))
                .reduce((a, b) -> b)  // último
                .map(s -> {
                    List<List<Integer>> targetBoard = s.isBot() ? playerBoard : botBoard;
                    return new ShotResultDTO(
                            s.getRow(),
                            s.getCol(),
                            s.isHit() ? "hit" : "miss",
                            s.isShipSunk(),
                            s.isShipSunk() ? getShipId(targetBoard, s.getRow(), s.getCol()) : null
                    );
                })
                .orElse(null);
    }


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

