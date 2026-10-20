package com.aurum.casino.game.session;

import java.time.Clock;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurum.casino.game.core.GameRound;
import com.aurum.casino.game.core.GameRoundRepository;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RandomSource;
import com.aurum.casino.game.core.StakePolicy;
import com.aurum.casino.player.Player;
import com.aurum.casino.player.PlayerService;
import com.aurum.casino.shared.error.DomainException;
import com.aurum.casino.shared.error.ErrorCode;

/**
 * Ciclo de vida de los juegos con varias jugadas:
 * abrir (debitar apuesta) → acciones (debitar aumentos como doblar/dividir) → liquidar.
 * Los motores solo transforman su estado; saldo, persistencia e historial viven aquí.
 */
@Service
public class SessionService {

    private final GameSessionRepository sessions;
    private final GameRoundRepository rounds;
    private final PlayerService playerService;
    private final StakePolicy stakePolicy;
    private final GameStateCodec codec;
    private final RandomSource random;
    private final Clock clock;

    public SessionService(GameSessionRepository sessions, GameRoundRepository rounds, PlayerService playerService,
                          StakePolicy stakePolicy, GameStateCodec codec, RandomSource random, Clock clock) {
        this.sessions = sessions;
        this.rounds = rounds;
        this.playerService = playerService;
        this.stakePolicy = stakePolicy;
        this.codec = codec;
        this.random = random;
        this.clock = clock;
    }

    @Transactional
    public <V, S extends SessionState<V>> SessionResponse<V> start(Long playerId, GameType game,
                                                                  Function<ActionContext, S> factory) {
        if (sessions.findFirstByPlayerIdAndGameAndStatus(playerId, game, SessionStatus.ACTIVE).isPresent()) {
            throw new DomainException(ErrorCode.ACTIVE_SESSION, "Ya tenés una mano en curso en este juego");
        }
        S state = factory.apply(context());
        stakePolicy.validate(state.totalStake());

        Player player = playerService.getById(playerId);
        player.debit(state.totalStake());

        GameSession session = sessions.save(new GameSession(playerId, game, state.totalStake(),
                codec.write(state), clock.instant()));
        return settleIfFinished(player, session, state);
    }

    @Transactional
    public <V, S extends SessionState<V>> SessionResponse<V> act(Long playerId, GameType game, Class<S> type,
                                                                BiConsumer<S, ActionContext> action) {
        GameSession session = activeSession(playerId, game);
        S state = codec.read(session.getState(), type);
        action.accept(state, context());

        Player player = playerService.getById(playerId);
        long increase = state.totalStake() - session.getCharged();
        if (increase > 0) {
            player.debit(increase);
        }
        session.update(codec.write(state), state.totalStake(), clock.instant());
        return settleIfFinished(player, session, state);
    }

    @Transactional(readOnly = true)
    public <V, S extends SessionState<V>> Optional<SessionResponse<V>> active(Long playerId, GameType game,
                                                                             Class<S> type) {
        return sessions.findFirstByPlayerIdAndGameAndStatus(playerId, game, SessionStatus.ACTIVE)
                .map(session -> {
                    S state = codec.read(session.getState(), type);
                    long balance = playerService.getById(playerId).getBalance();
                    return response(session, state, balance);
                });
    }

    private <V, S extends SessionState<V>> SessionResponse<V> settleIfFinished(Player player, GameSession session,
                                                                              S state) {
        if (state.isFinished()) {
            player.credit(state.payout());
            session.finish();
            rounds.save(new GameRound(player.getId(), session.getGame(), state.totalStake(), state.payout(),
                    player.getBalance(), state.summary(), clock.instant()));
        }
        return response(session, state, player.getBalance());
    }

    private static <V> SessionResponse<V> response(GameSession session, SessionState<V> state, long balance) {
        return new SessionResponse<>(session.getId(), session.getGame(), state.isFinished(), state.totalStake(),
                state.isFinished() ? state.payout() : 0L, balance, state.view());
    }

    private GameSession activeSession(Long playerId, GameType game) {
        return sessions.findFirstByPlayerIdAndGameAndStatus(playerId, game, SessionStatus.ACTIVE)
                .orElseThrow(() -> new DomainException(ErrorCode.NO_ACTIVE_SESSION, "No hay una mano en curso"));
    }

    private ActionContext context() {
        return new ActionContext(random, clock.instant());
    }
}
