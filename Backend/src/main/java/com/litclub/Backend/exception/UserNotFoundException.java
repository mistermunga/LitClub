package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

/**
 * Thrown when a database lookup for a User does not find
 * a valid User
 */
public class UserNotFoundException extends EntityNotFoundException {

    private final String field;
    private final String value;

    public UserNotFoundException(String field, String value) {
        super("User not found with field: " + field + " and value: " + value);
        this.field = field;
        this.value = value;
    }

    public String getField() { return field; }
    public String getValue() { return value; }
}
