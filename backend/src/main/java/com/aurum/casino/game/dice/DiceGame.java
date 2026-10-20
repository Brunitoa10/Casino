package com.aurum.casino.game.dice;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.shared.error.DomainException;

/**
 * Dados de probabilidad elegida: el jugador fija un objetivo y apuesta por debajo o por encima.
 * Multiplicador = 99 / probabilidad (ventaja de la casa del 1 %).
 */
@Component
public class DiceGame {

    public static final int MIN_TARGET = 2;
    public static final int MAX_TARGET = 98;
    private static final int RESOLUTION = 10_000;       // 0.00 - 99.99
    private static final int RTP_PERCENT = 99;

    public DiceOutcome roll(long stake, int target, DiceDirection direction, RandomSource random) {
        validate(target, direction);
        int winChance = direction == DiceDirection.UNDER ? target : 100 - target;
        int threshold = target * (RESOLUTION / 100);

        int roll = random.nextInt(RESOLUTION);
        boolean won = direction == DiceDirection.UNDER ? roll < threshold : roll >= threshold;

        long payout = won ? stake * RTP_PERCENT / winChance : 0L;
        double multiplier = Math.floor(RTP_PERCENT * 10_000.0 / winChance) / 10_000.0;
        return new DiceOutcome(roll, target, direction, winChance, multiplier, won, payout);
    }

    private static void validate(int target, DiceDirection direction) {
        if (direction == null) {
            throw DomainException.invalidBet("Elegí OVER o UNDER");
        }
        if (target < MIN_TARGET || target > MAX_TARGET) {
            throw DomainException.invalidBet("El objetivo debe estar entre " + MIN_TARGET + " y " + MAX_TARGET);
        }
    }
}
