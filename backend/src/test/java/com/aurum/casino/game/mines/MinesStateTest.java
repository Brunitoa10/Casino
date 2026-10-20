package com.aurum.casino.game.mines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;
import com.aurum.casino.shared.error.DomainException;

class MinesStateTest {

    @Test
    void hittingAMineLosesAndRevealsTheBoard() {
        var state = MinesState.withMines(100, List.of(3, 7, 12));
        state.reveal(0);
        state.reveal(7);

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isZero();
        assertThat(state.view().minePositions()).hasSize(3);
    }

    @Test
    void minesAreHiddenWhileTheRoundIsOpen() {
        var state = MinesState.withMines(100, List.of(3));
        state.reveal(0);

        assertThat(state.view().minePositions()).hasSize(0);
    }

    @Test
    void cashoutPaysTheCurrentMultiplier() {
        var state = MinesState.withMines(100, List.of(3, 7, 12));
        state.reveal(0);
        state.reveal(1);
        state.cashout();

        assertThat(state.payout()).isEqualTo((long) Math.floor(100 * MinesState.multiplier(3, 2)));
        assertThat(state.payout()).isGreaterThan(100L);
    }

    @Test
    void cannotCashoutBeforeRevealing() {
        var state = MinesState.withMines(100, List.of(3));
        assertThatThrownBy(state::cashout).isInstanceOf(DomainException.class);
    }

    @Test
    void cashingOutAfterFirstTileHasOnePercentEdge() {
        var random = SeededRandom.withSeed(9);
        long returned = 0;
        int rounds = 50_000;
        for (int i = 0; i < rounds; i++) {
            var state = MinesState.start(10_000, 5, random);
            state.reveal(random.nextInt(MinesState.TILES));
            if (!state.isFinished()) {
                state.cashout();
            }
            returned += state.payout();
        }
        assertThat((double) returned / (10_000L * rounds)).isBetween(0.96, 1.01);
    }
}
