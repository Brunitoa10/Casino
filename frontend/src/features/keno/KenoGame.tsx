import { useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { KenoOutcome, RoundResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameRound } from '@/hooks/useGameRound';
import { sleep } from '@/lib/format';
import { sound } from '@/lib/sound';
import { KENO_MAX_PICKS, KENO_NUMBERS, KENO_PAYTABLE } from './kenoPaytable';
import styles from './Keno.module.css';

const NUMBERS = Array.from({ length: KENO_NUMBERS }, (_, i) => i + 1);
const BALL_INTERVAL_MS = 110;

function randomPicks(count: number): number[] {
  const pool = [...NUMBERS];
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [pool[i], pool[j]] = [pool[j], pool[i]];
  }
  return pool.slice(0, count).sort((a, b) => a - b);
}

export function KenoGame() {
  const { player } = useSession();
  const { busy, play } = useGameRound<KenoOutcome>();
  const [stake, setStake] = useState(20);
  const [picks, setPicks] = useState<number[]>([]);
  const [drawn, setDrawn] = useState<number[]>([]);
  const [round, setRound] = useState<RoundResponse<KenoOutcome> | null>(null);

  const drawnSet = new Set(drawn);
  const hits = picks.filter((n) => drawnSet.has(n)).length;
  const table = KENO_PAYTABLE[picks.length] ?? [];

  const toggle = (n: number) => {
    if (busy) return;
    setDrawn([]);
    setRound(null);
    setPicks((current) => {
      if (current.includes(n)) return current.filter((x) => x !== n);
      if (current.length >= KENO_MAX_PICKS) return current;
      sound.chip();
      return [...current, n].sort((a, b) => a - b);
    });
  };

  const quickPick = () => {
    sound.chip();
    setDrawn([]);
    setRound(null);
    setPicks(randomPicks(Math.max(picks.length, 5)));
  };

  const start = async () => {
    setDrawn([]);
    setRound(null);
    const chosen = picks;
    const result = await play(stake, () => casinoApi.keno(stake, chosen), async (r) => {
      for (const n of r.outcome.drawn) {
        await sleep(BALL_INTERVAL_MS);
        setDrawn((d) => [...d, n]);
        if (chosen.includes(n)) sound.gem();
        else sound.tick();
      }
      await sleep(300);
    });
    if (!result) return;
    setRound(result);
    if (result.payout > result.stake) sound.win(result.outcome.multiplier >= 20 ? 3 : result.outcome.multiplier >= 5 ? 2 : 1);
  };

  return (
    <GameFrame
      eyebrow="Máquina II"
      title="Keno"
      description="Elegí de 1 a 10 números del 1 al 80. Se sortean 20: cuantos más aciertos, mayor el multiplicador."
      stage={
        <div className={styles.layout}>
          <div className={styles.board}>
            {NUMBERS.map((n) => {
              const picked = picks.includes(n);
              const out = drawnSet.has(n);
              const cls = [styles.cell, picked && styles.picked, out && styles.drawn, picked && out && styles.hit]
                .filter(Boolean).join(' ');
              return (
                <button key={n} type="button" className={cls} onClick={() => toggle(n)} disabled={busy}>
                  {n}
                </button>
              );
            })}
          </div>

          <aside className={styles.side}>
            <div className={styles.counter}>
              <span className="eyebrow">Aciertos</span>
              <strong>{hits}<small>/{picks.length || '–'}</small></strong>
              <span className={styles.balls}>{drawn.length}/20 bolillas</span>
            </div>
            <div className={styles.paytable}>
              {picks.length === 0 ? (
                <p className={styles.hint}>Elegí tus números para ver la tabla de pagos.</p>
              ) : (
                table.map((multiplier, h) =>
                  multiplier > 0 ? (
                    <div key={h} className={`${styles.payRow} ${drawn.length === 20 && h === hits ? styles.payHit : ''}`}>
                      <span>{h} aciertos</span>
                      <strong>×{multiplier.toLocaleString('es-AR')}</strong>
                    </div>
                  ) : null,
                )
              )}
            </div>
          </aside>

          {round && round.payout > round.stake && <WinBanner key={round.roundId} amount={round.payout} stake={round.stake} />}
        </div>
      }
      controls={
        <>
          <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
          <div className={styles.actions}>
            <Button variant="subtle" onClick={() => { setPicks([]); setDrawn([]); setRound(null); }} disabled={busy || picks.length === 0}>
              Limpiar
            </Button>
            <Button variant="ghost" onClick={quickPick} disabled={busy}>Al azar</Button>
            <Button size="lg" onClick={() => void start()} loading={busy} disabled={picks.length === 0}>
              {busy ? 'Sorteando' : 'Jugar'}
            </Button>
          </div>
        </>
      }
    />
  );
}
