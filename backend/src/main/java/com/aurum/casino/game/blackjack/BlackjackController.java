package com.aurum.casino.game.blackjack;

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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/games/blackjack")
public class BlackjackController {

    private final SessionService sessions;

    public BlackjackController(SessionService sessions) {
        this.sessions = sessions;
    }

    public record DealRequest(@Positive long stake) {
    }

    public record ActionRequest(@NotNull BlackjackAction action) {
    }

    @PostMapping("/deal")
    public SessionResponse<BlackjackView> deal(@CurrentPlayer Long playerId, @Valid @RequestBody DealRequest request) {
        return sessions.start(playerId, GameType.BLACKJACK,
                ctx -> BlackjackState.deal(request.stake(), ctx.random()));
    }

    @PostMapping("/action")
    public SessionResponse<BlackjackView> action(@CurrentPlayer Long playerId,
                                                 @Valid @RequestBody ActionRequest request) {
        return sessions.act(playerId, GameType.BLACKJACK, BlackjackState.class,
                (state, ctx) -> state.apply(request.action()));
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse<BlackjackView>> active(@CurrentPlayer Long playerId) {
        return SessionResponses.okOrNoContent(sessions.active(playerId, GameType.BLACKJACK, BlackjackState.class));
    }
}
