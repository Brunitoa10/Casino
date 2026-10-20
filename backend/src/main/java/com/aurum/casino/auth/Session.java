package com.aurum.casino.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Sesión de un jugador. Solo se guarda el hash SHA-256 del token:
 * una filtración de la base no expone tokens utilizables.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @Column(length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private Instant expiresAt;

    protected Session() {
        // requerido por JPA
    }

    public Session(String tokenHash, Long playerId, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.playerId = playerId;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Long getPlayerId() {
        return playerId;
    }
}
