package com.aurum.casino.game.cards;

public enum Suit {
    SPADES('S'),
    HEARTS('H'),
    DIAMONDS('D'),
    CLUBS('C');

    private final char code;

    Suit(char code) {
        this.code = code;
    }

    public char code() {
        return code;
    }

    public static Suit fromCode(char code) {
        for (Suit suit : values()) {
            if (suit.code == code) {
                return suit;
            }
        }
        throw new IllegalArgumentException("Palo inválido: " + code);
    }
}
