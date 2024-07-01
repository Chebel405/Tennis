package com.dyma.tennis.service;

import com.dyma.tennis.Player;
import com.dyma.tennis.PlayerToSave;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.Month;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class PlayerServiceIntegrationTest {
    @Autowired
    private PlayerService playerService;
    @Test
    public void shouldCreatePlayer(){
        //Given
        PlayerToSave playerToSave = new PlayerToSave(
                "John",
                "Doe",
                LocalDate.of(2000, Month.JANUARY, 1),
                10000
        );

        //When

        Player createdPlayer = playerService.create(playerToSave);

        //Then
        Assertions.assertThat(createdPlayer.firstName()).isEqualTo("John");

    }


}
