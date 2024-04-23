package com.dyma.tennis.service;

public class PlayerNotFoundException extends RuntimeException{
    public PlayerNotFoundException(String lastName) {
        super("Player with last name " + lastName + " couldn't be found");
    }
}
