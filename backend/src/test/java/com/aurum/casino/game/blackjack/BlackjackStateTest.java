package com.aurum.casino.game.blackjack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.cards.Cards;
import com.aurum.casino.shared.error.DomainException;

/** Orden de reparto: jugador, crupier, jugador, crupier (oculta), luego cartas extra. */
class BlackjackStateTest {

    @Test
    void naturalBlackjackPaysThreeToTwo() {
        var state = BlackjackState.deal(100, Cards.shoe("AS", "9H", "KD", "7C"));

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isEqualTo(250);
    }

    @Test
    void dealerBlackjackEndsHandImmediately() {
        var state = BlackjackState.deal(100, Cards.shoe("9S", "AH", "8D", "KC"));

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isZero();
    }

    @Test
    void hiddenCardIsNotRevealedWhileOpen() {
        var state = BlackjackState.deal(100, Cards.shoe("9S", "7H", "8D", "KC", "2S"));

        var view = state.view();
        assertThat(view.dealerCards()).containsExactly("7H", "??");
        assertThat(view.dealerTotal()).isEqualTo(7);
        assertThat(view.canDouble()).isTrue();
    }

    @Test
    void standOnSeventeenAndDealerDrawsToSeventeen() {
        // Jugador 9+8=17; crupier 7+K=17 → empate
        var state = BlackjackState.deal(100, Cards.shoe("9S", "7H", "8D", "KC"));
        state.apply(BlackjackAction.STAND);

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isEqualTo(100);
    }

    @Test
    void doubleDownDoublesStakeAndTakesOneCard() {
        // Jugador 6+5=11, dobla y recibe 10 = 21; crupier 6+K=16, pide 9 → se pasa
        var state = BlackjackState.deal(100, Cards.shoe("6S", "6H", "5D", "KC", "TS", "9D"));
        state.apply(BlackjackAction.DOUBLE);

        assertThat(state.totalStake()).isEqualTo(200);
        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isEqualTo(400);
    }

    @Test
    void splitCreatesTwoHandsWithTheirOwnStake() {
        // Par de 8 vs crupier 6+T; cada 8 recibe 3 y luego se planta
        var state = BlackjackState.deal(50, Cards.shoe("8S", "6H", "8D", "TC", "3S", "3D", "9H"));
        state.apply(BlackjackAction.SPLIT);
        assertThat(state.totalStake()).isEqualTo(100);

        state.apply(BlackjackAction.STAND);
        state.apply(BlackjackAction.STAND);
        // crupier 16 pide 9 → 25, se pasa: ambas manos ganan
        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isEqualTo(200);
    }

    @Test
    void bustLosesWithoutDealerDrawing() {
        var state = BlackjackState.deal(100, Cards.shoe("TS", "6H", "6D", "TC", "KS"));
        state.apply(BlackjackAction.HIT);

        assertThat(state.isFinished()).isTrue();
        assertThat(state.payout()).isZero();
        assertThat(state.view().dealerCards()).hasSize(2);
    }

    @Test
    void cannotActAfterFinish() {
        var state = BlackjackState.deal(100, Cards.shoe("AS", "9H", "KD", "7C"));
        assertThatThrownBy(() -> state.apply(BlackjackAction.HIT)).isInstanceOf(DomainException.class);
    }

    @Test
    void softHandsCountAcesFlexibly() {
        assertThat(BlackjackHand.total(Cards.of("AS", "6H"))).isEqualTo(17);
        assertThat(BlackjackHand.isSoft(Cards.of("AS", "6H"))).isTrue();
        assertThat(BlackjackHand.total(Cards.of("AS", "6H", "9D"))).isEqualTo(16);
        assertThat(BlackjackHand.total(Cards.of("AS", "AH", "9D"))).isEqualTo(21);
    }
}
