package com.aurum.casino.game.session;

import java.time.Instant;

import com.aurum.casino.game.core.GameType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Mano en curso de un juego con estado. Un jugador tiene como máximo una activa por juego. */
@Entity
@Table(name = "game_sessions", indexes = @Index(name = "idx_sessions_lookup", columnList = "playerId, game, status"))
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GameType game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SessionStatus status;

    /** Monto ya debitado al jugador en esta sesión. */
    @Column(nullable = false)
    private long charged;

    /** Estado del motor en JSON (TEXT en SQLite, sin límite práctico). */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String state;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected GameSession() {
        // requerido por JPA
    }

    public GameSession(Long playerId, GameType game, long charged, String state, Instant now) {
        this.playerId = playerId;
        this.game = game;
        this.status = SessionStatus.ACTIVE;
        this.charged = charged;
        this.state = state;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void update(String newState, long newCharged, Instant now) {
        this.state = newState;
        this.charged = newCharged;
        this.updatedAt = now;
    }

    public void finish() {
        this.status = SessionStatus.FINISHED;
    }

    public Long getId() {
        return id;
    }

    public GameType getGame() {
        return game;
    }

    public long getCharged() {
        return charged;
    }

    public String getState() {
        return state;
    }
}
