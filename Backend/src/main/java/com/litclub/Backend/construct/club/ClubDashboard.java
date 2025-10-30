package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.Meeting;

import java.util.List;

public record ClubDashboard(
        ClubRecord club,
        int memberCount,
        int upcomingMeetingsCount,
        int activePromptsCount,
        List<UserRecord> recentlyActive,
        Meeting nextMeeting
) {}
