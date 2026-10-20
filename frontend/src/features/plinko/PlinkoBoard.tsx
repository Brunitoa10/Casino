import { PLINKO_ROWS } from './plinkoConfig';
import styles from './Plinko.module.css';

export const BOARD = { width: 560, height: 520, dx: 40, dy: 36, top: 40 } as const;
const CX = BOARD.width / 2;

/** Posición del centro de la bola después de {@code row} rebotes con {@code rights} a la derecha. */
export function ballPoint(row: number, rights: number): { x: number; y: number } {
  return {
    x: CX + (rights - row / 2) * BOARD.dx,
    y: BOARD.top + (row - 0.55) * BOARD.dy,
  };
}

export const slotY = () => BOARD.top + (PLINKO_ROWS - 0.2) * BOARD.dy;

interface PlinkoBoardProps {
  multipliers: number[];
  ball: { x: number; y: number } | null;
  landedSlot: number | null;
  litRow: number;
}

const slotTone = (m: number) => (m >= 10 ? 'hot' : m >= 2 ? 'warm' : m >= 1 ? 'even' : 'cold');

export function PlinkoBoardView({ multipliers, ball, landedSlot, litRow }: PlinkoBoardProps) {
  const pegs: { x: number; y: number; row: number }[] = [];
  for (let row = 0; row < PLINKO_ROWS; row++) {
    const count = row + 3;
    for (let i = 0; i < count; i++) {
      pegs.push({ x: CX + (i - (count - 1) / 2) * BOARD.dx, y: BOARD.top + row * BOARD.dy, row });
    }
  }

  return (
    <svg className={styles.board} viewBox={`0 0 ${BOARD.width} ${BOARD.height}`} role="img" aria-label="Tablero de Plinko">
      <defs>
        <radialGradient id="plinkoBall" cx=".35" cy=".35">
          <stop offset="0" stopColor="#fff8dc" />
          <stop offset=".55" stopColor="#f0d48e" />
          <stop offset="1" stopColor="#8a6a2f" />
        </radialGradient>
      </defs>
      {pegs.map((peg, i) => (
        <circle key={i} cx={peg.x} cy={peg.y} r="4.5" className={`${styles.peg} ${peg.row === litRow ? styles.pegLit : ''}`} />
      ))}
      {multipliers.map((m, slot) => {
        const x = CX + (slot - PLINKO_ROWS / 2) * BOARD.dx;
        return (
          <g key={slot} className={`${styles.slot} ${styles[slotTone(m)]} ${landedSlot === slot ? styles.slotHit : ''}`}>
            <rect x={x - 18} y={slotY() - 2} width="36" height="30" rx="7" />
            <text x={x} y={slotY() + 18} textAnchor="middle">{m >= 100 ? Math.round(m) : m}</text>
          </g>
        );
      })}
      {ball && <circle cx={ball.x} cy={ball.y} r="10" fill="url(#plinkoBall)" className={styles.ball} />}
    </svg>
  );
}
