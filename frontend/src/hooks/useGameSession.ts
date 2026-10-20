import { useCallback, useEffect, useState } from 'react';
import { ApiError } from '@/api/http';
import type { SessionResponse } from '@/api/types';
import { useSession } from '@/context/SessionContext';
import { useToast } from '@/context/ToastContext';

/**
 * Estado de cliente para juegos de varias jugadas (blackjack, minas, crash, video póker):
 * retoma la mano abierta al entrar, serializa las acciones y mantiene el saldo sincronizado.
 */
export function useGameSession<V>(loadActive: () => Promise<SessionResponse<V> | undefined>) {
  const { syncBalance, settleRound } = useSession();
  const { notify } = useToast();
  const [session, setSession] = useState<SessionResponse<V> | null>(null);
  const [busy, setBusy] = useState(false);

  const resume = useCallback(async () => {
    try {
      const active = await loadActive();
      if (active) setSession(active);
    } catch {
      /* sin mano abierta */
    }
  }, [loadActive]);

  useEffect(() => {
    void resume();
  }, [resume]);

  const apply = useCallback(
    (next: SessionResponse<V>) => {
      setSession(next);
      if (next.finished) settleRound(next.balance);
      else syncBalance(next.balance);
    },
    [settleRound, syncBalance],
  );

  /**
   * Ejecuta una acción. {@code beforeApply} permite animar antes de mostrar el resultado final
   * (por ejemplo, revelar cartas una a una).
   */
  const run = useCallback(
    async (call: () => Promise<SessionResponse<V>>): Promise<SessionResponse<V> | null> => {
      if (busy) return null;
      setBusy(true);
      try {
        const next = await call();
        apply(next);
        return next;
      } catch (error) {
        if (error instanceof ApiError && error.code === 'ACTIVE_SESSION') {
          await resume();
          notify('Retomamos tu mano en curso', 'info');
        } else {
          notify(error instanceof ApiError ? error.message : 'Algo salió mal', 'error');
        }
        return null;
      } finally {
        setBusy(false);
      }
    },
    [busy, apply, resume, notify],
  );

  const isActive = session !== null && !session.finished;

  return { session, isActive, busy, run, apply, clear: () => setSession(null) };
}
