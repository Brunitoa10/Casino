package com.aurum.casino.game.roulette;

import java.util.Set;

/** Propiedades de los números de la ruleta europea (0-36). */
public final class RouletteNumbers {

    public static final int MAX = 36;

    private static final Set<Integer> RED = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36);

    private RouletteNumbers() {
    }

    public static boolean isRed(int number) {
        return RED.contains(number);
    }

    public static boolean isBlack(int number) {
        return number != 0 && !isRed(number);
    }

    public static String colorOf(int number) {
        if (number == 0) {
            return "GREEN";
        }
        return isRed(number) ? "RED" : "BLACK";
    }
}
