package com.aurum.casino.game.baccarat;

import com.aurum.casino.shared.error.DomainException;

/** Fichas sobre las tres casillas del paño. Se autovalida. */
public record BaccaratBets(long player, long banker, long tie) {

    public BaccaratBets {
        if (player < 0 || banker < 0 || tie < 0) {
            throw DomainException.invalidBet("Las apuestas no pueden ser negativas");
        }
        if (player + banker + tie == 0) {
            throw DomainException.invalidBet("Colocá al menos una ficha");
        }
    }

    public long total() {
        return player + banker + tie;
    }
}
