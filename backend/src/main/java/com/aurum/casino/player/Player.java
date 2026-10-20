package com.aurum.casino.player;

import java.time.Instant;

import com.aurum.casino.shared.error.DomainException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Jugador y su billetera. Modelo rico: las reglas del saldo
 * (no negativo, montos positivos) viven aquí y no en los servicios.
 */
@Entity
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Player() {
        // requerido por JPA
    }

    private Player(String username, String passwordHash, long balance, Instant createdAt) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public static Player register(String username, String passwordHash, long initialBalance, Instant now) {
        return new Player(username, passwordHash, initialBalance, now);
    }

    public void debit(long amount) {
        requirePositive(amount);
        if (amount > balance) {
            throw DomainException.insufficientFunds();
        }
        balance -= amount;
    }

    public void credit(long amount) {
        if (amount < 0) {
            throw DomainException.invalidBet("No se puede acreditar un monto negativo");
        }
        balance += amount;
    }

    public boolean isBelow(long threshold) {
        return balance < threshold;
    }

    private static void requirePositive(long amount) {
        if (amount <= 0) {
            throw DomainException.invalidBet("El monto debe ser mayor a cero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public long getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
