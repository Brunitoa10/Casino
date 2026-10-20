package com.aurum.casino.game.core;

import java.time.Clock;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurum.casino.player.Player;
import com.aurum.casino.player.PlayerService;

/**
 * Flujo único y transaccional de cualquier apuesta (Template Method con el motor como paso variable):
 * validar → debitar → jugar → acreditar → registrar.
 * Agregar un juego nuevo no requiere tocar esta clase (OCP).
 */
@Service
public class BettingService {

    private final PlayerService playerService;
    private final GameRoundRepository rounds;
    private final RandomSource random;
    private final StakePolicy stakePolicy;
    private final Clock clock;

    public BettingService(PlayerService playerService, GameRoundRepository rounds,
                          RandomSource random, StakePolicy stakePolicy, Clock clock) {
        this.playerService = playerService;
        this.rounds = rounds;
        this.random = random;
        this.stakePolicy = stakePolicy;
        this.clock = clock;
    }

    @Transactional
    public <O extends GameOutcome> RoundResponse<O> play(Long playerId, GameType game, long stake,
                                                         GameEngine<O> engine) {
        stakePolicy.validate(stake);
        Player player = playerService.getById(playerId);

        player.debit(stake);
        O outcome = engine.play(random);
        player.credit(outcome.payout());

        GameRound round = rounds.save(new GameRound(player.getId(), game, stake, outcome.payout(),
                player.getBalance(), outcome.summary(), clock.instant()));

        return new RoundResponse<>(round.getId(), game, stake, outcome.payout(), player.getBalance(), outcome);
    }
}
