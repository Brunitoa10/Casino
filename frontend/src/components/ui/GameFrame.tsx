import type { ReactNode } from 'react';
import styles from './GameFrame.module.css';

interface GameFrameProps {
  eyebrow: string;
  title: string;
  description: string;
  stage: ReactNode;
  controls: ReactNode;
  aside?: ReactNode;
}

/** Plantilla visual común a todas las mesas: encabezado, escenario y controles. */
export function GameFrame({ eyebrow, title, description, stage, controls, aside }: GameFrameProps) {
  return (
    <div className={styles.frame}>
      <header className={styles.header}>
        <div>
          <span className="eyebrow">{eyebrow}</span>
          <h2 className={styles.title}>{title}</h2>
        </div>
        <p className={styles.description}>{description}</p>
      </header>
      <div className={styles.stage}>{stage}</div>
      <div className={styles.controls}>{controls}</div>
      {aside && <div className={styles.aside}>{aside}</div>}
    </div>
  );
}
