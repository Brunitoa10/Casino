package com.aurum.casino.shared.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {

    /** Reloj inyectable: permite controlar el tiempo en los tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
