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

/**
 * Service pour gérer les opérations liées aux joueurs.
 * Fournit des méthodes pour créer, mettre à jour, récupérer et supprimer des joueurs.
 * Intègre des logs pour surveiller le comportement et faciliter le débogage.
 */
@Service
public class PlayerService {

    // Logger pour capturer les événements et erreurs dans le service.
    private final Logger log = LoggerFactory.getLogger(PlayerService.class);

    // Dépendance vers le repository pour effectuer les opérations sur la base de données.
    @Autowired
    private PlayerRepository playerRepository;

    // Constructeur pour injecter le PlayerRepository.
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    /**
     * Récupère tous les joueurs, triés par leur position dans le classement.
     *
     * @return Une liste de joueurs triés.
     */
    public List<Player> getAllPlayers() {
        log.info("Invoking getAllPlayers()");
        try {
            // Conversion des entités PlayerEntity en objets Player, triés par position.
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

    /**
     * Récupère un joueur par son identifiant unique.
     *
     * @param identifier L'identifiant unique du joueur.
     * @return Le joueur correspondant.
     */
    public Player getByIdentifier(UUID identifier) {
        log.info("Invoking getByIdentifier with identifier={}", identifier);
        try {
            // Recherche du joueur dans la base de données.
            Optional<PlayerEntity> player = playerRepository.findOneByIdentifier(identifier);
            if (player.isEmpty()) {
                log.warn("Couldn't find player with identifier={}", identifier);
                throw new PlayerNotFoundException(identifier);
            }
            // Conversion de PlayerEntity en Player.
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
     * Crée un nouveau joueur après vérification qu'il n'existe pas déjà.
     * Recalcule ensuite le classement des joueurs.
     *
     * @param playerToCreate Les informations du joueur à créer.
     * @return Le joueur nouvellement créé.
     */
    public Player create(PlayerToCreate playerToCreate) {
        log.info("Invoking create with playerToCreate={}", playerToCreate);
        //Vérifier que le joueur n'existe pas
        try {
            // Vérification de l'existence du joueur dans la base de données.
            Optional<PlayerEntity> player = playerRepository.findOneByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(
                    playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
            if (player.isPresent()) {
                log.warn("Player to create with firstName={} and lastName={} and birthDate={} exist already",
                        playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
                throw new PlayerAlreadyExistsException(playerToCreate.firstName(), playerToCreate.lastName(), playerToCreate.birthDate());
            }

            // Création d'une nouvelle entité PlayerEntity.
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

            // Enregistrement du joueur dans la base de données.
            PlayerEntity registeredPlayer = playerRepository.save(playerToRegister);

            // Recalcule le classement des joueurs après l'ajout.
            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);

            // Retourne le joueur nouvellement créé.
            return this.getByIdentifier(registeredPlayer.getIdentifier());
        } catch (DataAccessException e) {
            log.error("Could not create player={}", playerToCreate, e);
            throw new PlayerDataRetrievalException(e);
        }
    }


    /**
     * Met à jour les informations d'un joueur existant et recalcule les classements.
     *
     * @param playerToUpdate Les informations mises à jour du joueur.
     * @return Le joueur mis à jour.
     */
    public Player update(PlayerToUpdate playerToUpdate) {
        log.info("Invoking update with playerToUpdate={}", playerToUpdate);
        try {
            // Recherche du joueur existant.
            Optional<PlayerEntity> existingPlayer= playerRepository.findOneByIdentifier(playerToUpdate.identifier());
            if (existingPlayer.isEmpty()) {
                log.warn("Couldn't find player to update with identifier={}", playerToUpdate.identifier());
                throw new PlayerNotFoundException(playerToUpdate.identifier());
            }

            //Vérification des doublons potentiels avec d'autres joueurs.
            Optional<PlayerEntity> potentiallyDuplicatedPlayer = playerRepository.findOneByFirstNameIgnoreCaseAndLastNameIgnoreCaseAndBirthDate(playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
            if(potentiallyDuplicatedPlayer.isPresent() && !potentiallyDuplicatedPlayer.get().getIdentifier().equals(playerToUpdate.identifier())){
                log.warn("Player to update with firstName={} lastName={} and birthDate={} already exists ", playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
                throw new PlayerAlreadyExistsException(playerToUpdate.firstName(), playerToUpdate.lastName(), playerToUpdate.birthDate());
            }

            // Mise à jour des informations du joueur.
            existingPlayer.get().setFirstName(playerToUpdate.firstName());
            existingPlayer.get().setLastName(playerToUpdate.lastName());
            existingPlayer.get().setBirthDate(playerToUpdate.birthDate());
            existingPlayer.get().setPoints(playerToUpdate.points());
            PlayerEntity updatedPlayer = playerRepository.save(existingPlayer.get());

            // Recalcule et met à jour les classements.
            RankingCalculator rankingCalculator = new RankingCalculator(playerRepository.findAll());
            List<PlayerEntity> newRanking = rankingCalculator.getNewPlayersRanking();
            playerRepository.saveAll(newRanking);

            // Retourne le joueur mis à jour.
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
     * @param identifier L'identifiant du joueur
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
