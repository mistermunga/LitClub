package com.litclub.Backend.exception;

/**
 * Thrown when a DTO or request object lacks an expected identifier
 * such as an ID, username, or email field.
 */

public class MissingIdentifierException extends MalformedDTOException {
    public MissingIdentifierException(String message) {
        super(message);
    }
}
