import { useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { BlackjackAction, BlackjackHandView, BlackjackView, HandOutcome, SessionResponse } from '@/api/types';
import { PlayingCard } from '@/components/cards/PlayingCard';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useGameSession } from '@/hooks/useGameSession';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import styles from './Blackjack.module.css';

const OUTCOME_LABEL: Record<HandOutcome, string> = {
  PENDING: '',
  BLACKJACK: 'Blackjack',
  WIN: 'Ganás',
  PUSH: 'Empate',
  LOSE: 'Perdés',
  BUST: 'Te pasaste',
};

const ACTIONS: { action: BlackjackAction; label: string; enabled: (v: BlackjackView) => boolean }[] = [
  { action: 'HIT', label: 'Pedir', enabled: (v) => v.canHit },
  { action: 'STAND', label: 'Plantarse', enabled: (v) => v.canStand },
  { action: 'DOUBLE', label: 'Doblar', enabled: (v) => v.canDouble },
  { action: 'SPLIT', label: 'Dividir', enabled: (v) => v.canSplit },
];

/** Total legible: una mano blanda se muestra como "7 / 17". */
const totalLabel = (total: number, soft: boolean) => (soft && total < 21 ? `${total - 10} / ${total}` : `${total}`);

/** Escalona solo el reparto inicial (jugador, crupier, jugador, crupier). */
const playerDealIndex = (i: number) => (i < 2 ? i * 2 : 0);
const dealerDealIndex = (i: number) => (i < 2 ? i * 2 + 1 : 2 + (i - 2) * 2);

export function BlackjackGame() {
  const { player } = useSession();
  const { session, isActive, busy, run } = useGameSession(casinoApi.blackjack.active);
  const [stake, setStake] = useState(100);
  const view = session?.state;

  const celebrate = (result: SessionResponse<BlackjackView> | null) => {
    if (!result?.finished) return;
    if (result.payout > result.stake) sound.win(result.payout >= result.stake * 2.5 ? 2 : 1);
    else if (result.payout === 0) setTimeout(() => sound.lose(), 400);
  };

  const deal = async () => {
    [0, 1, 2, 3].forEach((i) => setTimeout(() => sound.deal(), i * 140));
    celebrate(await run(() => casinoApi.blackjack.deal(stake)));
  };

  const act = async (action: BlackjackAction) => {
    sound.deal();
    celebrate(await run(() => casinoApi.blackjack.act(action)));
  };

  return (
    <GameFrame
      eyebrow="Mesa IV"
      title="Blackjack"
      description="Seis mazos. Blackjack paga 3 a 2, el crupier se planta en 17. Doblá con dos cartas o dividí un par."
      stage={
        <div className={styles.table}>
          <p className={styles.rules}>Blackjack paga 3 a 2 · El crupier se planta en 17</p>

          <section className={styles.dealer}>
            <span className="eyebrow">Crupier</span>
            <div className={styles.cards}>
              {view ? (
                view.dealerCards.map((code, i) => (
                  <PlayingCard key={i} code={code} dealIndex={dealerDealIndex(i)} />
                ))
              ) : (
                <div className={styles.placeholder} />
              )}
            </div>
            {view && <span className={styles.total}>{view.dealerTotal}</span>}
          </section>

          <section className={styles.players}>
            {view ? (
              view.hands.map((hand, i) => (
                <Hand
                  key={i}
                  hand={hand}
                  active={!view.finished && view.hands.length > 1 && view.activeHand === i}
                />
              ))
            ) : (
              <div className={styles.invite}>
                <span className="eyebrow">Tu lugar</span>
                <p>Elegí tu apuesta y repartí.</p>
              </div>
            )}
          </section>

          {session?.finished && session.payout > session.stake && (
            <WinBanner key={session.sessionId} amount={session.payout} stake={session.stake} />
          )}
        </div>
      }
      controls={
        isActive && view ? (
          <div className={styles.actions}>
            <div className={styles.bet}>
              <span className="eyebrow">En juego</span>
              <strong>{formatCredits(session.stake)}</strong>
            </div>
            {ACTIONS.map(({ action, label, enabled }) => (
              <Button
                key={action}
                variant={action === 'STAND' ? 'primary' : 'ghost'}
                size={action === 'STAND' ? 'lg' : 'md'}
                onClick={() => void act(action)}
                disabled={busy || !enabled(view)}
              >
                {label}
              </Button>
            ))}
          </div>
        ) : (
          <>
            <StakeControl value={stake} onChange={setStake} max={player?.balance ?? 0} disabled={busy} />
            <Button size="lg" onClick={() => void deal()} loading={busy} disabled={!player || player.balance < stake}>
              Repartir
            </Button>
          </>
        )
      }
    />
  );
}

function Hand({ hand, active }: { hand: BlackjackHandView; active: boolean }) {
  const outcome = OUTCOME_LABEL[hand.outcome];
  const tone = hand.outcome === 'WIN' || hand.outcome === 'BLACKJACK' ? styles.good
    : hand.outcome === 'PUSH' ? styles.neutral : styles.bad;
  return (
    <div className={`${styles.hand} ${active ? styles.activeHand : ''}`}>
      <div className={styles.cards}>
        {hand.cards.map((code, i) => (
          <PlayingCard key={i} code={code} dealIndex={playerDealIndex(i)} highlight={hand.outcome === 'BLACKJACK'} />
        ))}
      </div>
      <div className={styles.handMeta}>
        <span className={styles.total}>{totalLabel(hand.total, hand.soft)}</span>
        <span className={styles.stakeChip}>{formatCredits(hand.stake)}{hand.doubled ? ' ×2' : ''}</span>
        {outcome && <span className={`${styles.outcome} ${tone}`}>{outcome}</span>}
      </div>
    </div>
  );
}
