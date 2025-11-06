package com.litclub.construct.interfaces.club;
import com.litclub.construct.DiscussionPrompt;
import com.litclub.construct.Meeting;
import com.litclub.construct.Note;

import java.util.List;

public record ClubActivityReport(
        ClubRecord club,
        List<Meeting> upcomingMeetings,
        List<Meeting> pastMeetings,
        List<DiscussionPrompt> activePrompts,
        List<Note> recentNotes,
        int totalMembers
) {}

