package com.aurum.casino.game.core;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

/** RNG criptográficamente seguro para producción. */
@Component
public class SecureRandomSource implements RandomSource {

    private final SecureRandom random = new SecureRandom();

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }
}
