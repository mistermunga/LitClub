package com.litclub.construct.interfaces.club;

import com.litclub.construct.interfaces.user.UserRecord;

public record ClubCreateRequest (
        String clubName,
        String description,
        UserRecord creator
) {
}
