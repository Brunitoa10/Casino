package com.aurum.casino.game.session;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurum.casino.game.core.GameType;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {

    public Optional<GameSession> findFirstByPlayerIdAndGameAndStatus(Long playerId, GameType game, SessionStatus status);
}
