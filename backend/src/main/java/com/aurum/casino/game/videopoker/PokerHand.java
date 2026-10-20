package com.aurum.casino.game.videopoker;

/**
 * Tabla de pagos "Jacks or Better 9/6" (pago "por 1": incluye la apuesta).
 * RTP con estrategia óptima ≈ 99,5 %.
 */
public enum PokerHand {
    ROYAL_FLUSH("Escalera real", 800),
    STRAIGHT_FLUSH("Escalera de color", 50),
    FOUR_OF_A_KIND("Póker", 25),
    FULL_HOUSE("Full", 9),
    FLUSH("Color", 6),
    STRAIGHT("Escalera", 4),
    THREE_OF_A_KIND("Trío", 3),
    TWO_PAIR("Doble par", 2),
    JACKS_OR_BETTER("Par de J o mejor", 1),
    NOTHING("Sin juego", 0);

    private final String label;
    private final int multiplier;

    PokerHand(String label, int multiplier) {
        this.label = label;
        this.multiplier = multiplier;
    }

    public String label() {
        return label;
    }

    public int multiplier() {
        return multiplier;
    }
}
