package com.dyma.tennis;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record Player(
        // @NotBlank empêche les espaces
        @NotBlank(message = "Firstname est obligatoire") String firstName,
        @NotBlank(message = "Lastname est obligatoire") String lastName,
        // @PastOrPresent Obliger à ce que la date soit dans le présent ou dans le passé
        @NotNull(message = "Date obligatoire") @PastOrPresent(message = "Date acceptée passé ou présent")LocalDate birthDate,
        @Valid Rank rank) {

}
