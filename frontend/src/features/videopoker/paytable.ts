import type { PokerHand } from '@/api/types';

/** Espejo de PokerHand en el backend (Jacks or Better 9/6, pago "por 1"). */
export const PAYTABLE: readonly { hand: PokerHand; label: string; multiplier: number }[] = [
  { hand: 'ROYAL_FLUSH', label: 'Escalera real', multiplier: 800 },
  { hand: 'STRAIGHT_FLUSH', label: 'Escalera de color', multiplier: 50 },
  { hand: 'FOUR_OF_A_KIND', label: 'Póker', multiplier: 25 },
  { hand: 'FULL_HOUSE', label: 'Full', multiplier: 9 },
  { hand: 'FLUSH', label: 'Color', multiplier: 6 },
  { hand: 'STRAIGHT', label: 'Escalera', multiplier: 4 },
  { hand: 'THREE_OF_A_KIND', label: 'Trío', multiplier: 3 },
  { hand: 'TWO_PAIR', label: 'Doble par', multiplier: 2 },
  { hand: 'JACKS_OR_BETTER', label: 'Par de J o mejor', multiplier: 1 },
];
