package com.aurum.casino.game.keno;

import java.util.Map;

/**
 * Multiplicadores en centésimas por cantidad de números elegidos y aciertos.
 * Calibrados con la distribución hipergeométrica para un RTP de ~93–96 % en cada fila.
 */
public final class KenoPaytable {

    private static final Map<Integer, int[]> TABLE = Map.of(
            1, new int[] {0, 380},
            2, new int[] {0, 100, 950},
            3, new int[] {0, 0, 200, 4800},
            4, new int[] {0, 0, 160, 700, 9500},
            5, new int[] {0, 0, 120, 300, 1500, 30000},
            6, new int[] {0, 0, 0, 300, 900, 5500, 100000},
            7, new int[] {0, 0, 0, 150, 500, 2500, 20000, 250000},
            8, new int[] {0, 0, 0, 100, 300, 1200, 6500, 50000, 500000},
            9, new int[] {0, 0, 0, 100, 200, 500, 2800, 15000, 150000, 1000000},
            10, new int[] {0, 0, 0, 0, 200, 500, 1500, 8000, 50000, 400000, 1000000});

    private KenoPaytable() {
    }

    public static int multiplierCents(int picks, int hits) {
        int[] row = TABLE.get(picks);
        if (row == null || hits < 0 || hits >= row.length) {
            return 0;
        }
        return row[hits];
    }

    public static int[] row(int picks) {
        return TABLE.get(picks).clone();
    }
}
