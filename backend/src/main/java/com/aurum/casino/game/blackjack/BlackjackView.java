package com.aurum.casino.game.blackjack;

import java.util.List;

/**
 * Vista pública de la mesa. Mientras la mano sigue abierta, la carta oculta
 * del crupier se envía como "??" y su total solo cuenta la carta visible.
 */
public record BlackjackView(
        List<HandView> hands,
        int activeHand,
        List<String> dealerCards,
        int dealerTotal,
        boolean finished,
        boolean canHit,
        boolean canStand,
        boolean canDouble,
        boolean canSplit) {

    public record HandView(List<String> cards, int total, boolean soft, long stake, boolean doubled,
                           HandOutcome outcome, long payout) {
    }
}
