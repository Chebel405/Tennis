package com.dyma.tennis;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record Player(
        // @NotBlank empêche les espaces
        @NotBlank String firstName,
        @NotBlank String lastName,
        // @PastOrPresent Obliger à ce que la date soit dans le présent ou dans le passé
        @PastOrPresent LocalDate birthDate,
        @Valid Rank rank) {

}
