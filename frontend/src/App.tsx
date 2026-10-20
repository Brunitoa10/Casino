import { useState } from 'react';
import type { GameType } from '@/api/types';
import { AppShell } from '@/components/layout/AppShell';
import { GameNav } from '@/components/layout/GameNav';
import { useSession } from '@/context/SessionContext';
import { AuthPage } from '@/features/auth/AuthPage';
import { GAMES } from '@/games/registry';

const GAME_KEY = 'aurum.game';

function readSavedGame(): GameType {
  try {
    const saved = localStorage.getItem(GAME_KEY);
    return GAMES.some((g) => g.id === saved) ? (saved as GameType) : 'BLACKJACK';
  } catch {
    return 'BLACKJACK';
  }
}

export function App() {
  const { status } = useSession();
  const [active, setActive] = useState<GameType>(readSavedGame);

  if (status === 'loading') {
    return <div style={{ display: 'grid', placeItems: 'center', minHeight: '100vh' }} className="eyebrow">Cargando salón…</div>;
  }
  if (status === 'anonymous') return <AuthPage />;

  const select = (id: GameType) => {
    setActive(id);
    window.scrollTo({ top: 0, behavior: 'smooth' });
    try {
      localStorage.setItem(GAME_KEY, id);
    } catch {
      /* sin almacenamiento */
    }
  };

  const Game = GAMES.find((g) => g.id === active)!.component;

  return (
    <AppShell nav={<GameNav games={GAMES} active={active} onSelect={select} />}>
      <Game key={active} />
    </AppShell>
  );
}
