package com.litclub.construct.record.auth;

public record UserRegistrationRecord(
        String username,
        String firstName,
        String surname,
        String email,
        String password,
        boolean isAdmin
)
{}
