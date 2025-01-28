package com.dyma.tennis.service;

import java.time.LocalDate;

    /**
     * Exception personnalisée pour signaler qu'un joueur existe déjà dans le système.
     * Cette exception est utilisée lorsque l'on tente de créer un joueur avec les mêmes informations
     * (prénom, nom et date de naissance) qu'un joueur existant.
     */
public class PlayerAlreadyExistsException extends RuntimeException{

        /**
         * Constructeur de l'exception PlayerAlreadyExistsException.
         *
         * @param firstName Le prénom du joueur qui existe déjà.
         * @param lastName  Le nom de famille du joueur qui existe déjà.
         * @param birthDate La date de naissance du joueur qui existe déjà.
         *
         * Le message d'erreur généré inclut les informations sur le joueur concerné
         * pour faciliter le débogage ou la gestion des erreurs.
         */
    public PlayerAlreadyExistsException(String firstName, String lastName, LocalDate birthDate) {
        super("Player with  firstName " + firstName + " lastName " + lastName + " and birthDate " + birthDate + " already exists.");

    }
}
