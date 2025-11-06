package com.litclub.construct.interfaces.club;

import com.litclub.construct.Meeting;
import com.litclub.construct.interfaces.user.UserRecord;

import java.util.List;

public record ClubDashboard(
        ClubRecord club,
        int memberCount,
        int upcomingMeetingsCount,
        int activePromptsCount,
        List<UserRecord> recentlyActive,
        Meeting nextMeeting
) {}
