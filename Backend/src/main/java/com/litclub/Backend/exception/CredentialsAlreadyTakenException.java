package com.litclub.Backend.exception;

public class CredentialsAlreadyTakenException extends RuntimeException {
    public CredentialsAlreadyTakenException(String message) {
        super(message);
    }
}
