package com.litclub.Backend.exception;

import jakarta.persistence.EntityNotFoundException;

public class MembershipNotFoundException extends EntityNotFoundException {
    private String user;
    private String club;

    public MembershipNotFoundException(String user, String club) {
        super("User " + user + " is not a member of " + club);
        this.user = user;
        this.club = club;
    }
}
