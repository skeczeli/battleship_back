package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/game")
public class GameController {

    @PostMapping("/startGame")
    public ResponseEntity<GameState> startGame() {
    
        GameState game = new GameState();  // Al instanciar, el bot ya coloca sus barcos.
    
        return ResponseEntity.ok(game);
    }
    
    
    
}
