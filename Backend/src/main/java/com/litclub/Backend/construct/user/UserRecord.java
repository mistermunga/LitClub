package com.litclub.Backend.construct.user;

import com.litclub.Backend.construct.club.ClubSimulacra;

import java.util.Set;

public record UserRecord (
        Long userID,
        String firstName,
        String surname,
        String username,
        String email,
        Set<ClubSimulacra> clubs
) {}
