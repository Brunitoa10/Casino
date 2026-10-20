package com.aurum.casino.game.slots;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aurum.casino.game.core.RandomSource;

/**
 * Tragamonedas 3x3 con 5 líneas. La apuesta total se reparte entre las líneas;
 * cada línea con tres símbolos iguales paga {@code apuesta × multiplicador / líneas}.
 */
@Component
public class SlotMachine {

    public static final int REELS = 3;
    public static final int ROWS = 3;

    private final List<Payline> paylines;

    public SlotMachine() {
        this(Payline.STANDARD);
    }

    SlotMachine(List<Payline> paylines) {
        this.paylines = List.copyOf(paylines);
    }

    public SlotOutcome spin(long stake, RandomSource random) {
        List<List<SlotSymbol>> reels = drawReels(random);
        List<SlotOutcome.WinningLine> wins = new ArrayList<>();

        for (Payline line : paylines) {
            SlotSymbol first = symbolAt(reels, 0, line);
            boolean allMatch = true;
            for (int reel = 1; reel < REELS; reel++) {
                allMatch &= symbolAt(reels, reel, line) == first;
            }
            if (allMatch) {
                long amount = stake * first.multiplier() / paylines.size();
                wins.add(new SlotOutcome.WinningLine(line.index(), first, first.multiplier(), amount));
            }
        }

        long payout = wins.stream().mapToLong(SlotOutcome.WinningLine::amount).sum();
        return new SlotOutcome(reels, List.copyOf(wins), payout);
    }

    private static List<List<SlotSymbol>> drawReels(RandomSource random) {
        List<List<SlotSymbol>> reels = new ArrayList<>(REELS);
        for (int reel = 0; reel < REELS; reel++) {
            List<SlotSymbol> column = new ArrayList<>(ROWS);
            for (int row = 0; row < ROWS; row++) {
                column.add(SlotSymbol.draw(random));
            }
            reels.add(List.copyOf(column));
        }
        return List.copyOf(reels);
    }

    private static SlotSymbol symbolAt(List<List<SlotSymbol>> reels, int reel, Payline line) {
        return reels.get(reel).get(line.rows().get(reel));
    }
}
