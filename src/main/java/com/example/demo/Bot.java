package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bot {

    private List<List<String>> board;

    public Bot() {
        initializeBoard();
        placeShips();
    }

    private void initializeBoard() {
        board = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                row.add(null);
            }
            board.add(row);
        }
    }

    private void placeShips() {
        int[] shipSizes = {5, 4, 3, 3, 2};
        Random rand = new Random();

        for (int size : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int row = rand.nextInt(10);
                int col = rand.nextInt(10);
                boolean horizontal = rand.nextBoolean();

                if (canPlaceShip(row, col, size, horizontal)) {
                    for (int i = 0; i < size; i++) {
                        if (horizontal) {
                            board.get(row).set(col + i, "S");
                        } else {
                            board.get(row + i).set(col, "S");
                        }
                    }
                    placed = true;
                }
            }
        }
    }

    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        if (horizontal) {
            if (col + size > 10) return false;
            for (int i = 0; i < size; i++) {
                if (board.get(row).get(col + i) != null) return false;
            }
        } else {
            if (row + size > 10) return false;
            for (int i = 0; i < size; i++) {
                if (board.get(row + i).get(col) != null) return false;
            }
        }
        return true;
    }

    public List<List<String>> getBoard() {
        return board;
    }
}
