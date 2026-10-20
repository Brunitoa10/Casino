package com.aurum.casino.game.plinko;

/**
 * Multiplicadores (centésimas) de las 13 casillas para 12 filas, simétricos desde el centro.
 * RTP de cada perfil ≈ 99 % (distribución binomial de 12 rebotes).
 */
public enum PlinkoRisk {
    LOW(new int[] {1000, 300, 160, 140, 110, 100, 50}),
    MEDIUM(new int[] {3300, 1100, 400, 200, 110, 60, 30}),
    HIGH(new int[] {17000, 2400, 810, 200, 70, 20, 20});

    private final int[] halfTable;

    PlinkoRisk(int[] halfTable) {
        this.halfTable = halfTable;
    }

    /** Multiplicador de la casilla {@code slot} (0..12). */
    public int multiplierCents(int slot) {
        int fromEdge = Math.min(slot, PlinkoBoard.ROWS - slot);
        return halfTable[fromEdge];
    }
}
