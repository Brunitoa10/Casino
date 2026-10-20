package com.aurum.casino.player;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aurum.casino.shared.config.AurumProperties;
import com.aurum.casino.shared.error.DomainException;
import com.aurum.casino.shared.error.ErrorCode;

@Service
public class PlayerService {

    private final PlayerRepository players;
    private final AurumProperties.Wallet walletRules;

    public PlayerService(PlayerRepository players, AurumProperties properties) {
        this.players = players;
        this.walletRules = properties.wallet();
    }

    @Transactional(readOnly = true)
    public Player getById(Long playerId) {
        return players.findById(playerId)
                .orElseThrow(() -> new DomainException(ErrorCode.NOT_FOUND, "Jugador no encontrado"));
    }

    @Transactional(readOnly = true)
    public PlayerResponse describe(Long playerId) {
        return toResponse(getById(playerId));
    }

    /** Recarga de créditos de cortesía cuando el saldo cae por debajo del umbral. */
    @Transactional
    public PlayerResponse refill(Long playerId) {
        Player player = getById(playerId);
        if (!player.isBelow(walletRules.refillThreshold())) {
            throw new DomainException(ErrorCode.REFILL_NOT_AVAILABLE,
                    "La recarga solo está disponible con saldo menor a " + walletRules.refillThreshold());
        }
        player.credit(walletRules.refillAmount());
        return toResponse(player);
    }

    public PlayerResponse toResponse(Player player) {
        return new PlayerResponse(player.getId(), player.getUsername(), player.getBalance(),
                player.isBelow(walletRules.refillThreshold()));
    }
}
