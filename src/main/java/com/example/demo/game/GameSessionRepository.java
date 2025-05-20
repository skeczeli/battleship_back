package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findBySessionId(String sessionId);
}

