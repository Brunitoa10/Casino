package com.aurum.casino.game.core;

import org.springframework.stereotype.Component;

import com.aurum.casino.shared.config.AurumProperties;
import com.aurum.casino.shared.error.DomainException;

/** Límites de apuesta de la mesa, configurables desde application.yml. */
@Component
public class StakePolicy {

    private final AurumProperties.Limits limits;

    public StakePolicy(AurumProperties properties) {
        this.limits = properties.limits();
    }

    public void validate(long stake) {
        if (stake < limits.minStake() || stake > limits.maxStake()) {
            throw DomainException.invalidBet(
                    "La apuesta debe estar entre " + limits.minStake() + " y " + limits.maxStake());
        }
    }
}
