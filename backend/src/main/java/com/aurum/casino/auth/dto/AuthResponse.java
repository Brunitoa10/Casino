package com.aurum.casino.auth.dto;

import com.aurum.casino.player.PlayerResponse;

public record AuthResponse(String token, PlayerResponse player) {
}
