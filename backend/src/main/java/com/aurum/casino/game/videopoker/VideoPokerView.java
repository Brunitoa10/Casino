package com.aurum.casino.game.videopoker;

import java.util.List;

/** {@code hand} es la mano actual evaluada (tras el reparto sirve de pista; tras el cambio, es el resultado). */
public record VideoPokerView(List<String> cards, List<Boolean> held, PokerHand hand, String handLabel,
                             boolean finished, long payout) {
}
