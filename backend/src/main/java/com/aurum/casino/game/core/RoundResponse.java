package com.aurum.casino.game.core;

/** Respuesta común a todas las jugadas; {@code outcome} varía según el juego. */
public record RoundResponse<O extends GameOutcome>(
        Long roundId,
        GameType game,
        long stake,
        long payout,
        long balance,
        O outcome) {
}
