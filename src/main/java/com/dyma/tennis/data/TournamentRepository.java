package com.dyma.tennis.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<TournamentEntity, Long> {
Optional<TournamentEntity> findOneByIdentifier(UUID identifier);
Optional<TournamentEntity> findOneByNameIgnoreCase(String name);

}
