package com.aurum.casino.shared.config;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración tipada de la aplicación (prefijo {@code aurum} en application.yml).
 */
@ConfigurationProperties(prefix = "aurum")
public record AurumProperties(Cors cors, Auth auth, Wallet wallet, Limits limits) {

    public record Cors(List<String> allowedOrigins) {
    }

    public record Auth(Duration sessionTtl) {
    }

    public record Wallet(long initialBalance, long refillThreshold, long refillAmount) {
    }

    public record Limits(long minStake, long maxStake) {
    }
}
