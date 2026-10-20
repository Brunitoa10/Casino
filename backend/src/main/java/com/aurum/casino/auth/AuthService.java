package com.aurum.casino.auth;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurum.casino.auth.dto.AuthResponse;
import com.aurum.casino.auth.dto.CredentialsRequest;
import com.aurum.casino.player.Player;
import com.aurum.casino.player.PlayerRepository;
import com.aurum.casino.player.PlayerService;
import com.aurum.casino.shared.config.AurumProperties;
import com.aurum.casino.shared.error.DomainException;
import com.aurum.casino.shared.error.ErrorCode;

@Service
public class AuthService {

    private final PlayerRepository players;
    private final SessionRepository sessions;
    private final PlayerService playerService;
    private final TokenService tokens;
    private final AurumProperties properties;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(PlayerRepository players, SessionRepository sessions, PlayerService playerService,
                       TokenService tokens, AurumProperties properties, Clock clock) {
        this.players = players;
        this.sessions = sessions;
        this.playerService = playerService;
        this.tokens = tokens;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse register(CredentialsRequest request) {
        String username = request.username().trim();
        if (players.existsByUsernameIgnoreCase(username)) {
            throw new DomainException(ErrorCode.USERNAME_TAKEN, "Ese nombre de usuario ya está en uso");
        }
        Player player = players.save(Player.register(username,
                passwordEncoder.encode(request.password()),
                properties.wallet().initialBalance(),
                clock.instant()));
        return openSession(player);
    }

    @Transactional
    public AuthResponse login(CredentialsRequest request) {
        Player player = players.findByUsernameIgnoreCase(request.username().trim())
                .filter(p -> passwordEncoder.matches(request.password(), p.getPasswordHash()))
                .orElseThrow(() -> new DomainException(ErrorCode.INVALID_CREDENTIALS,
                        "Usuario o contraseña incorrectos"));
        return openSession(player);
    }

    @Transactional
    public void logout(String token) {
        sessions.deleteById(tokens.hash(token));
    }

    /** Resuelve el id del jugador dueño del token o lanza UNAUTHORIZED. */
    @Transactional(readOnly = true)
    public Long authenticate(String token) {
        Instant now = clock.instant();
        return sessions.findById(tokens.hash(token))
                .filter(session -> !session.isExpired(now))
                .map(Session::getPlayerId)
                .orElseThrow(DomainException::unauthorized);
    }

    private AuthResponse openSession(Player player) {
        String token = tokens.newToken();
        Instant expiresAt = clock.instant().plus(properties.auth().sessionTtl());
        sessions.save(new Session(tokens.hash(token), player.getId(), expiresAt));
        return new AuthResponse(token, playerService.toResponse(player));
    }
}
