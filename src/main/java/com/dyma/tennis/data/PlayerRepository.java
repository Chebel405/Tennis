package com.dyma.tennis.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<PlayerEntity, Long> {
    // Declaration de la méthode
    Optional<PlayerEntity> findOneByLastName(String lastName);
}