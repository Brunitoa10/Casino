import { useMemo } from 'react';
import { CRASH_GROWTH, multiplierAt } from './crashCurve';
import styles from './Crash.module.css';

interface CrashChartProps {
  elapsedMs: number;
  tone: 'idle' | 'flying' | 'busted' | 'cashed';
}

const W = 600;
const H = 300;
const SAMPLES = 60;
const GRID_STEPS = [1.5, 2, 3, 5, 10, 20, 50, 100, 250, 500, 1000];

/** Curva del vuelo con ejes que se reescalan a medida que el multiplicador sube. */
export function CrashChart({ elapsedMs, tone }: CrashChartProps) {
  const current = multiplierAt(elapsedMs);
  const xMax = Math.max(8000, elapsedMs * 1.15);
  const yMax = Math.max(2, current * 1.25);

  const { line, area, tip } = useMemo(() => {
    const x = (t: number) => (t / xMax) * W;
    const y = (m: number) => H - ((m - 1) / (yMax - 1)) * H;
    const pts: [number, number][] = [];
    for (let i = 0; i <= SAMPLES; i++) {
      const t = (elapsedMs * i) / SAMPLES;
      pts.push([x(t), y(Math.exp(CRASH_GROWTH * t))]);
    }
    const d = pts.map(([px, py], i) => `${i ? 'L' : 'M'}${px.toFixed(1)},${py.toFixed(1)}`).join(' ');
    return { line: d, area: `${d} L${pts[pts.length - 1][0].toFixed(1)},${H} L0,${H} Z`, tip: pts[pts.length - 1] };
  }, [elapsedMs, xMax, yMax]);

  const grid = GRID_STEPS.filter((m) => m < yMax).slice(-4);

  return (
    <svg className={`${styles.chart} ${styles[tone]}`} viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid meet" aria-hidden="true">
      <defs>
        <linearGradient id="crashArea" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="currentColor" stopOpacity="0.35" />
          <stop offset="1" stopColor="currentColor" stopOpacity="0" />
        </linearGradient>
      </defs>
      {grid.map((m) => {
        const gy = H - ((m - 1) / (yMax - 1)) * H;
        return (
          <g key={m}>
            <line x1="0" x2={W} y1={gy} y2={gy} className={styles.gridLine} />
            <text x={W - 6} y={gy - 6} textAnchor="end" className={styles.gridLabel}>×{m}</text>
          </g>
        );
      })}
      {elapsedMs > 0 && (
        <>
          <path d={area} fill="url(#crashArea)" />
          <path d={line} className={styles.curve} vectorEffect="non-scaling-stroke" />
          <circle cx={tip[0]} cy={tip[1]} r="7" className={styles.tip} />
        </>
      )}
    </svg>
  );
}
