package com.example.demo.socket;

import com.example.demo.game.GameController;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class GameSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    public GameSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/test")
    public void receiveTest(String message) {
        System.out.println("Mensaje recibido del front: " + message);
        messagingTemplate.convertAndSend("/topic/test", "Hola desde el backend");
    }

    @MessageMapping("/game/{gameId}/join")
    public void handleJoin(@DestinationVariable String gameId) {
        Map<String, Object> initData = GameController.gameInitData.get(gameId);
        if (initData == null) return;

        messagingTemplate.convertAndSend("/topic/game/" + gameId, Map.of(
                "type", "GAME_START",
                "gameId", gameId,
                "turn", initData.get("turn"),
                "playerBoard", initData.get("playerBoard"),
                "botBoard", initData.get("botBoard")
        ));
    }

}