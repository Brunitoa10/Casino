package com.aurum.casino.game.roulette;

import java.util.List;

import com.aurum.casino.game.core.GameOutcome;

public record RouletteOutcome(int number, String color, List<BetResult> bets, long payout)
        implements GameOutcome {

    public record BetResult(RouletteBetType type, Integer value, long amount, boolean won, long payout) {
    }

    @Override
    public String summary() {
        long hits = bets.stream().filter(BetResult::won).count();
        return "Salió " + number + " " + color + " · " + hits + "/" + bets.size() + " aciertos";
    }
}
