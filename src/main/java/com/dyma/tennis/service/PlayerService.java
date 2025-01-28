package com.dyma.tennis.service;

import com.dyma.tennis.model.Player;
import com.dyma.tennis.model.PlayerToCreate;
import com.dyma.tennis.model.PlayerToUpdate;
import com.dyma.tennis.model.Rank;
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
import java.util.UUID;
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
                            player.getIdentifier(),
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
    public Player getByIdentifier(UUID identifier) {
        log.info("Invoking getByIdentifier with identifier={}", identifier);
        try {
            Optional<PlayerEntity> player = playerRepository.findOneByIdentifier(identifier);
            if (player.isEmpty()) {
                log.warn("Couldn't find player with identifier={}", identifier);
                throw new PlayerNotFoundException(identifier);
            }

            return new Player(
                    player.get().getIdentifier(),
                    player.get().getFirstName(),
                    player.get().getLastName(),
                    player.get().getBirthDate(),
                    new Rank(player.get().getRank(), player.get().getPoints())
            );
        } catch (DataAccessException e) {
            log.error("Couldn't find player with identifer={}", identifier, e);
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
     * @param playerToCreate
     * @return
     */
    public Player create(PlayerToCreate playerToCreate) {
        log.info("Invoking create with playerToCreate={}", playerToCreate);
        //Vérifier que le joueur n'existe pas
        try {
            Optional<PlayerEntity> player = playerRepository.findOneByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                    playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
            if (player.isPresent()) {
                log.warn("Player to create with firstName={} and lastName={} and birthDate={} exist already",
                        playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
                throw new PlayerAlreadyExistsException(playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
            }

            PlayerEntity playerToRegister= new PlayerEntity(
                    UUID.randomUUID(),
                    playerToCreate.lastName(),
                    playerToCreate.firstName(),
                    playerToCreate.birthDate(),
                    playerToCreate.points(),
                    999999999);

            /**
             *      Le service enregistre le nouveau joueur
             *      RankingCalculator : recalculer le classement
             *      PlayerRepository : Mettre à jour les joueurs
             *      GetByLastName retourne le joueur créé
             */

            PlayerEntity registeredPlayer = playerRepository.save(playerToRegister);

            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);

            return this.getByIdentifier(registeredPlayer.getIdentifier());
        } catch (DataAccessException e) {
            log.error("Could not create player={}", playerToCreate, e);
            throw new PlayerDataRetrievalException(e);
        }
    }


    //Mise à jour d'un joueur
    public Player update(PlayerToUpdate playerToUpdate) {
        log.info("Invoking update with playerToUpdate={}", playerToUpdate);
        try {
            Optional<PlayerEntity> existingPlayer= playerRepository.findOneByIdentifier(playerToUpdate.identifier());
            if (existingPlayer.isEmpty()) {
                log.warn("Couldn't find player to update with identifier={}", playerToUpdate.identifier());
                throw new PlayerNotFoundException(playerToUpdate.identifier());
            }

            Optional<PlayerEntity> potentiallyDuplicatedPlayer = playerRepository.findOneByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
            if(potentiallyDuplicatedPlayer.isPresent() && !potentiallyDuplicatedPlayer.get().getIdentifier().equals(playerToUpdate.identifier())){
                log.warn("Player to update with firstName={} lastName={} and birthDate={} already exists ", playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
                throw new PlayerAlreadyExistsException(playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
            }


            existingPlayer.get().setFirstName(playerToUpdate.firstName());
            existingPlayer.get().setLastName(playerToUpdate.lastName());
            existingPlayer.get().setBirthDate(playerToUpdate.birthDate());
            existingPlayer.get().setPoints(playerToUpdate.points());
            PlayerEntity updatedPlayer = playerRepository.save(existingPlayer.get());

            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);

            return getByIdentifier(updatedPlayer.getIdentifier());
        } catch (DataAccessException e) {
            log.error("Couldn't update player {}", playerToUpdate, e);
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
     * @param identifier
     */

    public void delete(UUID identifier) {
        log.info("Invoking delete with identifier={}", identifier);
        try {
            Optional<PlayerEntity> playerDelete = playerRepository.findOneByIdentifier(identifier);
            if (playerDelete.isEmpty()) {
                log.warn("Couldn't find player to delete with identifier={}", identifier);
                throw new PlayerNotFoundException(identifier);
            }

            playerRepository.delete(playerDelete.get());

            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);
        } catch (DataAccessException e) {
            log.error("Couldn't delete player with identifier={}", identifier, e);
            throw new PlayerDataRetrievalException(e);
        }

    }


}
