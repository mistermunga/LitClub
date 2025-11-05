package com.litclub.construct.simulacra;

import com.litclub.construct.record.user.UserRecord;

import java.time.LocalDateTime;

public record ClubSimulacra (
        Long clubID,
        String clubName,
        String description,
        UserRecord creator,
        LocalDateTime createdAt
) {
}