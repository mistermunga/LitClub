package com.litclub.construct.interfaces.club;

import com.litclub.construct.interfaces.user.UserRecord;

public record MemberParticipation(
        UserRecord user,
        int meetingsAttended,
        int notesPosted,
        int promptsCreated,
        boolean isActive
) {}
