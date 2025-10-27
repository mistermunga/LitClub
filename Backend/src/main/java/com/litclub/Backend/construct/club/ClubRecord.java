package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;

import java.util.Set;

public record ClubRecord(
        Long clubID,
        String name,
        UserRecord administrator,
        Set<UserRecord> members
) {
}
