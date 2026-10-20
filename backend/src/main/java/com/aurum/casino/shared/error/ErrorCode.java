package com.aurum.casino.shared.error;

/**
 * Códigos de error de negocio. El dominio no conoce HTTP: la traducción
 * a códigos de estado vive en la capa web ({@code GlobalExceptionHandler}).
 */
public enum ErrorCode {
    INVALID_BET,
    INSUFFICIENT_FUNDS,
    INVALID_CREDENTIALS,
    UNAUTHORIZED,
    USERNAME_TAKEN,
    REFILL_NOT_AVAILABLE,
    NOT_FOUND,
    INVALID_ACTION,
    ACTIVE_SESSION,
    NO_ACTIVE_SESSION
}
