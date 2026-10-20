package com.aurum.casino.game.mines;

import java.util.ArrayList;
import java.util.List;

import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.game.session.SessionState;
import com.aurum.casino.shared.error.DomainException;

/**
 * Buscaminas de apuestas en un tablero 5×5. Cada casilla segura multiplica el premio;
 * el jugador puede retirarse en cualquier momento. Multiplicador justo × 0,99 (1 % de ventaja).
 */
public class MinesState implements SessionState<MinesView> {

    public static final int TILES = 25;
    public static final int MIN_MINES = 1;
    public static final int MAX_MINES = 24;
    private static final double RTP = 0.99;

    private long stake;
    private int mines;
    private List<Integer> minePositions = new ArrayList<>();
    private List<Integer> revealed = new ArrayList<>();
    private boolean finished;
    private boolean busted;
    private Integer hitMine;
    private long payout;

    private MinesState() {
        // deserialización
    }

    public static MinesState start(long stake, int mines, RandomSource random) {
        if (mines < MIN_MINES || mines > MAX_MINES) {
            throw DomainException.invalidBet("Elegí entre " + MIN_MINES + " y " + MAX_MINES + " minas");
        }
        List<Integer> tiles = new ArrayList<>(TILES);
        for (int i = 0; i < TILES; i++) {
            tiles.add(i);
        }
        // Fisher-Yates parcial: solo se necesitan las primeras {@code mines} posiciones
        for (int i = 0; i < mines; i++) {
            int j = i + random.nextInt(TILES - i);
            int tmp = tiles.get(i);
            tiles.set(i, tiles.get(j));
            tiles.set(j, tmp);
        }
        return withMines(stake, tiles.subList(0, mines));
    }

    static MinesState withMines(long stake, List<Integer> positions) {
        MinesState state = new MinesState();
        state.stake = stake;
        state.mines = positions.size();
        state.minePositions = new ArrayList<>(positions);
        return state;
    }

    /** Multiplicador tras {@code safe} casillas seguras: inverso de la probabilidad de sobrevivir. */
    public static double multiplier(int mines, int safe) {
        double value = RTP;
        for (int i = 0; i < safe; i++) {
            value *= (double) (TILES - i) / (TILES - mines - i);
        }
        return safe == 0 ? 1.0 : Math.floor(value * 100) / 100;
    }

    public void reveal(int tile) {
        requireOpen();
        if (tile < 0 || tile >= TILES) {
            throw DomainException.invalidAction("Casilla inválida");
        }
        if (revealed.contains(tile)) {
            throw DomainException.invalidAction("Esa casilla ya está descubierta");
        }
        if (minePositions.contains(tile)) {
            hitMine = tile;
            busted = true;
            finished = true;
            payout = 0;
            return;
        }
        revealed.add(tile);
        if (revealed.size() == TILES - mines) {
            settle();
        }
    }

    public void cashout() {
        requireOpen();
        if (revealed.isEmpty()) {
            throw DomainException.invalidAction("Descubrí al menos una casilla antes de retirarte");
        }
        settle();
    }

    private void settle() {
        payout = (long) Math.floor(stake * multiplier(mines, revealed.size()));
        finished = true;
    }

    private void requireOpen() {
        if (finished) {
            throw DomainException.invalidAction("La ronda ya terminó");
        }
    }

    @Override
    public long totalStake() {
        return stake;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public long payout() {
        return payout;
    }

    @Override
    public String summary() {
        String result = busted ? "explotó" : "×" + multiplier(mines, revealed.size());
        return mines + " minas · " + revealed.size() + " gemas · " + result;
    }

    @Override
    public MinesView view() {
        int safe = revealed.size();
        boolean canContinue = !finished && safe < TILES - mines;
        return new MinesView(mines, List.copyOf(revealed), multiplier(mines, safe),
                canContinue ? multiplier(mines, safe + 1) : multiplier(mines, safe),
                finished, busted, hitMine, finished ? List.copyOf(minePositions) : List.of(), payout);
    }
}
