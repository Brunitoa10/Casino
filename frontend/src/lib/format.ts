const credits = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 });

export const formatCredits = (value: number): string => credits.format(Math.round(value));

export const formatSigned = (value: number): string =>
  `${value > 0 ? '+' : value < 0 ? '−' : ''}${formatCredits(Math.abs(value))}`;

export const formatTime = (iso: string): string =>
  new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });

export const sleep = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms));
