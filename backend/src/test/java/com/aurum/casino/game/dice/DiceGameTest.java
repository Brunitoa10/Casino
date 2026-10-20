package com.aurum.casino.game.dice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;

class DiceGameTest {

    private final DiceGame dice = new DiceGame();

    @Test
    void underWinsBelowThreshold() {
        var outcome = dice.roll(100, 50, DiceDirection.UNDER, SeededRandom.sequence(4999));

        assertThat(outcome.won()).isTrue();
        assertThat(outcome.payout()).isEqualTo(198);
        assertThat(outcome.multiplier()).isEqualTo(1.98);
    }

    @Test
    void overLosesBelowThreshold() {
        var outcome = dice.roll(100, 50, DiceDirection.OVER, SeededRandom.sequence(4999));

        assertThat(outcome.won()).isFalse();
        assertThat(outcome.payout()).isZero();
    }

    @Test
    void houseEdgeIsOnePercent() {
        var random = SeededRandom.withSeed(7);
        long returned = 0;
        int rounds = 300_000;
        for (int i = 0; i < rounds; i++) {
            returned += dice.roll(1_000, 25, DiceDirection.UNDER, random).payout();
        }
        assertThat((double) returned / (1_000L * rounds)).isBetween(0.97, 1.01);
    }
}
