package com.aurum.casino.game.cards;

/** Valor de carta. {@code order} sirve para póker (2..14, el As alto). */
public enum Rank {
    TWO(2, '2'),
    THREE(3, '3'),
    FOUR(4, '4'),
    FIVE(5, '5'),
    SIX(6, '6'),
    SEVEN(7, '7'),
    EIGHT(8, '8'),
    NINE(9, '9'),
    TEN(10, 'T'),
    JACK(11, 'J'),
    QUEEN(12, 'Q'),
    KING(13, 'K'),
    ACE(14, 'A');

    private final int order;
    private final char code;

    Rank(int order, char code) {
        this.order = order;
        this.code = code;
    }

    public int order() {
        return order;
    }

    public char code() {
        return code;
    }

    /** Blackjack: figuras valen 10, el As 11 (el ajuste a 1 lo hace la mano). */
    public int blackjackValue() {
        if (this == ACE) {
            return 11;
        }
        return Math.min(order, 10);
    }

    /** Baccarat: el As vale 1, diez y figuras valen 0. */
    public int baccaratValue() {
        if (this == ACE) {
            return 1;
        }
        return order >= 10 ? 0 : order;
    }

    public static Rank fromCode(char code) {
        for (Rank rank : values()) {
            if (rank.code == code) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Valor inválido: " + code);
    }
}
