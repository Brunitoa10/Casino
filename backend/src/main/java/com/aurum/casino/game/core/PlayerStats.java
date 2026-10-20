package com.aurum.casino.game.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Proyección de estadísticas (tipos wrapper para la expresión {@code select new} de JPQL). */
public record PlayerStats(Long rounds, Long wagered, Long won, Long biggestWin) {

    @JsonProperty("net")
    public long net() {
        return won - wagered;
    }
}
