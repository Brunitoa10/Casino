package com.aurum.casino.game.plinko;

import java.util.List;

import com.aurum.casino.game.core.GameOutcome;

/** {@code path}: 0 = rebote a la izquierda, 1 = a la derecha, en cada fila. */
public record PlinkoOutcome(PlinkoRisk risk, List<Integer> path, int slot, double multiplier, long payout)
        implements GameOutcome {

    @Override
    public String summary() {
        return "Riesgo " + risk.name().toLowerCase() + " · ×" + multiplier;
    }
}
