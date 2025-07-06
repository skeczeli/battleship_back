package com.example.demo.bot.bots;

import com.example.demo.bot.dto.ShotResultDTO;
import com.example.demo.game.GameState;

import java.util.*;

public class ProbabilisticBot extends AbstractBot {

    private final List<int[]> hitStack = new ArrayList<>();
    private int[] currentDirection = null;
    private int[] origin = null;

    private static final int[][] DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    public ProbabilisticBot(int BOARD_SIZE) {
        super(BOARD_SIZE);
    }

    @Override
    public List<List<Integer>> generateRandomBoard() {
        List<List<Integer>> board = emptyBoard();
        int[][] shipsSorted = Arrays.stream(SHIPS)
            .sorted((a, b) -> Integer.compare(b[1], a[1]))
            .toArray(int[][]::new);

        if (placeShipBacktrack(board, shipsSorted, 0)) return board;
        return generateRandomBoard();
    }

    private boolean placeShipBacktrack(List<List<Integer>> board, int[][] ships, int idx) {
        if (idx == ships.length) return true;
        int id = ships[idx][0], size = ships[idx][1];
        List<Placement> placements = generateShuffledPlacements(size);

        for (Placement p : placements) {
            if (canPlace(board, p.row, p.col, size, p.horizontal)) {
                putShip(board, id, p.row, p.col, size, p.horizontal);
                if (placeShipBacktrack(board, ships, idx + 1)) return true;
                removeShip(board, p.row, p.col, size, p.horizontal);
            }
        }
        return false;
    }

    private static class Placement {
        int row, col; boolean horizontal;
        Placement(int r, int c, boolean h) { row = r; col = c; horizontal = h; }
    }

    private List<Placement> generateShuffledPlacements(int size) {
        List<Placement> list = new ArrayList<>();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (!isCorner(r, c) || Math.random() < 0.3) {
                    list.add(new Placement(r, c, true));
                    list.add(new Placement(r, c, false));
                }
            }
        }
        Collections.shuffle(list);
        return list;
    }

    private boolean canPlace(List<List<Integer>> board, int row, int col, int size, boolean horizontal) {
        if (horizontal && col + size > BOARD_SIZE) return false;
        if (!horizontal && row + size > BOARD_SIZE) return false;

        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            if (board.get(r).get(c) != null || hasNeighbor(board, r, c)) return false;
        }
        return true;
    }

    private boolean hasNeighbor(List<List<Integer>> board, int row, int col) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int rr = row + dr, cc = col + dc;
                if (inbound(rr, cc) && board.get(rr).get(cc) != null) return true;
            }
        }
        return false;
    }

    private boolean inbound(int r, int c) {
        return r >= 0 && c >= 0 && r < BOARD_SIZE && c < BOARD_SIZE;
    }

    private boolean isCorner(int r, int c) {
        return (r == 0 || r == BOARD_SIZE - 1) && (c == 0 || c == BOARD_SIZE - 1);
    }

    private void putShip(List<List<Integer>> board, int shipId, int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            board.get(r).set(c, shipId);
        }
    }

    private void removeShip(List<List<Integer>> board, int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            board.get(r).set(c, null);
        }
    }

    private List<List<Integer>> emptyBoard() {
        List<List<Integer>> board = new ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {
            board.add(new ArrayList<>(Collections.nCopies(BOARD_SIZE, null)));
        }
        return board;
    }

    @Override
    public ShotResultDTO processBotShot(GameState gameState) {
        int row, col;

        if (!hitStack.isEmpty()) {
            int[] target = getNextTarget(gameState);
            row = target[0];
            col = target[1];
        } else {
            int[] target = getMostProbableShot(gameState);
            row = target[0];
            col = target[1];
        }

        gameState.getplayerTwoShots()[row][col] = true;

        String result = "miss";
        Integer shipId = null;
        boolean shipSunk = false;

        if (gameState.getPlayerBoard().get(row).get(col) != null) {
            shipId = gameState.getPlayerBoard().get(row).get(col);
            result = "hit";

            if (origin == null) origin = new int[]{row, col};
            hitStack.add(new int[]{row, col});

            shipSunk = isShipSunk(gameState, shipId);
            if (shipSunk) resetHuntingState();
        } else {
            if (currentDirection != null && origin != null) {
                currentDirection = new int[]{-currentDirection[0], -currentDirection[1]};
                hitStack.clear();
                hitStack.add(origin.clone());
            }
        }

        return new ShotResultDTO(row, col, result, shipSunk, shipId);
    }

    private int[] getNextTarget(GameState gameState) {
        if (currentDirection != null && origin != null) {
            int[] lastHit = hitStack.get(hitStack.size() - 1);
            int nextRow = lastHit[0] + currentDirection[0];
            int nextCol = lastHit[1] + currentDirection[1];
            if (isValid(nextRow, nextCol) && !gameState.getplayerTwoShots()[nextRow][nextCol]) {
                return new int[]{nextRow, nextCol};
            } else {
                currentDirection = new int[]{-currentDirection[0], -currentDirection[1]};
                hitStack.clear();
                hitStack.add(origin.clone());
            }
        }

        if (origin != null) {
            for (int[] dir : DIRECTIONS) {
                int nextRow = origin[0] + dir[0];
                int nextCol = origin[1] + dir[1];
                if (isValid(nextRow, nextCol) && !gameState.getplayerTwoShots()[nextRow][nextCol]) {
                    currentDirection = dir;
                    return new int[]{nextRow, nextCol};
                }
            }
        }

        resetHuntingState();
        return getMostProbableShot(gameState);
    }

    private int[] getMostProbableShot(GameState gameState) {
        int[][] heatmap = new int[BOARD_SIZE][BOARD_SIZE];

        for (int[] ship : SHIPS) {
            int shipSize = ship[1];

            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col <= BOARD_SIZE - shipSize; col++) {
                    boolean fits = true;
                    for (int k = 0; k < shipSize; k++) {
                        if (gameState.getplayerTwoShots()[row][col + k]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        for (int k = 0; k < shipSize; k++) {
                            heatmap[row][col + k]++;
                        }
                    }
                }
            }

            for (int col = 0; col < BOARD_SIZE; col++) {
                for (int row = 0; row <= BOARD_SIZE - shipSize; row++) {
                    boolean fits = true;
                    for (int k = 0; k < shipSize; k++) {
                        if (gameState.getplayerTwoShots()[row + k][col]) {
                            fits = false;
                            break;
                        }
                    }
                    if (fits) {
                        for (int k = 0; k < shipSize; k++) {
                            heatmap[row + k][col]++;
                        }
                    }
                }
            }
        }

        int bestRow = -1, bestCol = -1, max = -1;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (!gameState.getplayerTwoShots()[row][col] && heatmap[row][col] > max) {
                    max = heatmap[row][col];
                    bestRow = row;
                    bestCol = col;
                }
            }
        }

        return new int[]{bestRow, bestCol};
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    private void resetHuntingState() {
        hitStack.clear();
        currentDirection = null;
        origin = null;
    }
} 
