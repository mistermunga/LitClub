package com.litclub.Backend.exception;

public class InsufficientPermissionsException extends RuntimeException {
    public InsufficientPermissionsException(String requiredPermission) {
        super("You lack permission " + requiredPermission);
    }
}
