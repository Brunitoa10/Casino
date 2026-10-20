/** Misma curva que CrashCurve en el backend: m(t) = e^(k·t), t en milisegundos. */
export const CRASH_GROWTH = 0.00012;

export const multiplierAt = (elapsedMs: number) =>
  Math.floor(100 * Math.exp(CRASH_GROWTH * Math.max(0, elapsedMs))) / 100;

export const elapsedFor = (multiplier: number) => Math.log(Math.max(1, multiplier)) / CRASH_GROWTH;
