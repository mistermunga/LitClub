package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

public class BookNotFoundException extends EntityNotFoundException {
    public BookNotFoundException(String field, String value) {
        super("Book with " + field + " : " + value + " not found");
    }
}
