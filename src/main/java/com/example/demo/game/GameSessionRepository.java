package com.example.demo.game;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    GameSession findBySessionId(String sessionId);
    List<GameSession> findByPlayerOneId(String userId);
    List<GameSession> findByPlayerTwoId(String userId);
    List<GameSession> findAllByEndedAtIsNull();
}

