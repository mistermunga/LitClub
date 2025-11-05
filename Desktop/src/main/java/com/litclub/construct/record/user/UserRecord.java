package com.litclub.construct.record.user;

import com.litclub.construct.simulacra.ClubSimulacra;

import java.util.Set;

public record UserRecord (
        Long userID,
        String firstName,
        String surname,
        String username,
        String email,
        Set<ClubSimulacra> clubs
) {}
