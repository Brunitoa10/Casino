package com.aurum.casino.game.baccarat;

import java.util.List;

import com.aurum.casino.game.core.GameOutcome;

public record BaccaratOutcome(List<String> playerCards, List<String> bankerCards, int playerTotal,
                              int bankerTotal, BaccaratSide winner, long payout) implements GameOutcome {

    @Override
    public String summary() {
        String result = switch (winner) {
            case PLAYER -> "Gana Jugador";
            case BANKER -> "Gana Banca";
            case TIE -> "Empate";
        };
        return result + " " + playerTotal + "–" + bankerTotal;
    }
}
