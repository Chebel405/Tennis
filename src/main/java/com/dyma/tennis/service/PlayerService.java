package com.dyma.tennis.service;

import com.dyma.tennis.Player;
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

    public PlayerService(PlayerRepository playerRepository){
        this.playerRepository = playerRepository;
    }

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


    /**
     * Création d'un joueur
     *      Vérifier que le joueur n'existe pas déjà
     *      Créer le nouveau joueur
     *      Recalculer l'ensemble des classements
     *      Mettre à jour tous les joueurs
     * @param playerToSave
     * @return
     */
    public Player create(PlayerToSave playerToSave){
        //Vérifier que le joueur n'existe pas
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

        /**
         *      Le service enregistre le nouveau joueur
         *      RankingCalculator : recalculer le classement
         *      PlayerRepository : Mettre à jour les joueurs
         *      GetByLastName retourne le joueur créé
         */
        playerRepository.save(playerEntity);

        RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
        List<PlayerEntity>updatedPlayers = rankingCalculator.getNewPlayersRanking();
        playerRepository.saveAll(updatedPlayers);

        return getByLastName(playerEntity.getLastName());
    }


    //Mise à jour d'un joueur
    public Player update(PlayerToSave playerToSave){
        Optional<PlayerEntity>playerToUpdate = playerRepository.findOneByLastNameIgnoreCase(playerToSave.lastName());
        if (playerToUpdate.isEmpty()) {
            throw new PlayerNotFoundException(playerToSave.lastName());
        }
        playerToUpdate.get().setFirstName(playerToSave.firstName());
        playerToUpdate.get().setBirthDate(playerToSave.birthDate());
        playerToUpdate.get().setPoints(playerToSave.points());
        PlayerEntity updatedPlayer = playerRepository.save(playerToUpdate.get());

        RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
        List<PlayerEntity>newRanking = rankingCalculator.getNewPlayersRanking();
        playerRepository.saveAll(newRanking);

        return getByLastName(updatedPlayer.getLastName());
    }

    /**
     * Méthode Delete
     *      1/ Vérifier que le joueur que l'on souhaite supprimer existe
     *      2/ Supprimer le joueur
     *      3/ Recalculer l'ensemble des classements
     *      4/ Mettre à jour les joueurs pour tenir compte du nouveau classement
     * @param lastName
     */

    public void delete(String lastName){
        Optional<PlayerEntity>playerDelete = playerRepository.findOneByLastNameIgnoreCase(lastName);
        if (playerDelete.isEmpty()) {
            throw new PlayerNotFoundException(lastName);
        }

        playerRepository.delete(playerDelete.get());

        RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
        List<PlayerEntity>newRanking = rankingCalculator.getNewPlayersRanking();
        playerRepository.saveAll(newRanking);
    }



}
