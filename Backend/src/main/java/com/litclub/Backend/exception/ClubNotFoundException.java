package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

public class ClubNotFoundException extends EntityNotFoundException {

    public ClubNotFoundException(String field, String value) {
        super("Club with" + field + " : " + value + " does not exist");
    }
}
