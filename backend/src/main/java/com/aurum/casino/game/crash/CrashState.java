package com.aurum.casino.game.crash;

import java.time.Duration;
import java.time.Instant;

import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.game.session.SessionState;
import com.aurum.casino.shared.error.DomainException;

/**
 * Crash de un jugador: el multiplicador sube con el tiempo del servidor hasta un punto
 * secreto sorteado al inicio. Retirarse antes paga apuesta × multiplicador.
 * Distribución: P(crash ≥ m) = 0,99 / m (1 % de ventaja de la casa).
 */
public class CrashState implements SessionState<CrashView> {

    public static final double MIN_AUTO = 1.01;
    public static final double MAX_AUTO = 1000;
    private static final long MAX_CRASH_CENTS = 100_000;   // tope ×1000
    private static final int PRECISION = 1_000_000;

    private long stake;
    private long crashCents;
    private Long autoCashoutCents;
    private Instant startedAt;
    private long elapsedMs;
    private boolean finished;
    private boolean busted;
    private Long cashedOutCents;

    private CrashState() {
        // deserialización
    }

    public static CrashState start(long stake, Double autoCashout, RandomSource random, Instant now) {
        return withCrashPoint(stake, autoCashout, drawCrashCents(random), now);
    }

    static CrashState withCrashPoint(long stake, Double autoCashout, long crashCents, Instant now) {
        if (autoCashout != null && (autoCashout < MIN_AUTO || autoCashout > MAX_AUTO)) {
            throw DomainException.invalidBet("El retiro automático va de ×" + MIN_AUTO + " a ×" + (int) MAX_AUTO);
        }
        CrashState state = new CrashState();
        state.stake = stake;
        state.crashCents = crashCents;
        state.autoCashoutCents = autoCashout == null ? null : Math.round(autoCashout * 100);
        state.startedAt = now;
        state.evaluate(now);
        return state;
    }

    /** Muestra uniforme u ∈ [0,1) → crash = 0,99 / (1 − u), con mínimo ×1,00. */
    static long drawCrashCents(RandomSource random) {
        long u = random.nextInt(PRECISION);
        long cents = 99L * PRECISION / (PRECISION - u);
        return Math.max(100, Math.min(MAX_CRASH_CENTS, cents));
    }

    /** Actualiza el vuelo con el reloj del servidor (consulta periódica del cliente). */
    public void tick(Instant now) {
        if (!finished) {
            evaluate(now);
        }
    }

    public void cashout(Instant now) {
        if (finished) {
            return;   // ya explotó o se retiró: se devuelve el estado final
        }
        evaluate(now);
        if (!finished) {
            settle(CrashCurve.centsAt(elapsedMs));
        }
    }

    private void evaluate(Instant now) {
        elapsedMs = Duration.between(startedAt, now).toMillis();
        long current = CrashCurve.centsAt(elapsedMs);
        boolean autoReached = autoCashoutCents != null && autoCashoutCents <= crashCents
                && current >= autoCashoutCents;
        if (autoReached) {
            settle(autoCashoutCents);
        } else if (current >= crashCents) {
            busted = true;
            finished = true;
        }
    }

    private void settle(long cents) {
        cashedOutCents = cents;
        finished = true;
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
        return cashedOutCents == null ? 0L : stake * cashedOutCents / 100;
    }

    @Override
    public String summary() {
        return busted
                ? "Explotó en ×" + CrashCurve.toMultiplier(crashCents)
                : "Retiro en ×" + CrashCurve.toMultiplier(cashedOutCents == null ? 100 : cashedOutCents);
    }

    @Override
    public CrashView view() {
        long current = finished && cashedOutCents != null ? cashedOutCents
                : Math.min(CrashCurve.centsAt(elapsedMs), crashCents);
        return new CrashView(elapsedMs, CrashCurve.toMultiplier(current),
                autoCashoutCents == null ? null : CrashCurve.toMultiplier(autoCashoutCents),
                finished, busted,
                cashedOutCents == null ? null : CrashCurve.toMultiplier(cashedOutCents),
                finished ? CrashCurve.toMultiplier(crashCents) : null,
                payout());
    }
}
