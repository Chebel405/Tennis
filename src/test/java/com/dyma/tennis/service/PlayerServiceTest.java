package com.dyma.tennis.service;

import com.dyma.tennis.model.Player;
import com.dyma.tennis.data.PlayerEntityList;
import com.dyma.tennis.data.PlayerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataRetrievalFailureException;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayerServiceTest {
    @Mock
    private PlayerRepository playerRepository;

    private PlayerService playerService;
    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);
        playerService = new PlayerService(playerRepository);
    }
    @Test
    public void shouldReturnPlayerRanking(){
        // Given
        // Lorsque le repo se comporte de telle maniere
        Mockito.when(playerRepository.findAll()).thenReturn(PlayerEntityList.ALL);

        //When
        // Lorque j'appelle mon service qui est ce que je test
        List<Player> allPlayers = playerService.getAllPlayers();

        //Then
        // Alors j'obtiens la list de mes joueurs triée
        Assertions.assertThat(allPlayers)
                .extracting("lastName")
                .containsExactly("Nadal", "Djokovic", "Federer", "Murray");
    }

    @Test
    public void shouldFailToReturnPlayerRanking_whenDataAccessExceptionOccurs(){
        //Given
        Mockito.when(playerRepository.findAll()).thenThrow(new DataRetrievalFailureException("Data access error"));

        //When / Then
        Exception exception = assertThrows(PlayerDataRetrievalException.class, () -> {
            playerService.getAllPlayers();
        });
        Assertions.assertThat(exception.getMessage()).isEqualTo("Could not retrieve player data");

    }
    @Test
    public void shouldRetrievePlayer(){
        // Given
        String playerToRetrieve = "nadal";
        Mockito.when(playerRepository.findOneByLastNameIgnoreCase(playerToRetrieve)).thenReturn(Optional.of(PlayerEntityList.RAFAEL_NADAL));

        // when
        Player retrievedPlayer = playerService.getByLastName(playerToRetrieve);

        //then
        Assertions.assertThat(retrievedPlayer.lastName()).isEqualTo("Nadal");
        Assertions.assertThat(retrievedPlayer.firstName()).isEqualTo("Rafael");
        Assertions.assertThat(retrievedPlayer.rank().position()).isEqualTo(1);

    }
    @Test
    public void shouldFailToRetrieved_WhenPlayerDoesNotExist(){
        //Given
        String unknownPlayer = "doe";
        Mockito.when(playerRepository.findOneByLastNameIgnoreCase(unknownPlayer)).thenReturn(Optional.empty());

        //When / Then
        Exception exception = assertThrows(PlayerNotFoundException.class, () -> {
            playerService.getByLastName(unknownPlayer);
        });
        Assertions.assertThat(exception.getMessage()).isEqualTo("Player with last name doe couldn't be found");

    }
}
