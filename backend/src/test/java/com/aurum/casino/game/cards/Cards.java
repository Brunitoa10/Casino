package com.aurum.casino.game.cards;

import java.util.Arrays;
import java.util.List;

/** Atajos para tests: {@code Cards.of("AS", "KH")}. */
public final class Cards {

    private Cards() {
    }

    public static List<Card> of(String... codes) {
        return Arrays.stream(codes).map(Card::fromCode).toList();
    }

    /** Sabot que reparte exactamente estas cartas en este orden. */
    public static Shoe shoe(String... codes) {
        return Shoe.stacked(of(codes));
    }
}
