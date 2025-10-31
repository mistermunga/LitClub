package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;

import java.util.List;
import java.util.Set;

public record ClubRecord(
        Long clubID,
        String name,
        List<UserRecord> administrators,
        Set<UserRecord> members
) {
}
