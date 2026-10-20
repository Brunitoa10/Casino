import { useState, type FormEvent } from 'react';
import { ApiError } from '@/api/http';
import { Button } from '@/components/ui/Button';
import { useSession } from '@/context/SessionContext';
import styles from './AuthPage.module.css';

type Mode = 'login' | 'register';

export function AuthPage() {
  const { signIn } = useSession();
  const [mode, setMode] = useState<Mode>('login');
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await signIn(mode, username, password);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <section className={styles.hero}>
        <div className={styles.ring} aria-hidden="true">
          <div className={styles.ringInner} />
        </div>
        <span className="eyebrow">Private Club · Est. 2026</span>
        <h1 className={styles.title}>
          <span className="gold-text">Aurum</span>
        </h1>
        <p className={styles.lead}>
          Tragamonedas, ruleta europea y dados de precisión en una mesa diseñada para el detalle.
        </p>
        <ul className={styles.perks}>
          <li><strong>10.000</strong><span>créditos de bienvenida</span></li>
          <li><strong>97,3 %</strong><span>RTP en ruleta</span></li>
          <li><strong>3</strong><span>mesas abiertas</span></li>
        </ul>
      </section>

      <section className={styles.panel}>
        <form className={styles.card} onSubmit={submit}>
          <div className={styles.tabs} role="tablist">
            {(['login', 'register'] as const).map((m) => (
              <button
                key={m}
                type="button"
                role="tab"
                aria-selected={mode === m}
                className={mode === m ? styles.tabActive : ''}
                onClick={() => {
                  setMode(m);
                  setError(null);
                }}
              >
                {m === 'login' ? 'Ingresar' : 'Crear cuenta'}
              </button>
            ))}
          </div>

          <label className={styles.field}>
            <span>Usuario</span>
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoComplete="username"
              minLength={3}
              maxLength={20}
              pattern="[A-Za-z0-9_]{3,20}"
              title="3 a 20 caracteres: letras, números o guion bajo"
              required
            />
          </label>
          <label className={styles.field}>
            <span>Contraseña</span>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
              minLength={6}
              maxLength={72}
              required
            />
          </label>

          {error && <p className={styles.error}>{error}</p>}

          <Button type="submit" size="lg" loading={loading}>
            {mode === 'login' ? 'Entrar al salón' : 'Unirme al club'}
          </Button>
          <p className={styles.fine}>Juego de demostración con créditos ficticios. Sin dinero real.</p>
        </form>
      </section>
    </div>
  );
}
