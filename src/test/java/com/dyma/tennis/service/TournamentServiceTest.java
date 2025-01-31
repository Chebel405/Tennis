package com.dyma.tennis.service;

import com.dyma.tennis.data.PlayerEntityList;
import com.dyma.tennis.data.PlayerRepository;
import com.dyma.tennis.data.TournamentEntityList;
import com.dyma.tennis.data.TournamentRepository;
import com.dyma.tennis.model.Player;
import com.dyma.tennis.model.Tournament;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataRetrievalFailureException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    private TournamentService tournamentService;
    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        tournamentService = new TournamentService(tournamentRepository);
    }

    @Test
    public void shouldReturnAllTournaments(){
        // Given
        // Lorsque le repo se comporte de telle maniere
        Mockito.when(tournamentRepository.findAll()).thenReturn(TournamentEntityList.ALL);

        //When
        // Lorque j'appelle mon service qui est ce que je test
        List<Tournament> allTournements = tournamentService.getAllTournaments();

        //Then
        // Alors j'obtiens la list de mes joueurs triée
        Assertions.assertThat(allTournements)
                .extracting("name")
                .containsExactly("Australian Open", "French Open", "Wimbledon", "US Open");
    }

    @Test
    public void shouldRetrieveTournament(){
        //Given
        UUID tournamentToRetrieve = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12");
        Mockito.when(tournamentRepository.findOneByIdentifier(tournamentToRetrieve)).thenReturn(Optional.of(TournamentEntityList.FRENCH_OPEN));

        //When
        Tournament retrievedTournament =  tournamentService.getByIdentifier(tournamentToRetrieve);

        //Then
        Assertions.assertThat(retrievedTournament.name()).isEqualTo("French Open");

    }


}
