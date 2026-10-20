package com.aurum.casino.game.videopoker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.aurum.casino.game.cards.Cards;

class PokerHandEvaluatorTest {

    private static PokerHand eval(String... codes) {
        return PokerHandEvaluator.evaluate(Cards.of(codes));
    }

    @Test
    void recognisesEveryHand() {
        assertThat(eval("TS", "JS", "QS", "KS", "AS")).isEqualTo(PokerHand.ROYAL_FLUSH);
        assertThat(eval("5H", "6H", "7H", "8H", "9H")).isEqualTo(PokerHand.STRAIGHT_FLUSH);
        assertThat(eval("9S", "9H", "9D", "9C", "2S")).isEqualTo(PokerHand.FOUR_OF_A_KIND);
        assertThat(eval("3S", "3H", "3D", "KC", "KS")).isEqualTo(PokerHand.FULL_HOUSE);
        assertThat(eval("2D", "7D", "9D", "JD", "KD")).isEqualTo(PokerHand.FLUSH);
        assertThat(eval("AS", "2H", "3D", "4C", "5S")).isEqualTo(PokerHand.STRAIGHT);
        assertThat(eval("TS", "JH", "QD", "KC", "AS")).isEqualTo(PokerHand.STRAIGHT);
        assertThat(eval("7S", "7H", "7D", "KC", "2S")).isEqualTo(PokerHand.THREE_OF_A_KIND);
        assertThat(eval("7S", "7H", "4D", "4C", "2S")).isEqualTo(PokerHand.TWO_PAIR);
        assertThat(eval("JS", "JH", "4D", "8C", "2S")).isEqualTo(PokerHand.JACKS_OR_BETTER);
        assertThat(eval("TS", "TH", "4D", "8C", "2S")).isEqualTo(PokerHand.NOTHING);
        assertThat(eval("QS", "KH", "AD", "2C", "3S")).isEqualTo(PokerHand.NOTHING);
    }

    @Test
    void drawReplacesOnlyUnheldCards() {
        var state = VideoPokerState.deal(10, Cards.shoe("AS", "AH", "4D", "8C", "2S", "AD", "AC", "KS"));
        state.draw(Set.of(0, 1));

        assertThat(state.view().cards()).containsExactly("AS", "AH", "AD", "AC", "KS");
        assertThat(state.view().hand()).isEqualTo(PokerHand.FOUR_OF_A_KIND);
        assertThat(state.payout()).isEqualTo(250);
    }
}
