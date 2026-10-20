import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { casinoApi } from '@/api/casinoApi';
import { onUnauthorized, tokenStore } from '@/api/http';
import type { AuthResponse, Player } from '@/api/types';

type Status = 'loading' | 'anonymous' | 'authenticated';

interface SessionApi {
  status: Status;
  player: Player | null;
  /** Se incrementa con cada jugada liquidada: el historial lo usa para refrescarse. */
  roundsVersion: number;
  signIn: (mode: 'login' | 'register', username: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  /** Aplica el saldo devuelto por una jugada (se llama al terminar la animación). */
  settleRound: (balance: number) => void;
  /** Descuenta visualmente la apuesta mientras la jugada está en curso. */
  reserveStake: (stake: number) => void;
  /** Ajusta el saldo durante una mano abierta (p. ej. al doblar) sin cerrar la jugada. */
  syncBalance: (balance: number) => void;
  refill: () => Promise<void>;
}

const SessionContext = createContext<SessionApi | null>(null);

export function SessionProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>(tokenStore.get() ? 'loading' : 'anonymous');
  const [player, setPlayer] = useState<Player | null>(null);
  const [roundsVersion, setRoundsVersion] = useState(0);

  const clear = useCallback(() => {
    tokenStore.set(null);
    setPlayer(null);
    setStatus('anonymous');
  }, []);

  useEffect(() => onUnauthorized(clear), [clear]);

  useEffect(() => {
    if (!tokenStore.get()) return;
    casinoApi
      .me()
      .then((me) => {
        setPlayer(me);
        setStatus('authenticated');
      })
      .catch(clear);
  }, [clear]);

  const signIn = useCallback(async (mode: 'login' | 'register', username: string, password: string) => {
    const response: AuthResponse =
      mode === 'login' ? await casinoApi.login(username, password) : await casinoApi.register(username, password);
    tokenStore.set(response.token);
    setPlayer(response.player);
    setStatus('authenticated');
  }, []);

  const signOut = useCallback(async () => {
    await casinoApi.logout().catch(() => undefined);
    clear();
  }, [clear]);

  const settleRound = useCallback((balance: number) => {
    setPlayer((p) => (p ? { ...p, balance } : p));
    setRoundsVersion((v) => v + 1);
    // El backend es la fuente de verdad de reglas como la disponibilidad de recarga
    casinoApi.me().then(setPlayer).catch(() => undefined);
  }, []);

  const reserveStake = useCallback((stake: number) => {
    setPlayer((p) => (p ? { ...p, balance: p.balance - stake } : p));
  }, []);

  const syncBalance = useCallback((balance: number) => {
    setPlayer((p) => (p ? { ...p, balance } : p));
  }, []);

  const refill = useCallback(async () => {
    setPlayer(await casinoApi.refill());
  }, []);

  const api = useMemo(
    () => ({ status, player, roundsVersion, signIn, signOut, settleRound, reserveStake, syncBalance, refill }),
    [status, player, roundsVersion, signIn, signOut, settleRound, reserveStake, syncBalance, refill],
  );

  return <SessionContext.Provider value={api}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionApi {
  const ctx = useContext(SessionContext);
  if (!ctx) throw new Error('useSession debe usarse dentro de <SessionProvider>');
  return ctx;
}
