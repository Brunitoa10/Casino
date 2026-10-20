package com.aurum.casino.game.session;

import java.time.Instant;

import com.aurum.casino.game.core.RandomSource;

/** Dependencias que un motor puede necesitar para avanzar: azar y tiempo del servidor. */
public record ActionContext(RandomSource random, Instant now) {
}
