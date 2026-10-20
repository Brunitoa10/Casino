import type { RouletteBet, RouletteBetType } from '@/api/types';

/** Orden de los bolsillos en una ruleta europea, en sentido horario desde el cero. */
export const WHEEL_ORDER = [
  0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22,
  18, 29, 7, 28, 12, 35, 3, 26,
] as const;

const RED = new Set([1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36]);

export type PocketColor = 'RED' | 'BLACK' | 'GREEN';

export const colorOf = (n: number): PocketColor => (n === 0 ? 'GREEN' : RED.has(n) ? 'RED' : 'BLACK');

export const betKey = (type: RouletteBetType, value?: number) => `${type}:${value ?? ''}`;

/** Pago "a 1" de cada tipo (espejo de RouletteBetType). */
export const PAYOUT_RATIO: Record<RouletteBetType, number> = {
  STRAIGHT: 35, RED: 1, BLACK: 1, ODD: 1, EVEN: 1, LOW: 1, HIGH: 1, DOZEN: 2, COLUMN: 2,
};

/** Regla de acierto en el cliente: solo para resaltar el paño, el resultado real lo decide el servidor. */
export function covers(bet: Pick<RouletteBet, 'type' | 'value'>, n: number): boolean {
  if (n === 0) return bet.type === 'STRAIGHT' && bet.value === 0;
  switch (bet.type) {
    case 'STRAIGHT': return bet.value === n;
    case 'RED': return RED.has(n);
    case 'BLACK': return !RED.has(n);
    case 'ODD': return n % 2 === 1;
    case 'EVEN': return n % 2 === 0;
    case 'LOW': return n <= 18;
    case 'HIGH': return n >= 19;
    case 'DOZEN': return Math.ceil(n / 12) === bet.value;
    case 'COLUMN': return ((n - 1) % 3) + 1 === bet.value;
  }
}
