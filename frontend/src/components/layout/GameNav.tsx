import type { GameType } from '@/api/types';
import { GAME_CATEGORIES, type GameDefinition } from '@/games/registry';
import styles from './Layout.module.css';

interface GameNavProps {
  games: readonly GameDefinition[];
  active: GameType;
  onSelect: (id: GameType) => void;
}

export function GameNav({ games, active, onSelect }: GameNavProps) {
  return (
    <nav className={styles.nav} aria-label="Juegos">
      {GAME_CATEGORIES.map((category) => (
        <div key={category} className={styles.navGroup}>
          <span className={styles.navCategory}>{category}</span>
          {games.filter((g) => g.category === category).map((game) => (
            <button
              key={game.id}
              className={`${styles.navItem} ${active === game.id ? styles.navActive : ''}`}
              onClick={() => onSelect(game.id)}
              aria-current={active === game.id}
            >
              <span className={styles.navGlyph}>{game.glyph}</span>
              <span className={styles.navText}>
                <strong>{game.name}</strong>
                <small>{game.hint}</small>
              </span>
            </button>
          ))}
        </div>
      ))}
    </nav>
  );
}
