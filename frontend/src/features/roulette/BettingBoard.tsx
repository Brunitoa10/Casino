import type { ReactNode } from 'react';
import type { RouletteBetType } from '@/api/types';
import { betKey, colorOf } from '@/lib/roulette';
import { formatCredits } from '@/lib/format';
import styles from './Roulette.module.css';

interface BettingBoardProps {
  amounts: ReadonlyMap<string, number>;
  onPlace: (type: RouletteBetType, value?: number) => void;
  winningNumber: number | null;
  winningKeys: ReadonlySet<string>;
  disabled: boolean;
}

// Fila superior = columna 3, inferior = columna 1 (como en el paño real)
const ROWS = [3, 2, 1].map((offset) => Array.from({ length: 12 }, (_, col) => col * 3 + offset));

const OUTSIDE: { type: RouletteBetType; label: string }[] = [
  { type: 'LOW', label: '1–18' },
  { type: 'EVEN', label: 'Par' },
  { type: 'RED', label: '' },
  { type: 'BLACK', label: '' },
  { type: 'ODD', label: 'Impar' },
  { type: 'HIGH', label: '19–36' },
];

export function BettingBoard({ amounts, onPlace, winningNumber, winningKeys, disabled }: BettingBoardProps) {
  const cell = (type: RouletteBetType, value: number | undefined, label: ReactNode, className = '') => {
    const key = betKey(type, value);
    const amount = amounts.get(key);
    const isHit = winningKeys.has(key) || (type === 'STRAIGHT' && value === winningNumber);
    return (
      <button
        key={key}
        type="button"
        className={`${styles.spot} ${className} ${isHit ? styles.hit : ''}`}
        onClick={() => onPlace(type, value)}
        disabled={disabled}
      >
        <span>{label}</span>
        {amount !== undefined && <i className={styles.chipMark}>{formatCredits(amount)}</i>}
      </button>
    );
  };

  return (
    <div className={styles.boardScroll}>
      <div className={styles.board}>
        {cell('STRAIGHT', 0, '0', `${styles.zero} ${styles.green}`)}
        {ROWS.map((row, r) => (
          <div key={r} className={styles.row}>
            {row.map((n) => cell('STRAIGHT', n, n, colorOf(n) === 'RED' ? styles.red : styles.black))}
            {cell('COLUMN', 3 - r, '2:1', styles.outside)}
          </div>
        ))}
        <div className={styles.dozens}>
          {[1, 2, 3].map((d) => cell('DOZEN', d, `${d}ª docena`, styles.outside))}
        </div>
        <div className={styles.evens}>
          {OUTSIDE.map(({ type, label }) =>
            cell(
              type,
              undefined,
              label || <span className={type === 'RED' ? styles.diamondRed : styles.diamondBlack} />,
              styles.outside,
            ),
          )}
        </div>
      </div>
    </div>
  );
}
