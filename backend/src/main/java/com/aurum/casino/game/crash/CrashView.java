package com.aurum.casino.game.crash;

/**
 * Vista del cohete. {@code elapsedMs} es el tiempo de vuelo según el servidor al responder:
 * el cliente anima desde ahí. El punto de crash solo se revela al terminar.
 */
public record CrashView(long elapsedMs, double multiplier, Double autoCashout, boolean finished,
                        boolean busted, Double cashedOutAt, Double crashPoint, long payout) {
}
