package com.aurum.casino.game.keno;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;
import com.aurum.casino.shared.error.DomainException;

class KenoGameTest {

    private final KenoGame keno = new KenoGame();

    @Test
    void everyPaytableRowHasAReasonableRtp() {
        for (int picks = 1; picks <= KenoGame.MAX_PICKS; picks++) {
            double rtp = 0;
            for (int hits = 0; hits <= picks; hits++) {
                rtp += probability(picks, hits) * KenoPaytable.multiplierCents(picks, hits) / 100.0;
            }
            assertThat(rtp).as("RTP con %d números", picks).isBetween(0.90, 0.97);
        }
    }

    @Test
    void drawsTwentyDistinctNumbersAndCountsHits() {
        var outcome = keno.play(10, List.of(1, 2, 3, 4, 5), SeededRandom.withSeed(11));

        assertThat(outcome.drawn()).hasSize(KenoGame.DRAWN);
        assertThat(outcome.drawn().stream().distinct().count()).isEqualTo(20L);
        assertThat(outcome.hits().stream().allMatch(outcome.picks()::contains)).isTrue();
    }

    @Test
    void rejectsInvalidPicks() {
        assertThatThrownBy(() -> KenoGame.validate(List.of(1, 1))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> KenoGame.validate(List.of(81))).isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> KenoGame.validate(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)))
                .isInstanceOf(DomainException.class);
    }

    /** Distribución hipergeométrica: P(aciertos | elegidos, 20 de 80). */
    private static double probability(int picks, int hits) {
        BigInteger ways = binomial(picks, hits).multiply(binomial(80 - picks, 20 - hits));
        return ways.doubleValue() / binomial(80, 20).doubleValue();
    }

    private static BigInteger binomial(int n, int k) {
        BigInteger result = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            result = result.multiply(BigInteger.valueOf(n - i)).divide(BigInteger.valueOf(i + 1));
        }
        return result;
    }
}
