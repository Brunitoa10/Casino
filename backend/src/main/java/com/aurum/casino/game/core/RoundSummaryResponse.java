package com.aurum.casino.game.core;

import java.time.Instant;

public record RoundSummaryResponse(Long id, GameType game, long stake, long payout,
                                   String summary, Instant createdAt) {

    public static RoundSummaryResponse from(GameRound round) {
        return new RoundSummaryResponse(round.getId(), round.getGame(), round.getStake(),
                round.getPayout(), round.getSummary(), round.getCreatedAt());
    }
}
