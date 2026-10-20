package com.aurum.casino.game.crash;

/** Curva exponencial compartida con el cliente: m(t) = e^(k·t). Se trabaja en centésimas. */
public final class CrashCurve {

    /** Crecimiento por milisegundo: ×2 a los ~5,8 s, ×10 a los ~19 s. */
    public static final double GROWTH = 0.00012;

    private CrashCurve() {
    }

    public static long centsAt(long elapsedMs) {
        return (long) Math.floor(100 * Math.exp(GROWTH * Math.max(0, elapsedMs)));
    }

    public static double toMultiplier(long cents) {
        return cents / 100.0;
    }
}
