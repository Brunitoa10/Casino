package com.aurum.casino.game.baccarat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;
import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Cards;

/** Orden de reparto: jugador, banca, jugador, banca, tercera del jugador, tercera de la banca. */
class BaccaratTableTest {

    private final BaccaratTable table = new BaccaratTable();

    @Test
    void naturalStopsTheDeal() {
        var coup = BaccaratCoup.play(Cards.shoe("4S", "2H", "5D", "3C", "9S"));

        assertThat(coup.playerTotal()).isEqualTo(9);
        assertThat(coup.playerCards()).hasSize(2);
        assertThat(coup.bankerCards()).hasSize(2);
        assertThat(coup.winner()).isEqualTo(BaccaratSide.PLAYER);
    }

    @Test
    void bankerDrawingRules() {
        assertThat(BaccaratCoup.bankerDraws(3, Card.fromCode("8S"))).isFalse();
        assertThat(BaccaratCoup.bankerDraws(3, Card.fromCode("7S"))).isTrue();
        assertThat(BaccaratCoup.bankerDraws(6, Card.fromCode("6S"))).isTrue();
        assertThat(BaccaratCoup.bankerDraws(6, Card.fromCode("5S"))).isFalse();
        assertThat(BaccaratCoup.bankerDraws(5, null)).isTrue();
        assertThat(BaccaratCoup.bankerDraws(6, null)).isFalse();
    }

    @Test
    void bankerWinPaysMinusCommissionAndTiePushesSideBets() {
        var bankerWins = BaccaratCoup.play(Cards.shoe("2S", "4H", "TD", "4C", "KS"));   // J 2+0 → +0 = 2, B 8
        var outcome = table.settle(new BaccaratBets(0, 100, 0), bankerWins);
        assertThat(outcome.winner()).isEqualTo(BaccaratSide.BANKER);
        assertThat(outcome.payout()).isEqualTo(195);

        var tie = BaccaratCoup.play(Cards.shoe("4S", "4H", "3D", "3C"));
        var tieOutcome = table.settle(new BaccaratBets(50, 30, 10), tie);
        assertThat(tieOutcome.winner()).isEqualTo(BaccaratSide.TIE);
        assertThat(tieOutcome.payout()).isEqualTo(90 + 50 + 30);
    }

    @Test
    void houseEdgeOnBankerIsAboutOnePercent() {
        var random = SeededRandom.withSeed(3);
        long returned = 0;
        int rounds = 60_000;
        for (int i = 0; i < rounds; i++) {
            returned += table.play(new BaccaratBets(0, 100, 0), random).payout();
        }
        assertThat((double) returned / (100L * rounds)).isBetween(0.97, 1.0);
    }
}
