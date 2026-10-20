package com.aurum.casino.game.slots;

import java.util.List;

import com.aurum.casino.game.core.GameOutcome;

/**
 * @param reels  símbolos visibles por rodillo, de arriba hacia abajo
 * @param wins   líneas ganadoras
 */
public record SlotOutcome(List<List<SlotSymbol>> reels, List<WinningLine> wins, long payout)
        implements GameOutcome {

    public record WinningLine(int line, SlotSymbol symbol, int multiplier, long amount) {
    }

    @Override
    public String summary() {
        if (wins.isEmpty()) {
            return "Sin líneas ganadoras";
        }
        return wins.size() + (wins.size() == 1 ? " línea" : " líneas") + " · mejor: " + wins.stream()
                .map(WinningLine::symbol)
                .max(Enum::compareTo)
                .orElseThrow();
    }
}
