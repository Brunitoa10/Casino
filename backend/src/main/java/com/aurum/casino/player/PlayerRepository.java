package com.aurum.casino.player;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    public Optional<Player> findByUsernameIgnoreCase(String username);

    public boolean existsByUsernameIgnoreCase(String username);
}
