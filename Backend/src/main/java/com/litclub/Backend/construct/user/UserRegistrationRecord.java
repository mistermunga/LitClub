package com.litclub.Backend.construct.user;

public record UserRegistrationRecord (
        String username,
        String firstName,
        String surname,
        String email,
        String password,
        boolean isAdmin
)
{}
