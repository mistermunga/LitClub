package com.litclub.construct.interfaces.user;

import com.litclub.construct.Club;

import java.util.Set;

public record UserRecord (
        Long userID,
        String firstName,
        String surname,
        String username,
        String email,
        Set<Club> clubs
) {}
