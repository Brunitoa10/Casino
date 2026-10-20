import { useMemo, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { PlinkoOutcome, PlinkoRisk, RoundResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameRound } from '@/hooks/useGameRound';
import { sound } from '@/lib/sound';
import { PlinkoBoardView, ballPoint, slotY } from './PlinkoBoard';
import { PLINKO_ROWS, RISK_LABEL, slotMultipliers } from './plinkoConfig';
import styles from './Plinko.module.css';

const ROW_MS = 120;
const RISKS: PlinkoRisk[] = ['LOW', 'MEDIUM', 'HIGH'];

/** Anima la bola fila por fila: avance horizontal lineal y caída acelerada (gravedad). */
function animatePath(path: number[], onFrame: (p: { x: number; y: number }, row: number) => void) {
  const waypoints = [{ x: ballPoint(0, 0).x, y: 4 }];
  let rights = 0;
  path.forEach((bounce, row) => {
    rights += bounce;
    waypoints.push(ballPoint(row + 1, rights));
  });
  waypoints[waypoints.length - 1] = { x: waypoints[waypoints.length - 1].x, y: slotY() + 10 };

  return new Promise<void>((resolve) => {
    const start = performance.now();
    let lastRow = -1;
    const step = (now: number) => {
      const t = (now - start) / ROW_MS;
      const segment = Math.min(Math.floor(t), waypoints.length - 2);
      const u = Math.min(1, t - segment);
      const a = waypoints[segment];
      const b = waypoints[segment + 1];
      const hop = Math.sin(u * Math.PI) * 10;          // pequeño salto tras cada clavo
      onFrame({ x: a.x + (b.x - a.x) * u, y: a.y + (b.y - a.y) * u * u - hop }, segment);
      if (segment !== lastRow) {
        lastRow = segment;
        if (segment > 0) sound.tick();
      }
      if (t < waypoints.length - 1) requestAnimationFrame(step);
      else resolve();
    };
    requestAnimationFrame(step);
  });
}

export function PlinkoGame() {
  const { player } = useSession();
  const { busy, play } = useGameRound<PlinkoOutcome>();
  const [stake, setStake] = useState(20);
  const [risk, setRisk] = useState<PlinkoRisk>('MEDIUM');
  const [ball, setBall] = useState<{ x: number; y: number } | null>(null);
  const [litRow, setLitRow] = useState(-1);
  const [round, setRound] = useState<RoundResponse<PlinkoOutcome> | null>(null);
  const [recent, setRecent] = useState<number[]>([]);
  const multipliers = useMemo(() => slotMultipliers(risk), [risk]);

  const drop = async () => {
    setRound(null);
    sound.chip();
    const result = await play(stake, () => casinoApi.plinko(stake, risk), async (r) => {
      await animatePath(r.outcome.path, (point, row) => {
        setBall(point);
        setLitRow(row - 1);
      });
      setLitRow(-1);
    });
    if (!result) return setBall(null);
    setRound(result);
    setRecent((list) => [result.outcome.multiplier, ...list].slice(0, 12));
    if (result.outcome.multiplier >= 2) sound.win(result.outcome.multiplier >= 20 ? 3 : result.outcome.multiplier >= 5 ? 2 : 1);
    else if (result.outcome.multiplier < 1) sound.lose();
    setTimeout(() => setBall(null), 900);
  };

  return (
    <GameFrame
      eyebrow="Instantáneo III"
      title="Plinko"
      description="Soltá la bola: rebota en 12 filas de clavos y cae en un multiplicador. Más riesgo, extremos más altos."
      stage={
        <div className={styles.stage}>
          <PlinkoBoardView multipliers={multipliers} ball={ball} landedSlot={busy ? null : round?.outcome.slot ?? null} litRow={litRow} />
          <aside className={styles.recent}>
            {recent.map((m, i) => (
              <span key={i} className={m >= 2 ? styles.recentHot : m >= 1 ? styles.recentEven : styles.recentCold}>×{m}</span>
            ))}
          </aside>
          {round && round.payout > round.stake * 2 && <WinBanner key={round.roundId} amount={round.payout} stake={round.stake} />}
        </div>
      }
      controls={
        <>
          <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
          <div className={styles.actions}>
            <div className={styles.risk} role="radiogroup" aria-label="Riesgo">
              <span className="eyebrow">Riesgo</span>
              <div>
                {RISKS.map((r) => (
                  <button key={r} type="button" role="radio" aria-checked={risk === r}
                    className={risk === r ? styles.riskOn : ''} onClick={() => setRisk(r)} disabled={busy}>
                    {RISK_LABEL[r]}
                  </button>
                ))}
              </div>
            </div>
            <Button size="lg" onClick={() => void drop()} loading={busy} disabled={!player || player.balance < stake}>
              Soltar
            </Button>
          </div>
        </>
      }
      aside={<p className={styles.note}>{PLINKO_ROWS} filas · la casilla central es la más probable</p>}
    />
  );
}
