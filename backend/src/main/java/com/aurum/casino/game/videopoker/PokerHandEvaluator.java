package com.aurum.casino.game.videopoker;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Rank;

/** Clasifica una mano de 5 cartas. Función pura, sin estado. */
public final class PokerHandEvaluator {

    private static final int HAND_SIZE = 5;

    private PokerHandEvaluator() {
    }

    public static PokerHand evaluate(List<Card> cards) {
        if (cards.size() != HAND_SIZE) {
            throw new IllegalArgumentException("Una mano de póker tiene 5 cartas");
        }
        boolean flush = cards.stream().map(Card::suit).distinct().count() == 1;
        boolean straight = isStraight(cards);

        if (straight && flush) {
            boolean royal = cards.stream().anyMatch(c -> c.rank() == Rank.ACE)
                    && cards.stream().anyMatch(c -> c.rank() == Rank.KING);
            return royal ? PokerHand.ROYAL_FLUSH : PokerHand.STRAIGHT_FLUSH;
        }

        Map<Rank, Long> counts = cards.stream()
                .collect(Collectors.groupingBy(Card::rank, Collectors.counting()));
        long[] groups = counts.values().stream().mapToLong(Long::longValue)
                .sorted().toArray();
        long largest = groups[groups.length - 1];

        if (largest == 4) {
            return PokerHand.FOUR_OF_A_KIND;
        }
        if (largest == 3 && groups.length == 2) {
            return PokerHand.FULL_HOUSE;
        }
        if (flush) {
            return PokerHand.FLUSH;
        }
        if (straight) {
            return PokerHand.STRAIGHT;
        }
        if (largest == 3) {
            return PokerHand.THREE_OF_A_KIND;
        }
        long pairs = Arrays.stream(groups).filter(g -> g == 2).count();
        if (pairs == 2) {
            return PokerHand.TWO_PAIR;
        }
        if (pairs == 1) {
            boolean highPair = counts.entrySet().stream()
                    .anyMatch(e -> e.getValue() == 2 && e.getKey().order() >= Rank.JACK.order());
            return highPair ? PokerHand.JACKS_OR_BETTER : PokerHand.NOTHING;
        }
        return PokerHand.NOTHING;
    }

    private static boolean isStraight(List<Card> cards) {
        int[] orders = cards.stream().mapToInt(c -> c.rank().order()).sorted().toArray();
        boolean distinct = Arrays.stream(orders).boxed()
                .collect(Collectors.toMap(Function.identity(), o -> 1, Integer::sum)).size() == HAND_SIZE;
        if (!distinct) {
            return false;
        }
        boolean wheel = Arrays.equals(orders, new int[] {2, 3, 4, 5, Rank.ACE.order()});
        return wheel || orders[HAND_SIZE - 1] - orders[0] == HAND_SIZE - 1;
    }
}
