import { useSession } from '@/context/SessionContext';
import { useToast } from '@/context/ToastContext';
import { useAnimatedNumber } from '@/hooks/useAnimatedNumber';
import { useSoundToggle } from '@/hooks/useSound';
import { formatCredits } from '@/lib/format';
import { Button } from '@/components/ui/Button';
import styles from './Layout.module.css';

export function TopBar() {
  const { player, signOut, refill } = useSession();
  const { notify } = useToast();
  const { muted, toggle } = useSoundToggle();
  const balance = useAnimatedNumber(player?.balance ?? 0, 900);

  const handleRefill = async () => {
    try {
      await refill();
      notify('Créditos de cortesía acreditados', 'success');
    } catch {
      notify('La recarga no está disponible', 'error');
    }
  };

  return (
    <header className={styles.topbar}>
      <div className={styles.brand}>
        <span className={styles.monogram}>A</span>
        <span className={styles.wordmark}>AURUM</span>
        <span className={styles.tagline}>Private Club</span>
      </div>

      <div className={styles.account}>
        {player?.refillAvailable && (
          <Button variant="ghost" onClick={handleRefill}>Recargar</Button>
        )}
        <div className={styles.balance}>
          <span className="eyebrow">Saldo</span>
          <strong className="gold-text">{formatCredits(balance)}</strong>
        </div>
        <button className={styles.iconButton} onClick={toggle} aria-label={muted ? 'Activar sonido' : 'Silenciar'}>
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <path d="M11 5 6 9H3v6h3l5 4V5z" />
            {muted ? <path d="m17 9 5 6m0-6-5 6" /> : <path d="M15.5 8.5a5 5 0 0 1 0 7M18.5 5.5a9 9 0 0 1 0 13" />}
          </svg>
        </button>
        <div className={styles.user}>
          <span className={styles.avatar}>{player?.username.charAt(0).toUpperCase()}</span>
          <span className={styles.username}>{player?.username}</span>
          <button className={styles.logout} onClick={signOut}>Salir</button>
        </div>
      </div>
    </header>
  );
}
