import { useEffect, useState } from 'react';
import { casinoApi } from '@/api/casinoApi';
import type { PlayerStats, RoundSummary } from '@/api/types';
import { useSession } from '@/context/SessionContext';
import { formatCredits, formatSigned, formatTime } from '@/lib/format';
import { gameName } from '@/games/registry';
import styles from './Layout.module.css';

export function RoundFeed() {
  const { roundsVersion } = useSession();
  const [rounds, setRounds] = useState<RoundSummary[]>([]);
  const [stats, setStats] = useState<PlayerStats | null>(null);

  useEffect(() => {
    let alive = true;
    Promise.all([casinoApi.recentRounds(12), casinoApi.stats()])
      .then(([recent, summary]) => {
        if (!alive) return;
        setRounds(recent);
        setStats(summary);
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [roundsVersion]);

  return (
    <aside className={styles.feed}>
      <section className={styles.statsGrid}>
        <Stat label="Jugadas" value={formatCredits(stats?.rounds ?? 0)} />
        <Stat label="Mayor premio" value={formatCredits(stats?.biggestWin ?? 0)} accent />
        <Stat label="Apostado" value={formatCredits(stats?.wagered ?? 0)} />
        <Stat label="Resultado" value={formatSigned(stats?.net ?? 0)} tone={(stats?.net ?? 0) >= 0 ? 'up' : 'down'} />
      </section>

      <div className={styles.feedHead}>
        <span className="eyebrow">Últimas jugadas</span>
      </div>
      {rounds.length === 0 ? (
        <p className={styles.empty}>Tu historial aparecerá aquí.</p>
      ) : (
        <ul className={styles.feedList}>
          {rounds.map((round) => {
            const net = round.payout - round.stake;
            return (
              <li key={round.id} className={styles.feedItem}>
                <div>
                  <strong>{gameName(round.game)}</strong>
                  <small>{round.summary}</small>
                </div>
                <div className={styles.feedRight}>
                  <span className={net > 0 ? styles.up : net < 0 ? styles.down : ''}>{formatSigned(net)}</span>
                  <small>{formatTime(round.createdAt)}</small>
                </div>
              </li>
            );
          })}
        </ul>
      )}
      <p className={styles.disclaimer}>Créditos ficticios · sin valor monetario</p>
    </aside>
  );
}

function Stat({ label, value, accent, tone }: { label: string; value: string; accent?: boolean; tone?: 'up' | 'down' }) {
  return (
    <div className={styles.stat}>
      <small>{label}</small>
      <strong className={accent ? 'gold-text' : tone ? styles[tone] : undefined}>{value}</strong>
    </div>
  );
}
