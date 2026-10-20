package com.aurum.casino.game.roulette;

import java.util.List;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.shared.error.DomainException;

/** Ruleta europea de un solo cero (ventaja de la casa 2,7 %). */
@Component
public class RouletteWheel {

    public static final int MAX_BETS_PER_SPIN = 60;
    private static final int POCKETS = RouletteNumbers.MAX + 1;

    public static long totalStake(List<RouletteBet> bets) {
        if (bets == null || bets.isEmpty()) {
            throw DomainException.invalidBet("Colocá al menos una ficha");
        }
        if (bets.size() > MAX_BETS_PER_SPIN) {
            throw DomainException.invalidBet("Máximo " + MAX_BETS_PER_SPIN + " fichas por giro");
        }
        return bets.stream().mapToLong(RouletteBet::amount).sum();
    }

    public RouletteOutcome spin(List<RouletteBet> bets, RandomSource random) {
        int number = random.nextInt(POCKETS);
        List<RouletteOutcome.BetResult> results = bets.stream()
                .map(bet -> settle(bet, number))
                .toList();
        long payout = results.stream().mapToLong(RouletteOutcome.BetResult::payout).sum();
        return new RouletteOutcome(number, RouletteNumbers.colorOf(number), results, payout);
    }

    private static RouletteOutcome.BetResult settle(RouletteBet bet, int number) {
        boolean won = bet.type().wins(number, bet.value());
        long payout = won ? bet.amount() * (bet.type().payoutRatio() + 1L) : 0L;
        return new RouletteOutcome.BetResult(bet.type(), bet.value(), bet.amount(), won, payout);
    }
}
