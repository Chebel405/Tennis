package com.dyma.tennis.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // Methode faire charger les rôles
    Optional<UserEntity> findOneWithRolesByLoginIgnoreCase(String login);
}
