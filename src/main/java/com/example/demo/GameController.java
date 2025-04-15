package com.example.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play")
public class GameController {

    private final Bot bot = new Bot();

    // Endpoint para que el bot dispare una coordenada aleatoria
    @GetMapping("/bot/disparo")
    public ResponseEntity<Bot.Coordenada> disparoDelBot() {
        Bot.Coordenada disparo = bot.disparar();
        return ResponseEntity.ok(disparo);
    }

    // Endpoint para reiniciar el estado del bot (al empezar una nueva partida)
    @PostMapping("/bot/reiniciar")
    public ResponseEntity<String> reiniciarBot() {
        bot.reiniciar();
        return ResponseEntity.ok("Bot reiniciado");
    }

}
