package com.dyma.tennis.model;



import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;


import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record Player(
        @Valid PlayerDescription info,
        @Valid Set<TournamentDescription> tournaments
        ) {

}
