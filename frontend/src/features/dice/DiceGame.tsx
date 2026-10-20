import { useEffect, useRef, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { DiceDirection, DiceOutcome, RoundResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameRound } from '@/hooks/useGameRound';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import styles from './Dice.module.css';

const ROLL_MS = 900;
const MIN_TARGET = 2;
const MAX_TARGET = 98;
const RTP = 99;

export function DiceGame() {
  const { player } = useSession();
  const { busy, play } = useGameRound<DiceOutcome>();
  const [stake, setStake] = useState(50);
  const [target, setTarget] = useState(50);
  const [direction, setDirection] = useState<DiceDirection>('UNDER');
  const [display, setDisplay] = useState(50);
  const [result, setResult] = useState<RoundResponse<DiceOutcome> | null>(null);
  const [recent, setRecent] = useState<DiceOutcome[]>([]);
  const frame = useRef(0);

  useEffect(() => () => cancelAnimationFrame(frame.current), []);

  const chance = direction === 'UNDER' ? target : 100 - target;
  const multiplier = Math.floor((RTP * 10000) / chance) / 10000;
  const potential = Math.floor((stake * RTP) / chance);

  /** Números que giran rápido y desaceleran hasta el resultado real. */
  const animateTo = (final: number) =>
    new Promise<void>((resolve) => {
      const start = performance.now();
      let lastTick = 0;
      const step = (now: number) => {
        const t = Math.min(1, (now - start) / ROLL_MS);
        if (t < 1) {
          if (now - lastTick > 40 + t * 120) {
            setDisplay(Math.random() * 100);
            sound.tick();
            lastTick = now;
          }
          frame.current = requestAnimationFrame(step);
        } else {
          setDisplay(final);
          resolve();
        }
      };
      frame.current = requestAnimationFrame(step);
    });

  const roll = async () => {
    setResult(null);
    const round = await play(stake, () => casinoApi.rollDice(stake, target, direction), (r) =>
      animateTo(r.outcome.roll / 100),
    );
    if (!round) return;
    setResult(round);
    setRecent((list) => [round.outcome, ...list].slice(0, 10));
    if (round.outcome.won) sound.win(round.outcome.multiplier >= 10 ? 3 : round.outcome.multiplier >= 3 ? 2 : 1);
    else sound.lose();
  };

  const outcome = result?.outcome;
  const markerTone = outcome ? (outcome.won ? styles.markerWin : styles.markerLose) : '';
  const winZone =
    direction === 'UNDER'
      ? { left: '0%', width: `${target}%` }
      : { left: `${target}%`, width: `${100 - target}%` };

  return (
    <GameFrame
      eyebrow="Mesa III"
      title="Dados de precisión"
      description="Elegí un objetivo y apostá por debajo o por encima. Menor probabilidad, mayor multiplicador."
      stage={
        <div className={styles.stage}>
          <div className={`${styles.readout} ${outcome ? (outcome.won ? styles.win : styles.lose) : ''}`}>
            {display.toFixed(2)}
          </div>
          <div className={styles.track}>
            <div className={styles.winZone} style={winZone} />
            <div
              className={`${styles.marker} ${markerTone}`}
              style={{ left: `${Math.min(99.99, display)}%`, transition: busy ? 'left 80ms linear' : 'left 300ms var(--ease-out)' }}
            />
            <input
              className={styles.range}
              type="range"
              min={MIN_TARGET}
              max={MAX_TARGET}
              value={target}
              onChange={(e) => {
                setTarget(Number(e.target.value));
                setResult(null);
              }}
              disabled={busy}
              aria-label="Objetivo"
            />
          </div>
          <div className={styles.scale}>
            {[0, 25, 50, 75, 100].map((n) => <span key={n}>{n}</span>)}
          </div>

          <div className={styles.metrics}>
            <Metric label="Objetivo" value={`${direction === 'UNDER' ? '<' : '>'} ${target}`} />
            <Metric label="Probabilidad" value={`${chance}%`} />
            <Metric label="Multiplicador" value={`×${multiplier.toFixed(2)}`} gold />
            <Metric label="Premio posible" value={formatCredits(potential)} />
          </div>

          <div className={styles.recent}>
            {recent.map((r, i) => (
              <span key={i} className={r.won ? styles.chipWin : styles.chipLose}>{(r.roll / 100).toFixed(2)}</span>
            ))}
          </div>
          {result && result.payout > 0 && <WinBanner key={result.roundId} amount={result.payout} stake={result.stake} />}
        </div>
      }
      controls={
        <>
          <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
          <div className={styles.actions}>
            <div className={styles.toggle} role="radiogroup" aria-label="Dirección">
              {(['UNDER', 'OVER'] as const).map((d) => (
                <button
                  key={d}
                  type="button"
                  role="radio"
                  aria-checked={direction === d}
                  className={direction === d ? styles.toggleOn : ''}
                  onClick={() => {
                    setDirection(d);
                    setResult(null);
                    sound.chip();
                  }}
                  disabled={busy}
                >
                  {d === 'UNDER' ? 'Por debajo' : 'Por encima'}
                </button>
              ))}
            </div>
            <Button size="lg" onClick={() => void roll()} disabled={busy} loading={busy}>
              {busy ? 'Rodando' : 'Lanzar'}
            </Button>
          </div>
        </>
      }
    />
  );
}

function Metric({ label, value, gold }: { label: string; value: string; gold?: boolean }) {
  return (
    <div className={styles.metric}>
      <small>{label}</small>
      <strong className={gold ? 'gold-text' : undefined}>{value}</strong>
    </div>
  );
}
