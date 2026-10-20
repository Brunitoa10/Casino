package com.aurum.casino.game.roulette;

import com.aurum.casino.shared.error.DomainException;

/**
 * Tipos de apuesta. Cada constante define su pago y su regla de acierto
 * (Strategy vía enum): agregar un tipo no exige modificar el motor.
 */
public enum RouletteBetType {

    STRAIGHT(35, 0, RouletteNumbers.MAX) {
        @Override
        public boolean wins(int number, Integer value) {
            return number == value;
        }
    },
    RED(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return RouletteNumbers.isRed(number);
        }
    },
    BLACK(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return RouletteNumbers.isBlack(number);
        }
    },
    ODD(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return number != 0 && number % 2 == 1;
        }
    },
    EVEN(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return number != 0 && number % 2 == 0;
        }
    },
    LOW(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return number >= 1 && number <= 18;
        }
    },
    HIGH(1) {
        @Override
        public boolean wins(int number, Integer value) {
            return number >= 19;
        }
    },
    DOZEN(2, 1, 3) {
        @Override
        public boolean wins(int number, Integer value) {
            return number != 0 && (number - 1) / 12 + 1 == value;
        }
    },
    COLUMN(2, 1, 3) {
        @Override
        public boolean wins(int number, Integer value) {
            return number != 0 && (number - 1) % 3 + 1 == value;
        }
    };

    private final int payoutRatio;
    private final boolean requiresValue;
    private final int minValue;
    private final int maxValue;

    RouletteBetType(int payoutRatio) {
        this.payoutRatio = payoutRatio;
        this.requiresValue = false;
        this.minValue = 0;
        this.maxValue = 0;
    }

    RouletteBetType(int payoutRatio, int minValue, int maxValue) {
        this.payoutRatio = payoutRatio;
        this.requiresValue = true;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public abstract boolean wins(int number, Integer value);

    /** Pago "a 1": un pleno paga 35 a 1, el jugador recibe 36 veces lo apostado. */
    public int payoutRatio() {
        return payoutRatio;
    }

    public void validate(Integer value) {
        if (!requiresValue) {
            return;
        }
        if (value == null || value < minValue || value > maxValue) {
            throw DomainException.invalidBet(
                    name() + " requiere un valor entre " + minValue + " y " + maxValue);
        }
    }
}
