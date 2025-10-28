package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

public class MembershipNotFoundException extends EntityNotFoundException {

    public MembershipNotFoundException(String user, String club) {
        super("User " + user + " is not a member of " + club);
    }
}
