import { useMemo, type CSSProperties } from 'react';
import { useAnimatedNumber } from '@/hooks/useAnimatedNumber';
import { formatCredits } from '@/lib/format';
import styles from './WinBanner.module.css';

interface WinBannerProps {
  amount: number;
  stake: number;
}

/** Celebración de premio: escala según el múltiplo ganado. */
export function WinBanner({ amount, stake }: WinBannerProps) {
  const ratio = stake > 0 ? amount / stake : 0;
  const tier = ratio >= 20 ? 'mega' : ratio >= 5 ? 'big' : 'win';
  const shown = useAnimatedNumber(amount, tier === 'win' ? 600 : 1600);

  const coins = useMemo(
    () =>
      Array.from({ length: tier === 'mega' ? 42 : tier === 'big' ? 26 : 12 }, (_, i) => ({
        id: i,
        x: (Math.random() - 0.5) * 520,
        y: -(120 + Math.random() * 220),
        delay: Math.random() * 0.25,
        rotate: Math.random() * 720 - 360,
        size: 10 + Math.random() * 12,
      })),
    [tier],
  );

  const title = tier === 'mega' ? 'Mega premio' : tier === 'big' ? 'Gran premio' : 'Ganaste';

  return (
    <div className={`${styles.overlay} ${styles[tier]}`} aria-live="assertive">
      <div className={styles.burst}>
        {coins.map((c) => (
          <span
            key={c.id}
            className={styles.coin}
            style={{
              '--x': `${c.x}px`,
              '--y': `${c.y}px`,
              '--r': `${c.rotate}deg`,
              '--d': `${c.delay}s`,
              width: c.size,
              height: c.size,
            } as CSSProperties}
          />
        ))}
      </div>
      <div className={styles.card}>
        <span className={styles.title}>{title}</span>
        <strong className={`${styles.amount} gold-text`}>+{formatCredits(shown)}</strong>
        {ratio >= 2 && <span className={styles.ratio}>×{ratio.toFixed(ratio >= 10 ? 0 : 1)}</span>}
      </div>
    </div>
  );
}
