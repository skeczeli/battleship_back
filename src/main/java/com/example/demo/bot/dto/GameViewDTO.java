package com.example.demo.bot.dto;

import java.util.List;
import java.util.Map;

/**
 * @param playerBoard   tus barcos con disparos aplicados
 * @param opponentBoard sólo tus disparos al bot: "hit", "miss", null
 * @param winner        "YOU", "BOT" o null
 */
public record GameViewDTO(List<List<Integer>> playerBoard, List<List<String>> opponentBoard, Map<String, List<Integer>> sunkShips,
                          LastShotDTO lastShot, boolean gameOver, String winner, String turn) {
}

