import { useCallback, useEffect, useMemo, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { RoundResponse, SlotOutcome, SlotSymbol } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameRound } from '@/hooks/useGameRound';
import { sleep } from '@/lib/format';
import { sound } from '@/lib/sound';
import { Reel } from './Reel';
import { SlotGradients, SlotSymbolIcon } from './SlotSymbolIcon';
import { LINE_MULTIPLIER, PAYLINE_COUNT, PAYLINES, SPIN_BASE_MS, SPIN_STAGGER_MS, SYMBOLS, randomSymbol } from './slotsConfig';
import styles from './Slots.module.css';

const AUTO_SPINS = 10;
const initialReels = (): SlotSymbol[][] => [0, 1, 2].map(() => [randomSymbol(), randomSymbol(), randomSymbol()]);

export function SlotsGame() {
  const { player } = useSession();
  const { busy, play } = useGameRound<SlotOutcome>();
  const [stake, setStake] = useState(50);
  const [reels, setReels] = useState<SlotSymbol[][]>(initialReels);
  const [spinId, setSpinId] = useState(0);
  const [result, setResult] = useState<RoundResponse<SlotOutcome> | null>(null);
  const [autoLeft, setAutoLeft] = useState(0);

  const spin = useCallback(async () => {
    setResult(null);
    sound.spin();
    const round = await play(stake, () => casinoApi.spinSlots(stake), async (r) => {
      setReels(r.outcome.reels);
      setSpinId((id) => id + 1);
      await sleep(SPIN_BASE_MS + SPIN_STAGGER_MS * 2 + 120);
    });
    if (!round) {
      setAutoLeft(0);
      return;
    }
    setResult(round);
    if (round.payout > 0) {
      const ratio = round.payout / round.stake;
      sound.win(ratio >= 20 ? 3 : ratio >= 5 ? 2 : 1);
    }
  }, [play, stake]);

  // Giros automáticos: encadena el siguiente cuando termina el anterior
  useEffect(() => {
    if (autoLeft <= 0 || busy) return;
    const delay = result && result.payout > 0 ? 1600 : 450;
    const timer = setTimeout(() => {
      setAutoLeft((n) => n - 1);
      void spin();
    }, delay);
    return () => clearTimeout(timer);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoLeft, busy]);

  // Barra espaciadora = girar
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.code === 'Space' && !busy && e.target === document.body) {
        e.preventDefault();
        void spin();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [busy, spin]);

  /** Celdas ganadoras por rodillo (índices de fila). */
  const highlighted = useMemo(() => {
    const cells = [new Set<number>(), new Set<number>(), new Set<number>()];
    result?.outcome.wins.forEach((win) => PAYLINES[win.line].forEach((row, reel) => cells[reel].add(row)));
    return cells;
  }, [result]);

  const winningLines = result?.outcome.wins.map((w) => w.line) ?? [];

  return (
    <GameFrame
      eyebrow="Mesa I"
      title="Tragamonedas"
      description="Tres rodillos, cinco líneas de pago. Tres símbolos iguales en una línea pagan su multiplicador."
      stage={
        <div className={styles.cabinet}>
          <SlotGradients />
          <div className={styles.marquee}>
            {Array.from({ length: 14 }, (_, i) => (
              <span key={i} className={`${styles.bulb} ${busy ? styles.bulbChase : ''}`} style={{ animationDelay: `${i * 80}ms` }} />
            ))}
          </div>
          <div className={styles.window}>
            {reels.map((symbols, i) => (
              <Reel
                key={i}
                symbols={symbols}
                spinId={spinId}
                durationMs={SPIN_BASE_MS + SPIN_STAGGER_MS * i}
                highlighted={highlighted[i]}
              />
            ))}
            <svg className={styles.lines} viewBox="0 0 300 300" preserveAspectRatio="none" aria-hidden="true">
              {winningLines.map((line) => (
                <polyline
                  key={line}
                  points={PAYLINES[line].map((row, reel) => `${50 + reel * 100},${50 + row * 100}`).join(' ')}
                  className={styles.payline}
                />
              ))}
            </svg>
          </div>
          {result && result.payout > 0 && <WinBanner key={result.roundId} amount={result.payout} stake={result.stake} />}
        </div>
      }
      controls={
        <>
          <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy || autoLeft > 0} />
          <div className={styles.actions}>
            <Button
              variant="ghost"
              onClick={() => setAutoLeft((n) => (n > 0 ? 0 : AUTO_SPINS))}
              disabled={autoLeft === 0 && (!player || player.balance < stake)}
            >
              {autoLeft > 0 ? `Detener · ${autoLeft}` : `Auto ×${AUTO_SPINS}`}
            </Button>
            <Button size="lg" onClick={() => void spin()} disabled={busy || autoLeft > 0} loading={busy}>
              {busy ? 'Girando' : 'Girar'}
            </Button>
          </div>
        </>
      }
      aside={
        <div className={styles.paytable}>
          {[...SYMBOLS].reverse().map((symbol) => (
            <div key={symbol} className={styles.payItem}>
              <span className={styles.payIcon}><SlotSymbolIcon symbol={symbol} /></span>
              <span>×{(LINE_MULTIPLIER[symbol] / PAYLINE_COUNT).toLocaleString('es-AR')}</span>
            </div>
          ))}
          <span className={styles.payNote}>por línea, sobre la apuesta total · Espacio para girar</span>
        </div>
      }
    />
  );
}
