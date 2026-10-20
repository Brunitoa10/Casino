package com.aurum.casino.game.keno;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.BettingService;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RoundResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/keno")
public class KenoController {

    private final BettingService bettingService;
    private final KenoGame keno;

    public KenoController(BettingService bettingService, KenoGame keno) {
        this.bettingService = bettingService;
        this.keno = keno;
    }

    public record PlayRequest(@Positive long stake, @NotEmpty List<Integer> picks) {
    }

    @PostMapping("/play")
    public RoundResponse<KenoOutcome> play(@CurrentPlayer Long playerId, @Valid @RequestBody PlayRequest request) {
        KenoGame.validate(request.picks());
        return bettingService.play(playerId, GameType.KENO, request.stake(),
                random -> keno.play(request.stake(), request.picks(), random));
    }
}
