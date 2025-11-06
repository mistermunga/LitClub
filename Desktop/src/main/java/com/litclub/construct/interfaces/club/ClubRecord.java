package com.litclub.construct.interfaces.club;

import com.litclub.construct.interfaces.user.UserRecord;

import java.util.List;
import java.util.Set;

public record ClubRecord(
        Long clubID,
        String name,
        List<UserRecord> administrators,
        Set<UserRecord> members
) {
}
