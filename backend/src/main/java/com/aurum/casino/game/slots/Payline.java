package com.aurum.casino.game.slots;

import java.util.List;

/** Una línea de pago: fila elegida en cada rodillo (0 = arriba, 2 = abajo). */
public record Payline(int index, List<Integer> rows) {

    public static final List<Payline> STANDARD = List.of(
            new Payline(0, List.of(1, 1, 1)),   // centro
            new Payline(1, List.of(0, 0, 0)),   // arriba
            new Payline(2, List.of(2, 2, 2)),   // abajo
            new Payline(3, List.of(0, 1, 2)),   // diagonal descendente
            new Payline(4, List.of(2, 1, 0)));  // diagonal ascendente
}
