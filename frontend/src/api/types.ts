// Contratos de la API (espejo de los records del backend)

export type GameType =
  | 'SLOTS' | 'ROULETTE' | 'DICE'
  | 'BLACKJACK' | 'VIDEO_POKER' | 'BACCARAT' | 'KENO'
  | 'CRASH' | 'MINES' | 'PLINKO';

export interface Player {
  id: number;
  username: string;
  balance: number;
  refillAvailable: boolean;
}

export interface AuthResponse {
  token: string;
  player: Player;
}

export interface RoundResponse<O> {
  roundId: number;
  game: GameType;
  stake: number;
  payout: number;
  balance: number;
  outcome: O;
}

export interface RoundSummary {
  id: number;
  game: GameType;
  stake: number;
  payout: number;
  summary: string;
  createdAt: string;
}

export interface PlayerStats {
  rounds: number;
  wagered: number;
  won: number;
  biggestWin: number;
  net: number;
}

// ── Tragamonedas
export type SlotSymbol = 'CHERRY' | 'LEMON' | 'BELL' | 'CLOVER' | 'DIAMOND' | 'SEVEN' | 'CROWN';

export interface WinningLine {
  line: number;
  symbol: SlotSymbol;
  multiplier: number;
  amount: number;
}

export interface SlotOutcome {
  reels: SlotSymbol[][];
  wins: WinningLine[];
  payout: number;
}

// ── Ruleta
export type RouletteBetType =
  | 'STRAIGHT' | 'RED' | 'BLACK' | 'ODD' | 'EVEN' | 'LOW' | 'HIGH' | 'DOZEN' | 'COLUMN';

export interface RouletteBet {
  type: RouletteBetType;
  value?: number;
  amount: number;
}

export interface RouletteBetResult extends RouletteBet {
  won: boolean;
  payout: number;
}

export interface RouletteOutcome {
  number: number;
  color: 'RED' | 'BLACK' | 'GREEN';
  bets: RouletteBetResult[];
  payout: number;
}

// ── Dados
export type DiceDirection = 'UNDER' | 'OVER';

export interface DiceOutcome {
  roll: number;
  target: number;
  direction: DiceDirection;
  winChance: number;
  multiplier: number;
  won: boolean;
  payout: number;
}

// ── Juegos con estado (varias jugadas por mano)
export interface SessionResponse<V> {
  sessionId: number;
  game: GameType;
  finished: boolean;
  stake: number;
  payout: number;
  balance: number;
  state: V;
}

/** Código de carta de 2 caracteres ("AS", "TD") o "??" si está oculta. */
export type CardCode = string;

// ── Blackjack
export type BlackjackAction = 'HIT' | 'STAND' | 'DOUBLE' | 'SPLIT';
export type HandOutcome = 'PENDING' | 'BLACKJACK' | 'WIN' | 'PUSH' | 'LOSE' | 'BUST';

export interface BlackjackHandView {
  cards: CardCode[];
  total: number;
  soft: boolean;
  stake: number;
  doubled: boolean;
  outcome: HandOutcome;
  payout: number;
}

export interface BlackjackView {
  hands: BlackjackHandView[];
  activeHand: number;
  dealerCards: CardCode[];
  dealerTotal: number;
  finished: boolean;
  canHit: boolean;
  canStand: boolean;
  canDouble: boolean;
  canSplit: boolean;
}

// ── Video póker
export type PokerHand =
  | 'ROYAL_FLUSH' | 'STRAIGHT_FLUSH' | 'FOUR_OF_A_KIND' | 'FULL_HOUSE' | 'FLUSH'
  | 'STRAIGHT' | 'THREE_OF_A_KIND' | 'TWO_PAIR' | 'JACKS_OR_BETTER' | 'NOTHING';

export interface VideoPokerView {
  cards: CardCode[];
  held: boolean[];
  hand: PokerHand;
  handLabel: string;
  finished: boolean;
  payout: number;
}

// ── Minas
export interface MinesView {
  mines: number;
  revealed: number[];
  multiplier: number;
  nextMultiplier: number;
  finished: boolean;
  busted: boolean;
  hitMine: number | null;
  minePositions: number[];
  payout: number;
}

// ── Crash
export interface CrashView {
  elapsedMs: number;
  multiplier: number;
  autoCashout: number | null;
  finished: boolean;
  busted: boolean;
  cashedOutAt: number | null;
  crashPoint: number | null;
  payout: number;
}

// ── Baccarat
export type BaccaratSide = 'PLAYER' | 'BANKER' | 'TIE';

export interface BaccaratBets {
  player: number;
  banker: number;
  tie: number;
}

export interface BaccaratOutcome {
  playerCards: CardCode[];
  bankerCards: CardCode[];
  playerTotal: number;
  bankerTotal: number;
  winner: BaccaratSide;
  payout: number;
}

// ── Keno
export interface KenoOutcome {
  picks: number[];
  drawn: number[];
  hits: number[];
  multiplier: number;
  payout: number;
}

// ── Plinko
export type PlinkoRisk = 'LOW' | 'MEDIUM' | 'HIGH';

export interface PlinkoOutcome {
  risk: PlinkoRisk;
  path: number[];
  slot: number;
  multiplier: number;
  payout: number;
}
