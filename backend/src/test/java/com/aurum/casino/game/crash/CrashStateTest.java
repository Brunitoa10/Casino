package com.aurum.casino.game.crash;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;

class CrashStateTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private static long msFor(double multiplier) {
        return (long) Math.ceil(Math.log(multiplier) / CrashCurve.GROWTH);
    }

    @Test
    void cashoutBeforeCrashPaysCurrentMultiplier() {
        var state = CrashState.withCrashPoint(100, null, 500, T0);
        state.cashout(T0.plusMillis(msFor(2.0)));

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isEqualTo(200);
    }

    @Test
    void cashoutAfterCrashLoses() {
        var state = CrashState.withCrashPoint(100, null, 150, T0);
        state.cashout(T0.plusMillis(msFor(2.0)));

        assertThat(state.payout()).isZero();
        assertThat(state.view().busted()).isTrue();
        assertThat(state.view().crashPoint()).isEqualTo(1.5);
    }

    @Test
    void autoCashoutWinsEvenIfTheClientIsLate() {
        var state = CrashState.withCrashPoint(100, 1.8, 300, T0);
        state.tick(T0.plusMillis(msFor(5.0)));

        assertThat(state.payout()).isEqualTo(180);
    }

    @Test
    void crashPointIsHiddenWhileFlying() {
        var state = CrashState.withCrashPoint(100, null, 900, T0);
        state.tick(T0.plusMillis(1000));

        assertThat(state.isFinished()).isFalse();
        assertThat(state.view().crashPoint()).isNull();
    }

    @Test
    void distributionMatchesNinetyNineOverM() {
        var random = SeededRandom.withSeed(21);
        int rounds = 200_000;
        int reachedTwo = 0;
        for (int i = 0; i < rounds; i++) {
            if (CrashState.drawCrashCents(random) >= 200) {
                reachedTwo++;
            }
        }
        assertThat((double) reachedTwo / rounds).isBetween(0.48, 0.51);
    }
}
