import type { CSSProperties } from 'react';
import type { CardCode } from '@/api/types';
import styles from './PlayingCard.module.css';

const SUITS: Record<string, { symbol: string; red: boolean }> = {
  S: { symbol: '♠', red: false },
  H: { symbol: '♥', red: true },
  D: { symbol: '♦', red: true },
  C: { symbol: '♣', red: false },
};

const HIDDEN = '??';

interface PlayingCardProps {
  code: CardCode;
  /** Orden de reparto: escalona la animación de entrada. */
  dealIndex?: number;
  size?: 'sm' | 'md' | 'lg';
  highlight?: boolean;
  dimmed?: boolean;
  label?: string;
  onClick?: () => void;
  disabled?: boolean;
}

/** Carta con cara y dorso: al pasar de "??" a un código real se da vuelta en 3D. */
export function PlayingCard({ code, dealIndex = 0, size = 'md', highlight, dimmed, label, onClick, disabled }: PlayingCardProps) {
  const hidden = code === HIDDEN;
  const rank = hidden ? '' : code[0] === 'T' ? '10' : code[0];
  const suit = SUITS[code[1]] ?? SUITS.S;
  const classes = [styles.card, styles[size], hidden && styles.flipped, highlight && styles.highlight,
    dimmed && styles.dimmed, onClick && styles.clickable].filter(Boolean).join(' ');

  const content = (
    <span className={styles.inner}>
      <span className={`${styles.face} ${suit.red ? styles.red : styles.black}`}>
        <span className={styles.corner}>
          <b>{rank}</b>
          <i>{suit.symbol}</i>
        </span>
        <span className={styles.pip}>{suit.symbol}</span>
        <span className={`${styles.corner} ${styles.cornerBottom}`}>
          <b>{rank}</b>
          <i>{suit.symbol}</i>
        </span>
      </span>
      <span className={styles.back} aria-hidden="true" />
    </span>
  );

  const style = { '--deal-delay': `${dealIndex * 140}ms` } as CSSProperties;

  return onClick ? (
    <button type="button" className={classes} style={style} onClick={onClick} disabled={disabled}
      aria-label={hidden ? 'Carta oculta' : `${rank} ${suit.symbol}`}>
      {content}
      {label && <span className={styles.label}>{label}</span>}
    </button>
  ) : (
    <span className={classes} style={style} role="img" aria-label={hidden ? 'Carta oculta' : `${rank} ${suit.symbol}`}>
      {content}
      {label && <span className={styles.label}>{label}</span>}
    </span>
  );
}
