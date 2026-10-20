/** Espejo de KenoPaytable (multiplicadores por aciertos, índice = aciertos). */
export const KENO_PAYTABLE: Record<number, number[]> = {
  1: [0, 3.8],
  2: [0, 1, 9.5],
  3: [0, 0, 2, 48],
  4: [0, 0, 1.6, 7, 95],
  5: [0, 0, 1.2, 3, 15, 300],
  6: [0, 0, 0, 3, 9, 55, 1000],
  7: [0, 0, 0, 1.5, 5, 25, 200, 2500],
  8: [0, 0, 0, 1, 3, 12, 65, 500, 5000],
  9: [0, 0, 0, 1, 2, 5, 28, 150, 1500, 10000],
  10: [0, 0, 0, 0, 2, 5, 15, 80, 500, 4000, 10000],
};

export const KENO_NUMBERS = 80;
export const KENO_MAX_PICKS = 10;
