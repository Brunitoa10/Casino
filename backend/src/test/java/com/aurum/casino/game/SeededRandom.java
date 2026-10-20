package com.aurum.casino.game;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

import com.aurum.casino.game.core.RandomSource;

/** Fuentes de azar deterministas para tests. */
public final class SeededRandom {

    private SeededRandom() {
    }

    public static RandomSource withSeed(long seed) {
        Random random = new Random(seed);
        return random::nextInt;
    }

    /** Devuelve los valores indicados en orden (para forzar resultados). */
    public static RandomSource sequence(int... values) {
        Deque<Integer> queue = new ArrayDeque<>();
        for (int value : values) {
            queue.add(value);
        }
        return bound -> queue.removeFirst();
    }
}
