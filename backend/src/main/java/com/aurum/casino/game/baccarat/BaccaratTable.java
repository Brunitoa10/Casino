package com.aurum.casino.game.baccarat;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.cards.Card;
import com.aurum.casino.game.cards.Shoe;
import com.aurum.casino.game.core.RandomSource;

/**
 * Pagos: Jugador 1:1, Banca 0,95:1 (comisión 5 %), Empate 8:1.
 * En empate, las apuestas a Jugador y Banca se devuelven.
 */
@Component
public class BaccaratTable {

    public static final int DECKS = 8;
    private static final int TIE_PAYS = 8;
    private static final int BANKER_COMMISSION_PERCENT = 5;

    public BaccaratOutcome play(BaccaratBets bets, RandomSource random) {
        return settle(bets, BaccaratCoup.play(Shoe.shuffled(DECKS, random)));
    }

    BaccaratOutcome settle(BaccaratBets bets, BaccaratCoup coup) {
        BaccaratSide winner = coup.winner();
        long payout = switch (winner) {
            case PLAYER -> bets.player() * 2;
            case BANKER -> bets.banker() + bets.banker() * (100 - BANKER_COMMISSION_PERCENT) / 100;
            case TIE -> bets.tie() * (TIE_PAYS + 1) + bets.player() + bets.banker();
        };
        return new BaccaratOutcome(coup.playerCards().stream().map(Card::code).toList(),
                coup.bankerCards().stream().map(Card::code).toList(),
                coup.playerTotal(), coup.bankerTotal(), winner, payout);
    }
}
