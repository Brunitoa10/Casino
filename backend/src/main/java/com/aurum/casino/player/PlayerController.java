package com.aurum.casino.player;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;

@RestController
@RequestMapping("/api")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/me")
    public PlayerResponse me(@CurrentPlayer Long playerId) {
        return playerService.describe(playerId);
    }

    @PostMapping("/wallet/refill")
    public PlayerResponse refill(@CurrentPlayer Long playerId) {
        return playerService.refill(playerId);
    }
}
