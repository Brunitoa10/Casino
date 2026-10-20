package com.aurum.casino.game.slots;

import java.util.Arrays;

import com.aurum.casino.game.core.RandomSource;

/**
 * Símbolos del rodillo con su peso (frecuencia) y multiplicador por línea de tres iguales.
 * Con estos valores el retorno teórico al jugador (RTP) es ≈ 94,8 %.
 */
public enum SlotSymbol {
    CHERRY(30, 11),
    LEMON(25, 17),
    BELL(18, 30),
    CLOVER(12, 60),
    DIAMOND(8, 120),
    SEVEN(5, 300),
    CROWN(2, 1000);

    private static final int TOTAL_WEIGHT = Arrays.stream(values()).mapToInt(SlotSymbol::weight).sum();

    private final int weight;
    private final int multiplier;

    SlotSymbol(int weight, int multiplier) {
        this.weight = weight;
        this.multiplier = multiplier;
    }

    public int weight() {
        return weight;
    }

    public int multiplier() {
        return multiplier;
    }

    public double probability() {
        return (double) weight / TOTAL_WEIGHT;
    }

    /** Selección ponderada: los símbolos valiosos aparecen menos. */
    public static SlotSymbol draw(RandomSource random) {
        int ticket = random.nextInt(TOTAL_WEIGHT);
        for (SlotSymbol symbol : values()) {
            ticket -= symbol.weight;
            if (ticket < 0) {
                return symbol;
            }
        }
        throw new IllegalStateException("Selección ponderada fuera de rango");
    }
}
