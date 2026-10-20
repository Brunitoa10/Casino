package com.aurum.casino.game.plinko;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.core.RandomSource;

/** Tablero de 12 filas: la bola rebota a izquierda o derecha con igual probabilidad. */
@Component
public class PlinkoBoard {

    public static final int ROWS = 12;

    public PlinkoOutcome drop(long stake, PlinkoRisk risk, RandomSource random) {
        List<Integer> path = new ArrayList<>(ROWS);
        int slot = 0;
        for (int row = 0; row < ROWS; row++) {
            int bounce = random.nextInt(2);
            path.add(bounce);
            slot += bounce;
        }
        int cents = risk.multiplierCents(slot);
        return new PlinkoOutcome(risk, List.copyOf(path), slot, cents / 100.0, stake * cents / 100);
    }
}
