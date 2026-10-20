import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import type { SlotSymbol } from '@/api/types';
import { sound } from '@/lib/sound';
import { randomSymbol } from './slotsConfig';
import { SlotSymbolIcon } from './SlotSymbolIcon';
import styles from './Slots.module.css';

interface ReelProps {
  /** Los tres símbolos visibles finales, de arriba hacia abajo. */
  symbols: SlotSymbol[];
  /** Cambia en cada giro para disparar la animación. */
  spinId: number;
  durationMs: number;
  highlighted: ReadonlySet<number>;
}

const VISIBLE = 3;
const BLUR_CELLS = 18;

/**
 * Rodillo: arma una tira [visibles actuales, aleatorios…, finales] y la desplaza
 * con una transición CSS. Al terminar, la tira se compacta a los 3 finales.
 */
export function Reel({ symbols, spinId, durationMs, highlighted }: ReelProps) {
  const [strip, setStrip] = useState<SlotSymbol[]>(symbols);
  const [offset, setOffset] = useState(0);
  const [animating, setAnimating] = useState(false);
  const stripRef = useRef(strip);
  stripRef.current = strip;

  useLayoutEffect(() => {
    if (spinId === 0) return;
    const filler = Array.from({ length: BLUR_CELLS + Math.round(durationMs / 120) }, randomSymbol);
    const next = [...stripRef.current.slice(0, VISIBLE), ...filler, ...symbols];
    setAnimating(false);
    setOffset(0);
    setStrip(next);

    // Doble rAF: asegura que el navegador pinte la posición inicial antes de animar
    let raf2 = 0;
    const raf1 = requestAnimationFrame(() => {
      raf2 = requestAnimationFrame(() => {
        setAnimating(true);
        setOffset(((next.length - VISIBLE) / next.length) * 100);
      });
    });
    const stopTimer = setTimeout(() => sound.stop(), durationMs - 60);
    return () => {
      cancelAnimationFrame(raf1);
      cancelAnimationFrame(raf2);
      clearTimeout(stopTimer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [spinId]);

  useEffect(() => {
    if (!animating) return;
    const timer = setTimeout(() => {
      setAnimating(false);
      setOffset(0);
      setStrip(symbols);
    }, durationMs + 40);
    return () => clearTimeout(timer);
  }, [animating, durationMs, symbols]);

  return (
    <div className={styles.reel}>
      <div
        className={`${styles.strip} ${animating ? styles.spinning : ''}`}
        style={{
          transform: `translateY(-${offset}%)`,
          transition: animating ? `transform ${durationMs}ms cubic-bezier(0.15, 0.6, 0.25, 1.04)` : 'none',
        }}
      >
        {strip.map((symbol, index) => (
          <div
            key={index}
            className={`${styles.cell} ${!animating && highlighted.has(index) ? styles.winCell : ''}`}
          >
            <SlotSymbolIcon symbol={symbol} />
          </div>
        ))}
      </div>
    </div>
  );
}
