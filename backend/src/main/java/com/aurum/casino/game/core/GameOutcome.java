package com.aurum.casino.game.core;

/** Resultado de una jugada producido por un motor de juego. */
public interface GameOutcome {

    /** Monto total devuelto al jugador (0 si pierde; incluye la apuesta si gana). */
    public long payout();

    /** Descripción breve para el historial. */
    public String summary();
}
