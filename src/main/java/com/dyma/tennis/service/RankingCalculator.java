package com.dyma.tennis.service;


import com.dyma.tennis.data.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class RankingCalculator {
    // Liste de joueurs actuels

    /**
     * Trier la liste des joueurs en base de données (en fonction du nombre de points)
     * La position dans la liste triée détermine le classement
     * Les joueurs sont mis à jour
     * Le nouveau classement est renvoyé
     */
    private final List<PlayerEntity>currentPlayersRanking;

    public RankingCalculator(List<PlayerEntity> currentPlayersRanking) {
        this.currentPlayersRanking = currentPlayersRanking;

    }
    //Nouvelle liste dans laquelle le nouveau joueur est ajouté
    public List<PlayerEntity> getNewPlayersRanking(){
        currentPlayersRanking.sort((player1, player2) -> Integer.compare(player2.getPoints(), player1.getPoints()));

        List<PlayerEntity> updatedPlayers = new ArrayList<>();

        for(int i = 0; i < currentPlayersRanking.size(); i++) {
            PlayerEntity updatedPlayer = currentPlayersRanking.get(i);
            updatedPlayer.setRank(i + 1);
            updatedPlayers.add(updatedPlayer);
        }

        return updatedPlayers;
    }
}
