package com.dyma.tennis.service;

import com.dyma.tennis.Player;
import com.dyma.tennis.PlayerList;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    //Affichage de la liste dans l'ordre de position
    public List<Player>getAllPlayers(){
        return PlayerList.ALL.stream()
                .sorted(Comparator.comparing(player -> player.rank().position()))
                .collect(Collectors.toList());
    }

    // Récupérer un joueur par son nom de famille
    public Player getByLastName(String lastName){
        return PlayerList.ALL.stream()
                .filter(player -> player.lastName().equals(lastName))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(lastName));

    }
}