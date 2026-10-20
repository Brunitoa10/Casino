package com.aurum.casino.game.roulette;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;
import com.aurum.casino.shared.error.DomainException;

class RouletteWheelTest {

    private final RouletteWheel wheel = new RouletteWheel();

    @Test
    void straightBetPays35To1() {
        var outcome = wheel.spin(List.of(new RouletteBet(RouletteBetType.STRAIGHT, 17, 10)),
                SeededRandom.sequence(17));

        assertThat(outcome.number()).isEqualTo(17);
        assertThat(outcome.payout()).isEqualTo(360);
    }

    @Test
    void zeroLosesAllEvenMoneyBets() {
        var bets = List.of(
                new RouletteBet(RouletteBetType.RED, null, 10),
                new RouletteBet(RouletteBetType.BLACK, null, 10),
                new RouletteBet(RouletteBetType.EVEN, null, 10),
                new RouletteBet(RouletteBetType.LOW, null, 10));

        var outcome = wheel.spin(bets, SeededRandom.sequence(0));

        assertThat(outcome.color()).isEqualTo("GREEN");
        assertThat(outcome.payout()).isZero();
    }

    @Test
    void dozenAndColumnRules() {
        assertThat(RouletteBetType.DOZEN.wins(13, 2)).isTrue();
        assertThat(RouletteBetType.DOZEN.wins(12, 2)).isFalse();
        assertThat(RouletteBetType.COLUMN.wins(34, 1)).isTrue();
        assertThat(RouletteBetType.COLUMN.wins(36, 3)).isTrue();
    }

    @Test
    void rejectsBetWithoutRequiredValue() {
        assertThatThrownBy(() -> new RouletteBet(RouletteBetType.STRAIGHT, 40, 10))
                .isInstanceOf(DomainException.class);
    }
}
