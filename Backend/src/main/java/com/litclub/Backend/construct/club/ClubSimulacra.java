package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;

import java.time.LocalDateTime;

public record ClubSimulacra (
        Long clubID,
        String clubName,
        String description,
        UserRecord creator,
        LocalDateTime createdAt
){
}
