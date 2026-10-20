package com.aurum.casino.game.core;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;

@RestController
@RequestMapping("/api/rounds")
public class RoundHistoryController {

    private static final int MAX_LIMIT = 100;

    private final GameRoundRepository rounds;

    public RoundHistoryController(GameRoundRepository rounds) {
        this.rounds = rounds;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<RoundSummaryResponse> recent(@CurrentPlayer Long playerId,
                                             @RequestParam(defaultValue = "20") int limit) {
        int size = Math.max(1, Math.min(limit, MAX_LIMIT));
        return rounds.findByPlayerIdOrderByIdDesc(playerId, PageRequest.of(0, size)).stream()
                .map(RoundSummaryResponse::from)
                .toList();
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public PlayerStats stats(@CurrentPlayer Long playerId) {
        return rounds.statsFor(playerId);
    }
}
