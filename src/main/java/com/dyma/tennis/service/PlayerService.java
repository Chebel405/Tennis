package com.dyma.tennis.service;

import com.dyma.tennis.Player;
import com.dyma.tennis.PlayerList;
import com.dyma.tennis.PlayerToSave;
import com.dyma.tennis.Rank;
import com.dyma.tennis.data.PlayerEntity;
import com.dyma.tennis.data.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    //Affichage de la liste dans l'ordre de position
    public List<Player>getAllPlayers(){
        return playerRepository.findAll().stream()
                .map(player-> new Player(
                        player.getFirstName(),
                        player.getLastName(),
                        player.getBirthDate(),
                        new Rank(player.getRank(), player.getPoints()))
                )
                .sorted(Comparator.comparing(player -> player.rank().position()))
                .collect(Collectors.toList());
    }

    // Récupérer un joueur par son nom de famille
    public Player getByLastName(String lastName){
        Optional<PlayerEntity> player = playerRepository.findOneByLastNameIgnoreCase(lastName);
        if(player.isEmpty()){
            throw new PlayerNotFoundException(lastName);
        }

        return new Player(
                player.get().getFirstName(),
                player.get().getLastName(),
                player.get().getBirthDate(),
                new Rank(player.get().getRank(), player.get().getPoints())
        );

    }


    // Créer un joueur
    public Player create(PlayerToSave playerToSave){
        Optional<PlayerEntity>playerToCreate = playerRepository.findOneByLastNameIgnoreCase(playerToSave.lastName());
        if (playerToCreate.isPresent()) {
            throw new PlayerAlreadyExistsException(playerToSave.lastName());
        }

        PlayerEntity playerEntity = new PlayerEntity(
                playerToSave.lastName(),
                playerToSave.firstName(),
                playerToSave.birthDate(),
                playerToSave.points(),
                999999999);


        playerRepository.save(playerEntity);

        RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
        List<PlayerEntity>updatedPlayers = rankingCalculator.getNewPlayersRanking();
        playerRepository.saveAll(updatedPlayers);


        return getByLastName(playerEntity.getLastName());
    }


    //Mise à jour d'un joueur
    public Player update(PlayerToSave playerToSave){
        return null;
    }

    public void delete(String lastName){

    }



}
