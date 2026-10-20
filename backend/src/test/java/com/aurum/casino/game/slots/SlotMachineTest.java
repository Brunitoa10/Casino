package com.aurum.casino.game.slots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.aurum.casino.game.SeededRandom;

class SlotMachineTest {

    private final SlotMachine machine = new SlotMachine();

    @Test
    @DisplayName("Todas las celdas en CROWN pagan las 5 líneas")
    void fullGridOfCrownsPaysEveryLine() {
        // CROWN es el último símbolo: el ticket 99 cae en su rango de peso
        int[] tickets = new int[9];
        Arrays.fill(tickets, 99);

        SlotOutcome outcome = machine.spin(100, SeededRandom.sequence(tickets));

        assertThat(outcome.wins()).hasSize(5);
        assertThat(outcome.payout()).isEqualTo(5 * 100L * SlotSymbol.CROWN.multiplier() / 5);
    }

    @Test
    @DisplayName("El RTP teórico está entre 92 % y 97 %")
    void theoreticalRtpIsWithinRange() {
        double rtp = Arrays.stream(SlotSymbol.values())
                .mapToDouble(s -> Math.pow(s.probability(), 3) * s.multiplier())
                .sum();

        assertThat(rtp).isBetween(0.92, 0.97);
    }

    @Test
    @DisplayName("La simulación converge al RTP teórico")
    void simulatedRtpConverges() {
        var random = SeededRandom.withSeed(42);
        long stake = 1_000;
        long rounds = 200_000;
        long returned = 0;
        for (int i = 0; i < rounds; i++) {
            returned += machine.spin(stake, random).payout();
        }

        double rtp = (double) returned / (stake * rounds);
        assertThat(rtp).isBetween(0.90, 0.99);
    }
}
