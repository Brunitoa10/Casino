package com.aurum.casino.game.videopoker;

import java.util.List;
import java.util.Set;

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
@RequestMapping("/api/games/video-poker")
public class VideoPokerController {

    private final SessionService sessions;

    public VideoPokerController(SessionService sessions) {
        this.sessions = sessions;
    }

    public record DealRequest(@Positive long stake) {
    }

    public record DrawRequest(@NotNull List<@NotNull Integer> held) {
    }

    @PostMapping("/deal")
    public SessionResponse<VideoPokerView> deal(@CurrentPlayer Long playerId, @Valid @RequestBody DealRequest request) {
        return sessions.start(playerId, GameType.VIDEO_POKER,
                ctx -> VideoPokerState.deal(request.stake(), ctx.random()));
    }

    @PostMapping("/draw")
    public SessionResponse<VideoPokerView> draw(@CurrentPlayer Long playerId, @Valid @RequestBody DrawRequest request) {
        Set<Integer> held = Set.copyOf(request.held());
        return sessions.act(playerId, GameType.VIDEO_POKER, VideoPokerState.class,
                (state, ctx) -> state.draw(held));
    }

    @GetMapping("/active")
    public ResponseEntity<SessionResponse<VideoPokerView>> active(@CurrentPlayer Long playerId) {
        return SessionResponses.okOrNoContent(sessions.active(playerId, GameType.VIDEO_POKER, VideoPokerState.class));
    }
}
