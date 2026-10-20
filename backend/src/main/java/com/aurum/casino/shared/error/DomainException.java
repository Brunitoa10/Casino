package com.aurum.casino.shared.error;

/**
 * Excepción base de negocio. Cada error lleva un {@link ErrorCode} estable
 * que el frontend puede interpretar sin depender del texto del mensaje.
 */
public class DomainException extends RuntimeException {

    private final ErrorCode code;

    public DomainException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }

    public static DomainException invalidBet(String message) {
        return new DomainException(ErrorCode.INVALID_BET, message);
    }

    public static DomainException insufficientFunds() {
        return new DomainException(ErrorCode.INSUFFICIENT_FUNDS, "Saldo insuficiente para esta apuesta");
    }

    public static DomainException invalidAction(String message) {
        return new DomainException(ErrorCode.INVALID_ACTION, message);
    }

    public static DomainException unauthorized() {
        return new DomainException(ErrorCode.UNAUTHORIZED, "Sesión inválida o expirada");
    }
}
