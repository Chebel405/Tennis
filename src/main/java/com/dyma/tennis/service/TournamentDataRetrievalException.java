package com.dyma.tennis.service;

import org.springframework.dao.DataAccessException;

public class TournamentDataRetrievalException extends RuntimeException {
    public TournamentDataRetrievalException(DataAccessException e) {
        super("Couldn't retrieve tournaments", e);
    }
}
