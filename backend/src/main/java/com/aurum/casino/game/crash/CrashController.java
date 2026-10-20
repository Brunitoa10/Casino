package com.aurum.casino.game.crash;

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
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/crash")
public class CrashController {

    private final SessionService sessions;

    public CrashController(SessionService sessions) {
        this.sessions = sessions;
    }

    public record StartRequest(@Positive long stake, Double autoCashout) {
    }

    @PostMapping("/start")
    public SessionResponse<CrashView> start(@CurrentPlayer Long playerId, @Valid @RequestBody StartRequest request) {
        return sessions.start(playerId, GameType.CRASH,
                ctx -> CrashState.start(request.stake(), request.autoCashout(), ctx.random(), ctx.now()));
    }

    /** El cliente consulta periódicamente para saber si el cohete explotó. */
    @PostMapping("/tick")
    public SessionResponse<CrashView> tick(@CurrentPlayer Long playerId) {
        return sessions.act(playerId, GameType.CRASH, CrashState.class, (state, ctx) -> state.tick(ctx.now()));
    }

    @PostMapping("/cashout")
    public SessionResponse<CrashView> cashout(@CurrentPlayer Long playerId) {
        return sessions.act(playerId, GameType.CRASH, CrashState.class, (state, ctx) -> state.cashout(ctx.now()));
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse<CrashView>> active(@CurrentPlayer Long playerId) {
        return SessionResponses.okOrNoContent(sessions.active(playerId, GameType.CRASH, CrashState.class));
    }
}
