import { colorOf, WHEEL_ORDER } from '@/lib/roulette';
import styles from './Roulette.module.css';

interface RouletteWheelProps {
  wheelRotation: number;
  ballRotation: number;
  spinning: boolean;
  durationMs: number;
}

const STEP = 360 / WHEEL_ORDER.length;
const FILL = { RED: '#a3192f', BLACK: '#121414', GREEN: '#137a52' } as const;

const polar = (r: number, deg: number) => {
  const rad = ((deg - 90) * Math.PI) / 180;
  return [100 + r * Math.cos(rad), 100 + r * Math.sin(rad)] as const;
};

function pocketPath(index: number, outer: number, inner: number) {
  const a0 = index * STEP - STEP / 2;
  const a1 = a0 + STEP;
  const [x0, y0] = polar(outer, a0);
  const [x1, y1] = polar(outer, a1);
  const [x2, y2] = polar(inner, a1);
  const [x3, y3] = polar(inner, a0);
  return `M${x0},${y0} A${outer},${outer} 0 0 1 ${x1},${y1} L${x2},${y2} A${inner},${inner} 0 0 0 ${x3},${y3} Z`;
}

/** Ruleta vectorial: el plato gira en un sentido y la bola en el contrario. */
export function RouletteWheel({ wheelRotation, ballRotation, spinning, durationMs }: RouletteWheelProps) {
  return (
    <div className={styles.wheelWrap}>
      <div className={styles.pointer} aria-hidden="true" />
      <svg viewBox="0 0 200 200" className={styles.wheel} role="img" aria-label="Ruleta">
        <defs>
          <radialGradient id="rim" cx=".5" cy=".5" r=".5">
            <stop offset=".86" stopColor="#3b2a12" />
            <stop offset=".93" stopColor="#c9a45c" />
            <stop offset="1" stopColor="#5a4320" />
          </radialGradient>
          <radialGradient id="hub" cx=".4" cy=".35">
            <stop offset="0" stopColor="#f5dc9b" />
            <stop offset=".6" stopColor="#c9a45c" />
            <stop offset="1" stopColor="#6b5124" />
          </radialGradient>
        </defs>
        <circle cx="100" cy="100" r="99" fill="url(#rim)" />

        <g
          style={{
            transform: `rotate(${wheelRotation}deg)`,
            transformOrigin: '100px 100px',
            transition: spinning ? `transform ${durationMs}ms cubic-bezier(0.12, 0.6, 0.12, 1)` : 'none',
          }}
        >
          {WHEEL_ORDER.map((n, i) => (
            <g key={n}>
              <path d={pocketPath(i, 86, 62)} fill={FILL[colorOf(n)]} stroke="#c9a45c" strokeWidth="0.35" />
              <text
                x="100"
                y="22"
                transform={`rotate(${i * STEP} 100 100)`}
                textAnchor="middle"
                fontSize="7.5"
                fontWeight="700"
                fill="#f3ead6"
                fontFamily="Manrope, sans-serif"
              >
                {n}
              </text>
            </g>
          ))}
          <circle cx="100" cy="100" r="62" fill="#0c1c16" stroke="#c9a45c" strokeWidth="0.6" />
          {Array.from({ length: 8 }, (_, i) => (
            <line key={i} x1="100" y1="100" x2={polar(48, i * 45)[0]} y2={polar(48, i * 45)[1]} stroke="#a8874a" strokeWidth="2.4" strokeLinecap="round" />
          ))}
          <circle cx="100" cy="100" r="16" fill="url(#hub)" />
        </g>

        <g
          style={{
            transform: `rotate(${ballRotation}deg)`,
            transformOrigin: '100px 100px',
            transition: spinning ? `transform ${durationMs}ms cubic-bezier(0.2, 0.55, 0.15, 1)` : 'none',
          }}
        >
          <circle cx="100" cy="32" r="4.2" fill="#fbfaf6" className={styles.ball} />
        </g>
      </svg>
    </div>
  );
}

/** Rotación final del plato para que el número {@code n} quede bajo el puntero. */
export function wheelRotationFor(n: number, current: number, turns = 6): number {
  const index = WHEEL_ORDER.indexOf(n as (typeof WHEEL_ORDER)[number]);
  const desired = (((-index * STEP) % 360) + 360) % 360;
  const currentMod = ((current % 360) + 360) % 360;
  const delta = (desired - currentMod + 360) % 360;
  return current + turns * 360 + delta;
}

/** La bola gira en sentido contrario y termina arriba (0°). */
export function ballRotationFor(current: number, turns = 4): number {
  const currentMod = ((current % 360) + 360) % 360;
  return current - turns * 360 - currentMod;
}
