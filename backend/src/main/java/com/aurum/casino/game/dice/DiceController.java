package com.aurum.casino.game.dice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.BettingService;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RoundResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/dice")
public class DiceController {

    private final BettingService bettingService;
    private final DiceGame diceGame;

    public DiceController(BettingService bettingService, DiceGame diceGame) {
        this.bettingService = bettingService;
        this.diceGame = diceGame;
    }

    public record RollRequest(
            @Positive long stake,
            @Min(DiceGame.MIN_TARGET) @Max(DiceGame.MAX_TARGET) int target,
            @NotNull DiceDirection direction) {
    }

    @PostMapping("/roll")
    public RoundResponse<DiceOutcome> roll(@CurrentPlayer Long playerId, @Valid @RequestBody RollRequest request) {
        return bettingService.play(playerId, GameType.DICE, request.stake(),
                random -> diceGame.roll(request.stake(), request.target(), request.direction(), random));
    }
}
