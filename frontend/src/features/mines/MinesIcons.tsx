/** Ilustraciones propias: gema facetada y mina. */
export function GemIcon() {
  return (
    <svg viewBox="0 0 64 64" width="62%" height="62%" aria-hidden="true">
      <defs>
        <linearGradient id="gemFill" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#b9ffe0" />
          <stop offset="0.5" stopColor="#2fd38f" />
          <stop offset="1" stopColor="#0b6b45" />
        </linearGradient>
      </defs>
      <path d="M12 24 22 10h20l10 14-20 30z" fill="url(#gemFill)" />
      <path d="M12 24h40M22 10l10 14 10-14M32 24v30M22 24l10 30 10-30" stroke="#e6fff4" strokeWidth="1.4" fill="none" opacity=".75" />
    </svg>
  );
}

export function BombIcon() {
  return (
    <svg viewBox="0 0 64 64" width="62%" height="62%" aria-hidden="true">
      <circle cx="30" cy="36" r="18" fill="#1a1b1f" stroke="#e04a62" strokeWidth="2" />
      <circle cx="24" cy="30" r="5" fill="#fff" opacity=".25" />
      <path d="M40 22l6-6" stroke="#c9a45c" strokeWidth="4" strokeLinecap="round" />
      <circle cx="49" cy="13" r="4" fill="#ffb347" />
    </svg>
  );
}
