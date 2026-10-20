package com.aurum.casino.game.core;

/**
 * Strategy: cada juego encapsula su lógica pura en una función de la fuente de azar.
 * No conoce saldos, persistencia ni HTTP.
 */
@FunctionalInterface
public interface GameEngine<O extends GameOutcome> {

    public O play(RandomSource random);
}
