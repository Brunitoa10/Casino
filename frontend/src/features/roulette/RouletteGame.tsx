import { useCallback, useMemo, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { RouletteBet, RouletteBetType, RouletteOutcome, RoundResponse } from '@/api/types';
import { Button } from '@/components/ui/Button';
import { GameFrame } from '@/components/ui/GameFrame';
import { StakeControl } from '@/components/ui/StakeControl';
import { WinBanner } from '@/components/ui/WinBanner';
import { useSession } from '@/context/SessionContext';
import { useToast } from '@/context/ToastContext';
import { useGameRound } from '@/hooks/useGameRound';
import { formatCredits, sleep } from '@/lib/format';
import { betKey, colorOf } from '@/lib/roulette';
import { sound } from '@/lib/sound';
import { BettingBoard } from './BettingBoard';
import { RouletteWheel, ballRotationFor, wheelRotationFor } from './RouletteWheel';
import styles from './Roulette.module.css';

const SPIN_MS = 5200;
const MAX_STAKE_PER_SPIN = 5000;

type BetMap = Map<string, RouletteBet>;

export function RouletteGame() {
  const { player } = useSession();
  const { notify } = useToast();
  const { busy, play } = useGameRound<RouletteOutcome>();

  const [chip, setChip] = useState(10);
  const [bets, setBets] = useState<BetMap>(new Map());
  const [placementOrder, setPlacementOrder] = useState<string[]>([]);
  const [lastBets, setLastBets] = useState<RouletteBet[]>([]);
  const [wheel, setWheel] = useState({ rotation: 0, ball: 0, spinning: false });
  const [result, setResult] = useState<RoundResponse<RouletteOutcome> | null>(null);
  const [history, setHistory] = useState<number[]>([]);

  const total = useMemo(() => [...bets.values()].reduce((sum, b) => sum + b.amount, 0), [bets]);
  const amounts = useMemo(() => new Map([...bets].map(([k, b]) => [k, b.amount])), [bets]);

  const winningKeys = useMemo(
    () => new Set(result?.outcome.bets.filter((b) => b.won).map((b) => betKey(b.type, b.value)) ?? []),
    [result],
  );

  const place = useCallback(
    (type: RouletteBetType, value?: number) => {
      if (total + chip > MAX_STAKE_PER_SPIN) {
        notify(`Máximo ${formatCredits(MAX_STAKE_PER_SPIN)} por giro`, 'error');
        return;
      }
      if (player && total + chip > player.balance) {
        notify('Saldo insuficiente', 'error');
        return;
      }
      sound.chip();
      setResult(null);
      const key = betKey(type, value);
      setBets((current) => {
        const next = new Map(current);
        const existing = next.get(key);
        next.set(key, { type, value, amount: (existing?.amount ?? 0) + chip });
        return next;
      });
      setPlacementOrder((order) => [...order, key]);
    },
    [chip, total, player, notify],
  );

  const undo = () => {
    const key = placementOrder.at(-1);
    if (!key) return;
    setPlacementOrder((order) => order.slice(0, -1));
    setBets((current) => {
      const next = new Map(current);
      const bet = next.get(key);
      if (!bet) return next;
      const amount = bet.amount - chip;
      if (amount > 0) next.set(key, { ...bet, amount });
      else next.delete(key);
      return next;
    });
  };

  const clear = () => {
    setBets(new Map());
    setPlacementOrder([]);
  };

  const rebet = () => {
    const map: BetMap = new Map(lastBets.map((b) => [betKey(b.type, b.value), b]));
    setBets(map);
    setPlacementOrder([...map.keys()]);
    setResult(null);
    sound.chip();
  };

  const spin = async () => {
    if (bets.size === 0) return;
    const placed = [...bets.values()];
    setResult(null);
    sound.spin();
    const round = await play(total, () => casinoApi.spinRoulette(placed), async (r) => {
      setWheel((w) => ({
        rotation: wheelRotationFor(r.outcome.number, w.rotation),
        ball: ballRotationFor(w.ball),
        spinning: true,
      }));
      // Tic-tac de la bola que se va espaciando
      for (let t = 60; t < SPIN_MS - 400; t += 60 + t / 14) setTimeout(() => sound.tick(), t);
      await sleep(SPIN_MS + 100);
      setWheel((w) => ({ ...w, spinning: false }));
    });
    if (!round) return;
    setResult(round);
    setHistory((h) => [round.outcome.number, ...h].slice(0, 14));
    setLastBets(placed);
    clear();
    if (round.payout > 0) sound.win(round.payout / round.stake >= 10 ? 3 : 1);
    else sound.lose();
  };

  const outcome = result?.outcome;

  return (
    <GameFrame
      eyebrow="Mesa II"
      title="Ruleta europea"
      description="Un solo cero. Pleno paga 35 a 1, docenas y columnas 2 a 1, suertes sencillas 1 a 1."
      stage={
        <div className={styles.table}>
          <div className={styles.wheelColumn}>
            <RouletteWheel wheelRotation={wheel.rotation} ballRotation={wheel.ball} spinning={wheel.spinning} durationMs={SPIN_MS} />
            <div className={styles.resultBadge} data-color={outcome ? outcome.color : undefined}>
              {outcome ? outcome.number : busy ? '·' : '—'}
            </div>
            <div className={styles.history} aria-label="Últimos números">
              {history.map((n, i) => (
                <span key={`${n}-${i}`} className={styles[`h${colorOf(n)}`]}>{n}</span>
              ))}
            </div>
          </div>
          <BettingBoard
            amounts={amounts}
            onPlace={place}
            winningNumber={busy ? null : outcome?.number ?? null}
            winningKeys={winningKeys}
            disabled={busy}
          />
          {result && result.payout > 0 && <WinBanner key={result.roundId} amount={result.payout} stake={result.stake} />}
        </div>
      }
      controls={
        <>
          <StakeControl label="Valor de ficha" value={chip} onChange={setChip} max={player?.balance ?? 0} disabled={busy} />
          <div className={styles.actions}>
            <div className={styles.total}>
              <span className="eyebrow">En mesa</span>
              <strong>{formatCredits(total)}</strong>
            </div>
            <Button variant="subtle" onClick={undo} disabled={busy || placementOrder.length === 0}>Deshacer</Button>
            <Button variant="subtle" onClick={clear} disabled={busy || bets.size === 0}>Limpiar</Button>
            <Button variant="ghost" onClick={rebet} disabled={busy || lastBets.length === 0 || bets.size > 0}>Repetir</Button>
            <Button size="lg" onClick={() => void spin()} disabled={busy || bets.size === 0} loading={busy}>
              {busy ? 'No va más' : 'Girar'}
            </Button>
          </div>
        </>
      }
    />
  );
}
