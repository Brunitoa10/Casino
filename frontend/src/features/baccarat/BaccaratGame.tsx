import { useMemo, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { BaccaratBets, BaccaratOutcome, BaccaratSide, RoundResponse } from '@/api/types';
import { PlayingCard } from '@/components/cards/PlayingCard';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useToast } from '@/context/ToastContext';
import { useGameRound } from '@/hooks/useGameRound';
import { formatCredits, sleep } from '@/lib/format';
import { sound } from '@/lib/sound';
import styles from './Baccarat.module.css';

const SPOTS: { side: BaccaratSide; key: keyof BaccaratBets; label: string; pays: string }[] = [
  { side: 'PLAYER', key: 'player', label: 'Jugador', pays: '1 : 1' },
  { side: 'TIE', key: 'tie', label: 'Empate', pays: '8 : 1' },
  { side: 'BANKER', key: 'banker', label: 'Banca', pays: '0,95 : 1' },
];

const EMPTY: BaccaratBets = { player: 0, banker: 0, tie: 0 };
const CARD_INTERVAL_MS = 520;
const MAX_TABLE = 5000;

/** Orden real de reparto: J1, B1, J2, B2 y luego las terceras cartas. */
function dealOrder(outcome: BaccaratOutcome) {
  const order: { side: 'player' | 'banker'; index: number }[] = [
    { side: 'player', index: 0 }, { side: 'banker', index: 0 },
    { side: 'player', index: 1 }, { side: 'banker', index: 1 },
  ];
  if (outcome.playerCards.length > 2) order.push({ side: 'player', index: 2 });
  if (outcome.bankerCards.length > 2) order.push({ side: 'banker', index: 2 });
  return order;
}

export function BaccaratGame() {
  const { player } = useSession();
  const { notify } = useToast();
  const { busy, play } = useGameRound<BaccaratOutcome>();
  const [chip, setChip] = useState(50);
  const [bets, setBets] = useState<BaccaratBets>(EMPTY);
  const [lastBets, setLastBets] = useState<BaccaratBets | null>(null);
  const [round, setRound] = useState<RoundResponse<BaccaratOutcome> | null>(null);
  const [shown, setShown] = useState(0);
  const [road, setRoad] = useState<BaccaratSide[]>([]);

  const total = bets.player + bets.banker + bets.tie;
  const outcome = round?.outcome;
  const revealed = useMemo(() => (outcome ? dealOrder(outcome).slice(0, shown) : []), [outcome, shown]);
  const settled = outcome !== undefined && shown >= dealOrder(outcome).length && !busy;

  const place = (key: keyof BaccaratBets) => {
    if (total + chip > MAX_TABLE) return notify(`Máximo ${formatCredits(MAX_TABLE)} por mano`, 'error');
    if (player && total + chip > player.balance) return notify('Saldo insuficiente', 'error');
    sound.chip();
    setRound(null);
    setBets((b) => ({ ...b, [key]: b[key] + chip }));
  };

  const deal = async () => {
    const placed = bets;
    setRound(null);
    setShown(0);
    const result = await play(total, () => casinoApi.baccarat(placed), async (r) => {
      setRound(r);
      for (let i = 1; i <= dealOrder(r.outcome).length; i++) {
        await sleep(CARD_INTERVAL_MS);
        sound.deal();
        setShown(i);
      }
      await sleep(400);
    });
    if (!result) return;
    setLastBets(placed);
    setBets(EMPTY);
    setRoad((r) => [result.outcome.winner, ...r].slice(0, 24));
    if (result.payout > result.stake) sound.win(result.outcome.winner === 'TIE' ? 3 : 1);
    else if (result.payout === 0) sound.lose();
  };

  const cardsOf = (side: 'player' | 'banker') =>
    outcome ? (side === 'player' ? outcome.playerCards : outcome.bankerCards) : [];
  const visible = (side: 'player' | 'banker', index: number) => revealed.some((c) => c.side === side && c.index === index);
  const totalOf = (side: 'player' | 'banker') => {
    const values = cardsOf(side).filter((_, i) => visible(side, i)).map(cardValue);
    return values.reduce((a, b) => a + b, 0) % 10;
  };

  return (
    <GameFrame
      eyebrow="Mesa VI"
      title="Baccarat"
      description="Punto Banco clásico. Apostá a Jugador, Banca o Empate: las cartas siguen reglas fijas, gana el más cercano a 9."
      stage={
        <div className={styles.table}>
          <div className={styles.hands}>
            {(['player', 'banker'] as const).map((side) => {
              const winner = settled && outcome?.winner === (side === 'player' ? 'PLAYER' : 'BANKER');
              return (
                <section key={side} className={`${styles.side} ${styles[side]} ${winner ? styles.winner : ''}`}>
                  <header>
                    <span className={styles.sideName}>{side === 'player' ? 'Jugador' : 'Banca'}</span>
                    <span className={styles.score}>{outcome && shown > 0 ? totalOf(side) : '–'}</span>
                  </header>
                  <div className={styles.cards}>
                    {cardsOf(side).map((code, i) =>
                      visible(side, i) ? (
                        <PlayingCard key={`${round?.roundId}-${side}-${i}`} code={code} highlight={winner} />
                      ) : null,
                    )}
                  </div>
                </section>
              );
            })}
          </div>

          {settled && outcome?.winner === 'TIE' && <div className={styles.tieBadge}>Empate</div>}

          <div className={styles.spots}>
            {SPOTS.map(({ side, key, label, pays }) => (
              <button
                key={side}
                type="button"
                className={`${styles.spot} ${styles[`spot${side}`]} ${settled && outcome?.winner === side ? styles.spotWin : ''}`}
                onClick={() => place(key)}
                disabled={busy}
              >
                <span className={styles.spotLabel}>{label}</span>
                <span className={styles.spotPays}>{pays}</span>
                {bets[key] > 0 && <i className={styles.chip}>{formatCredits(bets[key])}</i>}
              </button>
            ))}
          </div>

          <div className={styles.road} aria-label="Historial">
            {road.map((side, i) => (
              <span key={i} className={styles[`bead${side}`]}>{side === 'PLAYER' ? 'J' : side === 'BANKER' ? 'B' : 'E'}</span>
            ))}
          </div>

          {settled && round && round.payout > round.stake && (
            <WinBanner key={round.roundId} amount={round.payout} stake={round.stake} />
          )}
        </div>
      }
      controls={
        <>
          <StakeControl label="Valor de ficha" value={chip} onChange={setChip} max={player?.balance ?? 0} disabled={busy} />
          <div className={styles.actions}>
            <div className={styles.totalBox}>
              <span className="eyebrow">En mesa</span>
              <strong>{formatCredits(total)}</strong>
            </div>
            <Button variant="subtle" onClick={() => setBets(EMPTY)} disabled={busy || total === 0}>Limpiar</Button>
            <Button variant="ghost" onClick={() => lastBets && setBets(lastBets)} disabled={busy || !lastBets || total > 0}>
              Repetir
            </Button>
            <Button size="lg" onClick={() => void deal()} loading={busy} disabled={total === 0}>Repartir</Button>
          </div>
        </>
      }
    />
  );
}

/** Valor de baccarat de un código de carta: As = 1, 10 y figuras = 0. */
function cardValue(code: string): number {
  const rank = code[0];
  if (rank === 'A') return 1;
  if ('TJQK'.includes(rank)) return 0;
  return Number(rank);
}
