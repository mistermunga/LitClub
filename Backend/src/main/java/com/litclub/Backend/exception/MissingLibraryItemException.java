package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

public class MissingLibraryItemException extends EntityNotFoundException {
    public MissingLibraryItemException(String user, String book) {
        super("User " + user + " has no book " + book + "in their library");
    }
}
