package com.aurum.casino.game.session;

import java.util.Optional;

import org.springframework.http.ResponseEntity;

/** Utilidad web: 200 con la sesión activa o 204 si no hay ninguna. */
public final class SessionResponses {

    private SessionResponses() {
    }

    public static <V> ResponseEntity<SessionResponse<V>> okOrNoContent(Optional<SessionResponse<V>> response) {
        return response.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
