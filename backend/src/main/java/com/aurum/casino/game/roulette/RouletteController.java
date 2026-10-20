package com.aurum.casino.game.roulette;

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

@RestController
@RequestMapping("/api/games/roulette")
public class RouletteController {

    private final BettingService bettingService;
    private final RouletteWheel wheel;

    public RouletteController(BettingService bettingService, RouletteWheel wheel) {
        this.bettingService = bettingService;
        this.wheel = wheel;
    }

    public record SpinRequest(@NotEmpty List<@Valid RouletteBet> bets) {
    }

    @PostMapping("/spin")
    public RoundResponse<RouletteOutcome> spin(@CurrentPlayer Long playerId,
                                               @Valid @RequestBody SpinRequest request) {
        List<RouletteBet> bets = List.copyOf(request.bets());
        return bettingService.play(playerId, GameType.ROULETTE, RouletteWheel.totalStake(bets),
                random -> wheel.spin(bets, random));
    }
}
