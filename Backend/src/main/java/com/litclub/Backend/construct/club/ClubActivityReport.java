package com.litclub.Backend.construct.club;

import com.litclub.Backend.entity.*;
import java.util.List;

public record ClubActivityReport(
        ClubRecord club,
        List<Meeting> upcomingMeetings,
        List<Meeting> pastMeetings,
        List<DiscussionPrompt> activePrompts,
        List<Note> recentNotes,
        int totalMembers
) {}

