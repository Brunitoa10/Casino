package com.aurum.casino.game.core;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/** Registro inmutable de una jugada: funciona como libro mayor de apuestas. */
@Entity
@Table(name = "game_rounds", indexes = @Index(name = "idx_rounds_player", columnList = "playerId, id"))
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private GameType game;

    @Column(nullable = false)
    private long stake;

    @Column(nullable = false)
    private long payout;

    @Column(nullable = false)
    private long balanceAfter;

    @Column(nullable = false, length = 255)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt;

    protected GameRound() {
        // requerido por JPA
    }

    public GameRound(Long playerId, GameType game, long stake, long payout,
                     long balanceAfter, String summary, Instant createdAt) {
        this.playerId = playerId;
        this.game = game;
        this.stake = stake;
        this.payout = payout;
        this.balanceAfter = balanceAfter;
        this.summary = summary;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public GameType getGame() {
        return game;
    }

    public long getStake() {
        return stake;
    }

    public long getPayout() {
        return payout;
    }

    public long getBalanceAfter() {
        return balanceAfter;
    }

    public String getSummary() {
        return summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
