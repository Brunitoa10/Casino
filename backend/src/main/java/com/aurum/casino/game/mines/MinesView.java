package com.aurum.casino.game.mines;

import java.util.List;

/** Las posiciones de las minas solo viajan al cliente cuando la ronda terminó. */
public record MinesView(int mines, List<Integer> revealed, double multiplier, double nextMultiplier,
                        boolean finished, boolean busted, Integer hitMine, List<Integer> minePositions,
                        long payout) {
}
