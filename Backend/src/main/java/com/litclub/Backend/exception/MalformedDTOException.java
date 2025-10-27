package com.litclub.Backend.exception;

/**
 * Thrown when a Data Transfer Object is syntactically correct
 * but violates structural or field requirements.
 */

public class MalformedDTOException extends RuntimeException {
    public MalformedDTOException(String message) {
        super(message);
    }
}
