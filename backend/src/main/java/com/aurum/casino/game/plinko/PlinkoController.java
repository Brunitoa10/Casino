package com.aurum.casino.game.plinko;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.BettingService;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RoundResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/plinko")
public class PlinkoController {

    private final BettingService bettingService;
    private final PlinkoBoard board;

    public PlinkoController(BettingService bettingService, PlinkoBoard board) {
        this.bettingService = bettingService;
        this.board = board;
    }

    public record DropRequest(@Positive long stake, @NotNull PlinkoRisk risk) {
    }

    @PostMapping("/drop")
    public RoundResponse<PlinkoOutcome> drop(@CurrentPlayer Long playerId, @Valid @RequestBody DropRequest request) {
        return bettingService.play(playerId, GameType.PLINKO, request.stake(),
                random -> board.drop(request.stake(), request.risk(), random));
    }
}
