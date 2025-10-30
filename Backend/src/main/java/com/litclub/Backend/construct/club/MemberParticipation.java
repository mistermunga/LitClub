package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;

public record MemberParticipation(
        UserRecord user,
        int meetingsAttended,
        int notesPosted,
        int promptsCreated,
        boolean isActive
) {}
