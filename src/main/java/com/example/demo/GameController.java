package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:3000")  // Permite que React acceda
public class GameController {

    @PostMapping("/setup")
    public Map<String, Object> setupGame(@RequestBody Map<String, Object> request) {
        List<List<Object>> playerBoard = (List<List<Object>>) request.get("board");

        List<List<Object>> botBoard = generateBotBoard();

        Map<String, Object> response = new HashMap<>();
        response.put("playerBoard", playerBoard);
        response.put("botBoard", botBoard);

        return response;
    }

    private List<List<Object>> generateBotBoard() {
        List<List<Object>> board = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<Object> row = new ArrayList<>(Collections.nCopies(10, null));
            board.add(row);
        }

        int[][] ships = { {5}, {4}, {3}, {3}, {2} };
        Random random = new Random();

        int shipId = 1;
        for (int[] ship : ships) {
            boolean placed = false;
            while (!placed) {
                int orientation = random.nextInt(2); // 0 horizontal, 1 vertical
                int row = random.nextInt(10);
                int col = random.nextInt(10);
                if (canPlace(board, row, col, ship[0], orientation)) {
                    placeShip(board, row, col, ship[0], orientation, shipId);
                    placed = true;
                    shipId++;
                }
            }
        }
        return board;
    }

    private boolean canPlace(List<List<Object>> board, int row, int col, int size, int orientation) {
        for (int i = 0; i < size; i++) {
            int r = orientation == 0 ? row : row + i;
            int c = orientation == 0 ? col + i : col;
            if (r >= 10 || c >= 10 || board.get(r).get(c) != null) {
                return false;
            }
        }
        return true;
    }

    private void placeShip(List<List<Object>> board, int row, int col, int size, int orientation, int shipId) {
        for (int i = 0; i < size; i++) {
            int r = orientation == 0 ? row : row + i;
            int c = orientation == 0 ? col + i : col;
            board.get(r).set(c, shipId);
        }
    }
}
