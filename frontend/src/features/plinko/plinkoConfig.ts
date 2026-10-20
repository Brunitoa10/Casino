import type { PlinkoRisk } from '@/api/types';

export const PLINKO_ROWS = 12;

const HALF: Record<PlinkoRisk, number[]> = {
  LOW: [10, 3, 1.6, 1.4, 1.1, 1, 0.5],
  MEDIUM: [33, 11, 4, 2, 1.1, 0.6, 0.3],
  HIGH: [170, 24, 8.1, 2, 0.7, 0.2, 0.2],
};

/** Multiplicadores de las 13 casillas (espejo de PlinkoRisk en el backend). */
export const slotMultipliers = (risk: PlinkoRisk): number[] =>
  Array.from({ length: PLINKO_ROWS + 1 }, (_, slot) => HALF[risk][Math.min(slot, PLINKO_ROWS - slot)]);

export const RISK_LABEL: Record<PlinkoRisk, string> = { LOW: 'Bajo', MEDIUM: 'Medio', HIGH: 'Alto' };
