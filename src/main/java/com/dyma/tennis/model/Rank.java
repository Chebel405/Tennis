package com.dyma.tennis.model;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record Rank(
        @Positive (message = "Nombre entier et posif") int position,
        @PositiveOrZero(message = "Uniquement des nombres positif") int points) {
}
