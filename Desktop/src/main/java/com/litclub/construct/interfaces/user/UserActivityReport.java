package com.litclub.construct.interfaces.user;

import com.litclub.construct.*;

import java.util.List;

public record UserActivityReport(
        UserRecord user,
        List<Meeting> upcomingMeetings,
        List<Meeting> pastMeetings,
        List<Review> recentReviews,
        List<Note> recentNotes,
        List<DiscussionPrompt> recentPrompts
) {}

