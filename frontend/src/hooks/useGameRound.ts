import { useCallback, useState } from 'react';
import { ApiError } from '@/api/http';
import type { RoundResponse } from '@/api/types';
import { useSession } from '@/context/SessionContext';
import { useToast } from '@/context/ToastContext';

/**
 * Ciclo común de una jugada en el cliente:
 * reservar apuesta → pedir resultado → animar → liquidar saldo.
 * Cada juego solo aporta la llamada y su animación.
 */
export function useGameRound<O>() {
  const { player, reserveStake, settleRound } = useSession();
  const { notify } = useToast();
  const [busy, setBusy] = useState(false);

  const play = useCallback(
    async (
      stake: number,
      call: () => Promise<RoundResponse<O>>,
      animate: (round: RoundResponse<O>) => Promise<void>,
    ): Promise<RoundResponse<O> | null> => {
      if (busy || !player) return null;
      if (stake > player.balance) {
        notify('Saldo insuficiente para esta apuesta', 'error');
        return null;
      }
      setBusy(true);
      const balanceBefore = player.balance;
      reserveStake(stake);
      try {
        const round = await call();
        await animate(round);
        settleRound(round.balance);
        return round;
      } catch (error) {
        settleRound(balanceBefore);
        notify(error instanceof ApiError ? error.message : 'Algo salió mal', 'error');
        return null;
      } finally {
        setBusy(false);
      }
    },
    [busy, player, reserveStake, settleRound, notify],
  );

  return { busy, play };
}
