package com.aurum.casino.game.plinko;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;

class PlinkoBoardTest {

    @Test
    void exactRtpIsAboutNinetyNinePercentForEveryRisk() {
        for (PlinkoRisk risk : PlinkoRisk.values()) {
            double rtp = 0;
            for (int slot = 0; slot <= PlinkoBoard.ROWS; slot++) {
                rtp += binomial(PlinkoBoard.ROWS, slot) / Math.pow(2, PlinkoBoard.ROWS)
                        * risk.multiplierCents(slot) / 100.0;
            }
            assertThat(rtp).as("RTP %s", risk).isBetween(0.98, 1.0);
        }
    }

    @Test
    void slotIsTheNumberOfRightBounces() {
        var outcome = new PlinkoBoard().drop(100, PlinkoRisk.MEDIUM, SeededRandom.withSeed(5));

        assertThat(outcome.path()).hasSize(PlinkoBoard.ROWS);
        assertThat(outcome.path().stream().mapToInt(Integer::intValue).sum()).isEqualTo(outcome.slot());
    }

    private static double binomial(int n, int k) {
        double result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }
}
