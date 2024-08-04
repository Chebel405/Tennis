package com.dyma.tennis.service;

import com.dyma.tennis.Player;
import com.dyma.tennis.PlayerToSave;
import com.dyma.tennis.Rank;
import com.dyma.tennis.data.PlayerEntity;
import com.dyma.tennis.data.PlayerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    //Genère des logs
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    @Autowired
    private PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    //Affichage de la liste dans l'ordre de position
    public List<Player> getAllPlayers() {
        log.info("Invoking getAllPlayers()");
        try {
            return playerRepository.findAll().stream()
                    .map(player -> new Player(
                            player.getFirstName(),
                            player.getLastName(),
                            player.getBirthDate(),
                            new Rank(player.getRank(), player.getPoints())
                    ))
                    .sorted(Comparator.comparing(player -> player.rank().position()))
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Couldn't retrieve players", e);
            throw new PlayerDataRetrievalException(e);
        }

    }

    // Récupérer un joueur par son nom de famille
    public Player getByLastName(String lastName) {
        log.info("Invoking getByLastName with lastName={}", lastName);
        try {
            Optional<PlayerEntity> player = playerRepository.findOneByLastNameIgnoreCase(lastName);
            if (player.isEmpty()) {
                log.warn("Couldn't find player with lastName={}", lastName);
                throw new PlayerNotFoundException(lastName);
            }

            return new Player(
                    player.get().getFirstName(),
                    player.get().getLastName(),
                    player.get().getBirthDate(),
                    new Rank(player.get().getRank(), player.get().getPoints())
            );
        } catch (DataAccessException e) {
            log.error("Couldn't find player with lastName={}", lastName, e);
            throw new PlayerDataRetrievalException(e);
        }
    }


    /**
     * Création d'un joueur
     * Vérifier que le joueur n'existe pas déjà
     * Créer le nouveau joueur
     * Recalculer l'ensemble des classements
     * Mettre à jour tous les joueurs
     *
     * @param playerToSave
     * @return
     */
    public Player create(PlayerToSave playerToSave) {
        log.info("Invoking create with playerToSave={}", playerToSave);
        //Vérifier que le joueur n'existe pas
        try {
            Optional<PlayerEntity> playerToCreate = playerRepository.findOneByLastNameIgnoreCase(playerToSave.lastName());
            if (playerToCreate.isPresent()) {
                log.warn("Player to create exist lastName={} exist already", playerToSave.lastName());
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
            List<PlayerEntity> updatedPlayers = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(updatedPlayers);

            return getByLastName(playerEntity.getLastName());
        } catch (DataAccessException e) {
            log.error("Couldn't create player {}", playerToSave, e);
            throw new PlayerDataRetrievalException(e);
        }

    }


    //Mise à jour d'un joueur
    public Player update(PlayerToSave playerToSave) {
        log.info("Invoking update with playerToSave={}", playerToSave);
        try {
            Optional<PlayerEntity> playerToUpdate = playerRepository.findOneByLastNameIgnoreCase(playerToSave.lastName());
            if (playerToUpdate.isEmpty()) {
                log.warn("Couldn't find player to update with lastName={}", playerToSave.lastName());
                throw new PlayerNotFoundException(playerToSave.lastName());
            }
            playerToUpdate.get().setFirstName(playerToSave.firstName());
            playerToUpdate.get().setBirthDate(playerToSave.birthDate());
            playerToUpdate.get().setPoints(playerToSave.points());
            PlayerEntity updatedPlayer = playerRepository.save(playerToUpdate.get());

            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);

            return getByLastName(updatedPlayer.getLastName());
        } catch (DataAccessException e) {
            log.error("Couldn't update player {}", playerToSave, e);
            throw new PlayerDataRetrievalException(e);
        }

    }

    /**
     * Méthode Delete
     * 1/ Vérifier que le joueur que l'on souhaite supprimer existe
     * 2/ Supprimer le joueur
     * 3/ Recalculer l'ensemble des classements
     * 4/ Mettre à jour les joueurs pour tenir compte du nouveau classement
     *
     * @param lastName
     */

    public void delete(String lastName) {
        log.info("Invoking delete with lastName={}", lastName);
        try {
            Optional<PlayerEntity> playerDelete = playerRepository.findOneByLastNameIgnoreCase(lastName);
            if (playerDelete.isEmpty()) {
                log.warn("Couldn't find player to delete with lastName={}", lastName);
                throw new PlayerNotFoundException(lastName);
            }

            playerRepository.delete(playerDelete.get());

            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);
        } catch (DataAccessException e) {
            log.error("Couldn't delete player with lastName={}", lastName, e);
            throw new PlayerDataRetrievalException(e);
        }

    }


}
