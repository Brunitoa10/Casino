package com.aurum.casino.game.baccarat;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.BettingService;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RoundResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/games/baccarat")
public class BaccaratController {

    private final BettingService bettingService;
    private final BaccaratTable table;

    public BaccaratController(BettingService bettingService, BaccaratTable table) {
        this.bettingService = bettingService;
        this.table = table;
    }

    @PostMapping("/deal")
    public RoundResponse<BaccaratOutcome> deal(@CurrentPlayer Long playerId, @Valid @RequestBody BaccaratBets bets) {
        return bettingService.play(playerId, GameType.BACCARAT, bets.total(), random -> table.play(bets, random));
    }
}
