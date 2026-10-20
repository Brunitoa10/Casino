package com.aurum.casino.game.session;

import com.aurum.casino.game.core.GameType;

/** Respuesta común de los juegos con estado; {@code state} es la vista pública del juego. */
public record SessionResponse<V>(
        Long sessionId,
        GameType game,
        boolean finished,
        long stake,
        long payout,
        long balance,
        V state) {
}
