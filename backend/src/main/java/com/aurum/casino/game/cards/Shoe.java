package com.aurum.casino.game.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aurum.casino.game.core.RandomSource;

/** Sabot de uno o más mazos barajados con Fisher-Yates. Se reparte desde el final. */
public final class Shoe {

    private List<Card> cards;

    private Shoe() {
        // requerido para la deserialización del estado
    }

    private Shoe(List<Card> cards) {
        this.cards = cards;
    }

    public static Shoe shuffled(int decks, RandomSource random) {
        List<Card> cards = new ArrayList<>(decks * 52);
        for (int d = 0; d < decks; d++) {
            for (Suit suit : Suit.values()) {
                for (Rank rank : Rank.values()) {
                    cards.add(new Card(rank, suit));
                }
            }
        }
        for (int i = cards.size() - 1; i > 0; i--) {
            Collections.swap(cards, i, random.nextInt(i + 1));
        }
        return new Shoe(cards);
    }

    /** Sabot con orden fijo (la última carta sale primero). Útil en tests. */
    public static Shoe stacked(List<Card> topFirst) {
        List<Card> cards = new ArrayList<>(topFirst);
        Collections.reverse(cards);
        return new Shoe(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("El sabot está vacío");
        }
        return cards.remove(cards.size() - 1);
    }

    public int remaining() {
        return cards.size();
    }
}
