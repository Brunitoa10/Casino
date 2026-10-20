import type { SlotSymbol } from '@/api/types';

/** Ilustraciones vectoriales propias para cada símbolo del rodillo. */
const ICONS: Record<SlotSymbol, JSX.Element> = {
  CHERRY: (
    <>
      <path d="M32 10c-4 10-10 18-18 26M32 10c2 10 6 18 14 24" stroke="#5f8f3e" strokeWidth="3" fill="none" strokeLinecap="round" />
      <path d="M32 10c6-4 14-4 18 0-6 4-12 4-18 0z" fill="#6fae4a" />
      <circle cx="18" cy="44" r="11" fill="url(#cherry)" />
      <circle cx="44" cy="42" r="11" fill="url(#cherry)" />
      <circle cx="14" cy="40" r="3" fill="#fff" opacity=".55" />
      <circle cx="40" cy="38" r="3" fill="#fff" opacity=".55" />
    </>
  ),
  LEMON: (
    <>
      <ellipse cx="32" cy="34" rx="22" ry="16" fill="url(#lemon)" transform="rotate(-20 32 34)" />
      <path d="M50 22l5-4M14 46l-5 4" stroke="#d9b520" strokeWidth="4" strokeLinecap="round" />
      <ellipse cx="25" cy="28" rx="6" ry="3" fill="#fff" opacity=".45" transform="rotate(-20 25 28)" />
    </>
  ),
  BELL: (
    <>
      <path d="M32 8c-11 0-16 9-16 20v10l-6 8h44l-6-8V28c0-11-5-20-16-20z" fill="url(#gold)" />
      <circle cx="32" cy="51" r="5" fill="#a07c35" />
      <path d="M22 26c0-6 4-11 9-12" stroke="#fff6d8" strokeWidth="3" fill="none" strokeLinecap="round" opacity=".7" />
    </>
  ),
  CLOVER: (
    <>
      <circle cx="24" cy="24" r="10" fill="url(#clover)" />
      <circle cx="40" cy="24" r="10" fill="url(#clover)" />
      <circle cx="24" cy="40" r="10" fill="url(#clover)" />
      <circle cx="40" cy="40" r="10" fill="url(#clover)" />
      <path d="M32 34c2 10 6 16 12 22" stroke="#2c7a4f" strokeWidth="3" fill="none" strokeLinecap="round" />
    </>
  ),
  DIAMOND: (
    <>
      <path d="M14 22l8-10h20l8 10-18 32z" fill="url(#diamond)" />
      <path d="M14 22h36M22 12l10 10 10-10M32 22v32" stroke="#e8fbff" strokeWidth="1.5" fill="none" opacity=".7" />
    </>
  ),
  SEVEN: (
    <text x="32" y="52" textAnchor="middle" fontFamily="Cormorant Garamond, serif" fontWeight="700" fontSize="56"
      fill="url(#ruby)" stroke="#f0d48e" strokeWidth="2">7</text>
  ),
  CROWN: (
    <>
      <path d="M10 46l-2-26 14 12 10-18 10 18 14-12-2 26z" fill="url(#gold)" />
      <rect x="10" y="46" width="44" height="8" rx="2" fill="#a07c35" />
      <circle cx="32" cy="36" r="4" fill="#e04a62" />
      <circle cx="20" cy="40" r="3" fill="#5fd3e0" />
      <circle cx="44" cy="40" r="3" fill="#5fd3e0" />
    </>
  ),
};

export function SlotSymbolIcon({ symbol }: { symbol: SlotSymbol }) {
  return (
    <svg viewBox="0 0 64 64" width="100%" height="100%" aria-label={symbol} role="img">
      {ICONS[symbol]}
    </svg>
  );
}

/** Gradientes compartidos: se montan una sola vez por máquina. */
export function SlotGradients() {
  return (
    <svg width="0" height="0" style={{ position: 'absolute' }} aria-hidden="true">
      <defs>
        <radialGradient id="cherry" cx=".35" cy=".35"><stop offset="0" stopColor="#ff6b81" /><stop offset="1" stopColor="#8e1027" /></radialGradient>
        <linearGradient id="lemon" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stopColor="#fff27a" /><stop offset="1" stopColor="#e0b318" /></linearGradient>
        <linearGradient id="gold" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stopColor="#fbe7a8" /><stop offset=".5" stopColor="#d4ac5c" /><stop offset="1" stopColor="#8a6a2f" /></linearGradient>
        <radialGradient id="clover" cx=".35" cy=".35"><stop offset="0" stopColor="#7be3a6" /><stop offset="1" stopColor="#1c7a4a" /></radialGradient>
        <linearGradient id="diamond" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stopColor="#e8fbff" /><stop offset=".5" stopColor="#6fd6f0" /><stop offset="1" stopColor="#2a7fb8" /></linearGradient>
        <linearGradient id="ruby" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stopColor="#ff5a74" /><stop offset="1" stopColor="#8e1027" /></linearGradient>
      </defs>
    </svg>
  );
}
