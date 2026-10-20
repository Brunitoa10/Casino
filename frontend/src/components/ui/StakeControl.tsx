import { STAKE_LIMITS, STAKE_PRESETS } from '@/config';
import { formatCredits } from '@/lib/format';
import { sound } from '@/lib/sound';
import styles from './StakeControl.module.css';

interface StakeControlProps {
  value: number;
  onChange: (value: number) => void;
  max: number;
  disabled?: boolean;
  label?: string;
}

const clamp = (value: number, max: number) =>
  Math.max(STAKE_LIMITS.min, Math.min(Math.floor(value), Math.min(max, STAKE_LIMITS.max)));

export function StakeControl({ value, onChange, max, disabled, label = 'Apuesta' }: StakeControlProps) {
  const set = (next: number) => {
    sound.chip();
    onChange(clamp(next, Math.max(STAKE_LIMITS.min, max)));
  };

  return (
    <div className={styles.wrapper}>
      <div className={styles.head}>
        <span className="eyebrow">{label}</span>
        <div className={styles.stepper}>
          <button type="button" onClick={() => set(value / 2)} disabled={disabled} aria-label="Mitad">½</button>
          <output className={styles.value}>{formatCredits(value)}</output>
          <button type="button" onClick={() => set(value * 2)} disabled={disabled} aria-label="Doble">2×</button>
        </div>
      </div>
      <div className={styles.chips}>
        {STAKE_PRESETS.map((preset) => (
          <button
            key={preset}
            type="button"
            className={`${styles.chip} ${value === preset ? styles.active : ''}`}
            data-tier={preset}
            onClick={() => set(preset)}
            disabled={disabled || preset > max}
          >
            {preset >= 1000 ? `${preset / 1000}K` : preset}
          </button>
        ))}
      </div>
    </div>
  );
}
