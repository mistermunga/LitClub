package com.litclub.Backend.construct.club;

import com.litclub.Backend.construct.user.UserRecord;
import com.litclub.Backend.entity.*;

import java.time.LocalDateTime;
import java.util.List;

public record ClubActivityReport(
        ClubRecord club,
        List<Meeting> upcomingMeetings,
        List<Meeting> pastMeetings,
        List<DiscussionPrompt> activePrompts,
        List<Note> recentNotes,
        int totalMembers
) {}

