package com.aurum.casino.game.dice;

import java.util.Locale;

import com.aurum.casino.game.core.GameOutcome;

/**
 * @param roll       resultado en centésimas (0 = 0.00, 9999 = 99.99)
 * @param multiplier multiplicador aplicado si gana
 */
public record DiceOutcome(int roll, int target, DiceDirection direction, int winChance,
                          double multiplier, boolean won, long payout) implements GameOutcome {

    @Override
    public String summary() {
        return String.format(Locale.ROOT, "%.2f %s %d · x%.2f", roll / 100.0,
                direction == DiceDirection.UNDER ? "<" : ">", target, multiplier);
    }
}
