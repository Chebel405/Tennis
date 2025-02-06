package com.dyma.tennis.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.UUID;

public record TournamentDescription(
        @NotNull(message = "Identifier est obligatoire") UUID identifier,
        @NotBlank(message ="Name est obligatoire") String name,
        @NotNull(message ="Start date est obligatoire") LocalDate startDate,
        @NotNull(message = "End date est obligatoire") LocalDate endDateDate,
        @Positive(message = " Prize money doit être positif") Integer prizeMoney,
        @NotNull(message = "Capacité est obligatoire") @Positive(message = "Capacité doit être positive") Integer capacity
) {
}
