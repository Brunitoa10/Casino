import { request } from './http';
import type {
  AuthResponse, BaccaratBets, BaccaratOutcome, BlackjackAction, BlackjackView, CrashView, DiceDirection,
  DiceOutcome, KenoOutcome, MinesView, Player, PlayerStats, PlinkoOutcome, PlinkoRisk, RoundResponse,
  RoundSummary, RouletteBet, RouletteOutcome, SessionResponse, SlotOutcome, VideoPokerView,
} from './types';

/** Endpoints comunes de un juego con estado: consultar la mano activa (204 = ninguna). */
const activeSession = <V>(path: string) => () =>
  request<SessionResponse<V> | undefined>(`/api/games/${path}/active`);

const post = <T>(path: string, body?: unknown) => request<T>(`/api/games/${path}`, { method: 'POST', body });

/** Fachada única de acceso al backend: los componentes nunca usan fetch directamente. */
export const casinoApi = {
  register: (username: string, password: string) =>
    request<AuthResponse>('/api/auth/register', { method: 'POST', body: { username, password } }),

  login: (username: string, password: string) =>
    request<AuthResponse>('/api/auth/login', { method: 'POST', body: { username, password } }),

  logout: () => request<void>('/api/auth/logout', { method: 'POST' }),

  me: () => request<Player>('/api/me'),

  refill: () => request<Player>('/api/wallet/refill', { method: 'POST' }),

  recentRounds: (limit = 15) => request<RoundSummary[]>(`/api/rounds?limit=${limit}`),

  stats: () => request<PlayerStats>('/api/rounds/stats'),

  spinSlots: (stake: number) =>
    request<RoundResponse<SlotOutcome>>('/api/games/slots/spin', { method: 'POST', body: { stake } }),

  spinRoulette: (bets: RouletteBet[]) =>
    request<RoundResponse<RouletteOutcome>>('/api/games/roulette/spin', { method: 'POST', body: { bets } }),

  rollDice: (stake: number, target: number, direction: DiceDirection) =>
    request<RoundResponse<DiceOutcome>>('/api/games/dice/roll', {
      method: 'POST',
      body: { stake, target, direction },
    }),

  baccarat: (bets: BaccaratBets) => post<RoundResponse<BaccaratOutcome>>('baccarat/deal', bets),

  keno: (stake: number, picks: number[]) => post<RoundResponse<KenoOutcome>>('keno/play', { stake, picks }),

  plinko: (stake: number, risk: PlinkoRisk) => post<RoundResponse<PlinkoOutcome>>('plinko/drop', { stake, risk }),

  blackjack: {
    active: activeSession<BlackjackView>('blackjack'),
    deal: (stake: number) => post<SessionResponse<BlackjackView>>('blackjack/deal', { stake }),
    act: (action: BlackjackAction) => post<SessionResponse<BlackjackView>>('blackjack/action', { action }),
  },

  videoPoker: {
    active: activeSession<VideoPokerView>('video-poker'),
    deal: (stake: number) => post<SessionResponse<VideoPokerView>>('video-poker/deal', { stake }),
    draw: (held: number[]) => post<SessionResponse<VideoPokerView>>('video-poker/draw', { held }),
  },

  mines: {
    active: activeSession<MinesView>('mines'),
    start: (stake: number, mines: number) => post<SessionResponse<MinesView>>('mines/start', { stake, mines }),
    reveal: (tile: number) => post<SessionResponse<MinesView>>('mines/reveal', { tile }),
    cashout: () => post<SessionResponse<MinesView>>('mines/cashout'),
  },

  crash: {
    active: activeSession<CrashView>('crash'),
    start: (stake: number, autoCashout: number | null) =>
      post<SessionResponse<CrashView>>('crash/start', { stake, autoCashout }),
    tick: () => post<SessionResponse<CrashView>>('crash/tick'),
    cashout: () => post<SessionResponse<CrashView>>('crash/cashout'),
  },
};
