package com.aurum.casino.game.session;

/**
 * Estado de un juego de varias jugadas (blackjack, minas, crash...).
 * Se persiste como JSON entre peticiones y expone una vista pública
 * que oculta la información secreta (mazo, minas, punto de crash) mientras la mano sigue abierta.
 *
 * @param <V> tipo de la vista que recibe el cliente
 */
public interface SessionState<V> {

    /** Total apostado hasta ahora (crece al doblar o dividir). */
    public long totalStake();

    public boolean isFinished();

    /** Monto devuelto al jugador; solo es significativo al terminar. */
    public long payout();

    public String summary();

    public V view();
}
