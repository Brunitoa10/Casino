package com.aurum.casino.game.keno;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.shared.error.DomainException;

/** Keno clásico: 80 números, el jugador elige de 1 a 10 y se sortean 20. */
@Component
public class KenoGame {

    public static final int NUMBERS = 80;
    public static final int DRAWN = 20;
    public static final int MAX_PICKS = 10;

    public KenoOutcome play(long stake, List<Integer> picks, RandomSource random) {
        Set<Integer> chosen = validate(picks);
        List<Integer> drawn = draw(random);
        List<Integer> hits = drawn.stream().filter(chosen::contains).sorted().toList();
        int cents = KenoPaytable.multiplierCents(chosen.size(), hits.size());
        return new KenoOutcome(List.copyOf(chosen), drawn, hits, cents / 100.0, stake * cents / 100);
    }

    public static Set<Integer> validate(List<Integer> picks) {
        if (picks == null || picks.isEmpty() || picks.size() > MAX_PICKS) {
            throw DomainException.invalidBet("Elegí entre 1 y " + MAX_PICKS + " números");
        }
        Set<Integer> chosen = new LinkedHashSet<>(picks);
        if (chosen.size() != picks.size()) {
            throw DomainException.invalidBet("No se pueden repetir números");
        }
        if (chosen.stream().anyMatch(n -> n == null || n < 1 || n > NUMBERS)) {
            throw DomainException.invalidBet("Los números van del 1 al " + NUMBERS);
        }
        return chosen;
    }

    /** Sorteo sin reposición (Fisher-Yates parcial); se conserva el orden de salida para animarlo. */
    private static List<Integer> draw(RandomSource random) {
        List<Integer> pool = new ArrayList<>(NUMBERS);
        for (int n = 1; n <= NUMBERS; n++) {
            pool.add(n);
        }
        for (int i = 0; i < DRAWN; i++) {
            int j = i + random.nextInt(NUMBERS - i);
            Integer tmp = pool.get(i);
            pool.set(i, pool.get(j));
            pool.set(j, tmp);
        }
        return List.copyOf(pool.subList(0, DRAWN));
    }
}
