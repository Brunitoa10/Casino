import { useEffect, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import { PlayingCard } from '@/components/cards/PlayingCard';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameSession } from '@/hooks/useGameSession';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import { PAYTABLE } from './paytable';
import styles from './VideoPoker.module.css';

const HAND_SIZE = 5;
const BACKS = Array.from({ length: HAND_SIZE }, () => '??');

export function VideoPokerGame() {
  const { player } = useSession();
  const { session, isActive, busy, run } = useGameSession(casinoApi.videoPoker.active);
  const [stake, setStake] = useState(50);
  const [held, setHeld] = useState<boolean[]>(() => Array(HAND_SIZE).fill(false));
  const view = session?.state;

  // Al retomar o repartir, la retención arranca vacía
  useEffect(() => {
    if (isActive) setHeld(Array(HAND_SIZE).fill(false));
  }, [isActive, session?.sessionId]);

  const toggleHold = (index: number) => {
    sound.chip();
    setHeld((current) => current.map((h, i) => (i === index ? !h : h)));
  };

  const deal = async () => {
    Array.from({ length: HAND_SIZE }).forEach((_, i) => setTimeout(() => sound.deal(), i * 110));
    await run(() => casinoApi.videoPoker.deal(stake));
  };

  const draw = async () => {
    held.forEach((h, i) => !h && setTimeout(() => sound.deal(), i * 110));
    const positions = held.flatMap((h, i) => (h ? [i] : []));
    const result = await run(() => casinoApi.videoPoker.draw(positions));
    if (!result) return;
    if (result.payout > result.stake) sound.win(result.payout >= result.stake * 9 ? 3 : result.payout > result.stake * 2 ? 2 : 1);
    else if (result.payout === 0) setTimeout(() => sound.lose(), 500);
  };

  const cards = view?.cards ?? BACKS;
  const currentHand = view?.hand;
  const finished = session?.finished ?? false;
  const payout = session?.payout ?? 0;
  const roundStake = session?.stake ?? stake;

  return (
    <GameFrame
      eyebrow="Mesa V"
      title="Video póker"
      description="Jacks or Better 9/6. Retené las cartas que quieras, cambiá el resto una vez y cobrá según la tabla."
      stage={
        <div className={styles.machine}>
          <div className={styles.paytable}>
            {PAYTABLE.map((row) => {
              const hit = currentHand === row.hand;
              return (
                <div key={row.hand} className={`${styles.payRow} ${hit ? (finished ? styles.payWin : styles.payHint) : ''}`}>
                  <span>{row.label}</span>
                  <strong>{formatCredits(row.multiplier * stake)}</strong>
                </div>
              );
            })}
          </div>

          <div className={styles.hand}>
            {cards.map((code, i) => (
              <PlayingCard
                key={`${session?.sessionId ?? 'idle'}-${i}-${code}`}
                code={code}
                size="lg"
                dealIndex={i}
                label={isActive && held[i] ? 'RETENER' : undefined}
                highlight={isActive && held[i]}
                dimmed={finished && payout === 0}
                onClick={isActive ? () => toggleHold(i) : undefined}
                disabled={busy}
              />
            ))}
          </div>

          <p className={styles.status}>
            {!view && 'Repartí para empezar'}
            {isActive && view && `${view.handLabel} · tocá las cartas que querés retener`}
            {finished && view && (payout > 0 ? `${view.handLabel} · cobrás ${formatCredits(payout)}` : 'Sin juego')}
          </p>

          {finished && payout > roundStake && (
            <WinBanner key={session?.sessionId} amount={payout} stake={roundStake} />
          )}
        </div>
      }
      controls={
        <>
          <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy || isActive} />
          {isActive ? (
            <Button size="lg" onClick={() => void draw()} loading={busy}>Cambiar</Button>
          ) : (
            <Button size="lg" onClick={() => void deal()} loading={busy} disabled={!player || player.balance < stake}>
              Repartir
            </Button>
          )}
        </>
      }
    />
  );
}
