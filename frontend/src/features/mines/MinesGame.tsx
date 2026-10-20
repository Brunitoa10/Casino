import { useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { MinesView, SessionResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameSession } from '@/hooks/useGameSession';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import { BombIcon, GemIcon } from './MinesIcons';
import styles from './Mines.module.css';

const TILES = Array.from({ length: 25 }, (_, i) => i);
const MINE_PRESETS = [1, 3, 5, 10, 24];

type TileState = 'hidden' | 'gem' | 'mine' | 'hitMine' | 'ghostGem';

function tileState(view: MinesView | undefined, tile: number): TileState {
  if (!view) return 'hidden';
  if (view.hitMine === tile) return 'hitMine';
  if (view.revealed.includes(tile)) return 'gem';
  if (view.finished && view.minePositions.includes(tile)) return 'mine';
  if (view.finished) return 'ghostGem';
  return 'hidden';
}

export function MinesGame() {
  const { player } = useSession();
  const { session, isActive, busy, run } = useGameSession(casinoApi.mines.active);
  const [stake, setStake] = useState(50);
  const [mines, setMines] = useState(3);
  const view = session?.state;

  const announce = (result: SessionResponse<MinesView> | null) => {
    if (!result) return;
    if (result.state.busted) sound.boom();
    else if (result.finished) sound.win(result.state.multiplier >= 10 ? 3 : result.state.multiplier >= 3 ? 2 : 1);
    else sound.gem();
  };

  const start = async () => {
    sound.chip();
    await run(() => casinoApi.mines.start(stake, mines));
  };

  const reveal = async (tile: number) => {
    if (!isActive || busy || view?.revealed.includes(tile)) return;
    announce(await run(() => casinoApi.mines.reveal(tile)));
  };

  const cashout = async () => announce(await run(() => casinoApi.mines.cashout()));

  const canCashout = isActive && (view?.revealed.length ?? 0) > 0;

  return (
    <GameFrame
      eyebrow="Instantáneo II"
      title="Minas"
      description="Descubrí gemas en un tablero de 25 casillas. Cada gema sube el multiplicador; una mina se lleva todo."
      stage={
        <div className={styles.stage}>
          <div className={styles.grid}>
            {TILES.map((tile) => {
              const state = tileState(view, tile);
              return (
                <button
                  key={tile}
                  type="button"
                  className={`${styles.tile} ${styles[state]}`}
                  onClick={() => void reveal(tile)}
                  disabled={!isActive || busy || state !== 'hidden'}
                  aria-label={`Casilla ${tile + 1}`}
                >
                  {(state === 'gem' || state === 'ghostGem') && <GemIcon />}
                  {(state === 'mine' || state === 'hitMine') && <BombIcon />}
                </button>
              );
            })}
          </div>

          <aside className={styles.meter}>
            <Stat label="Multiplicador" value={`×${(view?.multiplier ?? 1).toFixed(2)}`} gold />
            <Stat label="Próxima gema" value={isActive ? `×${view!.nextMultiplier.toFixed(2)}` : '—'} />
            <Stat label="Gemas" value={`${view?.revealed.length ?? 0} / ${25 - (view?.mines ?? mines)}`} />
            <Stat label="Cobro actual" value={formatCredits(Math.floor((session?.stake ?? stake) * (view?.multiplier ?? 1)))} />
          </aside>

          {session?.finished && session.payout > session.stake && (
            <WinBanner key={session.sessionId} amount={session.payout} stake={session.stake} />
          )}
        </div>
      }
      controls={
        isActive ? (
          <div className={styles.activeControls}>
            <div className={styles.info}>
              <span className="eyebrow">{view?.mines} minas</span>
              <strong>{formatCredits(session!.stake)}</strong>
            </div>
            <Button size="lg" onClick={() => void cashout()} disabled={!canCashout || busy}>
              Retirar {formatCredits(Math.floor(session!.stake * (view?.multiplier ?? 1)))}
            </Button>
          </div>
        ) : (
          <>
            <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
            <div className={styles.setup}>
              <div className={styles.minePicker}>
                <span className="eyebrow">Minas · {mines}</span>
                <div className={styles.presets}>
                  {MINE_PRESETS.map((m) => (
                    <button key={m} type="button" className={m === mines ? styles.presetOn : ''} onClick={() => setMines(m)}>
                      {m}
                    </button>
                  ))}
                </div>
                <input type="range" min={1} max={24} value={mines} onChange={(e) => setMines(Number(e.target.value))} aria-label="Cantidad de minas" />
              </div>
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

function Stat({ label, value, gold }: { label: string; value: string; gold?: boolean }) {
  return (
    <div className={styles.stat}>
      <small>{label}</small>
      <strong className={gold ? 'gold-text' : undefined}>{value}</strong>
    </div>
  );
}
