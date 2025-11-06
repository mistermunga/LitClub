package com.litclub.construct.interfaces.user;

public record UserRegistrationRecord (
        String username,
        String firstName,
        String surname,
        String email,
        String password,
        boolean isAdmin
)
{}
