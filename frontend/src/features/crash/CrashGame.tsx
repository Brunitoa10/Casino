import { useEffect, useRef, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { CrashView, SessionResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { useSession } from '@/context/SessionContext';
import { useGameSession } from '@/hooks/useGameSession';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import { CrashChart } from './CrashChart';
import { elapsedFor, multiplierAt } from './crashCurve';
import styles from './Crash.module.css';

const TICK_MS = 400;
const AFTERMATH_SPEED = 3;
const DRIFT_TOLERANCE_MS = 250;

type Anchor = { serverMs: number; at: number; speed: number };

export function CrashGame() {
  const { player } = useSession();
  const { session, isActive, busy, run, apply } = useGameSession(casinoApi.crash.active);
  const [stake, setStake] = useState(50);
  const [autoInput, setAutoInput] = useState('');
  const [elapsed, setElapsed] = useState(0);
  const [history, setHistory] = useState<number[]>([]);
  const anchor = useRef<Anchor | null>(null);
  const announced = useRef<number | null>(null);

  const view = session?.state;
  const busted = !!view?.busted;
  const cashed = !!session?.finished && !busted;
  const crashMs = view?.crashPoint ? elapsedFor(view.crashPoint) : null;
  // Tras retirarse, el cohete sigue (acelerado) hasta su punto de crash: suspenso sin riesgo
  const inAftermath = cashed && crashMs !== null && elapsed < crashMs;
  const animating = isActive || inAftermath;

  // Sincroniza el reloj local con el tiempo de vuelo del servidor
  useEffect(() => {
    if (!view) return;
    if (isActive) {
      const estimate = anchor.current ? anchor.current.serverMs + (performance.now() - anchor.current.at) : -Infinity;
      if (Math.abs(estimate - view.elapsedMs) > DRIFT_TOLERANCE_MS) {
        anchor.current = { serverMs: view.elapsedMs, at: performance.now(), speed: 1 };
      }
    } else if (cashed && anchor.current) {
      const now = performance.now();
      const current = anchor.current.serverMs + (now - anchor.current.at) * anchor.current.speed;
      anchor.current = { serverMs: current, at: now, speed: AFTERMATH_SPEED };
    } else if (busted && crashMs !== null) {
      setElapsed(crashMs);
    }
  }, [view, isActive, cashed, busted, crashMs]);

  // Bucle de animación
  useEffect(() => {
    if (!animating) return;
    let frame = 0;
    const loop = () => {
      const a = anchor.current;
      if (a) {
        const next = a.serverMs + (performance.now() - a.at) * a.speed;
        setElapsed(crashMs !== null ? Math.min(next, crashMs) : next);
      }
      frame = requestAnimationFrame(loop);
    };
    frame = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(frame);
  }, [animating, crashMs]);

  // Consulta periódica: el servidor decide si el cohete explotó
  useEffect(() => {
    if (!isActive) return;
    const id = setInterval(async () => {
      try {
        const next = await casinoApi.crash.tick();
        apply(next);
      } catch {
        /* se reintenta en el próximo ciclo */
      }
    }, TICK_MS);
    return () => clearInterval(id);
  }, [isActive, apply]);

  // Efectos al terminar (una sola vez por sesión)
  useEffect(() => {
    if (!session?.finished || announced.current === session.sessionId) return;
    announced.current = session.sessionId;
    if (session.state.crashPoint) setHistory((h) => [session.state.crashPoint!, ...h].slice(0, 16));
    if (session.state.busted) sound.boom();
    else sound.win(session.payout >= session.stake * 10 ? 3 : session.payout >= session.stake * 3 ? 2 : 1);
  }, [session]);

  const start = async () => {
    const auto = Number(autoInput.replace(',', '.'));
    anchor.current = null;
    setElapsed(0);
    sound.spin();
    const result = await run(() => casinoApi.crash.start(stake, autoInput && auto > 1 ? auto : null));
    if (result && !result.finished) {
      anchor.current = { serverMs: result.state.elapsedMs, at: performance.now(), speed: 1 };
    }
  };

  const cashout = () => run(() => casinoApi.crash.cashout());

  const live = multiplierAt(elapsed);
  const tone = busted ? 'busted' : cashed && !inAftermath ? 'cashed' : animating ? 'flying' : 'idle';

  return (
    <GameFrame
      eyebrow="Instantáneo I"
      title="Crash"
      description="El multiplicador sube hasta que el cohete explota. Retirate a tiempo y cobrás apuesta × multiplicador."
      stage={
        <div className={styles.stage}>
          <div className={styles.history}>
            {history.map((m, i) => (
              <span key={i} className={m >= 10 ? styles.hHigh : m >= 2 ? styles.hMid : styles.hLow}>×{m.toFixed(2)}</span>
            ))}
          </div>
          <div className={styles.screen}>
            <CrashChart elapsedMs={elapsed} tone={tone} />
            <div className={`${styles.readout} ${styles[tone]}`}>
              <strong>×{readoutValue(view, busted, cashed, live).toFixed(2)}</strong>
              <span>{statusLabel(session, busted, cashed, isActive, inAftermath, live)}</span>
              {cashed && session && <em className={styles.won}>+{formatCredits(session.payout)}</em>}
            </div>
            {busted && <div className={styles.flash} key={session?.sessionId} />}
          </div>
        </div>
      }
      controls={
        isActive ? (
          <div className={styles.flyingControls}>
            <div className={styles.stakeBox}>
              <span className="eyebrow">Apuesta</span>
              <strong>{formatCredits(session!.stake)}</strong>
            </div>
            <Button size="lg" className={styles.cashout} onClick={() => void cashout()} disabled={busy}>
              Retirar {formatCredits(Math.floor(session!.stake * live))}
            </Button>
          </div>
        ) : (
          <>
            <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
            <div className={styles.actions}>
              <label className={styles.auto}>
                <span className="eyebrow">Retiro automático</span>
                <input
                  inputMode="decimal"
                  placeholder="×2.00 (opcional)"
                  value={autoInput}
                  onChange={(e) => setAutoInput(e.target.value.replace(/[^0-9.,]/g, ''))}
                />
              </label>
              <Button size="lg" onClick={() => void start()} loading={busy} disabled={!player || player.balance < stake}>
                Apostar
              </Button>
            </div>
          </>
        )
      }
    />
  );
}

/** Al retirarse, el número grande queda fijo en el multiplicador cobrado. */
function readoutValue(view: CrashView | undefined, busted: boolean, cashed: boolean, live: number) {
  if (busted && view?.crashPoint) return view.crashPoint;
  if (cashed && view?.cashedOutAt) return view.cashedOutAt;
  return live;
}

function statusLabel(session: SessionResponse<CrashView> | null, busted: boolean, cashed: boolean,
                     active: boolean, aftermath: boolean, live: number) {
  if (active) return 'En vuelo';
  if (busted) return 'Explotó';
  if (cashed && aftermath) return `Retirado · el cohete sigue en ×${live.toFixed(2)}`;
  if (cashed && session?.state.crashPoint) return `Retirado · explotó en ×${session.state.crashPoint.toFixed(2)}`;
  return 'Esperando apuesta';
}
