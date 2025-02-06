package com.dyma.tennis.service;

import com.dyma.tennis.data.PlayerEntity;
import com.dyma.tennis.data.PlayerRepository;
import com.dyma.tennis.data.TournamentEntity;
import com.dyma.tennis.data.TournamentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final Logger log = LoggerFactory.getLogger(RegistrationService.class);
    @Autowired
    private final TournamentRepository tournamentRepository;
    @Autowired
     private final PlayerRepository playerRepository;

    public RegistrationService(TournamentRepository tournamentRepository, PlayerRepository playerRepository) {
        this.tournamentRepository = tournamentRepository;
        this.playerRepository = playerRepository;
    }

    public void register(UUID tournamentIdentifier, UUID playerToRegister){
        Optional<TournamentEntity>existingTournament = tournamentRepository.findOneByIdentifier(tournamentIdentifier);
        if(existingTournament.isEmpty()){
            log.warn("Couldn't find tournament {} to register player", tournamentIdentifier);
            throw new TournamentRegistrationException("Tournament with identifier " + tournamentIdentifier + " doesn't exist");
        }
        if(existingTournament.get().isFull()){
            log.warn("Tournament {} is full", tournamentIdentifier);
            throw new TournamentRegistrationException("Tournament with identifier " + tournamentIdentifier + " is full");
        }
        Optional<PlayerEntity> existingPlayer = playerRepository.findOneByIdentifier(playerToRegister);
        if(existingPlayer.isEmpty()){
            log.warn("Couldn't find player {} to register", playerToRegister);
            throw new TournamentRegistrationException("Player with identifier " + playerToRegister + " doesn't exist");
        }
        if(existingTournament.get().hasPlayer(existingPlayer.get())){
            log.warn("Player {} isalready to tournament {}", playerToRegister, tournamentIdentifier);
            throw new TournamentRegistrationException("Player with identifier " + playerToRegister + " is already registered to tournament " + tournamentIdentifier);
        }

        existingPlayer.get().addTournament(existingTournament.get());
        playerRepository.save(existingPlayer.get());




    }
}
