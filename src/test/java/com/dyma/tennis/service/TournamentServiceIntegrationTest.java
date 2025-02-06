package com.dyma.tennis.service;

import com.dyma.tennis.model.Tournament;
import com.dyma.tennis.model.TournamentToCreate;
import com.dyma.tennis.model.TournamentToUpdate;
import org.assertj.core.api.Assertions;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TournamentServiceIntegrationTest {

    @Autowired
    private TournamentService tournamentService;

    /**
     * Nettoie et initialise la base de données avant chaque test.
     * Cela garantit un environnement propre pour l'exécution des tests.
     */
    @BeforeEach
    void clearDatabase(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    /**
     * Teste la création d'un tournoi et vérifie que les données sont bien enregistrées.
     */
    @Test
    public void shouldCreateTournament() {
        // Given
        TournamentToCreate tournamentToCreate = new TournamentToCreate(
                "Madrid Master 1000",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(17),
                500000,
                64
        );

        // When: Sauvegarde du tournoi dans la base de données
        Tournament savedTournament = tournamentService.create(tournamentToCreate);
        Tournament createdTournament = tournamentService.getByIdentifier(savedTournament.info().identifier());

        // Then: Vérification que le tournoi a bien été créé avec le bon nom
        Assertions.assertThat(createdTournament.info().name()).isEqualTo("Madrid Master 1000");
    }

    /**
     * Teste la tentative de création d'un tournoi déjà existant et vérifie que l'exception est levée.
     */
    @Test
    public void shouldFailToCreateAnExistingTournament() {
        // Given
        TournamentToCreate tournamentToCreate = new TournamentToCreate(
                "Madrid Master 1000",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(17),
                500000,
                64
        );
        tournamentService.create(tournamentToCreate);

        // Création d'un tournoi avec les mêmes caractéristiques
        TournamentToCreate duplicatedTournamentToCreate = new TournamentToCreate(
                "Madrid Master 1000",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(17),
                500000,
                64
        );

        // When / Then: Vérification que l'exception est bien levée
        Exception exception = assertThrows(TournamentAlreadyExistsException.class, () -> {
            tournamentService.create(duplicatedTournamentToCreate);
        });
        Assertions.assertThat(exception.getMessage()).contains("Tournament with name Madrid Master 1000 already exists.");
    }

    /**
     * Teste la mise à jour d'un tournoi existant.
     */
    @Test
    public void shouldUpdateTournament() {
        // Given
        UUID frenchOpenIdentifier = UUID.fromString("d4a9f8e2-9051-4739-90bc-1cb7e4c7ad42");
        TournamentToUpdate tournamentToUpdate = new TournamentToUpdate(
                frenchOpenIdentifier,
                "Roland Garros",
                LocalDate.of(2025, Month.MAY, 26),
                LocalDate.of(2025, Month.JUNE, 9),
                2500000,
                128
        );

        // When: Mise à jour du tournoi
        tournamentService.update(tournamentToUpdate);
        Tournament updatedTournament = tournamentService.getByIdentifier(frenchOpenIdentifier);

        // Then: Vérification que le nom du tournoi a bien été mis à jour
        Assertions.assertThat(updatedTournament.info().name()).isEqualTo("Roland Garros");
    }

    /**
     * Teste la suppression d'un tournoi et vérifie que celui-ci n'existe plus après suppression.
     */
    @Test
    public void shouldDeleteTournament() {
        // Given: Identifiant d'un tournoi à supprimer
        UUID tournamentToDelete = UUID.fromString("124edf07-64fa-4ea4-a65e-3bfe96df5781");

        // When: Suppression du tournoi
        tournamentService.delete(tournamentToDelete);

        // Then: Vérification que le tournoi ne figure plus dans la liste des tournois
        List<Tournament> allTournaments = tournamentService.getAllTournaments();
        Assertions.assertThat(allTournaments)
                .extracting("info.name")
                .containsExactly("Australian Open", "French Open", "Wimbledon");
    }

    /**
     * Teste la suppression d'un tournoi inexistant et vérifie que l'exception correcte est levée.
     */
    @Test
    public void shouldFailToDeleteTournament_WhenTournamentDoesNotExist() {
        // Given: Identifiant d'un tournoi inexistant
        UUID tournamentToDelete = UUID.fromString("5f8c9b43-8d74-49e8-b821-f43d57e4a9b7");

        // When / Then: Vérification que l'exception TournamentNotFoundException est levée
        Exception exception = assertThrows(TournamentNotFoundException.class, () -> {
            tournamentService.delete(tournamentToDelete);
        });
        Assertions.assertThat(exception.getMessage()).isEqualTo("Tournament with identifier 5f8c9b43-8d74-49e8-b821-f43d57e4a9b7 couldn't be found.");
    }
}
