package com.dyma.tennis.service;

import com.dyma.tennis.data.TournamentEntity;
import com.dyma.tennis.data.TournamentRepository;
import com.dyma.tennis.model.Tournament;
import com.dyma.tennis.model.TournamentToCreate;
import com.dyma.tennis.model.TournamentToUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour gérer les opérations liées aux tournois.
 * Fournit des méthodes pour créer, mettre à jour, récupérer et supprimer des tournois.
 * Intègre des logs pour surveiller le comportement et faciliter le débogage.
 */
@Service
public class TournamentService {

    // Logger pour capturer les événements et erreurs dans le service
    private final Logger log = LoggerFactory.getLogger(TournamentService.class);

    // Dépendance vers le repository pour effectuer les opérations sur la bdd
    @Autowired
    private final TournamentRepository tournamentRepository;

    //Constructeur pour injecter le TournamentRepository
    public TournamentService(TournamentRepository tournamentRepository) {
        this.tournamentRepository = tournamentRepository;
    }

    /**
     * Récupère tous les tournois, triés par leur position dans le classement.
     *
     * @return Une liste de tournois triés.
     */
    public List<Tournament>getAllTournaments(){
        log.info("Invoking getAllTournaments()");
        try{
            //Conversion des entités TournamentEntity en objets Tournament, triés par position.
            return tournamentRepository.findAll().stream()
                    .map(tournament -> new Tournament(
                            tournament.getIdentifier(),
                            tournament.getName(),
                            tournament.getStartDate(),
                            tournament.getEndDate(),
                            tournament.getPrizeMoney(),
                            tournament.getCapacity())
                    )
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            log.error("Couldn't retrieve tournaments", e);
            throw new TournamentDataRetrievalException(e);
        }
    }
    /**
     * Récupère un tournois par son identifiant unique.
     *
     * @param identifier L'identifiant unique du tournois.
     * @return Le tournois correspondant.
     */
    public Tournament getByIdentifier(UUID identifier){
        log.info("Invoking getByIdentifier with identifier={}", identifier);
        try{
            Optional<TournamentEntity> tournament = tournamentRepository.findOneByIdentifier(identifier);
            if (tournament.isEmpty()) {
                log.warn("Couldn't find tournament with identifier={}", identifier);
                throw new TournamentNotFoundException(identifier);
            }
            return new Tournament(
                    tournament.get().getIdentifier(),
                    tournament.get().getName(),
                    tournament.get().getStartDate(),
                    tournament.get().getEndDate(),
                    tournament.get().getPrizeMoney(),
                    tournament.get().getCapacity()
            );
        } catch (DataAccessException e){
            log.error("Couldn't find tournament with identifier={}", identifier, e);
            throw new TournamentDataRetrievalException(e);
        }
    }

    /**
     * Crée un nouveau tournois après vérification qu'il n'existe pas déjà.
     * Recalcule ensuite le classement des tournois.
     *
     * @param tournamentToCreate Les informations du tournoi à créer.
     * @return Le tournoi nouvellement créé.
     */
    public Tournament create(TournamentToCreate tournamentToCreate){
        log.info("Invoking create with tournamentToCreate={}", tournamentToCreate);

        Optional<TournamentEntity>tournament = tournamentRepository.findOneByNameIgnoreCase(tournamentToCreate.name());
        if(tournament.isPresent()){
            log.warn("Tournament to create with name={} already exists", tournamentToCreate.name());
            throw new TournamentAlreadyExistsException(tournamentToCreate.name());
        }
        try{
            TournamentEntity tournamentToRegister = new TournamentEntity(
                    UUID.randomUUID(),
                    tournamentToCreate.name(),
                    tournamentToCreate.startDate(),
                    tournamentToCreate.endDateDate(),
                    tournamentToCreate.prizeMoney(),
                    tournamentToCreate.capacity());

            TournamentEntity registeredTournament = tournamentRepository.save(tournamentToRegister);

            return this.getByIdentifier(registeredTournament.getIdentifier());
        } catch (DataAccessException e){
            log.error("Couldn't create tournament={}", tournamentToCreate, e);
            throw new TournamentDataRetrievalException(e);
        }
    }

    public Tournament update(TournamentToUpdate tournamentToUpdate){
        log.info("Invoking update with tournamentToUpdate={}", tournamentToUpdate);
        try{
            Optional<TournamentEntity> existingTournament = tournamentRepository.findOneByIdentifier(tournamentToUpdate.identifier());
            if(existingTournament.isEmpty()){
                log.warn("Couldn't find tournament to update with identifier={}", tournamentToUpdate.identifier());
                throw new TournamentNotFoundException(tournamentToUpdate.identifier());
            }

            Optional<TournamentEntity> potentiallyDuplicatedTournament = tournamentRepository.findOneByNameIgnoreCase(tournamentToUpdate.name());
            if(potentiallyDuplicatedTournament.isPresent() && !potentiallyDuplicatedTournament.get().getIdentifier().equals(tournamentToUpdate.identifier())){
                log.warn("Tournament to update with name={} already exists", tournamentToUpdate.name());
                throw new TournamentAlreadyExistsException(tournamentToUpdate.name());
            }

            existingTournament.get().setName(tournamentToUpdate.name());
            existingTournament.get().setStartDate(tournamentToUpdate.startDate());
            existingTournament.get().setEndDate(tournamentToUpdate.endDateDate());
            existingTournament.get().setPrizeMoney(tournamentToUpdate.prizeMoney());
            existingTournament.get().setCapacity(tournamentToUpdate.capacity());

            TournamentEntity updatedTournament = tournamentRepository.save(existingTournament.get());

            return this.getByIdentifier(updatedTournament.getIdentifier());
        } catch (DataAccessException e) {
            log.error("Couldn't update tournament={}", tournamentToUpdate, e);
            throw new TournamentDataRetrievalException(e);
        }
    }

    public void delete (UUID identifier){
        log.info("Invoking delete with identifier={}", identifier);
        try{
            Optional<TournamentEntity> tournamentToDelete = tournamentRepository.findOneByIdentifier(identifier);
            if(tournamentToDelete.isEmpty()){
                log.warn("Couldn't find tournament to delete with identifier={}", identifier);
                throw new TournamentNotFoundException(identifier);
            }

            tournamentRepository.delete(tournamentToDelete.get());
        } catch (DataAccessException e){
            log.error("Coudn't delete tournament with identifier={}", identifier, e);
            throw new TournamentDataRetrievalException(e);
        }
    }
















}
