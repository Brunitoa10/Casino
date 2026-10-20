package com.aurum.casino.game.core;

/**
 * Abstracción de la fuente de azar (DIP): los motores de juego no dependen
 * de una implementación concreta y pueden probarse con semillas fijas.
 */
@FunctionalInterface
public interface RandomSource {

    /** Devuelve un entero uniforme en [0, bound). */
    public int nextInt(int bound);
}
