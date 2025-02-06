package com.dyma.tennis.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record Tournament(
       @Valid TournamentDescription info,
       @Valid Set<PlayerDescription> players
) {
}
