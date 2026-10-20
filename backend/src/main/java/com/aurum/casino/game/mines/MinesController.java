package com.aurum.casino.game.mines;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aurum.casino.auth.CurrentPlayer;
import com.aurum.casino.game.core.GameType;
import com.aurum.casino.game.session.SessionResponse;
import com.aurum.casino.game.session.SessionResponses;
import com.aurum.casino.game.session.SessionService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/mines")
public class MinesController {

    private final SessionService sessions;

    public MinesController(SessionService sessions) {
        this.sessions = sessions;
    }

    public record StartRequest(@Positive long stake,
                               @Min(MinesState.MIN_MINES) @Max(MinesState.MAX_MINES) int mines) {
    }

    public record RevealRequest(@Min(0) @Max(MinesState.TILES - 1) int tile) {
    }

    @PostMapping("/start")
    public SessionResponse<MinesView> start(@CurrentPlayer Long playerId, @Valid @RequestBody StartRequest request) {
        return sessions.start(playerId, GameType.MINES,
                ctx -> MinesState.start(request.stake(), request.mines(), ctx.random()));
    }

    @PostMapping("/reveal")
    public SessionResponse<MinesView> reveal(@CurrentPlayer Long playerId, @Valid @RequestBody RevealRequest request) {
        return sessions.act(playerId, GameType.MINES, MinesState.class, (state, ctx) -> state.reveal(request.tile()));
    }

    @PostMapping("/cashout")
    public SessionResponse<MinesView> cashout(@CurrentPlayer Long playerId) {
        return sessions.act(playerId, GameType.MINES, MinesState.class, (state, ctx) -> state.cashout());
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse<MinesView>> active(@CurrentPlayer Long playerId) {
        return SessionResponses.okOrNoContent(sessions.active(playerId, GameType.MINES, MinesState.class));
    }
}
