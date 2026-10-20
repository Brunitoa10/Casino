import type { SlotSymbol } from '@/api/types';

export const SYMBOLS: readonly SlotSymbol[] = ['CHERRY', 'LEMON', 'BELL', 'CLOVER', 'DIAMOND', 'SEVEN', 'CROWN'];

/** Multiplicador por línea (espejo de SlotSymbol en el backend). */
export const LINE_MULTIPLIER: Record<SlotSymbol, number> = {
  CHERRY: 11, LEMON: 17, BELL: 30, CLOVER: 60, DIAMOND: 120, SEVEN: 300, CROWN: 1000,
};

export const PAYLINE_COUNT = 5;

/** Fila (0-2) que recorre cada línea de pago en cada rodillo. */
export const PAYLINES: readonly (readonly [number, number, number])[] = [
  [1, 1, 1], [0, 0, 0], [2, 2, 2], [0, 1, 2], [2, 1, 0],
];

export const SPIN_BASE_MS = 1100;
export const SPIN_STAGGER_MS = 380;

export const randomSymbol = (): SlotSymbol => SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
