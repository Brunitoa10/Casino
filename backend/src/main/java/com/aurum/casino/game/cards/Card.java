package com.aurum.casino.game.cards;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Carta inmutable. Se serializa compacta como código de 2 caracteres
 * ("AS" = As de picas, "TD" = 10 de diamantes) tanto en la BD como en la API.
 */
public record Card(Rank rank, Suit suit) {

    @JsonValue
    public String code() {
        return "" + rank.code() + suit.code();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Card fromCode(String code) {
        if (code == null || code.length() != 2) {
            throw new IllegalArgumentException("Código de carta inválido: " + code);
        }
        return new Card(Rank.fromCode(code.charAt(0)), Suit.fromCode(code.charAt(1)));
    }

    @Override
    public String toString() {
        return code();
    }
}
