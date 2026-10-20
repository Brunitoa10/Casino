package com.aurum.casino.game.roulette;

import com.aurum.casino.shared.error.DomainException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Ficha colocada sobre el paño. Se autovalida al construirse. */
public record RouletteBet(@NotNull RouletteBetType type, Integer value, @Positive long amount) {

    public RouletteBet {
        if (type == null) {
            throw DomainException.invalidBet("Tipo de apuesta requerido");
        }
        if (amount <= 0) {
            throw DomainException.invalidBet("Cada ficha debe ser mayor a cero");
        }
        type.validate(value);
    }
}
