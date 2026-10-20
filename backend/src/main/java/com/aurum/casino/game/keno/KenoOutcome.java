package com.aurum.casino.game.keno;

import java.util.List;

import com.aurum.casino.game.core.GameOutcome;

public record KenoOutcome(List<Integer> picks, List<Integer> drawn, List<Integer> hits,
                          double multiplier, long payout) implements GameOutcome {

    @Override
    public String summary() {
        return hits.size() + "/" + picks.size() + " aciertos · ×" + multiplier;
    }
}
