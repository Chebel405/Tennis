package com.dyma.tennis.service;

import java.time.LocalDate;

public class PlayerAlreadyExistsException extends RuntimeException{
    public PlayerAlreadyExistsException(String firstName, String lastName, LocalDate birthDate) {
        super("Player with first name " + firstName + " and last name " + lastName + " and birth date " + birthDate + " already exists. ");
    }
}
