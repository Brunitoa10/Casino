package com.aurum.casino.game.slots;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.BettingService;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.core.RoundResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/slots")
public class SlotsController {

    private final BettingService bettingService;
    private final SlotMachine slotMachine;

    public SlotsController(BettingService bettingService, SlotMachine slotMachine) {
        this.bettingService = bettingService;
        this.slotMachine = slotMachine;
    }

    public record SpinRequest(@Positive long stake) {
    }

    @PostMapping("/spin")
    public RoundResponse<SlotOutcome> spin(@CurrentPlayer Long playerId, @Valid @RequestBody SpinRequest request) {
        return bettingService.play(playerId, GameType.SLOTS, request.stake(),
                random -> slotMachine.spin(request.stake(), random));
    }
}
