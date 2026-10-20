import type { ComponentType } from 'react';
import type { GameType } from '@/api/types';
import { BaccaratGame } from '@/features/baccarat/BaccaratGame';
import { BlackjackGame } from '@/features/blackjack/BlackjackGame';
import { CrashGame } from '@/features/crash/CrashGame';
import { DiceGame } from '@/features/dice/DiceGame';
import { KenoGame } from '@/features/keno/KenoGame';
import { MinesGame } from '@/features/mines/MinesGame';
import { PlinkoGame } from '@/features/plinko/PlinkoGame';
import { RouletteGame } from '@/features/roulette/RouletteGame';
import { SlotsGame } from '@/features/slots/SlotsGame';
import { VideoPokerGame } from '@/features/videopoker/VideoPokerGame';

export type GameCategory = 'Mesas' | 'Máquinas' | 'Instantáneos';

export interface GameDefinition {
  id: GameType;
  name: string;
  hint: string;
  glyph: string;
  category: GameCategory;
  component: ComponentType;
}

/**
 * Registro único de juegos (Open/Closed): agregar una mesa es sumar una entrada.
 * La navegación, el historial y el ruteo se derivan de aquí.
 */
export const GAMES: readonly GameDefinition[] = [
  { id: 'BLACKJACK', name: 'Blackjack', hint: 'Paga 3 a 2', glyph: '♠', category: 'Mesas', component: BlackjackGame },
  { id: 'ROULETTE', name: 'Ruleta', hint: 'Europea · un cero', glyph: '◎', category: 'Mesas', component: RouletteGame },
  { id: 'BACCARAT', name: 'Baccarat', hint: 'Punto Banco', glyph: '♦', category: 'Mesas', component: BaccaratGame },
  { id: 'VIDEO_POKER', name: 'Video póker', hint: 'Jacks or Better', glyph: '♣', category: 'Mesas', component: VideoPokerGame },
  { id: 'SLOTS', name: 'Tragamonedas', hint: '5 líneas · ×200', glyph: '7', category: 'Máquinas', component: SlotsGame },
  { id: 'KENO', name: 'Keno', hint: '20 de 80', glyph: '⁂', category: 'Máquinas', component: KenoGame },
  { id: 'CRASH', name: 'Crash', hint: 'Retirate a tiempo', glyph: '↗', category: 'Instantáneos', component: CrashGame },
  { id: 'MINES', name: 'Minas', hint: 'Gemas o explosión', glyph: '◆', category: 'Instantáneos', component: MinesGame },
  { id: 'PLINKO', name: 'Plinko', hint: 'Hasta ×170', glyph: '⋮', category: 'Instantáneos', component: PlinkoGame },
  { id: 'DICE', name: 'Dados', hint: 'Elegí tu riesgo', glyph: '⁙', category: 'Instantáneos', component: DiceGame },
];

export const GAME_CATEGORIES: readonly GameCategory[] = ['Mesas', 'Máquinas', 'Instantáneos'];

export const gameName = (id: GameType) => GAMES.find((g) => g.id === id)?.name ?? id;
