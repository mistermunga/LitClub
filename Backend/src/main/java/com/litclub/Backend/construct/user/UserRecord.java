package com.litclub.Backend.construct.user;

import com.litclub.Backend.entity.Club;

import java.util.Set;

public record UserRecord (
        Long userID,
        String firstName,
        String surname,
        String username,
        String email,
        Set<Club> clubs
) {}
