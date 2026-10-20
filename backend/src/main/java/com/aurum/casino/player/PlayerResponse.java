package com.aurum.casino.player;

public record PlayerResponse(Long id, String username, long balance, boolean refillAvailable) {
}
