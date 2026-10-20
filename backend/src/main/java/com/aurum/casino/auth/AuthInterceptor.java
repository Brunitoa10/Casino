package com.aurum.casino.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.aurum.casino.shared.error.DomainException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Valida el token Bearer y deja el id del jugador como atributo de la petición. */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String PLAYER_ID_ATTRIBUTE = "aurum.playerId";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        String token = BearerToken.from(request.getHeader(HttpHeaders.AUTHORIZATION))
                .orElseThrow(DomainException::unauthorized);
        request.setAttribute(PLAYER_ID_ATTRIBUTE, authService.authenticate(token));
        return true;
    }
}
