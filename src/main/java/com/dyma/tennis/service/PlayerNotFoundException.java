package com.dyma.tennis.service;

import java.util.UUID;

public class PlayerNotFoundException extends RuntimeException{
    public PlayerNotFoundException(UUID identifier) {
        super("Player with identifier " + identifier + " couldn't be found");
    }
}
