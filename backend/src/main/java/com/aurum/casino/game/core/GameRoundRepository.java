package com.aurum.casino.game.core;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    public List<GameRound> findByPlayerIdOrderByIdDesc(Long playerId, Pageable pageable);

    @Query("""
            select new com.aurum.casino.game.core.PlayerStats(
                count(r), coalesce(sum(r.stake), 0L), coalesce(sum(r.payout), 0L), coalesce(max(r.payout), 0L))
            from GameRound r
            where r.playerId = :playerId
            """)
    public PlayerStats statsFor(@Param("playerId") Long playerId);
}
